package batch

import com.typesafe.config.ConfigFactory
import domain.Activity
import org.apache.spark.sql.SparkSession

object BatchJob {
  def main(args: Array[String]): Unit = {

    //Spark session
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("Spark SQL basic example")
      .getOrCreate()

    //set spark log level
    spark.sparkContext.setLogLevel("ERROR")

    val defaultConfig = ConfigFactory.parseResources("defaults.conf")
    println(defaultConfig.getString("root.environment"))
    println(spark.version)

    val sourceFile = "C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\data.tsv"
    // For implicit conversions from RDDs to DataFrames
    import spark.implicits._

    val MS_IN_HOUR = 1000 * 60 * 60
    // Create an RDD of Person objects from a text file, convert it to a Dataframe
    val df = spark.sparkContext
      .textFile(sourceFile)
      .map(_.split("\\t"))
      .map(record => Activity(record(0).toLong / MS_IN_HOUR * MS_IN_HOUR, record(1), record(2), record(3), record(4), record(5), record(6)))
      .toDF()

    // Register the DataFrame as a temporary view
    df.createOrReplaceTempView("activity")
    val visitorsByProduct=spark.sqlContext.sql(
                                  //triple quote is used for multiline code
                                  """SELECT
                                   product,
                                    timestamp_hour,
                                    sum(case when action ='purchase' then 1 else 0 end) as purchase_count,
                                    sum(case when action ='add_to_cart' then 1 else 0 end) as add_to_cart_count,
                                    sum(case when action ='page_view' then 1 else 0 end) as page_view_count
                                    from activity
                                    group by product, timestamp_hour
                                   """).cache()
                                visitorsByProduct.show()

    visitorsByProduct.write
      .option("header", "true")

      .mode("overwrite")
      .save("C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\output")


  }
}
