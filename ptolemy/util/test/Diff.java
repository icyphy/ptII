/* A simple diff utility, implemented in Java.

Copyright (C) 2000-2011, Robert Sedgewick and Kevin Wayne.
All rights reserved.
 */

package ptolemy.util.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ptolemy.util.FileUtilities;

/**
 *  Read two files and compute the diff.
 *  <p>Based on <a href="http://introcs.cs.princeton.edu/96optimization/Diff.java">http://introcs.cs.princeton.edu/96optimization/Diff.java</a>, see
 *  <a href="http://introcs.cs.princeton.edu/96optimization">http://introcs.cs.princeton.edu/96optimization</a>.
 *
 *  <p>Note that this code has a non-opensource copyright and should
 *  be
 *  <p>Limitations:</p>
 *  <ul>
 *   <li>"Could hash the lines to avoid potentially more expensive
 *     string comparisons."
 *  </ul>
 *
 * @author Robert Sedgewick and Kevin Wayne, Contributor: Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Diff {

    /** Return the difference between two strings.
     *  @param aString The first string to be compared.
     *  @param bString The secondString to be compared
     *  @return A string describing the difference between the two
     *  strings in a format similar to the Unix diff command.
     */
    public static String diff(String aString, String bString) {
        String eol = System.getProperty("line.separator");
        String[] aStringSplit = aString.split(eol);
        String[] bStringSplit = bString.split(eol);
        int aNumberOfLines = aStringSplit.length;
        int bNumberOfLines = bStringSplit.length;

        // Find the Longest Common Subsequence (LCS).
        int[][] lcs = new int[aNumberOfLines + 1][bNumberOfLines + 1];
        for (int i = aNumberOfLines - 1; i >= 0; i--) {
            for (int j = bNumberOfLines - 1; j >= 0; j--) {
                if (aStringSplit[i].equals(bStringSplit[j])) {
                    lcs[i][j] = lcs[i + 1][j + 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
        }

        // Traverse the LCS and append the differences.
        StringBuffer result = new StringBuffer();
        int i = 0, j = 0;
        while (i < aNumberOfLines && j < bNumberOfLines) {
            if (aStringSplit[i].equals(bStringSplit[j])) {
                i++;
                j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                result.append("< " + aStringSplit[i++] + eol);
            } else {
                result.append("> " + bStringSplit[j++] + eol);
            }
        }

        // Append the remainder of the longer file.
        while (i < aNumberOfLines || j < bNumberOfLines) {
            if (i == aNumberOfLines) {
                result.append("> " + aStringSplit[j++] + eol);
            } else if (j == bNumberOfLines) {
                result.append("< " + bStringSplit[i++] + eol);
            }
        }
        return result.toString();
    }

    /** Print the difference between two files.
     *  <p>Usage:</p>
     *  <pre>
     *  java -classpath $PTII ptolemy.util.test.Diff File1.txt File2.txt
     *  </pre>

     *  @param args An array of two elements, where
     *  the first element is the filename of the first
     *  file and the second element is the filename of
     *  the second file.
     *  @exception MalformedURLException If a file name cannot be converted
     *  into a URL.
     *  @exception IOException If a file cannot be read.
     */
    public static void main(String[] args) throws MalformedURLException,
    IOException {
        if (args.length != 2) {
            System.err.println("Error: number of arguments must be 2, "
                    + "not " + args.length + ".");
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.util.test.Diff File1.txt File2.txt");
        }
        // Read in each file
        URL urlA = new File(args[0]).toURI().toURL();
        URL urlB = new File(args[1]).toURI().toURL();

        System.out.println(diff(
                new String(FileUtilities.binaryReadURLToByteArray(urlA)),
                new String(FileUtilities.binaryReadURLToByteArray(urlB))));
    }
}
