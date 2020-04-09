package edu.sjsu.cmpe256.ghost_towns

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.archive.archivespark._
import org.archive.archivespark.functions._
import org.archive.archivespark.model.MultiValueEnrichable
import org.archive.archivespark.specific.warc._

object Preparer extends {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder.appName("Preparer").config("spark.master", "local").getOrCreate
    import sparkSession.implicits._
    val warcPath = "/Users/aselvia/Downloads/CC-MAIN-20200216182139-20200216212139-00000.warc.gz"
    val cdxPath = "/Users/aselvia/Downloads/CC-MAIN-20200216182139-20200216212139-00000.cdx.gz"
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

    val originalUrl = first.originalUrl
    val strings: Seq[(String, String)] = first.enrichment("payload").get.enrichment("string").get.enrichment("html").get.enrichment("a").get.asInstanceOf[MultiValueEnrichable[Nothing]].children.map(child => (originalUrl, child.enrichment("attributes").get.enrichment("href").get.get.toString))


    val value: RDD[(String, String)] = records.sample(withReplacement = false, 3.0 / records.count).enrich(LinkUrls).flatMap(_.enrichment("payload").get.enrichment("string").get.enrichment("html").get.enrichment("a").get.asInstanceOf[MultiValueEnrichable[Nothing]].children.map(child => (originalUrl, child.enrichment("attributes").get.enrichment("href").get.get.toString)))
    val value1 = value.toDF("parentTLD", "childTLD")
    value1.printSchema
    value1.head(50).foreach(println)

    // Ex:
    // [http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr/decoration-evenementielle/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr/prestations-deco-evenementielle/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://www.mariages.net/decoration-mariage/cocoon-by-charlotte-gourdon--e165872/promotions]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr/decoration-interieur-blois/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://www.mariages.net]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,mailto:cocoon.charlottegourdon@gmail.com]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://www.facebook.com/charlottegourdondecoratrice]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://www.instagram.com/charlottegourdon_decoratrice/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://cocoonbycharlottegourdon.fr]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://wordpress.org]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://presscustomizr.com/customizr]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,#main]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,https://maps.google.com/?q=Meroni Arreda, Lissone, MB, Italia]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,tel:+39039794723]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://www.meroniarreda.com/contatti/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,#search-lightbox]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://www.meroniarreda.com/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,#]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://www.meroniarreda.com/]
    //[http://findnsave.providencejournal.com/offer/Playtek-116-Scale-Remote-Control-Construction-Truck-Crane/72180719/,http://www.meroniarreda.com/arredamenti-interni-lissone/]

    sparkSession.stop
  }
}