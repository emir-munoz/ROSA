package org.rosa.discovery;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.rosa.ml.numeric.DetectionType;
import org.rosa.ml.numeric.NumericOutlierDetector;
import org.rosa.model.CardCandidate;
import org.rosa.model.DataSource;
import org.rosa.model.OutlierResult;
import org.rosa.rdf.RDFUtil;
import org.rosa.util.MemoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Baseline implementation using SPARQL queries.
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 28/04/2016
 */
public class DiscoveryCardSparql {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(DiscoveryCardSparql.class.getSimpleName());
    /** blacklisted predicates */
    private final List<String> blacklist = Lists.newArrayList("http://www.w3.org/2002/07/owl#sameAs");

    /**
     * Main method.
     */
    public static void main(String... args) {
        DiscoveryCardSparql card = new DiscoveryCardSparql();
        try {
            card.runDiscovery(DataSource.FILE, "/media/sf_projects/rosa-ldd/ShEx/" +
                            "shexcala-0.5.8/experiments/dbpedia-personFunction/personFunction.nt.gz",
                    "http://dbpedia.org/ontology/PersonFunction",
                    DetectionType.ESD, 3);
        } catch (FileNotFoundException e) {
            _log.error(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Execute discovery method.
     *
     * @param source        Data source (e.g., FILE, SPARQL)
     * @param uri           URI to source, file or SPARQL endpoint.
     * @param constContext  Context of the constraint (i.e., a class)
     * @param outlierMethod Outlier detection method.
     * @param tValue        Deviation factor.
     * @throws FileNotFoundException
     */
    public void runDiscovery(final DataSource source, final String uri, final String constContext,
                             final DetectionType outlierMethod, final double tValue) throws FileNotFoundException {
        _log.info("Starting discovery of cardinality constraints from RDF data");
        if (!constContext.isEmpty()) {
            _log.info("Context is limited to class '{}'", constContext);
        } else {
            _log.info("Context is not specified");
        }

        Repository repository;
        switch (source) {
            case FILE:
                repository = RDFUtil.connectToMemoryRepository();
                // Loading statements from a file
                File file = new File(uri);
                if (!file.exists()) {
                    throw new FileNotFoundException("RDF file not found");
                }
                String baseURI = "http://example.org/example/local";
                MemoryUtils.printMemoryInfo();
                RDFUtil.loadRDFFromFile(file, RDFFormat.NTRIPLES, baseURI, repository);
                MemoryUtils.printMemoryInfo();
                break;
            case SPARQL:
                repository = RDFUtil.connectToSPARQLRepository(uri);
                break;
            default:
                throw new IllegalArgumentException("Input data source is not known");
        }

        // start counting execution time
        Stopwatch stopwatch = Stopwatch.createStarted();
        int nbSubjects = getNbSubjects(repository, constContext);
        _log.info("{} different subjects found in dataset", nbSubjects);
        List<String> predicateList = getListOfPredicates(repository, constContext);
        _log.info("{} predicates found in dataset", predicateList.size());

        // direct extraction of cardinalities
        //        List<CardCandidate> cardCandidatesBaseline = getCardinalityCandidates(repository, constContext,
        //                nbSubjects, predicateList);
        //        cardCandidatesBaseline.forEach(card -> _log.info("{}", card));

        MemoryUtils.printMemoryInfo();
        stopwatch.stop();
        _log.info("Elapsed time={}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        // extraction of cardinalities with outlier detection
        //        // MemoryUtils.printMemoryInfo();
        //        List<Integer> lowerBounds = getLowerBounds(repository, constContext, predicateList);
        //        // MemoryUtils.printMemoryInfo();
        //        List<Integer> upperBounds = getUpperBounds(repository, constContext, predicateList);

        stopwatch.reset();
        stopwatch.start();

        List<CardCandidate> cardCandidatesRosa = Lists.newArrayList();
        OutlierResult outlierResults;
        Map<String, Double> cardinalityMap;
        CardCandidate candidate;
        for (String predicate : predicateList) {
            // omit cardinality for some blacklisted predicates
            if (blacklist.contains(predicate)) {
                continue;
            }
            cardinalityMap = getCardinalityCounts(repository, constContext, predicate);
            _log.info("{} cardinalities found for <{}>", cardinalityMap.size(), predicate);
            if (cardinalityMap.size() > 1) {
                switch (outlierMethod) {
                    case ESD:
                        outlierResults = NumericOutlierDetector.
                                ruleESD(Doubles.toArray(cardinalityMap.values()), tValue);
                        break;
                    case HAMPEL:
                        outlierResults = NumericOutlierDetector.
                                ruleHampel(Doubles.toArray(cardinalityMap.values()), tValue);
                        break;
                    case BOXPLOT:
                        outlierResults = NumericOutlierDetector.
                                ruleBoxplot(Doubles.toArray(cardinalityMap.values()), tValue);
                        break;
                    case ZSCORE:
                        outlierResults = NumericOutlierDetector.
                                ruleZScore(Doubles.toArray(cardinalityMap.values()));
                        break;
                    case ROBUSTZSCORE:
                        outlierResults = NumericOutlierDetector.
                                ruleRobusZScore(Doubles.toArray(cardinalityMap.values()));
                        break;
                    default:
                        throw new IllegalArgumentException("The outlier detection method indicated is not supported");
                }
            } else {
                // _log.warn("Only one value found for predicate");
                outlierResults = new OutlierResult();
                outlierResults.setNonOutliers(Lists.newArrayList(cardinalityMap.values()));
            }
            Collections.sort(outlierResults.getNonOutliers());
            candidate = new CardCandidate(outlierResults.getNonOutliers().get(0).intValue(),
                    outlierResults.getNonOutliers().get(outlierResults.getNonOutliers().size() - 1).intValue(),
                    constContext, Lists.newArrayList(predicate));
            // check whether the lower bound should be removed
            if (cardinalityMap.keySet().size() < nbSubjects) {
                candidate.setMinBound(0);
            }
            cardCandidatesRosa.add(candidate);

            _log.info("predicate={} has {} outliers", predicate, outlierResults.getOutlierIndices().size());
            for (int index : outlierResults.getOutlierIndices()) {
                _log.info(Iterables.get(cardinalityMap.keySet(), index));
            }
        }
        cardCandidatesRosa.forEach(card -> _log.info("{}", card));

        MemoryUtils.printMemoryInfo();
        stopwatch.stop();
        _log.info("Elapsed time={}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        // close repository
        repository.shutDown();
    }

    /**
     * Get the number of distinct subjects in the KG in a given context.
     * <p/>
     * <pre>
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT (COUNT(DISTINCT ?sub) AS ?nbSub) WHERE { ?sub rdf:type <%s> . }
     * </pre>
     *
     * @param repository   Sesame repository.
     * @param constContext RDFS/OWL class or empty for unqualified.
     * @return Number of distinct subjects in KG.
     */
    private int getNbSubjects(final Repository repository, final String constContext) {
        int nbSubjects = 0;
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get total number of subjects ...");
            //            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            //                    "SELECT (COUNT(DISTINCT ?sub) AS ?nbSub) WHERE { ?sub rdf:type <%s> . }";
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "\n" +
                    "SELECT (COUNT(DISTINCT ?first) AS ?nbSubj)\n" +
                    "WHERE {\n" +
                    "  ?subj rdf:type <%s> .\n" +
                    "  {\n" +
                    "    SELECT ?subj ?first\n" +
                    "    WHERE {\n" +
                    "      ?subj ((owl:sameAs|^owl:sameAs)*) ?first .\n" +
                    "      OPTIONAL {\n" +
                    "        ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first .\n" +
                    "        FILTER (STR(?notfirst) < STR(?first))\n" +
                    "      }\n" +
                    "      FILTER(!BOUND(?notfirst))\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            queryString = String.format(queryString, constContext);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value nbSubVal;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    nbSubVal = bindingSet.getValue("nbSubj");
                    nbSubjects = Integer.valueOf(nbSubVal.stringValue());
                }
            }
        }

        return nbSubjects;
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT DISTINCT ?pred WHERE { ?s rdf:type <%s>; ?pred ?o . }
     *
     * @param repository   Sesame repository.
     * @param constContext RDFS/OWL class or empty for unqualified.
     * @return List of predicates in repository.
     */
    private List<String> getListOfPredicates(final Repository repository, final String constContext) {
        List<String> predicateList = Lists.newArrayList();
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get list of predicates ...");
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "SELECT DISTINCT ?pred WHERE { ?s rdf:type <%s>; ?pred ?obj . }";
            queryString = String.format(queryString, constContext);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value predVal;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    predVal = bindingSet.getValue("pred");
                    predicateList.add(predVal.stringValue());
                }
            }
        }

        return predicateList;
    }

