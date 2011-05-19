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
package diva.canvas;

import java.awt.Shape;

/** An interface for figure sets with that have their contents defined
 * geometrically. The geometry of the set is a Shape, and the contents
 * of the set is somehow determined by that shape.  A number of the
 * methods defined here are optional, so implementors are not obliged
 * to implement them. For example, some implementations might not
 * allow the geometry to be changed, so they can throw an exception on
 * the setGeometry(), freshFigures(), and staleFigures() methods.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public interface GeometricSet extends FigureSet {
    /** Return an iteration of undecorated figures added to the set
     * since the most recent call to setGeometry(). If there
     * are none, the iterator will return false() to the
     * first call to hasNext(). The figures are returned in
     * an undefined order. This is an optional operation.
     */

    //public abstract Iterator freshFigures();
    /** Get the geometry. The client must not modify the
     * returned shape.
     */
    public abstract Shape getGeometry();

    /** Set the geometry. All previously-obtained iterators
     * are invalid and must be discarded. This is an optional
     * operation. Implementors may choose to throw an exception
     * if the particular Shape class passed in as the geometry
     * is not suitable.
     */
    public abstract void setGeometry(Shape geometry);

    /** Return an iteration of undecorated figures removed from
     * the set since the most recent call to setGeometry(). If there
     * are none, the iterator will return false() to the
     * first call to hasNext(). The figures are returned in
     * an undefined order. This is an optional operation.
     */

    //public abstract Iterator staleFigures();
    /** Return an iteration of the undecorated figures in the set.
     * This is the same sets that will be returned by the
     * figures() method, but with any FigureDecorators stripped
     * out.
     */

    //public abstract Iterator undecoratedFigures();
}
