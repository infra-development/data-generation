package com.project.config.provider

import com.project.config.parser.ConfigParser

trait ConfigProvider {
  def loadBusinessConfig[T](path: String, parser: ConfigParser[T]): Option[T]
}