
name := "spark-tutorial"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark"  %%  "spark-core"    % "2.2.0",
  "org.apache.spark"  %%  "spark-sql"     % "2.2.0",
  "org.apache.spark"  %% "spark-yarn"     % "2.2.0",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "latest.integration"
)