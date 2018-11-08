package com.liyz.spark.starter

import org.apache.hadoop.yarn.util.RackResolver
import org.apache.spark.HashPartitioner
import org.apache.spark.sql.Dataset

class TransformationOperation_Tutorial {

}

/**
  * https://github.com/lupingqiu/spark-core-source-analysis/blob/master/RDD/RDD-basic-transformations.md
  * 图解RDD： https://spark-internals.books.yourtion.com/markdown/2-JobLogicalPlan.html
  */
class RddOp extends App with Context{
  // 关闭org和akka的日志 https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-logging.html
  import org.apache.log4j.{Level, Logger}
  Logger.getLogger(classOf[RackResolver]).getLevel
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  var data=sparkSession.read.textFile("src\\main\\resources\\worldCount.txt")
  def printRddInfo(dataset: Dataset[String])={
    println(dataset.rdd.toDebugString)
  }
}

object MapOp extends RddOp{
  import sparkSession.implicits._
  println(data.rdd.toDebugString)
  data.toDF().show()
  /**
    * +------------------+
    * |             value|
    * +------------------+
    * |                my|
    * |          my first|
    * |    my first spark|
    * |my first spark app|
    * +------------------+
    */
  // 将一个RDD中的每个数据项，通过map中的函数映射变为一个新的元素。输入分区与输出分区是一对一的，
  // 即：有多少个输入分区，就有多少个输出分区。
  var mapResult=data.map(line=>line+",")
  println(mapResult.rdd.toDebugString)
  mapResult.toDF().show()
  /**
    * +-------------------+
    * |              value|
    * +-------------------+
    * |                my,|
    * |          my first,|
    * |    my first spark,|
    * |my first spark app,|
    * +-------------------+
    */

  println(s"flatMap最后会将所有的输出分区合并成一个。")
  data.flatMap(line => line.split("\\s+")).toDF().show()
  /**
    * +-----+
    * |value|
    * +-----+
    * |   my|
    * |   my|
    * |first|
    * |   my|
    * |first|
    * |spark|
    * |   my|
    * |first|
    * |spark|
    * |  app|
    * +-----+
    */
  println(s"使用flatMap时候需要注意：flatMap会将字符串看成是一个字符数组。")
  data.map(_.toUpperCase).collect.flatMap(_.toUpperCase).foreach(println(_))
  println(s"distinct:对rdd元素去重")
  data.flatMap(_.split("")).distinct().collect().foreach(println)
  var rdd1=sparkSession.range(1,10,1,2)
 // 该函数和map函数类似，只不过映射函数的参数由RDD中的每一个元素变成了RDD中每一个分区的迭代器。
 // 如果在映射的过程中需要频繁创建额外的对象，使用mapPartitions要比map高效的多。
 // 比如，将RDD中的所有数据通过JDBC连接写入数据库，如果使用map函数，
 // 可能要为每一个元素都创建一个connection，这样开销很大，如果使用mapPartitions，那么只需要针对每一个分区建立一个connection。
  val rdd2=rdd1.mapPartitions(iterator=>{
    var result=List[Int]()
    var i=0
    while (iterator.hasNext){
      i += iterator.next().intValue()
    }
    result.::(i).iterator
  })
  rdd2.toDF().show()
  /**
    * +-----+
    * |value|
    * +-----+
    * |   10|
    * |   35|
    * +-----+
    */

}

object CoalesceOp extends RddOp{
  /**
    * 该函数用于将RDD进行重分区，使用HashPartitioner。
    * 第一个参数为重分区的数目，第二个为是否进行shuffle，默认为false，当默认为false时，
    * 重分区的数目不能大于原有数目; 第三个参数表示如何重分区的实现，默认为空。
    * def coalesce(numPartitions: Int, shuffle: Boolean = false,
    * partitionCoalescer: Option[PartitionCoalescer] = Option.empty)
    * (implicit ord: Ordering[T] = null)
    */
  println(s"rdd partitions size:${data.rdd.partitions.size} ")
  println(s"set rdd partitions to 1 ${data.rdd.coalesce(1)}")
  println(s"set rdd partisions to 3 ${data.rdd.coalesce(1,true)}")
  println(s"use repartition ${data.rdd.repartition(3)}")
}

