package com.liyz.scala.starter

/**
  * implicit关键字：
  * Scala 2.10 introduced a new feature called implicit classes. An implicit class is a class marked with the implicit keyword.
  * This keyword makes the class’s primary constructor available for implicit conversions when the class is in scope
  *
  * Scala 2.10 引入了一个名为隐式类的新功能。隐式类是使用implicit关键字标记的类。当类在范围内时，此关键字使类的主构造函数可用于隐式转换
  */
class ImplicitDemo {

}

class A{}

// 隐式类用例：
// 1.1 在自定义类RichA中，将类A以参数形式传入
class RichA(a:A){
  def rich(): Unit ={
    println("隐式转换")
  }
}

object ImplicitDemo extends App {
  // 1.2 用implicit关键词初始化RichA——implicit关键字将类A隐式转换为类RichA
  implicit def a2RichA(a: A) = new RichA(a)

  val a = new A
  // 1.3 在类A中可以直接调用RichA中定义的方法
  a.rich()

  // 2.1 在定义类的时候直接使用implicit关键字
  implicit class Calculator(x: Int) {
    def add(a: Int): Int = a + 1
  }

  // 2.2 类Calculator类实现了add方法，int类型的变量可直接使用
  println(1.add(1))



  // -----------------------* 限制Restrictions start *------------------------
  // 3.1 implicit 类必须定义在 trait/class/object的里面
  //  object Helpers {
  //    implicit class RichInt(x: Int) // OK!
  //  }
  //implicit class RichDouble(x: Double) // BAD!

  // 3.2 implicit类只允许有一个非隐式参数
  //implicit class RichDate(date: java.util.Date) // OK!
  //implicit class Indexer[T](collecton: Seq[T], index: Int) // BAD!
  //implicit class Indexer[T](collecton: Seq[T])(implicit index: Index) // OK!

  // -----------------------* 限制Restrictions end *--------------------------

  // --------------------* 隐式参数 start *--------------------
  def testParam(implicit name: String): Unit = {
    println(name)
  }

  implicit val name = "隐式参数"
  testParam("显式参数")
  testParam
  // ---------------------* 隐式参数 end *---------------------

}

/**
  * Implicit conversions are applied in two situations:
  * If an expression e is of type S, and S does not conform to the expression’s expected type T.
  * In a selection e.m with e of type S, if the selector m does not denote a member of S.
  * 隐式转换的时机：
  * 1.当方法中的参数的类型与目标类型不一致时
  * 2.当对象调用类中不存在的方法或成员时，编译器会自动将对象进行隐式转换
  *
  * The places Scala will look for these parameters fall into two categories:
  * Scala will first look for implicit definitions and implicit parameters that can be accessed directly (without a prefix) at the point the method with the implicit parameter block is called.
  * Then it looks for members marked implicit in all the companion objects associated with the implicit candidate type.
  * 隐式解析机制：
  * 1.首先会在当前代码作用域下查找隐式实体（隐式方法  隐式类 隐式对象）
  * 2.如果第一条规则查找隐式实体失败，会继续在隐式参数的类型的作用域里查找
  *  - （1）如果T被定义为T with A with B with C,那么A,B,C都是T的部分，在T的隐式解析过程中，它们的伴生对象都会被搜索
  *  - （2）如果T是参数化类型，那么类型参数和与类型参数相关联的部分都算作T的部分，比如List[String]的隐式搜索会搜索List的伴生对象和String的伴生对象
  *  - （3） 如果T是一个单例类型p.T，即T是属于某个p对象内，那么这个p对象也会被搜索
  *  - （4） 如果T是个类型注入S#T，那么S和T都会被搜索
  */
