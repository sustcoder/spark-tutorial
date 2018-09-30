package com.liyz.scala.starter

class HigherOrderFunctions {

}

/**
  * Higher order functions : 高阶函数
  * Higher order functions take other functions as parameters or return a function as a result.
  * This is possible because functions are first-class values in Scala.
  * The terminology can get a bit confusing at this point, and we use the phrase “higher order function”
  * for both methods and functions that take functions as parameters or that return a function.
  * 高阶函数将其他函数作为参数或作为结果返回函数。
  * 我们对于将函数作为参数或返回函数的方法和函数统称为“高阶函数”
  */

object HigherOrderFunctions extends App{

  val salaries=Seq(7000,8000,9000)

  // 基本实现方式：
  val doubleSalary=(x:Int) => x*2
  val newSalary1=salaries.map(doubleSalary)


  //  将函数doubleSalary的方法体直接作为map函数的入参
  val newSalary2=salaries.map((x:Int)=>x*2)
  /**
    *  取消对参数类型的定义
    *  Notice how x is not declared as an Int in the above example.
    *  That’s because the compiler can infer the type based on the type of function map expects
    *  编译器会根据map函数所期望的类型去定义参数x的类型
    */
  val newSalary3=salaries.map(x=>x*2)

  /**
    * 用 “_”代替参数
    * Since the Scala compiler already knows the type of the parameters (a single Int),
    * you just need to provide the right side of the function.
    * The only caveat is that you need to use _ in place of a parameter name (it was x in the previous example)
    * 因为scala编译器可以推断出参数类型，所以我们只需要写函数主体即可，不需要写函数的入参
    * 但是需要注意的是需要用“_”去做参数的占位符
    */
  val newSalary4=salaries.map(_*2)

  newSalary4.foreach(print)  // output: 14000 16000 18000
  println()

  /**
    *  不能省略类型的场景：
    *  For recursive methods, the compiler is not able to infer a result type.
    *  有迭代时不能够自动推断类型
    */
  //  Here is a program which will fail the compiler for this reason:
  //  def fac(n: Int) = if (n == 0) 1 else n * fac(n - 1)


  // --------------------------------* Functions that return functions *---------------------------------------
  /**
    * Notice the return type of urlBuilder (String, String) => String.
    * This means that the returned anonymous function takes two Strings and returns a String. In this case,
    * the returned anonymous function is (endpoint: String, page: String) => s"$schema$domainName/$endpoint/$page".
    *
    *  函数urlBuilder的返回结果是类型是一个输入(String,String)输出（String）类型的函数
    *  用def getUrl接收返回的函数，可以在下文中直接使用这个函数getUrl(String,String)
    */
  def urlBuilder(ssl:Boolean,domainName:String):(String,String)=>(String) = {
    val schema= if(ssl) "https://" else "http://"
    (endpoint:String,page:String) => s"$schema$domainName/$endpoint/$page"
  }
  val domainName="www.scala-lang.org"
  def getUrl=urlBuilder(ssl=true,domainName) // 返回为一个函数
  val endpoint="tour"
  val page="higher-order-functions.html"
  val url=getUrl(endpoint,page)
  println(url)

}

/**
  * It won’t compile, because the type inferred for obj was Null.
  * Since the only value of that type is null, it is impossible to assign a different value.
  * 注意：和java不同 如果变量赋值为Null,则不能给此变量赋值其他引用
  */
class AnyRef{}

object AnyRef extends App{

  var obj=null
//  obj=new AnyRef

  var obj1=new AnyRef
  obj1=null

  obj1=new AnyRef
}
