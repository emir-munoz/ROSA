package rdf.spine.dao;

import com.google.common.collect.Lists;
import org.openrdf.OpenRDFException;
import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rdf.spine.util.MemoryUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Baseline implementation using SPARQL queries.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 28/04/16.
 */
public class DiscoveryCardJava {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(DiscoveryCardJava.class.getSimpleName());

    /**
     * Main method.
     */
    public static void main(String[] args) {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();

        // Loading statements from a file
        File file = new File("/media/sf_projects/spine-ldd/ShEx/shexcala-0.5.8/experiments/" +
                                     "dbpedia-personFunction/personFunction.nt.gz");
        String classString = "http://dbpedia.org/ontology/PersonFunction";
        String baseURI = "http://example.org/example/local";

        DiscoveryCardJava card = new DiscoveryCardJava();
        MemoryUtils.printMemoryInfo();
        card.loadRDFFromFile(file, rep, baseURI, RDFFormat.NTRIPLES);
        MemoryUtils.printMemoryInfo();
        int totalNumSubjects = card.getTotalNumSubjects(rep, classString);
        List<String> predicateList = card.getListOfPredicates(rep, classString);
        MemoryUtils.printMemoryInfo();
        card.getLowerBoundPredicates(rep, classString, totalNumSubjects, predicateList);
        MemoryUtils.printMemoryInfo();
        card.getUpperBoundPredicates(rep, classString, predicateList);
        MemoryUtils.printMemoryInfo();

        rep.shutDown();
    }

    /**
     * Load test triples into repository.
     *
     * @param rep Sesame repository.
     */
    private void loadRDFTest(final Repository rep) {
        ValueFactory factory = rep.getValueFactory();

        IRI bob = factory.createIRI("http://example.org/bob");
        IRI name = factory.createIRI("http://example.org/name");
        Literal bobsName = factory.createLiteral("Bob");
        Statement nameStatement = factory.createStatement(bob, name, bobsName);

        // Loading statements programmatically
        try (RepositoryConnection conn = rep.getConnection()) {
            conn.begin();
            conn.add(nameStatement);
            conn.commit();
        }
    }

