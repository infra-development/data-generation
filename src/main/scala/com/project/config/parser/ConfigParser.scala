package com.project.config.parser

import io.circe.Decoder

trait ConfigParser[T] {
  def parse[T: Decoder](content: String): Option[T]
  def serialize(config: T): String
}