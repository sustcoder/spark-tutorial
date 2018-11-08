package com.liyz.demo

import com.liyz.spark.starter.Catalogs_Tutorial

class DfNaDemo {

}

object formatColumn extends Catalogs_Tutorial {
  val donuts = Seq((" plain  donut", 1.5, "2018-04-17"), (" vanilla donut ", 2.0, "2018-08-18")
    , (" glazed donut ", 3.1, "2019-09-10"), (null.asInstanceOf[String], 4.0, "2018-10-13"))
  val df = sparkSession.createDataFrame(donuts).toDF("name", "price", "date")

}

object modeDemo extends Catalogs_Tutorial {
  val array = Array(23, 29, 30, 32, 23, 21, 33, 33)

  /**
    * def foldLeft[B](z: B)(op: (B, A) => B): B
    * B: Map.empty[Int,Int]
    * (B, A) => B
    * (a -> (m.getOrElse(a, 0) + 1)) == Map(a -> (m.getOrElse(a, 0) + 1))
    */
  def modeUdf(arr: Array[Int]): Int = {
    val map = arr.foldLeft(Map.empty[Int, Int]) {
      (m, a) => m + (a -> (m.getOrElse(a, 0) + 1))
    }
    map.maxBy(_._2)._1
  }
  println(s"array mode is ${modeUdf(array)}")

  val df=sparkSession.sparkContext.parallelize(array)


}

