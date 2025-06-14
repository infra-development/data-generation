package com.project.config

import io.circe.generic.AutoDerivation

case class BusinessConfig(
                           businessDate: String,
                           threshold: Option[Int],
                           generateAccountData: Option[Boolean]
                         ) extends AutoDerivation {
  import java.time.LocalDate
  lazy val prevDate: String = LocalDate.parse(businessDate).minusDays(1).toString
}