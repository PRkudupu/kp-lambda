
package streaming

import com.datastax.spark.connector._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
object sampleCassandra {
  def main(args: Array[String]): Unit = {
    //location for data source
    val sourceFile = "C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\input"

    //Create a spark session specify the app name
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("Spark streaming")
      .getOrCreate()

    //Set spark configuration
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "localhost")
    //Read from cassandra keyspace
    val test_spark_rdd = spark.sparkContext
      .cassandraTable("test", "my_table")
    println(test_spark_rdd.first())
    println(conf)
  }
}
