package com.project.model

case class AccountInfo(
                        accountId: String,
                        customerId: String,
                        accountType: String,
                        openDate: String,
                        closeDate: Option[String],
                        status: String,
                        currentBalance: Double
                      )