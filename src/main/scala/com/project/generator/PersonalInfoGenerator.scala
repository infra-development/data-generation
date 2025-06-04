package com.project.generator

import com.github.javafaker.Faker
import com.project.model.CustomerPersonalInfo

import scala.util.Random

/**
 * Generates synthetic personal identification data for a customer using Faker.
 * Useful for testing, development, and demo purposes.
 */
class PersonalInfoGenerator(faker: Faker = new Faker()) extends DataGenerator[CustomerPersonalInfo] {

  /**
   * Generates a CustomerPersonalInfo instance with randomized, realistic-looking data.
   * @return CustomerPersonalInfo
   */
  override def generate(): CustomerPersonalInfo = {
    val name = faker.name().fullName()
    val dob = f"${Random.nextInt(50) + 1950}-${Random.nextInt(12) + 1}%02d-${Random.nextInt(28) + 1}%02d"
    val gender = if (Random.nextBoolean()) "Male" else "Female"
    val nationality = faker.country().name()
    val govId = faker.idNumber().valid()
    val ssn = faker.idNumber().ssnValid()
    val maritalStatus = faker.demographic().maritalStatus()
    val photograph = s"https://randomuser.me/api/portraits/med/${if (gender == "Male") "men" else "women"}/${Random.nextInt(100)}.jpg"

    CustomerPersonalInfo(
      fullName = name,
      dateOfBirth = dob,
      gender = gender,
      nationality = nationality,
      governmentId = govId,
      ssn = ssn,
      maritalStatus = maritalStatus,
      photograph = photograph
    )

  }

}