    /**
     * Get the cardinality constraint candidates for all predicates the KG.
     *
     * @param repository    Sesame repository.
     * @param constContext  RDFS/OWL class or empty for unqualified.
     * @param nbSubjects    Number of different resources of a given type in the KG.
     * @param predicateList List of predicates.
     */
    private List<CardCandidate> getCardinalityCandidates(final Repository repository, final String constContext,
                                                         final int nbSubjects, final List<String> predicateList) {
        List<CardCandidate> cardCandidates = Lists.newArrayList();
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get cardinality of predicates ...");
            CardCandidate cardCandidate;
            for (String predicate : predicateList) {
                // String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                // "SELECT (COUNT(?count) as ?nb) (MIN(?count) as ?min) (MAX(?count) as ?max)\n" +
                // "WHERE {\n" +
                // "  SELECT (COUNT(DISTINCT ?obj) as ?count)\n" +
                // "  WHERE {\n";
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "\n" +
                        "SELECT (COUNT(*) AS ?nbTimes) (MIN(?nbValues) AS ?minValue) (MAX(?nbValues) AS ?maxValue)\n" +
                        "WHERE {\n" +
                        "  SELECT (COUNT(DISTINCT ?first_obj) AS ?nbValues)\n" +
                        "  WHERE {\n" +
                        "    {\n" +
                        "      SELECT DISTINCT ?first_subj ?first_obj\n" +
                        "      WHERE {\n" +
                        "        ?subj <%s> ?obj .\n" +
                        "        {\n" +
                        "          SELECT ?subj ?first_subj\n" +
                        "          WHERE {\n" +
                        "            ?subj a <%s> .\n" +
                        "            ?subj ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                        "            OPTIONAL {\n" +
                        "              ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                        "              FILTER (STR(?notfirst) < STR(?first_subj))\n" +
                        "            }\n" +
                        "            FILTER(!BOUND(?notfirst))\n" +
                        "          }\n" +
                        "        }\n" +
                        "        {\n" +
                        "          SELECT ?obj ?first_obj\n" +
                        "          WHERE {\n" +
                        "            ?obj ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                        "            OPTIONAL {\n" +
                        "              ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                        "              FILTER (STR(?notfirst) < STR(?first_obj))\n" +
                        "            }\n" +
                        "            FILTER(!BOUND(?notfirst))\n" +
                        "          }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  } GROUP BY ?first_subj\n" +
                        "}";
                queryString = String.format(queryString, predicate, constContext);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    if (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        Value nbVal = bindingSet.getValue("nbTimes");
                        Value minVal = bindingSet.getValue("minValue");
                        Value maxVal = bindingSet.getValue("maxValue");

                        cardCandidate = new CardCandidate(
                                Integer.valueOf(minVal.stringValue()),
                                Integer.valueOf(maxVal.stringValue()),
                                constContext,
                                Lists.newArrayList(predicate));

                        // check whether the lower bound should be removed
                        int nbTimes = Integer.valueOf(nbVal.stringValue());
                        if (nbTimes < nbSubjects) {
                            cardCandidate.setMinBound(0);
                        }

                        cardCandidates.add(cardCandidate);
                    }
                }
            }
        }

        return cardCandidates;
    }

