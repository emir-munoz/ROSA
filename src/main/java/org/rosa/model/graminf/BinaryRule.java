package org.rosa.model.graminf;

/**
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class BinaryRule {

    private String parent;
    private String leftChild;
    private String rightChild;
    private Range range;

    public BinaryRule(String parent, String leftChild, String rightChild) {
        this(parent, leftChild, rightChild, null);
    }

    public BinaryRule(String parent, String leftChild, String rightChild, Range range) {
        this.parent = parent;
        this.setLeftChild(leftChild);
        this.rightChild = rightChild;
        this.range = range;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(String leftChild) {
        this.leftChild = leftChild;
    }

    public String getRightChild() {
        return rightChild;
    }

    public void setRightChild(String rightChild) {
        this.rightChild = rightChild;
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
        if (!(other instanceof BinaryRule)) {
            return false;
        }
        final BinaryRule binaryRule = (BinaryRule) other;
        if (parent != null ? !parent.equals(binaryRule.getParent()) : binaryRule.getParent() != null) {
            return false;
        }
        if (leftChild != null ? !leftChild.equals(binaryRule.getLeftChild()) : binaryRule.getLeftChild() != null) {
            return false;
        }
        if (rightChild != null ? !rightChild.equals(binaryRule.getRightChild()) : binaryRule.getRightChild() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (leftChild != null ? leftChild.hashCode() : 0);
        result = 31 * result + (rightChild != null ? rightChild.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        if (range != null) {
            return parent + " -> (" + leftChild + ": " + rightChild + ") " + range;
        } else {
            return parent + " -> " + leftChild + ": " + rightChild;
        }
    }

    public String toStringShort() {
        if (range != null) {
            return "(" + leftChild + ":::" + rightChild + ") " + range;
        } else {
            return leftChild + ":::" + rightChild;
        }
    }

}
