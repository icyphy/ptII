/* The node controller for locatable nodes

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy;

import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.graph.BasicNodeController;
import diva.graph.GraphController;
import diva.graph.GraphViewEvent;
import diva.graph.NodeInteractor;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Location;
import ptolemy.vergil.toolbox.SnapConstraint;

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
coordinate given by the Location.  A LocatableNodeDragInteractor
is used to update the location of the node as the figure moves.

@author Steve Neuendorffer
@version $Id$
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
        if(hasLocation(node)) {
            return ((Location) node).getLocation();
        } else throw new RuntimeException("The node " + node +
                "does not have a desired location");
    }

    /** Return true if the node is associated with a desired location.
     *  In this base class, return true if the the node's semantic object is
     *  an instance of Location.
     */
    public boolean hasLocation(Object node) {
        if(node instanceof Location) {
            Location object = (Location) node;
            double[] location = object.getLocation();
            if(location != null) return true;
        }
        return false;
    }

    /** Move the node's figure to the location specified in the node's
     *  semantic object, if that object is an instance of Location.
     *  If the semantic object is not a location, then do nothing.
     *  If the figure associated with the semantic object is an instance
     *  of TerminalFigure, then modify the location to ensure that the
     *  connect site snaps to grid.
     *  @param node The object to locate.
     */
    public void locateFigure(Object node) {
	Figure nf = getController().getFigure(node);
	if(hasLocation(node)) {
	    double[] location = getLocation(node);
            // NOTE: It might make sense to modify the translation of
            // all objects so that the logical zero location is located
            // at a grid point.  This would take over the rather complicated
            // operation in LocatableNodeDragInteractor.  However, this
            // would be tricky for composite figures.  Best to leave it
            // alone.  EAL
            if (nf instanceof TerminalFigure) {
                // Snap connect site to grid.
                Site connectSite = ((TerminalFigure)nf).getConnectSite();
                Point2D connectPoint = connectSite.getPoint();
                Rectangle2D bounds = nf.getBounds();
                double[] preSnapSiteLocation = new double[2];
                preSnapSiteLocation[0] = location[0]
                       + connectPoint.getX() - bounds.getCenterX();
                preSnapSiteLocation[1] = location[1]
                       + connectPoint.getY() - bounds.getCenterY();
                double[] postSnapSiteLocation = SnapConstraint
                       .constrainPoint(preSnapSiteLocation);
                // Translate back.
                location[0] = postSnapSiteLocation[0]
                       - connectPoint.getX() + bounds.getCenterX();
                location[1] = postSnapSiteLocation[1]
                       - connectPoint.getY() + bounds.getCenterY();
                // Record the new location, otherwise it will get
                // quantized again.
                try {
                    setLocation(node, location);
                } catch (IllegalActionException ex) {
                    // Ignore... not critical, and shouldn't happen.
                }
            }
	    CanvasUtilities.translateTo(nf, location[0], location[1]);
        }
    }

    /** Set the desired location of this node.  Throw an exception if the
     *  node can not be given a desired location.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void setLocation(Object node, double[] location)
            throws IllegalActionException {
	if(node instanceof Location) {
            ((Location)node).setLocation(location);
        } else throw new RuntimeException("The node " + node +
                "cannot have a desired location");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Render the children of the specified node.
     *  This overrides the base class to do nothing if the node
     *  contains an attribute named "_hide".
     *  @param The node with children to render.
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
        if (_hide(node)) {
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
        if (node instanceof Location) {
            if (((NamedObj)((Location)node)
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
