/* Run the Ptolemy model tests in the auto/knownFailedTests/ directory using JUnit.

   Copyright (c) 2011-2013 The Regents of the University of California.
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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

///////////////////////////////////////////////////////////////////
//// AutoKnownFailedTests
/**
 * Run the Ptolemy model tests in the auto/knownFailedTest/ directory using
 * JUnit.
 *
 * <p>
 * This test must be run from the directory that contains the
 * auto/knownFailedTests/ directory, for example:
 * </p>
 *
 * <pre>
 * (cd $PTII/ptolemy/actor/lib/net/test; java -classpath ${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.AutoKnownFailedTests)
 * </pre>
 *
 * <p>
 * This test uses JUnitParams from <a
 * href="http://code.google.com/p/junitparams/#in_browser"
 * >http://code.google.com/p/junitparams/</a>, which is released under <a
 * href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License
 * 2.0</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class AutoKnownFailedTests extends ModelKnownFailedTests {

    /**
     * Execute a model.
     *
     * @param fullPath
     *            The full path to the model file to be executed. If the
     *            fullPath ends with the value of the
     *            {@link #THERE_ARE_NO_KNOWN_FAILED_TESTS}, then the method
     *            returns immediately.
     * @exception Throwable
     *                If thrown while executing the model.
     */
    @Test
    @Parameters(method = "modelValues")
    public void RunModel(String fullPath) throws Throwable {
        if (fullPath.endsWith(THERE_ARE_NO_KNOWN_FAILED_TESTS)) {
            System.out.println("No auto/knownFailedTests/*.xml tests in "
                    + System.getProperty("user.dir"));
            System.out.flush();
            return;
        }
        System.out.println("----------------- testing (KnownFailure) "
                + fullPath);
        System.out.flush();
        try {
            if (_applicationConstructor == null) {
                // Delay instantiating MoMLSimpleApplication so that we
                // can run the kernel tests without requiring moml
                _applicationClass = Class
                        .forName("ptolemy.moml.MoMLSimpleApplication");
                _applicationConstructor = _applicationClass
                        .getConstructor(String.class);

            }
            _applicationConstructor.newInstance(fullPath);
        } catch (Throwable throwable) {
            System.out.println("Known Failure: " + throwable);
            throwable.printStackTrace();
        }
    }
}
