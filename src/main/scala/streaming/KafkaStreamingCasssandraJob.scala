import org.apache.spark.sql._
import org.apache.spark.sql.cassandra._

object KafkaStreamingCasssandraJob {
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
   val output= df
      .selectExpr( "CAST(value AS STRING)")

    //records
    import org.apache.spark.sql.functions.split
    import spark.implicits._

    val streamDF=output.withColumn("value", split($"value", "\\t"))
      .select(
                $"value".getItem(0).as("timestamp_hour"),
                $"value".getItem(1).as("product"),
                $"value".getItem(2).as("action")
              )
    val MS_IN_HOUR = 1000 * 60 * 60

    //function to change the timestamp
    def convertTime=(s:Long)=>{
      s/MS_IN_HOUR * MS_IN_HOUR
    }

    //Register the udf
    spark.udf.register("convertTimeUDF",convertTime)

    streamDF.createOrReplaceTempView("activity")

    val activityByProduct = spark.sql("""SELECT
                                                        product,
                                                        convertTimeUDF(timestamp_hour) timestamp_hour,
                                                        sum(case when action = 'purchase' then 1 else 0 end) as purchase_count,
                                                        sum(case when action = 'add_to_cart' then 1 else 0 end) as add_to_cart_count,
                                                        sum(case when action = 'page_view' then 1 else 0 end) as page_view_count
                                                        from activity
                                                        group by product, timestamp_hour
                                                   """)

     /*activityByProduct.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()*/

    activityByProduct.writeStream
        .foreachBatch{ (batchDF: DataFrame, batchId: Long) =>
         batchDF.write       // Use Cassandra batch data source to write streaming out
            .cassandraFormat("stream_activity_by_product", "lambda")
            .option("cluster", "Test Cluster")
            .mode("append")
            .save()
        }
      .outputMode("update")
      .start()
      .awaitTermination()
  }
}
