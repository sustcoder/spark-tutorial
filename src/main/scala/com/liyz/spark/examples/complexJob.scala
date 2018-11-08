package com.liyz.spark.examples

import org.apache.spark.{HashPartitioner, SparkContext}

class complexJob {

}

/**
  * https://spark-internals.books.yourtion.com/markdown/3-JobPhysicalPlan.html
  */
object complexJob {
  def main(args: Array[String]) {

    val sc = new SparkContext("local", "ComplexJob test")

    val data1 = Array[(Int, Char)](
      (1, 'a'), (2, 'b'),
      (3, 'c'), (4, 'd'),
      (5, 'e'), (3, 'f'),
      (2, 'g'), (1, 'h'))
    val rangePairs1 = sc.parallelize(data1, 3)

    val hashPairs1 = rangePairs1.partitionBy(new HashPartitioner(3))


    val data2 = Array[(Int, String)]((1, "A"), (2, "B"),
      (3, "C"), (4, "D"))

    val pairs2 = sc.parallelize(data2, 2)
    val rangePairs2 = pairs2.map(x => (x._1, x._2.charAt(0)))


    val data3 = Array[(Int, Char)]((1, 'X'), (2, 'Y'))
    val rangePairs3 = sc.parallelize(data3, 2)


    val rangePairs = rangePairs2.union(rangePairs3)

    val result = hashPairs1.join(rangePairs)

    result.foreach(x=>println(s"(${x._1},(${x._2._1},${x._2._2}))"))

    println(result.toDebugString)
  }

  /**
    * (3) MapPartitionsRDD[8] at join at complexJob.scala:37 []
    * |  MapPartitionsRDD[7] at join at complexJob.scala:37 []
    * |  CoGroupedRDD[6] at join at complexJob.scala:37 []
    * |  ShuffledRDD[1] at partitionBy at complexJob.scala:20 []
    * +-(3) ParallelCollectionRDD[0] at parallelize at complexJob.scala:18 []
    * +-(4) UnionRDD[5] at union at complexJob.scala:34 []
    * |  MapPartitionsRDD[3] at map at complexJob.scala:27 []
    * |  ParallelCollectionRDD[2] at parallelize at complexJob.scala:26 []
    * |  ParallelCollectionRDD[4] at parallelize at complexJob.scala:31 []
    */
  /**
    * (3,c,C)
    * (3,f,C)
    * (4,d,D)
    * (1,a,A)
    * (1,a,X)
    * (1,h,A)
    * (1,h,X)
    * (2,b,B)
    * (2,b,Y)
    * (2,g,B)
    * (2,g,Y)
    */
}

