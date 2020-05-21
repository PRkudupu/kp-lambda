package streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._

object StreamingKafkaJob {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(1))

    val kafkaParams = Map(
      "metadata.broker.list" -> "localhost:9092",
      "group.id" -> "lambda",
      "auto.offset.reset" -> "largest"
    )

    val topics = Array("weblogs-text")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
   stream.map(record =>{
      println("record")
      (record.key, record.value)
    }
    )


    // Import dependencies and create kafka params as in Create Direct Stream above
    stream.saveAsTextFiles("C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\steams")

    }
}
