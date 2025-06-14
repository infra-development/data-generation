package com.project.helper

import com.project.manager.BusinessDateDataManager
import com.project.generator.CustomerInfoGenerator
import com.project.model.CustomerInfo
import org.apache.spark.sql.{SparkSession, Dataset, Encoders}
import org.apache.spark.sql.functions._
import com.project.ProjectConstants._
import org.apache.logging.log4j.{LogManager, Logger}

class CustomerDataHelper(spark: SparkSession, dataManager: BusinessDateDataManager) {

  private val logger: Logger = LogManager.getLogger(getClass)

  def build(businessDate: String, prevDate: String, initialCount: Int): Dataset[CustomerInfo] = {
    import spark.implicits._

    logger.info(s"Building customer dataset for businessDate: $businessDate, using previous date: $prevDate")

    val prevDF = dataManager.loadPartition(CUSTOMERS, prevDate)

    val prevIds =
      if (!prevDF.isEmpty) {
        logger.debug("Previous customer data found. Extracting IDs.")
        prevDF.select(CUSTOMER_ID).as[String].collect().toSet
      } else {
        logger.warn(s"No previous customer data found for date: $prevDate")
        Set.empty[String]
      }

    val prevCustomers =
      if (!prevDF.isEmpty) {
        logger.debug("Extracting previous customer records as case class instances.")
        prevDF.drop(BUSINESS_DATE).as[CustomerInfo].collect().toSeq
      } else {
        Seq.empty[CustomerInfo]
      }

    val newCount =
      if (prevIds.nonEmpty) Math.ceil(prevIds.size * 0.08).toInt
      else initialCount

    logger.info(s"Generating $newCount new customer records.")

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

    logger.debug(s"Generated ${newCustomers.size} new unique customers.")

    val all = prevCustomers ++ newCustomers

    val resultDS = spark
      .createDataset(all)(Encoders.product[CustomerInfo])
      .withColumn(BUSINESS_DATE, lit(businessDate))
      .as[CustomerInfo]

    logger.info(s"Customer dataset for date $businessDate built with total size: ${resultDS.count()}")

    resultDS
  }
}
