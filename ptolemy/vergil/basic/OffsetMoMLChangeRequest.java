/* A MoMLChangeRequest that offsets any objects that are created.

   Copyright (c) 2007-2014 The Regents of the University of California.
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
   2
 */
package ptolemy.vergil.basic;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.SwingUtilities;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import diva.canvas.Figure;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// OffsetMoMLChangeRequest
/**
 A mutation request specified in MoML that offsets any objects
 that are created in the toplevel.

 <p>This class is used by the paste action in Vergil so that the
 pasted icon does not overlap the original icon.
 If a BasicGraphFrame can be found, then the position of the mouse
 is used to determine the offsite.  Otherwise, a small offset is
 used.</p>

 <p>The pasted objects are selected so that the can be moved as a
 group.</p>

 @author  Christopher Brooks, based on code from BasicGraphFrame by Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class OffsetMoMLChangeRequest extends MoMLChangeRequest {

    /** Construct a mutation request to be executed in the specified
     *  context.  The context is typically a Ptolemy II container,
     *  such as an entity, within which the objects specified by the
     *  MoML code will be placed.  This method resets and uses a
     *  parser that is a static member of this class.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  Alternatively, it can call waitForCompletion().
     *  All external references are assumed to be absolute URLs.  Whenever
     *  possible, use a different constructor that specifies the base.
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     *  @param request The mutation request in MoML.
     */
    public OffsetMoMLChangeRequest(Object originator, NamedObj context,
            String request) {
        super(originator, context, request);
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected method                  ////

    /** Offset the locations of top level objects that are created
     *  by the change request.
     *  If a BasicGraphFrame can be found, then the position of the mouse
     *  is used to determine the offsite.  Otherwise, a small offset is
     *  used.
     *  @param parser The parser
     */
    @Override
    protected void _postParse(MoMLParser parser) {
        // Find the upper-most, left-most location.  Note that
        // this is the center of the component.
        double[] minimumLocation = new double[] { Double.MAX_VALUE,
                Double.MAX_VALUE };
        Iterator topObjects = parser.topObjectsCreated().iterator();
        while (topObjects.hasNext()) {
            NamedObj topObject = (NamedObj) topObjects.next();
            Iterator locations = topObject.attributeList(Locatable.class)
                    .iterator();
            while (locations.hasNext()) {
                Locatable location = (Locatable) locations.next();
                double[] locationValue = location.getLocation();
                for (int i = 0; i < locationValue.length
                        && i < minimumLocation.length; i++) {
                    if (locationValue[i] < minimumLocation[i]) {
                        minimumLocation[i] = locationValue[i];
                    }
                }
            }
        }

        double xOffset = _PASTE_OFFSET;
        double yOffset = _PASTE_OFFSET;
        double scale = 1.0;
        GraphController controller = null;
        // If there is a basic graph frame, then get the mouse location.
        BasicGraphFrame basicGraphFrame = BasicGraphFrame
                .getBasicGraphFrame(_context);
        if (basicGraphFrame != null) {
            controller = basicGraphFrame.getJGraph().getGraphPane()
                    .getGraphController();
            Point componentLocation = basicGraphFrame.getJGraph()
                    .getGraphPane().getCanvas().getLocationOnScreen();

            AffineTransform current = basicGraphFrame.getJGraph()
                    .getCanvasPane().getTransformContext().getTransform();

            // We assume the scaling in the X and Y directions are the same.
            scale = current.getScaleX();

            Rectangle2D visibleCanvas = basicGraphFrame
                    .getVisibleCanvasRectangle();

            // Get the mouse location.  We don't use a MouseMotionListener here because we
            // need the mouse location only when we paste.
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

            // Take in to account the panner and read values from visibleCanvas.
            //xOffset = mouseLocation.x - componentLocation.x - minimumLocation[0];
            //yOffset = mouseLocation.y - componentLocation.y - minimumLocation[1];

            // We adjust by the scale here to get from screen coordinates to model coordinates?
            xOffset = (mouseLocation.x - componentLocation.x) / scale
                    + visibleCanvas.getX() - minimumLocation[0];
            yOffset = (mouseLocation.y - componentLocation.y) / scale
                    + visibleCanvas.getY() - minimumLocation[1];

            //System.out.println("OffsetMoMLChangeRequest: mouse.x: " + mouseLocation.x + " comp.x: " + componentLocation.x + " visCanv.x: " + visibleCanvas.getX() + " min.x: " + minimumLocation[0] + " scale: " + scale + " xOff: " + xOffset + " " + visibleCanvas);
            //System.out.println("OffsetMoMLChangeRequest: mouse.y: " + mouseLocation.y + " comp.y: " + componentLocation.y + " visCanv.y: " + visibleCanvas.getY() + " min.y: " + minimumLocation[1] + " scale: " + scale + " yOff: " + yOffset);
        }

        NamedObj container = null;
        final Set _topObjects = new HashSet<NamedObj>();

        // Update the locations.
        topObjects = parser.topObjectsCreated().iterator();
        while (topObjects.hasNext()) {
            NamedObj topObject = (NamedObj) topObjects.next();
            _topObjects.add(topObject);
            if (container == null) {
                container = topObject.getContainer();
            }
            try {
                // Update the location of each top object.
                Iterator locations = topObject.attributeList(Locatable.class)
                        .iterator();
                while (locations.hasNext()) {
                    Locatable location = (Locatable) locations.next();
                    double[] locationValue = location.getLocation();
                    for (int i = 0; i < locationValue.length; i++) {
                        if (i == 0) {
                            locationValue[i] += xOffset;
                        } else if (i == 1) {
                            locationValue[i] += yOffset;
                        } else {
                            locationValue[i] += _PASTE_OFFSET;
                        }
                        location.setLocation(locationValue);
                    }
                }
            } catch (IllegalActionException e) {
                MessageHandler.error("Change failed", e);
            }
        }

        if (controller != null) {
            // Select the pasted objects so that they can be dragged.
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3003

            final GraphController controllerFinal = controller;
            final NamedObj containerFinal = container;
            Runnable doHelloWorld = new Runnable() {
                @Override
                public void run() {
                    Interactor interactor = null;
                    try {
                        interactor = controllerFinal.getEdgeController(
                                new Object()).getEdgeInteractor();
                    } catch (Exception ex) {
                        interactor = controllerFinal.getNodeController(null)
                                .getNodeInteractor();
                    }
                    SelectionInteractor selectionInteractor = (SelectionInteractor) interactor;
                    selectionInteractor.getSelectionRenderer();
                    SelectionModel selectionModel = controllerFinal
                            .getSelectionModel();
                    selectionModel.clearSelection();
                    AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) controllerFinal
                            .getGraphModel();

                    if (graphModel != null) {
                        Iterator nodes = graphModel.nodes(containerFinal);
                        while (nodes.hasNext()) {
                            Locatable node = (Locatable) nodes.next();
                            NamedObj entity = (NamedObj) graphModel
                                    .getSemanticObject(node);
                            if (_topObjects.contains(entity)) {
                                // If we don't do this in an invokeLater, then the
                                // canvas will not be updated so the controller will
                                // not have the figures and this will be null.
                                Figure figure = controllerFinal.getFigure(node);
                                selectionModel.addSelection(figure);
                            }
                        }
                    }
                }
            };

            SwingUtilities.invokeLater(doHelloWorld);
        }

        parser.clearTopObjectsList();
    }

    /** Clear the list of top objects.
     *  @param parser The parser
     */
    @Override
    protected void _preParse(MoMLParser parser) {
        super._preParse(parser);
        parser.clearTopObjectsList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The context in which to execute the moml. */
    private NamedObj _context;

    /** Offset used when pasting objects. */
    private static int _PASTE_OFFSET = 10;

}
