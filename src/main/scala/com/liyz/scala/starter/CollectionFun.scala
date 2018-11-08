package com.liyz.scala.starter

class CollectionFun {

}

/**
  *   合计，求和 def aggregate[U: ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U): U
  * 　aggregate函数将每个分区里面的元素进行聚合，然后用combine函数将每个分区的结果和初始值(zeroValue)进行combine操作。这个函数最终返回的类型不需要和RDD中元素类型一致。
  *   注意：
  *     1、reduce函数和combine函数必须满足交换律(commutative)和结合律(associative)
　*     2、从aggregate 函数的定义可知，combine函数的输出类型必须和输入的类型一致
  */
object AggregateTest extends App{

  val set0=Set(2,3,4)
  def sumF0(sum:Int,element:Int):Int= sum+element
  def sumComb0(sum1:Int,sum2:Int):Int=sum1+sum2
  var allSum0=set0.aggregate(0)(sumF0,sumComb0)
  println("set0 all chars len: "+allSum0)

  val set1=Set(2,3,4)
  def sumF1(sum:Int,element:Int):Int= sum+element
  def sumComb1(sum1:Int,sum2:Int):Int=sum1-sum2

  // TODO: comOp是干什么的？？？？？？？？？？？？？？
  var allSum1=set0.aggregate(0)(sumF1,sumComb1)
  println("set1 all chars len: "+allSum1)

  val set2=Set("a","bb","cccc")
  def charLenF(len:Int,char:String):Int= len+char.length
  def charCombF(len1:Int,len2:Int):Int= len1+len2
  var totalLen=set2.aggregate(0)(charLenF,charCombF)
  println("set2 all chars len: "+totalLen)

  val set3=Set("a","bb","cccc")
  val charLen:(Int,String) => Int = (len,char)=> len+char.length
  var totalLenth=set3.aggregate(0)(charLen,_+_)
  println("set3 all chars len: "+totalLenth)

  val set4=Set("a","bb","cccc")
  val charLen4:(Int,String) => Int = (len,char)=> len+char.length
  var totalLenth4=set3.aggregate(0)((len:Int,char:String)=>charLen4(len,char),_+_)
  println("set4 all chars len: "+totalLenth4)


  val donutBasket2: Set[(String, Double, Int)] = Set(("Plain Donut", 1.50, 10), ("Strawberry Donut", 2.0, 10))
  val totalCostAccumulator:(Double,Double,Int) => Double= (accumulator,price,quantity)=>accumulator+(price*quantity)
  val totalCost = donutBasket2.aggregate(0.0)((accumulator: Double, tuple: (String, Double, Int)) => totalCostAccumulator(accumulator, tuple._2, tuple._3), _ + _)
  println("totalCost  : "+totalCost)
}

/**
  *  The collect method takes a Partial Function as its parameter and applies it to all the elements in the collection to create a new collection which satisfies the Partial Function
  *  def collect[B](pf: PartialFunction[A, B]): Traversable[B]
  *
  *  collect方法将Partial Function作为参数，并将其应用于集合中的所有元素，以创建满足Partial Function的新集合。
  *
  */
object CollectionTest extends App{
  val donutNamesandPrices: Seq[Any] = Seq("Plain Donut", 1.5, "Strawberry Donut", 2.0, "Glazed Donut", 2.5)
  val donutNames: Seq[String]= donutNamesandPrices.collect{case name:String => name}
  val dountPrices: Seq[Double]=donutNamesandPrices.collect{case price:Double => price }
  val filterPrices: Seq[Double]=donutNamesandPrices.collect{case price:Double if(price>1.8)=> price}
  println(s"donut names: $donutNames")
  println(s"donut prices: $dountPrices")
  println(s"donut filterPrices: $filterPrices")
}
object diffTest extends App{

  val set1=Set("a","b","c")
  val set2=Set("c","d","e")
  val set3=set1.diff(set2)
  println("set1 diff set2 : "+set3)

  val set4=set1 diff set2
  println("set1 diff set2 : "+set4)

  val set5=set1 drop 2
  println(s"set1 drop first tow element : $set5")

  val set6=set1 dropRight  2
  println(s"set1 drop first tow element : $set6")

  //  dropWhile 从第一个元素开始匹配，匹配成功后继续向后匹配，匹配失败直接返回，即：他会过滤掉连续匹配的结果且是从第一个元素开始
  var seq1=Seq("1","22","333")
  val set7=seq1.dropWhile(_.charAt(0)=='1')
  println(s"set7 dropWhile element : $set7" )// print: List(22, 333)