object zipOp extends RddOp{
  var rdd1=sparkSession.range(1,4,1).rdd
  var rdd2=sparkSession.range(4,7,1).rdd
  var rdd3=sparkSession.range(7,10,1).rdd
  // zip函数用于将两个RDD组合成Key/Value形式的RDD,这里默认两个RDD的partition数量以及每个partition的元素数量都相同，否则会抛出异常。
  var rdd4=rdd1 zip rdd2
  sparkSession.createDataFrame(rdd4).show()
 // zipPartitions函数将多个RDD按照partition组合成为新的RDD，该函数需要组合的RDD具有相同的分区数，但对于每个分区内的元素数量没有要求。
 def myfunc(aiter: Iterator[Long], biter: Iterator[Long], citer: Iterator[Long])=
 {
   var res = List[String]()
   while (aiter.hasNext && biter.hasNext && citer.hasNext)
   {
     val x = aiter.next + " " + biter.next + " " + citer.next
     res ::= x
   }
   res.iterator
 }

//  var rdd5=rdd1.zipPartitions(rdd2,rdd3)(myfunc).collect
  // 该函数将RDD中的元素和这个元素在RDD中的ID（索引号）组合成键/值对。
  var rdd6=rdd1.zipWithIndex
  sparkSession.createDataFrame(rdd6).show()

  //   该函数将RDD中元素和一个唯一ID组合成键/值对，该唯一ID生成算法如下：
  //每个分区中第一个元素的唯一ID值为：该分区索引号
  //每个分区中第N个元素的唯一ID值为：(前一个元素的唯一ID值) + (该RDD总的分区数)
  var rdd7=sparkSession.range(1,10,2).rdd
  var rdd8=rdd7.zipWithUniqueId()
  sparkSession.createDataFrame(rdd8).show()

}

/**
  * 该函数根据partitioner函数生成新的ShuffleRDD，将原RDD重新分区
  * def partitionBy(partitioner: Partitioner): RDD[(K, V)]
  */
object partitionByOp extends RddOp {
  var rdd11 = sparkSession.sparkContext.makeRDD(Array((1, "A"), (2, "B"), (3, "C"), (4, "D")),2)
  // 函数作用同mapPartitions相同，不过提供了两个参数，第一个参数为分区的索引
  var rdd9 = rdd11.mapPartitionsWithIndex {
    (partIdx, iter) => {
      var part_map = scala.collection.mutable.Map[String, List[(Int, String)]]()
      while (iter.hasNext) {
        var part_name = "part_" + partIdx
        var elem = iter.next()
        if (part_map.contains(part_name)) {
          var elems = part_map(part_name)
          elems ::= elem
          part_map(part_name)=elems
        } else {
          part_map(part_name) = List[(Int, String)] {elem}
        }
      }
      part_map.iterator
    }
  }.collect()
  sparkSession.createDataFrame(rdd9).show
  /**
    * +------+--------------+
    * |    _1|            _2|
    * +------+--------------+
    * |part_0|[[2,B], [1,A]]|
    * |part_1|[[4,D], [3,C]]|
    * +------+--------------+
    */
  // 使用partitionBy重新分区,该函数根据partitioner函数生成新的ShuffleRDD
  var rdd2=rdd11.partitionBy(new HashPartitioner(2))
  var rdd3 = rdd2.mapPartitionsWithIndex {
    (partIdx, iter) => {
      var part_map = scala.collection.mutable.Map[String, List[(Int, String)]]()
      while (iter.hasNext) {
        var part_name = "part_" + partIdx
        var elem = iter.next()
        if (part_map.contains(part_name)) {
          var elems = part_map(part_name)
          elems ::= elem
          part_map(part_name) = elems
        } else {
          part_map(part_name) = List[(Int, String)] {elem}
        }
      }
      part_map.iterator
    }
  }.collect
  sparkSession.createDataFrame(rdd3).show()
  /**
    * +------+--------------+
    * |    _1|            _2|
    * +------+--------------+
    * |part_0|[[4,D], [2,B]]|
    * |part_1|[[3,C], [1,A]]|
    * +------+--------------+
    */
}

object mapValuesOp extends RddOp{
  var rdd1=sparkSession.sparkContext.makeRDD(Array((1, "A"), (2, "B"), (3, "C"), (4, "D")),2)
  // 同基本转换操作中的map类似，只不过mapValues是针对[K,V]中的V值进行map操作。
//  var rdd2=rdd1.mapValues(x=>x+"_")
  var rdd2=rdd1.mapValues(_+"_")
  sparkSession.createDataFrame(rdd2).show()
  /**
    * +---+---+
    * | _1| _2|
    * +---+---+
    * |  1| A_|
    * |  2| B_|
    * |  3| C_|
    * |  4| D_|
    * +---+---+
    */
  // K和V分别加“_”
  rdd1.map(x=>(x._1+"_",x._2+"_")).foreach(println(_))
  /**
    * (1_,A_)
    * (3_,C_)
    * (2_,B_)
    * (4_,D_)
    */
  // 对[K,V]整体操作
  var rdd3=rdd1.map(_+"_").foreach(println(_))
  /**
    * (1,A)_
    * (3,C)_
    * (2,B)_
    * (4,D)_
    */
  rdd1.map(_.swap).foreach(println(_))
  /**
    * (C,3)
    * (D,4)
    * (A,1)
    * (B,2)
    */

