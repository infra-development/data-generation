package com.project.config.parser

import io.circe.parser.decode
import org.apache.logging.log4j.{LogManager, Logger}

class JsonConfigParser[T: io.circe.Decoder] extends ConfigParser[T] {
  private val logger: Logger = LogManager.getLogger(this.getClass)

  override def parse(content: String): Option[T] = {
    logger.debug("Attempting to parse JSON content.")
    decode[T](content) match {
      case Right(parsed) =>
        logger.debug("Successfully parsed JSON content into object.")
        Some(parsed)
      case Left(error) =>
        logger.error(s"Failed to parse JSON content. Error: ${error.getMessage}")
        None
    }
  }
}
