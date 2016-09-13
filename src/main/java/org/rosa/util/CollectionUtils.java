package org.rosa.util;

import com.beust.jcommander.internal.Lists;

import java.util.List;
import java.util.Map;

/**
 * Several functions to work with collections.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 09/09/2016
 */
public class CollectionUtils {

    public static <K, V> void addToValueList(Map<K, List<V>> map, K key, V value) {
        List<V> valueList = map.get(key);
        if (valueList == null) {
            valueList = Lists.newArrayList();
            map.put(key, valueList);
        }
        valueList.add(value);
    }

    public static <K, V> List<V> getValueList(Map<K, List<V>> map, K key) {
        List<V> valueList = map.get(key);
        if (valueList == null) {
            return Lists.newArrayList();
        }

        return valueList;
    }

}
