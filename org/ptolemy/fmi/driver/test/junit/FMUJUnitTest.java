/* JUnit test for Functional Mock-up Unit Co-Simulation and Model Exchange.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

package org.ptolemy.fmi.driver.test.junit;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.ptolemy.fmi.driver.FMUCoSimulation;
import org.ptolemy.fmi.driver.FMUModelExchange;

///////////////////////////////////////////////////////////////////
//// FMUUnitTest
/**
 * Invoke the co-simulator and model exchanger on various Functional
 * Mockup Unit (.fmu) files.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class FMUJUnitTest {
    /** Parse a Functional Mock-up Unit .fmu file, run it using co-simulation
     *  and compare the results against a known good file.
     *
     *  @param fmuFileName The absolute pathname of the .fmu file.  Absolute
     *  pathnames are used because this test could be run from anywhere.
     *  @param endTime The end time.
     *  @param stepSize The step size
     *  @param knownGoodFileName The absolute pathname of the known good results.
     *  Note that when the test is run, the output includes the command that could
     *  be run to create the known good file.
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public void cosimulate(String fmuFileName, double endTime, double stepSize,
            String knownGoodFileName) throws Exception {
        String resultsFileName = File.createTempFile("FMUJUnitTest", "csv")
                .getCanonicalPath();
        System.out.println("To update " + knownGoodFileName + ", run:\n"
                + "java -classpath \"" + topDirectory + "/lib/jna.jar:"
                + topDirectory + "\" org.ptolemy.fmi.driver.FMUCoSimulation "
                + fmuFileName + " " + endTime + " " + stepSize + " false c "
                + knownGoodFileName);
        new FMUCoSimulation().simulate(fmuFileName, endTime, stepSize,
                true /*logging*/, ',', resultsFileName);

        String results = FMUJUnitTest.readFile(resultsFileName);
        String knownGood = FMUJUnitTest.readFile(knownGoodFileName);
        if (results.length() != knownGood.length()) {
            Assert.fail(fmuFileName + ":results length " + results.length()
                    + " != known good length " + knownGood.length()
                    + "\nresults:\n" + results + "\nknownGood:\n" + knownGood);
        }
        assertArrayEquals(results.getBytes(), knownGood.getBytes());
    }

    /** Co-simulate a test.
     *  @param testName The name of the test with no file extension.
     *  @param endTime The end time.
     *  @param stepSize The step size
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public void cosimulate(String testName, double endTime, double stepSize)
            throws Exception {
        cosimulate(topDirectory + "/org/ptolemy/fmi/fmu/cs/" + testName
                + ".fmu", endTime, stepSize, topDirectory
                + "/org/ptolemy/fmi/driver/test/junit/" + testName + ".csv");
    }

    /** Run the bouncing ball co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateBouncingBall() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        cosimulate("bouncingBall", 4, 0.01);
    }

    /** Run the bouncing ball co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateBouncingBall20RC1() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        cosimulate("bouncingBall20RC1", 4, 0.01);
    }

    /** Run the dq co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateDq() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        cosimulate("dq", 1, 0.1);
    }

    /** Run the inc co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateInc() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        // run_all.bat has a end time of 15, which will cause the
        // fmu to terminate and doStep() to return fmiError, so we go with 11.
        cosimulate("inc", 11, 0.4);
    }

    /** Run the inc co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateInc20RC1() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        // run_all.bat has a end time of 15, which will cause the
        // fmu to terminate and doStep() to return fmiError, so we go with 11.
        cosimulate("inc20RC1", 11, 0.4);
    }

    /** Run the values co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateValues() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        cosimulate("values", 12, 0.3);
    }

    /** Run the vanDerPol co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void cosimulateVanDerPol() throws Exception {
        // The end time and step size come from run_all.bat in FMUSDK2.0.1.
        cosimulate("vanDerPol", 5, 0.1);
    }

    /** Parse a Functional Mock-up Unit .fmu file, run it using model exchange
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
    public void modelExchange(String fmuFileName, String knownGoodFileName)
            throws Exception {
        String resultsFileName = File.createTempFile("FMUJUnitTest", "csv")
                .getCanonicalPath();
        System.out.println("To update " + knownGoodFileName + ", run:\n"
                + "java -classpath \"" + topDirectory + "/lib/jna.jar:"
                + topDirectory + "\" org.ptolemy.fmi.driver.FMUModelExchange "
                + fmuFileName + " 1.0 0.1 false c " + knownGoodFileName);
        new FMUModelExchange().simulate(fmuFileName, 1.0, 0.1,
                true /*logging*/, ',', resultsFileName);

        String results = FMUJUnitTest.readFile(resultsFileName);
        String knownGood = FMUJUnitTest.readFile(knownGoodFileName);
        if (results.length() != knownGood.length()) {
            Assert.fail(fmuFileName + ":results length " + results.length()
                    + " != known good length " + knownGood.length()
                    + "\nresults:\n" + results + "\nknownGood:\n" + knownGood);
        }
        assertArrayEquals(results.getBytes(), knownGood.getBytes());
    }

    /** Invoke the Model exchange driver on a .fmu file.  The known
     *  good output is expected to be in a file whose name ends with
     *  "_me.csv".
     *  @param testName The name of the test with no file extension.
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public void modelExchange(String testName) throws Exception {
        modelExchange(topDirectory + "/org/ptolemy/fmi/fmu/me/" + testName
                + ".fmu", topDirectory + "/org/ptolemy/fmi/driver/test/junit/"
                        + testName + "_me.csv");
    }

    /** Run the bouncing ball model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeBouncingBall() throws Exception {
        modelExchange("bouncingBall");
    }

    /** Run the dq model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeDq() throws Exception {
        modelExchange("dq");
    }

    /** Run the inc model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeInc() throws Exception {
        modelExchange("inc");
    }

    /** Run the values model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeValues() throws Exception {
        modelExchange("values");
    }

    /** Run the vanDerPol model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeVanDerPol() throws Exception {
        modelExchange("vanDerPol");
    }

    /** Run FMI model exchange tests.
     *  <p>To run these tests, either us <code>ant test</code> or run:
     *  <code>(cd ../../..; java -classpath lib/jna.jar:lib/junit-4.8.2.jar:. org.ptolemy.fmi.driver.test.junit.FMUJUnitTest)</code></p>
     *
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("org.ptolemy.fmi.driver.test.junit.FMUJUnitTest");
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
            bufferedReader = new BufferedReader(new InputStreamReader(
                    dataInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
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
            topDirectory = new File(userDir).getParentFile().getParentFile()
                    .getParentFile().toString();
        } else if (userDir.endsWith("org/ptolemy/fmi/test/jni")) {
            topDirectory = new File(userDir).getParentFile().getParentFile()
                    .getParentFile().getParentFile().getParentFile().toString();
        } else {
            topDirectory = userDir;
        }
    }
}
