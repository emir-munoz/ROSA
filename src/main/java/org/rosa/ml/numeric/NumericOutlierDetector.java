package org.rosa.ml.numeric;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.rosa.model.OutlierResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Outlier detection for numerical data.
 *
 * @author Emir Munoz
 * @version 0.0.5
 * @since 25/06/2016
 */
public class NumericOutlierDetector {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(NumericOutlierDetector.class.getName());

    /**
     * Extreme Studentized Deviation (ESD) identifier as outlier detection rule. A default value for t is 3.
     *
     * @param data
     *         Array of numbers.
     * @param tValue
     *         Factor value.
     */
    public static OutlierResult ruleESD(final double[] data, final double tValue) {
        _log.info("Running ESD outlier detection");
        double mean = Statistics.getMean(data);
        double stdDev = Statistics.getStdDev(data);
        double lowerBound = mean - tValue * stdDev;
        double upperBound = mean + tValue * stdDev;
        OutlierResult result = new OutlierResult();
        result.setLowerBound(lowerBound);
        result.setUpperBound(upperBound);
        _log.warn("{}", result);
        for (int i = 0; i < data.length; i++) {
            if (data[i] >= lowerBound && data[i] <= upperBound) {
                result.addNonOutlier(data[i]);
            } else {
                result.addOutlierIndex(i);
            }
        }

        return result;
    }

    /**
     * Hampel identifier as outlier detection rule. A default value for t is 3.
     *
     * @param data
     *         Array of numbers.
     * @param tValue
     *         Factor value.
     */
    public static OutlierResult ruleHampel(final double[] data, final double tValue) {
        _log.info("Running HAMPEL outlier detection");
        _log.info("{}", data);
        double median = Statistics.getMedian(data);
        double mad = Statistics.getMAD(data);
        double lowerBound = median - tValue * mad;
        double upperBound = median + tValue * mad;
        OutlierResult result = new OutlierResult();
        result.setLowerBound(lowerBound);
        result.setUpperBound(upperBound);
        _log.warn("{}", result);
        for (int i = 0; i < data.length; i++) {
            if (data[i] >= lowerBound && data[i] <= upperBound) {
                result.addNonOutlier(data[i]);
            } else {
                result.addOutlierIndex(i);
            }
        }

        return result;
    }

    /**
     * Boxplot identifier as outlier detection rule. A default value for c is 1.5.
     *
     * @param data
     *         Array of numbers.
     * @param cValue
     *         Factor value.
     */
    public static OutlierResult ruleBoxplot(final double[] data, final double cValue) {
        _log.info("Running BOXPLOT outlier detection");
        double q1 = Statistics.getQuartile(data, 25);
        double q3 = Statistics.getQuartile(data, 75);
        double iqd = q3 - q1;
        double lowerBound = q1 - cValue * iqd;
        double upperBound = q3 + cValue * iqd;
        OutlierResult result = new OutlierResult();
        result.setLowerBound(lowerBound);
        result.setUpperBound(upperBound);
        // _log.warn("{}", result);
        for (int i = 0; i < data.length; i++) {
            if (data[i] >= lowerBound && data[i] <= upperBound) {
                result.addNonOutlier(data[i]);
            } else {
                result.addOutlierIndex(i);
            }
        }

        return result;
    }

    /**
     * Extract outliers from an array of values using z-score.
     *
     * @param data
     *         Array of numbers.
     */
    public static OutlierResult ruleZScore(final double[] data) {
        double mean = Statistics.getMean(data);
        double stdDev = Statistics.getStdDev(data);
        OutlierResult result = new OutlierResult();
        double zScore;
        for (int i = 0; i < data.length; i++) {
            zScore = zScore(data[i], mean, stdDev);
            if (zScore <= 3) {
                result.addNonOutlier(data[i]);
            } else {
                result.addOutlierIndex(i);
            }
        }

        return result;
    }

    /**
     * Extract outliers from an array of values using robust z-score.
     *
     * @param data
     *         Array of numbers.
     */
    public static OutlierResult ruleRobusZScore(final double[] data) {
        double mean = Statistics.getMean(data);
        double mad = Statistics.getMAD(data);
        OutlierResult result = new OutlierResult();
        double zScore;
        for (int i = 0; i < data.length; i++) {
            zScore = robustZScore(data[i], mean, mad);
            System.out.println("val=" + data[i] + ", z-score=" + zScore);
            if (zScore <= 3) {
                result.addNonOutlier(data[i]);
            } else {
                result.addOutlierIndex(i);
            }
        }

        return result;
    }

    /**
     * z-score (z-value, standard score, or normal score) is a measure of the divergence of
     * an individual experimental result from the most probable result.
     *
     * @param value
     *         Some value to test.
     * @param mean
     *         Mean value.
     * @param stdDev
     *         Standard deviation.
     * @return z-score.
     */
    private static double zScore(final double value, final double mean, final double stdDev) {
        return (Math.abs(value - mean) / stdDev);
    }

    /**
     * Get z-score for an array of values.
     *
     * @param data
     *         Array of numbers.
     * @param mean
     *         Mean value of sequence.
     * @param stdDev
     *         Standard deviation.
     * @return z-score for every double in the input.
     */
    private static double[] zScore(final double[] data, final double mean, final double stdDev) {
        double[] scores = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scores[i] = (Math.abs(data[i] - mean) / stdDev);
        }

        return scores;
    }

    /**
     * robust z-score for a single value.
     *
     * @param value
     *         Some value to test.
     * @param mean
     *         Mean value of sequence.
     * @param mad
     *         Median absolute deviation of sequence.
     * @return robust z-score for a value.
     */
    private static double robustZScore(final double value, final double mean, final double mad) {
        return (Math.abs(value - mean) / mad);
    }

    /**
     * Get robust z-score for an array of values.
     *
     * @param data
     *         Array of numbers.
     * @param mean
     *         Mean value of sequence.
     * @param mad
     *         Median absolute deviation of sequence.
     * @return robust z-score for every double in the input.
     */
    private static double[] robustZScore(final double[] data, final double mean, final double mad) {
        double[] scores = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scores[i] = (Math.abs(data[i] - mean) / mad);
        }

        return scores;
    }

    /**
     * Extract outliers from a list of string values.
     *
     * @param values
     *         List of string values.
     */
    public static void extractOutliersFromString(List<String> values) {
        List<Double> sequence = Lists.newArrayList();
        Pattern p = Pattern.compile("\\d+(,\\d+)*.\\d+");
        Matcher m;
        for (String val : values) {
            m = p.matcher(val);
            while (m.find()) {
                //System.out.println(m.group());
                sequence.add(Double.parseDouble(m.group().replace(",", "")));
            }
        }

        double[] data = Doubles.toArray(sequence);
        ruleZScore(data);
    }

}
