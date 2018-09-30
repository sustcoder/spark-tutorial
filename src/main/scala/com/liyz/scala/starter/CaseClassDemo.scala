package com.liyz.scala.starter

class CaseClassDemo {

}

/**
  *
  *  A minimal case class requires the keywords case class, an identifier, and a parameter list (which may be empty):
  *
  *  case类最少包括： 关键词(case class)、类名、一组属性（可以为空）
  */

object  CaseClassDemo extends App{
  /**
    * Notice how the keyword new was not used to instantiate the Book case class.
    * This is because case classes have an apply method by default which takes care of object construction.
    *
    * case class 不需要使用new进行初始化，因为case类有apply方法默认进行初始化
    */
  case class Book(isbn:String)
  val frankenstein=Book("978-0486282114")

  /**
    * When you create a case class with parameters, the parameters are public vals
    *
    * case类的属性是 公有的，静态的，如果执行message.sender="flink@qq.com" 去改变值时编译报错
    */
  case class Message(sender:String,recipient:String,body:String)
  val message1=Message("scala@qq.com","spark@qq.com","you are good")
  println(message1.sender)
  //  message1.sender = "travis@washington.us"  // this line does not compile

  /**
    *  Case classes are compared by structure and not by reference:
    *
    *  case类是值比较（构成），不是引用
    *
    *  Even though message1 and message2 refer to different objects, the value of each object is equal.
    *
    *  对象message1和message2指向了不同对象的引用，但是他们是相等的
    */
  val message2=Message("scala@qq.com","spark@qq.com","you are good")
  println(message1) // Message(scala@qq.com,spark@qq.com,you are good)
  println(message1==message2) // true

  /** copy函数：
    * You can create a (shallow) copy of an instance of a case class simply by using the copy method. You can optionally change the constructor arguments.
    *
    * 可对case class 进行自定义copy
    * 1. 默认拷贝对应属性到新对象
    * 2. 修改其中某个属性
    * 3. copy参数可为被拷贝对象属性
    */
  val message3=message2.copy(sender = message2.recipient,"otherRecipient@qq.com")
  println(message3) // Message(spark@qq.com,otherRecipient@qq.com,you are good)

}

//  ------------------------* pattern matching  Syntax *------------------------------------------


//  case语法举例
object caseSyntaxExample extends App{

  // A match expression has a value, the match keyword, and at least one case clause.

  // 引入随机变量类Random生成变量x
  import scala.util.Random
  val x:Int =Random.nextInt(10)
  def randomNum:String=x match{
    case 0 => "zero"
    case 1 => "one"
    case _ => "other" // 如果x不为0或者1则默认输出other
  }
  println("x value is :"+ x + ",case result is: " +randomNum)
}

//  ------------------------* Matching on case classes *------------------------------------------
// 类比于match 基本变量，也可对对象进行match,如下：定义一个抽象类和其实现，然后通过抽象类去match
abstract class Notification
case class Email(sender:String,title:String,msg:String) extends Notification
case class Sms(sender:String,msg:String)  extends  Notification
case class Tel(phone:Int,msg:String) extends Notification

object matchOnCaseClass extends App{
  def showNotification(notification: Notification):String={
    notification match {
      case Email(sender,_,msg) => s"you got an email from $sender"
      case Sms(sender,msg) => s"you got an sms from $sender,msg is :$msg"
      case Tel(phone,msg) => s"you got an phone from $phone"
    }
  }
  val someEmail=Email("scala@qq.com","hello","so happy?")
  val someSms=Sms("spark","hello sms")
  println(showNotification(someEmail))
  println(showNotification(someSms))
}

//  ------------------------* Pattern guards *------------------------------------------
/**
  *  Pattern guards are simply boolean expressions which are used to make cases more specific.
  *  Just add if <boolean expression> after the pattern.
  *
  *  可在case表达式上加 boolean判断，对case到的结果进行进一步的过滤
  *  格式： case pattern if boolean => rst
  */
object patternGuards extends App{
  def showNotification(notification: Notification):String={
    notification match {
      case Email(sender,_,msg) if(!sender.equals("scala@qq.com"))=> s"you got an email from $sender"
      case Sms(sender,msg) =>  s"you got an sms from $sender,msg is :$msg"
      case Tel(phone,msg)  if  111==phone => s"you can only receive from  $phone"
      case _ => ""
    }
  }
  val someEmail1=Email("scala@qq.com","hello","you will not receive this email")
  val someEmail2=Email("notScala@qq.com","hello","the if retrun true")
  val someSms=Tel(111,"hello 111")
  println(showNotification(someEmail1)) // output:  null
  println(showNotification(someEmail2)) // output: you got an email from notScala@qq.com
  println(showNotification(someSms))  // output: you can only receive from  111
}