package com.liyz.spark

import org.apache.spark.{SparkConf, SparkContext}
// https://www.cnblogs.com/frankdeng/p/9301485.html
// https://toutiao.io/posts/ijq61/preview
object HelloSpark {

  /**
    * 调用spark集群时需要将bak目录中的配置文件拷贝到resources目录下
    * @param args
    */
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir","");
    System.setProperty("HADOOP_USER_HOME","HDFS")
    System.setProperty("HADOOP_USER_NAME","hadoop")
    System.setProperty("user.name","hadoop")
    var sparkConf=new SparkConf()
    sparkConf.setAppName("helloSpark").setMaster("spark://node2:7077")
    val sc=new SparkContext(sparkConf)
    sc.addJar("E:\\data\\gitee\\run-spark\\target\\scala-2.11\\run-spark_2.11-0.1.jar")
    val hdfsRdd=sc.textFile("hdfs://node3:9000/test/hello.txt")
    val mapRdd=hdfsRdd.flatMap(line=> line.split(" "))
    println(mapRdd.foreach(l=>println(l)))
    mapRdd.saveAsTextFile("hdfs://node3:9000/test/mapRdd2")
    println("the all world is : "+mapRdd.count())
   sc.stop()
  }
}
