package com.project.config.provider

import com.project.config.BusinessConfig
import com.project.config.parser.ConfigParser
import io.circe.Decoder

trait ConfigProvider {
  def loadBusinessConfig[T](path: String, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Either[Throwable, T]

  def updateBusinessConfig[T](path: String, updatedConfig: BusinessConfig, parser: ConfigParser[T])(implicit decoder: Decoder[T]): Either[Throwable, T]
}

