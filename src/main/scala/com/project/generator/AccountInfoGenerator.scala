package com.project.generator

import com.github.javafaker.Faker
import com.project.model.AccountInfo

import scala.util.Random

class AccountInfoGenerator(faker: Faker = new Faker(), customerIds: Seq[String]) extends DataGenerator[AccountInfo] {
  override def generate(): AccountInfo = {
    val accountTypes = Seq("Investment", "Savings", "Retirement")
    val statuses = Seq("Active", "Dormant", "Closed")
    val accountId = s"ACC${Random.alphanumeric.take(8).mkString}"
    val customerId = customerIds(Random.nextInt(customerIds.length))
    val accountType = accountTypes(Random.nextInt(accountTypes.length))
    val openDate = f"${Random.nextInt(10) + 2015}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
    val status = statuses(Random.nextInt(statuses.length))
    val closeDate = if (status == "Closed") Some(f"${Random.nextInt(4) + 2021}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d") else None
    val balance = BigDecimal(1000 + Random.nextDouble() * 100000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

    AccountInfo(
      accountId = accountId,
      customerId = customerId,
      accountType = accountType,
      openDate = openDate,
      closeDate = closeDate,
      status = status,
      currentBalance = balance
    )
  }
}