    /**
     * Get the counted cardinalities for a context predicate in the KG.
     *
     * @param repository   Sesame repository.
     * @param constContext RDFS/OWL class or empty for unqualified.
     * @param predicate    Fixed predicate.
     * @return Lower bounds for predicates.
     */
    private Map<String, Double> getCardinalityCounts(final Repository repository,
                                                     final String constContext,
                                                     final String predicate) {
        // List<Integer> cardinaltities = Lists.newArrayList();
        Map<String, Double> cardinalityMap = Maps.newHashMap();
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get cardinality counts of predicate <{}>", predicate);
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "\n" +
                    "SELECT ?first_subj (COUNT(DISTINCT ?first_obj) AS ?nbValues)\n" +
                    "WHERE {\n" +
                    "  {\n" +
                    "    SELECT DISTINCT ?first_subj ?first_obj\n" +
                    "\tWHERE {\n" +
                    "      ?subj <%s> ?obj .\n" +
                    "      {\n" +
                    "        SELECT ?subj ?first_subj\n" +
                    "        WHERE {\n" +
                    "          ?subj a <%s> .\n" +
                    "          ?subj ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                    "          OPTIONAL {\n" +
                    "            ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                    "            FILTER (STR(?notfirst) < STR(?first_subj))\n" +
                    "          }\n" +
                    "          FILTER(!BOUND(?notfirst))\n" +
                    "        }\n" +
                    "      }\n" +
                    "      {\n" +
                    "        SELECT ?obj ?first_obj\n" +
                    "        WHERE {\n" +
                    "          ?obj ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                    "          OPTIONAL {\n" +
                    "            ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                    "            FILTER (STR(?notfirst) < STR(?first_obj))\n" +
                    "          }\n" +
                    "          FILTER(!BOUND(?notfirst))\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "} GROUP BY ?first_subj";
            queryString = String.format(queryString, predicate, constContext);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value cardVal;
                Value subjectVal;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    subjectVal = bindingSet.getValue("first_subj");
                    cardVal = bindingSet.getValue("nbValues");
                    // cardinaltities.add(Integer.valueOf(cardVal.stringValue()));
                    cardinalityMap.put(subjectVal.stringValue(), Double.valueOf(cardVal.stringValue()));
                }
            }
        }

        return cardinalityMap;
    }

    /**
     * Get the lower bound for each context predicate in the KG.
     *
     * @param repository    Sesame repository.
     * @param constContext  RDFS/OWL class or empty for unqualified.
     * @param predicateList List of predicate IRIs.
     * @return Lower bounds for predicates.
     */
    private List<Integer> getLowerBounds(final Repository repository, final String constContext,
                                         final List<String> predicateList) {
        List<Integer> lowerBounds = Lists.newArrayList();
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get lower bound cardinality of predicates ...");
            for (String predicate : predicateList) {
                //                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                //                        "  SELECT (COUNT(DISTINCT ?obj) as ?min)\n" +
                //                        "  WHERE {\n" +
                //                        "    ?sub rdf:type <%s>;\n" +
                //                        "      <%s> ?obj .\n" +
                //                        "  } GROUP BY ?sub ORDER BY asc(?min) LIMIT 1";
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "\n" +
                        "SELECT (COUNT(DISTINCT ?first_obj) AS ?nbValues)\n" +
                        "WHERE {\n" +
                        "  {\n" +
                        "    SELECT DISTINCT ?first_subj ?first_obj\n" +
                        "\tWHERE {\n" +
                        "      ?subj <%s> ?obj .\n" +
                        "      {\n" +
                        "        SELECT ?subj ?first_subj\n" +
                        "        WHERE {\n" +
                        "          ?subj a <%s> .\n" +
                        "          ?subj ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                        "          OPTIONAL {\n" +
                        "            ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                        "            FILTER (STR(?notfirst) < STR(?first_subj))\n" +
                        "          }\n" +
                        "          FILTER(!BOUND(?notfirst))\n" +
                        "        }\n" +
                        "      }\n" +
                        "      {\n" +
                        "        SELECT ?obj ?first_obj\n" +
                        "        WHERE {\n" +
                        "          ?obj ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                        "          OPTIONAL {\n" +
                        "            ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                        "            FILTER (STR(?notfirst) < STR(?first_obj))\n" +
                        "          }\n" +
                        "          FILTER(!BOUND(?notfirst))\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "} GROUP BY ?first_subj";
                // \n" +
                // "ORDER BY ASC(?nbValues)\n" +
                // "LIMIT 1";
                queryString = String.format(queryString, predicate, constContext);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    BindingSet bindingSet;
                    Value minVal;
                    // int lowerBound;
                    while (result.hasNext()) {
                        bindingSet = result.next();
                        minVal = bindingSet.getValue("nbValues");
                        lowerBounds.add(Integer.valueOf(minVal.stringValue()));
                        // minBound = lowerBound;
                        // if (minBound == totalNumSubjects) {
                        // minBound = lowerBound;
                        // } else {
                        // minBound = 0;
                        // }
                        // _log.info("min={} for predicate={}", minBound, predicate);
                    }
                }
            }
        }

        return lowerBounds;
    }

    /**
     * Get the upper bound for each context predicate in the KG.
     *
     * @param repository    Sesame repository.
     * @param constContext  RDFS/OWL class or empty for unqualified.
     * @param predicateList List of predicate IRIs.
     * @return Upper bounds for predicates.
     */
    private List<Integer> getUpperBounds(final Repository repository, final String constContext,
                                         final List<String> predicateList) {
        List<Integer> upperBounds = Lists.newArrayList();
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get upper bound cardinality of predicates ...");
            for (String predicate : predicateList) {
                //                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                //                        "  SELECT (COUNT(DISTINCT ?obj) as ?max)\n" +
                //                        "  WHERE {\n" +
                //                        "    ?sub rdf:type <%s>;\n" +
                //                        "      <%s> ?obj .\n" +
                //                        "  } GROUP BY ?sub ORDER BY desc(?max) LIMIT 1";
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "\n" +
                        "SELECT (COUNT(DISTINCT ?first_obj) AS ?nbValues)\n" +
                        "WHERE {\n" +
                        "  {\n" +
                        "    SELECT DISTINCT ?first_subj ?first_obj\n" +
                        "\tWHERE {\n" +
                        "      ?subj <%s> ?obj .\n" +
                        "      {\n" +
                        "        SELECT ?subj ?first_subj\n" +
                        "        WHERE {\n" +
                        "          ?subj a <%s> .\n" +
                        "          ?subj ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                        "          OPTIONAL {\n" +
                        "            ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_subj .\n" +
                        "            FILTER (STR(?notfirst) < STR(?first_subj))\n" +
                        "          }\n" +
                        "          FILTER(!BOUND(?notfirst))\n" +
                        "        }\n" +
                        "      }\n" +
                        "      {\n" +
                        "        SELECT ?obj ?first_obj\n" +
                        "        WHERE {\n" +
                        "          ?obj ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                        "          OPTIONAL {\n" +
                        "            ?notfirst ((owl:sameAs|^owl:sameAs)*) ?first_obj .\n" +
                        "            FILTER (STR(?notfirst) < STR(?first_obj))\n" +
                        "          }\n" +
                        "          FILTER(!BOUND(?notfirst))\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "} GROUP BY ?first_subj";
                //                        \n" +
                //                        "ORDER BY DESC(?nbValues)\n" +
                //                        "LIMIT 1";
                queryString = String.format(queryString, predicate, constContext);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    BindingSet bindingSet;
                    Value maxVal;
                    while (result.hasNext()) {
                        bindingSet = result.next();
                        maxVal = bindingSet.getValue("nbValues");
                        upperBounds.add(Integer.valueOf(maxVal.stringValue()));

                        // _log.info("max={} for predicate={}", maxBound, predicate);
                    }
                }
            }
        }

        return upperBounds;
    }

}
