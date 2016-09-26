package org.rosa.rdf;

import com.beust.jcommander.internal.Maps;
import com.opencsv.CSVReader;
import org.apache.xerces.util.XMLChar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Helper to handle namespaces such as xsd, rdf, owl, etc.
 * <p/>
 * The list of prefixes and the full version of URIs can be downloaded from <a href="https://prefix.cc/popular/all.file.csv">prefix.cc</a>
 * <p/>
 * Some of the functions were taken from Apache Jena project class: <a href="https://github.com/apache/jena/blob/master/jena-core/src/main/java/org/apache/jena/rdf/model/impl/Util.java">Util.java</a>
 *
 * @author Emir Munoz
 * @version 0.0.2
 * @since 14/09/2016
 */
public class PrefixMapping {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(PrefixMapping.class.getSimpleName());
    private Map<String, String> prefixToURI;
    private Map<String, String> URItoPrefix;

    /**
     * Class constructor.
     */
    public PrefixMapping() {
        prefixToURI = Maps.newHashMap();
        URItoPrefix = Maps.newHashMap();
        ClassLoader classLoader = this.getClass().getClassLoader();
        File nsFile = new File(classLoader.getResource("prefix.cc.all.file.csv").getFile());
        loadPrefixes(nsFile);
    }

    /**
     * Class constructor.
     *
     * @param filename
     *         Path to the prefix.cc file.
     */
    public PrefixMapping(String filename) {
        prefixToURI = Maps.newHashMap();
        URItoPrefix = Maps.newHashMap();
        File nsFile = new File(filename);
        loadPrefixes(nsFile);
    }

    /**
     * Given an absolute URI, determine the split point between the namespace part and the localname part. If there is
     * no valid localname part then the length of the string is returned. The algorithm tries to find the longest NCName
     * at the end of the uri, not immediately preceeded by the first colon in the string.
     * <p/>
     * This operation follows XML QName rules which are more complicated than needed for Turtle and TriG.   For example,
     * QName can't start with a digit.
     *
     * @param uri
     *         Input URI to split.
     * @return the index of the first character of the localname.
     */
    private int splitNamespaceXML(String uri) {
        // XML Namespaces 1.0:
        // A qname name is NCName ':' NCName
        // NCName             ::=      NCNameStartChar NCNameChar*
        // NCNameChar         ::=      NameChar - ':'
        // NCNameStartChar    ::=      Letter | '_'
        //
        // XML 1.0
        // NameStartChar      ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] |
        //                        [#xD8-#xF6] | [#xF8-#x2FF] |
        //                        [#x370-#x37D] | [#x37F-#x1FFF] |
        //                        [#x200C-#x200D] | [#x2070-#x218F] |
        //                        [#x2C00-#x2FEF] | [#x3001-#xD7FF] |
        //                        [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
        // NameChar           ::= NameStartChar | "-" | "." | [0-9] | #xB7 |
        //                        [#x0300-#x036F] | [#x203F-#x2040]
        // Name               ::= NameStartChar (NameChar)*

        char ch;
        int lg = uri.length();
        if (lg == 0)
            return 0;
        int i = lg - 1;
        for (; i >= 1; i--) {
            ch = uri.charAt(i);
            if (notNameChar(ch)) break;
        }
        int j = i + 1;
        if (j >= lg)
            return lg;
        // Check we haven't split up a %-encoding.
        if (j >= 2 && uri.charAt(j - 2) == '%')
            j = j + 1;
        if (j >= 1 && uri.charAt(j - 1) == '%')
            j = j + 2;

        // Have found the leftmost NCNameChar from the
        // end of the URI string.
        // Now scan forward for an NCNameStartChar
        // The split must start with NCNameStart.
        for (; j < lg; j++) {
            ch = uri.charAt(j);
            //            if (XMLChar.isNCNameStart(ch))
            //                break ;
            if (XMLChar.isNCNameStart(ch)) {
                // "mailto:" is special.
                // Keep part after mailto: at least one charcater.
                // Do a quick test before calling .startsWith
                // OLD: if ( uri.charAt(j - 1) == ':' && uri.lastIndexOf(':', j - 2) == -1)
                if (j == 7 && uri.startsWith("mailto:"))
                    continue; // split "mailto:me" as "mailto:m" and "e" !
                else
                    break;
            }
        }

        return j;
    }

