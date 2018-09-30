package com.liyz.spark

import org.apache.spark.{SparkConf, SparkContext}

object LocalWorldCount {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", "E:\\data\\gitee\\hadoop-2.6.0")
    val conf=new SparkConf()
    conf.setAppName("my first spark local App")
    conf.setMaster("local")
    val sc=new SparkContext(conf)
    val lines=sc.textFile("file:\\E:\\data\\gitee\\run-spark\\src\\main\\resources\\worldCount.txt")
    val words=lines.flatMap(line=>line.split(" "))
    val nums=words.map(word=>word+1)
    nums.foreach(num=>println(num))
    sc.stop()
  }
}
