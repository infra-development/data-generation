package com.project.config.parser

import io.circe.parser.decode

class JsonConfigParser[T : io.circe.Decoder] extends ConfigParser[T] {
  override def parse(content: String): Option[T] = decode[T](content).toOption
}