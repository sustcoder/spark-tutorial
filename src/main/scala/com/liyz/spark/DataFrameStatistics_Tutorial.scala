package com.liyz.spark

object basicStatisticFun extends Catalogs_Tutorial{
  import org.apache.spark.sql.functions._
  dfQuestions.select(avg("score")).show()
  dfQuestions.select(max("score")).show()
  dfQuestions.select(min("score")).show()
  dfQuestions.select(mean("score")).show()
  dfQuestions.select(sum("score")).show()
  //  a quick shortcut(捷径) to compute the count, mean, standard deviation（标准差）, min and max values from a DataFrame
  dfQuestions.describe().show()
}

/**
  * 聚集函数
  * Compute aggregates by specifying a series of aggregate columns. Note that this function by
  * * default retains the grouping columns in its output. To not retain grouping columns, set
  * * `spark.sql.retainGroupColumns` to false
  */
object aggregatesFun extends Catalogs_Tutorial{
  import org.apache.spark.sql.functions._
  dfQuestions.filter("id > 400 and id< 450")
    .filter("owner_userid is not null")
    .join(dfTags,dfQuestions.col("id").equalTo(dfTags("id")))
    .groupBy(dfQuestions.col("owner_userid"))
    .agg( avg("score"),max("answer_count"))
//    .sparkSession.conf.set("retainGroupColumns",false)
    .show()
}
import com.liyz.spark.advancedStatFun.dfQuestions
import org.apache.hadoop.yarn.util.RackResolver
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}


/**
  *  stat function: 统计函数 https://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/sql/DataFrameStatFunctions.html
  *  org.apache.spark.sql.DataFrameStatFunctions
  */

object advancedStatFun extends Catalogs_Tutorial{

  // Calculates the correlation(关联) of two columns of a DataFrame.
  // Currently only supports the Pearson Correlation Coefficient（皮尔逊相关系数）.
  // For Spearman（斯皮尔曼） Correlation, consider using RDD methods found in MLlib's Statistics.
  val correlation=dfQuestions.stat.corr("score","answer_count")
  println(s"correlation（相关性） between column score and answer_count=$correlation")
 // https://databricks.com/blog/2016/08/15/how-to-use-sparksession-in-apache-spark-2-0.html
   val df = sparkSession.range(0,10).withColumn("rand1", rand(seed=10))
    .withColumn("rand2", rand(seed=27))
  val correlation1=df.stat.corr("rand1", "rand2", "pearson")
  println(s"correlation（相关性） between column rand1 and rand2=$correlation1")
  val covariance=dfQuestions.stat.cov("score","answer_count")
  println(s"covariance(协方差) between column rand1 and rand2=$covariance")
  // frequent频繁项
  val dfFrequentScore=dfQuestions.stat.freqItems(Seq("answer_count")).show()
  // crosstab交叉表
  dfQuestions.filter("owner_userid>0 and owner_userid<20").stat.crosstab("score","owner_userid").show()

}

/**
  *  pivot: 透视 ，也叫做：行列转换、置换
  *   https://www.svds.com/pivoting-data-in-sparksql/
  *   https://juejin.im/post/5b1e343f518825137c1c6a27
  * Pivoting creates Pivot-table (sometimes called a cross-tab) which has 3 (or more) dimensions.
  * Normally a SQL table has two dimensions, just the column headers, and rows (with no row headers).
  * Note the Pivot table has both column and row headers,
  * and if there's more than 3 dimensions these row and column headers can have nested headers.
  *
  * To convert a SQL Table into a basic Pivot Table,
  * there's some column that needs to go into the third dimension, at the intersection between row and column headers,
  * and in order to display that cell in a two dimensional table, you need to collapse multiple values into a single value.
  * It can be any type of function that takes multiple values and returns a single value.
  * For example with strings you could take a max or a min,
  * but generally this works better for metrics that you are aggregating into meaningful values.
  * You might also use a flag or something to indicate that the value at the intersection exists.
  * If it doesn't exist, then it's not technically NULL, it's more akin to a hole in the crosstab.
  */
