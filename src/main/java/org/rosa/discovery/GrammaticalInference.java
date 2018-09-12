package org.rosa.discovery;

import com.opencsv.CSVReader;
import org.rosa.model.graminf.BinaryRule;
import org.rosa.model.graminf.ExtendedCFG;
import org.rosa.rdf.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Implementation of different grammatical inference algorithms.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class GrammaticalInference {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(GrammaticalInference.class.getSimpleName());
    private ExtendedCFG grammar;
    private PrefixMapping prefix;

    public GrammaticalInference() {
        grammar = new ExtendedCFG();
        prefix = new PrefixMapping();
    }

    public static void main(String... args) {
        GrammaticalInference gi = new GrammaticalInference();
        gi.naiveGrammarInduction("C:\\projects\\spine-ldd\\experiments\\ecfg\\bio2rdf-random-walk-depth-3-kegg.csv");
        gi.printGrammar();
    }

    /**
     * Naive method to read the random walks output by Apache Jena Fuseki as a CSV file,
     * and generate a grammar from them.
     *
     * @param filename
     *         Path to the document.
     */
    private void naiveGrammarInduction(final String filename) {
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(filename), ',', '\'', 1);
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                grammar.addBinaryRule(new BinaryRule(
                        "Start",
                        prefix.shortForm(nextLine[0].replace("\"", "").trim()),
                        prefix.shortForm(nextLine[1].replace("\"", "").trim())));
                for (int i = 1; i < nextLine.length - 3; i = i + 2) {
                    grammar.addBinaryRule(new BinaryRule(
                            prefix.shortForm(nextLine[i].replace("\"", "").trim()),
                            prefix.shortForm(nextLine[i + 1].replace("\"", "").trim()),
                            prefix.shortForm(nextLine[i + 2].replace("\"", "").trim())));
                }
            }
        } catch (FileNotFoundException e) {
            _log.error("Cannot find input file '{}'. Closing application.", filename);
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            _log.error("Error while reading input file '{}'. Closing application.", filename);
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void printGrammar() {
        System.out.println(grammar);
    }

}