/* The default KIELER layout with place and route.

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.vergil.basic.layout;

import java.util.Iterator;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.IGuiAction;
import ptolemy.vergil.basic.layout.KielerLayoutTableau.KielerLayoutFrame;
import ptolemy.vergil.basic.layout.kieler.KielerLayout;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;

///////////////////////////////////////////////////////////////////
//// KielerLayoutAction

/**
Trigger the KIELER place and route automatic dataflow layout algorithm
from withing the Vergil GUI. Operate on the current model, hence the
model needs to be an input in the doAction() method.

@author  Christian Motika
@version $Id: IGuiAction.java 59288 2010-09-27 19:39:22Z cmot $
@since Ptolemy II 2.1
@Pt.ProposedRating Red (cmot)
@Pt.AcceptedRating Red (cmot)
*/
public class KielerLayoutAction extends Object implements IGuiAction {

    /**
     * Layout the graph if the model is a CompositeActor. Otherwise throw an 
     * exception. The frame type must be ActorGraphFrame. The KIELER layouter
     * is called with placing and routing. The routing uses bend point 
     * annotations.
     * 
     * @param model the model
     */
    public void doAction(NamedObj model) {
                try {
                    if (!(model instanceof CompositeActor)) {
                        throw new InternalErrorException(
                                "For now only actor oriented graphs with ports are supported by KIELER layout. "
                                        + "The model \""
                                        + model.getFullName()
                                        + "\" was a "
                                        + model.getClass().getName()
                                        + " which is not an instance of CompositeActor.");
                    }
                    JFrame frame = null;
                    int tableauxCount = 0;
                    Iterator tableaux = Configuration.findEffigy(model)
                            .entityList(Tableau.class).iterator();
                    while (tableaux.hasNext()) {
                        Tableau tableau = (Tableau) (tableaux.next());
                        tableauxCount++;
                        if (tableau.getFrame() instanceof ActorGraphFrame) {
                            frame = tableau.getFrame();
                        }
                    }
                    // Check for supported type of editor 
                    if (!(frame instanceof ActorGraphFrame)) {
                        String message = "";
                        if (tableauxCount == 0) {
                            message = "findEffigy() found no Tableaux?  There should have been one "
                                    + "ActorGraphFrame.";
                        } else {
                            JFrame firstFrame = ((Tableau) Configuration
                                    .findEffigy(model)
                                    .entityList(Tableau.class).get(0))
                                    .getFrame();
                            if (firstFrame instanceof KielerLayoutFrame) {
                                message = "Internal Error: findEffigy() returned a KielerLayoutGUI, "
                                        + "please save the model before running the layout mechanism.";
                            } else {
                                message = "The first frame of "
                                        + tableauxCount
                                        + " found by findEffigy() is a \""
                                        + firstFrame.getClass().getName()
                                        + "\", which is not an instance of ActorGraphFrame."
                                        + " None of the other frames were ActorGraphFrames either.";
                            }
                        }
                        throw new InternalErrorException(
                                model,
                                null,
                                "For now only actor oriented graphs with ports are supported by KIELER layout. "
                                        + message
                                        + (frame != null ? " Details about the frame: "
                                                + StringUtilities.ellipsis(
                                                        frame.toString(), 80)
                                                : ""));
                    } else {
                        BasicGraphFrame graphFrame = (BasicGraphFrame) frame;

                        // fetch everything needed to build the LayoutTarget
                        GraphController graphController = graphFrame
                                .getJGraph().getGraphPane()
                                .getGraphController();
                        GraphModel graphModel = graphFrame.getJGraph()
                                .getGraphPane().getGraphController()
                                .getGraphModel();
                        BasicLayoutTarget layoutTarget = new BasicLayoutTarget(
                                graphController);

                        // create Kieler layouter for this layout target
                        KielerLayout layout = new KielerLayout(layoutTarget);
                        layout.setModel((CompositeActor) model);
                        layout.setApplyEdgeLayout(false);
                        layout.setApplyEdgeLayoutBendPointAnnotation(true);
                        layout.setBoxLayout(false);
                        layout.setTop(graphFrame);

                        layout.layout(graphModel.getRoot());
                    }
                } catch (Exception ex) {
                    // If we do not catch exceptions here, then they
                    // disappear to stdout, which is bad if we launched
                    // where there is no stdout visible.
                    MessageHandler
                            .error("Failed to layout \""
                                    + (model == null ? "name not found"
                                            : (model.getFullName())) + "\"", ex);
                }
    }

}
