/* A drag interactor for locatable nodes

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

package ptolemy.schematic.editor;

import ptolemy.moml.*;

import diva.graph.*;
import diva.graph.model.Edge;
import diva.graph.model.Graph;
import diva.graph.model.Node;
import diva.graph.model.CompositeNode;

import diva.canvas.Figure;
import diva.canvas.TransformContext;
import diva.canvas.connector.Connector;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionModel;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeController
/**
An interaction role that drags nodes that have locatable objects
as semantic objects.  When the node is dragged, this interactor
updates the location in the locatable object.

@author Steve Neuendorffer 
@version $Id$
 */
public class LocatableNodeDragInteractor extends NodeDragInteractor {
    /** Create a new interactor contained within the given controller.
     */
    public LocatableNodeDragInteractor(LocatableNodeController controller) {
        _controller = controller;
    }

    /** Drag all selected nodes and move any attached edges.
     *  Update the locatable with the current location.
     */
    public void translate (LayerEvent e, double x, double y) {
        super.translate(e, x, y);
        Iterator targets = targets();
        while (targets.hasNext()) {            
            Figure figure = (Figure) targets.next();
            Object object = figure.getUserObject();
            if(object instanceof Node) {
                Node node = (Node) figure.getUserObject();            
                double[] location = _controller.getLocation(node);
                location[0] += x;
                location[1] += y;
                _controller.setLocation(node, location);
            }
        }
    }

    LocatableNodeController _controller;
}

