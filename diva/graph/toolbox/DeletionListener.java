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
 */
package diva.graph.toolbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import diva.canvas.Figure;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;

/**
 * This class provides deletion support for most simple JGraph
 * components.  Associate this class with some action (such as a key
 * press on an instance of the JGraph class).  Any nodes or edges in
 * the selection model of the graph pane's controller will be removed.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class DeletionListener implements ActionListener {
    /**
     * Delete any nodes or edges from the graph that are currently
     * selected.  In addition, delete any edges that are connected to
     * any deleted nodes.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JGraph jgraph = (JGraph) e.getSource();
        GraphPane graphPane = jgraph.getGraphPane();
        GraphController controller = graphPane.getGraphController();
        GraphModel graphModel = controller.getGraphModel();
        SelectionModel model = controller.getSelectionModel();
        Object[] selection = model.getSelectionAsArray();
        Object[] userObjects = new Object[selection.length];

        // First remove the selection.
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure) selection[i]).getUserObject();
            model.removeSelection(selection[i]);
        }

        // Remove all the edges first, since if we remove the nodes first,
        // then removing the nodes might remove some of the edges.
        for (Object userObject : userObjects) {
            if (graphModel.isEdge(userObject)) {
                controller.removeEdge(userObject);
            }
        }

        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];

            if (graphModel.isNode(userObject)) {
                controller.removeNode(userObject);
            }
        }
    }
}
