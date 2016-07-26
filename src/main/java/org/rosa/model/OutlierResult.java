package org.rosa.model;

import com.beust.jcommander.internal.Lists;

import java.util.List;

/**
 * Represents the result of an outlier detection method.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 16/07/2016
 */
public class OutlierResult {

    private List<Integer> outlierIndices;
    private List<Double> nonOutliers;
    private double lowerBound;
    private double upperBound;

    /**
     * Class constructor.
     */
    public OutlierResult() {
        this.outlierIndices = Lists.newArrayList();
        this.nonOutliers = Lists.newArrayList();
    }

    public List<Integer> getOutlierIndices() {
        return outlierIndices;
    }

    public void setOutlierIndices(List<Integer> outlierIndices) {
        this.outlierIndices = outlierIndices;
    }

    public void addOutlierIndex(int index) {
        this.outlierIndices.add(index);
    }

    public List<Double> getNonOutliers() {
        return nonOutliers;
    }

    public void setNonOutliers(List<Double> nonOutliers) {
        this.nonOutliers = nonOutliers;
    }

    public void addNonOutlier(double elem) {
        this.nonOutliers.add(elem);
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public String toString() {
        return "OutlierRes:{lowerBound=" + lowerBound +
                ", upperBound=" + upperBound + "}";
    }

}
