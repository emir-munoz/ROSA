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
import rdf.spine.util.TimeWatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Baseline implementation using SPARQL queries.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 28/04/16.
 */
public class DiscoveryCardSparql {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(DiscoveryCardSparql.class.getSimpleName());

    /**
     * Main method.
     */
    public static void main(String[] args) {
        DiscoveryCardSparql card = new DiscoveryCardSparql();
        try {
            card.runDiscovery("/media/sf_projects/spine-ldd/ShEx/shexcala-0.5.8/experiments/dbpedia-personFunction/personFunction.nt.gz",
                    "http://dbpedia.org/ontology/PersonFunction");
        } catch (FileNotFoundException e) {
            _log.error(e.getMessage());
        }
    }

    /**
     * Execute discovery method.
     */
    public void runDiscovery(final String filename, final String constContext) throws FileNotFoundException {
        _log.info("Starting discovery of cardinality constraints from RDF data");
        if (!constContext.isEmpty()) {
            _log.info("Context is limited to class '{}'", constContext);
        } else {
            _log.info("Context is not specified");
        }

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        // Loading statements from a file
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("RDF file not found");
        }
        String baseURI = "http://example.org/example/local";

        MemoryUtils.printMemoryInfo();
        loadRDFFromFile(file, repository, baseURI, RDFFormat.NTRIPLES);
        MemoryUtils.printMemoryInfo();

        // start counting execution time
        TimeWatch time = TimeWatch.start();

        int totalNumSubjects = getTotalNumSubjects(repository, constContext);
        List<String> predicateList = getListOfPredicates(repository, constContext);
        // MemoryUtils.printMemoryInfo();
        getLowerBoundPredicates(repository, constContext, totalNumSubjects, predicateList);
        // MemoryUtils.printMemoryInfo();
        getUpperBoundPredicates(repository, constContext, predicateList);
        MemoryUtils.printMemoryInfo();

        _log.info("Elapsed time={}ms and {}s", time.time(), time.time(TimeUnit.SECONDS));
        repository.shutDown();
    }

    /**
     * Load test triples into repository.
     *
     * @param repository Sesame repository.
     */
    private void loadRDFTest(final Repository repository) {
        ValueFactory factory = repository.getValueFactory();

        IRI bob = factory.createIRI("http://example.org/bob");
        IRI name = factory.createIRI("http://example.org/name");
        Literal bobsName = factory.createLiteral("Bob");
        Statement nameStatement = factory.createStatement(bob, name, bobsName);

        // Loading statements programmatically
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.begin();
            conn.add(nameStatement);
            conn.commit();
        }
    }

    /**
     * Load an RDF file into the repository.
     *
     * @param file       RDF file descriptor.
     * @param repository Sesame repository.
     * @param baseURI    Base URI.
     * @param format     RDF format.
     */
    private void loadRDFFromFile(final File file, final Repository repository, final String baseURI, RDFFormat format) {
        _log.info("Processing dataset file '{}'", file.getPath());
        _log.info("Loading RDF statements in memory ...");
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(file, baseURI, format);
            conn.commit();
        } catch (OpenRDFException e) {
            // handle Sesame exception. This catch-clause is
            // optional since OpenRDFException is an unchecked exception
        } catch (IOException e) {
            e.printStackTrace();
        }
        _log.info("RDF statements loaded in memory");
    }

    /**
     * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     * SELECT (COUNT(DISTINCT ?sub) AS ?nbSub) WHERE { ?sub rdf:type <%s> . }
     *
     * @param repository   Sesame repository.
     * @param constContext RDFS/OWL class or empty for unqualified.
     * @return Total number of distinct subjects in repository.
     */
    private int getTotalNumSubjects(final Repository repository, final String constContext) {
        int totalNumSubjects = 0;
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get total number of subjects ...");
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                         "SELECT (COUNT(DISTINCT ?sub) AS ?nbSub) WHERE { ?sub rdf:type <%s> . }";
            queryString = String.format(queryString, constContext);
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
     * @param repository   Sesame repository.
     * @param constContext RDFS/OWL class or empty for unqualified.
     * @return List of predicates in repository.
     */
    private List<String> getListOfPredicates(final Repository repository, final String constContext) {
        List<String> predicateList = Lists.newArrayList();
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get list of predicates ...");
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                         "SELECT DISTINCT ?pred WHERE { ?s rdf:type <%s>; ?pred ?o . }";
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
     * @param repository   Sesame repository.
     * @param constContext RDFS/OWL class or empty for unqualified.
     */
    private void getBoundariesForPredicate(final Repository repository, final String constContext, final List<String> predicateList) {
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get cardinality of predicates ...");
            for (String predicate : predicateList) {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                             "SELECT (COUNT(?count) as ?nb) (MIN(?count) as ?min) (MAX(?count) as ?max)\n" +
                                             "WHERE {\n" +
                                             "  SELECT (COUNT(DISTINCT ?obj) as ?count)\n" +
                                             "  WHERE {\n";
                if (!constContext.isEmpty()) {
                    queryString += "    ?sub rdf:type <%s> .\n";
                    queryString = String.format(queryString, constContext);
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
     * @param repository       Sesame repository.
     * @param constContext     RDFS/OWL class or empty for unqualified.
     * @param totalNumSubjects Number of subjects in context.
     * @param predicateList    List of predicate IRIs.
     * @return Lower bounds for predicates.
     */
    private int getLowerBoundPredicates(final Repository repository, final String constContext, final int totalNumSubjects, final
    List<String> predicateList) {
        int minBound = 0;
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get upper bound for cardinality of predicates ...");
            for (String predicate : predicateList) {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                             "  SELECT (COUNT(DISTINCT ?obj) as ?min)\n" +
                                             "  WHERE {\n" +
                                             "    ?sub rdf:type <%s>;\n" +
                                             "      <%s> ?obj .\n" +
                                             "  } GROUP BY ?sub ORDER BY asc(?min) LIMIT 1";
                queryString = String.format(queryString, constContext, predicate);
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

                        _log.info("min={} for predicate={}", minBound, predicate);
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
     * @param repository    Sesame repository.
     * @param constContext  RDFS/OWL class or empty for unqualified.
     * @param predicateList List of predicate IRIs.
     * @return Upper bounds for predicates.
     */
    private int getUpperBoundPredicates(final Repository repository, final String constContext, final List<String> predicateList) {
        int maxBound = 0;
        try (RepositoryConnection conn = repository.getConnection()) {
            _log.info("Querying dataset to get lower bound for cardinality of predicates ...");
            for (String predicate : predicateList) {
                String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                             "  SELECT (COUNT(DISTINCT ?obj) as ?max)\n" +
                                             "  WHERE {\n" +
                                             "    ?sub rdf:type <%s>;\n" +
                                             "      <%s> ?obj .\n" +
                                             "  } GROUP BY ?sub ORDER BY desc(?max) LIMIT 1";
                queryString = String.format(queryString, constContext, predicate);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    BindingSet bindingSet;
                    Value maxVal;
                    if (result.hasNext()) {
                        bindingSet = result.next();
                        maxVal = bindingSet.getValue("max");
                        maxBound = Integer.valueOf(maxVal.stringValue());

                        _log.info("max={} for predicate={}", maxBound, predicate);
                    }
                }
            }
        }

        return maxBound;
    }

}
