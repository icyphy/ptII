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

import java.util.Arrays;
import java.util.Collection;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static junitparams.JUnitParamsRunner.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

///////////////////////////////////////////////////////////////////
//// TclTests
/**
 * Run the Tcl tests under JUnit.
 *
 * <p>This test must be run from the directory that contains the auto/ directory,
 * for example:</p>
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/io/test; java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar::${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.TclTests)
 * </pre>
 *
 * <p>This test uses JUnitParams from
 * <a href="http://code.google.com/p/junitparams/#in_browser">http://code.google.com/p/junitparams/</a>,
 * which is released under <a href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License 2.0</a>.
 * </p>

 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class TclTests {
 
    /** Return a two dimensional array of arrays of strings
     *  that name the model to be executed.
     *  If auto/ does not exist, or does not contain files
     *  that end with .xml or .moml, return a list with one
     *  element that is empty.
     *  @return The List of model names in auto/
     */  
    public Object[] parametersForRunTclFile() throws IOException {
        String [] tclFiles = new File(".").list(
                new FilenameFilter() {
                    /** Return true if the file name ends with .tcl and is
                     *  not alljtests.tcl or testDefs.tcl   
                     *  @param directory Ignored
                     *  @param name The name of the file.
                     *  @return true if the file name ends with .xml or .moml
                     */   
                    public boolean accept(File directory, String name) {
                        String fileName = name.toLowerCase();
                        if (fileName.endsWith(".tcl")) {
                            if (!fileName.endsWith("alljtests.tcl") 
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
            for(String tclFile: tclFiles) {
                data[i++][0] = new File(tclFile).getCanonicalPath();
            }
            return data;
        } else {
            return new Object[][] { { THERE_ARE_NO_TCL_TESTS } };
        }
     }
 
    /** Find the tcl.lang.Interp class and its interp(String) method.
     *  @exception Throwable If the class, constructor or method cannot be found.
     *  or if the Interp cannot be instantiated.
     */
    @Before public void setUp() throws Throwable {
        _interpClass = Class.forName("tcl.lang.Interp");
        _interp = _interpClass.newInstance();
        _evalFileMethod = _interpClass.getMethod("evalFile", String.class);

    }

    /** Run a tclFile.
     *  @exception Throwable If thrown while executing the tclFile.
     *  @param fullPath The full path to the model file to be executed.
     *  If the fullPath ends with the value of the 
     *  {@link #THERE_ARE_NO_TCL_TESTS}, then the method returns
     *  immediately.
     */
    @Test
    @Parameters
    public void RunTclFile(String tclFile) throws Throwable {
        if (tclFile.endsWith(THERE_ARE_NO_TCL_TESTS)) {
            System.out.println("No tcl tests in " + System.getProperty("user.dir"));
            return;
        }
        System.out.println(tclFile);
        _evalFileMethod.invoke(_interp, new Object [] {tclFile});
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The tcl.lang.Interp class.  We use reflection her
     *  to avoid false dependencies if auto/ does not exist.
     */
    private static Class _interpClass;

    /** The tcl.lang.Interp object upon which we invoke evalFile(String).
     */
    private Object _interp;

    private static Method _evalFileMethod;

    /** A special string that is passed when there are no tcl tests.
     *  This is necessary to avoid an exception in the JUnitParameters.
     */
    protected final static String THERE_ARE_NO_TCL_TESTS = "ThereAreNoTclTests";
}