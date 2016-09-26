package org.rosa.rdf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link PrefixMapping} unit tests.
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 14/09/2016
 */
public class PrefixMappingTest {

    private PrefixMapping ns;

    @Before
    public void setUp() {
        ns = new PrefixMapping();
    }

    @Test
    public void loadPrefixMappingTest() {
        Assert.assertNotNull(ns);
    }

    @Test
    public void shortFormTest1() {
        String iri = "http://bio2rdf.org/something";
        String result = ns.shortForm(iri);
        Assert.assertEquals("bio2rdf:something", result);
    }

    @Test
    public void shortFormTest2() {
        String iri = "http://bio2rdf.org/ns/kegg#something";
        String result = ns.shortForm(iri);
        Assert.assertEquals("bio2rdf:ns/kegg#something", result);
    }

    @Test
    public void shortFormTest3() {
        String iri = "http://bio2rdf.org/drugbank_vocabulary:Dosage";
        String result = ns.shortForm(iri);
        Assert.assertEquals("bio2rdf:drugbank_vocabulary:Dosage", result);
    }

    @Test
    public void qNameForTest1() {
        String iri = "http://bio2rdf.org/ns/kegg#something";
        String result = ns.qnameFor(iri);
        Assert.assertEquals("kegg:something", result);
    }

    @Test
    public void qNameForTest2() {
        String iri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String result = ns.qnameFor(iri);
        Assert.assertEquals("rdf:type", result);
    }

    @Test
    public void qNameForNoPrefixTest() {
        String iri = "http://seasonal.song/preamble/";
        String result = ns.qnameFor(iri);
        Assert.assertEquals(null, result);
    }

    @Test
    public void expandPrefixTest1() {
        String shortURI = "bio2rdf:something";
        String result = ns.expandPrefix(shortURI);
        Assert.assertEquals("http://bio2rdf.org/something", result);
    }

    @Test
    public void expandPrefixTest2() {
        String shortURI = "kegg:something";
        String result = ns.expandPrefix(shortURI);
        Assert.assertEquals("http://bio2rdf.org/ns/kegg#something", result);
    }

    @Test
    public void expandPrefixTest3() {
        String shortURI = "bio2rdf:kegg_vocabulary:same-as";
        String result = ns.expandPrefix(shortURI);
        Assert.assertEquals("http://bio2rdf.org/kegg_vocabulary:same-as", result);
    }

    @Test
    public void isNiceURITest1() {
        String uri = "http://bio2rdf.org/ns/kegg#something";
        Assert.assertTrue(ns.isNiceURI(uri));
    }

    @Test
    public void isNiceURITest2() {
        String uri = "http://bio2rdf.org/ns/kegg#";
        Assert.assertFalse(ns.isNiceURI(uri));
    }

}
