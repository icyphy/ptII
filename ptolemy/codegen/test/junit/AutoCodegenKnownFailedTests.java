/* Run the Ptolemy model tests in the auto/knownFailedTests/ directory using codegen code generation under JUnit.

   Copyright (c) 2011-2012 The Regents of the University of California.
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

package ptolemy.codegen.test.junit;

import java.io.IOException;

import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// AutoCodegenKnownFailedTests

/**
 * Run the Ptolemy model tests in the auto/knownFailedTests/ directory
 * using codegen code generation under JUnit.
 * 
 * <p> This class provides common facilities used by classes that
 * generate C and Java code.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class AutoCodegenKnownFailedTests extends AutoCodegenTests {

    /**
     * Return a two dimensional array of arrays of strings that name
     * the model to be executed. If auto/knownFailesTests/ does not
     * exist, or does not contain files that end with .xml or .moml,
     * return a list with one element that contains a special string.
     * 
     * @return The List of model names in auto/
     * @exception IOException If there is a problem accessing the auto/ directory.
     */
    public Object[] modelValues() throws IOException {
        return modelValues("auto/knownFailedTests/", THERE_ARE_NO_KNOWN_FAILED_TESTS);
    }

    /** 
     *  Generate, compile and run code for a model known to fail.
     *  @param fullPath  
     *            The full path to the model file to be executed. If the
     *            fullPath ends with the value of the
     *            {@link #THERE_ARE_NO_AUTO_TESTS}, then the method returns
     *            immediately.
     *  @param generatorPackage Either "ptolemy.codegen.c" or "ptolemy.codegen.java"
     *  @param inline True of inline code is to be generated.
     *  @exception Throwable If there is a problem generating,
     *  compiling or running the code.
     */
    public void runModel(String fullPath, String generatorPackage,
            boolean inline)
            throws Throwable {
        if (fullPath.endsWith(THERE_ARE_NO_AUTO_TESTS)) {
            System.out.println("No auto/*.xml tests in "
                    + StringUtilities.getProperty("user.dir"));
            return;
        }
   
        // Delete the ~/codegen directory each time so that if the user generates code for
        // a model using -generateInSubdirectory, we can still have gcc generate an
        // executable with that name.
        if (!FileUtilities.deleteDirectory(_codegenDirectory)) {
            System.out.println("Warning, failed to delete " + _codegenDirectory);
        }

        System.out.println("----------------- (Known Failure) AutoCodegen $PTII/bin/ptcodegen "
                + "-generatorPackage " + generatorPackage
                + " -inline " + inline
                + " " + fullPath);
        String [] args = new String [] {
            "-generatorPackage", generatorPackage,
            "-inline", Boolean.toString(inline),
            fullPath};
        try {
            _generateCodeMethod.invoke(null, (Object) args);
        } catch (Throwable throwable) {
            System.out.println("Known Failure: " + throwable);
            throwable.printStackTrace();
        }
    }
}
