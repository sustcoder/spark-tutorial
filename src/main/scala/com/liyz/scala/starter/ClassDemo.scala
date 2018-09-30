package com.liyz.scala.starter

class ClassDemo {

}

// scala中的类都是public类，一个文件中可以声明多个
class Person1 {
  var name: String = _ // "_" : 定义变量，但不赋初始值,会生成get和set方法
  var age: Int = 10 // val定义变量的同时给赋予初始值：只生成get方法,var 表示动态属性，即可生成get方法也可生成set方法
  private var _salary: Int = 1000000 // 类的私有属性，对外不可见

  def getSalary=_salary // 定义方法setSalary去设置salary的值
}

// 1. 主构造器：参数直接跟在类名后、最后会分别编译成字段
// 2. 主构造器在执行时会执行类中所有的语句
class Person2(val name: String, val age: Int) {
  println("this is the primary constuctor!")

  // 3. 附属构造器的名称为this
  // 4. 每一个附属构造器，必须先调用已经存在的主构造器
  var gender: String = _

  def this(name: String, age: Int, gender: String) {
    this(name, age)
    this.gender = gender
  }

  val school: String = "parSch"

  var x: Int = 0
  x += 2
  println("调用主构造器时将执行类中所有语句,所以X的值为： " + x) // 输出 2

}

// 5. 假设参数声明时不带val或者var,那么相当于private[this]，只能在内部使用
class Person3(val name: String, age: Int) {
  println("property age con't use outside ")
}

// 6. 子类引用父类属性时，不需要加var
class Student(name: String, age: Int, val major: String) extends Person2(name, age) {
  println("this is the subClass of Person,major is : " + major)

  // 7. 子类重写父类方法必须加 override
  override def toString() = "Override toString ..."

  // 8. 子类重写父类的属性时必须加 override
  override val school: String = "subSch"

}

object ClassDemo {
  def main(args: Array[String]) {
    val p=new Person1()
    val p1 = new Person1 // 生成对象时括号可以省略
    p1.name = "jack"


    //    p.age=12 // age是 val属性，此处赋值会报错
    println(p1.name + "'s age is " + p1.age+" salary: "+p1.getSalary )

    val p2 = new Person2("tom", 20)
    println(p2.name + "'s age is " + p2.age)

    val p3 = new Person3("mac", 10)
    // p3.age // age私有故会报错


    val s1 = new Student("s", 30, "Math")
    println(s1.toString())

    println(s1.school)
  }
}

/**
  *  There are methods def x and def y for accessing the private data
  *  def x_= and def y_= are for validating and setting the value of _x and _y
  *  Notice the special syntax for the setters: the method has _= appended to the identifier of the setter and the parameters come after
  */
class Point {
  private var _x = 0
  private var _y = 0
  private val bound = 100

  // 定义方法x，给私有变量赋值
  def x = _x
  // 定义方法identifier_= 方法对set的值进行校验处理
  def x_= (newValue: Int): Unit = {
    if (newValue < bound) _x = newValue else printWarning
  }

  def y = _y
  def y_= (newValue: Int): Unit = {
    if (newValue < bound) _y = newValue else printWarning
  }

  private def printWarning = println("WARNING: Out of bounds")
}

object Point extends App{
  val point1 = new Point
  point1.x = 99
  point1.y = 101 // prints the warning
  println("x: "+point1.x+",y: "+point1.y)
}

// 定义抽象类
abstract class Person4{
  def speak
  val name:String
  var age:Int
}
// 抽象类中不需要加override关键字
class Student1 extends Person4{
  def speak{
    println("speak!!!")
  }
  val name:String="zhangsan"
  var age:Int=12
}

// -----------------------------* SINGLETON OBJECTS *----------------------------------
/**
  * Object : 类比于java中的static关键字，注意：scala中static不是关键字
  *
  * An object is a class that has exactly one instance. It is created lazily when it is referenced, like a lazy val.
  *  一个object对象只实例化一次，且是在被引用的时候进行加载
  *
  * As a top-level value, an object is a singleton.
  * 最高级别的Object对象（和类的定义平级）是一个单例模式的
  *
  * As a member of an enclosing class or as a local value, it behaves exactly like a lazy val.
  * 作为类的成员或作为本地变量，它的行为与懒惰的val完全相同
  * 个人理解： object类里的方法和变量都是静态的
  */

object  Loggers{
  // 类似于定义了一个静态类，调用时不用声明
  def info(message:String)=println(s"INFO:$message")
  var properties:String="static properties"
}

object Test extends App{
  // Object对象不需要声明
  Loggers.info("created projects")
  Loggers.properties="use object.prop set  new value"
  println(Loggers.properties)
}

