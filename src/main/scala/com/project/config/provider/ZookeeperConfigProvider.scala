package com.project.config.provider

import com.project.config.parser.ConfigParser
import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

class ZookeeperConfigProvider extends ConfigProvider {

  val watcher = new Watcher {
    override def process(event: WatchedEvent): Unit = {
      println(s"Event triggered: ${event.getType} on path: ${event.getPath}")
    }
  }

  private val zk = new ZooKeeper("localhost:2181", 2000, watcher)

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