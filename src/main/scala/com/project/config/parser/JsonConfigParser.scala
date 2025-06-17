package com.project.config.parser

import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.generic.auto._
import org.apache.logging.log4j.{LogManager, Logger}

class JsonConfigParser[T: Decoder : Encoder] extends ConfigParser[T] {
  private val logger: Logger = LogManager.getLogger(this.getClass)

  override def parse[T: Decoder](content: String): Option[T] = {
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

  override def serialize(config: T): String = {
    logger.debug("Serializing config of type: {}", config.getClass.getSimpleName)

    try {
      val result = config.asJson.spaces2
      logger.info("Config serialization completed successfully")
      result
    } catch {
      case ex: Exception =>
        logger.error("Failed to serialize config", ex)
        throw ex
    }
  }
}
