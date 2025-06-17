package com.project.app

import com.project.ProjectConstants.{ACCOUNTS, CUSTOMERS, DEFAULT_THRESHOLD}
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

    logger.debug("Attempting to load business config...")

    val businessConfigResult: Either[Throwable, BusinessConfig] = for {
      parser <- ConfigParserFactory[BusinessConfig](format)
      provider = ConfigProviderFactory(providerType)
      config <- provider.loadBusinessConfig[BusinessConfig](configPath, parser)
    } yield config

    val businessConfig = businessConfigResult match {
      case Right(config) =>
        logger.info("Business config loaded successfully.")
        config

      case Left(error) =>
        logger.error(s"Failed to load business config: ${error.getMessage}", error)
        throw new RuntimeException("Business config load failed", error)
    }
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
      val customerDS = customerDataHelper.build(businessConfig.businessDate, prevDate, businessConfig.threshold.getOrElse(DEFAULT_THRESHOLD))
      logger.debug(s"Customer records count: ${customerDS.count()}")

      customerDataHelper.writeCustomerData(customerDS)

      logger.info("Generating account data...")
      val customerIds = customerDS.collect().map(_.customer_id)
      val accountDataHelper = new AccountDataHelper(spark, tableDataReader, tableDataWriter, customerIds)
      val accountDS = accountDataHelper.build(businessConfig.businessDate, prevDate, businessConfig.threshold.getOrElse(DEFAULT_THRESHOLD))

      accountDataHelper.writeAccountData(accountDS)

      val result: Either[Throwable, BusinessConfig] = for {
        parser <- ConfigParserFactory[BusinessConfig](format) // Ensures parser is typed correctly
        provider = ConfigProviderFactory(providerType)
        loadedConfig <- provider.loadBusinessConfig(configPath, parser)
        updatedConfig = loadedConfig.copy(
          businessDate = LocalDate.parse(loadedConfig.businessDate).plusDays(1).toString
        )
        savedConfig <- provider.updateBusinessConfig(configPath, updatedConfig, parser)
      } yield savedConfig

      result match {
        case Right(updatedConfig) =>
          logger.info(s"Business config updated successfully: $updatedConfig")
        case Left(error) =>
          logger.error(s"Failed to update business config: ${error.getMessage}", error)
          throw new RuntimeException("Business config update failed", error)
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
