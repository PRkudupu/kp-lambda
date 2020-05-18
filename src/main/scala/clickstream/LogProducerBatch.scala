package clickstream

import java.io.FileWriter

import config.Settings

import scala.util.Random

object LogProducerBatch extends App {
  // reference to webLog config
  val wlc = Settings.WebLogGen

  //Load the resource files
  val Products = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/products.csv")).getLines().toArray
  val Referrers = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/referrers.csv")).getLines().toArray
  //random list of visitors based on range specified in the config file
  //Underscore replaces the item that we are iterating.
  val Visitors = (0 to wlc.visitors).map("Visitor-" + _)
  //These are sequence of strings
  val Pages = (0 to wlc.pages).map("Page-" + _)

  val rnd = new Random()

  val filePath = wlc.filePath
  val fw = new FileWriter(filePath, true)

  // introduce a bit of randomness to time increments for demo purposes.Used to increment time
  val incrementTimeEvery = rnd.nextInt(math.min(wlc.records, 100) - 1) + 1

  //time stamp
  var timestamp = System.currentTimeMillis()
  var adjustedTimestamp = timestamp
  //iteration through no of records what we have. Adjust the timestamp based on the multiplier
  for (iteration <- 1 to wlc.records) {
    adjustedTimestamp = adjustedTimestamp + ((System.currentTimeMillis() - timestamp) * wlc.timeMultiplier)
    timestamp = System.currentTimeMillis() // move all this to a function
    //Action based on the random no, whether it is a purchase/add_to_cart/page_view
    val action = iteration % (rnd.nextInt(200) + 1) match {
      case 0 => "purchase"
      case 1 => "add_to_cart"
      case _ => "page_view"
    }
    //If it is a referrer it would match the previous page
    val referrer = Referrers(rnd.nextInt(Referrers.length - 1))
    val prevPage = referrer match {
      case "Internal" => Pages(rnd.nextInt(Pages.length - 1))
      case _ => ""
    }
    val visitor = Visitors(rnd.nextInt(Visitors.length - 1))
    val page = Pages(rnd.nextInt(Pages.length - 1))
    val product = Products(rnd.nextInt(Products.length - 1))
    //Generate line and format
    val line = s"$adjustedTimestamp\t$referrer\t$action\t$prevPage\t$visitor\t$page\t$product\n"
    fw.write(line)
    //Introduce a sleep based on the number of messages what we have sent
    //Multiplier of 60 to make sure that it would not run fast
    if (iteration % incrementTimeEvery == 0) {
      println(s"Sent $iteration messages!")
      val sleeping = rnd.nextInt(1500)
      println(s"Sleeping for $sleeping ms")
      Thread sleep sleeping
    }

  }
  fw.close()
}