package org.rosa.model.graminf;

import com.google.common.base.MoreObjects;

/**
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class UnaryRule {

    private String parent;
    private String child;
    private Range range;

    public UnaryRule(String parent, String child) {
        this(parent, child, null);
    }

    public UnaryRule(String parent, String child, Range range) {
        this.parent = parent;
        this.child = child;
        this.range = range;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UnaryRule)) {
            return false;
        }
        final UnaryRule unaryRule = (UnaryRule) other;
        if (child != null ? !child.equals(unaryRule.getChild()) : unaryRule.getChild() != null) {
            return false;
        }
        if (parent != null ? !parent.equals(unaryRule.getParent()) : unaryRule.getParent() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (child != null ? child.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (range != null) {
            return parent + " -> " + child + " " + range;
        } else {
            return parent + " -> " + child;
        }
    }

    public String toStringShort() {
        if (range != null) {
            return child + " " + range;
        } else {
            return child;
        }
    }

}
