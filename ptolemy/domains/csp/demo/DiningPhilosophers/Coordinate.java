/* A class for storing cartesian coordinates.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

/**
 * A class for storing cartesian coordinates.
 *
 * @author John Hall
 * @version $Id$
 */
package ptolemy.domains.csp.demo.DiningPhilosophers;

public class Coordinate {
    /** The X coordinate. */
    public int X = 0;
    /** The Y coordinate. */
    public int Y = 0;

    /** Constructs a new Coordinate initialized as the origin (0, 0). */
    public Coordinate() {}

    /** Constructs a new Coordinate with the specified values.
     *
     * @param  x the X coordinate.
     * @param  y the Y coordinate.
     */
    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    /**
     * Tests another object for equality with this instance.
     *
     * @return whether it is equal or not.
     */
    public boolean equals(Object o) {
        Coordinate c;

        if (o instanceof Coordinate) {
            c = (Coordinate) o;
            if (this.X == c.X && this.Y == c.Y) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Returns a string representation of the Coordinate in the form "(X, Y)".
     *
     * @return a string representation of the Coordinates.
     */
    public String toString() {
        return "(" + X + ", " + Y + ")";
    }
}
