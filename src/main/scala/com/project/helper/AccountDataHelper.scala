package com.project.helper

import com.project.ProjectConstants.{ACCOUNTS, ACCOUNT_ID, BUSINESS_DATE}
import com.project.manager.BusinessDateDataManager
import com.project.generator.AccountInfoGenerator
import com.project.model.AccountInfo
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}
import org.apache.spark.sql.functions._

class AccountDataHelper(spark: SparkSession, dataManager: BusinessDateDataManager, customerIds: Seq[String]) {
  def build(businessDate: String, prevDate: String): Dataset[AccountInfo] = {
    import spark.implicits._
    val prevDF = dataManager.loadPartition(ACCOUNTS, prevDate)
    val prevIds = if (!prevDF.isEmpty) prevDF.select(ACCOUNT_ID).as[String].collect().toSet else Set.empty[String]
    val prevAccounts = if (!prevDF.isEmpty) prevDF.drop(BUSINESS_DATE).as[AccountInfo].collect().toSeq else Seq.empty
    val initialCount = 1000
    val newCount = if (prevIds.nonEmpty) Math.ceil(prevIds.size * 0.08).toInt else initialCount

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
    val all = prevAccounts ++ newAccounts
    spark.createDataset(all)(Encoders.product[AccountInfo]).withColumn(BUSINESS_DATE, lit(businessDate)).as[AccountInfo]
  }
}