/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

/** A CanvasComponent is an object that can be inserted into the
 * display tree of a JCanvas. The tree is rooted by an instance of
 * CanvasPane, then consists of CanvasLayers, Figures, and other
 * kinds of recursively-structured CanvasComponents.
 *
 * @version        $Revision$
 * @author John Reekie
 * @rating Yellow
 */
public interface CanvasComponent {

    /** Return the parent of this component. Return null if the component
     *  does not have a parent.
     */
    public CanvasComponent getParent ();

    /** Return the transform context of the component. If the component
     * has its own transform context, this method should return it,
     * otherwise it should return the transform context of its parent.
     */
    public TransformContext getTransformContext ();

    /** Schedule a repaint of the component. This should be called after
     *  performing modifications on the component.
     */
    public void repaint ();

    /** Accept notification that a repaint has occurred somewhere
     * in the tree below this component. The component must
     * clear any cached data that depends on its children and
     * forward the notification upwards.
     */
    public void repaint (DamageRegion d);
}