/**
  * Companion objects:
  * An object with the same name as a class is called a companion object.Conversely, the class is the object’s companion class.
  * A companion class or object can access the private members of its companion.
  * Use a companion object for methods and values which are not specific to instances of the companion class.
  *
  * 伴侣（伴随）对象:
  * 当object和class的名字相同时，object是class的 companion objects，相反的class是object的 companion class
  * companion class和object之间的变量都是可直接访问的，注： class可以访问object的属性，object不可使用class的属性
  * 当一个方法或者属性和实例无关时可以放到类的comnion object中，所以可用object来实现工厂模式
  * 类比java: 将类的静态方法和属性抽离出来，单独放到一个objcet类型的对象中，但是在object不能使用class中的属性
  *
  */
case class Circle(radius:Double){
  import Circle._
  println(objProp)
  def area:Double=calculateArea(radius)
  val classProp:String="class prop con't access by object"
}
object Circle{
  import scala.math._
  var objProp:String="can be access by class"
  def calculateArea(radius: Double): Double= Pi*pow(radius,2)
 //  println(classProp) 会报错
}
object cal extends App{
  val circle=new Circle(5.0)
  println("cicle area is: "+circle.area)
}

// 用object实现工厂方法
class Emails(val username: String, val domainName: String)
object Emails {
  def fromString(emailString: String): Option[Emails] = {
    emailString.split('@') match {
      case Array(a, b) => Some(new Emails(a, b))
      case _ => None
    }
  }
}
object TestEmails extends App{
  val genEmail=Emails.fromString("scala.center@epfl.ch")
  genEmail match {
    case Some(emails) => println(s"registered an email username:${emails.username} domain name :${emails.domainName}")
    case None => println("Error: could not parse email")
  }
}


/**
  *  GENERIC CLASSES 泛型类
  *  Generic classes take a type as a parameter within square brackets [].
  *  One convention is to use the letter A as type parameter identifier, though any parameter name may be used.
  *
  *  UPPER TYPE BOUNDS:
  *  类似于java中的 <? extends T>  ?是T和T的子类
  *  In Scala, type parameters and abstract types may be constrained by a type bound.
  *  Such type bounds limit the concrete values of the type variables and possibly reveal more
  *  information about the members of such types. An upper type bound "T <: A "declares that type variable T refers to
  *  a subtype of type A.
  *  type parameters and abstract types: 参数类型和抽象类类型
  *  be constrained by ：被约束
  *  type bound：类型边界
  *  concrete： 具体的
  *
  *  LOWER TYPE BOUNDS:
  *  类似于java中的 <? super T> ?是T和T的父类
  *  While upper type bounds limit a type to a subtype of another type, lower type bounds declare a type to be a supertype of another type.
  *  The term "B >: A " expresses that the type parameter B or the abstract type B refer to a supertype of type A. In most cases,
  *  A will be the type parameter of the class and B will be the type parameter of a method.
  *
  */
abstract class Animals {
  def name: String
}
abstract class Pets extends Animals {}

class Cats extends Pets {
  override def name: String = "Cat"
}

class Dogs extends Pets {
  override def name: String = "Dog"
}

class Lions extends Animals {
  override def name: String = "Lion"
}

class PetContainers[P <: Pets](p: P) {
  def pet: P = p
}

object upperTypes extends App{
  val dogContainer = new PetContainers[Dogs](new Dogs)
  val catContainer = new PetContainers[Cats](new Cats)
  //  val lionContainer = new PetContainer[Lion](new Lion)
  //                         ^this would not compile
}

// INNER CLASSES
class Graph {
  class Node {
    var connectedNodes: List[Node] = Nil
    def connectTo(node: Node) {
      if (connectedNodes.find(node.equals).isEmpty) {
        connectedNodes = node :: connectedNodes
      }
    }
  }
  var nodes: List[Node] = Nil
  def newNode: Node = {
    val res = new Node
    nodes = res :: nodes
    res
  }
}
object Graph extends App{
  val graph1: Graph = new Graph
  val node1: graph1.Node = graph1.newNode
  val node2: graph1.Node = graph1.newNode
  val node3: graph1.Node = graph1.newNode
  node1.connectTo(node2)
  node3.connectTo(node1)

  /**
    * If we now have two graphs, the type system of Scala does not allow us to mix nodes defined within one graph with the nodes of another graph,
    *  since the nodes of the other graph have a different type. Here is an illegal program:
    *
    *  不同对象的内部类不能进行mix nodes
    */
  val graph2: Graph = new Graph
  val node4: graph2.Node = graph2.newNode
  val node5: graph2.Node = graph2.newNode
  node4.connectTo(node5)      // legal
  val graph3: Graph = new Graph
  val node6: graph2.Node = graph2.newNode
//  node4.connectTo(node6)      // illegal!
}