package com.project.model

case class CustomerInfo(
                         customer_id: String,
                         full_name: String, // Customer's full legal name
                         date_of_birth: String, // Date of birth in YYYY-MM-DD
                         gender: String, // Gender (Male/Female/Other)
                         nationality: String, // Country of citizenship
                         government_id: String, // Government-issued ID (e.g., passport, driver's license)
                         ssn: String, // Social Security Number or equivalent
                         marital_status: String, // Marital status (Single/Married/etc.)
                         home_address: String, // Primary residence address
                         mailing_address: String, // Address used for correspondence, bills, etc.
                         email: String, // Main email address
                         phone_numbers: List[String] // List of phone numbers (mobile, home, work)
                       )