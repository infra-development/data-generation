package com.project.app

import com.project.generator._
import com.project.model._
import org.apache.spark.sql.{SparkSession, Encoders}
import scala.collection.mutable.ArrayBuffer


object FinanceDataGeneratorApp {
  def main(args: Array[String]): Unit = {
    val spark = org.apache.spark.sql.SparkSession.builder()
      .appName("Finance Data Generator")
      .master("local[*]")
      .enableHiveSupport()// Use local mode for testing
      .getOrCreate()

    val df = spark.sql("SHOW DATABASES")
    df.show() // Display existing databases for verification

//    val numRecords = 1000 // Number of records to generate
//
//    val customerInfoGenerator = new CustomerInfoGenerator()
//    val customerInfos = ArrayBuffer[CustomerInfo]()
//
//    for (_ <- 1 to numRecords) {
//      customerInfos += customerInfoGenerator.generate()
//    }
//
//    import spark.implicits._
//
//    // Convert generated data to Spark Datasets for further processing or saving
//    // Both lines will work exactly same way, but using Encoders.product is more type-safe and also allows for better optimization by Spark
//    // If you want to use Encoders.product, you need to define the case class for CustomerContactInfo
//    // You do not need to pass the Encoder explicitly if you have import spark.implicits._ and the type is a case class.
//    val customerDf = spark.createDataset(customerInfos.toSeq)(Encoders.product[CustomerInfo])
//
//    customerDf.show(5) // Display first 5 records for verification
//
//    customerDf.write.option("header", "true").mode("overwrite").saveAsTable("finance_db.customer_info")
//    spark.stop()

  }

}
