package com.project.generator

import com.github.javafaker.Faker
import com.project.model.CustomerInfo
import org.apache.logging.log4j.{LogManager, Logger}

import scala.util.Random

/**
 * Generates synthetic contact information data for a customer using Faker.
 * Useful for testing, development, and demo purposes.
 */
class CustomerInfoGenerator(faker: Faker = new Faker()) extends DataGenerator[CustomerInfo] {

  private val logger: Logger = LogManager.getLogger(getClass)

  logger.debug("Instantiating CustomerInfoGenerator with new Faker instance.")

  override def generate(): CustomerInfo = {
    val customerId = s"CUST${Random.alphanumeric.take(10).mkString}".toUpperCase
    val name = faker.name().fullName()
    val dob = f"${Random.nextInt(50) + 1950}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
    val gender = if (Random.nextBoolean()) "Male" else "Female"
    val nationality = faker.country().name()
    val govId = faker.idNumber().valid()
    val ssn = faker.idNumber().ssnValid()
    val maritalStatus = faker.demographic().maritalStatus()
    val homeAddress = faker.address().fullAddress()
    val mailingAddress = faker.address().fullAddress()
    val email = faker.internet().emailAddress()
    val phoneNumbers = List(faker.phoneNumber().cellPhone(), faker.phoneNumber().phoneNumber())

    logger.debug(s"Generating CustomerInfo: ID=$customerId, Name=$name, DOB=$dob, Gender=$gender")

    val customer = CustomerInfo(
      customerId, name, dob, gender, nationality,
      govId, ssn, maritalStatus, homeAddress, mailingAddress, email, phoneNumbers
    )

    logger.debug(s"Generated CustomerInfo object: $customer")
    customer
  }
}

