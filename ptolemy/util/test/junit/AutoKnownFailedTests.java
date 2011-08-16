/* Run the Ptolemy model tests in the auto/knownFailedTests directory using JUnit.

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

import java.util.Arrays;
import java.util.Collection;

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

///////////////////////////////////////////////////////////////////
//// AutoKnownFailedTests
/**
 * Run the Ptolemy model tests in the auto/knownFailedTests directory using JUnit.
 *
 * <p>This test must be run from the directory that contains the auto/ directory,
 * for example:</p>
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/net/test; java -classpath ${PTII}:${PTII}/lib/junit-4.8.2.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.AutoKnownFailedTests)
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(value = Parameterized.class)
public class AutoKnownFailedTests {
 
    /** Create an auto test for a model.
     *  @param model the file path to the model's .xml file.
     */
    public AutoKnownFailedTests(String model) {
        _modelFile = model;
    }
 
    /** Return a List of two dimensional Object Arrays
     *  where each element of the List is an ObjectArray
     *  with one element that contains a String that is
     *  the path of the model in the auto/ directory to be executed
     *  If auto/ does not exist, or does not contain files
     *  that end with .xml or .moml, return a list with one
     *  element that is empty.
     *  @return The List of model names in auto/
     */  
    @Parameters
    public static Collection<Object[]> data() {
        File auto = new File("auto/knownFailedTests/");
        if (auto.isDirectory()) {
            String [] modelFiles = auto.list(
                    new FilenameFilter() {
                        /** Return true if the file name ends with .xml or .moml
                         *  @param directory Ignored
                         *  @param name The name of the file.
                         *  @return true if the file name ends with .xml or .moml
                         */   
                        public boolean accept(File directory, String name) {
                            String fileName = name.toLowerCase();
                            return fileName.endsWith(".xml") || fileName.endsWith(".moml");
                        }
                    });
            int i = 0;
            Object[][] data = new Object[modelFiles.length][1];
            for(String modelFile: modelFiles) {
                data[i++][0] = modelFile;
            }
            return Arrays.asList(data);
        }
        return Arrays.asList(new Object[0][0]);
     }
 
    /** Find the ptolemy.moml.MoMLSimpleApplication class and its constructor that
     *  takes a String.
     *  @exception Throwable If the class or constructor cannot be found.
     */
    @Before public void setUp() throws Throwable {
        _applicationClass = Class.forName("ptolemy.moml.MoMLSimpleApplication");
        _applicationConstructor = _applicationClass.getConstructor(String.class);

    }

    /** Execute a model.
     *  @exception Throwable If thrown while executing the model.
     */
    @Test
    public void testModel() throws Throwable {
        System.out.println("----------------- testing (KnownFailure) auto/knownFailedTests/"
                + _modelFile);
        try {
            _applicationConstructor.newInstance("auto/knownFailedTests/" + _modelFile);
        } catch (Throwable throwable) {
            System.out.println("Known Failure: " + throwable);
            throwable.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The MoMLSimpleApplication class.  We use reflection her
     *  to avoid false dependencies if auto/ does not exist.
     */
    protected static Class _applicationClass;

    /** The MoMLSimpleApplication(String) constructor. */
    protected static Constructor _applicationConstructor;

    /** The path to the .xml or .moml file that contains the model. */
    protected String _modelFile;
}