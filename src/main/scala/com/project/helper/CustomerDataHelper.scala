package com.project.helper

import com.project.manager.BusinessDateDataManager
import com.project.generator.CustomerInfoGenerator
import com.project.model.CustomerInfo
import org.apache.spark.sql.{SparkSession, Dataset, Encoders}
import org.apache.spark.sql.functions._
import com.project.ProjectConstants._

class CustomerDataHelper(spark: SparkSession, dataManager: BusinessDateDataManager) {
  def build(businessDate: String, prevDate: String): Dataset[CustomerInfo] = {
    import spark.implicits._
    val prevDF = dataManager.loadPartition(CUSTOMERS, prevDate)
    val prevIds = if (!prevDF.isEmpty) prevDF.select(CUSTOMER_ID).as[String].collect().toSet else Set.empty[String]
    val prevCustomers = if (!prevDF.isEmpty) prevDF.drop(BUSINESS_DATE).as[CustomerInfo].collect().toSeq else Seq.empty

    val initialCount = 1000
    val newCount = if (prevIds.nonEmpty) Math.ceil(prevIds.size * 0.08).toInt else initialCount

    val generator = new CustomerInfoGenerator()
    val newCustomers = collection.mutable.ArrayBuffer[CustomerInfo]()
    var usedIds = prevIds
    while (newCustomers.size < newCount) {
      val c = generator.generate()
      if (!usedIds.contains(c.customerId)) {
        newCustomers += c
        usedIds += c.customerId
      }
    }
    val all = prevCustomers ++ newCustomers

    // Convert generated data to Spark Datasets for further processing or saving
    // Both lines will work exactly same way, but using Encoders.product is more type-safe and also allows for better optimization by Spark
    // If you want to use Encoders.product, you need to define the case class for CustomerInfo
    // You do not need to pass the Encoder explicitly if you have import spark.implicits._ and the type is a case class.
    spark.createDataset(all)(Encoders.product[CustomerInfo]).withColumn(BUSINESS_DATE, lit(businessDate)).as[CustomerInfo]
  }
}