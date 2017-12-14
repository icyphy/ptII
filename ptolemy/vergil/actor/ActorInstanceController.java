/* The node controller for actor instances.

 Copyright (c) 2003-2016 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.awt.event.ActionEvent;

import diva.graph.GraphController;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;

///////////////////////////////////////////////////////////////////
//// ActorInstanceController

/**
 This class provides interaction with nodes that represent Ptolemy II
 actor instances (i.e., not classes).  This extends the base class by
 providing a mechanism in the context menu for converting the instance
 into a class.
 <p>
 NOTE: There should be only one instance of this class associated with
 a given GraphController. This is because this controller listens for
 changes to the graph and re-renders the ports of any actor instance
 in the graph when the graph changes. If there is more than one instance,
 this rendering will be done twice, which can result in bugs like port
 labels appearing twice.

 @author Edward A. Lee and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ActorInstanceController extends ActorController {
    /** Create an actor instance controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public ActorInstanceController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an entity controller associated with the specified graph
     *  controller with the specified access.
     *  @param controller The associated graph controller.
     *  @param access The access.
     */
    public ActorInstanceController(GraphController controller, Access access) {
        super(controller, access);

        if (access == FULL) {
            // The following do not require a configuration.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_convertToClassAction));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The action that handles creating an instance from a class.
     */
    protected ConvertToClassAction _convertToClassAction = new ConvertToClassAction(
            "Convert to Class");

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// ConvertToClassAction
    // An action to convert an instance to a class.
    @SuppressWarnings("serial")
    private class ConvertToClassAction extends FigureAction {
        public ConvertToClassAction(String commandName) {
            super(commandName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // If access is not full, do nothing.
            if (_access != FULL) {
                return;
            }

            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);

            NamedObj object = getTarget();
            NamedObj container = object.getContainer();

            // Assumes MoML parser will convert to class.
            // NOTE: This cast should be safe because this controller is
            // used for actors.
            if (((Instantiable) object).isClassDefinition()) {
                // Object is already a class. Do nothing.
                return;
            }

            String moml = "<class name=\"" + object.getName() + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                    moml);
            container.requestChange(request);
        }
    }
}
