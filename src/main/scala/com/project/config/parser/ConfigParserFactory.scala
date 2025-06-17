package com.project.config.parser

import com.project.config.BusinessConfig
import io.circe.generic.auto._
import org.apache.logging.log4j.{LogManager, Logger}

object ConfigParserFactory {
  val logger: Logger = LogManager.getLogger(this.getClass)
  /**
   * Returns a ConfigParser based on the specified format.
   *
   * @param format The format of the configuration file (e.g., "json", "yaml").
   * @return An instance of ConfigParser for the specified format.
   * @throws IllegalArgumentException if the format is unsupported.
   */
  def apply(format: String): ConfigParser[BusinessConfig] = {
    val parser: ConfigParser[BusinessConfig] = format match {
      case "json" =>
        logger.debug("Using JSON config parser.")
        new JsonConfigParser[BusinessConfig]
      case "yaml" | "yml" =>
        logger.debug("Using YAML config parser.")
        new YamlConfigParser[BusinessConfig]
      case unsupported =>
        logger.error(s"Unsupported config format: $unsupported")
        throw new IllegalArgumentException(s"Unsupported format: $unsupported")
    }
    parser
  }


}
