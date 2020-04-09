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

* [ArchiveSpark Paper](https://www.helgeholzmann.de/papers/JCDL_2016_ArchiveSpark.pdf)
* [Sort-friendly URI Reordering Transform (SURT)](http://crawler.archive.org/articles/user_manual/glossary.html#surt)