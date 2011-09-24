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

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.layout.kieler.KielerLayout;
import de.cau.cs.kieler.core.properties.IPropertyHolder;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;

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
    
    /**
     * Create controls for manipulating layout options and return a layout configuration that
     * derives its values from the UI controls.
     * 
     * @param container a container where UI controls are added
     * @return a configuration that derives its values from the UI controls
     */
    private LayoutConfiguration _createOptions(Container container) {
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
        float spacing = options.getProperty(KielerLayout.Options.SPACING);
        final JSlider spacingSlider = new JSlider(SwingConstants.HORIZONTAL, 2, 50,
                _saturate((int) spacing, 2, 50));
        spacingSlider.setToolTipText("The overall spacing between graph elements.");
        container.add(spacingSlider);
        
        container.add(new JLabel("Aspect ratio"));
        double aspectValue = (Math.log10(options.getProperty(KielerLayout.Options.ASPECT_RATIO)
                .doubleValue()) + 1) * 50;
        final JSlider aspectSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,
                _saturate((int) aspectValue, 0, 100));
        aspectSlider.setToolTipText("The aspect ratio for placement of connected components.");
        container.add(aspectSlider);
        
        // return an object that can create an options map on demand
        return new LayoutConfiguration() {
            public IPropertyHolder getOptions() {
                options.setProperty(KielerLayout.Options.DECORATIONS,
                        decorCheckBox.isSelected());
                options.setProperty(KielerLayout.Options.ROUTE_EDGES,
                        edgesCheckBox.isSelected());
                options.setProperty(KielerLayout.Options.SPACING,
                        Float.valueOf(spacingSlider.getValue()));
                options.setProperty(KielerLayout.Options.ASPECT_RATIO,
                        Float.valueOf((float) Math.pow(10, aspectSlider.getValue() / 50.0 - 1)));
                return options;
            }
        };
    }
    
    /**
     * Saturate the given number into a given range.
     * 
     * @param x an integer number
     * @param lower the lower bound
     * @param upper the upper bound
     * @return {@code max(lower, min(upper, x)) }
     */
    private int _saturate(int x, int lower, int upper) {
        if (x < lower) {
            return lower;
        }
        if (x > upper) {
            return upper;
        }
        return x;
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
            LayoutConfiguration layoutConfig = _createOptions(optionsPanel);
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
        
        /** Construct an action for applying layout.
         */
        LayoutAction(LayoutConfiguration config) {
            this._layoutConfiguration = config;
        }

        /** Perform the action.
         */
        public void actionPerformed(ActionEvent e) {
            NamedObj model = null;
            try {
                // Get the current model and the layout configuration.
                model = _frame.getModel();
                KielerLayoutAction action = new KielerLayoutAction(_layoutConfiguration.getOptions());
                action.doAction(model);
            } catch (Exception ex) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error("Failed to layout \""
                        + (model == null ? "name not found"
                                : (model.getFullName())) + "\"", ex);
            }
        }

        /** The layout configuration used to derive layout options. */
        private LayoutConfiguration _layoutConfiguration;
    }

    /** Use the older layout algorithm. */
    private class KielerTableauPtolemyLayoutAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            // Get the frame and the current model here.
            NamedObj model = _frame.getModel();
            Effigy effigy = Configuration.findEffigy(model);
            if (effigy == null) {
                effigy = Configuration.findEffigy(model.getContainer());
            }
            List tableaux = effigy.entityList(Tableau.class);
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
