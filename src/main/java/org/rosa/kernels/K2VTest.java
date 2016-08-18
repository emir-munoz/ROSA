package org.rosa.kernels;

import com.google.common.collect.Sets;
import org.data2semantics.mustard.rdf.LiteralType;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.nodes.DTGraph;
import org.nodes.DTNode;

/**
 * @author Emir Munoz
 * @version 0.0.1
 * @since 18/08/2016
 */
public class K2VTest {

    public static void main(String... args) {
        String rdf_file = args[0];
        RDFDataSet tripleStore = new RDFFileDataSet(rdf_file);
        // RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE2, RDFFormat.NQUADS);
        DTGraph<String, String> graph = RDFUtils.statements2Graph(
                Sets.newHashSet(tripleStore.getFullGraph()),
                LiteralType.REGULAR_LITERALS);
        for (DTNode<String, String> n : graph.nodes()) {
            System.out.println(n);
        }
    }

}
