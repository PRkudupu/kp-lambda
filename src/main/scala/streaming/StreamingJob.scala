package batch
package clickstream
import domain.Activity
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
    import spark.implicits._
    import org.apache.spark.sql.functions._

    val MS_IN_HOUR = 1000 * 60 * 60
    // Create an RDD of Person objects from a text file, convert it to a Dataframe
    val inputDF = spark.sparkContext
      .textFile(sourceFile)
      .map(_.split("\\t"))
      //convert the input rdd to an rdd of case class type activity
      .map(record => Activity(record(0).toLong / MS_IN_HOUR * MS_IN_HOUR, record(1), record(2), record(3), record(4), record(5), record(6)))
      .toDF()

    //We change the timestamp hour a month ahead
    val df = inputDF.select(
      add_months(from_unixtime(inputDF("timestamp_hour") / 1000), 1).as("timestamp_hour")
      , inputDF("referrer")
      , inputDF("action")
      , inputDF("prevPage")
      , inputDF("page")
      , inputDF("visitor")
      , inputDF("product")
    ).cache()
    df.show()

    // Register the DataFrame as a temporary view
    df.createOrReplaceTempView("activity")

    /*based on the action generate columns as
    purchase_count, add_to_cart,page_view_count */
    val activityByProduct=spark.sqlContext.sql(
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
    activityByProduct.show()
    /* write the aggregated data to hdfs
      partition the data by timestamp_hour
      write as parquet
     */

    activityByProduct.write
      .partitionBy("timestamp_hour")
      .mode("Append")
      .parquet("C:\\Boxes\\spark-kafka-cassandra-applying-lambda-architecture\\vagrant\\lambda\\batch1")
  }
}
