package com.project.config.provider

import com.project.config.parser.ConfigParser
import org.apache.zookeeper.ZooKeeper

class ZookeeperConfigProvider(zkAddress: String) extends ConfigProvider {
  private val zk = new ZooKeeper(zkAddress, 2000, null)

  def loadBusinessConfig[T](path: String, parser: ConfigParser[T]): Option[T] = {
    try {
      val data = zk.getData(path, false, null)
      val content = new String(data, "UTF-8")
      parser.parse(content)
    } catch {
      case _: Exception => None
    }
  }
}