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

  val customerId: String = s"CUST${Random.alphanumeric.take(10).mkString}"
  val name: String = faker.name().fullName()
  val dob: String = f"${Random.nextInt(50) + 1950}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
  val gender: String = if (Random.nextBoolean()) "Male" else "Female"
  val nationality: String = faker.country().name()
  val govId: String = faker.idNumber().valid()
  val ssn: String = faker.idNumber().ssnValid()
  val maritalStatus: String = faker.demographic().maritalStatus()

  /**
   * Generates a CustomerInfo instance with randomized, realistic-looking data.
   * @return CustomerInfo
   */
  override def generate(): CustomerInfo = {
    logger.debug(s"Generating CustomerInfo: ID=$customerId, Name=$name, DOB=$dob, Gender=$gender")

    val customer = CustomerInfo(
      customerId = customerId,
      fullName = name,
      dateOfBirth = dob,
      gender = gender,
      nationality = nationality,
      governmentId = govId,
      ssn = ssn,
      maritalStatus = maritalStatus,
      homeAddress = faker.address().fullAddress(),
      mailingAddress = faker.address().fullAddress(),
      email = faker.internet().emailAddress(),
      phoneNumbers = List(faker.phoneNumber().cellPhone(), faker.phoneNumber().phoneNumber())
    )

    logger.debug(s"Generated CustomerInfo object: $customer")
    customer
  }
}
