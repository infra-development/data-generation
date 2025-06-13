package com.project.config.parser

import io.circe.Decoder
import io.circe.yaml.parser

class YamlConfigParser[T : Decoder] extends ConfigParser[T] {
  override def parse(content: String): Option[T] =
    parser.parse(content).flatMap(_.as[T]).toOption
}