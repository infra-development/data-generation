package com.project.config



import com.project.config.parser.ConfigParserFactory
import com.project.config.provider.ConfigProviderFactory
import io.circe.{Decoder, Encoder}
import org.apache.logging.log4j.{LogManager, Logger}

import java.time.LocalDate
import scala.util.{Either, Left, Right}

class ConfigUpdater[T](implicit decoder: Decoder[T], encoder: Encoder[T]) {

  private val logger: Logger = LogManager.getLogger(this.getClass)

  private val allConfigs: List[(String, String, String)] = List(
    ("hdfs", "yaml", "/config/data_gen/finance-data.yaml"),
    ("hdfs", "json", "/config/data_gen/finance-data.json"),
    ("zookeeper", "yaml", "/config/data_gen/finance-data-yaml"),
    ("zookeeper", "json", "/config/data_gen/finance-data-json")
  )

  def propagateConfigFrom(sourceProviderType: String, sourceFormat: String, sourcePath: String): Either[Throwable, List[T]] = {
    for {
      // Step 1: Load source config
      sourceParser <- ConfigParserFactory[T](sourceFormat)
      sourceProvider = ConfigProviderFactory(sourceProviderType)
      loadedConfig <- sourceProvider.loadBusinessConfig(sourcePath, sourceParser)

      // Step 2: Update business date if type is BusinessConfig
      updatedConfig <- loadedConfig match {
        case bc: BusinessConfig =>
          val incrementedDate = LocalDate.parse(bc.businessDate).plusDays(1).toString
          Right(bc.copy(businessDate = incrementedDate).asInstanceOf[T])
        case _ =>
          Left(new IllegalArgumentException(s"Expected BusinessConfig but got ${loadedConfig.getClass.getSimpleName}"))
      }

      // Step 3: Write updated config to all destinations
      results = allConfigs.map {
        case (targetProviderType, targetFormat, targetPath) =>
          for {
            targetParser <- ConfigParserFactory[T](targetFormat)
            targetProvider = ConfigProviderFactory(targetProviderType)
            _ = logger.info(s"Updating $targetProviderType config at $targetPath in $targetFormat format")
            updated <- targetProvider.updateBusinessConfig(targetPath, updatedConfig, targetParser)
          } yield updated
      }

      // Step 4: Aggregate result
      (errors, successes) = results.partition(_.isLeft)
      errorList = errors.collect { case Left(err) => err }
      successList = successes.collect { case Right(v) => v }

      result <- if (errorList.nonEmpty)
        Left(new RuntimeException(s"Failed updates: ${errorList.map(_.getMessage).mkString("; ")}"))
      else
        Right(successList)

    } yield result
  }
}


