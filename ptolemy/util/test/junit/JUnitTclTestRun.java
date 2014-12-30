/* Run Tcl Tests using JUnit

 Copyright (c) 2010-2011 The Regents of the University of California.
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

import tcl.lang.Interp;
import tcl.lang.TclException;

///////////////////////////////////////////////////////////////////
//// JUnitTclTestRun
/**
 * Run the Tcl tests under JUnit.
 *
 * <p>If the fileName JVM property is set, then the file named by that property is
 * sourced. Otherwise, the testDefs.tcl file is sourced and the doallTests Tcl
 * proc that is defined in $PTII/util/testsuite/testDefs.tcl is invoked and then
 * any models in the auto/ directory are invoked.</p>
 *
 * <p>To run one test file (NamedObj.tcl):</p>
 * <pre>
 * cd $PTII/ptolemy/kernel/util/test
 * java -DfileName=NamedObj.tcl -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.JUnitTclTest
 * </pre>
 *
 * <p>To run all the .tcl files:</p>
 * <pre>
 * cd $PTII/ptolemy/kernel/util/test
 * java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.kernel.util.test.JUnitTclTest
 * </pre>
 *
 * <p>A copy of this file appears in each test/ subdirectory so that
 * it is easy for developers to run tests. The master file is in
 * $PTII/util/testsuite/JUnitTclTest.java.in. To update all the files,
 * run updateJUnitTclTest.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class JUnitTclTestRun {
    /**
     * Run a test.
     * <p>
     * If the fileName JVM property is set, then the file named by that property
     * is sourced. Otherwise, the testDefs.tcl file is sourced and the
     * doallTests Tcl proc that is defined in $PTII/util/testsuite/testDefs.tcl
     * is invoked and then any models in the auto/ directory are invoked.
     *
     * @exception TclException
     *                If thrown while evaluating the Tcl test code.
     */
    @org.junit.Test
    public void run() throws TclException {
        String fileName = System.getProperty("fileName");
        Interp interp = new Interp();
        if (fileName != null) {
            interp.evalFile(fileName);
        } else {
            if (!new File("testDefs.tcl").exists()) {
                // We might be running from a different directory
                String directory = getClass().getPackage().getName()
                        .replace('.', '/');
                if (new File(directory + "/testDefs.tcl").exists()) {
                    System.out.println(directory + "/testDefs.tcl exists");
                    interp.eval("cd " + directory);
                }
            }
            interp.evalFile("testDefs.tcl");
            interp.eval("doAllTests");
        }
    }
}
