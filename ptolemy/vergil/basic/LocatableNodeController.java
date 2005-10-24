/* The node controller for locatable nodes

 Copyright (c) 1998-2005 The Regents of the University of California.
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

 */
package ptolemy.vergil.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.BasicNodeController;
import diva.graph.GraphController;
import diva.graph.GraphModel;
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
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class LocatableNodeController extends BasicNodeController {
    /** Create an instance associated with the specified graph controller.
     *  @param controller The graph controller.
     */
    public LocatableNodeController(GraphController controller) {
        super(controller);

        NodeInteractor nodeInteractor = (NodeInteractor) getNodeInteractor();
        _dragInteractor = new LocatableNodeDragInteractor(this);
        nodeInteractor.setDragInteractor(_dragInteractor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public mtehods                 ////

    /** Add a node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y) {
        throw new UnsupportedOperationException("Cannot add node.");
    }

    /** Draw the node at its location. This overrides the base class
     *  to assign a location to the object.
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
        } else {
            throw new RuntimeException("The node " + node
                    + "does not have a desired location");
        }
    }

    /** Return true if the node is associated with a desired location.
     *  In this base class, return true if the the node's semantic object is
     *  an instance of Locatable.
     */
    public boolean hasLocation(Object node) {
        if (node instanceof Locatable) {
            Locatable object = (Locatable) node;
            double[] location = object.getLocation();

            if ((location != null) && (location.length == 2)) {
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
        } catch (Exception ex) {
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
        if ((location != null) && (location.length != 2)) {
            throw new RuntimeException("The location " + location
                    + " is not valid");
        }

        if (node instanceof Locatable) {
            ((Locatable) node).setLocation(location);
        } else {
            throw new RuntimeException("The node " + node
                    + "cannot have a desired location");
        }
    }

    /** Specify the snap resolution. The default snap resolution is 5.0.
     *  @param resolution The snap resolution.
     */
    public void setSnapResolution(double resolution) {
        _dragInteractor.setSnapResolution(resolution);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Fourth argument makes this highlight translucent, which enables
     *  combination with other highlights.
     */
    public static Color CLASS_ELEMENT_HIGHLIGHT_COLOR = new Color(255, 64, 64,
            200);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Render the children of the specified node.
     *  This overrides the base class to do nothing if the node
     *  contains a parameter named "_hide" with value true.
     *  @param node The node with children to render.
     */
    protected void _drawChildren(java.lang.Object node) {
        if (!_hide(node)) {
            super._drawChildren(node);
        }
    }

    /** Render the specified node.  This overrides the base class to
     *  return an invisible figure if the node contains a parameter
     *  named "_hide" with value true.  This overrides the base class
     *  to assign a location and to highlight the node if it is an
     *  inherited object, and hence cannot be deleted.
     *  @param node The node to render.
     */
    protected Figure _renderNode(java.lang.Object node) {
        if ((node == null) || _hide(node)) {
            // Return an empty figure.
            Figure newFigure = new CompositeFigure();
            newFigure.setInteractor(getNodeInteractor());
            newFigure.setUserObject(node);
            getController().setFigure(node, newFigure);
            return newFigure;
        } else {
            Figure nf = super._renderNode(node);
            GraphModel model = getController().getGraphModel();
            Object object = model.getSemanticObject(node);
            CompositeFigure cf;

            // Try to get a composite figure that we can add the
            // annotation to.  This is complicated by the fact that
            // ExternalIOPortController wraps its figure in a
            // TerminalFigure, and that there is nothing in the API
            // that enforces that a CompositeFigure is returned.
            // *sigh*
            if (nf instanceof CompositeFigure) {
                cf = (CompositeFigure) nf;
            } else if (nf instanceof TerminalFigure) {
                Figure f = ((TerminalFigure) nf).getFigure();

                if (f instanceof CompositeFigure) {
                    cf = (CompositeFigure) f;
                } else {
                    cf = null;
                }
            } else {
                cf = null;
            }

            if (_decoratable
                    && object instanceof NamedObj
                    && (((NamedObj) object).getDerivedLevel() < Integer.MAX_VALUE)
                    && (cf != null)) {
                float[] dash = { 2.0f, 5.0f };
                Stroke stroke = new BasicStroke(2f, /* width */
                BasicStroke.CAP_SQUARE, /* cap   */
                BasicStroke.JOIN_MITER, /* join  */
                10.0f, /* mitre limit */
                dash, /* dash  */
                0.0f); /* dash_phase  */
                BasicFigure bf = new BasicFigure(cf.getBackgroundFigure()
                        .getBounds());
                bf.setStroke(stroke);
                bf.setStrokePaint(CLASS_ELEMENT_HIGHLIGHT_COLOR);
                cf.add(bf);
            }

            return nf;
        }
    }

    /** In this base class, return true if the specified node contains a
     *  parameter named "_hide" with value true or an attribute that is not
     *  a parameter named "_hide". Derived classes can override this method
     *  to provide more sophisticated methods of choosing which nodes to
     *  display.
     */
    protected boolean _hide(java.lang.Object node) {
        if (node instanceof Locatable) {
            if (_isPropertySet(((Locatable) node).getContainer(), "_hide")) {
                return true;
            }
        }

        if (node instanceof NamedObj) {
            if (_isPropertySet((NamedObj) node, "_hide")) {
                return true;
            }
        }

        return false;
    }

    /** Return true if the property of the specified name is set for
     *  the specified object. A property is specified if the specified
     *  object contains an attribute with the specified name and that
     *  attribute is either not a boolean-valued parameter, or it is
     *  a boolean-valued parameter with value true.
     *  @param object The object.
     *  @param name The property name.
     *  @return True if the property is set.
     */
    protected boolean _isPropertySet(NamedObj object, String name) {
        Attribute attribute = object.getAttribute(name);

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** A flag indicating that the figures associated with this
     *  controller can be decorated to indicate that they are derived.
     *  Some derived classes (like IOPortController) override this
     *  to suppress such decoration. This is true by default.
     */
    protected boolean _decoratable = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The drag interactor, which is remembered so we can change the
     *  snap resolution.
     */
    private LocatableNodeDragInteractor _dragInteractor;
}
