/* A simple diff utility, implemented in Java.

 Copyright (c) 2011-2017 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */

package ptolemy.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ptolemy.util.FileUtilities;

/**
 *  Read two files and compute the diff.
 *
 *  <p>This file is based on <a href="http://introcs.cs.princeton.edu/96optimization/Diff.java#in_browser">http://introcs.cs.princeton.edu/96optimization/Diff.java</a>, from 2011, see
 *  <a href="http://introcs.cs.princeton.edu/96optimization/#in_browser">http://introcs.cs.princeton.edu/96optimization</a>.
 *  A current copy may be found at <a href="http://introcs.cs.princeton.edu/java/23recursion/Diff.java.html#in_browser">http://introcs.cs.princeton.edu/java/23recursion/Diff.java.html</a>
 *  
 *
 * @author Christopher Brooks
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
        String systemEol = System.getProperty("line.separator");
        // Since a string my be loaded from a file
        // that was saved on a different platform we must
        // allow any valid line separator to split the string
        String eol = "\r\n?|\n";
        String[] aStringSplit = aString.split(eol);
        String[] bStringSplit = bString.split(eol);
        int aNumberOfLines = aStringSplit.length;
        int bNumberOfLines = bStringSplit.length;

        // Find the Longest Common Subsequence (LCS).
        int[][] lcs = new int[aNumberOfLines + 1][bNumberOfLines + 1];
        for (int aIndex = 1; aIndex < aNumberOfLines; aIndex++) {
            for (int bIndex = 1; bIndex < bNumberOfLines; bIndex++) {
                if (aStringSplit[aIndex].equals(bStringSplit[bIndex])) {
                    lcs[aIndex][bIndex] = lcs[aIndex - 1][bIndex - 1] + 1;
                } else {
                    lcs[aIndex][bIndex] = Math.max(lcs[aIndex][bIndex - 1], lcs[aIndex - 1][bIndex]);
                }
            }
        }

        // Traverse the LCS and append the differences.
        StringBuffer result = new StringBuffer();
        int aIndex = 0;
        int bIndex = 0;
        while (aIndex < aNumberOfLines && bIndex < bNumberOfLines) {
            if (aStringSplit[aIndex].equals(bStringSplit[bIndex])) {
                aIndex++;
                bIndex++;
            } else if (lcs[aIndex + 1][bIndex] < lcs[aIndex][bIndex + 1]) {
                result.append("> " + bStringSplit[bIndex++] + systemEol);
            } else {
                result.append("< " + aStringSplit[aIndex++] + systemEol);
            }
        }

        // Append the remainder of the longer file.
        while (aIndex < aNumberOfLines || bIndex < bNumberOfLines) {
            if (aIndex == aNumberOfLines) {
                result.append("> " + bStringSplit[bIndex++] + systemEol);
            } else if (bIndex == bNumberOfLines) {
                result.append("< " + aStringSplit[aIndex++] + systemEol);
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

        System.out.print(diff(
                              new String(FileUtilities.binaryReadURLToByteArray(urlA)),
                              new String(FileUtilities.binaryReadURLToByteArray(urlB))));
    }
}
