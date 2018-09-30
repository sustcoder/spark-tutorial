package com.liyz.scala.starter

class ApplyDemo {

}

/**
  *  Scala中的 apply 方法有着不同的含义, 对于函数来说该方法意味着调用function本身
  *
  *  Every function value is an instance of some class that extends one of several FunctionN traits in package scala,
  *  such as Function0 for functions with no parameters, Function1 for functions with one parameter, and so on.
  *  Each FunctionN trait has an apply method used to invoke the function.
  *
  *  在Scala语言中, 函数也是对象, 每一个对象都是scala.FunctionN(1-22)的实例, 其中N是函数参数的数量, 例如我们定义一个函数并复制给一个变量:
  *  scala> val f = (x: Int) => x + 1
  *  f: Int => Int = <function1>
  *  这里定义了一个接收一个整型变量作为参数的函数, 函数的功能是返回输入参数加1. 可以看到REPL返回参数的toString方法 即 <function0> .
  *  那么如果我们有一个指向函数对象的引用, 我们该如何调用这个函数呢? 答案是通过FunctionN的 apply 方法, 即 FunctionN.apply() ,
  *  因此调用函数对象的方法如下:
  *  scala> f.apply(3)
  *  res2: Int = 4
  *  但是如果每次调用方法对象都要通过FunctionN.apply(x, y...), 就会略显啰嗦, Scala提供一种模仿函数调用的格式来调用函数对象
  *  scala> f(3)
  *  res3: Int = 4
  */
object FuncionApply extends App{

  val f= (x:Int) => x+1
  println(f(1))
  println(f.apply(1))

  // 在Scala集合一文中提到过Iterator迭代器的几个子trait包括Seq, Set, Map都继承PartialFunction并实现了apply方法, 不同的是实现的方式不一样
  val list1=List.apply(1,2,3,4)
  val list2=List(1,2,3,4)
  println(list1==list2)

  // seq检索
  Seq(1,2,3).apply(1) // ouput 2

  // Set判断是否存在
  Set(1,2,3).apply(1) // output true

  // map根据键查找值
  Map("china"->"beijing","US"->"Washington").apply("US") // output Washington

}

/**
  * An extractor object is an object with an unapply method.
  * Whereas the apply method is like a constructor which takes arguments and creates an object,
  * the unapply takes an object and tries to give back the arguments. This is most often used in pattern matching and partial functions.
  *  提取器对象是具有unapply方法的对象。
  *  apply方法就像是一个构造器，接受参数并创建一个对象， 而unapply就像是接受一个对象并试图返回其参数。unpply通常用于模式匹配和部分功能。
  */
import scala.util.Random
object CustomerID{
  def apply(name:String) = s"$name--${Random.nextLong()}"
  def unapply(customerId:String):Option[String] ={
    val stringArray:Array[String] = customerId.split("--")
    if(stringArray.tail.nonEmpty) Some(stringArray.head) else None
  }
}

object CustomerIDTest extends  App{
  val customerID=CustomerID("Sukyoung")
  println("like a constructor which takes arguments and creates an object: "+customerID)
  val CustomerID(name)=customerID
  println("the unapply takes an object and tries to give back the arguments："+name)


  /**
    * 使用unpply的场景
    * The return type of an unapply should be chosen as follows:
    * If it is just a test, return a Boolean. For instance case even().
    * If it returns a single sub-value of type T, return an Option[T].
    * If you want to return several sub-values T1,...,Tn, group them in an optional tuple Option[(T1,...,Tn)].
    */
  customerID match{
    case CustomerID(name) => println(name)
    case _=> println("could not extract a CustomerID") // output: Sukyoung
  }

  "xxxxxxxxxx" match{
    case CustomerID(name) => println(name)
    case _=> println("could not extract a CustomerID") // output: could not extract a CustomerID
  }

}

