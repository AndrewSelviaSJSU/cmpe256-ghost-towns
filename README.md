# Ghost Towns

In order to work on all the projects at once, open a terminal to the directory where you'd like to store the project, then run:  
```shell script
git clone git@github.com:AndrewSelviaSJSU/cmpe256-ghost-towns.git
cd cmpe256-ghost-towns
open . -a "IntelliJ IDEA"
```

Now, within this global project, add modules for the specific projects on which you need to work.

## preparer

![Preparer CI](https://github.com/AndrewSelviaSJSU/cmpe256-ghost-towns/workflows/Preparer%20CI/badge.svg)

This is our data preparation code.

### IntelliJ IDEA Set-up Instructions

1. File/New/Module...
2. Select `preparer/build.sbt`

Now that `preparer` is set up as a module, ensure that you have set up its dependencies correctly.

The Spark documentation specifies [the versions of Java & Scala to use](https://spark.apache.org/docs/latest):
> Spark runs on Java 8, Python 2.7+/3.4+ and R 3.1+. For the Scala API, Spark 2.4.5 uses Scala 2.12. You will need to use a compatible Scala version (2.12.x).

#### JDK

1. File/Project Structure/Modules/preparer
2. Set *Module SDK* to the latest version of JDK 8
3. Select *OK*

#### Scala

1. File/Project Structure/Platform Settings/Global Libraries
2. Select the *+* button
3. Select *Scala SDK*
4. Select *Download* at the bottom-left
5. Select *2.12.11*
6. Select *OK*
7. Select *OK*

## References

### Inspiration

* [Medium: Large Scale Graph Mining with Spark (Part 1)](https://towardsdatascience.com/large-scale-graph-mining-with-spark-750995050656)
* [Medium: Large Scale Graph Mining with Spark (Part 2)](https://towardsdatascience.com/large-scale-graph-mining-with-spark-part-2-2c3d9ed15bb5)
* [GitHub: Large Scale Graph Mining with Spark](https://github.com/wsuen/pygotham2018_graphmining)

### Papers

* [ArchiveSpark Paper](https://www.helgeholzmann.de/papers/JCDL_2016_ArchiveSpark.pdf)
* [Community detection in graphs](https://arxiv.org/pdf/0906.0612.pdf)

### Preparer

* [ArchiveSpark Paper](https://www.helgeholzmann.de/papers/JCDL_2016_ArchiveSpark.pdf)
* [Sort-friendly URI Reordering Transform (SURT)](http://crawler.archive.org/articles/user_manual/glossary.html#surt)
* [Using ArchiveSpark as a Library](https://github.com/helgeho/ArchiveSpark/blob/master/docs/Using_Library.md)
* [ArchiveSpark Documentation](https://github.com/helgeho/ArchiveSpark/blob/master/docs/README.md)
* [ArchiveSpark Operations](https://github.com/helgeho/ArchiveSpark/blob/master/docs/Operations.md)
* [Enrichment Functions](https://github.com/helgeho/ArchiveSpark/blob/master/docs/EnrichFuncs.md)
* [Data Specs](https://github.com/helgeho/ArchiveSpark/blob/master/docs/DataSpecs.md)
* [ArchiveSpark Recipes/Examples](https://github.com/helgeho/ArchiveSpark/blob/master/docs/Recipes.md): The links here are *critical* to getting started!
* [The WARC Ecosystem](https://www.archiveteam.org/index.php?title=The_WARC_Ecosystem#Tools): I discovered ArchiveSpark through this wiki.

### SBT

* [Sonatype: Maven Artifact Search Engine](https://search.maven.org)