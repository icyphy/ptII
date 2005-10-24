/*
 * $Id$
 *
 * Copyright (c) 1998-2005 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package ptolemy.apps.vergil.graph;

import ptolemy.actor.*;
import ptolemy.apps.vergil.graph.util.FigureToken;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeController;
import diva.graph.NodeRenderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;


/**
   A factory which creates and returns a NodeFigure given a node input
   to render.

   @author Steve Neuendorffer
*/
public class MetaNodeRenderer extends TypedCompositeActor
    implements NodeRenderer {
    /**
     * Create a renderer which renders nodes square and orange.
     */
    public MetaNodeRenderer(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(FigureToken.TYPE);
    }

    public TypedIOPort output;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetaNodeRenderer newObject = (MetaNodeRenderer) super.clone(workspace);
        newObject.output = (TypedIOPort) newObject.getPort("output");
        return newObject;
    }

    /**
     * Return the graph controller.
     */
    public GraphController getGraphController() {
        return ((NodeController) getContainer()).getController();
    }

    /**
     * Return the rendered visual representation of this node.
     */
    public Figure render(Object node) {
        try {
            prefire();
            fire();
            postfire();

            Receiver[][] receivers = output.getInsideReceivers();
            FigureToken token = (FigureToken) receivers[0][0].get();
            System.out.println("Figure = " + token.getFigure());
            return token.getFigure();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new BasicRectangle(0, 0, 10, 10);
        }
    }
}