  // 对K和V分开操作，返回k+v
  rdd1.map(x=>x._1+x._2).foreach(println(_))
  /**
    * 3C
    * 4D
    * 1A
    * 2B
    */
  rdd1.flatMap(_+"_").foreach(println(_))
  //  this also retains the original RDD's partitioning.
  rdd1.flatMapValues(_+"_").foreach(println(_))
  /**
    * (1,A)
    * (3,C)
    * (1,_)
    * (3,_)
    * (2,B)
    * (4,D)
    * (2,_)
    * (4,_)
    */
  // 该函数用于RDD[K,V]根据K将V做折叠、合并处理，其中的参数zeroValue表示先根据映射函数将zeroValue应用于每一个V,
  // 对V进行初始化,再将映射函数应用于初始化后的V。
  val rdd4=sparkSession.sparkContext.makeRDD(Array(("A",0),("A",2),("B",1),("B",2),("C",1)))
  val rdd5=rdd4.foldByKey(2)(_+_).collect()
  sparkSession.createDataFrame(rdd5).show()
  /**
    * +---+---+
    * | _1| _2|
    * +---+---+
    * |  A|  6|
    * |  B|  7|
    * |  C|  3|
    * +---+---+
    */

  /**
    * 该函数用于将RDD[K,V]中每个K对应的V值，合并到一个集合Iterable[V]中，
    * 参数numPartitions用于指定分区数；参数partitioner用于指定分区函数。
    * 该方法代价较高，在求每个key的sum或者average时，推荐使用reduceByKey或者aggregateByKey。
    * 返回类型是： Array[(String, Iterable[Int])]
    * (C,CompactBuffer(1))
    * (A,CompactBuffer(0, 2))
    * (B,CompactBuffer(1, 2))
    */
  rdd4.groupByKey().foreach(println(_))
  /**

    */

  /**
    * 该函数用于将RDD[K,V]中每个K对应的V值根据映射函数来运算。参数numPartitions用于指定分区数；参数partitioner用于指定分区函数。
    * 返回类型是org.apache.spark.rdd.RDD[(String, Int)]
    * +---+---+
    * | _1| _2|
    * +---+---+
    * |  A|  2|
    * |  B|  3|
    * |  C|  1|
    * +---+---+
    */
  sparkSession.createDataFrame(rdd4.reduceByKey(_+_).collect).show
  /**
    * 该函数将RDD[K,V]中每个K对应的V值根据映射函数来运算，运算结果映射到一个Map[K,V]中，而不是RDD[K,V]
    * (A,2)
    * (B,3)
    * (C,1)
    */
  rdd4.reduceByKeyLocally(_+_).foreach(println(_))
}


/**
  * 参考：
  * https://www.jianshu.com/p/d7552ea4f882
  * https://cloud.tencent.com/developer/ask/98711
  * 该函数用于将RDD[K,V]转换成RDD[K,C],这里的V类型和C类型可以相同也可以不同。
  *
  * def combineByKey[C](
  * createCombiner: V => C,
  * mergeValue: (C, V) => C,
  * mergeCombiners: (C, C) => C): RDD[(K, C)] = self.withScope {
  * combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners)(null)
  * }
  *
  * 参数的含义如下：
  * createCombiner：组合器函数，用于将V类型转换成C类型，输入参数为RDD[K,V]中的V,输出为C
  * mergeValue：合并值函数，将一个C类型和一个V类型值合并成一个C类型，输入参数为(C,V)，输出为C, 在每个分区上执行
  * mergeCombiners：合并组合器函数，用于将两个C类型值合并成一个C类型，输入参数为(C,C)，输出为C,将不同分区的结果合并
  * numPartitions：结果RDD分区数，默认保持原有的分区数
  * partitioner：分区函数,默认为HashPartitioner
  * mapSideCombine：是否需要在Map端进行combine操作，类似于MapReduce中的combine，默认为true
  * serializer：序列化类，默认为null
  */
