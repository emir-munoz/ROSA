---
layout: index
---

# Welcome to ROSA

ROSA is an **R**DF and **O**ntology con**S**traints **A**nalyzer implemented in Java 8. ROSA aims to mine cardinality bounds from RDF knowledge graphs.

## Pre-requisites

- Java 8
- [Eclipse RDF4J](http://rdf4j.org/) version 2.0M3
- [Apache Jena Fuseki](http://jena.apache.org/) version 2

## ROSA source code management

If you are interested in extending or working directly with the code, you can also check out the master branch from Git.

```bash
git clone https://github.com/emir-munoz/ROSA.git
```

## Compiling from sources

To generate a fat `.jar` with all dependencies,

```bash
mvn clean package -DskipTests
```

## Running project

To execute ROSA on top of Spark using multiple threads in a local machine

**NOTE:** Before start the discovery process, please ensure you have your RDF knowledge graph file in disk or loaded in an Apache Jena  Fuseki 2 instance.

### Usage

```bash
[INFO ] 2016-07-18 10:54:56.832 [main] (RosaMain) - [ROSA - RDF and Ontology Constraints Analyser (2016)]
Usage: <main class> [options]
  Options:
    --card
       Extraction for cardinality constraints
       Default: false
    -c, --context
       Context of constraints (class or empty)
    -e, --endpoint
       SPARQL endpoint
    -f, --file
       Path to RDF file (*.nt, *.nq, *.gz)
    -h, --help
       Displays this nice help message
       Default: false
    -m, --method
       Outlier detection method
    -t, --tval
       Deviation factor
    -v, --version
       Version of the application
       Default: false
```

### Discovery of Cardinality Bounds

```bash
$ java -Xmx2g -jar target/ROSA-0.1.0-jar-with-dependencies.jar ` \
	--card  \
    -e [RDF_FILE] \
    -c [RDFS/OWL_CLASS] \
    -m [OUTLIER_METHOD] \
    -t [FACTOR]
```

where:

- `[RDF_FILE]` is the RDF file to analyse,
- `[RDFS/OWL_CLASS]` is the URI of a class used as context,
- `[OUTLIER_METHOD]` is the name of the outlier detection method to use, and
- `[FACTOR]` is a value required by the outlier detection methods.

For example,

```bash
$ java -Xmx2g -jar target/ROSA-0.1.0-jar-with-dependencies.jar --card  -e http://localhost:3030/oaei-restaurant1/sparql -c http://www.okkam.org/ontology_restaurant1.owl#Restaurant -m BOXPLOT -t 1.5
```

## Example output

```bash
[INFO ] 2016-07-16 23:32:34.269 [main] (RosaMain)           - [ROSA - RDF and Ontology Constraints Analyser (2016)]
[INFO ] 2016-07-16 23:32:34.294 [main] (DiscoveryCardSparql) - Starting discovery of cardinality constraints from RDF data
[INFO ] 2016-07-16 23:32:34.295 [main] (DiscoveryCardSparql) - Context is limited to class 'http://www.okkam.org/ontology_restaurant1.owl#Restaurant'
[INFO ] 2016-07-16 23:32:34.301 [main] (RDFUtil)            - Connecting to SPARQL endpoint http://data.neuralnoise.com:3030/oaei-restaurant1/sparql
[INFO ] 2016-07-16 23:32:35.627 [main] (DiscoveryCardSparql) - Querying dataset to get total number of subjects ...
[INFO ] 2016-07-16 23:32:36.303 [main] (DiscoveryCardSparql) - 113 different subjects found in dataset
[INFO ] 2016-07-16 23:32:36.304 [main] (DiscoveryCardSparql) - Querying dataset to get list of predicates ...
[INFO ] 2016-07-16 23:32:36.370 [main] (DiscoveryCardSparql) - 5 predicates found in dataset
[INFO ] 2016-07-16 23:32:36.499 [main] (MemoryUtils)        - Used memory: 7.3MB	Max available memory: 3,641.0MB
[INFO ] 2016-07-16 23:32:36.500 [main] (DiscoveryCardSparql) - Elapsed time=2113ms
[INFO ] 2016-07-16 23:32:36.500 [main] (DiscoveryCardSparql) - Querying dataset to get cardinality counts of predicate <http://www.okkam.org/ontology_restaurant1.owl#has_address>
[INFO ] 2016-07-16 23:32:36.683 [main] (DiscoveryCardSparql) - 113 cardinalities found for <http://www.okkam.org/ontology_restaurant1.owl#has_address>
[INFO ] 2016-07-16 23:32:36.693 [main] (NumericOutlierDetector) - Running BOXPLOT outlier detection
[WARN ] 2016-07-16 23:32:36.696 [main] (NumericOutlierDetector) - OutlierRes:{lowerBound=1.0, upperBound=1.0}
[INFO ] 2016-07-16 23:32:36.698 [main] (DiscoveryCardSparql) - predicate=http://www.okkam.org/ontology_restaurant1.owl#has_address has 0 outliers

...

[INFO ] 2016-07-16 23:32:37.345 [main] (DiscoveryCardSparql) - card({<http://www.okkam.org/ontology_restaurant1.owl#has_address>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)
[INFO ] 2016-07-16 23:32:37.349 [main] (DiscoveryCardSparql) - card({<http://www.okkam.org/ontology_restaurant1.owl#name>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)
[INFO ] 2016-07-16 23:32:37.350 [main] (DiscoveryCardSparql) - card({<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)
[INFO ] 2016-07-16 23:32:37.359 [main] (DiscoveryCardSparql) - card({<http://www.okkam.org/ontology_restaurant1.owl#phone_number>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)
[INFO ] 2016-07-16 23:32:37.405 [main] (DiscoveryCardSparql) - card({<http://www.okkam.org/ontology_restaurant1.owl#category>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)
[INFO ] 2016-07-16 23:32:37.429 [main] (MemoryUtils)        - Used memory: 4.8MB	Max available memory: 3,641.0MB
[INFO ] 2016-07-16 23:32:37.429 [main] (DiscoveryCardSparql) - Elapsed time=929ms
```

The application takes 929ms and outputs the following cardinality bounds:

- `card({<http://www.okkam.org/ontology_restaurant1.owl#has_address>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)`
- `card({<http://www.okkam.org/ontology_restaurant1.owl#name>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)`
- `card({<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)`
- `card({<http://www.okkam.org/ontology_restaurant1.owl#phone_number>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)`
- `card({<http://www.okkam.org/ontology_restaurant1.owl#category>},<http://www.okkam.org/ontology_restaurant1.owl#Restaurant>)=(1,1)`

In this specific case, no inconsistencies are found due to the synthetic nature of the dataset. Found inconsistencies in DBpedia dataset are discussed in the paper.

## Contact

<i class="fa fa-envelope"></i> [Emir Munoz](http://emunoz.org)

> July 18, 2016
