package com.liyz.scala.starter

class TraitDemo {

}

/**
  * trait:
  * Traits are used to share interfaces and fields between classes.
  * They are similar to Java 8’s interfaces.
  * Classes and objects can extend traits but traits cannot be instantiated and therefore have no parameters.
  *  类似于java中的interface，类和对象都可以继承trait，trait不能被声明，没有成员变量
  */

trait Logger{
  // 1.可以有具体的方法实现
  def log(msg:String): Unit ={
    println("parent Trait "+msg)
  }
}

trait Logger1{
  // 2.类似于java中的接口,定义方法
  def log(msg:String)
}
// 3.trait间的继承，和父trait定义接口的实现
trait ConsoleLogger extends Logger1{
  def log(msg:String): Unit ={
    println("sub trait --> "+msg)
  }
}

// 4.类对trait的继承
class TraitC extends ConsoleLogger {
  def test: Unit ={
    log("class ")
  }
}

trait ObjectTrait{
  def objM(): Unit ={
    println("obj has this trait")
  }
}

object TraitC extends App{
  val t1=new TraitC
  t1.test

  // 给指定对象加trait
  val t2=new TraitC with ObjectTrait
  t2.objM()
}

// ----------------------------------* Subtyping start *-----------------------------------
/**
  * Subtyping
  * Where a given trait is required, a subtype of the trait can be used instead.
  * 在需要给定特征的情况下，可以使用特征的子类型
  * The trait Pet has an abstract field name which gets implemented by Cat and Dog in their constructors.
  * On the last line, we call pet.name which must be implemented in any subtype of the trait Pet.
  *
  * 类似于java中的多态使用，声明变量时使用trait，实际赋值是trait的子类
  */
import scala.collection.mutable.ArrayBuffer

trait Pet {
  val name: String
}

class Cat(val name: String) extends Pet
class Dog(val name: String) extends Pet

object TraitDemo extends App{
  val dog = new Dog("Harry")
  val cat = new Cat("Sally")

  val animals = ArrayBuffer.empty[Pet]
  animals.append(dog)
  animals.append(cat)
  animals.foreach(pet => println(pet.name))  // Prints Harry Sally
}
// ----------------------------------* Subtyping end *-----------------------------------

// ----------------------------------* CLASS COMPOSITION WITH MIXINS start *-----------------------------------
/**
  *  class 和 trait的混合使用 解决单继承问题
  *   Classes can only have one superclass but many mixins (using the keywords extends and with respectively).
  *   The mixins and the superclass may have the same supertype.
  *   类只允许有一个父类，但是可以通过trait和extends混合使用达到多继承目的
  *   汇合类需要有相同的父类类型
  */
abstract class AbsIterator{
  type T
  def hasNext:Boolean
  def next():T
}
class StringIterator(s:String) extends AbsIterator{
  type T = Char
  private var i=0
  def hasNext = i<s.length
  def next() = {
    val ch=s charAt i
    i+=1
    ch
  }
}
// Because RichIterator is a trait, it doesn’t need to implement the abstract members of AbsIterator.
// trait不需要实现abstract class中定义的方法
trait RichIterator extends AbsIterator{
  def foreach(f: T => Unit ):Unit = while(hasNext) f(next())
}

object StringIteratorTest extends App{
  // 实现了复合类型 compound types
  class RichStringIter extends StringIterator("scala") with RichIterator
  val richStringIter=new RichStringIter
  richStringIter foreach println
}

// ----------------------------------* CLASS COMPOSITION WITH MIXINS end *-----------------------------------

// ----------------------------------* SELF-TYPE start *-----------------------------------
/**
  *  Self-types are a way to declare that a trait must be mixed into another trait, even though it doesn’t directly extend it.
  *  That makes the members of the dependency available without imports.
  *  self-type可以使一个trait可以汇合进另一个trait中去
  *  self-type必须被其他trait直接或者间接的继承
  *  class 在继承含有sel-types的trait时，必须同时继承this所指向的trait
  */

// ----------------------------------* SELF-TYPE end *-----------------------------------
trait Users{
  def username:String
}
trait Tweeter{
  this:Users =>
  def tweet(tweeText:String) = println(s"$username:$tweeText")
}
class VerifiedTweeter(val username_ :String) extends Tweeter with Users  {
  def username =s"real $username_"
}

object VerifiedTweeter extends App{
  val realBeyonce=new VerifiedTweeter("Beyonce")
  realBeyonce.tweet("just spilled my glass of lemonade")
}