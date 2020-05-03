package edu.sjsu.cmpe256.ghost_towns

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.HEAD
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import com.typesafe.config.Config
import edu.sjsu.cmpe256.ghost_towns.Reaper.Reap
import resource.managed

import scala.concurrent.ExecutionContextExecutor
import scala.io.Source
import scala.util.{Failure, Success}

object Reaper {
  final case class Reap(uri: Uri)

  def apply(): Behavior[Reap] = Behaviors.receive { (context, message) =>
    context.log.info(s"Requesting $message")
    implicit val executionContext: ExecutionContextExecutor = context.executionContext
    implicit val actorSystem: ActorSystem[Nothing] = context.system
    Http(actorSystem).singleRequest(HttpRequest(HEAD, message.uri)).onComplete {
      case Success(httpResponse) =>
        context.log.info(s"${message.uri} Status Code: ${httpResponse.status.intValue}")
        httpResponse.discardEntityBytes()
      case Failure(_)   => context.log.error(s"${message.uri} Status Code: Failed")
    }
    Behaviors.same
  }
}

object ReaperApp extends App {
  val reaperActorSystem = ActorSystem(Reaper(), "reaper")
  val uris =
    if (false) List("co.jp", "hyipola.com", "tapchitienao.com")
    else managed(
      Source.fromFile("/Users/aselvia/Developer/github.com/AndrewSelviaSJSU/cmpe256-ghost-towns/output/vertices/part-00000-fe055b24-eaf6-4ed8-9aaa-946a6521faa5-c000.csv"))
      .map(_.getLines.drop(1).map(_.split(",").last).toList)
      .opt
      .getOrElse(List())
  uris.foreach(dnsName => reaperActorSystem ! Reap(Uri(s"http://${new String(dnsName.getBytes("ISO-8859-1"), "UTF-8")}")))
}
