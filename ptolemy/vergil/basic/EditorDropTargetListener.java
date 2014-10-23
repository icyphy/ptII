/* A drop target listener that provides drag and drop for the Ptolemy editor.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.DropTargetHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.RelativeLocation;
import ptolemy.kernel.util.Singleton;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.toolbox.PtolemyTransferable;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.util.UserObjectContainer;

/**
 * A drop target listener that provides drag and drop for the Ptolemy editor.
 *
 * <p>Derived classes could extend this class to provide a different
 * drag and drop interface.</p>
 *
 * @author Sven Koehler, Contributor: Christopher Brooks.
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (johnr)
 */
public class EditorDropTargetListener implements DropTargetListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Accept the event if the data is a known key.
     *  This is called while a drag operation is ongoing,
     *  when the mouse pointer enters the operable part of
     *  the drop site for the DropTarget registered with
     *  this listener.
     *  @param dropEvent The drop event.
     */
    @Override
    public void dragEnter(DropTargetDragEvent dropEvent) {
        // Notify additionalListeners.
        for (int i = 0; i < _dropTarget.getAdditionalListeners().size(); i++) {
            DropTargetListener listener = _dropTarget.getAdditionalListeners()
                    .elementAt(i);
            listener.dragEnter(dropEvent);
        }

        if (dropEvent.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
            dropEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dropEvent.rejectDrag();
        }
    }

    /** Remove any highlighting that might be active.
     *  This is called while a drag operation is ongoing, when the mouse
     *  pointer has exited the operable part of the drop site for the
     *  DropTarget registered with this listener.
     *  @param dropEvent The drop event.
     */
    @Override
    public void dragExit(DropTargetEvent dropEvent) {
        // Notify additionalListeners.
        for (int i = 0; i < _dropTarget.getAdditionalListeners().size(); i++) {
            DropTargetListener listener = _dropTarget.getAdditionalListeners()
                    .elementAt(i);
            listener.dragExit(dropEvent);
        }
        if (_highlighted != null) {
            _highlighter.renderDeselected(_highlightedFigure);
            _highlighted = null;
            _highlightedFigure = null;
        }
    }

    /** If the location of the event is over an icon for
     *  an instance of NamedObj, then highlight that icon.
     *  This is called when a drag operation is ongoing,
     *  while the mouse pointer is still over the operable
     *  part of the drop site for the DropTarget registered
     *  with this listener.
     *  @param dropEvent The drop event.
     */
    @Override
    public void dragOver(DropTargetDragEvent dropEvent) {
        // Notify additionalListeners.
        for (int i = 0; i < _dropTarget.getAdditionalListeners().size(); i++) {
            DropTargetListener listener = _dropTarget.getAdditionalListeners()
                    .elementAt(i);
            listener.dragOver(dropEvent);
        }
        // See whether there is a container under the point.
        Point2D originalPoint = SnapConstraint.constrainPoint(dropEvent
                .getLocation());
        NamedObj over = _getObjectUnder(originalPoint);

        if (over != _highlighted) {
            if (_highlighted != null) {
                _highlighter.renderDeselected(_highlightedFigure);
                _highlighted = null;
                _highlightedFigure = null;
            }

            if (over != null && _dropTarget.isDropIntoEnabled()) {
                if (_highlighter == null) {
                    _highlighter = new AnimationRenderer(Color.white);
                }

                _highlighted = over;
                _highlightedFigure = _getFigureUnder(originalPoint);
                _highlighter.renderSelected(_highlightedFigure);
            }
        }

        // Used to do this... Any reason for it?
        // dragEnter(dropEvent);
    }

    /** If the transferable object is recognized as a Ptolemy II object,
     *  then use the MoML description of the object to create a new
     *  instance of the object at the drop location. If the drop
     *  location is on top of an icon representing an instance of
     *  NamedObj, then make that instance the container of the new
     *  object. Otherwise, make the model associated with the graph
     *  the container.
     *  This is called when the drag operation has terminated with a
     *  drop on the operable part of the drop site for the DropTarget
     *  registered with this listener.
     *  @param dropEvent The drop event.
     */
    @Override
    public void drop(DropTargetDropEvent dropEvent) {
        // Notify additionalListeners.
        for (int i = 0; i < _dropTarget.getAdditionalListeners().size(); i++) {
            DropTargetListener listener = _dropTarget.getAdditionalListeners()
                    .elementAt(i);
            listener.drop(dropEvent);
        }
        // Unhighlight the target. Do this first in case
        // errors occur... Don't want to leave highlighting.
        if (_highlighted != null) {
            _highlighter.renderDeselected(_highlightedFigure);
            _highlighted = null;
            _highlightedFigure = null;
        }

        // See whether there is a container under the point.
        Point2D originalPoint = SnapConstraint.constrainPoint(dropEvent
                .getLocation());
        NamedObj targetContainer = _getObjectUnder(originalPoint);

        // Find the root container (the composite entity for the window).
        GraphPane pane = ((JGraph) _dropTarget.getComponent()).getGraphPane();
        GraphController controller = pane.getGraphController();
        GraphModel model = controller.getGraphModel();
        NamedObj rootContainer = (NamedObj) model.getRoot();

        if (targetContainer == null || !_dropTarget.isDropIntoEnabled()) {
            // Find the default container for the dropped object
            targetContainer = rootContainer;
        }

        // Find the location for the dropped objects.
        // Account for the scaling in the pane.
        Point2D transformedPoint = new Point2D.Double();
        pane.getTransformContext().getInverseTransform()
        .transform(originalPoint, transformedPoint);

        // Get an iterator over objects to drop.
        Iterator iterator = null;

        java.util.List dropObjects = null;
        if (dropEvent.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
            try {
                dropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                dropObjects = (java.util.List) dropEvent.getTransferable()
                        .getTransferData(PtolemyTransferable.namedObjFlavor);
                iterator = dropObjects.iterator();
            } catch (Exception e) {
                MessageHandler.error(
                        "Can't find a supported data flavor for drop in "
                                + dropEvent, e);
                return;
            }
        } else {
            dropEvent.rejectDrop();
        }

        if (iterator == null) {
            // Nothing to drop!
            return;
        }

        while (iterator.hasNext()) {
            // Create the MoML change request to instantiate the new objects.
            StringBuffer moml = new StringBuffer();

            final NamedObj dropObj = (NamedObj) iterator.next();

            // Figure out the destination container.
            NamedObj destinationContainer = targetContainer;
            boolean relativeLocation = false;
            if (dropObj instanceof RelativeLocatable
                    && targetContainer != rootContainer) {
                relativeLocation = true;
                destinationContainer = rootContainer;
            }
            final String name;

            if (dropObj instanceof Singleton) {
                name = dropObj.getName();
            } else {
                name = destinationContainer.uniqueName(dropObj.getName());
            }

            // Constrain point to snap to grid.
            Point2D newPoint = SnapConstraint.constrainPoint(transformedPoint);
            /* At this point we wish to provide for an alternative way to get the MOML
             * code for the drop object. This is to allow for a drop object that may just
             * contain a reference (e.g. an LSID) to the complete MOML rather than including
             * all the information in the tree node object being dragged.
             * DFH July 2007
             */

            // first try to get the LSID; if one cannot be found use
            // the Ptolemy method for dropping
            boolean lsidFlag = true;
            try {
                String lsidString = ((StringAttribute) dropObj
                        .getAttribute("entityId")).getExpression();
                if (lsidString == null || lsidString.equals("")) {
                    lsidFlag = false;
                }
            } catch (Exception eee) {
                lsidFlag = false;
            }

            String result = "";
            String dropObjElementType = dropObj.getElementName();
            Object object = null;

            StringAttribute alternateGetMomlActionAttribute = null;
            alternateGetMomlActionAttribute = (StringAttribute) dropObj
                    .getAttribute("_alternateGetMomlAction");
            if (alternateGetMomlActionAttribute == null && lsidFlag) {
                Configuration config = null;
                java.util.List configsList = Configuration.configurations();
                for (Iterator it = configsList.iterator(); it.hasNext();) {
                    config = (Configuration) it.next();
                    if (config != null) {
                        break;
                    }
                }
                if (config == null) {
                    throw new KernelRuntimeException(dropObj, "Could not find "
                            + "configuration, list of configurations was "
                            + configsList.size() + " elements, all were null.");
                }
                alternateGetMomlActionAttribute = (StringAttribute) config
                        .getAttribute("_alternateGetMomlAction");
            }

            boolean appendGroupAuto = true;
            if (alternateGetMomlActionAttribute != null) {
                String alternateGetMomlClassName = alternateGetMomlActionAttribute
                        .getExpression();
                try {
                    Class getMomlClass = Class
                            .forName(alternateGetMomlClassName);
                    object = getMomlClass.newInstance();
                    try {
                        Method getMomlMethod = getMomlClass.getMethod(
                                "getMoml", new Class[] { NamedObj.class,
                                        String.class });
                        result = (String) getMomlMethod.invoke(object,
                                new Object[] { dropObj, name });
                        appendGroupAuto = false;
                    } catch (NoSuchMethodException e) {
                        Method getMomlMethod = getMomlClass.getMethod(
                                "getMoml", new Class[] { NamedObj.class });
                        result = (String) getMomlMethod.invoke(object,
                                new Object[] { dropObj });
                        int int1 = 1;
                        int int2 = result.indexOf(" ");
                        dropObjElementType = result.substring(int1, int2);
                        // following string substitution is needed to
                        // replace possible name changes when multiple
                        // copies of an actor are added to a workspace
                        // canvas (name then has integer appended to it)
                        // -- DFH
                        int1 = result.indexOf("\"", 1);
                        int2 = result.indexOf("\"", int1 + 1);

                        result = result.substring(0, int1 + 1) + name
                                + result.substring(int2, result.length());
                    }
                    moml.append(result);
                } catch (Throwable throwable) {
                    System.out.println("Error creating alternateGetMoml!");
                }
            } else { // default method for PtolemyII use
                result = dropObj.exportMoML(name);
                moml.append(result);
            }

            if (appendGroupAuto) {
                moml.insert(0, "<group name=\"auto\">\n");
                moml.append("<" + dropObjElementType + " name=\"" + name
                        + "\">\n");
                if (relativeLocation) {
                    moml.append("<property name=\"_location\" "
                            + "class=\"ptolemy.kernel.util.RelativeLocation\" value=\"{"
                            + RelativeLocation.INITIAL_OFFSET
                            + ", "
                            + RelativeLocation.INITIAL_OFFSET
                            + "}\">"
                            + "<property name=\"relativeTo\" value=\""
                            + targetContainer.getName()
                            + "\"/>\n"
                            // Need to identify the targetContainer as an
                            // Entity, Port, Relation, or Attribute.
                            + "<property name=\"relativeToElementName\" value=\""
                            + targetContainer.getElementName()
                            + "\"/></property>\n</" + dropObjElementType
                            + ">\n");

                } else {
                    moml.append("<property name=\"_location\" "
                            + "class=\"ptolemy.kernel.util.Location\" value=\"{");
                    moml.append((int) newPoint.getX());
                    moml.append(", ");
                    moml.append((int) newPoint.getY());
                    moml.append("}\"/>\n</" + dropObjElementType + ">\n");
                }
                moml.append("</group>\n");
            }
            if (destinationContainer instanceof DropTargetHandler) {
                try {
                    ((DropTargetHandler) destinationContainer).dropObject(
                            destinationContainer, dropObjects, moml.toString());
                } catch (IllegalActionException e) {
                    MessageHandler.error("Unable to drop the object to "
                            + destinationContainer.getName() + ".", e);
                }
            } else {
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        destinationContainer, moml.toString());
                request.setUndoable(true);
                destinationContainer.requestChange(request);
            }
        }

        dropEvent.dropComplete(true); //success!

        //Added by MB 6Apr06 - without this, tooltips don't work
        //after first actor is dragged to canvas from library, until
        //pane loses & regains focus
        JComponent comp = (JComponent) _dropTarget.getComponent();
        if (comp != null) {
            ToolTipManager.sharedInstance().registerComponent(comp);
        }
    }

    /** Accept the event if the data is a known key.
     *  This is called if the user has modified the current drop gesture.
     *  @param dropEvent The drop event.
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent dropEvent) {
        // Notify additionalListeners.
        for (int i = 0; i < _dropTarget.getAdditionalListeners().size(); i++) {
            DropTargetListener listener = _dropTarget.getAdditionalListeners()
                    .elementAt(i);
            listener.dropActionChanged(dropEvent);
        }
        // Used to do this... Not needed?
        // dragEnter(dropEvent);
    }

    /** Links this Listener back to the EditorDropTarget it belongs to.
     *  @param dropTarget The drop target.
     */
    public void setDropTarget(EditorDropTarget dropTarget) {
        _dropTarget = dropTarget;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the figure that is an icon of a NamedObj and is
     *  under the specified point, or null if there is none.
     *  @param point The point in the graph pane.
     *  @return The object under the specified point, or null if there
     *   is none or it is not a NamedObj.
     */
    protected Figure _getFigureUnder(Point2D point) {
        GraphPane pane = ((JGraph) _dropTarget.getComponent()).getGraphPane();

        // Account for the scaling in the pane.
        Point2D transformedPoint = new Point2D.Double();
        pane.getTransformContext().getInverseTransform()
        .transform(point, transformedPoint);

        return BasicGraphFrame.getFigureUnder(pane, transformedPoint,
                new Object[] {});
    }

    /** Return the object under the specified point, or null if there
     *  is none.
     *  @param point The point in the graph pane.
     *  @return The object under the specified point, or null if there
     *   is none or it is not a NamedObj.
     */
    protected NamedObj _getObjectUnder(Point2D point) {
        Figure figureUnderMouse = _getFigureUnder(point);

        if (figureUnderMouse == null) {
            return null;
        }

        Object objectUnderMouse = ((UserObjectContainer) figureUnderMouse)
                .getUserObject();

        // Object might be a Location, in which case we want its container.
        if (objectUnderMouse instanceof Location) {
            return ((NamedObj) objectUnderMouse).getContainer();
        } else if (objectUnderMouse instanceof NamedObj) {
            return (NamedObj) objectUnderMouse;
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Associated DropTarget. */
    protected EditorDropTarget _dropTarget = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Currently highlighted drop target.
    private NamedObj _highlighted = null;

    // Currently highlighted figure.
    private Figure _highlightedFigure = null;

    // The renderer used for highlighting.
    private AnimationRenderer _highlighter = null;

}
