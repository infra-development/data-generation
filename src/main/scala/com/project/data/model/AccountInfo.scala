package com.project.data.model

case class AccountInfo(
                        account_id: String,
                        customer_id: String,
                        account_type: String,
                        open_date: String,
                        close_date: Option[String],
                        status: String,
                        current_balance: Double
                      )