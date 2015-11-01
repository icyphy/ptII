/* Run the Ptolemy model tests in the auto/ directory using Functional Mock-up Interface Master Algorithm (FMIMA) code generation under JUnit.

 Copyright (c) 2015 The Regents of the University of California.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

///////////////////////////////////////////////////////////////////
//// AutoCGFMIMANoInlineTests

/**
 * Run the Ptolemy model tests in the auto/ directory using Functional
 * Mock-up Interface Master Algorithm (FMIMA) code generation under
 * JUnit.
 *
 * <p>This test must be run from the directory that contains the auto/ directory,
 * for example:
 * </p>
 *
 * <pre>
 * (cd $PTII/ptolemy/cg/kernel/generic/program/procedural/fmima/test/; java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.4.0.jar:${PTII}/lib/jna-4.0.0-variadic.jar  org.junit.runner.JUnitCore ptolemy.cg.kernel.generic.program.procedural.fmima.test.junit.JUnitCGFMIMATest)
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
public class AutoCGFMIMANoInlineTests extends AutoCGTests {

    /**
     * Find the ptolemy.cg.kernel.generic.GenericCodeGenerator class
     * and its generateCode static method that takes an array of
     * strings.
     *
     * @exception Throwable
     *                If the class or constructor cannot be found.
     */
    @Override
    @Before
    public void setUp() throws Throwable {
        super.setUp();
    }

    /**
     * Generate, compile and run non-inline code for a model.
     *
     * @param fullPath The full path to the model file to be
     * executed. If the fullPath ends with the value of the {@link
     * #THERE_ARE_NO_AUTO_TESTS}, then the method returns
     * immediately.
     * @exception Throwable If thrown while executing the model.
     */
    @Test
    @Parameters(method = "modelValues")
    public void runModelNoInline(String fullPath) throws Throwable {
        runModel(fullPath, "fmima", false /* generateInSubdirectory */,
                false /* inline */, 2500 /* maximumLinesPerBlock */,
                false /*variablesAsArrays*/, "" /*generatorPackageList*/);
    }
}
