package com.project.app

import com.project.generator._
import com.project.model._
import org.apache.spark.sql.{SparkSession, Encoders}
import scala.collection.mutable.ArrayBuffer


object FinanceDataGeneratorApp {
  def main(args: Array[String]): Unit = {
    val spark = org.apache.spark.sql.SparkSession.builder()
      .appName("Finance Data Generator")
      .master("local[*]") // Use local mode for testing
      .getOrCreate()

    val numRecords = 1000 // Number of records to generate

    val personalDataGenerator = new PersonalInfoGenerator()
    val contactInfoGenerator = new ContactInfoGenerator()

    val personalInfos = ArrayBuffer[CustomerPersonalInfo]()
    val contactInfos = ArrayBuffer[CustomerContactInfo]()

    for (_ <- 1 to numRecords) {
      personalInfos += personalDataGenerator.generate()
      contactInfos += contactInfoGenerator.generate()
    }

    import spark.implicits._

    // Convert generated data to Spark Datasets for further processing or saving
    // Both lines will work exactly same way, but using Encoders.product is more type-safe and also allows for better optimization by Spark
    // If you want to use Encoders.product, you need to define the case class for CustomerContactInfo
    // You do not need to pass the Encoder explicitly if you have import spark.implicits._ and the type is a case class.
    val personalDF = spark.createDataset(personalInfos.toSeq)(Encoders.product[CustomerPersonalInfo])
    val contactDF = spark.createDataset(contactInfos.toSeq)

    contactDF.show(5) // Display first 5 records for verification

    // Save output as Parquet files (columnar, efficient for big data)
    personalDF.write.option("header", "true").mode("overwrite").parquet("output/personal_info")
    contactDF.write.option("header", "true").mode("overwrite").parquet("output/contact_info")
    spark.stop()

  }

}
