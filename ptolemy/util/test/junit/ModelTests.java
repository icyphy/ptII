/* Base class for tests that use models in the auto/ directory.

   Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.util.test.junit;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

///////////////////////////////////////////////////////////////////
//// ModelTests
/**
 * Base class for tests that use models in the auto/ directory.
 *
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ModelTests {

    /**
     * Return a two dimensional array of arrays of strings that name the model
     * to be executed. If auto/ does not exist, or does not contain files that
     * end with .xml or .moml, return a list with one element that contains
     * the value of the THERE_ARE_NO_AUTO_TESTS variable.
     *
     * @return The List of model names in auto/
     * @exception IOException If there is a problem accessing the auto/ directory.
     */
    public Object[] modelValues() throws IOException {
        return modelValues("auto/", THERE_ARE_NO_AUTO_TESTS);
    }

    /**
     * Return a two dimensional array of arrays of strings that name the model
     * to be executed. If directory does not exist, or does not contain files that
     * end with .xml or .moml, return a list with the value of the message parameter.
     *
     * @param directory The directory, which is usually either "auto/" or
     * "auto/knownFailedTests/".
     * @param message The message to use if the directory contains no
     * *.xml or *.moml files.
     * @return The List of model names in the directory.
     * @exception IOException If there is a problem accessing the directory.
     */
    public Object[] modelValues(String directory, String message)
            throws IOException {
        File auto = new File(directory);
        if (auto.isDirectory()) {
            String[] modelFiles = auto.list(new FilenameFilter() {
                /**
                 * Return true if the file name ends with .xml or .moml
                 *
                 * @param directory
                 *            Ignored
                 * @param name
                 *            The name of the file.
                 * @return true if the file name ends with .xml or .moml
                 */
                @Override
                public boolean accept(File directory, String name) {
                    String fileName = name.toLowerCase(Locale.getDefault());
                    return fileName.endsWith(".xml")
                            || fileName.endsWith(".moml");
                }
            });
            int i = 0;
            Object[][] data = new Object[modelFiles.length][1];
            if (modelFiles.length > 0) {
                for (String modelFile : modelFiles) {
                    data[i++][0] = new File(directory + modelFile)
                    .getCanonicalPath();
                }
                // Sort the files so that we execute the tests in
                // a predictable order.  Tests in ptolemy/actor/lib/test/auto
                // need this.  File.list() returns files in a different order
                // on different platforms.  So much for write once, run everywhere.
                Arrays.sort(data, new Comparator<Object[]>() {
                    @Override
                    public int compare(final Object[] entry1,
                            final Object[] entry2) {
                        final String file1 = (String) entry1[0];
                        final String file2 = (String) entry2[0];
                        return file1.compareTo(file2);
                    }
                });
                return data;
            } else {
                return new Object[][] { { message } };
            }
        }
        return new Object[][] { { message } };
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The application class. We use reflection here to avoid false dependencies
     * if auto/ does not exist.
     * In derived classes, _applicationClass should be initialized in right
     * before it is to be used.  It should not be initialized in setup()
     * so that we can avoid dependencies on MoMLSimpleApplication.
     */
    protected static Class<?> _applicationClass = null;

    /** The application constructor. */
    protected static Constructor _applicationConstructor;

    /**
     * A special string that is passed when there are no known failed tests.
     * This is necessary to avoid an exception in the JUnitParameters.
     */
    protected final static String THERE_ARE_NO_AUTO_TESTS = "ThereAreNoAutoTests";

    /**
     * A special string that is passed when there are no known failed tests.
     * This is necessary to avoid an exception in the JUnitParameters.
     */
    protected final static String THERE_ARE_NO_KNOWN_FAILED_TESTS = "ThereAreNoKnowFailedTests";

    static {
        // ptolemy.actor.lib.test.NonStrictTest checks isRunningNightlyBuild and
        // throws an exception if trainingMode is true.
        System.setProperty("ptolemy.ptII.isRunningNightlyBuild", "true");

        // ptolemy.util.StringUtilities.exit() checks ptolemy.ptII.doNotExit.
        System.setProperty("ptolemy.ptII.doNotExit", "true");
    }
}
