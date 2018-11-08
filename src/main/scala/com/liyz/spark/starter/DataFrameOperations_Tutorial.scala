package com.liyz.spark.starter

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.desc

class DataFrameOperations_Tutorial {

}
object setUpDataFrames extends Catalogs_Tutorial{
  dfTags.show(10)
  dfQuestionsCSV.show(10)
  dfQuestions.show(10)
  dfQuestions.describe().show()
}

case class Tag(id:Int,tag:String)

object dfAsDataSetDemo extends Catalogs_Tutorial{
  import sparkSession.implicits._
  val dfTagsOfTag:Dataset[Tag]=dfTags.as[Tag]
  dfTagsOfTag.take(10).foreach(tag=>println(s"id=${tag.id},tag=${tag.tag}"))
}

case class Question(owner_userid: Int, tag: String, creationDate: java.sql.Timestamp, score: Int)

object dfToCaseClassByMap extends Catalogs_Tutorial{
    def toQuestion(row:org.apache.spark.sql.Row): Question ={
      // val 变量名：类型
      // 返回类型为一个匿名函数，函数的返回类型即为变量的返回类型
      //  String=>Option[Int] 为入参和出参的定义
      // val IntOf:(String=>Option[Int])=(str:String)= str match{
    val IntOf: String=>Option[Int]=_ match {
      case str if str =="NA"=> None
      case str => Some(str.toInt)
    }

    import java.time._
    val DateOf: String => java.sql.Timestamp = _ match {
      case s => java.sql.Timestamp.valueOf(ZonedDateTime.parse(s).toLocalDateTime)
    }

    Question(
      owner_userid = IntOf(row.getString(0)).getOrElse(-1),
      tag=row.getString(1),
      creationDate = row.getTimestamp(2),//DateOf(row.getString(2)),
      score = row.getInt(3)
    )
  }


  import sparkSession.implicits._
  val dfQuestionsJioned = dfQuestionsCSV
    .filter("score > 400 and score < 410")
    .join(dfTags, "id")
    .select("owner_userid", "tag", "creation_date", "score")
    .toDF()
  dfQuestions.printSchema()
  val dfOfQuestion: Dataset[Question] = dfQuestionsJioned.map(row => toQuestion(row))
  dfOfQuestion.take(10).foreach(q=>println(s"owner userid = ${q.owner_userid}, tag = ${q.tag}, creation date = ${q.creationDate}, score = ${q.score}"))

}

object createDfFromCollection extends Catalogs_Tutorial {
  //create a Dataset using spark.range starting from 5 to 100, with increments of 5
  val numDs=sparkSession.range(1,100,5)
  numDs.describe().show()
  // reverse the order and display first 5 items
  numDs.orderBy(desc("id")).show(5)

  // create a dataFrame using spark.createDataFrame form a list or seq
  val langPercentDF=sparkSession.createDataFrame(List(("scala",40),("Python",50),("R",10)))
  // rename the columns
  val lpDF=langPercentDF.withColumnRenamed("_1","language")
    .withColumnRenamed("_2","percent")
  // order the dataFrame in descending order of percentage
  lpDF.orderBy(desc("percent")).show()

  import sparkSession.implicits._
  val list=List(("scala",40),("Python",50),("R",10))
  val dfMoreTags=list.toDF("language","percent")
  sparkSession.createDataFrame(list).toDF("language","percent").show(10)
  dfMoreTags.show(10)
  val columnNames:Array[String]=dfMoreTags.columns
  columnNames.foreach(name=>println(s"get columns names: $name"))
  println(s"Does percent column exist:${columnNames.contains("percent")} ")

  val (columnNameArray,columnDataTypes)=dfMoreTags.dtypes.unzip
  val columnDataTypes1=dfMoreTags.dtypes
  println(s"DataFrame column names=${columnDataTypes.mkString(",")}")
  println(s"DataFrame column data types=${columnDataTypes1.mkString(",")}")

}

object dfOperation extends Catalogs_Tutorial{
  import sparkSession.implicits._
  val seqTags=Seq(1-> "so_java",1-> "so_jsp",2->"so_erlang",3->"so_scala",3->"so_kafka")
  val dfMoreTags=seqTags.toDF("id","tag")
  val dfUnionOfTags=dfTags.union(dfMoreTags).filter("id in (1,3)")
  dfUnionOfTags.show(10)

