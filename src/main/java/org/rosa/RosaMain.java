package org.rosa;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.rosa.discovery.DiscoveryCardSparql;
import org.rosa.ml.numeric.DetectionType;
import org.rosa.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * Main class.
 *
 * @author Emir Munoz
 * @version 0.0.3
 * @since 29/04/2016
 */
public class RosaMain {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(RosaMain.class.getSimpleName());
    private static JCommander jc;
    @Parameter(names = {"-h", "--help"}, help = true, description = "Displays this nice help message")
    private boolean help;
    @Parameter(names = {"-v", "--version"}, description = "Version of the application")
    private boolean version;
    @Parameter(names = "--card", description = "Extraction for cardinality constraints")
    private boolean cardExp = false;
    @Parameter(names = {"-f", "--file"}, description = "Path to RDF file (*.nt, *.nq, *.gz)")
    private String filename;
    @Parameter(names = {"-e", "--endpoint"}, description = "SPARQL endpoint")
    private String sparqlEndpoint;
    @Parameter(names = {"-c", "--context"}, description = "Context of constraints (class or empty)")
    private String constContext;
    @Parameter(names = {"-m", "--method"}, description = "Outlier detection method")
    private String outlierMethod;
    @Parameter(names = {"-t", "--tval"}, description = "Deviation factor")
    private String tValue;

    /**
     * Main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String... args) {
        RosaMain main = new RosaMain();
        jc = new JCommander(main, args);
        main.run();
    }

    /**
     * Run method.
     */
    private void run() {
        final String header1 = "[ROSA - RDF and Ontology Constraints Analyser (2016)]";
        _log.info("{}", header1);

        // print version
        if (version) {
            System.out.println("Version 0.1.0");
            System.exit(0);
        }
        // compute cardinality constraints extraction
        if (cardExp) {
            DiscoveryCardSparql bs = new DiscoveryCardSparql();
            try {
                if (filename != null) {
                    bs.runDiscovery(DataSource.FILE, filename, constContext,
                            DetectionType.valueOf(outlierMethod), Double.valueOf(tValue));
                } else if (sparqlEndpoint != null) {
                    bs.runDiscovery(DataSource.SPARQL, sparqlEndpoint, constContext,
                            DetectionType.valueOf(outlierMethod), Double.valueOf(tValue));
                }
            } catch (FileNotFoundException e) {
                _log.error("The input dataset file cannot be found.");
                e.printStackTrace();
            }
        } else {
            jc.usage();
        }
    }

}
