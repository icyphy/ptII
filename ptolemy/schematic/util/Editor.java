/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */

package ptolemy.schematic.util;

import diva.graph.JGraph;
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
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
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
        GraphModel model = new GraphModel(new SchematicGraphImpl());
        _editor = new JGraph(model);
        f.getContentPane().add("Center", _editor);
        f.setSize(800, 600);
        f.setVisible(true);
    }


}

