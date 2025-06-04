package com.project.model

/**
 * Represents contact-related information for a bank customer.
 * Used for communication, verification, and emergency purposes.
 */
case class CustomerContactInfo(
                                homeAddress: String,         // Primary residence address
                                mailingAddress: String,      // Address used for correspondence, bills, etc.
                                emailPrimary: String,        // Main email address
                                emailSecondary: String,      // Backup or alternate email address
//                                phoneNumbers: List[String],  // List of phone numbers (mobile, home, work)
                                emergencyContact: String     // Emergency contact (name and phone number)
                              )