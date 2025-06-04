package com.project.generator

/**
 * Strategy pattern trait for generating instances of a specific data type.
 * Each data category should have its own generator implementing this trait.
 * @tparam T The type of data to generate.
 */
trait DataGenerator[T] {
  /**
   * Generates a single instance of data type T.
   * @return The generated data object.
   */
  def generate(): T
}