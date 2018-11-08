// scalastyle:off println
package com.liyz.spark.examples

import com.liyz.spark.starter.Context

import scala.math.random

object SparkPi extends Context{
  def main(args: Array[String]) {
//    val sparkSession = SparkSession.builder.appName("Spark Pi").getOrCreate()
    val slices = if (args.length > 0) args(0).toInt else 100
    val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = sparkSession.sparkContext.parallelize(1 until n, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x*x + y*y <= 1) 1 else 0
    }.reduce(_ + _)
    println("Pi is roughly " + 4.0 * count / (n - 1))
    sparkSession.stop()
  }
}

