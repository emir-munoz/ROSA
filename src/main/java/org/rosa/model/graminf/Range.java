package org.rosa.model.graminf;

/**
 * Representation of a range regular expression. Each term in an alphabet is defined with a range [l:r] of possible
 * occurrences, where l and r are non-negative integers, 0\leq l\leq r, and r can be \infty.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class Range {

    private int lower;
    private double upper;

    public Range(int lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public int getLower() {
        return lower;
    }

    public void setLower(int lower) {
        this.lower = lower;
    }

    public double getUpper() {
        return upper;
    }

    public void setUpper(double upper) {
        this.upper = upper;
    }

    @Override
    public String toString() {
        return "[" + lower + ", " + upper + "]";
    }

}
