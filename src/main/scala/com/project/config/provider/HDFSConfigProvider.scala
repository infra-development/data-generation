package com.project.config.provider

import com.project.config.parser.ConfigParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class HDFSConfigProvider(hdfsUri: String) extends ConfigProvider {
  private val conf = new Configuration()
  conf.set("fs.defaultFS", hdfsUri)
  private val fs = FileSystem.get(conf)

  def loadBusinessConfig[T](path: String, parser: ConfigParser[T]): Option[T] = {
    try {
      val hdfsPath = new Path(path)
      if (fs.exists(hdfsPath)) {
        val inputStream = fs.open(hdfsPath)
        val content = scala.io.Source.fromInputStream(inputStream).mkString
        inputStream.close()
        parser.parse(content)
      } else {
        None
      }
    } catch {
      case _: Exception => None
    }
  }
}