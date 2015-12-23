## Compile project

```
mvn package -DskipTests
```

## Run project

To execute the spark app in a local machine

In Spark 1.4
```
../../spark-1.4.0/bin/spark-submit --class "mllib.FPGrowthExample" --master local[3] target/rdf-spine-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

In Spark 1.5.1
```
../../spark-1.5.1-bin-hadoop2.6/bin/spark-submit --class "mllib.RDFFreqItemsets" --master local[3] target/rdf-spine-0.0.1-SNAPSHOT-jar-with-dependencies.jar 0.6 2 0.8
```

## Output

To check the logs in Spark 1.4
```
tail -f /home/emir/work/spark-1.4.0/sbin/../logs/spark-emir-org.apache.spark.deploy.master.Master-1-galway.out
```

To check the logs in Spark 1.5.1
```
tail -f /home/emir/work/spark-1.5.1-bin-hadoop2.6/sbin/../logs/spark-emir-org.apache.spark.deploy.master.Master-1-galway.out
```

## Options

```
--driver-memory 3g
--executor-memory 3g
```