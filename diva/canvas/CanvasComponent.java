/*
 Copyright (c) 1998-2014 The Regents of the University of California
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

/** A CanvasComponent is an object that can be inserted into the
 * display tree of a JCanvas. The tree is rooted by an instance of
 * CanvasPane, then consists of CanvasLayers, Figures, and other
 * kinds of recursively-structured CanvasComponents.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public interface CanvasComponent {
    /** Return the parent of this component. Return null if the component
     *  does not have a parent.
     *  @return The parent of this component.
     */
    public CanvasComponent getParent();

    /** Return the transform context of the component. If the component
     *  has its own transform context, this method should return it,
     *  otherwise it should return the transform context of its parent.
     *  @return  The transform context of this component.
     */
    public TransformContext getTransformContext();

    /** Schedule a repaint of the component. This should be called after
     *  performing modifications on the component.
     */
    public void repaint();

    /** Accept notification that a repaint has occurred somewhere
     *  in the tree below this component. The component must
     *  clear any cached data that depends on its children and
     *  forward the notification upwards.
     *  @param damageRegion The region where a repaint has occurred.
     */
    public void repaint(DamageRegion damageRegion);
}
