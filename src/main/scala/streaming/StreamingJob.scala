package batch
package clickstream
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.catalyst.ScalaReflection
import domain.Activity

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
    val schema = ScalaReflection.schemaFor[Activity].dataType.asInstanceOf[StructType]

    // For implicit conversions from RDDs to DataFrames

    val MS_IN_HOUR = 1000 * 60 * 60
    // Create an RDD of Person objects from a text file, convert it to a Dataframe
    val inputDF = spark
      .readStream
      .schema(schema)
      .load(sourceFile)
    println(inputDF)


  }
}
