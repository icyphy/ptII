/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

import java.util.Iterator;

/**
 * A collection of utilities dealing with iterators.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class IteratorUtilities {
    /** You can't instantiate this class.
     */
    private IteratorUtilities() {}

    /** Return the first element in an iterator that
     * matches the given filter, or null if there isn't one.
     */
    public static Object firstMatch(Iterator i, Filter f) {
        while (i.hasNext()) {
            Object o = i.next();
            if (f.accept(o)) {
                return o;
            }
        }
        return null;
    }

    /** Return the first element in an iterator that
     * doesn't match the given filter, or null if there isn't one.
     */
    public static Object firstNotMatch(Iterator i, Filter f) {
        while (i.hasNext()) {
            Object o = i.next();
            if (!f.accept(o)) {
                return o;
            }
        }
        return null;
    }

    /**
     * Print every element of an iterator to stdout.
     * The string argument is printed first, and then each
     * element is printed on a new line but indented.
     */
    public static void printElements(String desc, Iterator i) {
        printElements("", desc, i);
    }

    /**
     * Print every element of an iterator to stdout.
     * The string argument is printed first with the prefix argument
     * leading it; then each element is printed on a new line with
     * additional indentation.
     */
    public static void printElements(String prefix, String desc, Iterator i) {
        System.out.print(prefix);
        System.out.println(desc);
        prefix = prefix + "    ";
        while (i.hasNext()) {
            System.out.print(prefix);
            System.out.println(i.next().toString());
        }
    }
}


