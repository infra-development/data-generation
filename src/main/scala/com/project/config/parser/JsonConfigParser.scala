package com.project.config.parser

import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.generic.auto._
import org.apache.logging.log4j.{LogManager, Logger}

import scala.util.{Failure, Success, Try}

class JsonConfigParser[T: Decoder : Encoder] extends ConfigParser[T] {
  private val logger: Logger = LogManager.getLogger(this.getClass)

  override def parse(content: String): Either[Throwable, T] = {
    logger.debug("Parsing JSON config")

    decode[T](content) match {
      case Right(parsed) =>
        logger.info("JSON parsing succeeded.")
        Right(parsed)
      case Left(error) =>
        logger.error(s"JSON parsing failed: $error")
        Left(error)
    }
  }


  def serialize(config: T): Either[Throwable, String] = {
    logger.debug(s"Serializing config of type: ${config.getClass.getSimpleName}")

    Try(config.asJson.spaces2) match {
      case Success(result) =>
        logger.info("Config serialization completed successfully.")
        Right(result)
      case Failure(ex) =>
        logger.error("Failed to serialize config", ex)
        Left(ex)
    }
  }
}
