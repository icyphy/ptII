/* The node controller for icons of attributes that offers only a configure.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.kernel;

import ptolemy.kernel.util.NamedObj;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;

import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeInteractor;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//////////////////////////////////////////////////////////////////////////
//// ConfigureOnlyAttributeController
/**
This class provides interaction with nodes that represent Ptolemy II
attributes, where only a configure command is offered in the context
menu.  Specifically, this overrides the base class to construct
a default menu creator that offers only a configure command.

@author Edward A. Lee
@version $Id$
*/
public class ConfigureOnlyAttributeController extends AttributeController {

    /** Create an attribute controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public ConfigureOnlyAttributeController(final GraphController controller) {
	super(controller);
        setMenuFactory(new ConfigureOnlyMenuFactory(controller));

        // Add a double click interactor.
	final GraphModel graphModel = controller.getGraphModel();
        Action action = new AbstractAction("Configure") {
	    public void actionPerformed(ActionEvent e) {
                LayerEvent event = (LayerEvent)e.getSource();
                Figure figure = event.getFigureSource();
                Object object = figure.getUserObject();
                NamedObj target =
                         (NamedObj)graphModel.getSemanticObject(object);
                // Create a dialog for configuring the object.
                Component pane = controller.getGraphPane().getCanvas();
                while (pane.getParent() != null) {
                    pane = pane.getParent();
                }
                if (pane instanceof Frame) {
                    // The first argument below is the parent window
                    // (a Frame), which ensures that if this is iconified
                    // or sent to the background, it can be found again.
                    new EditParametersDialog((Frame)pane, target);
                } else {
                    new EditParametersDialog(null, target);
                }
	    }
	};
        ActionInteractor doubleClickInteractor = new ActionInteractor(action);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        // FIXME: this doesn't work... Double click is ignored.

        // Why doesn't getNodeInteractor() return a NodeInteractor?
        ((NodeInteractor)getNodeInteractor())
                 .addInteractor(doubleClickInteractor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    /** The factory for creating a context menu.
     */
    private class ConfigureOnlyMenuFactory extends PtolemyMenuFactory {
	public ConfigureOnlyMenuFactory(GraphController controller) {
	    super(controller);
            // FIXME: The following creates an action just like the
            // one above.  These need to be consolidated.
	    addMenuItemFactory(new EditParametersFactory("Configure"));
	}
    }
}
