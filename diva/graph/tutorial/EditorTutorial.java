/*
 * $Id$
 *
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

package diva.graph.tutorial;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.basic.BasicGraphController;
import diva.graph.basic.BasicGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.RandomLayout;
import diva.gui.AppContext;
import diva.gui.BasicFrame;

/**
 * This turotial expands on the simple tutorial and turns it into
 * a more complete graph editor.  Along with the editing window,
 * the user has the ability to layout the graph.
 * Control-click to add nodes,
 * select a node and control-drag to create new edges.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class EditorTutorial {
    LayoutTarget _target;
    GlobalLayout _layout;
    JGraph _editor;
    BasicGraphModel _model;

    /**
     * Pop up an empty graph editing window.
     */
    public static void main(String argv[]) {
        final AppContext context = new BasicFrame("Editor Tutorial");
        context.setSize(800, 600);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new EditorTutorial(context);
                context.setVisible(true);
            }
        });
    }

    public EditorTutorial(AppContext context) {
        _model = new BasicGraphModel();
        GraphPane pane = new GraphPane(new BasicGraphController(), _model);
        _editor = new JGraph(pane);
        context.getContentPane().add("Center", _editor);
        _target =
            new BasicLayoutTarget(_editor.getGraphPane().getGraphController());
        _layout = new LevelLayout(_target);
        LayoutWidget lw = new LayoutWidget(_target, _model.getRoot(), true);
        context.getContentPane().add("South", lw);
    }

    /**
     * An inner class for layout.
     */
    public class LayoutActionListener implements java.awt.event.ActionListener {
        public void actionPerformed(ActionEvent e) {
            _layout.layout(_model.getRoot());
        }
    }

    /**
     * A widget for configuring layouts.
     */
    public class LayoutWidget extends JPanel {
        /**
         * The layout target that this layout will be performed on.
         */
        private LayoutTarget _layoutTarget;

        /**
         * The graph that will be layed out.
         */
        private Object _graph;

        /**
         * Mapping from layout name to layout object.
         */
        private HashMap _nameMap;

        /**
         * Mapping from layout object to configuration widget.
         */
        private HashMap _configMap;

        /**
         * List that user selects from to choose the layout.
         */
        JComboBox _layoutList;

        /**
         * Button to apply the layout.
         */
        JButton _applyBtn;

        /**
         * Set up the basic data structures, initialize the GUI, and then
         * initialize some default layout styles if the useDefaults parameter
         * is true.  All layouts will be performed on the given layout target.
         */
        public LayoutWidget(LayoutTarget lt, Object graph, boolean useDefaults) {
            _layoutTarget = lt;
            _graph = graph;
            _nameMap = new HashMap();
            _configMap = new HashMap();

            _applyBtn = new JButton("Apply layout");
            _applyBtn.setEnabled(false);
            _applyBtn.addActionListener(new LayoutActionListener());
            _layoutList = new JComboBox();
            _layoutList.setEditable(false);
            _layoutList.addActionListener(new SelectionListener());

            setLayout(new GridLayout(1, 2));
            add(_applyBtn);
            add(_layoutList);

            if(useDefaults) {
                initDefaultLayouts();
            }
        }

        /**
         * A method that initalizes a bunch of default layouts.
         */
        public void initDefaultLayouts() {
            addLayout("Random", new RandomLayout(_target), null);
            addLayout("Levelized", new LevelLayout(_target), null);
        }

        /**
         * Add a layout to the widget, with the given name, layout
         * engine and widget for configuration of the layout.  Pass a
         * null pointer for the configuration widget if this is not
         * applicable.
         */
        public void addLayout(String name, GlobalLayout layout, JPanel config) {
            _nameMap.put(name, layout);
            _configMap.put(layout, config);
            _layoutList.addItem(name);

            //if the list was empty before
            _applyBtn.setEnabled(true);
        }

        private class SelectionListener implements java.awt.event.ActionListener {
            public void actionPerformed(ActionEvent e) {
                String name = (String)_layoutList.getSelectedItem();
            }
        }

        private class LayoutActionListener implements java.awt.event.ActionListener {
            public void actionPerformed(ActionEvent e) {
                String name = (String)_layoutList.getSelectedItem();
                GlobalLayout l = (GlobalLayout)_nameMap.get(name);
                l.layout(_graph);
            }
        }
    }
}



