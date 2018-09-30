package com.liyz.scala.starter

class ByNameParamter {

}
/**
  *   parameter type: by-name and by-val
  *    A parameter of type A acts like a val (its body is evaluated once, when bound) and one of type => A acts like a def (its body is evaluated whenever it is used).
  *   by-value使用： arg:Int eg: add(x:Int)
  *   by-name使用：  arg: => Int eg: add(x: =>Int)
  *   区别：
  *   1. By-name parameters have the advantage that they are not evaluated if they aren’t used in the function body , type => A acts like a def (its body is evaluated whenever it is used).
  *   2. By-value parameters have the advantage that they are evaluated only once, by-value parameter of type A acts like a val
  *    by-name在被调用时进行初始化，可以理解为是def的另一种定义方式
  *    by-value在定义时进行初始化，且只加载一次，类似于val 定义的变量
  *
  *    参考： https://tpolecat.github.io/2014/06/26/call-by-name.html
  *
  *    by-name和lazy val区别
  *    by-name 类似于def，每次被调用时都会初始化新的对象
  *    lazy val 也是延迟加载，但是值只初始化一次
  */

object byNameOrValue extends App{
  // 用by-value定义参数
  def when1[A](test:Boolean,whenTrue:A,whenFalse:A):A={
    test match{
      case true=>whenTrue
      case false => whenFalse
    }
  }
  println(when1(1==1,"foo","bar")) // 仅仅输出foo

  when1(1==1,println("foo"),println("bar")) // foo和bar都会输出

  // 用by-name方式定义
  def when2[A](test: Boolean, whenTrue: => A, whenFalse: => A): A =
    test match {
      case true  => whenTrue
      case false => whenFalse
    }

  when2(1==1,println("foo"),println("bar")) // 仅仅输出bar

//  So is this laziness?
//  Not exactly. When we talk about laziness we mean that an expression is reduced by need, as is the case with lazy val in Scala. In case you’re not familiar, let’s generate a random number in three ways, first as a val:
// 定义时就已经对参数a进行初始化
//  scala> val a = { println("computing a"); util.Random.nextInt(10) }
//  computing a
//   a: Int = 5
//
//  scala> List(a, a, a, a, a)
//  res4: List[Int] = List(5, 5, 5, 5, 5)
//  So a was computed immediately, one time, as expected. What if it’s a def?
//
//  scala> def a = { println("computing a"); util.Random.nextInt(10) }
//  a: Int
//
//  scala> List(a, a, a, a, a)
//  computing a
//    computing a
//    computing a
//    computing a
//    computing a
//    res5: List[Int] = List(1, 6, 8, 4, 3)
//  Again, no surprises; a is computed each time it’s used. And what about lazy val?
//  定义时未对参数A进行初始化
//  scala> lazy val a = { println("computing a"); util.Random.nextInt(10) }
//  a: Int = <lazy>
//   使用时初始化，但只初始化一次
//    scala> List(a, a, a, a, a)
//    computing a
//    res6: List[Int] = List(4, 4, 4, 4, 4)
//    So the lazy val is computed once like any other val, but not until its value is needed.
//
//    So what happens with a by-name parameter? It might not be what you expect.
//
//    scala> def five[A](a: => A): List[A] = List(a, a, a, a, a)
//    five: [A](a: => A)List[A]
//
//    scala> five { println("computing a"); util.Random.nextInt(10) }
//    computing a
//    computing a
//    computing a
//    computing a
//    computing a
//    res7: List[Int] = List(1, 4, 6, 1, 5)
//    The by-name parameter behaves like the def above, not like lazy val. So although we used by-name parameters to implement lazy behavior for our when function above, this is dependent on only referencing each parameter once.
//
//    A by-name parameter acts like a def.
//
//    Note that we can get the lazy behavior we might have expected by combining a by-name parameter with a lazy val in the function body.
//
//    scala> def five[A](a: => A): List[A] = { lazy val b = a; List(b, b, b, b, b) }
//    five: [A](a: => A)List[A]
//
//    scala> five { println("computing a"); util.Random.nextInt(10) }
//    computing a
//    res8: List[Int] = List(9, 9, 9, 9, 9)


}