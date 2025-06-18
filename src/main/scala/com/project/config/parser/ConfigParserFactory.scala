package com.project.config.parser

import io.circe.{Decoder, Encoder}
import org.apache.logging.log4j.{LogManager, Logger}

import scala.util.control.NonFatal

object ConfigParserFactory {
  val logger: Logger = LogManager.getLogger(this.getClass)
  /**
   * Returns a ConfigParser based on the specified format.
   *
   * @param format The format of the configuration file (e.g., "json", "yaml").
   * @return An instance of ConfigParser for the specified format.
   * @throws IllegalArgumentException if the format is unsupported.
   */
  def apply[T](format: String)(implicit decoder: Decoder[T], encoder: Encoder[T]): Either[Throwable, ConfigParser[T]] = {
    try {
      val parser: ConfigParser[T] = format.trim.toLowerCase match {
        case "json" =>
          logger.debug("Using JSON config parser.")
          new JsonConfigParser[T]
        case "yaml" | "yml" =>
          logger.debug("Using YAML config parser.")
          new YamlConfigParser[T]
        case unsupported =>
          val msg = s"Unsupported config format: $unsupported"
          logger.error(msg)
          throw new IllegalArgumentException(msg)
      }
      Right(parser)
    } catch {
      case NonFatal(ex) =>
        logger.error(s"Failed to create config parser for format '$format'", ex)
        Left(ex)
    }
  }

}
