/* A tableau that creates a new run control panel for a ptolemy model.

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
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;


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
*/
public class RunTableau extends Tableau {

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
    public RunTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        NamedObj model = container.getModel();
        if (!(model instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Cannot run a model that is not a CompositeActor."
                    + " It is: " + model);
        }
        CompositeActor actor = (CompositeActor)model;

        // Create a manager.
        Manager manager = actor.getManager();
        if (manager == null) {
            try {
                actor.setManager(new Manager(actor.workspace(), "manager"));
            } catch ( IllegalActionException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to set manager.  This can occur if "
                        + "you try to run a non-toplevel model that "
                        + "is a component of a toplevel model.  "
                        + "The solution is invoke View -> Run while in a "
                        + "toplevel window." );
            }
            manager = actor.getManager();
        }

        ModelFrame frame = new RunFrame(actor, this);
        setFrame(frame);
        frame.setBackground(BACKGROUND_COLOR);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: should be somewhere else?
    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of RunTableau.
     */
    public class RunFrame extends ModelFrame {

        /** Construct a frame to control the specified Ptolemy II model.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         */
        public RunFrame(CompositeActor model, Tableau tableau) {
            super(model, tableau);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected methods                 ////

        /** Add a Debug menu.
         */
        protected void _addMenus() {
            super._addMenus();
            JMenuItem[] debugMenuItems = {
                new JMenuItem("Listen to Manager", KeyEvent.VK_M),
                new JMenuItem("Listen to Director", KeyEvent.VK_D),
            };
            // NOTE: This has to be initialized here rather than
            // statically because this method is called by the constructor
            // of the base class, and static initializers have not yet
            // been run.
            _debugMenu = new JMenu("Debug");
            _debugMenu.setMnemonic(KeyEvent.VK_D);
            DebugMenuListener debugMenuListener = new DebugMenuListener();
            // Set the action command and listener for each menu item.
            for (int i = 0; i < debugMenuItems.length; i++) {
                debugMenuItems[i].setActionCommand(debugMenuItems[i].getText());
                debugMenuItems[i].addActionListener(debugMenuListener);
                _debugMenu.add(debugMenuItems[i]);
            }
            _menubar.add(_debugMenu);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected variables               ////

        /** Debug menu for this frame. */
        protected JMenu _debugMenu;

        ///////////////////////////////////////////////////////////////////
        ////                         inner classes                     ////

        /** Listener for debug menu commands. */
        public class DebugMenuListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                JMenuItem target = (JMenuItem)e.getSource();
                String actionCommand = target.getActionCommand();
                NamedObj model = getModel();
                if (model instanceof CompositeActor) {
                    try {
                        Debuggable debug;
                        if (actionCommand.equals("Listen to Manager")) {
                            debug = ((CompositeActor)model).getManager();
                        } else if (actionCommand.equals("Listen to Director")) {
                            debug = ((CompositeActor)model).getDirector();
                        } else {
                            debug = null;
                        }
                        if (debug != null) {
                            Effigy effigy = (Effigy)getContainer();
                            // Create a new text effigy inside this one.
                            Effigy textEffigy = new TextEffigy(effigy,
                                    effigy.uniqueName("debug listener"));
                            DebugListenerTableau tableau =
                                new DebugListenerTableau(textEffigy,
                                        textEffigy.uniqueName("debugListener"));
                            tableau.setDebuggable(debug);
                        }
                    } catch (KernelException ex) {
                        try {
                            MessageHandler.warning(
                                    "Failed to create debug listener: " + ex);
                        } catch (CancelException exception) {}
                    }
                }
            }
        }
    }

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
                RunTableau tableau =
                    (RunTableau)effigy.getEntity("runTableau");
                if (tableau == null) {
                    tableau = new RunTableau(
                            (PtolemyEffigy)effigy, "runTableau");
                }
                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }

    /** A factory that creates run control panel tableaux for the model
     *  associated with a top-level effigy (one that has a file
     *  representation).
     */
    public static class TopFactory extends Factory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public TopFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a tableau to run the model associated with the specified
         *  effigy.  The top-level effigy, as returned by
         *  {@link Effigy#topEffigy()}, is the one that is run.
         *  If that effigy already contains a tableau named
         *  "runTableau", then return that tableau; otherwise, create
         *  a new instance of RunTableau for the top effigy, and
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
            return super.createTableau(effigy.topEffigy());
        }
    }
}
