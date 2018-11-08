package com.liyz.scala.starter

import scala.collection.BitSet
import scala.collection.immutable.{ListSet, Map, Queue, SortedSet, TreeSet}

// https://docs.scala-lang.org/zh-cn/overviews/collections/seqs.html
class CollectionsDemo {

}
// http://allaboutscala.com/tutorials/chapter-7-beginner-tutorial-using-scala-mutable-collection/
object ListTest extends App{
  var list1:List[String] =List("plain","strawberry","chocolat")
  println(s"Elements of list1=$list1")// Elements of list1=List(plain, strawberry, chocolat)

  //  append elements at the end of immutable List using :+
  var list2:List[String]=list1 :+ "vanilla"
  println(s"Elements of list2=$list2") // Elements of list2=List(plain, strawberry, chocolat, vanilla)

  //  prepend elements at the front of immutable List using +:
  var list3="vainlla" +: list1
  println(s"Elements of list3=$list3")// Elements of list3=List(vainlla, plain, strawberry, chocolat)

  // add two immutable lists together using ::
  var list4= list1::list2
  // Elements of list4=List(List(plain, strawberry, chocolat), plain, strawberry, chocolat, vanilla)
  // note: Using :: returns a new List(List(...), elements from the second list)
  println(s"Elements of list4=$list4")
  //  list4(5) will be worang ,list4(0) will return List(plain, strawberry, chocolat)
  println(s"Elements of list4(0)=${list4(0)}")

  // add two immutable Lists together using :::
  var list5=list1:::list2
  println(s"Elements of list5=$list5")

  var list9=List(list1,list2)
  var list10=list9.flatten
  var list11=list9.flatMap(s=>s)
  println(s"Elements of list10=$list10")
  println(s"Elements of list11=$list11")

  // initialize an empty immutable List
  var list6=List.empty[String]
  println(s"Empty list = $list6")

  // Make a list element-by-element
  var list7="a"::"b"::"c"::List()
  println("list7: "+list7)
}
// ListSet: 会过滤掉重复元素的list
object ListSetTest extends  App{
  val listSet1=ListSet("a","b","c")
  println(listSet1)
  val listSet2=ListSet("a","b","c","c")
  println(listSet2)

  println(listSet2.contains("a"))

  // add elements of immutable ListSet using +
  val listSet3=listSet1+"d"
  println(listSet3)

  // add two ListSet together using ++
  val listSet4=listSet1++listSet3
  println(listSet4)
  println(s"merge tow set using union:${listSet1 union listSet2}")
  //  remove element from the ListSet using -
  val listSet5=listSet3-"d"
  println(listSet5)

  //  initialize an empty ListSet
  val listSet6=ListSet.empty
}

object MapTest1 extends App{
  var map1:Map[String,String]=Map("c"->"china","j"->"japan")
  var listMap5=Map(("c","china"),("j","japan"))
  println(map1) // Map(c -> china, j -> japan)
  var valueOfKeyC=map1("c")
  var map2=map1+("a"->"american")
  var map3=map2-("a")
  var map4=map1++map2

}


object QueueTest extends App{
  val queue1=Queue("a","b","c")
  println(queue1(0))
  // he Queue's elements are in First In First Out order (FIFO)
  val queue2=queue1:+"d"
  println(queue2)
  val queue3="d"+:queue1
  println(queue3)

  val queue4=queue1.enqueue("d")
  println(queue4)
  // note: Calling dequeue returns a Tuple2 with the last element that was inserted to the Queue and the remaining elements of the queue.
  val queue5=queue4.dequeue
  println(queue5) // (a,Queue(b, c, d))
  // access each individual element of tuple user
  println(queue5._1) // a

}

// set
object SetTest extends App{
  val set1=Set("a","b","c")
  val set2=Set("a","b")
  println(set1 & set2)  // Set(a, b)
  println(set1 &~ set2) // Set(c)
  println(set2("c")) // false
}

// SortedSet   SortedSet is a trait. Its concrete class implementation is a TreeSet
// soredSet会默认进行排序
object SortedSetTest extends App{
  val sortedSet1=SortedSet("2","1","3")
  println(sortedSet1) // TreeSet(1, 2, 3)

  val treeSet1=TreeSet("2","1","3")
  println(treeSet1) // TreeSet(1, 2, 3)

  object CustomOrderOfSortedSet extends Ordering[Int]{
    def compare(element1:Int,element2:Int)= {if (element1>element2)  -1 else 1}
  }
  val sortedSet2=SortedSet(2,1,3)(CustomOrderOfSortedSet)
  println(sortedSet2) // TreeSet(3, 2, 1)

  val sortedSet3=SortedSet(2,1,3)
  println(sortedSet3) // TreeSet(1, 2, 3)

  val bitSet1=BitSet(2,3,1)
  println(bitSet1)
  val bitSet2=bitSet1+4-1
  println(bitSet2)

  val treeSet4=TreeSet(2,3,1)
  //  println(treeSet1-2) // 会报错

}

//  a mutable data structure of fixed length. It also allows you to access and modify elements at specific index
object ArrayTest extends App{
  val array1=Array("1","2","3")
  val index0=array1(0)
  val array2:Array[String]=new Array(3) // note 需要制定array参数类型
  array2(0) = "11"
  array2(1) = "22"
  array2(2) = "33"
  println(array2.mkString("-")) // 11-22-33
  println(array2.mkString("the number array element is ","、",",not have 44"))
  val array3=Array(3) // note: different with  new Array(3)

  var array4=Array.ofDim[String](2,2)
  array4(0)(0)="00"
  array4(0)(1)="01"
  array4(1)(0)="10"
  array4(1)(1)="11"
  println(array4.toList) // List([Ljava.lang.String;@21bcffb5, [Ljava.lang.String;@380fb434)
  println(array4.deep.toList) // List(Array(00, 01), Array(10, 11))

  var array5:Array[Array[String]]=Array.ofDim[String](4,4)
  println(array5.deep.toList) // List(Array(null, null, null, null), Array(null, null, null, null), Array(null, null, null, null), Array(null, null, null, null))

  var array6=Array.tabulate(5)(_+3)
  println(array6.toList)

  var array7=Array.tabulate(3,3)((r,c)=> r+c)
  println(array7.deep.toList)

  var array8=(1 to 10).toArray
  println(array8.toList)

  val array9:Array[String]=new Array(5)
  Array.copy(array1,1,array9,1,2) //  Array.copy(r,rIndex,d,dIndex,rLen)
  println(array9.toList) // List(null, 2, 3, null, null)

  for(a <- array9) println(s" a=$a")

  val array10=Array("1","2","3")
  val array11=Array("1","2","3")
  val array12=array10.clone()
  println(array10==array10)
  println(array10==array11)
  println(array10==array12)
  println(array10 sameElements array12)
  println(array10.sameElements(array11))
  println(array10.deep==array11.deep)
}

object MutableMapTest extends App{
  var map1: Map[String, String] = Map("VD"-> "Vanilla Donut", "GD" -> "Glazed Donut")
  println(s"Elements of map2 = $map1")
  var map2= map1 += ("KD" -> "Krispy Kreme Donut")
   map1++=map1
   println(s"Elements of map2 = $map1")

  map1 -= "VD"
  println(s"Map without the key VD and its value = $map1")
}