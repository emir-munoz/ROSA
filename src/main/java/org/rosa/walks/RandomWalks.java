package org.rosa.walks;

import com.google.common.collect.Lists;
import org.eclipse.rdf4j.repository.Repository;
import org.rosa.rdf.RdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Extraction of random walks from a SPARQL endpoint for a given set of entity URIs.
 *
 * @author Emir Munoz
 * @version 0.0.3
 * @since 29/05/2016
 */
public class RandomWalks {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(RandomWalks.class.getSimpleName());
    private static int threads;
    private static int depth;
    private static int walks;

    /**
     * Main method.
     */
    public static void main(String... args) {
        RandomWalks rwalks = new RandomWalks();
        threads = Integer.valueOf(args[0]);
        depth = Integer.valueOf(args[1]);
        walks = Integer.valueOf(args[2]);
        rwalks.init();
    }

    /**
     * Initialization.
     */
    private void init() {
        Repository repository1 = RdfUtils.connectToSparqlRepository("http://140.203.155.53:3030/bio2rdf-0.2/sparql");
        this.getRandomWalksOfDepth(depth, walks, repository1);
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
    public void getRandomWalksOfDepth(final int depth, final int nbWalks, Repository repo) {
        _log.info("Querying dataset to get random walks of depth {}", depth);
        List<String> entities = Lists.newArrayList();
        entities.add("http://bio2rdf.org/drugbank:DB01600");
        entities.add("http://bio2rdf.org/drugbank:DB00208");
        entities.add("http://bio2rdf.org/drugbank:DB00753");
        entities.add("http://bio2rdf.org/drugbank:DB00632");
        entities.add("http://bio2rdf.org/drugbank:DB00305");
        entities.add("http://bio2rdf.org/drugbank:DB00644");
        entities.add("http://bio2rdf.org/drugbank:DB00531");

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
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "SELECT DISTINCT %s\n"
                + "WHERE {\n"
                + "%s"
                + "  FILTER (isLiteral(?target%s) || isIRI(?target%s))\n";
        if (nbWalks > 0) {
            queryString += "  BIND(RAND() AS ?sortKey)\n"
                    + "}\n"
                    + "ORDER BY ?sortKey\n"
                    + "LIMIT %s";
        } else {
            queryString += "}\n";
        }
        // build string of variables
        String variables = "";
        String triplePatterns = "";
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
        queryString = String.format(queryString, variables, triplePatterns, depth - 1, depth - 1, nbWalks);

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
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "SELECT DISTINCT %s\n"
                + "WHERE {\n"
                + "%s"
                + "  FILTER (isLiteral(?target%s) || isIRI(?target%s))\n";
        if (nbWalks > 0) {
            queryString += "  BIND(RAND() AS ?sortKey)\n"
                    + "}\n"
                    + "ORDER BY ?sortKey\n"
                    + "LIMIT %s";
        } else {
            queryString += "}\n";
        }
        // build string of variables
        String variables = "";
        String triplePatterns = "";
        for (int i = 0; i < depth; i++) {
            variables += "?p" + i + " ?class" + i + " ";
            if (i == 0) {
                triplePatterns += "  $ENTITY$ ?p" + i + " ?target" + i + " .\n"
                        + "  OPTIONAL { ?target" + i + " rdf:type ?class" + i + " }\n";
                //                        + "    FILTER (?p" + i + " != rdf:type)\n";
            } else {
                triplePatterns += "  ?target" + (i - 1) + " ?p" + i + " ?target" + i + " .\n"
                        + "  OPTIONAL { ?target" + i + " rdf:type ?class" + i + " }\n"
                        //                        + "  FILTER (?p" + i + " != rdf:type)\n";
                        + "  FILTER (?target" + (i - 1) + " != ?target" + i + ")\n";
            }
        }
        queryString = String.format(queryString, variables, triplePatterns, depth - 1, depth - 1, nbWalks);

        return queryString;
    }

}
