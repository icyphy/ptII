/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import java.awt.Shape;

import diva.canvas.Figure;


/** Geometry is an interface that captures the concept of an
 * abstract figure geometry. Classes that implement this geometry
 * are intended to be used as utility classes by figure and manipulators.
 * Geometry objects generally provide support for reshaping and
 * querying geometry-related aspects of figures and shapes.
 * Generally, Geometry objects will contains some number of
 * Site objects as part of this support.
 *
 * <p>This interface is fairly "loose," because there are
 * so many different ways that figures might choose to allow
 * access to their geometry. It is not intended to be rigid
 * framework-style interface that defines what implementors
 * should do, but merely to provide a way to loosely group
 * the geometry classes together.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public interface Geometry {

    /** Get the figure to which this geometry object is attached.
     * Returns null if there isn't one.
     */
    public Figure getFigure ();

    /** Get the shape that defines this geometry. In general,
     * a geometry is defined by a shape of some sort, and this
     * method provides the most general way that a client
     * can access that shape. Implementing classes may choose
     * to define other, more efficient or more type-specific
     * methods.
     */
    public Shape getShape ();

    /** Set the shape that defines this geometry object.
     * Implementing classes should check that the specific
     * type of the shape is one that they can accept, and
     * throw an IllegalArgumentException exception if not.
     */
    public void setShape (Shape shape);

    /** Translate the geometry object. This method is typically
     * used by clients as a fast way of changing the geometry.
     * Implementors can use it to update their internal data
     * more efficiently than by using the setShape() method.
     */
    public void translate (double x, double y);
}


