package com.project.config.parser

trait ConfigParser[T] {
  def parse(content: String): Option[T]
}