object combineByKeyOp extends RddOp{
  // 注意： mergeValue只有在已为此分区上找到的键创建了组合器(在本例中为tuple)时，才会触发
  var rdd1=sparkSession.sparkContext.makeRDD(Array(("A",1),("A",2),("B",3),("B",4),("C",1)),2)
  var rdd2=rdd1.combineByKey((v:Int)=>v + "_",(c:String,v:Int)=>c+"@"+v,(c1:String,c2:String)=>c1+"$"+c2).collect()
  sparkSession.createDataFrame(rdd2).show()
  /**
    * output:
    *  K类型相同才会执行
    * +---+----+
    * | _1|  _2|
    * +---+----+
    * |  B|3_@4|
    * |  A|1_@2|
    * |  C|  1_|
    * +---+----+
    */
  var rdd5=sparkSession.sparkContext.makeRDD(Array(("A",1),("B",3),("A",2),("B",4),("C",1)),2)
  var rdd6=rdd5.combineByKey((v:Int)=>v + "_",(c:String,v:Int)=>c+"@"+v,(c1:String,c2:String)=>c1+"$"+c2).collect()
  sparkSession.createDataFrame(rdd6).show()
  /**
    * output:
    * 不同partition的才会走mergeCombiners
    * +---+-----+
    * | _1|   _2|
    * +---+-----+
    * |  B|3_$4_|
    * |  A|1_$2_|
    * |  C|   1_|
    * +---+-----+
    */
  var rdd8=sparkSession.sparkContext.makeRDD(Array(("A",1),("A",2),("B",3),("A",2),("B",4),("B",1),("C",1)),2)
  var rdd9=rdd8.combineByKey((v:Int)=>v + "_",(c:String,v:Int)=>c+"@"+v,(c1:String,c2:String)=>c1+"$"+c2).collect()
  sparkSession.createDataFrame(rdd9).show()
  /**
    * output:
    * +---+-------+
    * | _1|     _2|
    * +---+-------+
    * |  B|3_$4_@1|
    * |  A|1_@2$2_|
    * |  C|     1_|
    * +---+-------+
    */

  // 对各个科目求平均值
  val scores = sparkSession.sparkContext.makeRDD(List(("chinese", 88) , ("chinese", 90) , ("math", 60), ("math", 87)),2)
  var avgScoresRdd=scores.combineByKey(
    (x:Int)=>(x,1),
    (c:(Int,Int),x:Int)=>(c._1+x,c._2+1),
    (c1:(Int,Int),c2:(Int,Int))=>(c1._1+c2._1,c1._2+c2._2))
   sparkSession.createDataFrame(avgScoresRdd).show()
   var avgScores=avgScoresRdd.map{ case (key, value) => (key, value._1 / value._2.toFloat) }//.map(x=>(x,(x._1/x._2))
   sparkSession.createDataFrame(avgScores).show()
}

/**
  * cogroup相当于SQL中的全外连接full outer join，返回左右RDD中的记录，关联不上的为空。参数numPartitions用于指定结果的分区数。参数partitioner用于指定分区函数。
  * //参数为1个RDD
  * def cogroup[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
  * def cogroup[W](other: RDD[(K, W)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W]))]
  * def cogroup[W](other: RDD[(K, W)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W]))]
  *
  * //参数为2个RDD
  * def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
  * def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
  * def cogroup[W1, W2](other1: RDD[(K, W1)], other2: RDD[(K, W2)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2]))]
  *
  * //参数为3个RDD
  * def cogroup[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)]): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
  * def cogroup[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)], numPartitions: Int): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
  * def cogroup[W1, W2, W3](other1: RDD[(K, W1)], other2: RDD[(K, W2)], other3: RDD[(K, W3)], partitioner: Partitioner): RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
  */
object cogroup extends RddOp {
  var rdd1 = sparkSession.sparkContext.makeRDD(Array(("A","1"),("B","2")),2)
  var rdd2 = sparkSession.sparkContext.makeRDD(Array(("A","3"),("C","4")),2)
  rdd1.cogroup(rdd2).collect().foreach(x=>println("("+x._1+","+x._2._1+","+x._2._2+")"))
  /**
    * output:
    * (B,CompactBuffer(2),CompactBuffer())
    * (A,CompactBuffer(1),CompactBuffer(3))
    * (C,CompactBuffer(),CompactBuffer(4))
    */
  var rdd3 = sparkSession.sparkContext.makeRDD(Array(("A","5"),("C","6"),("D","8")),2)
  rdd1.cogroup(rdd2,rdd3).collect().foreach(x=>println("("+x._1+","+x._2._1+","+x._2._2+x._2._3+")"))
  /**
    * output:
    * (B,CompactBuffer(2),CompactBuffer()CompactBuffer())
    * (D,CompactBuffer(),CompactBuffer()CompactBuffer(8))
    * (A,CompactBuffer(1),CompactBuffer(3)CompactBuffer(5))
    * (C,CompactBuffer(),CompactBuffer(4)CompactBuffer(6))
    */
}

