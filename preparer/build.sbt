import Dependencies._
import sbt.Keys.mainClass

ThisBuild / scalaVersion     := "2.11.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "edu.sjsu.cmpe256.ghost-towns"
ThisBuild / organizationName := "GhostTowns"
ThisBuild / useCoursier      := false // https://stackoverflow.com/a/58456468/6073927

lazy val preparer = (project in file("."))
  .settings(
    name := "preparer",
    mainClass in assembly := Some("edu.sjsu.cmpe256.ghost_towns.Preparer")
  )

resolvers := Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "SparkPackages" at "https://dl.bintray.com/spark-packages/maven")

libraryDependencies := Seq(
  "com.github.helgeho" %% "archivespark" % "3.0.1",
  "com.outr" %% "hasher" % "1.2.2",
  "graphframes" % "graphframes" % "0.8.0-spark2.4-s_2.11",
  "io.lemonlabs" %% "scala-uri" % "1.4.10",
  "io.spray" %% "spray-json" % "1.3.4",
  "org.apache.spark" % "spark-graphx_2.11" % "2.4.0",
  ("org.apache.spark" %% "spark-sql" % "2.4.0")
    .exclude("org.mortbay.jetty", "servlet-api")
    .exclude("commons-beanutils", "commons-beanutils-core")
    .exclude("commons-collections", "commons-collections")
    .exclude("commons-logging", "commons-logging")
    .exclude("com.esotericsoftware.minlog", "minlog"),
  scalaTest % Test
)

assemblyMergeStrategy in assembly := {
  // Added by Andrew Selvia
  case "git.properties" => MergeStrategy.discard

  // Added to handle issues with sbt-assembly merging Spark: http://queirozf.com/entries/creating-scala-fat-jars-for-spark-on-sbt-with-sbt-assembly-plugin
  case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}