package com.project.config.provider

import com.project.config.BusinessConfig
import com.project.config.parser.ConfigParser
import io.circe.Decoder
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

import scala.util.Try

class ZookeeperConfigProvider extends ConfigProvider {

  private val logger: Logger = LogManager.getLogger(this.getClass)

  private val watcher = new Watcher {
    override def process(event: WatchedEvent): Unit = {
      logger.debug(s"ZooKeeper event: ${event.getType} on path: ${event.getPath}")
    }
  }

  private val zk = new ZooKeeper("localhost:2181", 2000, watcher)

  override def loadBusinessConfig[T](path: String, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Either[Throwable, T] = {
    logger.debug(s"Attempting to read config from ZooKeeper path: $path")

    for {
      // Step 1: Read raw bytes from ZooKeeper
      data <- Try(zk.getData(path, false, null)).toEither.left.map { ex =>
        logger.error(s"Failed to read data from ZooKeeper path: $path", ex)
        ex
      }

      // Step 2: Convert bytes to UTF-8 string
      content <- Try(new String(data, "UTF-8")).toEither.left.map { ex =>
        logger.error(s"Failed to decode config content to UTF-8 at path: $path", ex)
        ex
      }

      _ = logger.debug("Config content fetched successfully. Attempting to parse...")

      // Step 3: Parse JSON/YAML content using the provided parser
      parsed <- parser.parse(content).left.map { err =>
        logger.error(s"Failed to parse config content at path: $path", err)
        err
      }

      _ = logger.info(s"Successfully parsed config from ZooKeeper path: $path")
    } yield parsed

  }

  override def updateBusinessConfig[T](path: String, updatedConfig: BusinessConfig, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Either[Throwable, T] = {
    for {
      // Step 1: Read existing config data
      data <- Try(zk.getData(path, false, null)).toEither.left.map { ex =>
        logger.error(s"Failed to read config data from ZooKeeper path: $path", ex)
        ex
      }

      // Step 2: Convert to String
      content <- Try(new String(data, "UTF-8")).toEither.left.map { ex =>
        logger.error(s"Failed to convert ZooKeeper data to string at path: $path", ex)
        ex
      }

      // Step 3: Parse existing config
      parsed <- parser.parse(content).left.map { err =>
        logger.error(s"Failed to parse existing config at path: $path", err)
        err
      }

      // Step 4: Validate and update config
      updated <- parsed match {
        case config: BusinessConfig =>
          logger.info(s"Updating businessDate in config at path: $path")
          Right(config.copy(businessDate = updatedConfig.businessDate).asInstanceOf[T])
        case _ =>
          val err = new IllegalStateException("Parsed config is not of type BusinessConfig")
          logger.error(err.getMessage)
          Left(err)
      }

      // Step 5: Serialize updated config
      serialized <- parser.asInstanceOf[ConfigParser[BusinessConfig]]
        .serialize(updated.asInstanceOf[BusinessConfig])
        .left.map { err =>
          logger.error(s"Failed to serialize updated config at path: $path", err)
          err
        }

      // Step 6: Write updated config back to ZooKeeper
      _ <- Try(zk.setData(path, serialized.getBytes("UTF-8"), -1)).toEither.left.map { ex =>
        logger.error(s"Failed to write updated config to ZooKeeper at path: $path", ex)
        ex
      }

      _ = logger.info(s"Successfully updated config at ZooKeeper path: $path")
    } yield updated
  }
}
