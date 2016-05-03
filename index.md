---
layout: index
---

Rosa is an **R**DF and **O**ntology con**S**traints **A**nalyzer implemented on top of Scala and Apache Spark.

## Pre-requisites

- [Apache Spark](http://spark.apache.org/) -- you can download the pre-built version with Hadoop 2.6 or later.
- [Scala](http://www.scala-lang.org/) (2.11.5)
- [Sesame](http://rdf4j.org/) (4.1.1)

## ROSA source code management

If you are interested in extending or working directly with the code, you can also check out the master branch from Git.

```bash
git clone https://github.com/emir-munoz/ROSA.git
```

## Compiling from sources

To generate a fat `.jar` with all dependencies,

```bash
mvn package -DskipTests
```

## Running project

To execute ROSA on top of Spark using multiple threads in a local machine

### Discovery of Cardinality Constraints

#### Baseline using SPARQL queries

```bash
$ java -Xmx2g -jar target/rdf-spine-0.1.0-jar-with-dependencies.jar -baseline  -file [RDF_FILE] -context [RDFS/OWL_CLASS]
```

where [RDF_FILE] is the RDF file to analyse, and [RDFS/OWL_CLASS] is the URI of the class used as context.

For example,

```bash
$ java -Xmx2g -jar target/rdf-spine-0.1.0-jar-with-dependencies.jar -baseline  -file dbpedia-musicalWork/musicalWork.nt.gz -context http://dbpedia.org/ontology/MusicalWork
```

#### Parallel implementation

In Spark 1.5.1

```bash
$ spark-1.5.1-bin-hadoop2.6/bin/spark-submit --driver-memory 6g --executor-memory 3g --class "rdf.spine.discovery.DiscoveryCard" --master local[2] rdf-spine-0.1.0-jar-with-dependencies.jar [NUM_PARTITIONS] [RDF_FILE] [RDF_CLASS]
```

where [NUM_PARTITIONS] is the number of partitions used in the data, [RDF_FILE] is the RDF file to analyse, and [RDFS/OWL_CLASS] is the URI of the class used as context.

For example,

```bash
$ spark-1.5.1-bin-hadoop2.6/bin/spark-submit --driver-memory 6g --executor-memory 3g --class "rdf.spine.discovery.DiscoveryCard" --master local[2] rdf-spine-0.1.0-jar-with-dependencies.jar 2 dbpedia-musicalWork/musicalWork.nt http://dbpedia.org/ontology/MusicalWork
```

## Example output

```bash
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

According to the hardware settings, i.e., single or multiple machines, it may be required to set the memory allocated to the driver and executor. In order to  manage RDF files of up to 2.6G we used the following values:

```bash
--driver-memory 6g
--executor-memory 3g
```

## Contact

<i class="fa fa-envelope"></i> [Emir Munoz](http://emunoz.org)

> Last update: 30 May 2016