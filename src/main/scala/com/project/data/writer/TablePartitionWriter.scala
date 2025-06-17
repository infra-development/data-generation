package com.project.data.writer

import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.DataFrame

import scala.util.{Failure, Success, Try}

class TablePartitionWriter(mode: String = "overwrite") extends BusinessDataWriter {
  private val logger: Logger = LogManager.getLogger(getClass)

  override def writePartition(df: DataFrame, table: String): Unit = {
    logger.info(s"Writing partition data to table '$table' with mode '$mode'")
    logger.debug(s"Schema being written to '$table': ${df.schema.treeString}")

    Try {
      df.write.mode(mode).insertInto(table)
    } match {
      case Success(_) =>
        logger.info(s"Successfully wrote partition data to table '$table'")
      case Failure(exception) =>
        logger.error(s"Failed to write partition data to table '$table'. Reason: ${exception.getMessage}", exception)
        throw exception
    }
  }
}
