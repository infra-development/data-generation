package com.project.config.parser

import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.yaml.{Printer, parser}
import org.apache.logging.log4j.{LogManager, Logger}

class YamlConfigParser[T: Decoder : Encoder] extends ConfigParser[T] {
  private val logger: Logger = LogManager.getLogger(this.getClass)

  override def parse(content: String): Either[Throwable, T] = {
    logger.debug("Attempting to parse YAML content.")

    parser.parse(content) match {
      case Left(parseError) =>
        logger.error(s"YAML parse error: ${parseError.getMessage}")
        Left(parseError)

      case Right(json) =>
        logger.debug("YAML parsed successfully. Attempting to decode to target type.")
        json.as[T] match {
          case Left(decodingError) =>
            logger.error(s"YAML decoding error: ${decodingError.getMessage}")
            Left(decodingError)
          case Right(obj) =>
            logger.debug("Successfully decoded YAML content to object.")
            Right(obj)
        }
    }
  }

  override def serialize(config: T): Either[Throwable, String] = {
    logger.debug("Serializing config of type: {}", config.getClass.getSimpleName)

    val jsonResult = config.asJson
    logger.debug("Successfully converted config to JSON")

    val result = Printer.spaces2.pretty(jsonResult)
    logger.info("Config serialization completed successfully")

    Right(result)
  }
}
