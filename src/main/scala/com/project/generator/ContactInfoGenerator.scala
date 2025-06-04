package com.project.generator

import com.github.javafaker.Faker
import com.project.model.CustomerContactInfo
import scala.util.Random

/**
 * Generates synthetic contact information data for a customer using Faker.
 * Useful for testing, development, and demo purposes.
 */
class ContactInfoGenerator(faker: Faker = new Faker()) extends DataGenerator[CustomerContactInfo] {
  /**
   * Generates a CustomerContactInfo instance with randomized, realistic-looking data.
   * @return CustomerContactInfo
   */
  override def generate(): CustomerContactInfo = {
    CustomerContactInfo(
      homeAddress = faker.address().fullAddress(),
      mailingAddress = faker.address().fullAddress(),
      emailPrimary = faker.internet().emailAddress(),
      emailSecondary = faker.internet().emailAddress(),
//      phoneNumbers = List(faker.phoneNumber().cellPhone(), faker.phoneNumber().phoneNumber()),
      emergencyContact = s"${faker.name().fullName()} - ${faker.phoneNumber().cellPhone()}"
    )
  }
}