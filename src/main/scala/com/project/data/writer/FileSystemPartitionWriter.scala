package com.project.data.writer

import com.project.ProjectConstants.BUSINESS_DATE
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

/**
 * Command to write business date partition to filesystem
 */
class FileSystemPartitionWriter (format: String = "parquet", mode: String = "overwrite") extends BusinessDataWriter {
  private val logger: Logger = LogManager.getLogger(getClass)

  override def writePartition(df: DataFrame, basePath: String): Unit = {
    logger.info(s"Writing partition data to filesystem path '$basePath' with format '$format' and mode '$mode'")
    logger.debug(s"Schema being written to filesystem: ${df.schema.treeString}")

    Try {
      df.write
        .mode(mode)
        .format(format)
        .partitionBy(BUSINESS_DATE)
        .save(basePath)
    } match {
      case Success(_) =>
        logger.info(s"Successfully wrote partition data to filesystem path '$basePath'")
      case Failure(exception) =>
        logger.error(s"Failed to write partition data to filesystem path '$basePath'. Reason: ${exception.getMessage}", exception)
        throw exception
    }
  }

}