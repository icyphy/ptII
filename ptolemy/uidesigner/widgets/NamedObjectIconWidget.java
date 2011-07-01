/* TODO
 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.uidesigner.widgets;

import java.awt.Dimension;
import java.net.URL;

import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.graph.GraphPane;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////
//// NamedObjectIconWidget

/**
* TODO
* @author Anar Huseynov
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class NamedObjectIconWidget extends Widget implements
        NamedObjectWidgetInterface {

    /**
     * TODO
     * @param scene
     * @param namedObject
     * @param imageURL
     */
    public NamedObjectIconWidget(Scene scene, NamedObj namedObject, URL imageURL) {
        super(scene);
        _namedObject = namedObject;
        // adapted from DocViewer
        ActorEditorGraphController controller = new ActorEditorGraphController();
        // Create a modified graph model with alternative error reporting.
        ActorGraphModel graphModel = new ActorGraphModel(namedObject);
        GraphPane _graphPane = new GraphPane(controller, graphModel);
        JGraph jgraph = new JGraph(_graphPane);
        // The icon window is fixed size.
        jgraph.setMinimumSize(new Dimension(_ICON_WINDOW_WIDTH,
                _ICON_WINDOW_HEIGHT));
        jgraph.setMaximumSize(new Dimension(_ICON_WINDOW_WIDTH,
                _ICON_WINDOW_HEIGHT));
        jgraph.setPreferredSize(new Dimension(_ICON_WINDOW_WIDTH,
                _ICON_WINDOW_HEIGHT));
        jgraph.setSize(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT);
        jgraph.setBackground(BasicGraphFrame.BACKGROUND_COLOR);
        ComponentWidget componentWidget = new ComponentWidget(scene, jgraph);
        addChild(componentWidget);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* TODO
     *  (non-Javadoc)
     * @see ptolemy.uidesigner.widgets.NamedObjectWidgetInterface#getNamedObject()
     */
    public NamedObj getNamedObject() {
        return _namedObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * TODO
     */
    private final NamedObj _namedObject;

    /** Icon window width. */
    private static int _ICON_WINDOW_HEIGHT = 200;

    /** Icon window width. */
    private static int _ICON_WINDOW_WIDTH = 200;
}
