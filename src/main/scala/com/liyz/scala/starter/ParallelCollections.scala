package com.liyz.scala.starter

class ParallelCollections {
}
object parTest extends App{
  // 为了并行的执行同样的操作，我们只需要简单的调用序列容器（列表）的par方法。这样，我们就可以像通常使用序列容器的方式那样来使用并行容器。
  val list = (1 to 10).toList
  list.map(_ + 42)
  list.par.map(_ + 42)

  val lastName=List("Smith","Jones").par
  lastName.map(_.toUpperCase)

  val parArray=(1 to 10).toArray.par
  parArray.fold(0)(_+_)

  //  通常，我们有两种方法来创建一个并行容器:
  //  第一种，通过使用new关键字和一个适当的import语句:
  import scala.collection.parallel.immutable.ParVector
  val pv1 = new ParVector[Int]
  // 第二种，通过从一个顺序容器转换得来：
  val pv2 = Vector(1,2,3,4,5,6,7,8,9).par

  // 这里需要着重强调的是这些转换方法：通过调用顺序容器(sequential collections)的par方法，顺序容器(sequential collections)可以被转换为并行容器；通过调用并行容器的seq方法，并行容器可以被转换为顺序容器。
  val pv3=pv2.seq

  // 副作用操作: 为了保持确定性，考虑到并行容器框架的并发执行的语义，一般应该避免执行那些在容器上引起副作用的操作。
  var sum=0
  val list3=(1 to 10).toList.par
  //  三次计算结果不一致
  list3.foreach(sum+=_)
  println(s"the first sum:$sum")
  list3.foreach(sum+=_)
  println(s"the second sum:$sum")
  list3.foreach(sum+=_)
  println(s"the third sum:$sum")

  // 非关联(non-associative)操作
  // 对于”乱序“语义，为了避免不确定性，也必须注意只执行相关的操作。这就是说，给定一个并行容器：pcoll，
  // 我们应该确保什么时候调用一个pcoll的高阶函数，例如：pcoll.reduce(func)，被应用到pcoll元素的函数顺序是任意的。一个简单但明显不可结合(non-associative)例子是减法运算：
  println(s"non associative: ${list3.reduce(_-_)}")
  println(s"non associative: ${list3.reduce(_-_)}")
  println(s"non associative: ${list3.reduce(_-_)}")

  // 注意：通常人们认为，像不可结合(non-associative)作，不可交换(non-commutative)操作传递给并行容器的高阶函数同样导致非确定的行为。
  // 但和不可结合是不一样的，一个简单的例子是字符串联合(concatenation)，就是一个可结合但不可交换的操作：
  val strings = List("abc ","def ","ghi ","jk ","lmnop ","qrs ","tuv ","wx ","yz ").par
  println(strings.reduce(_++_))
  println(strings.reduce(_++_))
  println(strings.reduce(_++_))

//  并行容器的“乱序”语义仅仅意味着操作被执行是没有顺序的（从时间意义上说，就是非顺序的），并不意味着结果的重“组合”也是乱序的（从空间意义上）。
  // 恰恰相反，结果一般总是按序组合的 – 一个并行容器被分成A，B，C三部分，按照这个顺序，将重新再次按照A，B，C的顺序组合。而不是某种其他随意的顺序如B，C，A。

  println(s"parallel range: ${1 to 6 par}")
  println(s"parallel range ${1 to 6 by 2 par}")
}

