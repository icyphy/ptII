/* An attribute that produces a custom node controller for resizable icons.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.AttributeBoundsManipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// ResizableAttributeControllerFactory

/**
 This is attribute that produces a custom node controller for icons that
 can be interactively resized. It provides a context menu suitable for
 an attribute as well as an interactor that provides for resizing.
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
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ResizableAttributeControllerFactory extends NodeControllerFactory {
    /** Construct a new attribute with the given container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name.
     */
    public ResizableAttributeControllerFactory(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
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
    @Override
    public NamedObjController create(GraphController controller) {
        return new ResizeAttributeController(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Custom controller that uses a bounds manipulator to allow
     *  the user to resize the image.
     */
    public class ResizeAttributeController extends AttributeController {
        /** Create a controller associated with the specified graph
         *  controller.  The attribute controller is given full access.
         *  @param controller The associated graph controller.
         */
        public ResizeAttributeController(GraphController controller) {
            super(controller);

            SelectionInteractor interactor = (SelectionInteractor) getNodeInteractor();

            // Create and set up the manipulator for connectors.
            // FIXME: This is only a prototype.  But due to a Diva bug,
            // the prototype is what gets the mouse events!  I.e., not
            // the instance decorator that is created from the prototype!
            NamedObj container = getContainer();
            _manipulator = new AttributeBoundsManipulator(container);

            interactor.setPrototypeDecorator(_manipulator);
        }

        /** Specify the snap resolution. The default snap resolution is 5.0.
         *  This overrides the base class to set the snap resolution on the
         *  bounds manipulator as well as the drag interactor.
         *  @param resolution The snap resolution.
         */
        @Override
        public void setSnapResolution(double resolution) {
            super.setSnapResolution(resolution);
            _manipulator.setSnapResolution(resolution);
        }

        private AttributeBoundsManipulator _manipulator;
    }
}
