package com.project.helper

import com.project.app.FinanceDataGeneratorApp.logger
import com.project.config.BusinessConfig
import com.project.config.parser.ConfigParserFactory
import com.project.config.provider.ConfigProviderFactory
import org.apache.spark.sql.SparkSession
import io.circe.generic.auto._


import java.time.LocalDate

object FinanceDataGenHelper {
  def createSparkSession(): SparkSession = {
    logger.debug("Creating Spark session...")
    val spark: SparkSession = SparkSession.builder()
      .appName("Finance Data Generator")
      .master("local[*]") // For local testing
      .enableHiveSupport()
      .getOrCreate()

    logger.debug("Spark session created successfully.")
    spark
  }

  def logConfigs(businessConfig: BusinessConfig): Unit = {
    logger.info(s"Business Date: ${businessConfig.businessDate}")
    logger.info(s"Threshold: ${businessConfig.threshold}")
    logger.info(s"Generate Account Data: ${businessConfig.generateAccountData}")
  }

  def loadConfigs(format: String, providerType: String, configPath: String): BusinessConfig = {
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
    businessConfig
  }

  /**
   * Verifies the connection to the Hive metastore by executing a simple query.
   *
   * @param spark The SparkSession to use for the connection.
   * @return True if the connection is successful, false otherwise.
   */
  def verifyHiveConnection(spark: SparkSession): Boolean = {
    try {
      logger.debug("Verifying Hive metastore connection using 'SHOW DATABASES'...")
      val databases = spark.sql("SHOW DATABASES").collect()
      logger.info(s"Hive connection verified. Found ${databases.length} databases.")
      true
    } catch {
      case ex: Exception =>
        logger.error("Failed to connect to Hive metastore.", ex)
        false
    }
  }


}