  // 第一个匹配为false后，将不会继续向后匹配
  val set8=seq1.dropWhile(_.charAt(0)=='2')
  println(s"set8 dropWhile element : $set8" )// List(1, 22, 333) "22"不会被删除

  // 第一个匹配为true后，会继续向后匹配
  var seq2=Seq("1","11","22","333")
  val set9=seq2.dropWhile(_.charAt(0)=='1')
  println(s"set9 dropWhile element : $set9" )// List( 22, 333) "1"和"11"都会被删除

  println(s"set2 take 2:${seq2.take(2)}")
  println(s"set2 tail:${seq2.tail}")
  println(s"set2 head:${seq2.head}")
  println(s"set2 tails:${seq2.tails}")


}

object dropTest extends App{

  val set1=Set("a","b","c")
  val set2=Set("c","d","e")

  var set3=set1.drop(2)
  println(s"set3 drop first tow element : $set3")

  val set5=set1 drop 2
  println(s"set1 drop first tow element : $set5")

  val set6=set1 dropRight  2
  println(s"set1 dropRight last tow element : $set6")

  //  dropWhile 从第一个元素开始匹配，匹配成功后继续向后匹配，匹配失败直接返回，即：他会过滤掉连续匹配的结果且是从第一个元素开始
  var seq1=Seq("1","22","333")
  val set7=seq1.dropWhile(_.charAt(0)=='1')
  println(s"set7 dropWhile element : $set7" )// print: List(22, 333)

  // 第一个匹配为false后，将不会继续向后匹配
  val set8=seq1.dropWhile(_.charAt(0)=='2')
  println(s"set8 dropWhile element : $set8" )// List(1, 22, 333) "22"不会被删除

  // 第一个匹配为true后，会继续向后匹配
  var seq2=Seq("1","11","22","333")
  val set9=seq2.dropWhile(_.charAt(0)=='1')
  println(s"set9 dropWhile element : $set9" )// List( 22, 333) "1"和"11"都会被删除

  val set10=seq2.filter(_.contains("1")) // 保留返回true的对象 List(1, 11)
  val set11=seq2.filterNot(_.contains("1")) // 不保留返回true的对象 List(22, 333)
  println(set10)
  println(set11)
}
object existsTest extends App {
  val seq1: Seq[String] = Seq("a", "b", "c")
  val doesAExists=seq1.exists(e=>e=="a")
  val doesAExists1=seq1.exists(_=="a")
  val existsInSeq1F:(String)=>Boolean = (e) => e.equals("a")
  val doesExists2=seq1.exists(existsInSeq1F)
  println(s"does a exists in seq1: $doesExists2")
}
// find the first element in the collection which matches the predicate
object findTest extends App{
  val set1=Set(1,2,3,4)
  val e:Option[Int]=set1.find( _>2 )
  println(e.get) // 3
  val e1:Option[Int]=set1.find( _==5 )
  println(e1.getOrElse("not found")) // nor found
  println(set1.head) // 1
  println(set1.last) // 4
  println(set1.max) // 4
  println(e1.headOption.getOrElse("not found")) // nor found
}
object flatMapTet extends App{
  val list1=List("1","2","3")
  val list2=List("3","4","5")
  val list3=List(list1,list2)
  println( list3)
  println( list3.flatten)
  println( list3.flatMap( e=> e ))

  println(list1.intersect(list2)) // 交集 List(3)
  println(list1 intersect list2) // 交集 List(3)
  println(Set("1","2") & Set("2","3")) // 用&求set的交集： Set(2)

  val donuts1: Seq[String] = Seq("Plain", "Strawberry", "Glazed")
  val donuts2: Seq[String] = donuts1.map(_ + " Donut")
  println(s"Elements of donuts2 = $donuts2")

  // TODO: list char
  println( list1.flatMap( _+"x" )) // List(1, x, 2, x, 3, x)  // list char???
  println( list1.map( _+"x" )) // List(1x, 2x, 3x)
}
object groupByTest extends App{
  val seq1=Seq("scala","java","spring")
  val grouped1:Map[Char,Seq[String]]=seq1.groupBy(_.charAt(0))
  println(grouped1) // Map(j -> List(java), s -> List(scala, spring))

