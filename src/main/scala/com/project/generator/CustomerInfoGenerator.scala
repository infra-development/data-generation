package com.project.generator

import com.github.javafaker.Faker
import com.project.model.CustomerInfo
import org.apache.logging.log4j.{LogManager, Logger}

import java.util.concurrent.atomic.AtomicLong
import scala.util.Random

/**
 * Generates synthetic contact information data for a customer using Faker.
 * Useful for testing, development, and demo purposes.
 */
class CustomerInfoGenerator(
                             faker: Faker = new Faker(),
                             startingId: Long // You pass maxId + 1 here
                           ) extends DataGenerator[CustomerInfo] {

  private val logger: Logger = LogManager.getLogger(getClass)
  private val customerIdCounter = new AtomicLong(startingId)

  logger.debug(s"Instantiating CustomerInfoGenerator with starting ID: $startingId")

  override def generate(): CustomerInfo = {
    val idNum = customerIdCounter.getAndIncrement()
    val customerId = f"CUST$idNum%06d"

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

    logger.debug(s"Generated CustomerInfo: ID=$customerId, Name=$name")

    CustomerInfo(
      customerId, name, dob, gender, nationality,
      govId, ssn, maritalStatus, homeAddress, mailingAddress, email, phoneNumbers
    )
  }
}
