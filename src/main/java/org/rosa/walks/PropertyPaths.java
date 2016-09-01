package org.rosa.walks;

import com.google.common.collect.Lists;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.rosa.rdf.RdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Extraction of all property paths for instances of a class.
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 29/05/2016
 */
public class PropertyPaths {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(PropertyPaths.class.getSimpleName());

    /**
     * Main method.
     */
    public static void main(String... args) {
        PropertyPaths prop = new PropertyPaths();
        prop.init();
    }

    /**
     * Initialization.
     */
    private void init() {
        Repository repository = RdfUtils.connectToMemoryRepository("./rdf/rosa-repo/");
        final File file = new File("C:/projects/tomoe-dev/bio-uc/datasets/bio2rdf/sider/20160405/sider-se.nq");
        final String baseURI = "http://example.org/";
        RdfUtils.loadRdfFile(file, RDFFormat.NQUADS, baseURI, repository);
        // RdfUtils.writeRepoToFile("./rdf/rosa-rdf/two-entities-v0.0.1.nt.gz", repository);
        List<String> paths = this.getPropertyPathsOfLength(2, "http://bio2rdf.org/sider_vocabulary:Drug-Effect-Association", repository);
        paths.forEach(System.out::println);
    }

    /**
     * Get property paths of a given length.
     *
     * @param length
     *         Length of the property path.
     * @param type
     *         Type of source node.
     * @param repo
     *         Reference repository.
     * @return List of property paths.
     */
    private List<String> getPropertyPathsOfLength(final int length, final String type, Repository repo) {
        List<String> paths = Lists.newArrayList();
        try (RepositoryConnection conn = repo.getConnection()) {
            _log.info("Querying dataset to get paths of length {}", length);
            String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "  SELECT %s\n" +
                    "  WHERE {\n" +
                    "    ?source rdf:type <%s> .\n" +
                    "%s" +
                    "    FILTER (isLiteral(?target%s) || isIRI(?target%s))\n" +
                    "  }";
            // build string of variables
            String variables = "";
            String triplePatterns = "";
            for (int i = 0; i < length; i++) {
                variables += "?p" + i + " ?class" + i + " ";
                if (i == 0) {
                    triplePatterns += "    ?source ?p" + i + " ?target" + i + " .\n" +
                            "    OPTIONAL { ?target" + i + " rdf:type ?class" + i + " }\n" +
                            "    FILTER (?p" + i + " != rdf:type)\n";
                } else {
                    triplePatterns += "    ?target" + (i - 1) + " ?p" + i + " ?target" + i + " .\n" +
                            "    OPTIONAL { ?target" + i + " rdf:type ?class" + i + " }\n" +
                            "    FILTER (?p" + i + " != rdf:type)\n";
                }
            }
            queryString = String.format(queryString, variables, type, triplePatterns, length - 1, length - 1);
            System.out.println(queryString);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value aProp;
                Value aClass;
                String path;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    path = "";
                    for (int i = 0; i < length; i++) {
                        aProp = bindingSet.getValue("p" + i);
                        aClass = bindingSet.getValue("class" + i);
                        if (aClass != null) {
                            path += "<" + aProp.stringValue() + "> (" + aClass.stringValue() + ")";
                        } else {
                            path += "<" + aProp.stringValue() + ">";
                        }
                        // if this property is not the last one, add a slash
                        if (i + 1 < length) {
                            path += "/";
                        }
                    }
                    paths.add(path);
                }
            }
        }

        return paths;
    }

}
