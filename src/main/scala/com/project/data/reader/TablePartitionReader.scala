package com.project.data.reader

import com.project.ProjectConstants.BUSINESS_DATE
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

/**
 * Command to read business date partition from table
 */
class TablePartitionReader(spark: SparkSession) extends BusinessDataReader {
  private val logger: Logger = LogManager.getLogger(getClass)

  override def readPartition(businessDate: String, table: String): DataFrame = {
    logger.debug(s"Reading partition from table: $table, businessDate: $businessDate")

    Try {
      val df = spark.read.table(table).filter(col(BUSINESS_DATE) === businessDate)
      logger.info(s"Successfully loaded partition from table '$table' for businessDate '$businessDate'")
      df
    } match {
      case Success(df) => df
      case Failure(exception) =>
        logger.warn(s"Failed to load partition from table '$table' for businessDate '$businessDate'. Returning empty DataFrame. Reason: ${exception.getMessage}")
        spark.emptyDataFrame
    }
  }
}

