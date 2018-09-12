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
public class AveragePrecision {

    public double averagePrecision(int[] ranking, boolean[] trueLabels) {
        final double[] avgP = {0.0};
        List<Integer> relevant = Lists.newArrayList();
        IntStream.range(0, trueLabels.length)
                .forEach(index -> {
                    if (trueLabels[index])
                        relevant.add(index);
                });
        if (!relevant.isEmpty()) {
            relevant.forEach(index1 -> {
                final double[] rankedAbove = {0};
                relevant.forEach(index2 -> {
                    if (ranking[index2] <= ranking[index1])
                        rankedAbove[0]++;
                });
                avgP[0] += (rankedAbove[0] / ranking[index1]);
            });
            avgP[0] /= relevant.size();
            System.out.println(avgP[0]);
        }

        return avgP[0];
    }

    @Test
    public void testAveragePrecision() {
        int[] ranking = new int[]{3, 1, 2};
        boolean[] trueLabels = new boolean[]{true, true, false};
        double expected_value = (1 / 2.0) * ((2 / 3.0) + (1 / 1.0));
        Assert.assertEquals(expected_value, averagePrecision(ranking, trueLabels), 0.001);
    }

    @Test
    public void testAveragePrecision2() {
        int[] ranking = new int[]{1, 2, 3, 4, 5};
        boolean[] trueLabels = new boolean[]{true, true, false, false, true};
        double expected_value = (1 / 3.0) * ((1 / 1.0) + (2 / 2.0) + (3 / 5.0));
        Assert.assertEquals(expected_value, averagePrecision(ranking, trueLabels), 0.001);
    }

}