  case class Person(name:String,dept:String)
  val person1=Seq(Person("张三","技术部b"),Person("李四","技术部a"),Person("王麻子","薪酬部"))
  val grouped2=person1.groupBy(_.dept)
  println(grouped2) //  Map(薪酬部 -> List(Person(王麻子,薪酬部)), 技术部 -> List(Person(张三,技术部), Person(李四,技术部)))
  val grouped3=person1.maxBy(_.name) // String类型按照字典序排序
  println(grouped3)
  println(s"person1 sortBy =${person1.sortBy(_.dept)}")
  println(s"person1 sortWith =${person1.sortWith(_.dept>_.dept)}") // 用sortWith实现降序排列
}

object MapTest extends App{
  val seq1=Set(1,2,3,4)
  val seq2=seq1.map(_+2)
  println(s"seq1=$seq2")
  var seq3=Seq(1,2,3,None)
//  val seq4=seq3.map{case s:Int=> s} // 未考虑None的情况，会抛异常
//  println(s"seq4.map=$seq4")
  val seq5=seq3.map{case s:Int=>1 case None=> 0}
  println(s"seq5=$seq5") //  seq5=List(1, 1, 1, 0)
  def notNoneSeq=Some(1,2)
  val seq6=notNoneSeq.map(seq5=>println(s"seq6=$seq5"))
 }

// def partition(p: (A) ⇒ Boolean): (Repr, Repr)  返回值必须是一个true 一个false
object PartitionTest extends  App{
  val seq1=Seq( "scala",1,"spark",2,"flink",3 )
  val seq2:(Seq[Any],Seq[Any])=seq1.partition{case name:String => true case version :Int=>true case _=>false}
  println(s"seq2.names=${seq2._1}")
  println(s"seq2.version=${seq2._2}")
  val (names,versions)=seq1.partition{case name:String=>true case _ =>false}
  println(s"seq2.names=$names")
  println(s"seq2.version=$versions")
  println(s"names and verions transpose:${Seq(names,versions).transpose}")
  println(s"names and verions zip:${ names zip versions }")
  println(s"names and verions unzip:${ (names zip versions) unzip }")
  println(s"names and verions unzip:${ ((names zip versions) unzip)_1 }")
}

// def reduce[A1 >: A](op: (A1, A1) ⇒ A1): A1
object ReduceTest extends App{
  val seq1=Seq(1,2,3,4)
  println(s"sum of seq1: ${seq1.reduce(_+_)}")
  println(s"max of seq1: ${seq1.reduce(_ max _)}")
  println(s"min of seq1: ${seq1.reduce(_.min(_))}")
//  println(s"numbers of seq1: "+ seq1.reduce( (left, right) => {  left + ", " + right})) // 会报错：类型不匹配，原因： reduce的入参和出参默认是一种类型
  val seq2=Seq("1","2","3","4")
  println(s"numbers of seq1: "+ seq2.reduce( (left, right) => {  left + "-" + right}))
  println(s"numbers of seq1 scaned +: ${seq1.scan(0)(_ + _)}") // scan: 累计求和，并将每一步的计算结果记录在数组中返回
  println(s"numbers of seq1 scaned -: ${seq1.scan(0)(_ - _)}") // List(0, -1, -3, -6, -10)
  println(s"numbers of seq1 scaned *: ${seq1.scan(2)(_ * _)}") // List(2, 2, 4, 12, 48)
  println(s"numbers of seq1 scaned min: ${seq1.scan(0)(_ min _)}") // List(0, 0, 0, 0, 0)
  println(s"numbers of seq1 scaned max: ${seq1.scan(0)(_ max _)}") // List(0, 1, 2, 3, 4)
  println(s"numbers of seq1 slice : ${seq1.slice(2,8)}") // List( 3, 4)
  println(s"numbers of seq2 slice : ${seq2.slice(2,3)}") // List( 3,) //  [startIndex,endIndex) 左闭又开
}

/**
  * The view method will create a non-strict version of the collection which means that the elements of the collection will only be made available at access time.
  */
object viewTest extends App{
  val largeOddNumberList:List[Int] = (1 to 1000000).filter(_%2!=0).take(10).toList
  val layLargeOddNumberList:List[Int] = (1 to 1000000).view.filter(_%2!=0).take(10).toList
  println(layLargeOddNumberList)
  println(largeOddNumberList==layLargeOddNumberList)
}




