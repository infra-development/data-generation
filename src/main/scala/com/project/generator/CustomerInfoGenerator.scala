package com.project.generator

import com.github.javafaker.Faker
import com.project.model.CustomerInfo

import scala.util.Random

/**
 * Generates synthetic contact information data for a customer using Faker.
 * Useful for testing, development, and demo purposes.
 */
class CustomerInfoGenerator(faker: Faker = new Faker()) extends DataGenerator[CustomerInfo] {

  val customerId = s"CUST${Random.alphanumeric.take(10).mkString}"
  val name = faker.name().fullName()
  val dob = f"${Random.nextInt(50) + 1950}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
  val gender = if (Random.nextBoolean()) "Male" else "Female"
  val nationality = faker.country().name()
  val govId = faker.idNumber().valid()
  val ssn = faker.idNumber().ssnValid()
  val maritalStatus = faker.demographic().maritalStatus()
  /**
   * Generates a CustomerContactInfo instance with randomized, realistic-looking data.
   * @return CustomerContactInfo
   */
  override def generate(): CustomerInfo = {
    CustomerInfo(
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
  }


}