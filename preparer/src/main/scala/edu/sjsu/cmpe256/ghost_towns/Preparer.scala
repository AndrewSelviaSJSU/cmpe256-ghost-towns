package edu.sjsu.cmpe256.ghost_towns

import java.nio.file.{Files, Paths}

import com.roundeights.hasher.Implicits._
import io.lemonlabs.uri.Url
import org.apache.spark.sql.SaveMode.Overwrite
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.archive.archivespark._
import org.archive.archivespark.functions._
import org.archive.archivespark.model.MultiValueEnrichable
import org.archive.archivespark.specific.warc._
import org.graphframes.GraphFrame

import scala.language.postfixOps

object Preparer extends {
  def main(args: Array[String]): Unit = {
    val isDevelopmentRun = args(0).toBoolean
    val warcPath = args(1)
//    val isDevelopmentRun = false
//    val warcPath = "/home/014547273/cmpe256/ghost-towns/CC-MAIN-20200216182139-20200216212139-00000.warc.gz"

    val sparkSession = SparkSession.builder.appName("Preparer").config("spark.master", "local").getOrCreate
    sparkSession.sparkContext.setLogLevel("WARN")
    import sparkSession.implicits._

    val cdxPath = warcPath.replace("warc", "cdx")
    if (Files.notExists(Paths.get(cdxPath))) {
      ArchiveSpark.load(WarcSpec.fromFiles(warcPath)).saveAsCdx(cdxPath)
    }

    val records = ArchiveSpark.load(WarcSpec.fromFiles(cdxPath, warcPath))
//    println(records.count)

    // Print out a single record with its HTML content
//    println(records.sample(withReplacement = false, 1.0/records.count).enrich(HtmlText).peekJson)

    // Print out a single record with all outlinks extracted
    val Links = Html.all("a")
    val LinkUrls = HtmlAttribute("href").ofEach(Links)

//    val first = records.sample(withReplacement = false, 3.0 / records.count).enrich(LinkUrls).first
//    val jsonString = first.toJsonString
//    println(jsonString)

    val recordsToUse = if (isDevelopmentRun) sparkSession.sparkContext.parallelize(records.sample(withReplacement = false, 100.0 / records.count).take(100)) else records
    val parentChildTldTupleRdd =
      recordsToUse
        .enrich(LinkUrls)
        .flatMap(warcRecord =>
          warcRecord
            .enrichment("payload")
            .get
            .enrichment("string")
            .get
            .enrichment("html")
            .get
            .enrichment("a")
            .map(
              _
                .asInstanceOf[MultiValueEnrichable[Nothing]]
                .children
                .flatMap(
                  _
                    .enrichment("attributes")
                    .flatMap(_.enrichment("href"))
                    .map(_.get.toString)
                    .map(childUrl => Vector(warcRecord.originalUrl, childUrl)
                      .map(Url.parseOption(_).flatMap(_.apexDomain))
                      .filter(_.isDefined)
                      .map(_.get))
                    .flatMap {
                      case Vector(parentTld, childTld) =>
                        if (parentTld != childTld) Option((parentTld, childTld))
                        else Option.empty
                      case _ => Option.empty
                    }
                ))
            .getOrElse(List()))
        .distinct

    val parentTlds = parentChildTldTupleRdd.map(_._1)
    val childTlds = parentChildTldTupleRdd.map(_._2)
    val verticesRdd = parentTlds.union(childTlds).distinct.keyBy(_.sha1.hex)
    val vertices = verticesRdd.toDF("id", "name")
    vertices
      .sort("name")
      .write
      .mode(Overwrite)
      .options(Map(("header", "true")))
      .csv("output/vertices")

    val edges = parentChildTldTupleRdd.map { case (parentTLD, childTLD) => (parentTLD.sha1.hex, childTLD.sha1.hex) } toDF("src", "dst")
    edges
      .write
      .mode(Overwrite)
      .options(Map(("header", "true")))
      .csv("output/edges")

    val graph = GraphFrame(vertices, edges)
    val iterations = if (isDevelopmentRun) 1 else 5
    val communities = graph.labelPropagation.maxIter(iterations).run
    communities
      .write
      .mode(Overwrite)
      .options(Map(("header", "true")))
      .csv("output/communities")

    communities
      .rdd
      .groupBy(_(2).asInstanceOf[Long])
      .sortBy(_._2.size, ascending = false)
      .saveAsTextFile("output/communities-grouped-by-label")

    sparkSession.stop
  }
}