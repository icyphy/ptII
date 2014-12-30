/**
 *
 * Copyright (c) 2003-2014 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package ptolemy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Manage the resources for a locale using a set of static strings from a property file.
 * See <code>java.util.ResourceBundle</code> for more information.
 *
 * <p>
 * Unlike other types of resource bundle, <code>OrderedResourceBundle</code> is not
 * usually subclassed. Instead, the properties files containing the resource data
 * are supplied. <code>OrderedResourceBundle.getBundle</code>
 * will automatically look for the appropriate properties file and create an
 * <code>OrderedResourceBundle</code> that refers to it. See
 * <code>java.util.ResourceBundle.getBundle()</code> for a complete description
 * of the search and instantiation strategy.
 *
 * @version $Id$
 * @author Matthew Brooke
 * @since Ptolemy II 8.0
 */
public class OrderedResourceBundle {

    /**
     * Construct an OrderedResourceBundle.
     *
     * @param stream
     *            InputStream for reading the java properties file from which
     *            this object will take its values.  The stream is closed
     *            by this constructor.
     * @exception IOException
     *             if there is a problem reading the InputStream
     * @exception NullPointerException
     *             if the InputStream is null
     */
    public OrderedResourceBundle(InputStream stream) throws IOException,
    NullPointerException {

        if (stream == null) {
            throw new NullPointerException(
                    "OrderedResourceBundle constructor received a NULL InputStream");
        }
        BufferedReader propsReader = new BufferedReader(new InputStreamReader(
                stream));

        // This method closes propsReader
        orderedMap = getPropsAsOrderedMap(propsReader);
    }

    ///////////////////////////////////////////////////////////////////
    //                                              public methods

    /**
     * Get a resource bundle using the specified base name and the default
     * locale. The returned bundle has its entries in the same order as those in
     * the original properties file, so a call to getKeys() will return an
     * Iterator that allows retrieval of the keys in the original order. See
     * javadoc for <code>java.util.ResourceBundle</code> for a complete
     * description of the search and instantiation strategy.
     *
     * @param baseName
     *            String denoting the name of the properties file that will be
     *            read to populate this ResourceBundle.<br>
     *            <br> Example 1: if the baseName is MyPropsFile, and the
     *            default Locale is en_US, a properties file named:
     *            <code>MyPropsFile_en_US.properties</code> will be sought on
     *            the classpath.<br>
     *            <br> Example 2: if the baseName is
     *            org.mydomain.pkg.MyPropsFile, and the default Locale is en_US,
     *            a properties file named:
     *            <code>org/mydomain/pkg/MyPropsFile_en_US.properties</code>
     *            will be sought on the classpath.<br>
     *            <br> NOTE: valid comment chars are # and !<br>
     *            <br> valid delimiters are (space) : =
     * @return OrderedResourceBundle - a ResourceBundle with its entries in the
     *         same order as those in the original properties file
     * @exception IOException
     *             if there is a problem reading the file
     * @exception MissingResourceException
     *             if the file cannot be found
     * @exception NullPointerException
     *             if baseName is null
     */
    public static OrderedResourceBundle getBundle(String baseName)
            throws IOException, MissingResourceException, NullPointerException {

        String filename = getPropsFileNamePlusLocale(baseName);

        InputStream stream = OrderedResourceBundle.class
                .getResourceAsStream(filename);

        // The OrderedResourceBundle closes stream.
        return new OrderedResourceBundle(stream);
    }

    /**
     * Get a string for the given key from this resource bundle.
     *
     * @param key
     *            the key for the desired string
     * @return the string for the given key, or null if: a null value is
     *         actually mapped to this key, key not found, or key is null
     */
    public String getString(String key) {
        return (String) orderedMap.get(key);
    }

    /**
     * Get an Iterator over the Set of keys, allowing retrieval of the keys in
     * the original order as listed in the properties file.
     *
     * @return Iterator
     */
    public Iterator getKeys() {
        return orderedMap.keySet().iterator();
    }

    ///////////////////////////////////////////////////////////////////
    //                           private methods

