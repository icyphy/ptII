/*  A top-level dialog window for controlling the Kieler graph layout algorithm.

 Copyright (c) 2010-2011 The Regents of the University of California.
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.layout.kieler.KielerLayout;
import de.cau.cs.kieler.core.properties.IPropertyHolder;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;

/**
 A top-level dialog window for controlling the Kieler graph layout algorithm.

 @author Christopher Brooks, based on JVMTableau. Christian Motika (<a href="mailto:cmot@informatik.uni-kiel.de">cmot</a>)
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class KielerLayoutTableau extends Tableau {
    
    /** Construct a frame to control layout of graphical elements
     *  using the KIELER algorithms for the specified Ptolemy II model.
     *
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public KielerLayoutTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        NamedObj model = container.getModel();

        _frame = new KielerLayoutFrame((CompositeEntity) model, this);
        setFrame(_frame);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    private LayoutConfiguration createOptions(Container container) {
        // TODO read previously stored configuration
        final IPropertyHolder options = new MapPropertyHolder();
        
        container.add(new JLabel("Include decorations"));
        final JCheckBox decorCheckBox = new JCheckBox();
        decorCheckBox.setSelected(options.getProperty(KielerLayout.Options.DECORATIONS));
        decorCheckBox.setToolTipText("Whether to include unconnected nodes such as comments.");
        container.add(decorCheckBox);
        
        container.add(new JLabel("Route edges"));
        final JCheckBox edgesCheckBox = new JCheckBox();
        edgesCheckBox.setSelected(options.getProperty(KielerLayout.Options.ROUTE_EDGES));
        edgesCheckBox.setToolTipText("Whether to apply edge routing or to use the standard router.");
        container.add(edgesCheckBox);
        
        container.add(new JLabel("Object spacing"));
        final JSlider spacingSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 50,
                options.getProperty(KielerLayout.Options.SPACING).intValue());
        spacingSlider.setToolTipText("The overall spacing between graph elements.");
        container.add(spacingSlider);
        
        // return an object that can create an options map on demand
        return new LayoutConfiguration() {
            public IPropertyHolder getOptions() {
                options.setProperty(KielerLayout.Options.DECORATIONS,
                        decorCheckBox.isSelected());
                options.setProperty(KielerLayout.Options.ROUTE_EDGES,
                        edgesCheckBox.isSelected());
                options.setProperty(LayoutOptions.SPACING,
                        Float.valueOf(spacingSlider.getValue()));
                return options;
            }
        };
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The KIELER Layout Frame, needed so that we can call getModel(). */
    private KielerLayoutFrame _frame;


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of KielerLayoutTableau.
     */
    public class KielerLayoutFrame extends PtolemyFrame {
        
        /** Construct a frame to display KIELER layout controls.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public KielerLayoutFrame(final CompositeEntity model, Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
            super(model, tableau);

            setTitle("Layout Options for " + model.getName());
            JPanel upperPanel = new JPanel();
            upperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));

            // Caveats panel.
            JPanel caveatsPanel = new JPanel();
            caveatsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
            caveatsPanel.setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

            JLabel descriptionLabel = new JLabel("Parameters for the KIELER layout algorithm");
            descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.BOLD));
            caveatsPanel.add(descriptionLabel);

            JButton moreInfoButton = new JButton("More Info");
            moreInfoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String infoResource = "ptolemy/vergil/basic/layout/package.html";
                    try {
                        Configuration configuration = getConfiguration();

                        // Use Thread.currentThread() so that this code will
                        // work under WebStart.
                        URL infoURL = Thread.currentThread()
                                .getContextClassLoader()
                                .getResource(infoResource);
                        configuration.openModel(null, infoURL,
                                infoURL.toExternalForm());
                    } catch (Exception ex) {
                        throw new InternalErrorException(model, ex,
                                "Failed to open " + infoResource + ": ");
                    }
                }
            });
            caveatsPanel.add(moreInfoButton);
            upperPanel.add(caveatsPanel);

            // Panel for layout options.
            JPanel optionsPanel = new JPanel();
            optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            optionsPanel.setLayout(new GridLayout(0, 2, 4, 4));
            LayoutConfiguration layoutConfig = createOptions(optionsPanel);
            upperPanel.add(optionsPanel);
            
            // Buttons for applying layout.
            JPanel layoutPanel = new JPanel();
            layoutPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton applyButton = new JButton("Apply Layout");
            applyButton.setToolTipText("Store the current setting in the model and apply automatic layout.");
            applyButton.addActionListener(new LayoutAction(layoutConfig));
            layoutPanel.add(applyButton);
            JButton oldButton = new JButton("Old Ptolemy Layout");
            oldButton.setToolTipText("Apply the old Ptolemy layout algorithm.");
            oldButton.addActionListener(new KielerTableauPtolemyLayoutAction());
            layoutPanel.add(oldButton);
            upperPanel.add(layoutPanel);
            
            getContentPane().add(upperPanel, BorderLayout.CENTER);
            pack();
        }
    }

    /**
     * Action for storing options in the model and applying automatic layout.
     * TODO store options persistently in the model
     */
    private class LayoutAction extends AbstractAction {
        
        /** Constructs an action for applying layout.
         */
        LayoutAction(LayoutConfiguration config) {
            this._layoutConfiguration = config;
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj model = null;
            try {
                // Get the frame and the current model here.
                model = _frame.getModel();
                actionPerformed(e, model);
            } catch (Exception ex) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error("Failed to layout \""
                        + (model == null ? "name not found"
                                : (model.getFullName())) + "\"", ex);
            }
        }

        void actionPerformed(ActionEvent e, NamedObj model) {
            if (!(model instanceof CompositeActor)) {
                // TODO extend this for modal models
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
                    JFrame firstFrame = (Configuration.findEffigy(model)
                            .entityList(Tableau.class).get(0)).getFrame();
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
                throw new InternalErrorException(model, null,
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

                // create KIELER layouter for this layout target
                KielerLayout layout = new KielerLayout(layoutTarget);
                layout.setModel((CompositeActor) model);
                layout.setTop(graphFrame);
                IPropertyHolder options = _layoutConfiguration.getOptions();
                layout.setOptions(options);

                layout.layout(graphModel.getRoot());
            }
        }

        private LayoutConfiguration _layoutConfiguration;
    }

    /** Use the older layout algorithm. */
    private class KielerTableauPtolemyLayoutAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            // Get the frame and the current model here.
            NamedObj model = _frame.getModel();
            List tableaux = Configuration.findEffigy(model).entityList(
                    Tableau.class);
            JFrame frame = ((Tableau) tableaux.get(0)).getFrame();
            BasicGraphFrame graphFrame = (BasicGraphFrame) frame;
            graphFrame.layoutGraphWithPtolemyLayout();
        }
    }
    
    /** Internally used configuration class for transferring the layout options. */
    private interface LayoutConfiguration {
        /**
         * Create a map of layout options from the user settings.
         * @return a property holder for mapping layout option identifiers to specific values
         */
        IPropertyHolder getOptions();
    }

}
