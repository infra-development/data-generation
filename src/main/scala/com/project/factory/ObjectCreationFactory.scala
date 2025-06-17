package com.project.factory

import com.project.data.reader.{BusinessDataReader, FileSystemPartitionReader, TablePartitionReader}
import com.project.data.writer.{BusinessDataWriter, FileSystemPartitionWriter, TablePartitionWriter}
import org.apache.spark.sql.SparkSession

/**
 * Factory to create business date partition reader and writer commands
 */
object ObjectCreationFactory {

  // Reader factory methods
  def createTablePartitionReader(spark: SparkSession): BusinessDataReader =
    new TablePartitionReader(spark)

  def createFileSystemPartitionReader(spark: SparkSession, format: String = "parquet"): BusinessDataReader =
    new FileSystemPartitionReader(spark, format)

  // Writer factory methods
  def createTablePartitionWriter(mode: String = "overwrite"): BusinessDataWriter =
    new TablePartitionWriter(mode)

  def createFileSystemPartitionWriter(format: String = "parquet", mode: String = "overwrite"): BusinessDataWriter =
    new FileSystemPartitionWriter(format, mode)
}
