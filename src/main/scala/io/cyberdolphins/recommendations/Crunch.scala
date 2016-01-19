package io.cyberdolphins.recommendations

import java.util.UUID

import io.cyberdolphins.recommendations.commons.CsvParser
import org.apache.spark.{SparkContext, SparkConf}
import org.joda.time.DateTime

/**
  * Created by mikwie on 19/01/16.
  */
object Crunch extends App {



  val path = "/Users/mikwie/opigram/ratings.csv"

  val sparkConf = new SparkConf()
    .setAppName("SparkApp")
    .setMaster("local[10]")

  val sparkCtx = new SparkContext(sparkConf)

  val answers = sparkCtx.textFile(path).map(CsvParser.parse).map {
    case a :: b :: c :: Nil => Answer(a.toLong, b, c.toDouble)
  }

  val starWarsEnthusiasts = answers.filter {
    case Answer(_, variable, rat) =>
      variable == Constants.starWarsRatingVar && rat > 2
  }.map(_.user).collect().toSet

  val starWarsEnthusiastsBroadcast = sparkCtx.broadcast(starWarsEnthusiasts)

  val positiveRatingsOfStarWarsEnthusiasts = answers.filter {
    case Answer(user, variable, rat) =>
      starWarsEnthusiastsBroadcast.value.contains(user) &&
        variable.startsWith("ratings_recoded") &&
        rat > 2
  }

  val scores = positiveRatingsOfStarWarsEnthusiasts.map {
    answer => answer.variable -> answer.value
  }.reduceByKey(_ + _).collect()

  scores.sortBy(_._2)(Ordering[Double].reverse).take(10).foreach(println)

}

object Constants {

  val starWarsRatingVar = "ratings_recoded_68aecba0-be6f-11e3-8f88-005056900044"

}

case class Answer(user: Long, variable: String, value: Double)

