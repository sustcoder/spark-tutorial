package com.liyz.spark

import org.apache.spark.{SparkConf, SparkContext}

object LocalWorldCount {
  def main(args: Array[String]): Unit = {
//    System.setProperty("hadoop.home.dir", "E:\\data\\gitee\\hadoop-2.6.0")
    val conf=new SparkConf()
    conf.setAppName("my first spark local App")
    conf.setMaster("local")
    val sc=new SparkContext(conf)
    val lines=sc.textFile("file:\\E:\\data\\gitee\\spark-tutorial\\src\\main\\resources\\worldCount.txt")
    val words=lines.flatMap(line=>line.split(" "))
    val pairs=words.map(word=>(word,1))
    val worldCount=pairs.reduceByKey(_+_)
    /**
      * 在这里通过reduceByKey方法之后可以获得每个单词出现的次数
      * 第一个map将单词和出现的次数交换，将出现的次数作为key，使用sortByKey进行排序（false为降序）
      * 第二个map将出现的次数和单词交换，这样还是恢复到以单词作为key
      */
    val sortedWordCount=worldCount.map(pair=>(pair._2,pair._1)).sortByKey(true).map(pair=>(pair._2,pair._1))
    // 打印结果，使用collect会将集群中的数据收集到当前运行drive的机器上，需要保证单台机器能放得下所有数据
    sortedWordCount.collect.foreach(println)
    sc.stop()
  }
}
