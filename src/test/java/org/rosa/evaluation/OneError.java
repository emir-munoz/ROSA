package org.rosa.evaluation;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Emir Munoz
 * @since 18/10/2016
 */
public class OneError {

    public double oneError(int[] ranking, boolean[] groundTruth) {
        final double[] oneError = {0.0};
        IntStream.range(0, groundTruth.length)
                .forEach(index -> {
                    if (ranking[index] == 1) {
                        if (!groundTruth[index])
                            oneError[0]++;
                    }
                });

        return oneError[0];
    }

    @Test
    public void testOneError() {
        int[] ranking = new int[]{3, 1, 2};
        boolean[] trueLabels = new boolean[]{true, true, false};
        double expected_value = (1 / 2.0) * ((2 / 3.0) + (1 / 1.0));
        System.out.println(oneError(ranking, trueLabels));
//        Assert.assertEquals(expected_value, coverageError(ranking, trueLabels), 0.001);
    }

}
