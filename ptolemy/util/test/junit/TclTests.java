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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ptolemy.moml.MoMLSimpleApplication;

///////////////////////////////////////////////////////////////////
//// TclTests
/**
 * Run the Tcl tests under JUnit.
 *
 * <p>This test must be run from the directory that contains the auto/ directory,
 * for example:</p>
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/io/test; java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.TclTests)
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(value = Parameterized.class)
public class TclTests {
 
    /** Create an auto test for a model.
     *  @param tclFile the file path to the .tcl file to be executed
     */
    public TclTests(String tclFile) {
        _tclFile = tclFile;
    }
 
    /** Return a List of two dimensional Object Arrays
     *  where each element of the List is an ObjectArray
     *  with one element that contains a String that is
     *  the path of the tclFile in the auto/ directory to be executed
     *  If auto/ does not exist, or does not contain files
     *  that end with .xml or .moml, return a list with one
     *  element that is empty.
     *  @return The List of tclFile names in auto/
     */  
    @Parameters
    public static Collection<Object[]> data() {
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
                            if (!fileName.equals("alljtests.tcl") 
                                    && !fileName.equals("testDefs.tcl")) {
                                return true;
                            }
                        }
                        return false;
                    }
                });

        int i = 0;
        Object[][] data = new Object[tclFiles.length][1];
        for(String tclFile: tclFiles) {
            data[i++][0] = tclFile;
        }
        return Arrays.asList(data);
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

    /** Execute a tclFile.
     *  @exception Throwable If thrown while executing the tclFile.
     */
    @Test
    public void testTclFile() throws Throwable {
        System.err.println(_tclFile);
        _evalFileMethod.invoke(_interp, new Object [] {_tclFile});
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

    /** The path to the .xml or .moml file that contains the tclFile. */
    private String _tclFile;
}