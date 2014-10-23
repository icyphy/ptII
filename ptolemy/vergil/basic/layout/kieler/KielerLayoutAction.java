/* The default KIELER layout with place and route.

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
 2
 */
package ptolemy.vergil.basic.layout.kieler;

import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.IGuiAction;
import ptolemy.vergil.basic.PtolemyLayoutAction;
import ptolemy.vergil.basic.layout.LayoutConfiguration;
import ptolemy.vergil.modal.FSMGraphFrame;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.util.Filter;

///////////////////////////////////////////////////////////////////
//// KielerLayoutAction

/**
 * Trigger the KIELER place and route automatic dataflow layout algorithm
 * from within the Vergil GUI. Operate on the current model, hence the
 * model needs to be an input in the doAction() method.
 * <p>
 * This action implements the {@link Filter} interface to check whether
 * a given model is supported.
 * </p>
 *
 * @author  Christian Motika
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cmot)
 * @Pt.AcceptedRating Red (cmot)
 */
public class KielerLayoutAction extends Object implements IGuiAction, Filter {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Layout the graph if the model is a CompositeActor. Otherwise throw an
     * exception. The frame type must be ActorGraphFrame. The KIELER layouter
     * is called with placing and routing. The routing uses bend point
     * annotations.
     *
     * @param model the model
     */
    @Override
    public void doAction(NamedObj model) {
        try {
            if (!accept(model)) {
                throw new InternalErrorException(
                        "For now only actor models and modal models are supported by KIELER layout. "
                                + "The model \"" + model.getFullName()
                                + "\" is a " + model.getClass().getName()
                                + " which is not supported yet.");
            }
            JFrame frame = null;
            int tableauxCount = 0;
            Effigy effigy = Configuration.findEffigy(model);
            if (effigy == null) {
                effigy = Configuration.findEffigy(model.getContainer());
            }
            if (effigy != null) {
                Iterator tableaux = effigy.entityList(Tableau.class).iterator();
                while (tableaux.hasNext()) {
                    Tableau tableau = (Tableau) tableaux.next();
                    tableauxCount++;
                    if (tableau.getFrame() instanceof ActorGraphFrame
                            || tableau.getFrame() instanceof FSMGraphFrame) {
                        frame = tableau.getFrame();
                        break;
                    }
                }
            }
            // Check for supported type of editor.
            if (frame == null) {
                String message = "";
                if (tableauxCount == 0) {
                    message = "findEffigy() found no Tableaux.";
                } else if (effigy != null) {
                    JFrame firstFrame = effigy.entityList(Tableau.class).get(0)
                            .getFrame();
                    message = "The first frame of "
                            + tableauxCount
                            + " found by findEffigy() is "
                            + (firstFrame == null ? "null" : "a \""
                                    + firstFrame.getClass().getName() + "\"")
                                    + ", which is not an ActorGraphFrame or FSMGraphFrame.";
                }
                throw new InternalErrorException(model, null,
                        "For now only actor models and modal models are supported by KIELER layout. "
                                + message);
            } else {
                BasicGraphFrame graphFrame = (BasicGraphFrame) frame;

                // Check if the old layout algorithm should be used
                if (_useOldAlgorithm(model)) {
                    new PtolemyLayoutAction().doAction(model);
                } else {
                    // Fetch everything needed to build the LayoutTarget.
                    GraphController graphController = graphFrame.getJGraph()
                            .getGraphPane().getGraphController();
                    GraphModel graphModel = graphFrame.getJGraph()
                            .getGraphPane().getGraphController()
                            .getGraphModel();
                    BasicLayoutTarget layoutTarget = new BasicLayoutTarget(
                            graphController);

                    // Create KIELER layouter for this layout target.
                    KielerLayout layout = new KielerLayout(layoutTarget);
                    layout.setModel((CompositeEntity) model);
                    layout.setTop(graphFrame);

                    layout.layout(graphModel.getRoot());
                }
            }
        } catch (Throwable throwable) {
            // Catch a throwable in case the guava classes are not found.

            // If we do not catch throwables here, then they
            // disappear to stdout, which is bad if we launched
            // where there is no stdout visible.
            MessageHandler.error("Failed to layout \""
                    + (model == null ? "name not found" : model.getFullName())
                    + "\"", throwable);
        }
    }

    /**
     * Check whether the given model is supported by this layout action.
     *
     * @param o The object to be be checked.
     * @return true if the model can be laid out with this action.
     */
    @Override
    public boolean accept(Object o) {
        return o instanceof CompositeActor || o instanceof FSMActor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Checks if the given model is configured to use Ptolemy's old layout algorithm.
     *
     * @param model the model to check.
     * @return {@code true} if the model has a layout configuration that explicitly
     *         instructs us to use the old layout algorithm.
     */
    private boolean _useOldAlgorithm(NamedObj model) {
        try {
            // Find the model's LayoutConfiguration element
            List<LayoutConfiguration> configAttributes = model
                    .attributeList(LayoutConfiguration.class);

            // If there is such an element, check if the old algorithm is to be used
            if (!configAttributes.isEmpty()) {
                LayoutConfiguration configuration = configAttributes.get(0);

                BooleanToken useOldAlgorithm = BooleanToken
                        .convert(configuration.useOldAlgorithm.getToken());
                if (useOldAlgorithm.booleanValue()) {
                    return true;
                }
            }
        } catch (IllegalActionException e) {
            // Ignore exception -- we'll return false
        }

        // Default to false
        return false;
    }

}
