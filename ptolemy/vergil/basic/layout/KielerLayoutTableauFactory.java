/*  A top-level dialog window for controlling the Kieler graph layout algorithm.

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
 */
package ptolemy.vergil.basic.layout;

import java.util.Iterator;

import javax.swing.JFrame;

import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.layout.KielerLayoutTableau.KielerLayoutFrame;
import ptolemy.vergil.basic.layout.kieler.KielerLayout;
import ptolemy.vergil.basic.layout.kieler.PtolemyModelUtil;

/**
   A factory that creates a control panel to display Kieler layout controls.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class KielerLayoutTableauFactory extends TableauFactory {
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public KielerLayoutTableauFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of KielerLayoutTableau in the specified
     *  effigy. If the specified effigy is not an
     *  instance of PtolemyEffigy, then do not create a tableau
     *  and return null. It is the responsibility of callers of
     *  this method to check the return value and call show().
     *
     *  @param effigy The model effigy.
     *  @return A new control panel tableau if the effigy is
     *    a PtolemyEffigy, or null otherwise.
     *  @exception Exception If the factory should be able to create a
     *   tableau for the effigy, but something goes wrong.
     */
    public Tableau createTableau(Effigy effigy) throws Exception {
        if (effigy instanceof PtolemyEffigy) {
            // possibly pass on no dialog option
            boolean noDialog = false;
            if (this.getName().equals("NODIALOG")) {
                noDialog = true;
            }
            
            KielerLayoutTableau returnTableau = null;
            if (noDialog) {
                
                NamedObj model = null;
                try {
                    // Get the frame and the current model here.
                    model = ((PtolemyEffigy) effigy).getModel();
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
                                        .findEffigy(model).entityList(Tableau.class)
                                        .get(0)).getFrame();
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
                        MessageHandler.error(
                                "Failed to layout \""
                                        + (model == null ? "name not found" : (model
                                                .getFullName())) + "\"", ex);
                    }
                } catch (Exception ex) {
                    // If we do not catch exceptions here, then they
                    // disappear to stdout, which is bad if we launched
                    // where there is no stdout visible.
                    MessageHandler.error(
                            "Failed to layout \""
                                    + (model == null ? "name not found" : (model
                                            .getFullName())) + "\"", ex);
                }

                
            }
            else {
                // First see whether the effigy already contains a tableau.
                returnTableau = (KielerLayoutTableau) effigy
                        .getEntity("KielerLayoutTableau");
                if (returnTableau == null) {
                    returnTableau = new KielerLayoutTableau((PtolemyEffigy) effigy,
                            "KielerLayoutTableau");
                }
                
            }

            return returnTableau;
        } else {
            return null;
        }
    }
}
