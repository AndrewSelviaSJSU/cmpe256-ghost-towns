import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "edu.sjsu.cmpe.256.ghost-towns"
ThisBuild / organizationName := "GhostTowns"

lazy val root = (project in file("."))
  .settings(
    name := "preparer",
    libraryDependencies += scalaTest % Test
  )