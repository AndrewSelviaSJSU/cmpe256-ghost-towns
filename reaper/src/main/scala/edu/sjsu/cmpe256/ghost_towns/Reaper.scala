package edu.sjsu.cmpe256.ghost_towns

import java.nio.file.StandardOpenOption.{APPEND, CREATE, CREATE_NEW, TRUNCATE_EXISTING}
import java.nio.file.{Files, Path, Paths}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.HEAD
import akka.http.scaladsl.model.{HttpRequest, Uri}
import edu.sjsu.cmpe256.ghost_towns.Reaper.Reap
import resource.managed

import scala.concurrent.ExecutionContextExecutor
import scala.io.Source
import scala.util.{Failure, Success}

object Reaper {
  val path: Path = Paths.get("output/reaper.csv")
  Files.write(
    path,
    "url,isAlive\n".getBytes(),
//    CREATE_NEW)
    TRUNCATE_EXISTING)

  final case class Reap(dnsName: String)

  def apply(): Behavior[Reap] = Behaviors.receive { (context, reap) =>
    implicit val executionContext: ExecutionContextExecutor = context.executionContext
    implicit val actorSystem: ActorSystem[Nothing] = context.system
    val uri = Uri(s"http://${new String(reap.dnsName.getBytes("ISO-8859-1"), "UTF-8")}")
    Http(actorSystem).singleRequest(HttpRequest(HEAD, uri)).onComplete {
      case Success(httpResponse) =>
        context.log.info(s"$uri Status Code: ${httpResponse.status.intValue}")
        val statusCode = httpResponse.status.intValue.toString
        Files.write(
          path,
          s"${reap.dnsName},${(!(statusCode.startsWith("4") || statusCode.startsWith("5"))).toString}\n".getBytes(),
          CREATE, APPEND)
        httpResponse.discardEntityBytes()
      case Failure(_) =>
        context.log.error(s"$uri Status Code: Failed")
        Files.write(
          path,
          s"${reap.dnsName},false\n".getBytes(),
          CREATE, APPEND)
    }
    Behaviors.same
  }
}

object ReaperApp extends App {
  val reaperActorSystem = ActorSystem(Reaper(), "reaper")
  val fileName = args(0)
  val uris = managed(Source.fromFile(fileName))
    .map(_.getLines.drop(1).map(_.split(",")(1)).toList)
    .opt
    .getOrElse(List())

  uris
    .foreach(dnsName => {
      reaperActorSystem ! Reap(dnsName)
      Thread.sleep(100)
    })
}
