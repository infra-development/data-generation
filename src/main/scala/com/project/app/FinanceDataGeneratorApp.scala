package com.project.app

import com.project.ProjectConstants.{ACCOUNTS, CUSTOMERS, FINANCE_DB}
import com.project.config.parser.{ConfigParser, JsonConfigParser, YamlConfigParser}
import com.project.config.provider.{HDFSConfigProvider, ZookeeperConfigProvider}
import com.project.config.{BusinessConfig, ConfigProviderFactory}
import com.project.helper.{AccountDataHelper, CustomerDataHelper}
import com.project.manager.BusinessDateDataManager
import io.circe.generic.auto._

import java.time.LocalDate


object FinanceDataGeneratorApp {
  def main(args: Array[String]): Unit = {

    if (args.length < 4) {
      println("Usage: FinanceDataGeneratorApp <provider> <location> <format> <configPath>")
      println("provider: hdfs | zookeeper")
      println("location: e.g. hdfs://host:port or zk-host:port")
      println("format: json | yaml")
      println("configPath: path in HDFS or ZooKeeper")
      sys.exit(1)
    }

    val providerType = args(0).toLowerCase
    val location = args(1)
    val format = args(2).toLowerCase
    val configPath = args(3)

    val parser: ConfigParser[BusinessConfig] = format match {
      case "json" => new JsonConfigParser[BusinessConfig]
      case "yaml" => new YamlConfigParser[BusinessConfig]
      case _ => throw new IllegalArgumentException("Unsupported format: " + format)
    }

    val configOpt: Option[BusinessConfig] = providerType match {
      case "hdfs" =>
        val provider = new HDFSConfigProvider(location)
        provider.loadBusinessConfig(configPath, parser)
      case "zookeeper" =>
        val provider = new ZookeeperConfigProvider(location)
        provider.loadBusinessConfig(configPath, parser)
      case _ =>
        throw new IllegalArgumentException("Unsupported provider: " + providerType)
    }

    val businessConfig = configOpt.getOrElse {
      throw new RuntimeException("Could not load business configuration!")
    }

    val prevDate = LocalDate.parse(businessConfig.businessDate).minusDays(1).toString

    val spark = org.apache.spark.sql.SparkSession.builder()
      .appName("Finance Data Generator")
      .master("local[*]")
      .enableHiveSupport()// Use local mode for testing
      .getOrCreate()

    val df = spark.sql("SHOW DATABASES")
    df.show() // Display existing databases for verification

    val dataManager = new BusinessDateDataManager(spark)

    // Generate customer data
    val customerDataBuilder = new CustomerDataHelper(spark, dataManager)
    val customerDS = customerDataBuilder.build(businessConfig.businessDate, prevDate)
    dataManager.writePartition(customerDS.toDF(), CUSTOMERS)

    // Generate account data
    val accountDataBuilder = new AccountDataHelper(spark, dataManager, customerDS.collect().map(_.customerId))
    val accountDS = accountDataBuilder.build(businessConfig.businessDate, prevDate)
    dataManager.writePartition(accountDS.toDF(), ACCOUNTS)

    spark.stop()

  }

}
