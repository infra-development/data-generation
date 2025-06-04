package com.project.model

/**
 * Represents personal identification information for a bank customer.
 * Fields are typical for financial KYC and identity verification.
 */
case class CustomerPersonalInfo(
                                 fullName: String,      // Customer's full legal name
                                 dateOfBirth: String,   // Date of birth in YYYY-MM-DD
                                 gender: String,        // Gender (Male/Female/Other)
                                 nationality: String,   // Country of citizenship
                                 governmentId: String,  // Government-issued ID (e.g., passport, driver's license)
                                 ssn: String,           // Social Security Number or equivalent
                                 maritalStatus: String, // Marital status (Single/Married/etc.)
                                 photograph: String     // URL or placeholder for customer photograph
                               )