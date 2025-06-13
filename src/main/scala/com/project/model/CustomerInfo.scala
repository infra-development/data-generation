package com.project.model

case class CustomerInfo(
                         customerId: String,
                         fullName: String, // Customer's full legal name
                         dateOfBirth: String, // Date of birth in YYYY-MM-DD
                         gender: String, // Gender (Male/Female/Other)
                         nationality: String, // Country of citizenship
                         governmentId: String, // Government-issued ID (e.g., passport, driver's license)
                         ssn: String, // Social Security Number or equivalent
                         maritalStatus: String, // Marital status (Single/Married/etc.)
                         homeAddress: String, // Primary residence address
                         mailingAddress: String, // Address used for correspondence, bills, etc.
                         email: String, // Main email address
                         phoneNumbers: List[String] // List of phone numbers (mobile, home, work)
                       )