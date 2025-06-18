package com.project.helper

import com.project.ProjectConstants._
import com.project.data.model.CustomerInfo
import com.project.data.reader.BusinessDataReader
import com.project.data.writer.BusinessDataWriter
import com.project.generator.CustomerInfoGenerator
import com.project.utils.StringUtils.StringOps
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}

class CustomerDataHelper(spark: SparkSession, dataReader: BusinessDataReader, dataWriter: BusinessDataWriter) {

  private val logger: Logger = LogManager.getLogger(getClass)

  def build(businessDate: String, prevDate: String, initialCount: Int): Dataset[CustomerInfo] = {
    import spark.implicits._

    logger.info(s"Building customer dataset for businessDate: $businessDate, using previous date: $prevDate")

    val prevDF = dataReader.readPartition(prevDate, CUSTOMERS)

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

    val maxId = prevIds
      .map(_.stripPrefix("CUST"))
      .flatMap(id => scala.util.Try(id.toLong).toOption)
      .foldLeft(0L)(Math.max)

    val newCount =
      if (prevIds.nonEmpty) Math.ceil(prevIds.size * 0.08).toInt
      else initialCount

    logger.info(s"Generating $newCount new customer records.")

    val generator = new CustomerInfoGenerator(startingId = maxId + 1)
    val newCustomers = collection.mutable.ArrayBuffer[CustomerInfo]()
    var usedIds = prevIds

    while (newCustomers.size < newCount) {
      val c = generator.generate()
      if (!usedIds.contains(c.customer_id)) {
        newCustomers += c
        usedIds += c.customer_id
      }
    }

    logger.debug(s"Generated ${newCustomers.size} new unique customers.")

    val all = prevCustomers ++ newCustomers

    val customerDS = spark
      .createDataset(all)(Encoders.product[CustomerInfo])
      .withColumn(BUSINESS_DATE, lit(businessDate))
      .as[CustomerInfo]

    logger.info(s"Customer dataset for date $businessDate built with total size: ${customerDS.count()}")
    customerDS
  }

  def writeCustomerData(customerDS: Dataset[CustomerInfo]): Unit = {
    val customerDF = customerDS.toDF()
    val finalCustomerDF = customerDF.columns.foldLeft(customerDF) { (df, colName) =>
      df.withColumnRenamed(colName, colName.camelToSnakeCase)
    }
    finalCustomerDF.show(false)
    finalCustomerDF.printSchema()
    dataWriter.writePartition(finalCustomerDF, CUSTOMERS)
    logger.info("Customer data written successfully.")
  }
}
