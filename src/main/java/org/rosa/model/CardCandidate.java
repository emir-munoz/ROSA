package org.rosa.model;

import com.google.common.base.Joiner;

import java.util.List;

/**
 * Class to represent a cardinality constraint candidate.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 12/07/2016
 */
public class CardCandidate {

    private int minBound;
    private int maxBound;
    private String context;
    private List<String> properties;

    /**
     * Default constructor.
     */
    public CardCandidate() {

    }

    /**
     * Class constructor.
     *
     * @param minBound
     *         Lower bound.
     * @param maxBound
     *         Upper bound.
     * @param context
     *         Context.
     * @param properties
     *         Set of properties.
     */
    public CardCandidate(final int minBound, final int maxBound,
                         final String context, final List<String> properties) {
        this.setMinBound(minBound);
        this.setMaxBound(maxBound);
        this.setContext(context);
        this.setProperties(properties);
    }

    public int getMinBound() {
        return minBound;
    }

    public void setMinBound(int minBound) {
        this.minBound = minBound;
    }

    public int getMaxBound() {
        return maxBound;
    }

    public void setMaxBound(int maxBound) {
        this.maxBound = maxBound;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public String toShEx() {
        // dot represents anything, i.e., IRI, xsd:string, etc.
        return String.format("<%s> .{%s, %s} ,", properties.get(0), minBound, maxBound);
    }

    @Override
    public String toString() {
        return String.format("card({<%s>},<%s>)=(%s,%s)",
                Joiner.on(",").join(properties), context, minBound, maxBound);
    }

}
