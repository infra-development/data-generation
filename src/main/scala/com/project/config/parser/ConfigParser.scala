package com.project.config.parser

trait ConfigParser[T] {
  def parse(content: String): Either[Throwable, T]
  def serialize(config: T): Either[Throwable, String]
}