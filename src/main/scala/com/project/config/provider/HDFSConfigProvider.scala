package com.project.config.provider

import com.project.config.BusinessConfig
import com.project.config.parser.ConfigParser
import io.circe.Decoder
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.logging.log4j.LogManager

import scala.util.Try


class HDFSConfigProvider extends ConfigProvider {

  private val logger = LogManager.getLogger(this.getClass)
  private val conf = new Configuration()
  private val fs = FileSystem.get(conf)


  override def loadBusinessConfig[T](path: String, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Either[Throwable, T] = {
    val hdfsPath = new Path(path)
    logger.debug(s"Checking if config exists at: $hdfsPath")

    if (!fs.exists(hdfsPath)) {
      val msg = s"Config file does not exist at: $hdfsPath"
      logger.error(msg)
      Left(new RuntimeException(msg))
    } else {
      Try {
        val inputStream = fs.open(hdfsPath)
        val content = scala.io.Source.fromInputStream(inputStream).mkString
        inputStream.close()
        logger.debug("Config content read successfully.")
        content
      }.toEither
        .flatMap(parser.parse) // this returns Either[Throwable, T]
        .left.map { err =>
          logger.error(s"Failed to load or parse config: ${err.getMessage}")
          err
        }
        .map { config =>
          logger.info("Config parsed and loaded successfully.")
          config
        }
    }
  }

  override def updateBusinessConfig[T](path: String, updatedConfig: T, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Either[Throwable, T] = {
    val hdfsPath = new Path(path)
    logger.debug(s"Checking if HDFS path exists: $hdfsPath")

    if (!fs.exists(hdfsPath)) {
      val err = new RuntimeException(s"Config file not found at: $hdfsPath")
      logger.error(err.getMessage)
      return Left(err)
    }

    logger.info(s"Config file found at: $hdfsPath. Writing updated config...")

    for {
      serialized <- parser.serialize(updatedConfig).left.map { err =>
        logger.error(s"Failed to serialize config: ${err.getMessage}")
        err
      }

      _ <- Try {
        val outputStream = fs.create(hdfsPath, true)
        outputStream.write(serialized.getBytes("UTF-8"))
        outputStream.close()
        logger.info(s"Successfully wrote updated config to: $hdfsPath")
      }.toEither.left.map { ioErr =>
        logger.error(s"Failed to write config file: ${ioErr.getMessage}")
        ioErr
      }

    } yield updatedConfig
  }


}