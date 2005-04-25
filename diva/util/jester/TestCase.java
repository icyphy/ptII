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


/**
 * An abstract superclass for all test cases. This is intended for
 * subclassing (generally anonymously), and to be passed to a
 * TestHarness for execution. Subclasses are required to implement
 * the run() and check() methods, and can optionally override
 * the failed() method.
 *
 * Note that test suites may choose to subclass this with
 * another abstract class, and then to anonymously override
 * that new class in the methods. This allows, for example,
 * the addition of methods for setting up scenarios shared
 * by multiple tests, common result testing methods, and so on.
 *
 * @author John Reekie
 * @version $Id$
 */
public abstract class TestCase {
    // The name of this test case
    private String _name;

    // The start time of this test case
    private long _startTime = 0;

    // The stop time of this test case
    private long _stopTime = 0;

    /**
     * Construct a test case with the given name
     */
    public TestCase(String name) {
        this._name = name;
    }

    /**
     * Report on the results of running the test. This method should
     * perform a series of checks on the data produced by the run()
     * method, and for each, call fail() if the produced data does
     * not conform to the expected result.
     */
    public abstract void check() throws TestFailedException;

    /**
     * Make an assertion, and fail the test if it isn't satisfied.
     * This is a handy way of doing boolean results checking. The
     * second argument is a string, which should generally be a copy
     * of the expression that produced the first argument.
     */
    public void assertExpr(boolean passed, String msg)
        throws TestFailedException {
        if (!passed) {
            throw new TestFailedException(msg);
        }
    }

    /**
     * Assert the equality of two objects. This method uses the equals()
     * method of the first object to compare it with the second
     * object. If the objects are not equal, then the test is failed. If
     * the verbosity of the test harness is sufficiently high, then the
     * harness may print out or log additional information on the two
     * objects. The third argument is a string which should generally be
     * something like "foo == bar".
     *
     */
    public void assertEquals(Object first, Object second, String msg)
        throws TestFailedException {
        if (!first.equals(second)) {
            throw new TestFailedException(msg, first, second);
        }
    }

    /**
     * Fail a test. This method constructs and throws a
     * TestFailedException that includes the passed string as
     * part of the messages. It is intended as a simple convenience
     * method to make it easier to write test cases.
     */
    public void fail(String msg) throws TestFailedException {
        throw new TestFailedException(msg);
    }

    /**
     * Get the execution time of this test case. If the time is
     * zero (and the timers were called) then return 1.
     */
    public int getExecutionTime() {
        int diff = (int) (_stopTime - _startTime);

        if ((_startTime != 0) && (diff == 0)) {
            return 1;
        } else {
            return diff;
        }
    }

    /**
     * Get the name of this test case
     */
    public String getName() {
        return _name;
    }

    /**
     * Initialize the test case. This is a concrete method that does
     * nothing. It is not requires that this method be overridden, but
     * any test that is more than a few lines should do so to
     * distinguish between the initialization and execution phases
     * of the test. It also allows local classes to be created
     * that override init() to create an object or set of objects
     * that will be used in multiple tests.
     */
    public void init() throws Exception {
        // do nothing
    }

    /**
     * Run the test case. This method actually runs the test.
     * Result checking should not be done here, but in the
     * check() method. The method signature includes the completely
     * general Exception class, since this method can contain
     * arbitrary code.
     */
    public abstract void run() throws Exception;

    /** Start the execution timer. This can be called only once in
     * the execution of the test case. If it is called (and the
     * stopTimer() method id also called), the execution time is
     * printed by the test harness.
     */
    public void startTimer() {
        _startTime = System.currentTimeMillis();
    }

    /** Stop the execution timer. The difference between the start and
     * stop times will be reported by the test harness.
     */
    public void stopTimer() {
        _stopTime = System.currentTimeMillis();
    }
}
