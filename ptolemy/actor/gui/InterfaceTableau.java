/* A tableau that creates a new run control panel for a ptolemy model.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mlc.swing.layout.LayoutConstraintsManager;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.gui.CloseListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.jgoodies.forms.factories.DefaultComponentFactory;

//////////////////////////////////////////////////////////////////////////
//// RunTableau

/**
 A tableau that creates a new run control panel for a ptolemy model.
 This panel has controls for parameters of the top-level entity
 and its director, if any, a set of buttons to control execution
 of the model, and a panel displaying the placeable entities within
 the model.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class InterfaceTableau extends Tableau {
    /** Create a new run control panel for the model with the given
     *  effigy.  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public InterfaceTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (!(model instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Cannot run a model that is not a CompositeActor."
                            + " It is: " + model);
        }

        _model = (CompositeActor) model;
        _manager = _model.getManager();
        // Create a manager if necessary.
        if (_manager == null) {
            try {
                _manager = new Manager(_model.workspace(), "manager");
                _model.setManager(_manager);
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(
                        this,
                        ex,
                        "Failed to set manager.  This can occur if "
                                + "you try to run a non-toplevel model that "
                                + "is a component of a toplevel model.  "
                                + "The solution is invoke View -> Run while in a "
                                + "toplevel window.");
            }
        }

        JFrame frame = new TableauFrame(this);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new Panel(), BorderLayout.CENTER);
        setFrame(frame);
    }
    
    /** If the model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     */
    public void startRun() {
        if (_manager != null) {
            try {
                _manager.startRun();
            } catch (IllegalActionException ex) {
                // Model is already running.  Ignore.
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    ////                             private variables                     ////
    
    /** The manager. */
    private Manager _manager;

    ///////////////////////////////////////////////////////////////////////////
    ////                             inner classes                         ////
    
    /** A factory that creates run control panel tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "runTableau", then return that tableau; otherwise, create
         *  a new instance of RunTableau for the effigy, and
         *  name it "runTableau".  If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new run tableau if the effigy is a PtolemyEffigy,
         *    or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a RunTableau.
                InterfaceTableau tableau = (InterfaceTableau) effigy
                        .getEntity("interfaceTableau");

                if (tableau == null) {
                    tableau = new InterfaceTableau((PtolemyEffigy) effigy,
                            "interfaceTableau");
                }

                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }
    
    /** The panel.
     */
    private class Panel extends JPanel implements CloseListener {
        public Panel() {
            super();
            // FIXME: This XML specification of the layout should
            // be stored in an attribute in the model.
            // Here, we create a default.
            StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xml.append("<containers>");
            xml.append("<container name=\"panel\" ");
            xml.append("columnSpecs=\"right:max(30dlu;pref),3dlu,80dlu,10dlu,right:max(30dlu;pref),3dlu,80dlu,1dlu:grow\" ");
            xml.append("rowSpecs=\"pref,5dlu,pref,5dlu,pref,5dlu,pref,5dlu,pref,3dlu,pref,7dlu,pref,5dlu,100dlu:grow,7dlu,pref\">");
            xml.append("<cellconstraints name=\"runControlSeparator\" gridX=\"1\" gridY=\"1\" gridWidth=\"4\" gridHeight=\"1\" horizontalAlignment=\"default\" verticalAlignment=\"default\" topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>");
            xml.append("<cellconstraints name=\"goButton\" gridX=\"1\" gridY=\"3\" gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" verticalAlignment=\"default\" topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>");
       
            // Add constraints for each object that implements Placeable.
            if (_model != null) {
                Iterator atomicEntities = _model.allAtomicEntityList().iterator();
                int row = 1;
                while (atomicEntities.hasNext()) {
                    Object object = atomicEntities.next();
                    if (object instanceof Placeable) {
                        xml.append("<cellconstraints name=\"");
                        xml.append(((NamedObj)object).getFullName());
                        xml.append("\" gridX=\"5\" gridY=\"");
                        xml.append(row);
                        xml.append("\" gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" verticalAlignment=\"default\" topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>");
                        // FIXME: Need to calculate the number of rows to specify above!
                        row = row + 2;
                    }
                }
            }
            
            xml.append("</container>");
            xml.append("</containers>");
            // FIXME: The following class is deprecated, but there seems
            // to be no other way to turn a string into an InputStream!!!!!
            InputStream stream = new StringBufferInputStream(xml.toString());

            LayoutConstraintsManager layoutConstraintsManager =
                    LayoutConstraintsManager.getLayoutConstraintsManager(stream);
            setBorder(com.jgoodies.forms.factories.Borders.DIALOG_BORDER);
            LayoutManager layout = layoutConstraintsManager.createLayout("panel", this);
            this.setLayout(layout);

            // Create the run control section.
            Component runControlSeparator = DefaultComponentFactory.getInstance().createSeparator("Run Control");
            this.add(runControlSeparator, "runControlSeparator");
            
            _goButton = new JButton("Go");
            _goButton.setToolTipText("Execute the model");
            _goButton.setAlignmentX(LEFT_ALIGNMENT);
            this.add(_goButton, "goButton");
            _goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    startRun();
                }
            });
            
            // Place all the components that implement Placeable.
            if (_model != null) {
                Iterator atomicEntities = _model.allAtomicEntityList().iterator();
                while (atomicEntities.hasNext()) {
                    Object object = atomicEntities.next();
                    if (object instanceof Placeable) {
                        // Regrettably, it seems we need an intermediate JPanel.
                        JPanel dummy = new JPanel();
                        ((Placeable) object).place(dummy);
                        this.add(dummy, ((NamedObj)object).getFullName());
                    }
                }
            }

            // FIXME: Layout frame to edit the layout.
            // This should be used by an attribute that can be placed in the model
            // to customize the layout of the run control window.
            // LayoutFrame frame = new LayoutFrame(layoutConstraintsManager);
            // frame.setVisible(true);
        }
        
        /** Notify the contained instances of PtolemyQuery that the window
         *  has been closed, and remove all Placeable displays by calling
         *  place(null).  This method is called if this pane is contained
         *  within a container that supports such notification.
         *  @param window The window that closed.
         *  @param button The name of the button that was used to close the window.
         */
        public void windowClosed(Window window, String button) {
            // FIXME: This is not getting invoked. Need to override
            // TableauFrame above with an override to _close().
            // Better yet, should use ModelFrame instead of TableauFrame,
            // but then ModelPane needs to be converted to an interface
            // so that this pane can be used instead.
            if (_directorQuery != null) {
                _directorQuery.windowClosed(window, button);
            }

            if (_parameterQuery != null) {
                _parameterQuery.windowClosed(window, button);
            }

            if (_model != null) {
                _closeDisplays();
            }
        }
        
        /** Close any open displays by calling place(null).
         */
        private void _closeDisplays() {
            if (_model != null) {
                Iterator atomicEntities = _model.allAtomicEntityList().iterator();

                while (atomicEntities.hasNext()) {
                    Object object = atomicEntities.next();

                    if (object instanceof Placeable) {
                        ((Placeable) object).place(null);
                    }
                }
            }
        }
    }

    // The query box for the director parameters.
    private Configurer _directorQuery;

    // The go button.
    private JButton _goButton;
    
    // The associated model.
    private CompositeActor _model;
    
    // The query box for the top-level parameters.
    private Configurer _parameterQuery;
}
