/* A first try at a ptolemy gui

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

import ptolemy.schematic.util.*;
import diva.graph.schematic.*;
import diva.graph.JGraph;
import diva.graph.GraphPane;
import diva.graph.BasicGraphController;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.RandomLayout;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.model.GraphModel;
import diva.util.gui.TutorialWindow;

//for layout widget
import diva.graph.model.Graph;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;

/**
 * This is the most basic tutorial, popping up an empty graph
 * editing window.  Control-click to add nodes,
 * select a node and control-drag to create new edges.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu
 * @version $Id$
 * @rating Red
 */
public class Editor {
    LayoutTarget _target;
    GlobalLayout _layout;
    JGraph _editor;
    
    /**
     * Pop up an empty graph editing window.
     */
    public static void main(String argv[]) {
        new Editor();
    }

    public Editor() {
        TutorialWindow f = new TutorialWindow("Simple");
	GraphPane pane = new GraphPane(new EditorGraphController(), 
				       new SchematicGraphImpl());
	_editor = new JGraph(pane);

	f.getContentPane().add("Center", _editor);
        f.setSize(800, 600);
        f.setVisible(true);
    }
}

