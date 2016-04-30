package rdf.spine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import rdf.spine.dao.DiscoveryCardSparql;

import java.io.FileNotFoundException;

/**
 * Main class.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 29/04/16.
 */
public class Rosa {

    @Parameter(names = "-baseline", description = "Run SPARQL extraction for cardinality constraints")
    private boolean baseline = false;

    @Parameter(names = "-file", description = "Path to RDF file (*.nt, *.nq, *.gz)")
    private String filename;

    @Parameter(names = "-context", description = "Context of constraints (class or empty)")
    private String constContext;

    private static JCommander jc;

    /**
     * Main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Rosa main = new Rosa();
        jc = new JCommander(main, args);
        main.run();
    }

    /**
     * Run method.
     */
    private void run() {
        if (baseline) {
            DiscoveryCardSparql bs = new DiscoveryCardSparql();
            try {
                bs.runDiscovery(filename, constContext);
                //                bs.runDiscovery("/media/sf_projects/spine-ldd/ShEx/shexcala-0.5
                // .8/experiments/dbpedia-personFunction/personFunction.nt.gz",
                //                        "http://dbpedia.org/ontology/PersonFunction");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            jc.usage();
        }
    }

}
