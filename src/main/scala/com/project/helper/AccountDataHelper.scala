package com.project.helper

import com.project.ProjectConstants.{ACCOUNTS, ACCOUNT_ID, BUSINESS_DATE}
import com.project.manager.BusinessDateDataManager
import com.project.generator.AccountInfoGenerator
import com.project.model.AccountInfo
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.logging.log4j.{LogManager, Logger}

class AccountDataHelper(spark: SparkSession, dataManager: BusinessDateDataManager, customerIds: Seq[String]) {

  private val logger: Logger = LogManager.getLogger(getClass)

  def build(businessDate: String, prevDate: String, initialCount: Int): Dataset[AccountInfo] = {
    import spark.implicits._

    logger.info(s"Building account dataset for businessDate: $businessDate, using previous date: $prevDate")

    val prevDF = dataManager.loadPartition(ACCOUNTS, prevDate)

    val prevIds =
      if (!prevDF.isEmpty) {
        logger.debug("Previous account data found. Extracting IDs.")
        prevDF.select(ACCOUNT_ID).as[String].collect().toSet
      } else {
        logger.warn(s"No previous account data found for date: $prevDate")
        Set.empty[String]
      }

    val prevAccounts =
      if (!prevDF.isEmpty) {
        logger.debug("Extracting previous account records as case class instances.")
        prevDF.drop(BUSINESS_DATE).as[AccountInfo].collect().toSeq
      } else {
        Seq.empty[AccountInfo]
      }

    val newCount =
      if (prevIds.nonEmpty) Math.ceil(prevIds.size * 0.08).toInt
      else initialCount

    logger.info(s"Generating $newCount new account records.")

    val generator = new AccountInfoGenerator(customerIds = customerIds)
    val newAccounts = collection.mutable.ArrayBuffer[AccountInfo]()
    var usedIds = prevIds

    while (newAccounts.size < newCount) {
      val a = generator.generate()
      if (!usedIds.contains(a.accountId)) {
        newAccounts += a
        usedIds += a.accountId
      }
    }

    logger.debug(s"Generated ${newAccounts.size} new unique accounts.")

    val all = prevAccounts ++ newAccounts

    val resultDS = spark
      .createDataset(all)(Encoders.product[AccountInfo])
      .withColumn(BUSINESS_DATE, lit(businessDate))
      .as[AccountInfo]

    logger.info(s"Account dataset for date $businessDate built with total size: ${resultDS.count()}")

    resultDS
  }
}
