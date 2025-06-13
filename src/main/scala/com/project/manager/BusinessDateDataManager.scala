package com.project.manager

import com.project.ProjectConstants.BUSINESS_DATE
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Try

/**
 * Handles partitioned data loading and saving for business-date based incremental data pipelines.
 * Generalized for any entity/table.
 */
class BusinessDateDataManager(spark: SparkSession) {

  /**
   * Loads data as a DataFrame for a given table and business date partition.
   * @param table Name of the table (without db prefix)
   * @param businessDate The business date partition to load (format: yyyy-MM-dd)
   * @return DataFrame (maybe empty)
   */
  def loadPartition(table: String, businessDate: String): DataFrame = {
    val fullTable = s"$table"
    Try {
      spark.read.table(fullTable).filter(col(BUSINESS_DATE) === businessDate)
    }.getOrElse(spark.emptyDataFrame)
  }

  /**
   * Writes a DataFrame to a partitioned Hive table for the given business date.
   * @param df DataFrame to write (must contain a "business_date" column)
   * @param table Name of the table (without db prefix)
   */
  def writePartition(df: DataFrame, table: String): Unit = {
    df.write
      .mode("overwrite")
      .insertInto(s"$table")
  }
}