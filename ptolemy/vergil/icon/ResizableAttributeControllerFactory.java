/* An attribute that produces a custom node controller for resizable icons.

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

import java.awt.geom.Rectangle2D;

import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.GraphController;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.kernel.AttributeController;

//////////////////////////////////////////////////////////////////////////
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
@since Ptolemy II 2.0
*/
public class ResizableAttributeControllerFactory
    extends NodeControllerFactory {

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
    public NamedObjController create(GraphController controller) {
        return new ResizeAttributeController(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        inner classes                      ////

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
            SelectionInteractor interactor =
                (SelectionInteractor) getNodeInteractor();

            // Create and set up the manipulator for connectors.
            // FIXME: This is only a prototype.  But due to a Diva bug,
            // the prototype is what gets the mouse events!  I.e., not
            // the instance decorator that is created from the prototype!
            BoundsManipulator manipulator = new AttributeBoundsManipulator();

            interactor.setPrototypeDecorator(manipulator);
        }
    }

    /** Custom bounds manipulator that records the new size when the
     *  mouse is released.
     */
    public class AttributeBoundsManipulator extends BoundsManipulator {

        // FIXME: Need the resizing to snap to grid.

        /** Make a persistent record of the new size by issuing a change request.
         *  @param e The mouse event.
         */
        public void mouseReleased(LayerEvent e) {
            Figure child = getChild();
            // FIXME: Diva has a bug where this method is called on the
            // prototype rather than the instance that has a child.
            // We work around this by getting access to the instance.
            if (child == null && _instanceDecorator != null) {
                child = _instanceDecorator.getChild();
            }
            if (child != null) {
                // NOTE: Calling getBounds() on the child itself yields an
                // inaccurate bounds, for some reason.
                // Weirdly, to get the size right, we need to use this.
                // But to get the location right, we need the other!
                Rectangle2D bounds = child.getShape().getBounds2D();
                Rectangle2D childBounds = child.getBounds();

                // Use a MoMLChangeRequest here so that the resize can be
                // undone and so that a repaint occurs.
                NamedObj container =
                    (NamedObj) ResizableAttributeControllerFactory
                        .this
                        .getContainer();
                NamedObj context =
                    MoMLChangeRequest.getDeferredToParent(container);
                if (context == null) {
                    context = container;
                }

                Attribute widthParameter = container.getAttribute("width");
                Attribute heightParameter = container.getAttribute("height");
                Attribute locationParameter =
                    container.getAttribute("_location");

                // Proceed only if the container has these parameters.
                if (widthParameter != null && heightParameter != null) {
                    StringBuffer command =
                        new StringBuffer("<group><property name =\"");
                    command.append(widthParameter.getName(context));
                    command.append("\" value=\"");
                    // FIXME: Force to integer values only. Is this a good idea?
                    command.append(Math.rint(bounds.getWidth()));
                    command.append("\"/><property name =\"");
                    command.append(heightParameter.getName(context));
                    command.append("\" value=\"");
                    command.append(Math.rint(bounds.getHeight()));
                    command.append("\"/>");

                    // Location needs to change too if dragged left or down.
                    if (locationParameter != null) {
                        // Weirdly, to get the location right, need to use
                        // these bounds.
                        double newX = childBounds.getX();
                        double newY = childBounds.getY();
                        // If the figure is centered, have to use the center
                        // instead.
                        try {
                            Attribute centered =
                                container.getAttribute(
                                    "centered",
                                    Parameter.class);
                            if (centered != null) {
                                boolean isCentered =
                                    ((BooleanToken) ((Parameter) centered)
                                        .getToken())
                                        .booleanValue();
                                if (isCentered) {
                                    newX = childBounds.getCenterX();
                                    newY = childBounds.getCenterY();
                                }
                            }
                        } catch (IllegalActionException ex) {
                            // Something went wrong. Use default.
                        }
                        command.append("<property name = \"");
                        command.append(locationParameter.getName(context));
                        command.append("\" value=\"");
                        // FIXME: Make locations only integral?
                        command.append(Math.rint(newX));
                        command.append(", ");
                        command.append(Math.rint(newY));
                        command.append("\"/>");
                    }
                    command.append("</group>");

                    MoMLChangeRequest request =
                        new MoMLChangeRequest(
                            this,
                            context,
                            command.toString());
                    context.requestChange(request);
                }
            } else {
                throw new InternalErrorException("No child figure for the manipulator!");
            }
        }

        /** Create a new instance of this manipulator. The new
         *  instance will have the same grab handle, and interactor
         *  for grab-handles.  This is typically called on the prototype
         *  to yield a decorator that gets displayed while the object
         *  is selected.
         */
        public FigureDecorator newInstance(Figure f) {
            BoundsManipulator m = new AttributeBoundsManipulator();
            m.setGrabHandleFactory(this.getGrabHandleFactory());
            m.setHandleInteractor(this.getHandleInteractor());
            m.setDragInteractor(getDragInteractor());

            // FIXME: There is a bug in Diva where mouseReleased()
            // is called on the prototype that is used to create this
            // new instance, not on the new instance.  So we make
            // a record of the new instance to get access to it in
            // mouseReleased().
            _instanceDecorator = m;

            return m;
        }

        // FIXME: Instance used to work around Diva bug.
        private FigureDecorator _instanceDecorator;
    }
}
