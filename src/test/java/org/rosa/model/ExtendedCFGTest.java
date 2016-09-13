package org.rosa.model;

import org.junit.Assert;
import org.junit.Test;
import org.rosa.model.graminf.BinaryRule;
import org.rosa.model.graminf.ExtendedCFG;
import org.rosa.model.graminf.Range;
import org.rosa.model.graminf.UnaryRule;

/**
 * {@link ExtendedCFGTest}, {@link org.rosa.model.graminf.BinaryRule},
 * {@link UnaryRule} unit tests.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class ExtendedCFGTest {

    @Test
    public void newUnaryRuleWithRangeTest() {
        UnaryRule p = new UnaryRule("A1", "String", new Range(0, 500));
        Assert.assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void newUnaryRuleWithoutRangeTest() {
        UnaryRule p = new UnaryRule("A1", "String");
        Assert.assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void newUnaryRuleInfinityRangeTest() {
        UnaryRule p = new UnaryRule("A1", "String", new Range(0, Double.POSITIVE_INFINITY));
        Assert.assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void newBinaryRuleWithRangeTest() {
        BinaryRule p = new BinaryRule("Start", "name", "String", new Range(0, 500));
        Assert.assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void newBinaryRuleWithoutRangeTest() {
        BinaryRule p = new BinaryRule("Start", "name", "String");
        Assert.assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void newExtendedCFG() {
        ExtendedCFG ecfg = new ExtendedCFG();
        ecfg.addUnaryRule(new UnaryRule("A1", "String", new Range(0, Double.POSITIVE_INFINITY)));
        ecfg.addUnaryRule(new UnaryRule("A1", "Double"));
        ecfg.addBinaryRule(new BinaryRule("Start", "name", "String"));
        ecfg.addBinaryRule(new BinaryRule("Start", "name", "String", new Range(0, 500)));
        System.out.println(ecfg);
    }

}
