package com.project.config

import com.project.config.provider.{ConfigProvider, HDFSConfigProvider, ZookeeperConfigProvider}

object ConfigProviderFactory {
  def apply(sourceType: String, address: String): ConfigProvider = {
    sourceType.toLowerCase match {
      case "zookeeper" => new ZookeeperConfigProvider(address)
      case "hdfs"      => new HDFSConfigProvider(address)
      case _ => throw new IllegalArgumentException(s"Unknown config source: $sourceType")
    }
  }
}