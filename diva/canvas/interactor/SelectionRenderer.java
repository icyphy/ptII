/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @version        $Revision$
 * @author         John Reekie
 */
public interface SelectionRenderer {

    /** Test if the given figure is currently rendered selected.
     */
    public abstract boolean isRenderedSelected (Figure f);

    /** Set the rendering of the figure as deselected.
     * If the figure is not rendered selected, do nothing.
     */
    public abstract void renderDeselected (Figure f);

    /** Set the rendering of the figure as selected. If the figure is
     * already rendered that way, update the rendering to reflect
     * the figure's current position and state.
     */
    public abstract void renderSelected (Figure f);
}


