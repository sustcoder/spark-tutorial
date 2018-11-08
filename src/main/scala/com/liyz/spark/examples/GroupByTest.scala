package com.liyz.spark.examples

import java.util.Random

import com.liyz.spark.starter.Context

/**
 * Usage: GroupByTest [numMappers] [numKVPairs] [KeySize] [numReducers]
  * 例子代码分析：https://www.jianshu.com/p/1ce1c4c7126a
  * 例子代码分析： https://spark-internals.books.yourtion.com/markdown/1-Overview.html
  * 大数据实战项目《大型电商日志分析》的知识点 https://www.jianshu.com/nb/11681227
 */
object GroupByTest extends Context {
  def main(args: Array[String]) {
    val numMappers = if (args.length > 0) args(0).toInt else 10
    val numKVPairs = if (args.length > 1) args(1).toInt else 1000
    val valSize = if (args.length > 2) args(2).toInt else 100
    val numReducers = if (args.length > 3) args(3).toInt else 4

    val pairs1 = sparkSession.sparkContext.parallelize(0 until numMappers, numMappers).flatMap { p =>
      val ranGen = new Random
      val arr1 = new Array[(Int, Array[Byte])](numKVPairs)
      for (i <- 0 until numKVPairs) {
        val byteArr = new Array[Byte](valSize)
        ranGen.nextBytes(byteArr) // 随机生成一个大型为valSize的byte对象
        arr1(i) = (ranGen.nextInt(Int.MaxValue), byteArr)
      }
      arr1
    }.cache() // 每个 mapper 将产生的 arr1 数组 cache 到内存(parallelize函数第二个参数指定将collection分为几个partitions（分几个task执行）)
    println(pairs1.toDebugString)
    // 来统计所有 mapper 中 arr1 中的元素个数，执行结果是 numMappers * numKVPairs = 3*1000。这一步主要是为了将每个 mapper 产生的 arr1 数组 cache 到内存。
    // Enforce that everything has been calculated and in cache
    pairs1.count()
    println(s"pairs1.count()=${pairs1.count()}")
    println(pairs1.toDebugString)
    // 在已经被 cache 的 paris1 上执行 groupByKey 操作，groupByKey 产生的 reducer （也就是 partition） 个数为 numReducers。
    // 理论上，如果 hash(Key) 比较平均的话，每个 reducer 收到的 <Int, Array[Byte]> record 个数为
    // numMappers * numKVPairs / numReducer
    // reducer 将收到的 <Int, Byte[]> records 中拥有相同 Int 的 records 聚在一起，得到 <Int, list(Byte[], Byte[], ..., Byte[])>
    val reducedPairs=pairs1.groupByKey(numReducers)
    // 最后 count 将所有 reducer 中 records 个数进行加和，最后结果实际就是 pairs1 中不同的 Int 总个数。
    println(reducedPairs.count())
    println(reducedPairs.toDebugString)
    sparkSession.stop()
  }
}


trait GroupByParams extends App with Context {
  val numMappers = 10
  val numKVPairs = 100
  val valSize = 100
  val numReducers = 3
}

/**
  * 创建并行集合的一个重要参数，是slices的数目（例子中是numMappers），它指定了将数据集切分为几份。
  * 在集群模式中，Spark将会在一份slice上起一个Task。典型的，你可以在集群中的每个cpu上，起2-4个Slice （也就是每个cpu分配2-4个Task）。
  * 一般来说，Spark会尝试根据集群的状况，来自动设定slices的数目。当让，也可以手动的设置它，通过parallelize方法的第二个参数。
  *
  *  slice~=partitions
  *  partitions ~= block ~= split ~
  *  RDD本质之一：a list of partition  由多个机器里面的partition组成的
  *
  *  partitions: 数据的逻辑分区，默认是执行此程序的CPU核数
  */
object GroupByStep extends GroupByParams {
  val pairs1 = sparkSession.sparkContext.parallelize(0 until numMappers,numMappers)
  pairs1.foreach(p=> println(p))
  println(pairs1.getNumPartitions)
}

///**
//  * Generates random bytes and places them into a user-supplied
//  * byte array.  The number of random bytes produced is equal to
//  * the length of the byte array.
//  *
//  *  /

//public void nextBytes(byte[] bytes) {
//  for (int i = 0, len = bytes.length; i < len; )
//  for (int rnd = nextInt(),
//  n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
//  n-- > 0; rnd >>= Byte.SIZE)
//  bytes[i++] = (byte)rnd;
//}
