/* The node controller for locatable nodes

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.vergil.graph;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeController
/**
This node controller provides interaction techniques for nodes that are
associated with locatable objects.   This is common when the node has some
concept of its graphical location, but does not know about the figure that it
is associated with.  This class provides the connection between the
figure's notion of location and the node's concept of location.
<p>
When nodes are drawn, they are automatically placed at the
coordinate given by the Locatable interface, if that interface is
implemented by the object attached to the node.  A LocatableNodeDragInteractor
is used to update the location of the node as the figure moves.

@author Steve Neuendorffer
@version $Id$
*/
public class LocatableNodeController extends NodeController {
    public LocatableNodeController(GraphController controller) {
	super(controller);
        NodeInteractor nodeInteractor = (NodeInteractor) getNodeInteractor();
        nodeInteractor.setDragInteractor(new LocatableNodeDragInteractor(this));
    }

    /** Add a node to this graph editor and render it
     * at the given location.
     */
    public Node addNode(Object semanticObject, double x, double y) {
        Node node = super.addNode(semanticObject, x, y);
        double location[] = new double[2];
        location[0] = x;
        location[1] = y;
        setLocation(node, location);
        return node;
    }

    /** Draw the node at it's location.
     */
    public Figure drawNode(Node n) {
        Figure nf = super.drawNode(n);
        locateFigure(n);
        return nf;
    }

    /** Return true if the node is associated with a desired location.
     *  In this base class, return true if the the node's semantic object is
     *  an instance of Locatable.
     */
    public boolean hasLocation(Node n) {
        if(n.getSemanticObject() instanceof Locatable) {
            Locatable object = (Locatable) n.getSemanticObject();
            double[] location = object.getLocation();
            if(location != null) return true;
        }
        return false;
    }

    /** Return the desired location of this node.  Throw an exception if the
     *  node does not have a desired location.
     */
    public double[] getLocation(Node n) {
        Object object = n.getSemanticObject();
        if(hasLocation(n)) {
            return ((Locatable) object).getLocation();
        } else throw new GraphException("The node " + n +
                "does not have a desired location");
    }

    /** Set the desired location of this node.  Throw an exception if the
     *  node can not be given a desired location.
     */
    public void setLocation(Node n, double[] location) {
        Object object = n.getSemanticObject();
        if(object instanceof Locatable) {
            ((Locatable)object).setLocation(location);
        } else throw new GraphException("The node " + n +
                "can not have a desired location");
    }

    /** Move the node's figure to the location specified in the node's
     *  semantic object, if that object is an instance of Locatable.
     *  If the semantic object is not locatable, then do nothing.
     */
    public void locateFigure(Node n) {
        Figure nf = (Figure)n.getVisualObject();
        if(hasLocation(n)) {
            double[] location = getLocation(n);
            CanvasUtilities.translateTo(nf, location[0], location[1]);
        }
    }
}