    /** Get the properties as an ordered map.
     *         @param propsReader The reader that contains the properties.  This method
     *         closes propsReader upon completion
     *  @return The properties.
     */
    private LinkedHashMap getPropsAsOrderedMap(BufferedReader propsReader)
            throws IOException {

        LinkedHashMap orderedMap = new LinkedHashMap();
        try {
            String readLine = null;

            while ((readLine = propsReader.readLine()) != null) {

                readLine = readLine.trim();

                if (readLine.length() < 1) {
                    continue;
                }

                // Find start of key
                int lineLen = readLine.length();
                int keyStart;
                for (keyStart = 0; keyStart < lineLen; keyStart++) {
                    if (whiteSpaceChars.indexOf(readLine.charAt(keyStart)) == -1) {
                        break;
                    }
                }

                // Continue lines that end in slashes if they are not comments
                char firstChar = readLine.charAt(keyStart);
                if (firstChar != '#' && firstChar != '!') {
                    while (continueLine(readLine)) {
                        String nextLine = propsReader.readLine();
                        if (nextLine == null) {
                            nextLine = "";
                        }
                        String choppedLine = readLine.substring(0, lineLen - 1);
                        // Advance beyond whitespace on new line
                        int startIndex;
                        for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
                            if (whiteSpaceChars.indexOf(nextLine
                                    .charAt(startIndex)) == -1) {
                                break;
                            }
                        }
                        nextLine = nextLine.substring(startIndex,
                                nextLine.length());
                        readLine = choppedLine + nextLine;
                        lineLen = readLine.length();
                    }

                    // Find separation between key and value
                    int sepIdx;
                    for (sepIdx = keyStart; sepIdx < lineLen; sepIdx++) {
                        char currentChar = readLine.charAt(sepIdx);
                        if (currentChar == '\\') {
                            sepIdx++;
                        } else if (keyValueSeparators.indexOf(currentChar) != -1) {
                            break;
                        }
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex = sepIdx; valueIndex < lineLen; valueIndex++) {
                        if (whiteSpaceChars
                                .indexOf(readLine.charAt(valueIndex)) == -1) {
                            break;
                        }
                    }

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < lineLen) {
                        if (strictKeyValueSeparators.indexOf(readLine
                                .charAt(valueIndex)) != -1) {
                            valueIndex++;
                        }
                    }
                    // Skip over white space after other separators if any
                    while (valueIndex < lineLen) {
                        if (whiteSpaceChars
                                .indexOf(readLine.charAt(valueIndex)) == -1) {
                            break;
                        }
                        valueIndex++;
                    }
                    String nextKey = readLine.substring(keyStart, sepIdx);
                    String nextVal = sepIdx < lineLen ? readLine.substring(
                            valueIndex, lineLen) : "";
                            orderedMap.put(unescape(nextKey), unescape(nextVal));
                }
            }
        } finally {
            try {
                if (propsReader != null) {
                    propsReader.close();
                }
            } catch (IOException ce) {
            }
        }
        return orderedMap;
    }

    // un-escape all the escaped standard delimiters and comment chars, if any
    // exist. Works for " " : # = !
    private String unescape(String line) {

        line = line.replaceAll("\\\\ ", " ");
        line = line.replaceAll("\\\\:", ":");
        line = line.replaceAll("\\\\#", "#");
        line = line.replaceAll("\\\\=", "=");
        line = line.replaceAll("\\\\!", "!");

        return line;
    }

    /*
     * Returns true if the given line is a line that must be appended to the
     * next line
     */
    private boolean continueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while (index >= 0 && line.charAt(index--) == '\\') {
            slashCount++;
        }
        // FindBugs: The code uses x % 2 == 1 to check to see if a value is odd,
        // but this won't work for negative numbers (e.g., (-5) % 2 == -1).
        // If this code is intending to check for oddness, consider
        // using x & 1 == 1, or x % 2 != 0.

        return slashCount % 2 != 0;
    }

    private static String getPropsFileNamePlusLocale(String baseName)
            throws MissingResourceException, NullPointerException {

        if (baseName == null) {
            return null;
        }
        baseName = baseName.trim();

        if (baseName.length() < 1) {
            return baseName;
        }

        // use ResourceBundle's code to find the properties file's locale
        // - ie the last part of its name - such as the "_en_US" at the end
        // of "mypropsfile_en_US.properties"
        Locale bundleLocale = ResourceBundle.getBundle(baseName).getLocale();

        String lang = bundleLocale.getLanguage();
        String ctry = bundleLocale.getCountry();
        String vart = bundleLocale.getVariant();

        boolean hasLang = lang.length() > 0;
        boolean hasCtry = ctry.length() > 0;
        boolean hasVart = vart.length() > 0;

        baseName = baseName.replace('.', '/');

        if (!baseName.startsWith(FWD_SLASH)) {
            baseName = FWD_SLASH + baseName;
        }

        StringBuffer fnBuff = new StringBuffer(baseName);

        if (!hasLang && !hasCtry && !hasVart) {
            fnBuff.append(PROPS_EXT);
            return fnBuff.toString();
        }

        if (hasLang) {
            fnBuff.append(UNDERSCORE);
            fnBuff.append(lang);
        }
        if (hasCtry) {
            fnBuff.append(UNDERSCORE);
            fnBuff.append(ctry);
        }
        if (hasVart) {
            fnBuff.append(UNDERSCORE);
            fnBuff.append(vart);
        }
        fnBuff.append(PROPS_EXT);

        return fnBuff.toString();
    }

    ///////////////////////////////////////////////////////////////////
    // private variables

    private final static String FWD_SLASH = "/";
    private final static String UNDERSCORE = "_";
    private final static String PROPS_EXT = ".properties";

    private static final String whiteSpaceChars = " \t\r\n\f";
    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String strictKeyValueSeparators = "=:";

    private LinkedHashMap orderedMap;
}
