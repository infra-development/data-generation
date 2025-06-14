package com.project.manager

import com.project.ProjectConstants.BUSINESS_DATE
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

/**
 * Handles partitioned data loading and saving for business-date based incremental data pipelines.
 * Generalized for any entity/table.
 */
class BusinessDateDataManager(spark: SparkSession) {

  private val logger: Logger = LogManager.getLogger(getClass)

  /**
   * Loads data as a DataFrame for a given table and business date partition.
   * @param table Name of the table (without db prefix)
   * @param businessDate The business date partition to load (format: yyyy-MM-dd)
   * @return DataFrame (maybe empty)
   */
  def loadPartition(table: String, businessDate: String): DataFrame = {
    val fullTable = s"$table"
    logger.debug(s"Attempting to load partition for table: $fullTable, businessDate: $businessDate")

    Try {
      val df = spark.read.table(fullTable).filter(col(BUSINESS_DATE) === businessDate)
      logger.info(s"Successfully loaded partition from table '$fullTable' for businessDate '$businessDate'")
      df
    } match {
      case Success(df) => df
      case Failure(exception) =>
        logger.warn(s"Failed to load partition from table '$fullTable' for businessDate '$businessDate'. Returning empty DataFrame. Reason: ${exception.getMessage}")
        spark.emptyDataFrame
    }
  }

  /**
   * Writes a DataFrame to a partitioned Hive table for the given business date.
   * @param df DataFrame to write (must contain a "business_date" column)
   * @param table Name of the table (without db prefix)
   */
  def writePartition(df: DataFrame, table: String): Unit = {
    logger.info(s"Writing data to table '$table' with overwrite mode.")
    logger.debug(s"Schema being written to '$table': ${df.schema.treeString}")

    Try {
      df.write
        .mode("overwrite")
        .insertInto(s"$table")
    } match {
      case Success(_) =>
        logger.info(s"Successfully wrote data to table '$table'.")
      case Failure(exception) =>
        logger.error(s"Failed to write data to table '$table'. Reason: ${exception.getMessage}", exception)
        throw exception
    }
  }
}
