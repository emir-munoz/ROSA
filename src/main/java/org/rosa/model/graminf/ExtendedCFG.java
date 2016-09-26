package org.rosa.model.graminf;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import org.rosa.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Representation of an Extended Context-Free Grammar.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class ExtendedCFG {

    private Set<String> states;
    private List<UnaryRule> unaryRules;
    private Map<String, List<UnaryRule>> unaryRulesByChild;
    private Map<String, List<UnaryRule>> unaryRulesByParent;
    private List<BinaryRule> binaryRules;
    private Map<String, List<BinaryRule>> binaryRulesByParent;
    private Map<String, List<BinaryRule>> binaryRulesByLeftChild;
    private Map<String, List<BinaryRule>> binaryRulesByRightChild;

    public ExtendedCFG() {
        states = Sets.newHashSet();
        unaryRules = Lists.newArrayList();
        unaryRulesByParent = Maps.newHashMap();
        unaryRulesByChild = Maps.newHashMap();
        binaryRules = Lists.newArrayList();
        binaryRulesByParent = Maps.newHashMap();
        binaryRulesByLeftChild = Maps.newHashMap();
        binaryRulesByRightChild = Maps.newHashMap();
    }

    public List<UnaryRule> getUnaryRules() {
        return unaryRules;
    }

    public Set<String> getStates() {
        return states;
    }

    public void setStates(Set<String> states) {
        this.states = states;
    }

    public Map<String, List<UnaryRule>> getUnaryRulesByChild() {
        return unaryRulesByChild;
    }

    public void setUnaryRulesByChild(Map<String, List<UnaryRule>> unaryRulesByChild) {
        this.unaryRulesByChild = unaryRulesByChild;
    }

    public Map<String, List<UnaryRule>> getUnaryRulesByParent() {
        return unaryRulesByParent;
    }

    public void setUnaryRulesByParent(Map<String, List<UnaryRule>> unaryRulesByParent) {
        this.unaryRulesByParent = unaryRulesByParent;
    }

    public void addUnaryRule(final UnaryRule unaryRule) {
        states.add(unaryRule.getParent());
        states.add(unaryRule.getChild());
        unaryRules.add(unaryRule);
        CollectionUtils.addToValueList(unaryRulesByParent, unaryRule.getParent(), unaryRule);
        CollectionUtils.addToValueList(unaryRulesByChild, unaryRule.getChild(), unaryRule);
    }

    public void addBinaryRule(final BinaryRule binaryRule) {
        states.add(binaryRule.getParent());
        states.add(binaryRule.getLeftChild());
        states.add(binaryRule.getRightChild());
        binaryRules.add(binaryRule);
        CollectionUtils.addToValueList(binaryRulesByParent, binaryRule.getParent(), binaryRule);
        CollectionUtils.addToValueList(binaryRulesByLeftChild, binaryRule.getLeftChild(), binaryRule);
        CollectionUtils.addToValueList(binaryRulesByRightChild, binaryRule.getRightChild(), binaryRule);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("");
        // maybe sort here the collections
        //        unaryRules.sort((r1, r2) -> r1.compareTo(r2));
        //        binaryRules.sort((r1, r2) -> r1.compareTo(r2));
        //        unaryRules.forEach(rule -> sb.append(rule).append("\n"));
        //        binaryRules.forEach(rule -> sb.append(rule).append("\n"));
        //        unaryRulesByParent.entrySet().forEach(rule -> sb.append(rule).append("\n"));
        //        binaryRulesByParent.entrySet().forEach(rule -> sb.append(rule).append("\n"));
        unaryRulesByParent.forEach((head, tail) -> {
            sb.append(head).append(" -> ");
            // Set<UnaryRule> tailSet = Sets.newHashSet();
            // tailSet.addAll(tail);
            sb.append(tail.stream().map(UnaryRule::toStringShort)
                    .collect(Collectors.joining(" | ")));
            sb.append("\n");
        });
        binaryRulesByParent.forEach((head, tail) -> {
            sb.append(head).append(" -> ");
            // Set<BinaryRule> tailSet = Sets.newHashSet();
            // tailSet.addAll(tail);
            sb.append(tail.stream().map(BinaryRule::toStringShort)
                    .collect(Collectors.joining(" | ")));
            sb.append("\n");
        });

        return sb.toString();
    }

}
