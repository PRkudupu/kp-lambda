package batch
package clickstream
import domain.ActivityStreams
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.streaming.Trigger

object StreamingJob {
  def main(args: Array[String]): Unit = {
    //location for data source
    val sourceFile = "C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\input"

    //Create a spark session specify the app name
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("Spark streaming")
      .getOrCreate()

    //set spark log level to only error
    spark.sparkContext.setLogLevel("ERROR")

    //convert case class into schema
    val schema = ScalaReflection.schemaFor[ActivityStreams].dataType.asInstanceOf[StructType]

    val MS_IN_HOUR = 1000 * 60 * 60
    // Create an RDD of Person objects from a text file, convert it to a Dataframe
    val inputDF = spark
      .readStream
        .option("header","true")
         .schema(schema)
         .option("delimiter","\t")
         .csv(sourceFile)

    //Write
    inputDF.writeStream
      .outputMode("append")
      .format("console")
      .option("truncate","false")
      .trigger(Trigger.ProcessingTime("2 seconds"))
      .start()
     .awaitTermination()
  }
}
