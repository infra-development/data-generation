package com.project.app

import com.project.ProjectConstants.{ACCOUNTS, CUSTOMERS, FINANCE_DB}
import com.project.helper.{AccountDataHelper, CustomerDataHelper}
import com.project.manager.BusinessDateDataManager

import java.time.LocalDate


object FinanceDataGeneratorApp {
  def main(args: Array[String]): Unit = {

    require(args.length == 1, "Usage: CustomerAccountSnapshotApp <business_date: yyyy-MM-dd>")
    val businessDate = args(0)
    val prevDate = LocalDate.parse(businessDate).minusDays(1).toString

    val spark = org.apache.spark.sql.SparkSession.builder()
      .appName("Finance Data Generator")
      .master("local[*]")
      .enableHiveSupport()// Use local mode for testing
      .getOrCreate()

    val df = spark.sql("SHOW DATABASES")
    df.show() // Display existing databases for verification

    val dataManager = new BusinessDateDataManager(spark)

    // Generate customer data
    val customerDataBuilder = new CustomerDataHelper(spark, dataManager)
    val customerDS = customerDataBuilder.build(businessDate, prevDate)
    dataManager.writePartition(customerDS.toDF(), CUSTOMERS)

    // Generate account data
    val accountDataBuilder = new AccountDataHelper(spark, dataManager, customerDS.collect().map(_.customerId))
    val accountDS = accountDataBuilder.build(businessDate, prevDate)
    dataManager.writePartition(accountDS.toDF(), ACCOUNTS)

    spark.stop()

  }

}
