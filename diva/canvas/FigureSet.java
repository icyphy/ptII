/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import java.util.Iterator;

/** The FigureSet interface is the interface implemented by any
 * class that contain references to a set of figures. Some of the
 * operations are noted as optional -- implementors can throw
 * an UnsupportedOperation exception if these methods do not make
 * sense for them.
 *
 * @version        $Revision$
 * @author John Reekie
 * @rating Yellow
 */
public interface FigureSet {

    /** Test if this set contains the given figure. As a general
     * rule, the implementation of this method is not required to be
     * efficient -- O(n) in the length of the list is acceptable.
     * Clients should note that, in general, a much better way
     * of making this same test is to check if the parent of the figure
     * is the same object as this set.
     */
    boolean contains (Figure f);

    /** Return an iteration of the figures in this set, in an
     * undefined order. Generally, an implementor will return figures
     * from front to back, but if there is a substantially more
     * efficient way of returning them, then the implementor can
     * use that.
     */
    Iterator figures ();


    /** Return an iteration of the figures in this set, from
     * back to front. This is the order in which
     * figures should normally be painted.
     */
  public Iterator figuresFromBack ();

    /** Return an iteration of the figures in this set, from back to
     * front. This is the order in which events should normally be
     * intercepted.
     */
  public Iterator figuresFromFront ();
}


