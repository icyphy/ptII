/* Run the Tcl tests under JUnit.

   Copyright (c) 2011 The Regents of the University of California.
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
import java.lang.reflect.Method;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;


import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

///////////////////////////////////////////////////////////////////
//// TclTests
/**
 * Run the Tcl tests under JUnit.
 * 
 * <p>
 * This test must be run from the directory that contains the auto/ directory,
 * for example:
 * </p>
 * 
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/io/test; java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.TclTests)
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
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class TclTests {

    /**
     * Call the Tcl doneTests command to print out the number of errors.
     * 
     * @exception Throwable
     *                If the class, constructor or method cannot be found. or if
     *                the Interp cannot be instantiated.
     */
    @AfterClass
    public static void doneTests() throws Throwable {
        
        // util/testsuite/testDefs.tcl doneTests tcl command checks
        // the value of the reallyExit tcl variable.  If reallyExit is
        // not present or 1, then ::tycho::TopLevel::exitProgram is
        // called.  We don't want that because it prints an error
        // message, so we set reallyExit to 0.
        _setVarMethod.invoke(_interp, new Object [] {"reallyExit",
                                                     _tclObjectZero,
                                                     1 /*TCL.GLOBAL_ONLY*/});

        // Invoke the doneTests Tcl command which prints the number of
        // tests.
        _evalMethod.invoke(_interp, new Object[] { "doneTests", 0 });
    }

    /**
     * Return a two dimensional array of arrays of strings that name the .tcl files
     * to be executed. If there are no .tcl files, return a list with one element that
     * has the value of the {@link #THERE_ARE_NO_TCL_TESTS} field.
     * 
     * @return The List of tcl tests.
     * @exception IOException If there is a problem accessing the auto/ directory.
     */
    public Object[] parametersForRunTclFile() throws IOException {
        String[] tclFiles = new File(".").list(new FilenameFilter() {
                /**
                 * Return true if the file name ends with .tcl and is not
                 * alljtests.tcl or testDefs.tcl
                 * 
                 * @param directory
                 *            Ignored
                 * @param name
                 *            The name of the file.
                 * @return true if the file name ends with .xml or .moml
                 */
                public boolean accept(File directory, String name) {
                    String fileName = name.toLowerCase();
                    if (fileName.endsWith(".tcl")) {
                        // alljsimpletests.tcl calls exit,
                        // which results in JUnit
                        // producing
                        // "junit.framework.AssertionFailedError:
                        // Forked Java VM exited
                        // abnormally. Please note the
                        // time in the report does not
                        // reflect the time until the VM
                        // exit."

                        if (!fileName.endsWith("alljsimpletests.tcl")
                                && !fileName.endsWith("alljtests.tcl")
                                && !fileName.endsWith("testdefs.tcl")) {
                            return true;
                        }
                    }
                    return false;
                }
            });

        if (tclFiles.length > 0) {
            int i = 0;
            Object[][] data = new Object[tclFiles.length][1];
            for (String tclFile : tclFiles) {
                data[i++][0] = new File(tclFile).getCanonicalPath();
            }
            return data;
        } else {
            return new Object[][] { { THERE_ARE_NO_TCL_TESTS } };
        }
    }

    /**
     * Run a tclFile.
     * 
     * @exception Throwable
     *                If thrown while executing the tclFile.
     * @param tclFile
     *            The full path to the .tcl file to be executed. If tclFile
     *            ends with the value of the {@link #THERE_ARE_NO_TCL_TESTS},
     *            then the method returns immediately.
     */
    @Test
    @Parameters
    public void RunTclFile(String tclFile) throws Throwable {
        if (tclFile.endsWith(THERE_ARE_NO_TCL_TESTS)) {
            System.out.println("No tcl tests in "
                    + System.getProperty("user.dir"));
            System.out.flush();
            return;
        }
        System.out.println(tclFile);
        System.out.flush();
        _evalFileMethod.invoke(_interp, new Object[] { tclFile });

        // Get the value of the Tcl FAILED global variable.
        // We check for non-zero results for *each* .tcl file.
        Object tclObject = _getVarMethod.invoke(_interp,
                new Object [] {
                    "FAILED", (String) null, 1 /*TCL.GLOBAL_ONLY*/
                });
        // If the Tcl FAILED global variable is not equal to 0, then
        // add a failure.
        int failed = Integer.parseInt(tclObject.toString());
        assertEquals("Number of failed tests is non-zero",
                0, failed);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The tcl.lang.Interp.eval(String, int) method. */
    private static Method _evalMethod;

    /** The tcl.lang.Interp.evalFile(String) method. */
    private static Method _evalFileMethod;

    /** The tcl.lang.Interp.getVar(String name1, String name2, int flags) method. */
    private static Method _getVarMethod;

    /**
     * The tcl.lang.Interp class. We use reflection here to avoid false
     * dependencies if auto/ does not exist.
     */
    private static Class _interpClass;

    /**
     * The tcl.lang.Interp object upon which we invoke evalFile(String).
     */
    private static Object _interp;

    /** The tcl.lang.Interp.setVar(String name1, String name2, int flags) method. */
    private static Method _setVarMethod;

    /**
     * The tcl.lang.TclObject class. We use reflection here to avoid false
     * dependencies if auto/ does not exist.
     */
    private static Class _tclObjectClass;

    /**
     * A tcl.lang.TclObject that has the integer value 0.
     * Used when we call the doneTests Tcl method.
     */
    private static Object _tclObjectZero;

    /**
     * A special string that is passed when there are no tcl tests. This is
     * necessary to avoid an exception in the JUnitParameters.
     */
    protected final static String THERE_ARE_NO_TCL_TESTS = "ThereAreNoTclTests";

    // We place initialization of the _interp in a static block so
    // that it happens once per directory of tcl files.  The doneTests() method
    // prints the number of test case failures for us.
    static {
        try {
            _interpClass = Class.forName("tcl.lang.Interp");
            _interp = _interpClass.newInstance();

            _evalMethod = _interpClass.getMethod("eval",
                    new Class [] {String.class, Integer.TYPE});

            _evalFileMethod = _interpClass.getMethod("evalFile", String.class);

            _getVarMethod = _interpClass.getMethod("getVar",
                    new Class [] {String.class, String.class, Integer.TYPE});

            _tclObjectClass = Class.forName("tcl.lang.TclObject");
            _setVarMethod = _interpClass.getMethod("setVar",
                    new Class [] {String.class, _tclObjectClass, Integer.TYPE});

            // Create a TclObject with value 0 for use with the doneTests Tcl proc.
            Class tclIntegerClass = Class.forName("tcl.lang.TclInteger");
            Method newInstanceTclIntegerMethod = tclIntegerClass.getMethod("newInstance",
                    new Class [] {Integer.TYPE});

            _tclObjectZero = newInstanceTclIntegerMethod.invoke(null, 
                    new Object [] {Integer.valueOf(0)});

        } catch (Throwable throwable) {
            // Exceptions sometimes get marked as multiple failures here so
            // we print the stack to aid debugging.
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

}