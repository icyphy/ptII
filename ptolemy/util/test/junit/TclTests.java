/* Run the Tcl tests under JUnit.

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.AfterClass;
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
 * @since Ptolemy II 10.0
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

        if (_tclFileCount == 0) {
            // No .tcl files were found, so testDefs.tcl, which
            // defines the Tcl doneTests command has *not* been
            // sourced.  So, we return.
            return;
        }

        // util/testsuite/testDefs.tcl doneTests tcl command checks
        // the value of the reallyExit tcl variable.  If reallyExit is
        // not present or 1, then ::tycho::TopLevel::exitProgram is
        // called.  We don't want that because it prints an error
        // message, so we set reallyExit to 0.
        _setVarMethod.invoke(_interp, new Object[] { "reallyExit",
                _tclObjectZero, 1 /*TCL.GLOBAL_ONLY*/});

        // Invoke the doneTests Tcl command which prints the number of
        // tests.
        try {
            _evalMethod.invoke(_interp, new Object[] { "doneTests", 0 });
        } catch (Throwable throwable) {
            if (!_tclExceptionClass.isInstance(throwable.getCause())) {
                throw throwable;
            } else {
                Integer completionCode = (Integer) _getCompletionCodeMethod
                        .invoke(throwable.getCause(), new Object[] {});
                if (completionCode.intValue() == 1 /** TCL.ERROR */
                        ) {
                    // The completion code was 1, which means that the
                    // command could not be completed successfully.

                    // The Tcl errorInfo global variable will have information
                    // about what went wrong.
                    Object errorInfoTclObject = _getVarMethod.invoke(_interp,
                            new Object[] { "errorInfo", null, 1 /*TCL.GLOBAL_ONLY*/
                    });
                    throw new Exception(
                            "Evaluating the Tcl method \"doneTests\" "
                                    + "resulted in a TclException being thrown.\nThe Tcl "
                                    + "errorInfo global variable has the value:\n"
                                    + errorInfoTclObject);
                }
            }
        }
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
            @Override
            public boolean accept(File directory, String name) {
                String fileName = name.toLowerCase(Locale.getDefault());
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
            // Sort the tcl files so that _Configuration.tcl is first
            // in ptolemy/actor/gui/test
            // File.list() returns files in a different order
            // on different platforms.  So much for write once, run everywhere.
            Arrays.sort(data, new Comparator<Object[]>() {
                @Override
                public int compare(final Object[] entry1, final Object[] entry2) {
                    final String file1 = (String) entry1[0];
                    final String file2 = (String) entry2[0];
                    return file1.compareTo(file2);
                }
            });
            return data;
        } else {
            return new Object[][] { { THERE_ARE_NO_TCL_TESTS } };
        }
    }

    /**
     * Run a tclFile.

     * <p>Timeout after 480000 ms.  The
     * ptolemy/cg/kernel/generic/program/procedural/java/test/AutoAdapter.tcl
     * test requires more than 240 seconds.</p>
     *
     * @param tclFile
     *            The full path to the .tcl file to be executed. If tclFile
     *            ends with the value of the {@link #THERE_ARE_NO_TCL_TESTS},
     *            then the method returns immediately.
     * @exception Throwable If thrown while executing the tclFile.
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
        // Keep track of the number of Tcl files evaluated
        // If 1 or more files were evaluated, then we call doneTests.
        TclTests._incrementTclFileCount();

        System.out.println(tclFile);
        System.out.flush();
        try {
            _evalFileMethod.invoke(_interp, new Object[] { tclFile });
        } catch (Throwable throwable) {
            if (!_tclExceptionClass.isInstance(throwable.getCause())) {
                throw throwable;
            } else {
                Integer completionCode = (Integer) _getCompletionCodeMethod
                        .invoke(throwable.getCause(), new Object[] {});
                if (completionCode.intValue() == 1 /** TCL.ERROR */
                        ) {
                    // The completion code was 1, which means that the
                    // command could not be completed successfully.

                    // The Tcl errorInfo global variable will have information
                    // about what went wrong.
                    Object errorInfoTclObject = null;
                    try {
                        errorInfoTclObject = _getVarMethod.invoke(_interp,
                                new Object[] { "errorInfo", null, 1 /*TCL.GLOBAL_ONLY*/
                        });
                        throw new Exception(
                                "Evaluating the Tcl file \""
                                        + tclFile
                                        + "\"resulted in a TclException being thrown.\nThe Tcl "
                                        + "errorInfo global variable has the value:\n"
                                        + errorInfoTclObject);
                    } catch (Throwable throwable2) {
                        throwable2.printStackTrace();
                        throw new Exception(
                                "Evaluating the Tcl file \""
                                        + tclFile
                                        + "\"resulted in a TclException being thrown.\n"
                                        + "The Tcl errorInfo variable could not be obtained.\n"
                                        + throwable2, throwable);
                    }
                }
            }
        }

        // Get the value of the Tcl FAILED global variable.
        // We check for non-zero results for *each* .tcl file.
        Object newFailedCountTclObject = _getVarMethod.invoke(_interp,
                new Object[] { "FAILED", null, 1 /*TCL.GLOBAL_ONLY*/
        });
        int newFailed = Integer.parseInt(newFailedCountTclObject.toString());
        int lastFailed = Integer.parseInt(_failedTestCount.toString());

        // We only report if the number of test failures has increased.
        // this prevents us from reporting cascading errors if the
        // first .tcl file has a failure.
        TclTests._setFailedTestCount(_newInstanceTclIntegerMethod.invoke(null,
                new Object[] { Integer.valueOf(newFailed) }));

        // If the Tcl FAILED global variable is not equal to the
        // previous number of failures, then add a failure.
        assertEquals("Number of failed tests is has increased.", lastFailed,
                newFailed);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Increment the count of the number of tcl files visited.
     * Keep track of the number of Tcl files evaluated
     * If 1 or more files were evaluated, then we call doneTests.
     */
    protected static void _incrementTclFileCount() {
        // To avoid FindBugs: Write to static field from instance method
        _tclFileCount++;
    }

    /** Set the cached value of the count of the number of failed tests.
     *  @param count The object representing the number of failed tests.
     */
    protected static void _setFailedTestCount(Object count) {
        // To avoid FindBugs: Write to static field from instance method
        _failedTestCount = count;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * A special string that is passed when there are no tcl tests. This is
     * necessary to avoid an exception in the JUnitParameters.
     */
    protected final static String THERE_ARE_NO_TCL_TESTS = "ThereAreNoTclTests";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The tcl.lang.Interp.eval(String, int) method. */
    private static Method _evalMethod;

    /** The tcl.lang.Interp.evalFile(String) method. */
    private static Method _evalFileMethod;

    /** The number of failed tests.  Each .tcl file tests to see if
     * the number has increased.
     */
    private static Object _failedTestCount;

    /** The tcl.lang.TclException.getCompletionCode() method. */
    private static Method _getCompletionCodeMethod;

    /** The tcl.lang.Interp.getVar(String name1, String name2, int flags) method. */
    private static Method _getVarMethod;

    /**
     * The tcl.lang.Interp class. We use reflection here to avoid false
     * dependencies if auto/ does not exist.
     */
    private static Class<?> _interpClass;

    /**
     * The tcl.lang.Interp object upon which we invoke evalFile(String).
     */
    private static Object _interp;

    /** The newInstance() method of the tcl.lang.TclInteger class. */
    private static Method _newInstanceTclIntegerMethod;

    /** The tcl.lang.Interp.setVar(String name1, String name2, int flags) method. */
    private static Method _setVarMethod;

    /** The tcl.lang.TclException class. **/
    private static Class<?> _tclExceptionClass;

    /** Keep track of the number of Tcl files evaluated
     * If 1 or more files were evaluated, then we call doneTests.
     */
    private static int _tclFileCount = 0;

    /**
     * The tcl.lang.TclObject class. We use reflection here to avoid false
     * dependencies if auto/ does not exist.
     */
    private static Class<?> _tclObjectClass;

    /**
     * A tcl.lang.TclObject that has the integer value 0.
     * Used when we call the doneTests Tcl method.
     */
    private static Object _tclObjectZero;

    // We place initialization of the _interp in a static block so
    // that it happens once per directory of tcl files.  The doneTests() method
    // prints the number of test case failures for us.
    static {
        try {
            // ptolemy.actor.lib.test.NonStrictTest checks isRunningNightlyBuild and
            // throws an exception if trainingMode is true.
            System.setProperty("ptolemy.ptII.isRunningNightlyBuild", "true");

            // ptolemy.util.StringUtilities.exit() checks ptolemy.ptII.doNotExit.
            System.setProperty("ptolemy.ptII.doNotExit", "true");

            _interpClass = Class.forName("tcl.lang.Interp");
            _interp = _interpClass.newInstance();

            _evalMethod = _interpClass.getMethod("eval", new Class[] {
                    String.class, Integer.TYPE });

            _evalFileMethod = _interpClass.getMethod("evalFile", String.class);

            _getVarMethod = _interpClass.getMethod("getVar", new Class[] {
                    String.class, String.class, Integer.TYPE });

            _tclObjectClass = Class.forName("tcl.lang.TclObject");
            _setVarMethod = _interpClass.getMethod("setVar", new Class[] {
                    String.class, _tclObjectClass, Integer.TYPE });

            // Create a TclObject with value 0 for use with the doneTests Tcl proc.
            Class<?> tclIntegerClass = Class.forName("tcl.lang.TclInteger");
            _newInstanceTclIntegerMethod = tclIntegerClass.getMethod(
                    "newInstance", new Class[] { Integer.TYPE });

            _tclExceptionClass = Class.forName("tcl.lang.TclException");

            _getCompletionCodeMethod = _tclExceptionClass.getMethod(
                    "getCompletionCode", new Class[] {});

            _tclObjectZero = _newInstanceTclIntegerMethod.invoke(null,
                    new Object[] { Integer.valueOf(0) });

            _failedTestCount = _newInstanceTclIntegerMethod.invoke(null,
                    new Object[] { Integer.valueOf(0) });

        } catch (Throwable throwable) {
            // Exceptions sometimes get marked as multiple failures here so
            // we print the stack to aid debugging.
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

}
