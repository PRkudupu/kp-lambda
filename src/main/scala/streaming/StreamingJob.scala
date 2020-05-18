package batch
package clickstream
import org.apache.spark.sql.SparkSession

object StreamingJob {
  def main(args: Array[String]): Unit = {
    //location for data source
    val sourceFile = "C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\data.tsv"

    //Create a spark session specify the app name
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("Spark streaming")
      .getOrCreate()

    //set spark log level to only error
    spark.sparkContext.setLogLevel("ERROR")

    // For implicit conversions from RDDs to DataFrames

    val MS_IN_HOUR = 1000 * 60 * 60
    // Create an RDD of Person objects from a text file, convert it to a Dataframe
    val inputDF = spark
      .readStream
      .load(sourceFile)

    println(inputDF)


  }
}
