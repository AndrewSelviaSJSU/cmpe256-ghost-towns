import Dependencies._

ThisBuild / scalaVersion     := "2.11.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "edu.sjsu.cmpe256.ghost-towns"
ThisBuild / organizationName := "GhostTowns"
ThisBuild / useCoursier      := false // https://stackoverflow.com/a/58456468/6073927

lazy val preparer = (project in file("."))
  .settings(
    name := "preparer"
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
  "org.apache.spark" % "spark-graphx_2.11" % "2.4.5",
  "org.apache.spark" %% "spark-sql" % "2.4.5",
  scalaTest % Test
)