package org.rosa.rdf;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.rosa.util.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

/**
 * Set of RDF utils.
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 01/07/2016
 */
public class RDFUtil {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(RDFUtil.class.getSimpleName());

    /**
     * Generate a main memory RDF repository.
     *
     * @return Repository instance.
     */
    public static Repository connectToMemoryRepository() {
        _log.info("Creating Memory RDF repository");
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        return repository;
    }

    /**
     * Generate a main memory RDF repository in a given path.
     *
     * @param dirPath Path to directory.
     * @return Repository instance.
     */
    public static Repository connectToMemoryRepository(final String dirPath) {
        _log.info("Creating Memory RDF repository at '{}'", dirPath);
        File dataDir = new File(dirPath);
        Repository repository = new SailRepository(new MemoryStore(dataDir));
        repository.initialize();

        return repository;
    }

    /**
     * Connecting to SPARQL endpoint.
     * @param sparqlEndpoint SPARQL endpoint URI.
     * @return Repository instance.
     */
    public static Repository connectToSPARQLRepository(final String sparqlEndpoint) {
        _log.info("Connecting to SPARQL endpoint {}", sparqlEndpoint);
        Repository repository = new SPARQLRepository(sparqlEndpoint);
        repository.initialize();

        return repository;
    }

    /**
     * Generate a native RDF repository in a given path.
     *
     * @param dirPath Path to directory.
     * @return New native repository instance.
     */
    //    public static Repository connectToNativeRepository(final String dirPath) {
    //        _log.info("Creating Native RDF repository at '{}'", dirPath);
    //        File dataDir = new File(dirPath);
    //        Repository repository = new SailRepository(new NativeStore(dataDir));
    //        repository.initialize();
    //
    //        return repository;
    //    }

    /**
     * Load an RDF file into the repository.
     *
     * @param file       RDF file descriptor.
     * @param format     Serialization format of input file.
     * @param baseURI    Base URI.
     * @param repository Reference repository.
     */
    public static void loadRDFFromFile(final File file, final RDFFormat format,
                                       final String baseURI, Repository repository) {
        _log.info("Loading {} file '{}' into repository", format.getName(), file.getPath());
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(file, baseURI, format);
            conn.commit();
        } catch (RDF4JException e) {
            // handle Sesame exception. This catch-clause is
            // optional since RDF4JException is an unchecked exception
        } catch (IOException e) {
            _log.error("Error loading RDF file to local repository. Closing the application");
            e.printStackTrace();
            System.exit(1);
        }
        _log.info("RDF file loaded and repository ready for querying.");
    }

    /**
     * Dump the content of a repository into a static file.
     *
     * @param filename   Path to output file.
     * @param repository Reference repository.
     */
    public static void writeRepoToFile(final String filename, Repository repository) {
        _log.info("Saving repository to file ...");
        try (RepositoryConnection conn = repository.getConnection()) {
            // conn.begin();
            // Export all statements in the context to System.out, in RDF/XML format
            //RDFHandler writer = Rio.createWriter(RDFFormat.NTRIPLES, System.out);
            RDFHandler writer;
            GZIPOutputStream gzip = null;
            try {
                // create the directories if they don't exist
                File file = Paths.get(filename).toFile();
                FileSystem.setUpFolder(file);
                // determine whether file will be compressed or not
                if (com.google.common.io.Files.getFileExtension(filename).equals("gz")) {
                    gzip = new GZIPOutputStream(new FileOutputStream(file, false));
                    writer = Rio.createWriter(RDFFormat.NTRIPLES, gzip);
                } else {
                    writer = Rio.createWriter(RDFFormat.NTRIPLES, new FileOutputStream(file, false));
                }
                conn.export(writer);
                if (gzip != null) {
                    gzip.close();
                }
            } catch (IOException e) {
                _log.error("Output file cannot be created at {}", filename);
                e.printStackTrace();
            }
        }
        _log.info("RDF repository exported at {}", filename);
    }

}
