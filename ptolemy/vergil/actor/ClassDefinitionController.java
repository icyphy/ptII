/* The node controller for class definitions.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.vergil.actor;

import java.awt.Color;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// ClassDefinitionController
/**
This class provides interaction with nodes that represent Ptolemy II
classes.  This extends the base class by providing mechanisms in the
context menu for creating an instance, creating a subclass,
and converting to an instance.  Since none of these mechanisms
make sense without full access, this class aways assumes full access.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ClassDefinitionController extends ActorController {

    /** Create an entity controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public ClassDefinitionController(GraphController controller) {
        super(controller, FULL);
        
        // The following do not require a configuration.
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_createInstanceAction));

        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_createSubclassAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Draw the node at its location.
     */
    public Figure drawNode(Object node) {
        Figure nf = super.drawNode(node);
        AnimationRenderer decorator = new AnimationRenderer(_HIGHLIGHT_COLOR);
        decorator.renderSelected(nf);
        return nf;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The action that handles creating an instance from a class.
     */
    protected CreateInstanceAction _createInstanceAction
            = new CreateInstanceAction("Create Instance");

    /** The action that handles creating a subclass from a class.
     */
    protected CreateSubclassAction _createSubclassAction
            = new CreateSubclassAction("Create Subclass");
            
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a change request to create an instance or a subclass
     *  of the object.
     *  @param object The class to subclass or instantiate.
     *  @param subclass True to create a subclass, false to create
     *   an instance.
     */
    private void _createChangeRequest(NamedObj object, boolean subclass) {
            
        NamedObj container = (NamedObj)object.getContainer();
        NamedObj context = MoMLChangeRequest
                .getDeferredToParent(container);
        if (context == null) {
            context = container;
        }
        StringBuffer moml = new StringBuffer();
        if (context != container) {
            moml.append("<entity name=\""
                    + container.getName(context)
                    + "\">");
        }
        moml.append("<group name=\"auto\">");
        // FIXME: Can we adjust the location here?
        if (object.isClassDefinition()) {
            if (subclass) {
                moml.append("<class name=\""
                        + "SubclassOf"
                        + object.getName()
                        + "\" extends=\""
                        + object.getName()
                        + "\"/>");
            } else {
                moml.append("<entity name=\""
                        + "InstanceOf"
                        + object.getName()
                        + "\" class=\""
                        + object.getName()
                        + "\"/>");
            }
        } else {
            throw new InternalErrorException(
            "Expected target to be a class definition");
        }
        moml.append("</group>");
        if (context != container) {
            moml.append("</entity>");
        }
        // FIXME
        System.out.println(moml.toString());
        MoMLChangeRequest request = new MoMLChangeRequest(
                this, context, moml.toString());
        context.requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static Color _HIGHLIGHT_COLOR = new Color(200, 200, 240);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /////////////////////////////////////////////////////////////////////
    //// CreateInstanceAction

    // An action to instantiate a class.
    private class CreateInstanceAction extends FigureAction {

        public CreateInstanceAction(String commandName) {
            super(commandName);
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            _createChangeRequest(object, false);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    //// CreateSubclassAction

    // An action to subclass a class.
    private class CreateSubclassAction extends FigureAction {

        public CreateSubclassAction(String commandName) {
            super(commandName);
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the
            // create subclass action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            _createChangeRequest(object, true);
        }
    }
}
