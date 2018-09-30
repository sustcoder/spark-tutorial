package com.liyz.scala.starter

class SymbolDemo {

}
object SymbolDemo extends App{
//  var list1=List("A","B")
//  var list2="A"::"B"

  println(10.+(1))
}

/**
  *  operators:
  *  In Scala, operators are methods. Any method with a single parameter can be used as an infix operator.
  *  在scala中，算子都属于方法，任何只有一个参数的方法都可以作为一个中缀表达式
  *
  *  中缀与前缀表达式：首先他们都是表达式的一种记忆方法，区别在于运算符与操作数的相对位置的不同，前缀表达式的运算符位于与其相关的操作数之前，中缀在操作数之间，后缀就是位于操作数后面的意思。
  *  比如：
  *     2 + 3  *5 - 4      是中缀表达式
  *     +   -  * 3 5 4  2  是前缀表达式
  *     3 5 * 2 + 4 -      是后缀表达式
  *
  */
object OperatorsTest extends App{
  val s1=10+1
  val s2=10.+(2)
  println(s1==s2)
}

// 自定义operators
case class Vec(val x:Double,val y:Double){
  def +(that :Vec)=new Vec(this.x+that.x,this.y+that.y)
}
object Vec extends App{
  val vector1=Vec(1.0,2.0)
  val vector2=Vec(2.0,1.0)
  val vector3=vector1.+(vector2)
  val vector4=vector1+vector2
  println("vector3.x: "+vector3.x+",y: "+vector3.y) // vector3.x: 3.0,y: 3.0
  println("vector4.x: "+vector4.x+",y: "+vector4.y)// vector4.x: 3.0,y: 3.0
}



