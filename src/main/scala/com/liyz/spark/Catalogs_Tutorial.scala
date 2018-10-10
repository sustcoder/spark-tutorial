package com.liyz.spark

import com.liyz.spark.dfToCaseClassByMap.{dfQuestionsCSV, dfTags, toQuestion}
import org.apache.hadoop.yarn.util.RackResolver
import org.apache.spark.sql.Dataset

trait Catalogs_Tutorial  extends App with Context {


  // 关闭org和akka的日志 https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-logging.html
  import org.apache.log4j.{Level, Logger}
  Logger.getLogger(classOf[RackResolver]).getLevel
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)


  // create a dataFrame from reading a CVS file
  val dfTags=sparkSession.read.option("header","true")
    .option("inferSchema","true") // 推断schema(每一列的数据类型)
    .csv("src/main/resources/question_tags_10K.csv")
    .toDF("id","tag")
  // print dataFrame schema
  dfTags.printSchema()
  val dfQuestionsCSV=sparkSession.read.option("header","true")
    .option("inferSchema","true")
    .option("dateFormat","yyyy-MM-dd HH:mm:ss")
    .csv("src/main/resources/questions_10k.csv")
    .toDF("id","creation_date","closed_date","deletion_date","score","owner_userid","answer_count")
  dfQuestionsCSV.printSchema()

  // Although we've passed in the inferSchema option, Spark did not fully match the data type for some of our columns.
  // Column closed_date is of type string and so is column owner_userid and answer_count.
  val dfQuestions=dfQuestionsCSV.select(
    dfQuestionsCSV.col("id").cast("integer"),
    dfQuestionsCSV.col("creation_date").cast("timestamp"),
    dfQuestionsCSV.col("closed_date").cast("timestamp"),
    dfQuestionsCSV.col("deletion_date").cast("date"),
    dfQuestionsCSV.col("score").cast("integer"),
    dfQuestionsCSV.col("owner_userid").cast("integer"),
    dfQuestionsCSV.col("answer_count").cast("integer")
  )
  dfQuestions.printSchema()

}
