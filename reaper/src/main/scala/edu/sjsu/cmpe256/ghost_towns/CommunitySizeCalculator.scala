package edu.sjsu.cmpe256.ghost_towns

import resource.managed

import scala.io.Source

object CommunitySizeCalculator extends App {
  private val communitySizes = (0 to 134)
    .flatMap(n =>
      managed(Source.fromFile(s"/Users/aselvia/Developer/github.com/AndrewSelviaSJSU/cmpe256-ghost-towns/output/communities-grouped-by-label/part-${"%05d".format(n)}"))
        .map(_.getLines.map(_.split(",").tail.length / 3).toList)
        .opt
        .getOrElse(List()))
    .reverse
  println(communitySizes.last)
  communitySizes
    .groupMap(x => x)(x => x)
    .view
    .mapValues(_.size)
    .toList
    .sortBy(_._1)
    .groupBy {
      case (communitySize, _) =>
        communitySize match {
          case n if n == 0 => 0
          case n if n == 1 => 1
          case n if n <= 5 => 2
          case n if n <= 10 => 3
          case n if n <= 20 => 4
          case n if n <= 50 => 5
          case _ => 6
        }
    }
    .filter(_._1 > 0)
    .toList
    .sortBy(_._1)
    .map(_._2.map(_._2).sum)
    .map(nodesInGroup => s"$nodesInGroup\t")
    .foreach(print)
}
