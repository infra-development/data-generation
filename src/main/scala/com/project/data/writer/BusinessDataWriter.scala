package com.project.data.writer

import org.apache.spark.sql.DataFrame

/**
 * Command interface for business date partition writing
 */
trait BusinessDataWriter {
  def writePartition(df: DataFrame, tableOrPath: String): Unit
}
