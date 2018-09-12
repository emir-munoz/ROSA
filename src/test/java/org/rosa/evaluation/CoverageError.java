package org.rosa.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * @author Emir Munoz
 * @since 18/10/2016
 */
public class CoverageError {

    public double coverageError(int[] ranking, boolean[] trueLabels) {
        int howDeep = 0;
        int numLabels = trueLabels.length;
        for (int rank = numLabels; rank >= 1; rank--) {
            int indexOfRank;
            for (indexOfRank = 0; indexOfRank < numLabels; indexOfRank++) {
                if (ranking[indexOfRank] == rank) {
                    break;
                }
            }
            if (trueLabels[indexOfRank]) {
                howDeep = rank - 1;
                break;
            }
        }

        return howDeep;
    }

    @Test
    public void testOneError() {
        int[] ranking = new int[]{2, 3, 1};
        boolean[] trueLabels = new boolean[]{true, false, false};
        double expected_value = (1 / 1.0) * (2) - 1;
        System.out.println(coverageError(ranking, trueLabels));
        Assert.assertEquals(expected_value, coverageError(ranking, trueLabels), 0.001);

        ranking = new int[]{1, 2, 3};
        trueLabels = new boolean[]{false, false, true};
        expected_value = (1 / 1.0) * (3) - 1;
        System.out.println(coverageError(ranking, trueLabels));
        Assert.assertEquals(expected_value, coverageError(ranking, trueLabels), 0.001);
    }

}
