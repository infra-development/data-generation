package com.project.config.parser

import io.circe.Decoder

trait ConfigParser[T] {
  def parse(content: String): Either[Throwable, T]
  def serialize(config: T): Either[Throwable, String]
}