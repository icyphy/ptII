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

import java.util.Iterator;

/** The FigureSet interface is the interface implemented by any
 * class that contain references to a set of figures. Some of the
 * operations are noted as optional -- implementors can throw
 * an UnsupportedOperation exception if these methods do not make
 * sense for them.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public interface FigureSet {
    /** Test if this set contains the given figure. As a general
     * rule, the implementation of this method is not required to be
     * efficient -- O(n) in the length of the list is acceptable.
     * Clients should note that, in general, a much better way
     * of making this same test is to check if the parent of the figure
     * is the same object as this set.
     * @param f The figure
     * @return True if the figure is contained by the set.
     */
    boolean contains(Figure f);

    /** Return an iteration of the figures in this set, in an
     * undefined order. Generally, an implementor will return figures
     * from front to back, but if there is a substantially more
     * efficient way of returning them, then the implementor can
     * use that.
     */
    Iterator figures();

    /** Return an iteration of the figures in this set, from
     * back to front. This is the order in which
     * figures should normally be painted.
     * @return The iterator
     */
    public Iterator figuresFromBack();

    /** Return an iteration of the figures in this set, from back to
     * front. This is the order in which events should normally be
     * intercepted.
     * @return The iterator
     */
    public Iterator figuresFromFront();
}
