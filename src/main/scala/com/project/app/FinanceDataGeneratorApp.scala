package com.project.app

import com.project.ProjectConstants.{ACCOUNTS, CUSTOMERS}
import com.project.config.parser.{ConfigParser, JsonConfigParser, YamlConfigParser}
import com.project.config.{BusinessConfig, ConfigProviderFactory}
import com.project.helper.{AccountDataHelper, CustomerDataHelper}
import com.project.manager.BusinessDateDataManager
import com.project.utils.StringUtils.StringOps
import io.circe.generic.auto._
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.SparkSession

import java.time.LocalDate

object FinanceDataGeneratorApp {
  val logger: Logger = LogManager.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    Configurator.setRootLevel(Level.DEBUG)
    logger.info("Starting FinanceDataGeneratorApp...")

    if (args.length < 3) {
      logger.error("Insufficient arguments passed to application.")
      logger.info("Usage: FinanceDataGeneratorApp <provider> <location> <format> <configPath>")
      logger.info("provider: hdfs | zookeeper")
      logger.info("format: json | yaml")
      logger.info("configPath: path in HDFS or ZooKeeper")
      sys.exit(1)
    }

    val providerType = args(0).toLowerCase
    val format = args(1).toLowerCase
    val configPath = args(2)

    logger.info(s"Provider: $providerType, Format: $format, ConfigPath: $configPath")

    val parser: ConfigParser[BusinessConfig] = format match {
      case "json" =>
        logger.debug("Using JSON config parser.")
        new JsonConfigParser[BusinessConfig]
      case "yaml" | "yml" =>
        logger.debug("Using YAML config parser.")
        new YamlConfigParser[BusinessConfig]
      case unsupported =>
        logger.error(s"Unsupported config format: $unsupported")
        throw new IllegalArgumentException(s"Unsupported format: $unsupported")
    }

    logger.debug("Attempting to load business config...")
    val configOpt = ConfigProviderFactory(providerType).loadBusinessConfig[BusinessConfig](configPath, parser)

    val businessConfig = configOpt.getOrElse {
      logger.error("Could not load business configuration!")
      throw new RuntimeException("Could not load business configuration!")
    }

    val logLevel = businessConfig.loggingLevel.getOrElse("INFO").toUpperCase
    Configurator.setRootLevel(Level.toLevel(logLevel))
    logger.info(s"Log level updated to $logLevel")

    logger.info(s"Loaded business config: $businessConfig")

    val prevDate = LocalDate.parse(businessConfig.businessDate).minusDays(1).toString
    logger.info(s"Business Date: ${businessConfig.businessDate}, Previous Date: $prevDate")
    logger.info(s"Threshold: ${businessConfig.threshold}")
    logger.info(s"Generate Account Data: ${businessConfig.generateAccountData}")
//    System.exit(0)

    logger.debug("Creating Spark session...")
    val spark: SparkSession = SparkSession.builder()
      .appName("Finance Data Generator")
      .master("local[*]") // For local testing
      .enableHiveSupport()
      .getOrCreate()

    logger.debug("Spark session created successfully.")

    logger.debug("Verifying available Hive databases...")
    val df = spark.sql("SHOW DATABASES")
    df.show()
    logger.info("Hive databases listed successfully.")

    try {
      val dataManager = new BusinessDateDataManager(spark)

      logger.info("Generating customer data...")
      val customerDataBuilder = new CustomerDataHelper(spark, dataManager)
      val customerDS = customerDataBuilder.build(businessConfig.businessDate, prevDate, businessConfig.threshold.getOrElse(1000))
      logger.debug(s"Customer records count: ${customerDS.count()}")
      val customerDF = customerDS.toDF()
      val finalCustomerDF = customerDF.columns.foldLeft(customerDF) { (df, colName) =>
        df.withColumnRenamed(colName, colName.camelToSnakeCase)
      }
      finalCustomerDF.show(false)
      finalCustomerDF.printSchema()
      dataManager.writePartition(finalCustomerDF, CUSTOMERS)
      logger.info("Customer data written successfully.")

      logger.info("Generating account data...")
      val customerIds = customerDS.collect().map(_.customerId)
      val accountDataBuilder = new AccountDataHelper(spark, dataManager, customerIds)
      val accountDS = accountDataBuilder.build(businessConfig.businessDate, prevDate, businessConfig.threshold.getOrElse(1000))
      logger.debug(s"Account records count: ${accountDS.count()}")
      val accountDF = accountDS.toDF()
      val finalAccountDF = accountDF.columns.foldLeft(accountDF) { (df, colName) =>
        df.withColumnRenamed(colName, colName.camelToSnakeCase)
      }
      finalAccountDF.show(false)
      finalAccountDF.printSchema()
      dataManager.writePartition(finalAccountDF, ACCOUNTS)
      logger.info("Account data written successfully.")

      // now update the config file from which we read the business date and set the business date to next day, if we read config from HDFS
      // then we will write the updated config back to HDFS whatever file format we read be it json or yaml
      // if we read config from zookeeper then we will update the zookeeper node with new business date whatever file format we read be it json or yaml
      // we will create generalized code
      if (providerType == "hdfs") {
        logger.info("Updating business date in HDFS config file...")
        val updatedConfig = businessConfig.copy(businessDate = LocalDate.parse(businessConfig.businessDate).plusDays(1).toString)
        ConfigProviderFactory(providerType).updateBusinessConfig(configPath, updatedConfig, parser)
        logger.info("Business date updated successfully in HDFS config file.")
      } else if (providerType == "zookeeper") {
        logger.info("Updating business date in ZooKeeper node...")
        ConfigProviderFactory(providerType).updateBusinessConfig(configPath, businessConfig.copy(businessDate = LocalDate.parse(businessConfig.businessDate).plusDays(1).toString), parser)
        logger.info("Business date updated successfully in ZooKeeper node.")
      }

    } catch {
      case ex: Exception =>
        logger.error("Exception during data generation process.", ex)
        throw ex
    } finally {
      logger.info("Stopping Spark session.")
      spark.stop()
    }

    logger.info("FinanceDataGeneratorApp completed.")
  }
}
