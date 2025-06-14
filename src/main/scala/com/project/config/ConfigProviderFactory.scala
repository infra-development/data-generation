package com.project.config

import com.project.config.provider.{ConfigProvider, HDFSConfigProvider, ZookeeperConfigProvider}
import org.apache.logging.log4j.{LogManager, Logger}

object ConfigProviderFactory {
  private val logger: Logger = LogManager.getLogger(this.getClass)

  def apply(sourceType: String): ConfigProvider = {
    val lowerSource = sourceType.toLowerCase
    logger.debug(s"Creating ConfigProvider for source type: $lowerSource")

    lowerSource match {
      case "zookeeper" =>
        logger.debug("Instantiating ZookeeperConfigProvider.")
        new ZookeeperConfigProvider()
      case "hdfs" =>
        logger.debug("Instantiating HDFSConfigProvider.")
        new HDFSConfigProvider()
      case _ =>
        logger.error(s"Unknown config source: $sourceType")
        throw new IllegalArgumentException(s"Unknown config source: $sourceType")
    }
  }
}
