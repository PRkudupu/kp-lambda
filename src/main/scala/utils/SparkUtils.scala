package utils

import org.apache.spark.sql.SparkSession

object SparkUtils {

  def getSparkSession(appName:String):Any ={
    //Create a spark session specify the app name
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName(appName)
      .getOrCreate()
     spark
  }

}
