/* An attribute that produces a custom node controller for image icons.

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

package ptolemy.vergil.icon;

import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.GraphController;
import ptolemy.kernel.util.*;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.kernel.AttributeController;

//////////////////////////////////////////////////////////////////////////
//// ImageAttributeControllerFactory
/**
This is attribute that produces a custom node controller for icons that
are images.  It provides a context menu suitable for an attribute as
well as an interactor that provides for resizing.
To use this class, just insert it as an attribute inside
a Ptolemy II attribute, and then right clicking on the icon for
that object will result in the use of the controller specified
here.  The instance by convention will be named "_controllerFactory",
but the only reason to enforce this is that only the first such
controller factory found as an attribute will be used.
It is a singleton, so placing it any container will replace any
previous controller factory with the same name.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ImageAttributeControllerFactory extends NodeControllerFactory {

    /** Construct a new attribute with the given container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name.
     */
    public ImageAttributeControllerFactory(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new node controller.  This base class returns an
     *  instance of IconController.  Derived
     *  classes can return some other class to customize the
     *  context menu.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    public NamedObjController create(GraphController controller) {
        return new ImageController(controller);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        inner classes                      ////
    
    /** Custom controller that uses a bounds manipulator to allow
     *  the user to resize the image.
     */
    public class ImageController extends AttributeController {
        
        /** Create a controller associated with the specified graph
         *  controller.  The attribute controller is given full access.
         *  @param controller The associated graph controller.
         */
        public ImageController(GraphController controller) {
            super(controller);
            SelectionInteractor interactor =
                    (SelectionInteractor) getNodeInteractor();

            // Create and set up the manipulator for connectors
            BoundsManipulator manipulator = new BoundsManipulator();
            interactor.setPrototypeDecorator(manipulator);
        }
    }
    
    // FIXME: Subclass BoundsManipulator here with a mouseReleased()
    // method that notifies the actor, via a MoMLChangeRequest, of the
    // change in size.  This will mean the change is undoable and
    // persistent.  See vergil.basic.LocatableNodeDragInteractor
    // for an example of such a mouseReleased() method.
    
    
}
