package streaming

import org.apache.spark.sql.SparkSession


object KafkaStreamingCassandraJob {
  def main(args: Array[String]): Unit = {
    //location for data source
    val sourceFile = "C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\input"

    //Create a spark session specify the app name
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("Spark streaming")
      .getOrCreate()

    println(spark.version)
    //set spark log level to only error
    spark.sparkContext.setLogLevel("ERROR")

    val df = spark
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", "weblogs-text")
        .load()

    //Here we are only fetching values not keys.
    //Kafka producer has written to the topic with key as null value
   val output= df.selectExpr( "CAST(value AS STRING)")

    output.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()
  }
}
