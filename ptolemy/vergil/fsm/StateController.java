/* The node controller for states.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.vergil.fsm;

import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;
import ptolemy.actor.TypedActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.*;
import ptolemy.moml.Location;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;

import javax.swing.Action;
import java.awt.event.ActionEvent;

//////////////////////////////////////////////////////////////////////////
//// StateController
/**
This class provides interaction with nodes that represent states in an
FSM graph.  It provides a double click binding to edit the parameters
of the state, and a context menu containing a commands to edit parameters
("Configure"), rename, get documentation, and look inside.  The looks
inside command opens the refinement of the state, if it exists.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class StateController extends AttributeController {

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public StateController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public StateController(GraphController controller, Access access) {
	super(controller, access);
	setNodeRenderer(new StateRenderer());

        // NOTE: This requires that the configuration be non null,
        // or it will report an error.
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new LookInsideAction()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to look inside a state at its refinement, if it has one.
     *  NOTE: This requires that the configuration be non null, or it
     *  will report an error with a fairly cryptic message.
     */
    private class LookInsideAction extends FigureAction {
	public LookInsideAction() {
	    super("Look Inside");
	}
	public void actionPerformed(ActionEvent e) {

            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot look inside without a configuration.");
                return;
            }
	    super.actionPerformed(e);
	    NamedObj target = getTarget();
            // If the target is not an instance of State, do nothing.
            if (target instanceof State) {
                try {
                    TypedActor[] refinements = ((State)target).getRefinement();
                    if (refinements != null && refinements.length > 0) {
                        for (int i = 0; i < refinements.length; i++) {
                            // Open each refinement.
                            _configuration.openModel((NamedObj)refinements[i]);
                        }
                    } else {
                        MessageHandler.error("State has no refinement.");
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Look inside failed: ", ex);
                }
            }
	}
    }

    /** Render the state as a circle.
     */
    public static class StateRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Location location = (Location)n;
	    NamedObj object = (NamedObj) location.getContainer();
	    EditorIcon icon;
            try {
                icon = (EditorIcon)object.getAttribute("_icon");
		if (icon == null) {
		    icon = new XMLIcon(object, "_icon");
		}
	    } catch (KernelException ex) {
		throw new InternalErrorException("could not create icon " +
                        "in " + object + " even " +
                        "though one did not exist");
	    }

	    Figure figure = icon.createFigure();
            figure.setToolTipText(object.getName());
	    return figure;
	}
    }
}
