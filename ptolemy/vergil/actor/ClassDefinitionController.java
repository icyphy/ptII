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

import ptolemy.kernel.Entity;
import ptolemy.kernel.Prototype;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.IncrLayoutAdapter;
import diva.graph.layout.IncrementalLayoutListener;
import diva.gui.GUIUtilities;
import diva.util.Filter;

//////////////////////////////////////////////////////////////////////////
//// ClassDefinitionController
/**
This class provides interaction with nodes that represent Ptolemy II
classes.  This extends the base class by providing mechanisms in the
context menu for creating an instance, creating a subclass,
and converting to an instance.
<p>
NOTE: There should be only one instance of this class associated with
a given GraphController. This is because this controller listens for
changes to the graph and re-renders the ports of any actor instance
in the graph when the graph changes. If there is more than one instance,
this rendering will be done twice, which can result in bugs like port
labels appearing twice.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ClassDefinitionController extends ActorController {

    /** Create an actor instance controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public ClassDefinitionController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a controller associated with the specified graph
     *  controller with the specified access.
     *  @param controller The associated graph controller.
     *  @param access The access level, one of FULL or PARTIAL.
     */
    public ClassDefinitionController(GraphController controller, Access access) {
        super(controller, access);
        
        if (access == FULL) {
            // The following do not require a configuration.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_createInstanceAction));

            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_createSubclassAction));                
        }

        // Set up a listener to lay out the ports when graph changes.
        // NOTE: Because of this listener, it is imperative that there
        // be no more than one instance of this object associated with
        // a graph controller!  If there is more than one instance, the
        // ports will be laid out more than once. This manifests itself
        // as a bug where port names are rendered twice, and for some
        // inexplicable reason, are rendered in two different places!
        
        // The filter for the layout algorithm of the ports within this
        // entity. This returns true only if the candidate object is
        // an instance of Locatable and the semantic object associated
        // with it is an instance of Entity.
        Filter portFilter = new Filter() {
                public boolean accept(Object candidate) {
                    GraphModel model = getController().getGraphModel();
                    Object semanticObject = model.getSemanticObject(candidate);
                    if (candidate instanceof Locatable
                            && semanticObject instanceof Entity
                            && ((Entity)semanticObject).isClassDefinition()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };

        // Anytime we add a port to an entity, we want to layout all the
        // ports within that entity.
        GlobalLayout layout = new EntityLayout();
        controller.addGraphViewListener(
                new IncrementalLayoutListener(
                        new IncrLayoutAdapter(layout) {
                                public void nodeDrawn(Object node) {
                                    layout(node);
                                }
                            }, portFilter));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Draw the node at its location. This overrides the base class
     *  to highlight the actor to indicate that it is a class definition.
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
        StringBuffer moml = new StringBuffer();
        moml.append("<group name=\"auto\">");
        // FIXME: Can we adjust the location here?
        // NOTE: This controller is expected to be used
        // only for class definitions, which must be instances
        // of Prototype, so this cast should be safe.b
        if (((Prototype)object).isClassDefinition()) {
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
        MoMLChangeRequest request = new MoMLChangeRequest(
                this, container, moml.toString());
        container.requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Fourth argument makes this highlight transluscent, which enables
    // combination with other highlights.
    private static Color _HIGHLIGHT_COLOR = new Color(0, 0, 255, 64);

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
            // If access is not full, do nothing.
            if (_access != FULL) {
                return;
            }
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
            // If access is not full, do nothing.
            if (_access != FULL) {
                return;
            }
            // Determine which entity was selected for the
            // create subclass action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            _createChangeRequest(object, true);
        }
    }
}
