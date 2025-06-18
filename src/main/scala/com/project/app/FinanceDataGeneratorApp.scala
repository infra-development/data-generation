package com.project.app

import com.project.ProjectConstants.DEFAULT_THRESHOLD
import com.project.config.{BusinessConfig, ConfigUpdater}
import com.project.config.parser.ConfigParserFactory
import com.project.config.provider.ConfigProviderFactory
import io.circe.generic.auto._
import com.project.factory.ObjectCreationFactory
import com.project.helper.{AccountDataHelper, CustomerDataHelper, FinanceDataGenHelper}
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.{Level, LogManager, Logger}

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

    val businessConfig = FinanceDataGenHelper.loadConfigs(format, providerType, configPath)
    FinanceDataGenHelper.logConfigs(businessConfig)

    val prevDate = LocalDate.parse(businessConfig.businessDate).minusDays(1).toString
    logger.info(s"Business Date: ${businessConfig.businessDate}, Previous Date: $prevDate")

    val logLevel = businessConfig.loggingLevel.getOrElse("INFO").toUpperCase
    Configurator.setRootLevel(Level.toLevel(logLevel))
    logger.info(s"Log level updated to $logLevel")

    val spark = FinanceDataGenHelper.createSparkSession()

    FinanceDataGenHelper.verifyHiveConnection(spark)

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
    } catch {
      case ex: Exception =>
        logger.error("Exception during data generation process.", ex)
        throw ex
    }

    val updater = new ConfigUpdater[BusinessConfig]()
    val newBusinessDate = LocalDate.parse(businessConfig.businessDate).plusDays(1).toString
    val result = updater.propagateConfigFrom(sourceProviderType = providerType, sourceFormat = format, sourcePath = configPath)
//    FinanceDataGenHelper.updateConfigs(format, providerType, configPath)
    logger.info("FinanceDataGeneratorApp completed.")
  }
}