object crossTabDemo extends Catalogs_Tutorial{
  // TODO: schema createDataFrame(rdd,schema)
//  val schema =StructType(StructField("年月", StringType) :: StructField("项目", StringType):: StructField("收入", IntegerType) :: Nil)
  sparkSession.sparkContext.parallelize(
    List(("2018-01","项目1",100), ("2018-01","项目2",200), ("2018-01","项目3",300),
      ("2018-02","项目1",1000), ("2018-02","项目2",2000), ("2018-03","项目x",999)))
  val df=sparkSession.createDataFrame(
    List(("2018-01","项目1",100), ("2018-01","项目2",200), ("2018-01","项目3",300),
      ("2018-02","项目1",1000), ("2018-02","项目2",2000), ("2018-03","项目x",999)))
      .withColumnRenamed("_1","年月")
      .withColumnRenamed("_2","项目")
      .withColumnRenamed("_3","收入")
  df.createOrReplaceTempView("tab_project")
  df.show()


  /**
    * 透视操作简单直接，逻辑如下:
    *   按照不需要转换的字段分组，本例中是年月；
    *   使用pivot函数进行透视，透视过程中可以提供第二个参数来明确指定使用哪些数据项；
    *   汇总数字字段，本例中是收入；
    */
  val df_pivot=df.groupBy("年月")
    .pivot("项目")
    .agg(sum("收入"))
  df_pivot.show()

  /**
    * 逆透视Unpivot
    * Spark没有提供内置函数来实现unpivot操作，不过我们可以使用Spark SQL提供的stack函数来间接实现需求。有几点需要特别注意：
    * 使用selectExpr在Spark中执行SQL片段；
    * 如果字段名称有中文，要使用反引号**`** 把字段包起来；
    **/
    df_pivot.selectExpr("`年月`"
      ,"stack(4, '项目1', `项目1`,'项目2', `项目2`, '项目3', `项目3`, '项目x', `项目x`) as (`项目`,`收入`)")
      .filter("`收入` > 0 ")
    .show()
}

/**
  *  sampleBy: stratified sampling 分层抽样
  */
object stratifiedSamplingDemo extends Catalogs_Tutorial{

  // find all rows where answer_count in (5,10,20)
  val dfQuestionsByAnswerCount=dfQuestions.filter("owner_userid>0").filter("answer_count in (5,10,20)")
  // count how many rows match anser_count in (5,10,20)
  dfQuestionsByAnswerCount.groupBy("answer_count").count().show()
  // Create a fraction map where we are only interested:
  // - 50% of the rows that have answer_count = 5
  // - 10% of the rows that have answer_count = 10
  // - 100% of the rows that have answer_count = 20
  // Note also that fractions should be in the range [0, 1]
  val fractionKeyMap=Map(5->0.5,10->0.1,20->1.0)
  // Note also that you need to specify a random seed parameter as well
  dfQuestionsByAnswerCount.stat.sampleBy("answer_count",fractionKeyMap,7L)
    .groupBy("answer_count").count().show()
  dfQuestionsByAnswerCount.stat.sampleBy("answer_count",fractionKeyMap,37L)
    .groupBy("answer_count").count().show()
}


object ApproximateQuantileDemo extends Catalogs_Tutorial{
  val quantiles=dfQuestions.stat.approxQuantile("score",Array(0,0.5,1),0.25)
  println(s"Qauntiles segments = ${quantiles.toSeq}")
  dfQuestions.createOrReplaceTempView("so_questions")
  sparkSession
    .sql("select min(score), percentile_approx(score, 0.25), max(score) from so_questions")
    .show()
}

object BloomFilterDemo extends Catalogs_Tutorial{
  val tagsBloomFilter=dfTags.stat.bloomFilter("tag",1000L,0.1)
  println(s"bloom filter contains java tag = ${tagsBloomFilter.mightContain("java")}")
  println(s"bloom filter contains some unknown tag = ${tagsBloomFilter.mightContain("unknown tag")}")
}

object CountMinSketchDemo extends Catalogs_Tutorial{
  // Count Min Sketch
  val cmsTag = dfTags.stat.countMinSketch("tag", 0.1, 0.9, 37)
  val estimatedFrequency = cmsTag.estimateCount("java")
  println(s"Estimated frequency for tag java = $estimatedFrequency")
}

object SamplingWithReplacementDemo extends Catalogs_Tutorial{
  // Sampling With Replacement
  val dfTagsSample = dfTags.sample(true, 0.2, 37L)
  println(s"Number of rows in sample dfTagsSample = ${dfTagsSample.count()}")
  println(s"Number of rows in dfTags = ${dfTags.count()}")
}