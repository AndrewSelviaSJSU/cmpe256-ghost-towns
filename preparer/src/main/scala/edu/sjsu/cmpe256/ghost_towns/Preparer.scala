package edu.sjsu.cmpe256.ghost_towns

import io.lemonlabs.uri.Url
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{RelationalGroupedDataset, SparkSession}
import org.archive.archivespark._
import org.archive.archivespark.functions._
import org.archive.archivespark.model.MultiValueEnrichable
import org.archive.archivespark.specific.warc._
import org.graphframes.GraphFrame

import scala.collection.immutable
import com.roundeights.hasher.Implicits._

import scala.language.postfixOps

object Preparer extends {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder.appName("Preparer").config("spark.master", "local").getOrCreate
    sparkSession.sparkContext.setLogLevel("WARN")
    import sparkSession.implicits._
    val warcPath = "/Users/aselvia/Downloads/CC-MAIN-20200216182139-20200216212139-00000.warc.gz"
    val cdxPath = "/Users/aselvia/Downloads/CC-MAIN-20200216182139-20200216212139-00000.cdx.gz"

    // TODO: DO NOT DELETE THE NEXT TWO LINES!!!!! They must be run for each new WARC file for more efficient parsing
    //    val warc = ArchiveSpark.load(WarcSpec.fromFiles(warcPath))
    //    warc.saveAsCdx(cdxPath)

    val records = ArchiveSpark.load(WarcSpec.fromFiles(cdxPath, warcPath))
    //    println(records.count)

    // Print out a single record with its HTML content
    //    println(records.sample(withReplacement = false, 1.0/records.count).enrich(HtmlText).peekJson)

    // Print out a single record with all outlinks extracted
    val Links = Html.all("a")
    val LinkUrls = HtmlAttribute("href").ofEach(Links)
    val first = records.sample(withReplacement = false, 3.0 / records.count).enrich(LinkUrls).first
    val jsonString = first.toJsonString
    println(jsonString)

    //    val originalUrl = first.originalUrl
    //    val strings: Seq[(String, String)] = first.enrichment("payload").get.enrichment("string").get.enrichment("html").get.enrichment("a").get.asInstanceOf[MultiValueEnrichable[Nothing]].children.map(child => (originalUrl, child.enrichment("attributes").get.enrichment("href").get.get.toString))


    val value =
      records
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

    val parentTLDs = value.map(_._1)
    val childTLDs = value.map(_._2)
    val verticesRdd = parentTLDs.union(childTLDs).distinct.keyBy(_.sha1.hex)
    val vertices = verticesRdd.toDF("id", "name")
    vertices.printSchema
    vertices.head(50).foreach(println)
    val edges = value.map { case (parentTLD, childTLD) => (parentTLD.sha1.hex, childTLD.sha1.hex) } toDF("src", "dst")
    edges.printSchema
    edges.head(50).foreach(println)

    val graph = GraphFrame(vertices, edges)
    val communities = graph.labelPropagation.maxIter(5).run
    communities.select("id", "label").show
    //+--------------------+-----------+
    //|                  id|      label|
    //+--------------------+-----------+
    //|9e4c65ef3d5fd8f28...|94489280512|
    //|0370310a8f5f7db0b...|94489280512|
    //|6ddb37897a8bdc1ae...|94489280512|
    //|8d1d4789a4ad440f6...|94489280512|
    //|d7e222c8d7ba68d80...|94489280512|
    //|06252e37318dc5e20...|94489280512|
    //|11b76b017f8eef1ff...|94489280512|
    //|d6e8607b10d89dfce...|94489280512|
    //|465806fbb3547c258...|94489280512|
    //|baea954b95731c68a...|94489280512|
    //|9dcbb8ebccdd952eb...|60129542144|
    //+--------------------+-----------+

    println(s"count: ${communities.count}")
    //count: 2
    val communitiesGroupedByLabel = communities.groupBy("label")
    println(s"community count: ${communitiesGroupedByLabel.count()}")
    //community count: [label: bigint, count: bigint]
    communities.rdd.groupBy(_.get(2)).sortBy(_._2.size, ascending = false).take(50).foreach(println)
    //(996432412672,CompactBuffer([b60b80ebfb2b6db9a85fe6cc1f4ac3baee509c14,altarta.com,996432412672]))
    //(747324309504,CompactBuffer([7fe3eb02242f7ab0318d5e7decda19a9bd121ef1,123rf.com,747324309504]))

    sparkSession.stop
  }
}