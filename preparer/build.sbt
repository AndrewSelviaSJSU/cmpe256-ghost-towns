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

resolvers := Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies := Seq(
  "org.apache.spark" %% "spark-sql" % "2.4.5",
  "com.github.helgeho" %% "archivespark" % "3.0.1",
  scalaTest % Test
)