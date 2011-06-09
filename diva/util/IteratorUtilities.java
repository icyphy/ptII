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
package diva.util;

import java.util.Iterator;

/**
 * A collection of utilities dealing with iterators.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version $Id$
 */
public class IteratorUtilities {
    /** You can't instantiate this class.
     */
    private IteratorUtilities() {
    }

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
