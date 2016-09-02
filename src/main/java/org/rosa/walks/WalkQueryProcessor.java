package org.rosa.walks;

import com.google.common.collect.Lists;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Query processor for walks as a thread. It returns the found walks for a given entity.
 *
 * See also: {@link RandomWalks} and {@link WalksWriter}
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 31/08/2016
 */
public class WalkQueryProcessor implements Runnable {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(WalkQueryProcessor.class.getSimpleName());
    private int depth;
    private String query;
    private Repository repo;

    /**
     * Class constructor.
     *
     * @param depth
     *         Depth of the walk.
     * @param query
     *         SPARQL query to process.
     * @param repo
     *         SPARQL endpoint.
     */
    public WalkQueryProcessor(int depth, String query, Repository repo) {
        this.depth = depth;
        this.query = query;
        this.repo = repo;
    }

    @Override
    public void run() {
        // _log.info("Thread {} run starts here", Thread.currentThread().getId());
        WalksWriter.getInstance().write(processWalks());
        // _log.info("Thread {} run is over", Thread.currentThread().getId());
    }

    private List<String> processWalks() {
        List<String> walksList = Lists.newArrayList();
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, this.query);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value aProp;
                Value aTarget;
                String walk;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    walk = "";
                    for (int i = 0; i < this.depth; i++) {
                        aProp = bindingSet.getValue("p" + i);
                        aTarget = bindingSet.getValue("target" + i);
                        // if (aTarget != null) {
                        walk += "<" + aProp.stringValue() + "> --> <" + aTarget.stringValue() + ">";
                        // if this property is not the last one, add a slash
                        if (i + 1 < this.depth) {
                            walk += " --> ";
                        }
                    }
                    walksList.add(walk);
                }
            }
        }

        return walksList;
    }

}
