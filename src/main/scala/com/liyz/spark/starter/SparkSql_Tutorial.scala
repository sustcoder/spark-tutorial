package com.liyz.spark.starter

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.when


trait DataFrame_Tutorial extends App with Context {
  // create a dataFrame from reading a CVS file
  val dfTags = sparkSession.read.option("header", "true")
    .option("inferSchema", "true") // 推断schema(每一列的数据类型)
    .csv("src/main/resources/question_tags_10K.csv")
    .toDF("id", "tag")
  // print dataFrame schema
  dfTags.printSchema()

  val dfQuestionsCSV = sparkSession.read.option("header", "true")
    .option("inferSchema", "true")
    .option("dateFormat", "yyyy-MM-dd HH:mm:ss")
    .csv("src/main/resources/questions_10k.csv")
    .toDF("id", "creation_date", "closed_date", "deletion_date", "score", "owner_userid", "answer_count")
  dfQuestionsCSV.printSchema()

  // Although we've passed in the inferSchema option, Spark did not fully match the data type for some of our columns.
  // Column closed_date is of type string and so is column owner_userid and answer_count.
  val dfQuestions = dfQuestionsCSV.select(
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

object dfTagsOperation extends DataFrame_Tutorial {

  dfTags.show(10)
  dfTags.select("tag").show(10)
  dfTags.filter("tag=='php'").show(10)
  println(s"Nums of php tags=${dfTags.filter("tag=='php'").count()}")
  dfTags.filter("tag like 's%'").filter("id==25 or id%10==2").show(10)
  dfTags.filter("id in (25,592,1042)").show(10)
  dfTags.groupBy("tag").count().show(10)
  dfTags.groupBy("tag").count().filter("count > 5").show(10)
  dfTags.groupBy("tag").count().orderBy("count").show(10)
  dfTags.select("tag").distinct().show(10)
}

// jion 参考：http://www.powerxing.com/sql-join/
// jion源码解析： https://github.com/ColZer/DigAndBuried/blob/master/spark/spark-join.md
object dfQuestionsOperation extends DataFrame_Tutorial {
  dfQuestions.show(10)
  val dfQuestionsSubset = dfQuestions.filter("score>400 and score < 410").toDF()
  dfQuestionsSubset.show(10)

  dfQuestionsSubset.join(dfTags, "id").show(10)
  dfQuestionsSubset.join(dfTags, dfTags("id") === dfQuestionsSubset("id")).show(10)
  dfQuestionsSubset.join(dfTags, "id").select("owner_userid", "tag", "creation_date", "score").show(10)
  // left_semi: 相当于In操作，left_anti：相当于not in操作 cross_jion ： 笛卡尔积 , left_jion==left_outer_jion:
  dfQuestionsSubset.join(dfTags, Seq("id"), "inner").show(10)
  dfQuestionsSubset.join(dfTags, Seq("id"), "left").show(10)
  dfQuestionsSubset.join(dfTags, Seq("id"), "left_outer").show(10)
  dfQuestionsSubset.join(dfTags, Seq("id"), "left_anti").show(10)
}

object temporaryTabOperation extends DataFrame_Tutorial {
  // instead of operating directly on the dataFrame dfTags,we will register it as a tmp table in Spark`s catalog and name the table so_tags
  dfTags.createOrReplaceTempView("so_tags")
  // list all tables in Spark`s catalog
  sparkSession.catalog.listTables().show()
  sparkSession.sql("show tables").show()

  // DataFrame Query
  dfTags.select("id", "tag").show(10)
  // SparkSQL query
  sparkSession.sql("select id,tag from so_tags limit 10").show()
  sparkSession.sql("select id,tag from so_tags where tag='php'").show(10)

  // scala中创建多行字符串  http://rscala.com/index.php/175.html
  /**
    * 要在Scala中创建多行字符串，就需要了解Scala的Multiline String。在Scala中，利用三个双引号包围多行字符串就可以实现
    *
    * 同时更有趣的是利用stripMargin.replaceAll方法，还可以将多行字符串”合并”一行显示。
    * val speech = “””Let us scala and
    * learn spark oh”””.stripMargin.replaceAll(“\n”, ” “)
    */
  sparkSession.sql(
    """select
      |count(*) as php_count
      |from so_tags where tag='php'""".stripMargin).show(10)

  sparkSession.sql(
    """select *
      from so_tags
      where tag like 's%'
    """.stripMargin.replaceAll("\n", "")).show(10)

  sparkSession.sql("select tag from so_tags where tag like 's%' and id in (25,100) ").show(10)
  sparkSession.sql("select tag,count(*) as count from so_tags " +
    "group by tag having count>5 order by tag").show(10)
}

object sparkSqlInner extends DataFrame_Tutorial {
  dfTags.createOrReplaceTempView("so_tags")
  val dfQuestionsSubset = dfQuestions.filter("score>400 and score < 410").toDF()
    .createOrReplaceTempView("so_questions")
  sparkSession.sql(
    """select t.*,q.*
      |from so_questions q
      |inner join so_tags t
      |on t.id=q.id
    """.stripMargin).show(10)
}

// 自定义函数
object userDefinedFunction extends DataFrame_Tutorial {
  dfTags.createOrReplaceTempView("so_tags")

  // step 1: def a function: function to prefix a String with so_ short
  def prefixStr(s: String): String = s"so_$s"

  // step 2: register User Defined Function(UDF) to sparkSession
  sparkSession.udf.register("prefix_so", prefixStr _) // prefixStr _ == prefixStr(_)
  // step3: use udf prefix_so to augment each tag value with so_
  sparkSession.sql("""select id,prefix_so(tag) from so_tags""".stripMargin).show(10)
}

object modeFunInSqlDemo extends DataFrame_Tutorial {

  def modeUdf(arr: Array[Row]): Int = {
    val map = arr.foldLeft(Map.empty[Int, Int]) {
      (m, a) => m + (a.getInt(0) -> (m.getOrElse(a.getInt(0), 0) + 1))
    }
    map.maxBy(_._2)._1
  }

  sparkSession.udf.register("modeUdf", modeUdf _)

  val rdd1 = sparkSession.createDataFrame(Seq((1, 1), (2, 2), (3, 2), (4, 3), (4, 0))).toDF("id", "age")
  dfQuestionsCSV.na.fill(0)
  rdd1.createTempView("tab_rdd1")
  val modeScore = modeUdf(rdd1.select("age").rdd.collect())
  println(modeScore)
  //  sparkSession.udf.register("get_mode",new com.liyz.spark.starter.UDAFGetMode)
  //  sparkSession.sql(s"select get_mode(age) as modeAge from tab_rdd1").show()

  rdd1.show()

//  import org.apache.spark.sql.functions._ // for `when`
  rdd1.withColumn("age", when(rdd1("age") === 0, modeScore).otherwise(rdd1("age"))).show()
}







