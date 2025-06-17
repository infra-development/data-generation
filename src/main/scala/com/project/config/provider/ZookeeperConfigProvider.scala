package com.project.config.provider

import com.project.config.BusinessConfig
import io.circe.generic.auto._
import io.circe.Decoder
import com.project.config.parser.ConfigParser
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

class ZookeeperConfigProvider extends ConfigProvider {

  private val logger: Logger = LogManager.getLogger(this.getClass)

  private val watcher = new Watcher {
    override def process(event: WatchedEvent): Unit = {
      logger.debug(s"ZooKeeper event: ${event.getType} on path: ${event.getPath}")
    }
  }

  private val zk = new ZooKeeper("localhost:2181", 2000, watcher)

  override def loadBusinessConfig[T](path: String, parser: ConfigParser[T])(implicit decoder: Decoder[T]): T = {
    val configOpt = try {
      logger.debug(s"Attempting to read config from ZooKeeper path: $path")
      val data = zk.getData(path, false, null)
      val content = new String(data, "UTF-8")

      logger.debug("Config content fetched. Attempting to parse...")
      val parsed = parser.parse[T](content)

      parsed match {
        case Some(_) =>
          logger.info(s"Successfully parsed config from ZooKeeper path: $path")
        case None =>
          logger.error(s"Failed to parse config content from ZooKeeper path: $path")
      }
      parsed
    } catch {
      case ex: Exception =>
        logger.error(s"Exception while reading config from ZooKeeper path: $path", ex)
        None
    }
    val businessConfig: T = configOpt.getOrElse {
      logger.error("Could not load business configuration from ZooKeeper!")
      throw new RuntimeException("Could not load business configuration from ZooKeeper!")
    }
    businessConfig

  }

  override def updateBusinessConfig[T](path: String, updatedConfig: BusinessConfig, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Option[T] = {
    try {
      logger.debug(s"Reading existing config from ZooKeeper path: $path")
      val data = zk.getData(path, false, null)
      val content = new String(data, "UTF-8")

      parser.parse[T](content).map {
        case config: BusinessConfig =>
          logger.info(s"Updating businessDate in ZooKeeper path: $path")
          val updated = config.copy(businessDate = updatedConfig.businessDate)
          val serialized = parser.asInstanceOf[ConfigParser[BusinessConfig]].serialize(updated)
          zk.setData(path, serialized.getBytes("UTF-8"), -1)
          logger.info(s"Successfully updated businessDate in ZooKeeper path: $path")
          updated.asInstanceOf[T]

        case _ =>
          val msg = "Parsed config is not of type BusinessConfig"
          logger.error(msg)
          throw new IllegalStateException(msg)
      }
    } catch {
      case ex: Exception =>
        logger.error(s"Exception while updating config in ZooKeeper path: $path", ex)
        None
    }
  }
}
