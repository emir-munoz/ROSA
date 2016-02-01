---
layout: index
---

# Welcome to ROSA

Rosa is an **R**DF and **O**ntology con**S**traints **A**nalyzer implemented on top of Scala and Apache Spark.

## Pre-requisites

- [Apache Spark](http://spark.apache.org/) -- you can download the pre-built version with Hadoop 2.6 or later.
- [Scala](http://www.scala-lang.org/)

## ROSA source code management

If you are interested in extending or working directly with the code, you can also check out the master branch from Git.

```
git clone https://github.com/emir-munoz/ROSA.git
```

## Compiling from sources

To generate a fat `.jar` with all dependencies,

```
mvn package -DskipTests
```

## Running project

To execute ROSA on top of Spark using multiple threads in a local machine

In Spark 1.5.1

```
../../spark-1.5.1-bin-hadoop2.6/bin/spark-submit --driver-memory 6g --executor-memory 3g --class "spine.discovery.DiscoveryCard" --master local[2] target/rdf-spine-0.0.1-SNAPSHOT-jar-with-dependencies.jar 2 [[RDF_FILE]] [[RDF_CLASS]]
```

## Example output

```
[INFO ] 2015-12-17 17:41:09.054 [main] (ProjectionOp$          ) - 3042916 RDF triples found in file 'experiments/dbpedia-careerstation/careerstation.nt.gz'
[INFO ] 2015-12-17 17:41:09.056 [main] (ProjectionOp$          ) - 643162 different subjects found in file 'experiments/dbpedia-careerstation/careerstation.nt.gz'
[INFO ] 2015-12-17 17:41:14.331 [main] (DiscoveryCard$         ) - Number of different subjects: 643162
[INFO ] 2015-12-17 17:42:47.117 [Executor task launch worker-0] (DiscoveryCard$         ) - card({http://dbpedia.org/ontology/numberOfMatches}, http://dbpedia.org/ontology/CareerStation) = (0, 3)
[INFO ] 2015-12-17 17:42:47.118 [Executor task launch worker-0] (DiscoveryCard$         ) - card({http://dbpedia.org/ontology/team}, http://dbpedia.org/ontology/CareerStation) = (0, 3)
[INFO ] 2015-12-17 17:42:50.040 [Executor task launch worker-1] (DiscoveryCard$         ) - card({http://www.w3.org/1999/02/22-rdf-syntax-ns#type}, http://dbpedia.org/ontology/CareerStation) = (2, 2)
[INFO ] 2015-12-17 17:42:50.042 [Executor task launch worker-1] (DiscoveryCard$         ) - card({http://dbpedia.org/ontology/years}, http://dbpedia.org/ontology/CareerStation) = (0, 4)
[INFO ] 2015-12-17 17:43:10.190 [main] (DiscoveryCard$         ) - ShEx shape: 
      PREFIX ex: <http://example.com>
      PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      start=<AShape>

      <AShape> {
        
<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> .{2, 2} ,
<http://dbpedia.org/ontology/years> .{0, 4} ,
<http://dbpedia.org/ontology/numberOfMatches> .{0, 3} ,
<http://dbpedia.org/ontology/team> .{0, 3} ,
      }
```

## Spark options

According to the hardware settings, i.e., single or multiple machines, it may be required to set the memory allocated to the driver and executor.

```
--driver-memory 3g
--executor-memory 3g
```

## Contact

<i class="fa fa-envelope"></i> [Emir Munoz](http://emunoz.org)

> Last update: 30 January 2016
