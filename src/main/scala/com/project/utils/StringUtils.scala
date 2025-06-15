package com.project.utils

object StringUtils {

  implicit class StringOps(val string: String) extends AnyVal {
    def camelToSnakeCase: String = {
      string.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase
    }
  }

}
