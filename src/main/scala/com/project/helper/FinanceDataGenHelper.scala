package com.project.helper

import com.project.app.FinanceDataGeneratorApp.logger
import org.apache.spark.sql.SparkSession

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

}