/**
  * join相当于SQL中的内关联join，只返回两个RDD根据K可以关联上的结果，join只能用于两个RDD之间的关联，如果要多个RDD关联，多关联几次即可。
  * def join[W](other: RDD[(K, W)]): RDD[(K, (V, W))]
  * leftOuterJoin类似于SQL中的左外关联left outer join，返回结果以前面的RDD为主，关联不上的记录为空。只能用于两个RDD之间的关联，如果要多个RDD关联，多关联几次即可
  * def leftOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (V, Option[W]))]
  * rightOuterJoin类似于SQL中的有外关联right outer join，返回结果以参数中的RDD为主，关联不上的记录为空。只能用于两个RDD之间的关联，如果要多个RDD关联，多关联几次即可。
  * def rightOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (Option[V], W))]
  * cogroup相当于SQL中的全外连接full outer join，返回左右RDD中的记录，关联不上的为空。
  * def cogroup[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
  */
object jionOp extends RddOp{
  var rdd1 = sparkSession.sparkContext.makeRDD(Array(("A","1"),("B","2")),2)
  var rdd2 = sparkSession.sparkContext.makeRDD(Array(("A","3"),("C","4")),2)
  rdd1.join(rdd2).collect().foreach(x=>println(s"(${x._1},(${x._2._1},${x._2._2}))"))
  /**
    * (A,(1,3))
    */
  rdd1.leftOuterJoin(rdd2).collect().foreach(x=>println(s"(${x._1},(${x._2._1},${x._2._2}))"))
  /**
    * (B,(2,None))
    * (A,(1,Some(3)))
    */
  rdd1.rightOuterJoin(rdd2).collect().foreach(x=>println(s"(${x._1},(${x._2._1},${x._2._2}))"))
  /**
    * (A,(Some(1),3))
    * (C,(None,4))
    */
  rdd1.cogroup(rdd2).collect().foreach(x=>println(s"(${x._1},(${x._2._1},${x._2._2}))"))
  /**
    * (B,(CompactBuffer(2),CompactBuffer()))
    * (A,(CompactBuffer(1),CompactBuffer(3)))
    * (C,(CompactBuffer(),CompactBuffer(4)))
    */
}

/**
  * subtractByKey和基本转换操作中的subtract类似，返回在主RDD中出现，并且不在otherRDD中出现的元素。参数numPartitions用于指定结果的分区数;参数partitioner用于指定分区函数
  * def subtractByKey[W](other: RDD[(K, W)])(implicit arg0: ClassTag[W]): RDD[(K, V)]
  */
object subtractOp extends RddOp{
  var rdd1 = sparkSession.sparkContext.makeRDD(Array(("A","1"),("B","2")),2)
  var rdd2 = sparkSession.sparkContext.makeRDD(Array(("A","1"),("C","4")),2)
  rdd1.subtract(rdd2).collect().foreach(x=>println(s"(${x._1},${x._2})"))
  sparkSession.createDataFrame(rdd1.subtract(rdd2)).show()
  /**
    * (B,2)
    */
  sparkSession.createDataFrame(rdd2.subtract(rdd1)).show()
  /**
    * (C,4)
    */
  // Return an array that contains all of the elements in this RDD.
  rdd2.subtract(rdd1).collect().foreach(x=>println(s"(${x._1},${x._2})"))
  /**
    * +---+---+
    * | _1| _2|
    * +---+---+
    * |  C|  4|
    * +---+---+
    */
}

/**
  *  Acion算子实现里面都会有一个sc.runJob()方法，用来提交一个task，开始真正的计算
  */
object actionOp extends RddOp{
  var rdd1 = sparkSession.sparkContext.makeRDD(Array(("A","1"),("B","2"),("C","3")),2)
  println(s"rdd first element:${rdd1.first()}")
  println(s"rdd elements count:${rdd1.count()}")

//  val rdd2=rdd1.reduce((x:(String,String),y:(String,String)) => {(x._1+y._1,x._2+y._2)})

