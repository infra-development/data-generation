package com.project.config.provider

import com.project.config.BusinessConfig
import com.project.config.parser.ConfigParser
import io.circe.Decoder
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.logging.log4j.LogManager

class HDFSConfigProvider extends ConfigProvider {

  private val logger = LogManager.getLogger(this.getClass)
  private val conf = new Configuration()
  private val fs = FileSystem.get(conf)

  override def loadBusinessConfig[T](path: String, parser: ConfigParser[T])(implicit decoder: Decoder[T]): T = {
    try {
      val hdfsPath = new Path(path)
      logger.debug(s"Checking if config exists at: $hdfsPath")

      val configOpt = if (fs.exists(hdfsPath)) {
        logger.info(s"Config file found at: $hdfsPath. Reading content...")

        val inputStream = fs.open(hdfsPath)
        val content = scala.io.Source.fromInputStream(inputStream).mkString
        inputStream.close()

        logger.debug("Content read successfully. Parsing config...")
        val parsedConfig = parser.parse(content)

        parsedConfig match {
          case Some(_) =>
            logger.info("Config parsed successfully.")
          case None =>
            logger.error("Failed to parse config content.")
        }
        parsedConfig
      } else {
        logger.error(s"Config file does not exist at: $hdfsPath")
        None
      }
      val businessConfig: T = configOpt.getOrElse {
        logger.error("Could not load business configuration!")
        throw new RuntimeException("Could not load business configuration!")
      }
      businessConfig
    } catch {
      case ex: Exception =>
        throw new RuntimeException(s"Failed to load config from HDFS path: $path", ex)
    }
  }


  override def updateBusinessConfig[T](path: String, updatedConfig: BusinessConfig, parser: ConfigParser[T])(implicit decoder: Decoder[T]) = {
    try {
      val hdfsPath = new Path(path)
      logger.debug(s"Checking if HDFS path exists: $hdfsPath")

      if (fs.exists(hdfsPath)) {
        logger.info(s"Config file found at: $hdfsPath. Reading content...")

        val inputStream = fs.open(hdfsPath)
        val content = scala.io.Source.fromInputStream(inputStream).mkString
        inputStream.close()

        logger.debug("Config content read successfully. Parsing...")
        parser.parse(content).map {
          case config: BusinessConfig =>
            logger.info("Parsed config successfully. Updating businessDate...")

            val updated = config.copy(businessDate = updatedConfig.businessDate)
            val serialized = parser.asInstanceOf[ConfigParser[BusinessConfig]].serialize(updated)

            logger.debug(s"Serialized updated config: $serialized")
            val outputStream = fs.create(hdfsPath, true)
            outputStream.write(serialized.getBytes("UTF-8"))
            outputStream.close()

            logger.info(s"Updated businessDate and wrote config back to: $hdfsPath")
            updated.asInstanceOf[T]

          case _ =>
            logger.error("Parsed object is not of type BusinessConfig")
            throw new IllegalStateException("Parsed config is not a BusinessConfig")
        }
      } else {
        logger.error(s"Config file not found at: $hdfsPath")
        None
      }
    } catch {
      case ex: Exception =>
        logger.error(s"Failed to update config at: $path", ex)
        None
    }
  }

}