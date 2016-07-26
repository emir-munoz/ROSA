package org.rosa.ml.numeric;

import java.util.Arrays;

/**
 * Basic numerical statistics.
 *
 * @author Emir Munoz
 * @version 0.0.5
 * @since 25/06/2016
 */
public class Statistics {

    /**
     * Get the mean (average) over an array of numbers.
     *
     * @param data
     *         Array of numbers.
     * @return Mean.
     */
    public static double getMean(final double[] data) {
        double sum = 0.0;
        for (double num : data) {
            sum += num;
        }

        return (sum / data.length);
    }

    /**
     * Compute median of an array of numbers.
     *
     * @param data
     *         Array of numbers.
     * @return Median.
     */
    public static double getMedian(final double[] data) {
        Arrays.sort(data);
        int middle = data.length / 2;
        if (data.length % 2 == 0) {
            return (data[middle] + data[middle - 1]) / 2;
        } else {
            return data[middle + 1];
        }
    }

    /**
     * Standard deviation gives an idea of how close the entire set of data is to the average value.
     *
     * @param data
     *         Array of numbers.
     * @return Standard deviation.
     */
    public static double getStdDev(final double[] data) {
        return Math.sqrt(getVariance(data));
    }

    /**
     * Variance measures how far a set of numbers is spread out. A variance of zero indicates that all the values are
     * identical.
     *
     * @param data
     *         Array of numbers.
     * @return Variance.
     */
    public static double getVariance(final double[] data) {
        double mean = getMean(data);
        double aux = 0;
        for (double num : data) {
            aux += (mean - num) * (mean - num);
        }

        return (aux / data.length);
    }

    /**
     * Compute the Median Absolute Deviation for an array of values. https://en.wikipedia.org/wiki/Median_absolute_deviation
     *
     * @param data
     *         Array of numbers.
     * @return Median Absolute Deviation (MAD) of sequence.
     */
    public static double getMAD(final double[] data) {
        double median = getMedian(data);
        double[] abs = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            abs[i] = Math.abs(data[i] - median);
        }

        return getMedian(abs);
    }

    /**
     * Compute the quartile value from an array.
     *
     * @param data
     *         Array of numbers.
     * @param lowerPercent
     *         Percent cut-off. For the lower quartile use 25, for the upper-quartile use 75.
     * @return Quartile.
     */
    public static double getQuartile(final double[] data, double lowerPercent) {
        double[] v = new double[data.length];
        System.arraycopy(data, 0, v, 0, data.length);
        Arrays.sort(v);

        int n = (int) Math.round(v.length * lowerPercent / 100);

        return v[n];
    }

}
