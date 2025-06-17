package com.project.generator

import com.github.javafaker.Faker
import com.project.data.model.AccountInfo
import org.apache.logging.log4j.{LogManager, Logger}

import java.util.concurrent.atomic.AtomicLong
import scala.util.Random

class AccountInfoGenerator(
                            faker: Faker = new Faker(),
                            customerIds: Seq[String],
                            startingId: Long // Provide max(account_id) + 1
                          ) extends DataGenerator[AccountInfo] {

  private val logger: Logger = LogManager.getLogger(getClass)
  private val accountIdCounter = new AtomicLong(startingId)

  logger.debug(s"AccountInfoGenerator initialized with ${customerIds.size} customer IDs and starting ID: $startingId")

  override def generate(): AccountInfo = {
    val accountTypes = Seq("Investment", "Savings", "Retirement")
    val statuses = Seq("Active", "Dormant", "Closed")

    val accountId = f"ACC${accountIdCounter.getAndIncrement()}%06d"
    val customerId = customerIds(Random.nextInt(customerIds.length))
    val accountType = accountTypes(Random.nextInt(accountTypes.length))
    val openDate = f"${Random.nextInt(10) + 2015}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
    val status = statuses(Random.nextInt(statuses.length))
    val closeDate = if (status == "Closed") Some(f"${Random.nextInt(4) + 2021}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d") else None
    val balance = BigDecimal(1000 + Random.nextDouble() * 100000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

    logger.debug(s"Generating AccountInfo: ID=$accountId, CustomerId=$customerId")

    AccountInfo(
      account_id = accountId,
      customer_id = customerId,
      account_type = accountType,
      open_date = openDate,
      close_date = closeDate,
      status = status,
      current_balance = balance
    )
  }
}
