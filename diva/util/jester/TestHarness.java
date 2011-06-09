/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.util.jester;

import java.io.PrintStream;

/**
 * A test harness. A test harness is created by each test suite
 * and has test cases passed to it. It is where such things as the
 * level of output from the test, logging, and so on are set up.
 * (Currently, the harness is very simple. It will be extended
 * in the future.)
 *
 * @author John Reekie
 * @version $Id$
 */
public class TestHarness {
    /** The output stream
     */
    private PrintStream _outputStream = System.out;

    /** The indent level
     */
    private int _indentLevel = 0;

    /** The indent string
     */
    private String _indentString = "";

    /**
     * Run a single test and log the results
     */
    public void runTestCase(TestCase testCase) {
        printnoln(testCase.getName());

        try {
            testCase.init();
        } catch (Exception e) {
            println("\nInitialization aborted: ");
            println("    " + testCase.toString());
            println("    " + e.getMessage());
            println("Stack trace: ");
            e.printStackTrace(_outputStream);
            return;
        }

        try {
            testCase.run();
        } catch (Exception e) {
            println("\nTest case aborted: ");
            println("    " + testCase.toString());
            println("    " + e.getMessage());
            println("Stack trace: ");
            e.printStackTrace(_outputStream);
            return;
        }

        try {
            testCase.check();
        } catch (TestFailedException e) {
            println("\nTest failed: ");
            println("    " + testCase.toString());
            println("    " + e.getMessage());
            return;
        } catch (Exception e) {
            println("\nTest check aborted: ");
            println("    " + testCase.toString());
            println("    " + e.getMessage());
            println("Stack trace: ");
            e.printStackTrace(_outputStream);
        }

        if (testCase.getExecutionTime() > 0) {
            print(" (" + testCase.getExecutionTime() + " ms)\n");
        } else {
            print("\n");
        }
    }

    /**
     * Get ready to run a test suite. Calls to this can be nested.
     */
    void readyTestSuite(TestSuite suite) {
        Object factory = suite.getFactory();
        String suiteClass = suite.getClass().getName();
        int index = suiteClass.lastIndexOf(".");
        suiteClass = suiteClass.substring(index + 1);

        if (factory == null) {
            println(suiteClass);
        } else {
            println(suiteClass + ": " + suite.getFactory().toString());
        }

        _indentLevel++;
        _indentString = _indentString + "    ";
    }

    /**
     * Clean up after running a test suite.
     */
    void doneTestSuite() {
        _indentLevel--;
        _indentString = _indentString.substring(4);
    }

    /**
     * Set the output stream
     */
    public void setOutputStream(PrintStream s) {
        _outputStream = s;
    }

    /** Print to the output stream with the current indent
     */
    void println(String s) {
        _outputStream.print(_indentString);
        _outputStream.println(s);
    }

    /** Print to the output stream with the current indent
     */
    void printnoln(String s) {
        _outputStream.print(_indentString);
        _outputStream.print(s);
    }

    /** Print to the output stream with no indent
     */
    void print(String s) {
        _outputStream.print(s);
    }
}
