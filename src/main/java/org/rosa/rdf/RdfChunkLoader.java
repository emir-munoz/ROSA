package org.rosa.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RDF chunk loader for large files. This code is based on example at <a href="http://www.rivuli-development.com/further-reading/sesame-cookbook/loading-large-file-in-sesame-native/"></a>
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 22/07/2016
 */
public class RdfChunkLoader extends AbstractRDFHandler {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(RdfChunkLoader.class.getSimpleName());
    private RDFInserter inserter;
    private RepositoryConnection connection;
    private long count = 0L;
    /** commit every 500,000 triples */
    private long chunkSize = 500000L;

    /**
     * Class constructor.
     *
     * @param conn
     *         Repository connection.
     */
    public RdfChunkLoader(RepositoryConnection conn) {
        inserter = new RDFInserter(conn);
        this.connection = conn;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        inserter.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        _log.info("loaded {} triples", count);
        inserter.endRDF();
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        inserter.handleNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        inserter.handleStatement(st);
        count++;
        // commit whenever the number of triples has reached a multiple
        // of the chunk size
        if (count % chunkSize == 0) {
            try {
                connection.commit();
                _log.info("loaded {} triples", count);
            } catch (RepositoryException e) {
                throw new RDFHandlerException(e);
            }
        }
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        inserter.handleComment(comment);
    }

}
