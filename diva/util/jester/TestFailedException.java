/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.jester;

/**
 * An exception that is thrown when a test produces an
 * incorrect result.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision$
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
    public TestFailedException (String message) {
        super(message);
    }

    /**
     * Create a new test exception with a detail message
     * and one additional object.
     */
    public TestFailedException (String message, Object a) {
        super(message);
        first = a;
    }

    /**
     * Create a new test exception with a detail message
     * and two additional objects.
     */
    public TestFailedException (String message, Object a, Object b) {
        super(message);
        first = a;
        second = b;
    }
}


