package com.project.data.reader

import org.apache.spark.sql.DataFrame

/**
 * Command interface for business date partition reading
 */
trait BusinessDataReader {
  def readPartition(businessDate: String, tableOrPath: String): DataFrame
}
