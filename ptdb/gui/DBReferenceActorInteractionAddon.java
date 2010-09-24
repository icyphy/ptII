/*
@Copyright (c) 2010 The Regents of the University of California.
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

package ptdb.gui;

import javax.swing.JOptionPane;

import diva.graph.GraphController;

import ptdb.common.dto.XMLDBModel;
import ptolemy.data.expr.StringConstantParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.actor.ActorInteractionAddon;
import ptolemy.vergil.toolbox.FigureAction;

/**
 * Implementation of interface for interaction with actors.  This
 * implementation defines how to interact with database reference actors.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */
public class DBReferenceActorInteractionAddon implements ActorInteractionAddon {

    /** Determine of a given actor is a database reference actor.
     *
     * @param actor The actor of interest.
     * @return True if the actor is a database reference actor, False otherwise.
     */
    public boolean isActorOfInterestForLookInside(NamedObj actor) {

        if (actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {

            if (actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR)
                instanceof StringConstantParameter &&
                ((StringParameter) actor
                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                .getExpression().equals("TRUE")) {

                return true;

            }

        }

        return false;
    }

    /**
     * Show a dialog box warning the user that changes will not be propagated
     * to the database reference model.
     * @param figureAction The FigureAction from which the call is being made.
     * @param actor The actor being opened.
     * @exception IllegalActionException Thrown for illegal action.
     * @exception NameDuplicationException Thrown for name duplication.
     */
    public void lookInsideAction(FigureAction figureAction, NamedObj actor)
            throws IllegalActionException, NameDuplicationException {

        JOptionPane.showMessageDialog(figureAction.getFrame(),
                "Changes to this actor will not " +
                "be saved to the database.  " +
                "To make changes to the " +
                "referenced model, open " +
                "it from the database.",
                "Open Actor",
                JOptionPane.WARNING_MESSAGE, null);

        String referenceTag = "<property name=\"" +
            ActorGraphDBFrame.DB_NO_EDIT_ATTR + "\" " +
            "class=\"ptolemy.data.expr.StringConstantParameter\" " +
            "value=\"TRUE\"></property>";

        MoMLChangeRequest change = new MoMLChangeRequest(null,
                actor, referenceTag);

        change.setUndoable(true);
        actor.requestChange(change);

    }

    /**
     * Get an instance of a DBActorController to be used for control of
     * database reference actors.
     * @param controller The associated graph controller.
     * @param fullAccess Indication if the controller should be instantiated
     *                  with Full access.
     * @return An new instance a DBActorController.
     */
    public ActorController getControllerInstance(GraphController controller, boolean fullAccess) {

        if (fullAccess) {

            return new DBActorController(controller, ActorController.FULL);

        } else {

            return new DBActorController(controller, ActorController.PARTIAL);

        }

    }

    /**
     * Get an instance of a DBActorController to be used for control of
     * database reference actors.  This assumes full access.
     * @param controller The associated graph controller.
     * @return An instance of the appropriate controller.
     */
    public ActorController getControllerInstance(GraphController controller) {
        return new DBActorController(controller, ActorController.FULL);
    }

    /** Determine of a given actor is a database reference actor.
     *
     * @param actor The actor of interest.
     * @return True if the actor is a database reference actor, False otherwise.
     */
    public boolean isActorOfInterestForOpenInstance(NamedObj actor) {

        if (actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {

            if (actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR)
                instanceof StringConstantParameter &&
                ((StringParameter) actor
                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                .getExpression().equals("TRUE")) {

                return true;

            }

        }

        return false;

    }

    /**
     * Show a dialog box warning the user that changes will not be propagated
     * to the database reference model.
     * @param figureAction The FigureAction from which the call is being made.
     * @param actor The actor being opened.
     * @exception IllegalActionException Thrown for illegal action.
     * @exception NameDuplicationException Thrown for name duplication.
     */
    public void openInstanceAction(FigureAction figureAction, NamedObj actor)
            throws IllegalActionException, NameDuplicationException {
        JOptionPane.showMessageDialog(figureAction.getFrame(),
                "Changes to this instance will not " +
                "be saved to the database.  " +
                "To make changes to the " +
                "referenced model, open " +
                "it from the database.",
                "Open Instance",
                JOptionPane.WARNING_MESSAGE, null);

        String referenceTag = "<property name=\"" +
        ActorGraphDBFrame.DB_NO_EDIT_ATTR + "\" " +
        "class=\"ptolemy.data.expr.StringConstantParameter\" " +
        "value=\"TRUE\"></property>";

        MoMLChangeRequest change = new MoMLChangeRequest(null,
                actor, referenceTag);

        change.setUndoable(true);
        actor.requestChange(change);

    }

    /** Determine of a given actor is a database reference actor.
     *
     * @param actor The actor of interest.
     * @return True if the actor is a database reference actor, False otherwise.
     */
    public boolean isActorOfInterestForAddonController(NamedObj actor) {

        if (actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {

            if (actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR)
                instanceof StringConstantParameter &&
                ((StringParameter) actor
                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                .getExpression().equals("TRUE")) {

                return true;

            }

        }

        return false;
    }

}
