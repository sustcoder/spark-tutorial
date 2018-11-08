package com.liyz.spark.starter

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{StructField, StructType, _}

class UDAFGetMode extends UserDefinedAggregateFunction{
  override def inputSchema: StructType = {
    StructType(StructField("inputStr",StringType,true):: Nil)
  }

  override def bufferSchema: StructType = {
    StructType(StructField("bufferMap",MapType(keyType = StringType,valueType = IntegerType),true):: Nil)
  }

  override def dataType: DataType = StringType

  override def deterministic: Boolean = false

  //初始化map
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = scala.collection.immutable.Map[String,Int]()
  }

  //如果包含这个key则value+1，否则 写入key，value=1
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    val key = input.getAs[String](0)
    val immap = buffer.getAs[scala.collection.immutable.Map[String,Int]](0)
    val bufferMap = scala.collection.mutable.Map[String,Int](immap.toSeq: _*)
    val ret = if (bufferMap.contains(key)){
      val new_value = bufferMap.get(key).get + 1
      bufferMap.put(key,new_value)
      bufferMap
    }else{
      bufferMap.put(key,1)
      bufferMap
    }
    buffer.update(0,ret)
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    //合并两个map 相同的key的value累加
    //http://www.cnblogs.com/tugeler/p/5134862.html
    buffer1.update(0,( buffer1.getAs[scala.collection.immutable.Map[String,Int]](0) /: buffer2.getAs[scala.collection.immutable.Map[String,Int]](0) ) { case (map, (k,v)) => map + ( k -> (v + map.getOrElse(k, 0)) ) })
  }

  override def evaluate(buffer: Row): Any = {
    //返回值最大的key
    var max_vale = 0
    var max_key = ""
    buffer.getAs[scala.collection.immutable.Map[String,Int]](0).foreach{
      x=>
        val key = x._1
        val value = x._2
        if(value>max_vale) {
          max_vale=value
          max_key=key
        }
    }
    max_key
  }
}

