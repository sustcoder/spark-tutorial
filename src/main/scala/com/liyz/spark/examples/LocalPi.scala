// scalastyle:off println
package com.liyz.spark.examples

import scala.math.random

/**
  * sparkPi计算原理:
  * 半径为1的圆其面积为π，计算边长为2的正方形内，圆的面积概率
  * 在一个边长为2的正方形内画个圆，正方形的面积 S1=4，圆的半径 r=1，面积 S2=πr^2=π
  * 现在只需要计算出S2就可以知道π，这里取圆心为坐标轴原点，在正方向中不断的随机选点，
  * 总共选n个点，计算在圆内的点的数目为count，则 S2=S1*count/n
  * 随机选取点：random*2-1 ,random值区间为[0,1),x和y的值区间为[-1,1)
  * 判断点是否在圆内：x*x+y*y<=1
  * 计算在院内点占比：count/1000000*/
object LocalPi {
  def main(args: Array[String]) {
    var count = 0
    for (i <- 1 to 100000) {
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x*x + y*y <= 1) count += 1
      println(s"x=${x}, y=${y}, x*x=${x*x}, y*y=${y*y}, random=${random}")
    }
    println("Pi is roughly " + 4 * count / 100000.0)
  }
}
// scalastyle:on println
