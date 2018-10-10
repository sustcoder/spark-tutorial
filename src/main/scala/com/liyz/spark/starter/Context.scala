package com.liyz.spark.starter

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  *  A SparkSession takes a SparkConf where we've specified a name for our Spark application
  *  the Spark master which is our local node and also have limited the use of only 2 cores.
  *
  *  在Spark 2.0中，我们可以通过SparkSession来实现同样的功能，而不需要显式地创建SparkConf,
  *  SparkContext 以及 SQLContext，因为这些对象已经封装在SparkSession中。使用生成器的设计模式(builder design pattern)，
  *  如果我们没有创建SparkSession对象，则会实例化出一个新的SparkSession对象及其相关的上下文
  *  参考： https://www.iteblog.com/archives/1751.html
  */
trait Context {
    lazy val sparkConf=new SparkConf()
      .setAppName("learn spark")
      .setMaster("local[*]") // 使用windows本地的spark，只需将spark安装包解压然后配置环境变量即可
      .set("spark.cores.max","2")

    lazy val sparkSession=SparkSession
      .builder()
      .config(sparkConf)
      .getOrCreate()

    // 也可以在sparkSession创建好之后配置相关属性
    sparkSession.conf.set("spark.excutor.memory","512m")


    // 在设置完成后，打印所有属性
    val configMap:Map[String,String]=sparkSession.conf.getAll
    configMap.foreach(println)
}
