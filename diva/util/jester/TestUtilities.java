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
 *
 */
package diva.util.jester;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/** Utility functions to make tests easier to write.
 *
 * @version $Id$
 * @author  John Reekie
 * @author  Michael Shilman

 */
public final class TestUtilities {
    /** Test if two shapes are equal. The epsilon is a number such
     * that if two points are different by less than epsilon in both
     * coordinates they are considered "equal". This method is
     * not very efficient but is particularly useful for testing
     * purposes.
     */
    public static boolean shapeEquals(Shape s1, Shape s2, double epsilon) {
        PathIterator p1 = s1.getPathIterator(new AffineTransform());
        PathIterator p2 = s2.getPathIterator(new AffineTransform());
        double[] c1 = new double[6];
        double[] c2 = new double[6];
        int t1;
        int t2;
        int i;
        int n = 0;

        while (!p1.isDone() && !p2.isDone()) {
            t1 = p1.currentSegment(c1);
            t2 = p2.currentSegment(c2);

            if (t1 != t2) {
                return false;
            }

            switch (t1) {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
                n = 2;
                break;

            case PathIterator.SEG_QUADTO:
                n = 4;
                break;

            case PathIterator.SEG_CUBICTO:
                n = 6;
                break;

            default:
            // presumably SEG_CLOSE
            }

            for (i = 0; i < n; i++) {
                if (Math.abs(c1[i] - c2[i]) > epsilon) {
                    return false;
                }
            }

            p1.next();
            p2.next();
        }

        // Make sure that both are the same length
        return (p1.isDone() && p2.isDone());
    }
}
