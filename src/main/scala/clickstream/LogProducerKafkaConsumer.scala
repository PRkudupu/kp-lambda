package clickstream

import java.util.Properties

import config.Settings
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerConfig, ProducerRecord}

import scala.util.Random

object LogProducerKafkaConsumer extends App {
  //Create a spark session specify the app name
  val spark = SparkSession
    .builder()
    .master("local[2]")
    .appName("Spark streaming")
    .getOrCreate()


  val kafkaParams = Map(
    "zookeeper.connect" -> "localhost:2181",
    "group.id" -> "lambda",
    "auto.offset.reset" -> "largest"
  )

  case class Activity(timestamp_hour: Long,
                      referrer: String,
                      action: String,
                      prevPage: String,
                      page: String,
                      visitor: String,
                      product: String,
                      inputProps: Map[String, String] = Map()
                     )

  val kstream = KafkaUtils.createStream(
    spark, kafkaParams, Map("weblogs-text" -> 1), StorageLevel.MEMORY_AND_DISK)
    .map(_._2)
}