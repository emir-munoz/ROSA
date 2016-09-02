package org.rosa.walks;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import org.eclipse.rdf4j.repository.Repository;
import org.rosa.rdf.RdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Extraction of random walks from a SPARQL endpoint for a given set of entity URIs. The two types of walks are:
 * <p/>
 * - Random walks of type property -> resource -> property -> ... -> resource - Random walks of type property ->
 * class(resource) -> property -> ... -> class(resource)
 *
 * @author Emir Munoz
 * @version 0.0.3
 * @since 29/05/2016
 */
public class RandomWalks {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(RandomWalks.class.getSimpleName());
    private String entitiesFile;
    private int threads;
    private int depth;
    private int walks;
    private String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n";

    /**
     * Main method.
     */
    public static void main(String... args) {
        RandomWalks rwalks = new RandomWalks();
        rwalks.init(args);
    }

    /**
     * Initialization.
     */
    private void init(String... args) {
        entitiesFile = args[0];
        threads = Integer.valueOf(args[1]);
        depth = Integer.valueOf(args[2]);
        walks = Integer.valueOf(args[3]);

        List<String> entities = Lists.newArrayList();
        try {
            CSVReader reader = new CSVReader(new FileReader(this.entitiesFile));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                entities.add(nextLine[0]);
            }
        } catch (FileNotFoundException e) {
            _log.error("Cannot find input file '{}'. Closing application.", this.entitiesFile);
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            _log.error("Error while reading input file '{}'. Closing application.", this.entitiesFile);
            e.printStackTrace();
            System.exit(0);
        }

        Repository repository1 = RdfUtils.connectToSparqlRepository("http://140.203.155.53:3030/bio2rdf-0.2/sparql");
        this.getRandomWalksOfDepth(entities, depth, walks, repository1);
    }

    /**
     * Get property paths of a given depth.
     *
     * @param depth
     *         Length of the property path.
     * @param nbWalks
     *         Maximum number of walks to get.
     * @param repo
     *         Reference repository.
     */
    public void getRandomWalksOfDepth(final List<String> entities, final int depth, final int nbWalks,
                                      Repository repo) {
        _log.info("Querying dataset to get random walks of depth {}", depth);

        // start a pool of threads and assign an entity to each one
        // ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS,
        //  new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        WalksWriter.getInstance("walks-bio2rdf.txt.gz", "UTF-8", walks * 2);
        String queryString;
        for (String entity : entities) {
            queryString = generateRandomWalksQuery(entity, depth, nbWalks);
            WalkQueryProcessor qp = new WalkQueryProcessor(depth, queryString, repo);
            threadPool.execute(qp);
        }
        // shutdown pool and await for the threads to finish
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            _log.error("Timeout: some threads took more than 10 days to finish");
            e.printStackTrace();
        } finally {
            if (!threadPool.isTerminated()) {
                _log.info("cancel non-finished tasks");
            } else {
                threadPool.shutdownNow();
                WalksWriter.closeWriter();
            }
        }
        _log.info("Finished all threads");
    }

    /**
     * Generate random walk queries for a given entity, considering predicates and entities, the depth of the walks, and
     * a maximum number of walks.
     *
     * @param rootEntity
     *         Root entity of the walks.
     * @param depth
     *         Depth of the walks.
     * @param nbWalks
     *         Maximum number of walks to retrieve. User 0 to get the direct property values.
     * @return A SPARQL query.
     */
    public String generateRandomWalksQuery(final String rootEntity, final int depth, final int nbWalks) {
        String queryString = "%s\n"
                + "SELECT DISTINCT %s\n"
                + "WHERE {\n"
                + "%s" // triplePatterns
                + "%s"; // filters
        if (nbWalks > 0) {
            queryString += "  BIND(RAND() AS ?sortKey)\n"
                    + "}\n"
                    + "ORDER BY ?sortKey\n"
                    + "LIMIT %s";
        } else {
            queryString += "}\n";
        }
        String variables = "";
        String triplePatterns = "";
        String filters = "";
        for (int i = 0; i < depth; i++) {
            variables += "?p" + i + " ?target" + i + " ";
            if (i == 0) {
                triplePatterns += "  <" + rootEntity + "> ?p" + i + " ?target" + i + " .\n";
                //                        + "    FILTER (?p" + i + " != rdf:type)\n";
            } else {
                triplePatterns += "  ?target" + (i - 1) + " ?p" + i + " ?target" + i + " .\n"
                        //                        + "  FILTER (?p" + i + " != rdf:type)\n";
                        + "  FILTER (?target" + (i - 1) + " != ?target" + i + ")\n";
            }
        }
        filters = "  FILTER (isLiteral(?target{0}) || isIRI(?target{0}))\n";
        filters = MessageFormat.format(filters, depth - 1);
        queryString = String.format(queryString, prefixes, variables, triplePatterns, filters, nbWalks);

        return queryString;
    }

    /**
     * Generate random walk queries for a given entity, considering predicates and the class of the object entities, the
     * depth of the walks, and a maximum number of walks.
     *
     * @param rootEntity
     *         Root entity of the walks.
     * @param depth
     *         Depth of the walks.
     * @param nbWalks
     *         Maximum number of walks to retrieve. User 0 to get the direct property values.
     * @return A SPARQL query.
     */
    public String generateClassPathQuery(final String rootEntity, final int depth, final int nbWalks) {
        String queryString = "%s"
                + "SELECT DISTINCT%s\n"
                + "WHERE {\n"
                + "%s" // triplePatterns
                + "%s"; // filters
        if (nbWalks > 0) {
            queryString += "  BIND(RAND() AS ?sortKey)\n"
                    + "}\n"
                    + "ORDER BY ?sortKey\n"
                    + "LIMIT %s";
        } else {
            queryString += "}\n";
        }
        String variables = "";
        String triplePatterns = "";
        String filters = "";
        for (int i = 0; i < depth; i++) {
            variables += " ?p" + i + " ?class" + i;
            if (i == 0) {
                triplePatterns += "  <" + rootEntity + "> ?p" + i + " ?target" + i + " .\n"
                        + "  OPTIONAL { ?target" + i + " rdf:type ?class" + i + " }\n";
                //                        + "    FILTER (?p" + i + " != rdf:type)\n";
            } else {
                triplePatterns += "  ?target" + (i - 1) + " ?p" + i + " ?target" + i + " .\n"
                        + "  OPTIONAL { ?target" + i + " rdf:type ?class" + i + " }\n"
                        //                        + "  FILTER (?p" + i + " != rdf:type)\n";
                        + "  FILTER (?target" + (i - 1) + " != ?target" + i + ")\n";
            }
        }
        // here we add a 1 to decide the value of the last class with COALESCE
        variables += "1 ?target" + (depth - 1) + " ";
        filters += "  FILTER (isLiteral(?target{0}) || isIRI(?target{0}))\n"
                + "  BIND(COALESCE(?class{0}, datatype(?target{0})) as ?class{0}1)\n";
        filters = MessageFormat.format(filters, depth - 1);
        queryString = String.format(queryString, prefixes, variables, triplePatterns, filters, nbWalks);

        return queryString;
    }

}
