/* The node controller for actor instances.

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

import java.awt.event.ActionEvent;

import ptolemy.kernel.Prototype;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

//////////////////////////////////////////////////////////////////////////
//// ActorInstanceController
/**
This class provides interaction with nodes that represent Ptolemy II
actor instances (i.e., not classes).  This extends the base class by
providing a mechanism in the context menu for converting the instance
into a class.  Since this mechanism only make sense with full access,
this class aways assumes full access.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ActorInstanceController extends ActorController {

    /** Create an entity controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public ActorInstanceController(GraphController controller) {
        super(controller, FULL);
        
        // The following do not require a configuration.
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_convertToClassAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The action that handles creating an instance from a class.
     */
    protected ConvertToClassAction _convertToClassAction
            = new ConvertToClassAction("Convert to Class");

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /////////////////////////////////////////////////////////////////////
    //// CreateInstanceAction

    // An action to instantiate a class.
    private class ConvertToClassAction extends FigureAction {

        public ConvertToClassAction(String commandName) {
            super(commandName);
        }

        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);
            
            NamedObj object = getTarget();            
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
            // Assumes MoML parser will convert to class.
            // NOTE: This cast should be safe because this controller is
            // used for actors.
            if (!((Prototype)object).isClassDefinition()) {
                moml.append("<class name=\""
                        + object.getName()
                        + "\"/>");
            } else {
                // Object is already a class. Do nothing.
            }
            if (context != container) {
                moml.append("</entity>");
            }
            MoMLChangeRequest request = new MoMLChangeRequest(
                    this, context, moml.toString());
            context.requestChange(request);
        }
    }
}
