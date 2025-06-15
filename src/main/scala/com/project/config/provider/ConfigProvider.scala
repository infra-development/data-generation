package com.project.config.provider

import com.project.config.BusinessConfig
import com.project.config.parser.ConfigParser

trait ConfigProvider {
  def loadBusinessConfig[T](path: String, parser: ConfigParser[T]): Option[T]

  def updateBusinessConfig[T](path: String, updatedConfig: BusinessConfig, parser: ConfigParser[T]): Option[T]

}