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
    /**
     * @exception Exception If there is a rpoblem reading the test.
     */
    public void cosimulate(String fmuFileName, String knownGoodFileName) throws Exception {
        System.out.println("user.dir: " + System.getProperty("user.dir"));
        String resultsFileName = File.createTempFile("FMUCoSimulationJUnitTest", "csv").getCanonicalPath();
        System.out.println("To update " + knownGoodFileName + ", run:\n"
                + "(cd ../../..; java -classpath lib/jna.jar:. org.ptolemy.fmi.FMUCoSimulation "
                + fmuFileName + " 1.0 0.1 false s "
                + knownGoodFileName + ")");
        FMUCoSimulation.simulate(fmuFileName,
                1.0, 0.1, false /*logging*/, "s", resultsFileName);

        String results = FMUCoSimulationJUnitTest.readFile(resultsFileName);
        String knownGood = FMUCoSimulationJUnitTest.readFile(knownGoodFileName);
        assertArrayEquals(results.getBytes(), knownGood.getBytes());
    }

    /** Run the bouncing ball co-simulation functional mock-up unit test.
     *  @exception Exception If there is a problem reading or running the test.
     */   
    @org.junit.Test
    public void runBouncingBall() throws Exception {
        // FIXME: paths are relative to the directory where ant is run.
        cosimulate("fmu/cs/bouncingBall.fmu",
                "test/junit/bouncingBall.csv");
    }

    /** Run FMI co-simulation tests.
     *  <p>To run these tests, either use ant or run:   
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
        StringBuffer results = new StringBuffer();
        try {
            fileInputStream = new FileInputStream(fileName);
            dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)   {
                results.append(line);
            }
        } finally {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        }
        return results.toString();
    }

    private static String _eol = System.getProperty("line.separator");

}
