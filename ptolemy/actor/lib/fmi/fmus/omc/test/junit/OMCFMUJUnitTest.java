/* JUnit test for Open Modelica (OMC) Functional Mock-up Unit Co-Simulation and Model Exchange.

 Copyright (c) 2014 The Regents of the University of California.
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

package ptolemy.actor.lib.fmi.fmus.omc.test.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
public class OMCFMUJUnitTest {
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
        String resultsFileName = File.createTempFile("OMCFMUJUnitTest", "csv")
                .getCanonicalPath();
        String updateString = "To update " + knownGoodFileName + ", run:\n"
                + "java -classpath \"" + topDirectory + "/lib/jna.jar"
	        + System.getProperty("path.separator")
                + topDirectory + "\" org.ptolemy.fmi.driver.FMUCoSimulation "
                + fmuFileName + " " + endTime + " " + stepSize + " false c "
  	        + knownGoodFileName;
        System.out.println(updateString.replace("\\", "/"));
        new FMUCoSimulation().simulate(fmuFileName, endTime, stepSize,
                LOGGING, ',', resultsFileName);

        String results = OMCFMUJUnitTest.readFile(resultsFileName);
        String knownGood = OMCFMUJUnitTest.readFile(knownGoodFileName);
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

    // /** Run the bouncing ball co-simulation functional mock-up unit test.
    //  *  @exception Exception If there is a problem reading or running the test.
    //  */
    // @org.junit.Test
    // public void cosimulateBouncingBall() throws Exception {
    //     // The end time and step size come from run_all.bat in FMUSDK2.0.1.
    //     cosimulate("bouncingBall", 4, 0.01);
    // }

    /** Parse a Functional Mock-up Unit .fmu file, run it using model exchange
     *  and compare the results against a known good file.
     *
     *  @param fmuFileName The absolute pathname of the .fmu file.  Absolute
     *  pathnames are used because this test could be run from anywhere.
     *  @param knownGoodFileName The absolute pathname of the known good results.
     *  Note that when the test is run, the output includes the command that could
     *  be run to create the known good file.
     *  @return the file name of the .csv file that is created.
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public String modelExchange(String fmuFileName, String knownGoodFileName)
            throws Exception {
        String resultsFileName = File.createTempFile("OMCFMUJUnitTest", ".csv")
                .getCanonicalPath();
        String updateString = "To update " + knownGoodFileName + ", run:\n"
                + "java -classpath \"" + topDirectory + "/lib/jna.jar"
	        + System.getProperty("path.separator")
                + topDirectory + "\" org.ptolemy.fmi.driver.FMUModelExchange "
                + fmuFileName + " 1.0 0.001 false c " + knownGoodFileName;
        System.out.println(updateString.replace("\\", "/"));
        new FMUModelExchange().simulate(fmuFileName, 1.0, 0.001,
                LOGGING, ',', resultsFileName);

        String results = OMCFMUJUnitTest.readFile(resultsFileName);
        String knownGood = OMCFMUJUnitTest.readFile(knownGoodFileName);
        if (results.length() != knownGood.length()) {
            Assert.fail(fmuFileName + ":results length " + results.length()
                    + " != known good length " + knownGood.length()
                    + "\nresults:\n" + results + "\nknownGood:\n" + knownGood);
        }
        assertArrayEquals(results.getBytes(), knownGood.getBytes());
        return resultsFileName;
    }

    /** Invoke the Model exchange driver on a .fmu file.  The known
     *  good output is expected to be in a file whose name ends with
     *  "_me.csv".
     *  @param testName The name of the test with no file extension.
     *  @return the file name of the .csv file that is created.
     *  @exception Exception If there is a problem reading or executing the test
     *  or if the results is not the same as the known good results.
     */
    public String modelExchange(String testName) throws Exception {
        return modelExchange(topDirectory + "/ptolemy/actor/lib/fmi/fmus/omc/test/auto/"
                + testName + ".fmu", 
                topDirectory + "/ptolemy/actor/lib/fmi/fmus/omc/test/junit/"
                + testName + ".csv");
    }

    /** Check the csv file against equations.
     *  The equations come from sparse_fmi by James Nutaro.
     *  Some variance is acceptable, though the test fails if there is too much.
     *  @param testName The name of the test, ex. "Linsys".
     *  @param csvFile The comma separated file to be checked, typically generated
     *  by the {@link #modelExchange(String)} method.
     *  @param checkX2 True if the 3rd argument is x2 and should be checked.
     *  @exception Exception If there is a problem parsing the file.
     */
    public void modelExchangeCheck(String testName, String csvFile, boolean checkX2)
    throws Exception {
        // Read in the csv file and check the results
        double x1MaximumError = 0.0;
        double x2MaximumError = 0.0;

        // This value comes from the sparse_fmi/test/*_check.cpp files.
        double epsilon = 0.003;
        int row = 0;
        String line = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(csvFile));
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split(",");
                row++;
                // Skip the header.
                if (row > 1 ) {
                    double t = Double.valueOf(fields[0]);
                    double x1 = Double.valueOf(fields[1]);
                    double x1CalculatedValue = 0.0;
                    double x2 = 0.0;
                    double x2CalculatedValue = 0.0;
                    String message = "Error: While validating the results for "
                                + testName + " and reading " + csvFile
                                + " row: " + row
                                + "\n t: " + t
                                + "\nx1: " + x1
                                + " calculatedValue: " + x1CalculatedValue
                                + (checkX2
                                        ? ("\nx2: " + x2
                                                + " caclulatedValue: " + x2CalculatedValue)
                                        : "");

                    if (checkX2) {
                        // From sparse_fmi/test/Linsys_check.cpp or Linsys2_check.cpp
                        x1CalculatedValue = 1.0 * Math.exp(-0.5 * t);
                        x2 = Double.valueOf(fields[2]);
                        x2CalculatedValue = 2.0 * Math.exp(-1.0 * t);
                        x2MaximumError = Math.max(x2MaximumError, Math.abs(x2 - x2CalculatedValue));
                        assertEquals(message, x2, x2CalculatedValue, epsilon);
                    } else {
                        // Test1_check.cpp
                        x1CalculatedValue = 1.0 * Math.exp(-1.0 * t);
                    }
                    x1MaximumError = Math.max(x1MaximumError, Math.abs(x1 - x1CalculatedValue));
                    assertEquals(message, x1, x1CalculatedValue, epsilon);
                }
            } 
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        System.out.println(testName + ": x1 maximum error: " + x1MaximumError
                + (checkX2 ? (", x2 maximum error: " + x2MaximumError) : "")
                + " (Optional: Compare this against running the tests in sparse_fmi/test)");
        assertTrue("The error for x1 was " + x1MaximumError + ", which is greater than " + epsilon,
                x1MaximumError < epsilon);
        assertTrue("The error for x2 was " + x2MaximumError + ", which is greater than " + epsilon,
                x2MaximumError < epsilon);
    }

    /** Run the influenza model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeInfluenza() throws Exception {
        modelExchange("Influenza");
    }

    /** Run the linsys2 model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeLinsys2() throws Exception {
        String testName = "Linsys2";
        // Generate the csv file and compare the results. 
        String csvFile = modelExchange(testName);

        // Check the results against a calculation.
        modelExchangeCheck(testName, csvFile, true);
    }

    /** Run the linsys model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeLinsys() throws Exception {
        String testName = "Linsys";
        // Generate the csv file and compare the results. 
        String csvFile = modelExchange(testName);

        // Check the results against a calculation.
        modelExchangeCheck(testName, csvFile, true);
    }

    /** Run the Test1 model exchange functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */
    @org.junit.Test
    public void modelExchangeTest1() throws Exception {
        String testName = "Test1";
        // Generate the csv file and compare the results. 
        String csvFile = modelExchange(testName);

        // Check the results against a calculation.
        modelExchangeCheck(testName, csvFile, false);
    }

    /** Run the OpenModelica FMI model exchange tests.
     *  <p>To run these tests, either us <code>ant test</code> or run:
     *  <code>(cd ../../..; java -classpath lib/jna.jar:lib/junit-4.8.2.jar:. ptolemy.actor.lib.fmi.fmus.omc.test.junit.OMCFMUJUnitTest)</code></p>
     *
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("ptolemy.actor.lib.fmi.fmus.omc.test.junit.OMCFMUJUnitTest");
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

    /** True if the FMU should be called with enableLogging set to true. */
    static boolean LOGGING = false;

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
        } else if (userDir.endsWith("ptolemy/actor/lib/fmi/fmus/omc/test")) {
            topDirectory = new File(userDir).getParentFile().getParentFile()
                .getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().toString();
        } else {
            topDirectory = userDir;
        }
    }
}
