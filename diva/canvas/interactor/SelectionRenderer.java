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
package diva.canvas.interactor;

import diva.canvas.Figure;

/** An interface that defines rendering for selections.
 * An implementation of this class modifies the representation
 * of a figure, or adds additional graphics to the context
 * in which the figure is drawn, to produce selected and
 * deselected renderings of the figure.
 *
 * <p> (Should this be made into a more general class?)
 *
 * @version        $Id$
 * @author         John Reekie
 */
public interface SelectionRenderer {
    /** Test if the given figure is currently rendered selected.
     */
    public abstract boolean isRenderedSelected(Figure f);

    /** Set the rendering of the figure as deselected.
     * If the figure is not rendered selected, do nothing.
     */
    public abstract void renderDeselected(Figure f);

    /** Set the rendering of the figure as selected. If the figure is
     * already rendered that way, update the rendering to reflect
     * the figure's current position and state.
     */
    public abstract void renderSelected(Figure f);
}
