package com.project.model

case class TransactionInfo(
                            txnId: String,
                            accountId: String,
                            txnDate: String,
                            amount: Double,
                            txnType: String,
                            channel: String,
                            counterparty: String,
                            location: String
                          )