/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @version        $Revision$
 * @author John Reekie
 * @rating Red
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


