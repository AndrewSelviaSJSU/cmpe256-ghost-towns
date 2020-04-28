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
    //[8a01d51d5ed069a4d900e892b8f013a50ef1d8e9,cgd.go.th]
    //[52bd42e61fd31854eb3216bff9194b0b067eab6f,littleriveroutfitters.com]
    //[1b0d6e2b610c8adb9f09112ef6d562b6e55069e3,premier.org.uk]
    //[9501d1d246e3dbf05406eb928fc4cd66a612bb14,hcxww.com]
    //[6650205feafec8feaad7b512d6bd9d6fea66aa3f,afixerupper.com]
    //[755352347199db2b80c8cc572195b64ffc20fd04,ampps.com]
    //[a952182f66ccfa0de8fa81bdfd333838a0e8ec27,rapidgrowth.ru]
    //[17732d08dd1d6eac559039cc208f524637113a96,movook.com]
    //[6fed499ae58c6b942320d3ebcd48036123266e3a,bella-marie.com]
    //[80de0ff0f55bf435742a6189f3d3e294bf2f13d9,researchyouth.eu]
    //[a3446ae74c0aaabf31ccfb64f080ef6068913003,water-server.biz]
    //[39f54dde4526a295583d2093141051ad4b5d9f31,tastefullyfrugal.org]
    //[62111cc106c7edf451ea2864d0ca14898b1dba36,salacuatro.com]
    //[ca61b22f906171b9483260697b2c6c19a46815ca,elizabethwattssoprano.com]
    //[651760a3799733e49e6807c3004ae337fd8cd050,worldfuturecouncil.org]
    //[09adb35ba54bbbcc7c3eda56e273ed1f9a2c69a7,nordwest-ticket.de]
    //[1b8678dd92e9134fd385087a799a6349d9c18a2d,bigdecisions.com]
    //[de6c608453793038d1ecc23540828d478f619a26,dewahome.co.jp]
    //[f44a6d335543fdeb15f195937f7cd19aeb9d7ee8,laboratoriosacme.net]
    //[a118f084eb259e065bc552fe31b2d8bf3ad0b1e3,czpll.com]
    //[b9c4439ca26aa3544345a04b0a3c0589626c3561,mustdotravels.com]
    //[80f5ecb89b0fb5d5a6b4d0fc1bedf3b2a7a41b3f,viewfromtheback.com]
    //[0bdefa1385d945a61e067c8aa602af56ef59cb86,minseg.gob.ar]
    //[3cb8b587be4878514b643175ef11c2bae09564be,mtlzdfu.ga]
    //[403b32af62e69e55985d42f7e9e058237425b58b,nipendo.com]
    //[c0e355f860c59149f53d4b0b9d566bcbfa1c097b,168zcw.com]
    //[b6b2a6fe0f57d908fcfb8bcfdbc9d6ace85b5655,indymedia.ca]
    //[4949f03249c3422d424ecedbab20031cfc600197,mayaravieira.com.br]
    //[9fded13a09083612a904fe64c7eed841549066d7,paolocacciolati.blogspot.com]
    //[83fcf1c6af2211e6ea955551198d8e8b480cfacb,tissotwatches.com]
    //[7e494f8b2b55c7cfc6f8ea0bcbc7a83ebb3f92b7,babypark.jp]
    //[e4e8db22ffa9d737e31e4b17947e74541c843093,stm-licht.de]
    //[c391a8e6b816f0586dfc0937e5754eb672f61107,beanforum.com]
    //[fe49d60a6d8a9705d5f0efe023f5ce756e88df13,aiwuyan184.com]
    //[1809f558af7e96f3255c51c7a37a39760142b453,radiationestimator.ru]
    //[45ad6f104d29d0ec39ab9ac08c26615c6bc8c4b2,fifa555sc.com]
    //[fabe3c046a3b9d9618b840a33bae008c1dc3ef04,slogin.info]
    //[f1957f7dd8a2e18797f7b94a6c22a82291265f33,alliancegenome.org]
    //[b9787a5c25cfd62ea9d89e654911634523465ad0,tribuna.com]
    //[c1271fb65af5f3cbadf81dc36d986fc045a40d40,himplasia.eu]
    //[7614b8bf8c59cbe8eae4ecd02b5cc7d17d2d192c,programming-school-hikaku.jp]
    //[03a5e04276cabbc5cb39db7f331639cf0f867cb3,gaccom.jp]
    //[d8d4c73b675a800c0e7814cf451070032b8844a1,speechtherapyaustintx.com]
    //[1851e91755186fbba83916d918416b87f2495540,bookblog.ro]
    //[d72aea3473bf50bed1192c0e5c2d245bdb96fc8c,copystar.com]
    //[4783930d5da9510341545f53240955d251206da3,bfm.ru]
    //[a6440e9b0eb5a9f1bedd511093b097da9c4cf26e,todoiphone.net]
    //[45d46e482afa50f09889087761ed5f20f8c5da56,egmlaw.com]
    //[715c5584abebe4dc00ac6e93b9641f84a5fa1b20,elkalimanews.com]
    //[a1cae854f27348877dabe7529996ff2107e0446f,andornot.com]
    val edges = value.map { case (parentTLD, childTLD) => (parentTLD.sha1.hex, childTLD.sha1.hex) } toDF("src", "dst")
    edges.printSchema
    edges.head(50).foreach(println)
    //[22f71014ce5b072258571372b13b10a97345cba3,70e9b6a9794eaedfcc346e6f20bdc7d3da8bbbf3]
    //[2e2850e5e3c1665f301be1171d9dce8a41990fca,ccb117a9f93f6bc7de5cbf6acc022baeb08170f9]
    //[6fb570f9039d0d4429b8914b9f706658ad41a035,deb78ddefd8edc4d0b2b4d2d4b26dbd172cb7deb]
    //[d4ec74af461e1d70f4a7b8bb0e531e3247fc60ee,32dc97161870868bfd54a75187d5ebafe8594336]
    //[d11304f7e66d4e42331560cb347a207ee39c54f4,eebc3eabb4878c85e39c53d2633f066d0fbc4d82]
    //[44a1b8039bd12e84e2525fb60b6a0d0faa95ddbf,8751a27a0b7ea911b1fd202d34602123df995951]
    //[8174a14236af7451cae34f464435f2c2925880ad,568bde79d28d2060d52d5fd452ec410de3a119d1]
    //[f1fc6930f09dad7232258569d58cb2527743dceb,dd740ba9dcf83d585fa4dc8977ac0f86db74810f]
    //[abd27f8a6b336945196824af4f0b1cdb58850320,baea954b95731c68ae6e45bd1e252eb4560cdc45]
    //[25ffb0feefd17075c15e22ec183aaa832050ada8,932f08e5019baf91accbc6e29e0d6d7eaeeccb0c]
    //[f067c9454fbf1f87db6ff19a9ac6d6833bf521b9,86fc16c8e1165c1f666410be25ffe96cb7cafa74]
    //[2b1739a002fb09bac43830ee414aa8a895dd0850,97ab0ec4e27ecdb1ec26ae682fe28a81139216b1]
    //[6ccdc0e7745abbd5b18bb33f964562b5048e71df,e10172d546ef3d17bc10c3d013cf03a21d2525e5]
    //[0f382f9ccaafa1891629a3efc2a7aa6c8c713f70,baea954b95731c68ae6e45bd1e252eb4560cdc45]
    //[969302425644ea15e5d77c5b0cc5c9ce3eb56afe,b7c70898d90f5bb3a32353817e451b646b40299a]
    //[dc2347137ccc3492a2679ab04244687b33d3605e,baea954b95731c68ae6e45bd1e252eb4560cdc45]
    //[1914ffc35be71e064d9b5a0a5618788c2625d7b0,b7c70898d90f5bb3a32353817e451b646b40299a]
    //[efb0375013096d49f0659044764ab5cc3ac7130c,9c8581eb24a2481ff1579f85d42df8766d735fe3]
    //[b44a49a9561ad9bee594112fd9afeb7bf0b1c314,465806fbb3547c258cfa20becfef6e08f41c233b]
    //[7ba37ef8ca5c9fb0bbf82c7c2eaf26f79db5a0fc,d978986975dd84583ec40a8d300661af56cc55fa]
    //[768ae79d7937d3c4b5d7fe268a7c12af7fd1c940,db546baba3acb079f91946f80b9078ffa565e36d]
    //[f47791e33ea03c829d89fc0621f1fc51065ed83d,465806fbb3547c258cfa20becfef6e08f41c233b]
    //[88e88b489d3439e820246e063de1f5e0a124fea4,755c16e64249d132ebd2728a527c0087b52a3141]
    //[3651cf90e44e15bc391dedb75f4cc7c4ba652e20,c29b7d2aa9321b1101c9abf49dc9af6b7e0d531b]
    //[b017daa4bc5ea197ef4b8c1472a0efa58b559932,08a51f8cf38355f50c6f1cdd2faf42b460c40426]
    //[6119efaeb1979d600a1039bb57fe09d07ebdb32a,9fc48cea6b68509c95d0700f06689157ad09d765]
    //[472ccb17dc5ad3bd44e5dcd44684f618433682da,576d962210c1322b637a5be6639d504b021a5e50]
    //[178dfeb61f1d8510abd42dfcdb8ee61b550783ee,0841b8637dcd77cd2baa35cf46f706962a99a974]
    //[02243431fbe07bc0fa14160526b71ca8a4cf0099,189131deebcb6f4da83f4310f6a6f1b80d790433]
    //[93f9687640cfa41220ddc983304b08e538fe564d,65c0b4d819806aa7165e48efc9d0e313d8c16330]
    //[7a312eea0fe8d3e01a36ab004ab19da1ff5a52c2,08c32d2cd0a819fad99fc6a834f72e2a166e4b42]
    //[b694f1aa2e7cc2b56fe7b8ca6a83d3ddc86f0330,ba56d7271cc4c3c7b95da5e64b4cf26ed4f8b854]
    //[d48d0bd8b48f2b0c0cd93b2433ac7e83bbb7282b,465806fbb3547c258cfa20becfef6e08f41c233b]
    //[42b373348e228fc2dd12d4cd67265c963ba7a437,bc5655139ff6380d40fd24109a47db9f15cdf131]
    //[be2b47dcc348f92dea462344f366991e31ef884d,06252e37318dc5e20c03ed8c995d6ee4963a721e]
    //[90bbca33f7556ce3543e0077f2082d03704fb924,3517461572e55b2b785797bae407131b877fc5d0]
    //[608ceb816e5ccb85bfb977d211c1904ba500b42b,959c54f80e3609def3fa9e37eee92c4d101cafc4]
    //[24be9f674adf5162ac3b7d25943dd6ca5ae4ee05,d84f49043447dd09db1e5e3b2bbbb9b9b8a120ac]
    //[dbcf2a9c6cd5040a97530bce05fd0f55948bb993,249838fd5298357de7ad9d10d0a40d77cf6ec4d2]
    //[debc3ed74871940ed707a402bc5163a0d1198354,56673be4f5ebf316ae540ae84cfe1a30d36bbea5]
    //[46a2648a203ec011e3b8330f49815794f827586a,2fc5e2a27487531f4dc1d050718c10c75da2cbb8]
    //[187d7cde34be0e1ab67145ad0739f30d07f1d50c,58f33f7f51a9e0bf68d5612aeb8f831ada36bc23]
    //[7a34d6394f3d35f86a37162f552f1e1726a098fa,06252e37318dc5e20c03ed8c995d6ee4963a721e]
    //[adda34f466d66ca93a8773184d42a102e9b65501,b7c70898d90f5bb3a32353817e451b646b40299a]
    //[a5a4a93f5717518e90d4e93e767198d244b27a96,0101d7486ce5468c10b132e868246e2d2370ff3d]
    //[563868ce69cf6df4dd5975458d79945097ad6580,465806fbb3547c258cfa20becfef6e08f41c233b]
    //[4c41cd7f7b589c4589b59c206ce079d6d206f538,a7f3818e71f0291e6c7b91ca7cbfb083aa8ba429]
    //[43d14c45a61233e8fa8abbaedc68ddc744c26793,c2208abde9668e8e9815c3690855edd1e63abeac]
    //[bfca420b0e327b55c8fede5a7d6f59c64c71ae27,b7c70898d90f5bb3a32353817e451b646b40299a]
    //[e9acfb11d19411a57d1019aee414e4705b6826ca,4205b59218cfa2c0354b63200566e0e820876711]

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