  val seqTags1=Seq(1-> "so_java",1-> "so_jsp",2->"so_erlang",4->"so_react")
  val dfMoreTags1=seqTags1.toDF("id","tag")
  val dfIntersectionTags=dfMoreTags.intersect(dfMoreTags1).show()
  import org.apache.spark.sql.functions._
  dfMoreTags.withColumn("tmp",rand(seed=27)).show(10)
  dfMoreTags.withColumn("tmp",split($"tag", "_")).show(10)
  dfMoreTags.withColumn("tmp",split($"tag","_"))
    .select($"id",$"tag",$"tmp".getItem(0).as("so_prefix"),$"tmp".getItem(1)as("so_tag"))
    .drop("tmp").show(10)
 }

import org.apache.spark.sql.functions._
object nestedJson extends Catalogs_Tutorial{
  import sparkSession.sqlContext.implicits._
  val tagsDF = sparkSession
    .read
    .option("multiLine", true)
    .option("inferSchema", false)
    .json("src/main/resources/tags_sample.json")
  tagsDF.printSchema()
  tagsDF.show()
  val df=tagsDF.select(explode($"stackoverflow") as "stackoverflow_tags")
  val renamedDf=df.select(
    $"stackoverflow_tags.tag.id" as "id",
    $"stackoverflow_tags.tag.author" as "author",
    $"stackoverflow_tags.tag.name" as "tag_name",
    $"stackoverflow_tags.tag.frameworks.id" as "frameworks_id",
    $"stackoverflow_tags.tag.frameworks.name" as "frameworks_name"
  )
  renamedDf.show()

  // use array_contains in df select
  renamedDf.select("*").where(array_contains($"frameworks_name","Play Framework")).show()

}

object splitDfArrayColumn extends Catalogs_Tutorial{
  import sparkSession.sqlContext.implicits._
  val targets=Seq(("Plain Donut",Array(1.5,2.0)),("Vanilla Donut",Array(2.0,2.50)),("Strawberry Donut",Array(2.50,3.50)))
  val df=sparkSession.createDataFrame(targets).toDF("name","prices")
  df.printSchema()
  df.show()
  val df2=df.select($"name",$"prices"(0) as "low price",$"prices"(1) as "high price")
  df2.show()
  df2.withColumn("Tasty",lit(true)).withColumn("constantCol",lit(1)).
    withColumn("stock min max",typedLit(Seq(100,500))).show()

  import org.apache.spark.sql.functions._
//  val stockMinMax: (String => Seq[Int])=(donutName:String) => donutName match {
  val stockMinMax: String => Seq[Int]= _ match {
    case "Plain Donut" => Seq(100,200)
    case "Vanilla Donut" => Seq(200,300)
    case "Strawberry Donut" => Seq(300,400)
    case _ => Seq(100,400)
  }
  val udfStockMinMax=udf(stockMinMax)
  val df3=df.withColumn("Stock min Max",udfStockMinMax($"name"))
  df3.show()

  println(s"the df first row=${df3.first()}")
  println(s"the df first row col 1 =${df3.first().get(0)}")
  println(s"parase first col to Double=${df3.first().getAs[Double]("prices")}")

}

object formatColumn extends Catalogs_Tutorial{
  val donuts=Seq((" plain  donut",1.5,"2018-04-17"),(" vanilla donut ",2.0,"2018-08-18")
    ,(" glazed donut ",3.1,"2019-09-10"),(null.asInstanceOf[String],4.0,"2018-10-13"))
  val df=sparkSession.createDataFrame(donuts).toDF("name","price","date")
  import org.apache.spark.sql.functions._
  import sparkSession.implicits._
  df.withColumn("price formatted",format_number($"price",4))
    .withColumn("name formatted",format_string("awesome %s",$"name"))
    .withColumn("name upper",upper($"name"))
    .withColumn("name lowwer",lower($"name"))
    .withColumn("date formatted",date_format($"date","yyyyMMdd"))
    .withColumn("year",year($"date"))
    .withColumn("month",month($"date"))
    .withColumn("day",dayofmonth($"date")).show()

  df.withColumn("Hash",hash($"name"))// nurmur3 hash as default
    .withColumn("MD5",md5($"name"))
    .withColumn("SHA1",sha1($"name"))
    .withColumn("SHA2",sha2($"name",256)).show()

  df.withColumn("index of the str",instr($"name","donut"))
    .withColumn("length",length($"name"))
    .withColumn("trim",trim($"name"))
    .withColumn("lTrim",ltrim($"name"))
    .withColumn("rTrim",rtrim($"name"))
    .withColumn("reverse",reverse($"name"))
    .withColumn("sub",substring($"name",0,5))
    .withColumn("concat",concat_ws(" - ",$"name",$"price"))
    .withColumn("first letter upper",initcap($"name"))
    .show()
  df.na.drop().show() // drop row has null
}