    /**
     * Checks whether <code>ch</code> is not a legal NCName character, i.e., it is a possible split-point start.
     *
     * @param ch
     *         Input character.
     * @return true if <code>ch</code> is not a legal NCName character; false otherwise.
     */
    public boolean notNameChar(char ch) {
        return !XMLChar.isNCName(ch);
    }

    /**
     * Checks whether the input URI does not contain illegal NCName characters.
     *
     * @param uri
     *         Input URI.
     * @return true if last character of <code>uri</code> is not illegal; false otherwise.
     */
    public boolean isNiceURI(String uri) {
        if (uri.equals("")) return false;
        char last = uri.charAt(uri.length() - 1);
        return !notNameChar(last);
    }

    /**
     * Load the prefixes from a file.
     *
     * @param file
     *         File with prefix mappings.
     */
    private void loadPrefixes(final File file) {
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(file), ',', '"');
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                prefixToURI.put(nextLine[0], nextLine[1]);
                URItoPrefix.put(nextLine[1], nextLine[0]);
            }
        } catch (FileNotFoundException e) {
            _log.error("File with prefix mappings not found.");
            e.printStackTrace();
        } catch (IOException e) {
            _log.error("Error while reading file with prefix mappings.");
            e.printStackTrace();
        }
    }

    /**
     * Expand a prefixed URI. There's an assumption that any URI of the form Head:Tail is subject to mapping if Head is
     * in the prefix mapping. So, if someone takes it into their heads to define eg "http" or "ftp" we have problems.
     *
     * @param prefixed
     *         Input prefixed URI. For example, <code>rdf:type</code>.
     * @return The expanded URI if prefix is found in the map; null otherwise.
     */
    public String expandPrefix(String prefixed) {
        int colon = prefixed.indexOf(':');
        if (colon < 0) {
            return prefixed;
        } else {
            String uri = prefixToURI.get(prefixed.substring(0, colon));
            return uri == null ? prefixed : uri + prefixed.substring(colon + 1);
        }
    }

    /**
     * Compress the URI using the prefix mapping. This version of the code looks through all the maplets and checks each
     * candidate prefix URI for being a leading substring of the argument URI. There's probably a much more efficient
     * algorithm available, pre-processing the prefix strings into some kind of search table, but for the moment we
     * don't need it.
     *
     * @param uri
     *         Input URI.
     * @return Shortened URI if the substring of the URI is found in the map; null otherwise.
     */
    public String shortForm(String uri) {
        Map.Entry<String, String> e = findMapping(uri, true);
        return e == null ? uri : e.getKey() + ":" + uri.substring((e.getValue()).length());
    }

    /**
     * Answer the qname for <code>uri</code> which uses a prefix from this mapping, or null if there isn't one.
     * <p/>
     * Relies on <code>splitNamespaceXML</code> to carve uri into namespace and localname components; this ensures that
     * the localname is legal and we just have to (reverse-)lookup the namespace in the prefix table.
     *
     * @param uri
     *         Input URI.
     * @return Shortened URI if the substring of the URI is found in the map; null otherwise.
     */
    public String qnameFor(String uri) {
        int split = splitNamespaceXML(uri);
        String ns = uri.substring(0, split), local = uri.substring(split);
        if (local.equals("")) return null;
        String prefix = URItoPrefix.get(ns);
        return prefix == null ? null : prefix + ":" + local;
    }

    /**
     * Answer a prefixToURI entry in which the value is an initial substring of <code>uri</code>. If
     * <code>partial</code> is false, then the value must be equal to <code>uri</code>.
     * <p/>
     * It does a linear search of the entire prefixToURI, so not terribly efficient for large maps.
     *
     * @param uri
     *         the value to search for
     * @param partial
     *         true if the match can be any leading substring, false for exact match
     * @return some entry (k, v) such that uri starts with v [equal for partial=false]
     */
    private Map.Entry<String, String> findMapping(String uri, boolean partial) {
        String ss;
        for (Map.Entry<String, String> e : prefixToURI.entrySet()) {
            ss = e.getValue();
            if (uri.startsWith(ss) && (partial || ss.length() == uri.length())) {
                return e;
            }
        }

        return null;
    }

}
