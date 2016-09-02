package org.rosa.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Map;

/**
 * String template unit tests.
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 02/09/2016
 */
public class StringUtilsTest {

    @Test
    public void testStringTemplate() {
        Map<String, String> values = Maps.newHashMap();
        values.put("value", "1");
        values.put("column", "2");
        StrSubstitutor sub = new StrSubstitutor(values, "%(", ")");
        String result = sub.replace("There's an incorrect value '%(value)' in column #%(column)");
        Assert.assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testStringTemplateSet() {
        Map<String, String> values = Maps.newHashMap();
        values.put("value", "1");
        values.put("column", "2");
        StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
        String result = sub.replace("There's an incorrect value '{value}' in column #{column} because of {value}");
        Assert.assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testMessageFormat() {
        String result = MessageFormat.format("There''s an incorrect value ''{0}'' in column #{1} because of {0}", 1, 2);
        Assert.assertNotNull(result);
        System.out.println(result);
    }

}
