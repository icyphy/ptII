/* The node controller for locatable nodes

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.basic;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.graph.BasicNodeController;
import diva.graph.GraphController;
import diva.graph.NodeInteractor;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeController
/**
This node controller provides interaction techniques for nodes that are
locations.   This is common when the node has some concept of its
graphical location, but does not know about the figure that it
is associated with.  This class provides the connection between the
figure's notion of location and the node's concept of location.
<p>
When nodes are drawn, they are automatically placed at the
coordinate given by the location.  A LocatableNodeDragInteractor
is used to update the location of the node as the figure moves.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class LocatableNodeController extends BasicNodeController {

    public LocatableNodeController(GraphController controller) {
        super(controller);
        NodeInteractor nodeInteractor = (NodeInteractor) getNodeInteractor();
        nodeInteractor.setDragInteractor(new LocatableNodeDragInteractor(this));
    }

    /** Add a node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y) {
        throw new UnsupportedOperationException("Cannot add node.");
    }

    /** Draw the node at its location.
     */
    public Figure drawNode(Object node) {
        Figure nf = super.drawNode(node);
        locateFigure(node);
        return nf;
    }

    /** Return the desired location of this node.  Throw an exception if the
     *  node does not have a desired location.
     */
    public double[] getLocation(Object node) {
        if (hasLocation(node)) {
            return ((Locatable) node).getLocation();
        } else throw new RuntimeException("The node " + node +
                "does not have a desired location");
    }

    /** Return true if the node is associated with a desired location.
     *  In this base class, return true if the the node's semantic object is
     *  an instance of Locatable.
     */
    public boolean hasLocation(Object node) {
        if (node instanceof Locatable) {
            Locatable object = (Locatable) node;
            double[] location = object.getLocation();
            if (location != null && location.length == 2) {
                return true;
            }
        }
        return false;
    }

    /** Move the node's figure to the location specified in the node's
     *  semantic object, if that object is an instance of Locatable.
     *  If the semantic object is not a location, then do nothing.
     *  If the figure associated with the semantic object is an instance
     *  of TerminalFigure, then modify the location to ensure that the
     *  connect site snaps to grid.
     *  @param node The object to locate.
     */
    public void locateFigure(Object node) {
        Figure nf = getController().getFigure(node);
        try {
            if (hasLocation(node)) {
                double[] location = getLocation(node);
                CanvasUtilities.translateTo(nf, location[0], location[1]);
            }
        } catch(Exception ex) {
            // FIXME: Ignore if there is no valid location.  This
            // happens occasionally due to a race condition in the
            // Bouncer demo.  Occasionally, the repaint thread will
            // attempt to locate the bouncing icon before the location
            // parameter has been evaluated, causing an exception to
            // be thrown.  Basically the lazy parameter evaluation
            // mechanism causes rerendering in Diva to be rentrant,
            // which it shouldn't be.  Unfortunately, I have no idea
            // how to fix it... SN 5/5/2003
        }
    }

    /** Set the desired location of this node.  Throw an exception if the
     *  node can not be given a desired location.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void setLocation(Object node, double[] location)
            throws IllegalActionException {
        if (location != null &&
                location.length != 2) {
            throw new RuntimeException("The location " + location +
                    " is not valid");
        }
        if (node instanceof Locatable) {
            ((Locatable)node).setLocation(location);
        } else throw new RuntimeException("The node " + node +
                "cannot have a desired location");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Render the children of the specified node.
     *  This overrides the base class to do nothing if the node
     *  contains an attribute named "_hide".
     *  @param node The node with children to render.
     */
    protected void _drawChildren(java.lang.Object node) {
        if (!_hide(node)) super._drawChildren(node);
    }

    /** Render the specified node.  This overrides the base class to
     *  return an invisible figure if the node contains an attribute
     *  named "_hide".
     *  @param node The node to render.
     */
    protected Figure _renderNode(java.lang.Object node) {
        if (node == null || _hide(node)) {
            // Return an empty figure.
            Figure newFigure = new CompositeFigure();
            newFigure.setInteractor(getNodeInteractor());
            newFigure.setUserObject(node);
            getController().setFigure(node, newFigure);
            return newFigure;
        } else {
            return super._renderNode(node);
        }
    }

    /** In this base class, return true if the specified node contains an
     *  attribute named "_hide". Derived classes can override this method
     *  to provide more sophisticated methods of choosing which nodes to
     *  display.
     */
    protected boolean _hide(java.lang.Object node) {
        if (node instanceof Locatable) {
            if (((NamedObj)((Locatable)node)
                    .getContainer()).getAttribute("_hide") != null) {
                return true;
            }
        }
        if (node instanceof NamedObj) {
            if (((NamedObj)node).getAttribute("_hide") != null) {
                return true;
            }
        }
        return false;
    }
}
