/*
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package diva.graph;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.GrabHandle;

/** An interactor that interactively drags edges from one node
 * to another.
 *
 * @author         Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version        $Id$
 * @rating      Red
 */
public abstract class EdgeCreator extends AbstractInteractor {
    // The Controller that this creator is using.
    GraphController _controller;

    public EdgeCreator(GraphController controller) {
        _controller = controller;
    }

    /** Create a new edge, add it to the graph controller and add
     * the connector to the selection.
     */
    public void mousePressed(LayerEvent e) {
        Figure source = e.getFigureSource();
        FigureLayer layer = (FigureLayer) e.getLayerSource();

        Object edge = createEdge();

        // Add it to the editor
        // FIXME what about an error?
        _controller.addEdge(edge,
                source.getUserObject(),
                ConnectorEvent.TAIL_END,
                e.getLayerX(),
                e.getLayerY());

        // Add it to the selection so it gets a manipulator, and
        // make events go to the grab-handle under the mouse
        Figure ef = _controller.getFigure(edge);
        _controller.getSelectionModel().addSelection(ef);
        ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
        GrabHandle gh = cm.getHeadHandle();
        layer.grabPointer(e, gh);
    }

    /** Create a new Edge.  Subclasses should implement this method to create
     * an object that is consistent with the graphmodel being used.
     */
    public abstract Object createEdge();
}



