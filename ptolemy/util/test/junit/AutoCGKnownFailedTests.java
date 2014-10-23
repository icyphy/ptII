/* Run the Ptolemy model tests in the auto/knownFailedTests/ directory using cg code generation under JUnit.

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

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// AutoCGKnownFailedTests

/**
 * Run the Ptolemy model tests in the auto/knownFailedTests/ directory using cg code generation
 * under JUnit.
 *
 * <p>
 * This class provides common facilities used by classes that generate
 * C and Java code.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class AutoCGKnownFailedTests extends AutoCGTests {

    /**
     * Return a two dimensional array of arrays of strings that name
     * the model to be executed. If auto/knownFailedTests/ does not
     * exist, or does not contain files that end with .xml or .moml,
     * return a list with one element that contains a special string.
     *
     * @return The List of model names in auto/
     * @exception IOException If there is a problem accessing the auto/ directory.
     */
    @Override
    public Object[] modelValues() throws IOException {
        return modelValues("auto/knownFailedTests/",
                THERE_ARE_NO_KNOWN_FAILED_TESTS);
    }

    /**
     *  Generate, compile and run code for a model known to fail.
     *  @param fullPath
     *            The full path to the model file to be executed. If the
     *            fullPath ends with the value of the
     *            {@link #THERE_ARE_NO_AUTO_TESTS}, then the method returns
     *            immediately.
     *  @param language Either "c" or "java".
     *  @param generateInSubdirectory If true, then generate the code in
     *  in a subdirectory of ~/cg/.
     *  @param inline If true, then generate inline code.
     *  @param maximumLinesPerBlock The maximum number of line of code generated
     *  per block
     *  @param variablesAsArrays If true, then try to save space by putting variables
     *  into arrays.
     *  @param generatorPackageList A semicolon or * separated list of
     *  Java packages to be searched for adapters.  For example,
     *  generic.program.procedural.c.arduino means use the arduino
     *  @exception Throwable If thrown while generating, compiling or executing the compiled code.
     */
    @Override
    public void runModel(String fullPath, String language,
            boolean generateInSubdirectory, boolean inline,
            int maximumLinesPerBlock, boolean variablesAsArrays,
            String generatorPackageList) throws Throwable {
        if (fullPath.endsWith(THERE_ARE_NO_KNOWN_FAILED_TESTS)) {
            System.out.println("No auto/*.xml tests in "
                    + StringUtilities.getProperty("user.dir"));
            return;
        }

        // Delete the ~/cg directory each time so that if the user generates code for
        // a model using -generateInSubdirectory, we can still have gcc generate an
        // executable with that name.
        if (!FileUtilities.deleteDirectory(_cgDirectory)) {
            System.out.println("Warning, failed to delete " + _cgDirectory);
        }

        LinkedList<String> argumentsList = new LinkedList<String>(
                Arrays.asList("-language", language, "-generateInSubdirectory",
                        Boolean.toString(generateInSubdirectory), "-inline",
                        Boolean.toString(inline), "-maximumLinesPerBlock",
                        Integer.toString(maximumLinesPerBlock),
                        "-variablesAsArrays",
                        Boolean.toString(variablesAsArrays)));
        if (generatorPackageList != null && generatorPackageList.length() > 0) {
            argumentsList.add("-generatorPackageList");
            argumentsList.add(generatorPackageList);
        }
        argumentsList.add(fullPath);

        String[] args = argumentsList.toArray(new String[argumentsList.size()]);

        System.out
                .print("----------------- (Known Failure) AutoCG $PTII/bin/ptcg");
        for (int i = 0; i < args.length; i++) {
            System.out.print(" " + args[i]);
        }

        try {
            int returnValue = ((Integer) _generateCodeMethod.invoke(null,
                    (Object) args)).intValue();
            if (returnValue != 0) {
                System.out
                .println("Known Failure: Return value of the last command executed was not zero, it was: "
                        + returnValue);
            }
        } catch (Throwable throwable) {
            System.out.println("Known Failure: " + throwable);
            throwable.printStackTrace();
        }
    }
}
