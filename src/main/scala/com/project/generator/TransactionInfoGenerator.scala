package com.project.generator

import com.github.javafaker.Faker
import com.project.model.TransactionInfo

import scala.util.Random

class TransactionInfoGenerator(faker: Faker = new Faker(), accountIds: Seq[String]) extends DataGenerator[TransactionInfo] {
  override def generate(): TransactionInfo = {
    val txnTypes = Seq("Deposit", "Withdrawal", "Transfer", "Purchase", "Sale")
    val channels = Seq("Branch", "Online", "Mobile", "ATM")
    val locations = Seq("NY", "LA", "TX", "FL", "IL")

    val txnId = s"TXN${Random.alphanumeric.take(10).mkString}"
    val accountId = accountIds(Random.nextInt(accountIds.length))
    val txnDate = f"${Random.nextInt(2) + 2023}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
    val amount = BigDecimal(50 + Random.nextDouble() * 50000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val txnType = txnTypes(Random.nextInt(txnTypes.length))
    val channel = channels(Random.nextInt(channels.length))
    val counterparty = s"CPTY${Random.nextInt(1000)}"
    val location = locations(Random.nextInt(locations.length))

    TransactionInfo(
      txnId = txnId,
      accountId = accountId,
      txnDate = txnDate,
      amount = amount,
      txnType = txnType,
      channel = channel,
      counterparty = counterparty,
      location = location
    )
  }
}