    /**
     * Load an RDF file into the repository.
     *
     * @param file    RDF file descriptor.
     * @param rep     Sesame repository.
     * @param baseURI Base URI.
     * @param format  RDF format.
     */
    private void loadRDFFromFile(final File file, final Repository rep, final String baseURI, RDFFormat format) {
        try (RepositoryConnection conn = rep.getConnection()) {
            _log.info("Loading RDF statements in memory ...");
            conn.add(file, baseURI, format); // RDFFormat.NTRIPLES
            _log.info("RDF statements loaded in memory");
            conn.commit();
        } catch (OpenRDFException e) {
            // handle Sesame exception. This catch-clause is
            // optional since OpenRDFException is an unchecked exception
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT (COUNT(DISTINCT ?sub) AS ?nbSub) WHERE { ?sub rdf:type <%s> . }
     *
     * @param rep     Sesame repository.
     * @param context RDFS/OWL class or empty for unqualified.
     * @return Total number of distinct subjects in rep.
     */
    private int getTotalNumSubjects(final Repository rep, final String context) {
        int totalNumSubjects = 0;
        try (RepositoryConnection conn = rep.getConnection()) {
            _log.info("Querying dataset to get total number of subjects ...");
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                         "SELECT (COUNT(DISTINCT ?sub) AS ?nbSub) WHERE { ?sub rdf:type <%s> . }";
            queryString = String.format(queryString, context);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value nbSubVal;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    nbSubVal = bindingSet.getValue("nbSub");
                    totalNumSubjects = Integer.valueOf(nbSubVal.stringValue());
                }
            }
            _log.info("{} different subjects found in dataset", totalNumSubjects);
        }

        return totalNumSubjects;
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT DISTINCT ?pred WHERE { ?s rdf:type <%s>; ?pred ?o . }
     *
     * @param rep     Sesame repository.
     * @param context RDFS/OWL class or empty for unqualified.
     * @return List of predicates in rep.
     */
    private List<String> getListOfPredicates(final Repository rep, final String context) {
        List<String> predicateList = Lists.newArrayList();
        try (RepositoryConnection conn = rep.getConnection()) {
            _log.info("Querying dataset to get list of predicates ...");
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                         "SELECT DISTINCT ?pred WHERE { ?s rdf:type <%s>; ?pred ?o . }";
            queryString = String.format(queryString, context);
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
            _log.info("{} predicates found in dataset", predicateList.size());
        }

        return predicateList;
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     *
     * SELECT (COUNT(?count) as ?nb) (MIN(?count) as ?min) (MAX(?count) as ?max)
     * WHERE {
     * SELECT (COUNT(DISTINCT ?pred) as ?count)
     * WHERE {
     * ?sub rdf:type %s .
     * ?sub ?pred ?obj .
     * } GROUP BY ?sub
     * }
     *
     * @param rep     Sesame repository.
     * @param context RDFS/OWL class or empty for unqualified.
     */
    private void getBoundariesForPredicate(final Repository rep, final String context, final List<String> predicateList) {
        try (RepositoryConnection conn = rep.getConnection()) {
            _log.info("Querying dataset to get cardinality of predicates ...");
            for (String predicate : predicateList) {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                             "SELECT (COUNT(?count) as ?nb) (MIN(?count) as ?min) (MAX(?count) as ?max)\n" +
                                             "WHERE {\n" +
                                             "  SELECT (COUNT(DISTINCT ?obj) as ?count)\n" +
                                             "  WHERE {\n";
                if (!context.isEmpty()) {
                    queryString += "    ?sub rdf:type <%s> .\n";
                    queryString = String.format(queryString, context);
                }
                queryString += "    ?sub <%s> ?obj .\n" +
                                       "  } GROUP BY ?sub\n" +
                                       "}";
                queryString = String.format(queryString, predicate);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        Value nbVal = bindingSet.getValue("nb");
                        Value minVal = bindingSet.getValue("min");
                        Value maxVal = bindingSet.getValue("max");

                        _log.info("predicate={} #objects={} min={} max={}", predicate, nbVal.stringValue(), minVal.stringValue
                                                                                                                           (), maxVal.stringValue());
                    }
                }
            }
        }
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT (COUNT(DISTINCT ?obj) as ?min)
     * WHERE {
     * ?sub rdf:type <%s>;
     * <%s> ?obj .
     * } GROUP BY ?sub ORDER BY asc(?count) LIMIT 1
     *
     * @param rep              Sesame repository.
     * @param context          RDFS/OWL class or empty for unqualified.
     * @param totalNumSubjects Number of subjects in context.
     * @param predicateList    List of predicate IRIs.
     * @return Lower bounds for predicates.
     */
    private int getLowerBoundPredicates(final Repository rep, final String context, final int totalNumSubjects, final List<String>
                                                                                                                        predicateList) {
        int minBound = 0;
        try (RepositoryConnection conn = rep.getConnection()) {
            _log.info("Querying dataset to get upper bound for cardinality of predicates ...");
            for (String predicate : predicateList) {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                             "  SELECT (COUNT(DISTINCT ?obj) as ?min)\n" +
                                             "  WHERE {\n" +
                                             "    ?sub rdf:type <%s>;\n" +
                                             "      <%s> ?obj .\n" +
                                             "  } GROUP BY ?sub ORDER BY asc(?min) LIMIT 1";
                queryString = String.format(queryString, context, predicate);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    BindingSet bindingSet;
                    Value minVal;
                    int lowerBound;
                    if (result.hasNext()) {
                        bindingSet = result.next();
                        minVal = bindingSet.getValue("min");
                        lowerBound = Integer.valueOf(minVal.stringValue());
                        if (minBound == totalNumSubjects) {
                            minBound = lowerBound;
                        } else {
                            minBound = 0;
                        }

                        _log.info("min={} for predicate={}", predicate, minBound);
                    }
                }
            }
        }

        return minBound;
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT (COUNT(DISTINCT ?obj) as ?max)
     * WHERE {
     * ?sub rdf:type <%s>;
     * <%s> ?obj .
     * } GROUP BY ?sub ORDER BY desc(?count) LIMIT 1
     *
     * @param rep           Sesame repository.
     * @param classType     RDFS/OWL class or empty for unqualified.
     * @param predicateList List of predicate IRIs.
     * @return Upper bounds for predicates.
     */
    private int getUpperBoundPredicates(final Repository rep, final String classType, final List<String> predicateList) {
        int maxBound = 0;
        try (RepositoryConnection conn = rep.getConnection()) {
            _log.info("Querying dataset to get lower bound for cardinality of predicates ...");
            for (String predicate : predicateList) {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                             "  SELECT (COUNT(DISTINCT ?obj) as ?max)\n" +
                                             "  WHERE {\n" +
                                             "    ?sub rdf:type <%s>;\n" +
                                             "      <%s> ?obj .\n" +
                                             "  } GROUP BY ?sub ORDER BY desc(?max) LIMIT 1";
                queryString = String.format(queryString, classType, predicate);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    BindingSet bindingSet;
                    Value maxVal;
                    if (result.hasNext()) {
                        bindingSet = result.next();
                        maxVal = bindingSet.getValue("max");
                        maxBound = Integer.valueOf(maxVal.stringValue());

                        _log.info("max={} for predicate={}", predicate, maxBound);
                    }
                }
            }
        }

        return maxBound;
    }

}
