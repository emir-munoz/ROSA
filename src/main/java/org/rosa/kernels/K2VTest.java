package org.rosa.kernels;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountKernel;
import org.data2semantics.mustard.rdf.LiteralType;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.nodes.DTGraph;

import java.util.List;

/**
 * @author Emir Munoz
 * @version 0.0.1
 * @since 18/08/2016
 */
public class K2VTest {

    public static void main(String... args) {
        String rdfFile = args[0];
        String property = args[1];
        int pathLength = Integer.valueOf(args[2]);
        int depth = Integer.valueOf(args[3]);
        boolean inference = false;

        RDFDataSet tripleStore = new RDFFileDataSet(rdfFile);
//        // RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE2, RDFFormat.NQUADS);
//        DTGraph<String, String> graph = RDFUtils.statements2Graph(
//                Sets.newHashSet(tripleStore.getFullGraph()),
//                LiteralType.REGULAR_LITERALS);
//        for (DTNode<String, String> n : graph.nodes()) {
//            System.out.println(n);
//        }
        List<Statement> stms = tripleStore.getStatementsFromStrings(
                null,
                property,
                null);
        List<Resource> instances = Lists.newArrayList();
        for (Statement stm : stms) {
            instances.add(stm.getSubject());
        }
        RDFData data = new RDFData(tripleStore, instances, Lists.newArrayList());
        RDFWalkCountKernel kernel = new RDFWalkCountKernel(pathLength, depth, inference, true);
        SparseVector[] fv = kernel.computeFeatureVectors(data);
        for (int i = 0; i < fv.length; i++) {
            System.out.println(instances.get(i) + "\t" + fv[i] + "\t" +
                    kernel.getFeatureDescriptions(Lists.newArrayList(fv[i].getIndices())));
        }
    }

}