  // Return an array that contains all of the elements in this RDD.
  rdd1.collect().foreach(println)

  // take用于获取RDD中num个元素，不排序。
  rdd1.take(1).foreach(println )


  /**
    * top函数用于从RDD中，按照默认（降序）或者指定的排序规则，返回前num个元素。
    * This method should only be used if the resulting array is expected to be small, as
    * all the data is loaded into the driver's memory.
    */
  val rdd2=sparkSession.sparkContext.makeRDD(Seq(1,2,3,4,5))
//  rdd2.top(1).foreach(println(_))
//  // 指定排序规则
  implicit val myOrd = implicitly[Ordering[Int]].reverse
  rdd2.top(1)( myOrd).foreach(println(_))
  // takeOrdered和top类似，只不过以和top相反的顺序返回元素。
  // 如果top设置排序为升序，则takeOrdered排序方式降序，
  // 如果top使用默认排序，则takeOrdered排序方式为升序
  rdd2.takeOrdered(1).foreach(println(_))

}
object actionOp1 extends RddOp {
  var rdd1 = sparkSession.sparkContext.makeRDD(Array(("A", "1"),("A", "2"), ("A", "3"), ("C", "1")), 2)
  rdd1.countByValue().foreach(x=>println(s"(${x._1},${x._2})"))
  rdd1.map(x => (x, 1L)).reduceByKey(_ + _).foreach(x=>println(s"(${x._1},${x._2})"))
  /**
    * ((A,3),1)
    * ((A,1),1)
    * ((A,2),1)
    * ((C,1),1)
    */
  // def foreachPartition(f: (Iterator[T]) => Unit): Unit
  val rdd2=sparkSession.sparkContext.makeRDD(1 to 10,2)
  var allsize=sparkSession.sparkContext.accumulator(0)
  rdd2.foreachPartition { x => {
         allsize += x.size
        }}
 // sortBy根据给定的排序k函数将RDD中的元素进行排序。
  // def sortBy[K](f: (T) => K, ascending: Boolean = true, numPartitions: Int = this.partitions.length)(implicit ord: Ordering[K], ctag: ClassTag[K]): RDD[T]
//  rdd2.sortBy(_,false)

  // 按照V进行降序排序
  rdd1.sortBy(x=>x._2,false)

}
object aggregateOp extends RddOp{
  val rdd1=sparkSession.sparkContext.makeRDD(1 to 10,2)
  /**
    * aggregate用于聚合RDD中的元素，先使用seqOp将RDD中每个分区中的T类型元素聚合成U类型，
    * 再使用combOp将之前每个分区聚合后的U类型聚合成U类型，
    * 特别注意seqOp和combOp都会使用zeroValue的值，zeroValue的类型为U。
    *
    * def aggregate[U: ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U): U
    */
  var rdd2=rdd1.aggregate(1)({(x:Int,y:Int)=>x+y},{(a1:Int,a2:Int)=>a1+a2})
  println(s"the aggregate rst:${rdd2}") //rst: 58

  /**
    *  fold是aggregate的简化，将aggregate中的seqOp和combOp使用同一个函数op。
    *
    *  def fold(zeroValue: T)(op: (T, T) ⇒ T): T
    */
  val rdd3=rdd1.fold(1)((x:Int,y:Int)=>x+y)
  println(s"the fold rst:${rdd3}") //rst: 58

}


object saveOp extends RddOp{
  /**
    * saveAsTextFile用于将RDD以文本文件的格式存储到文件系统中。codec参数指定压缩的类名。
    * def saveAsTextFile(path: String): Unit
    * def saveAsTextFile(path: String, codec: Class[_ <: CompressionCodec]): Unit
    * saveAsSequenceFile用法和saveAsTextFile相同。
    */
  var rdd1 = sparkSession.sparkContext.makeRDD(Array(("A", "1"),("A", "2"), ("A", "3"), ("C", "1")), 2)
//  rdd1.saveAsTextFile("hdfs://cdh5/tmp/1234.com/") //保存到HDFS
  rdd1.saveAsTextFile("src\\main\\resources\\saveAsTextFile1.txt")  //保存到本地文件
  //指定压缩格式
//  rdd1.saveAsTextFile("src\\main\\resources\\saveAsTextFile.txt",com.hadoop.compression.lzo.LzopCodec])
  rdd1.saveAsObjectFile("src\\main\\resources\\saveAsObjectFile.txt")
}

