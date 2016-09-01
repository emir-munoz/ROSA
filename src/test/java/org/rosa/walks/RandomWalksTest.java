package org.rosa.walks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link RandomWalks} unit tests.
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since  31/08/2016
 */
public class RandomWalksTest {

    private RandomWalks rw;

    @Before
    public void setUp() {
        rw = new RandomWalks();
    }

    @Test
    public void testFirstPropertiesQuery() {
        String query = rw.generateRandomWalksQuery("http://something", 1, 0);
        Assert.assertNotNull(query);
        System.out.println(query);
    }

    @Test
    public void testDepthQuery() {
        String query = rw.generateRandomWalksQuery("http://something", 1, 3);
        Assert.assertNotNull(query);
        System.out.println(query);
    }

    @Test
    public void testClassWalkQuery() {
        String query = rw.generateClassPathQuery("http://something", 3, 500);
        Assert.assertNotNull(query);
        System.out.println(query);
    }

}
