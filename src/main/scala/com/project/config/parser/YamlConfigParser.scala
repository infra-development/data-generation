package com.project.config.parser

import io.circe.Decoder
import io.circe.yaml.parser
import org.apache.logging.log4j.{LogManager, Logger}

class YamlConfigParser[T: Decoder] extends ConfigParser[T] {
  private val logger: Logger = LogManager.getLogger(this.getClass)

  override def parse(content: String): Option[T] = {
    logger.debug("Attempting to parse YAML content.")

    parser.parse(content) match {
      case Left(parseError) =>
        logger.error(s"YAML parse error: ${parseError.getMessage}")
        None

      case Right(json) =>
        logger.debug("YAML parsed successfully. Attempting to decode to target type.")
        json.as[T] match {
          case Left(decodingError) =>
            logger.error(s"YAML decoding error: ${decodingError.getMessage}")
            None
          case Right(obj) =>
            logger.debug("Successfully decoded YAML content to object.")
            Some(obj)
        }
    }
  }
}
