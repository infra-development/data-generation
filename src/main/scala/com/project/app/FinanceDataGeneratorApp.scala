package com.project.app

import com.project.ProjectConstants.{ACCOUNTS, CUSTOMERS}
import com.project.config.parser.{ConfigParser, ConfigParserFactory, JsonConfigParser, YamlConfigParser}
import com.project.config.BusinessConfig
import com.project.config.provider.ConfigProviderFactory
import com.project.factory.ObjectCreationFactory
import com.project.helper.{AccountDataHelper, CustomerDataHelper, FinanceDataGenHelper}
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

    val parser = ConfigParserFactory(format)
    logger.debug("Attempting to load business config...")
    val businessConfig = ConfigProviderFactory(providerType).loadBusinessConfig[BusinessConfig](configPath, parser)
    logger.info(s"Loaded business config: $businessConfig")

    val prevDate = LocalDate.parse(businessConfig.businessDate).minusDays(1).toString
    logger.info(s"Business Date: ${businessConfig.businessDate}, Previous Date: $prevDate")
    logger.info(s"Threshold: ${businessConfig.threshold}")
    logger.info(s"Generate Account Data: ${businessConfig.generateAccountData}")

    val logLevel = businessConfig.loggingLevel.getOrElse("INFO").toUpperCase
    Configurator.setRootLevel(Level.toLevel(logLevel))
    logger.info(s"Log level updated to $logLevel")

    val spark = FinanceDataGenHelper.createSparkSession()


    logger.debug("Verifying available Hive databases...")
    val df = spark.sql("SHOW DATABASES")
    df.show()
    logger.info("Hive databases listed successfully.")

    try {
      val tableDataReader = ObjectCreationFactory.createTablePartitionReader(spark)
      val tableDataWriter = ObjectCreationFactory.createTablePartitionWriter()

      logger.info("Generating customer data...")
      val customerDataHelper = new CustomerDataHelper(spark, tableDataReader, tableDataWriter)
      val customerDS = customerDataHelper.build(businessConfig.businessDate, prevDate, businessConfig.threshold.getOrElse(1000))
      logger.debug(s"Customer records count: ${customerDS.count()}")

      customerDataHelper.writeCustomerData(customerDS)

      logger.info("Generating account data...")
      val customerIds = customerDS.collect().map(_.customer_id)
      val accountDataHelper = new AccountDataHelper(spark, tableDataReader, tableDataWriter, customerIds)
      val accountDS = accountDataHelper.build(businessConfig.businessDate, prevDate, businessConfig.threshold.getOrElse(1000))

      accountDataHelper.writeAccountData(accountDS)


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
