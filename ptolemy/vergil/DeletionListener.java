/* A Listener that deletes nodes in a JGraph

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil;

import ptolemy.vergil.graph.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.toolbox.GraphParser;
import diva.graph.toolbox.GraphWriter;

import diva.canvas.interactor.SelectionModel;
import diva.canvas.Figure;
import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * This class provides deletion support for most simple JGraph components. 
 * Associate this class with some action (such as a key press on an instance 
 * of the JGraph class.  Any nodes or edges in the selection model of the 
 * graph pane's controller will be removed.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class DeletionListener implements ActionListener {
    /** 
     * Delete any nodes or edges from the graph that are currently selected.
     * In addition, delete any edges that are connected to any deleted nodes.
     */
    public void actionPerformed(ActionEvent e) {
	JGraph jgraph = (JGraph) e.getSource();
	GraphPane graphPane = jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	GraphImpl impl = controller.getGraphImpl();
	SelectionModel model = controller.getSelectionModel();
	Object selection[] = model.getSelectionAsArray();
	// Remove all the edges first, since if we remove the nodes first,
	// then removing the nodes might remove some of the edges.
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject =
		    ((Figure)selection[i]).getUserObject();
		if(userObject instanceof Edge) {
		    model.removeSelection(selection[i]);
		    Edge edge = (Edge) userObject;
		    controller.removeEdge(edge);
		}
	    }
	}
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject =
		    ((Figure)selection[i]).getUserObject();
		if(userObject instanceof Node) {
		    model.removeSelection(selection[i]);
		    Node node = (Node) userObject;
		    controller.removeNode(node);
		}
	    }
	}
    }
}
