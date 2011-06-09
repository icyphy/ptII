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
 * An exception that is thrown when a test produces an
 * incorrect result.
 *
 * @author John Reekie
 * @version $Id$
 */
public class TestFailedException extends Exception {
    /**
     * One of the objects involved in the failed test.
     * @serial
     */
    public Object first;

    /**
     * Another of the objects involved in the failed test.
     * @serial
     */
    public Object second;

    /**
     * Create a new test exception with a detail message
     */
    public TestFailedException(String message) {
        super(message);
    }

    /**
     * Create a new test exception with a detail message
     * and one additional object.
     */
    public TestFailedException(String message, Object a) {
        super(message);
        first = a;
    }

    /**
     * Create a new test exception with a detail message
     * and two additional objects.
     */
    public TestFailedException(String message, Object a, Object b) {
        super(message);
        first = a;
        second = b;
    }
}
