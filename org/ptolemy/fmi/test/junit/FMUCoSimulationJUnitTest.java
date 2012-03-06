/* JUnit test for Functional Mock-up Unit Co-Simulation

 Copyright (c) 2012 The Regents of the University of California.
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

package org.ptolemy.fmi.test.junit;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.ptolemy.fmi.FMUCoSimulation;


import static org.junit.Assert.assertArrayEquals;
import org.junit.Assert;

///////////////////////////////////////////////////////////////////
//// FMUCoSimulationJUnitTest
/**
 * Invoke the FMUCoSimulation class on various .fmu files.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class FMUCoSimulationJUnitTest {
    /** Parse a Functional Mock-up Unit .fmu file, run it using co-simulation
     *  and compare the results against a known good file.
     *  
     *  @param fmuFileName The absolute pathname of the .fmu file.  Absolute
     *  pathnames are used because this test could be run from anywhere.
     *  @param knownGoodFileName The absolute pathname of the known good results.
     *  Note that when the test is run, the output includes the command that could
     *  be run to create the known good file.
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public void cosimulate(String fmuFileName, String knownGoodFileName) throws Exception {
        String resultsFileName = File.createTempFile("FMUCoSimulationJUnitTest", "csv").getCanonicalPath();
        System.out.println("To update " + knownGoodFileName + ", run:\n"
                + "java -classpath \"" + topDirectory + "/lib/jna.jar:" + topDirectory
                + "\" org.ptolemy.fmi.FMUCoSimulation "
                + fmuFileName + " 1.0 0.1 false c "
                + knownGoodFileName);
        FMUCoSimulation.simulate(fmuFileName,
                1.0, 0.1, true /*logging*/, ',' , resultsFileName);

        String results = FMUCoSimulationJUnitTest.readFile(resultsFileName);
        String knownGood = FMUCoSimulationJUnitTest.readFile(knownGoodFileName);
        if (results.length() != knownGood.length()) {
            Assert.fail(fmuFileName + ":results length "
                    + results.length() + " != known good length "
                    + knownGood.length()
                    + "\nresults:\n" + results
                    + "\nknownGood:\n" + knownGood);
        }
        assertArrayEquals(results.getBytes(), knownGood.getBytes());
    }

    /** Co-simulate a test.
     *  @param testName The name of the test with no file extension.
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public void cosimulate(String testName) throws Exception {
        cosimulate(topDirectory + "/org/ptolemy/fmi/fmu/cs/" + testName + ".fmu",
                topDirectory + "/org/ptolemy/fmi/test/junit/" + testName + ".csv");
    }

    /** Run the bouncing ball co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */   
     @org.junit.Test
     public void runBouncingBall() throws Exception {
         cosimulate("bouncingBall");
     }

    /** Run the dq co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */   
    @org.junit.Test
    public void runDq() throws Exception {
        cosimulate("dq");
    }

    /** Run the inc co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */   
    @org.junit.Test
    public void runInc() throws Exception {
       cosimulate("inc");
    }

    /** Run the values co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */   
    @org.junit.Test
    public void runValues() throws Exception {
       cosimulate("values");
    }

    /** Run the vanDerPol co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */   
    @org.junit.Test
    public void runVanDerPol() throws Exception {
        cosimulate("vanDerPol");
    }

    /** Run FMI co-simulation tests.
     *  <p>To run these tests, either us <code>ant test</code> or run:   
     *  <code>(cd ../../..; java -classpath lib/jna.jar:lib/junit-4.8.2.jar:. org.ptolemy.fmi.test.junit.FMUCoSimulationJUnitTest)</code></p>
     *
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
                .main("org.ptolemy.fmi.test.junit.FMUCoSimulationJUnitTest");
    }

    /** Read in the named file and returns the contents as a string.
     *  @param fileName The name of the file to be read.
     *  @return The contents of the file.
     *  @exception IOException If there is a problem reading or closing file.
     */   
    public static String readFile(String fileName) throws IOException {
        FileInputStream fileInputStream = null; 
        DataInputStream dataInputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer results = new StringBuffer();
        try {
            fileInputStream = new FileInputStream(fileName);
            dataInputStream = new DataInputStream(fileInputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)   {
                results.append(line + lineSeparator);
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return results.toString();
    }

    static String lineSeparator = "\n";

    static String topDirectory;

    static {
        String userDir = System.getProperty("user.dir");
        // If the test was invoked with -Dptolemy.ptII.dir=${PTII}
        String ptolemyPtIIDir = System.getProperty("ptolemy.ptII.dir");
        if (ptolemyPtIIDir != null) { 
            topDirectory = ptolemyPtIIDir;
        } else if (userDir.endsWith("org/ptolemy/fmi")) {
            topDirectory = new File(userDir).getParentFile().getParentFile().getParentFile().toString();
        } else if (userDir.endsWith("org/ptolemy/fmi/test/jni")) {
            topDirectory = new File(userDir).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().toString();
        } else {
            topDirectory = userDir;
        }
    }
}
