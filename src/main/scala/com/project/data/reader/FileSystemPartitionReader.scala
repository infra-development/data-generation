package com.project.data.reader

import com.project.ProjectConstants.BUSINESS_DATE
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

/**
 * Command to read business date partition from filesystem
 */
class FileSystemPartitionReader(spark: SparkSession,  format: String = "parquet") extends BusinessDataReader {
  private val logger: Logger = LogManager.getLogger(getClass)

  override def readPartition(businessDate: String, basePath: String): DataFrame = {
    val partitionPath = s"$basePath/$BUSINESS_DATE=$businessDate"
    logger.debug(s"Reading partition from filesystem: $partitionPath, format: $format")

    Try {
      val df = spark.read.format(format).load(partitionPath)
      logger.info(s"Successfully loaded partition from filesystem path '$partitionPath' for businessDate '$businessDate'")
      df
    } match {
      case Success(df) => df
      case Failure(exception) =>
        logger.warn(s"Failed to load partition from filesystem path '$partitionPath' for businessDate '$businessDate'. Returning empty DataFrame. Reason: ${exception.getMessage}")
        spark.emptyDataFrame
    }
  }
}
