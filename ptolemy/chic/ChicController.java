/* The node controller for ChicInvoker visible attributes.

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

@ProposedRating
@AcceptedRating
*/

package ptolemy.chic;

// Diva imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// ChicController
/**
This class provides interaction with nodes that represent ChicInvoker
visible attributes.  It provides a double click binding and context menu
entry to edit the parameters of the node ("Configure"), a command to get
documentation ("Get Documentation"), a command to look inside ("Look
Inside") and commands for invoking Chic. It can have one of two access
levels, FULL or PARTIAL. If the access level is FULL, then the context
menu also contains a command to rename the node ("Customize Name") and
a command to set the icon of the node ("Set Icon").

@author Eleftherios Matsikoudis
@version $Id$
@since Ptolemy II 3.0
*/
public class ChicController extends AttributeController {

    /** Create a Chic controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public ChicController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a Chic controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public ChicController(GraphController controller, Access access) {
        super(controller, access);

        _access = access;

        // Add commands to invoke Chic
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new AsynchronousIOAction()));
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new SynchronousAGAction()));
        //        _menuFactory.addMenuItemFactory(
        //                new MenuActionFactory(new BidirectionalSynAction()));
        //        _menuFactory.addMenuItemFactory(
        //                new MenuActionFactory(new StatelessSoftwareAction()));
        //        _menuFactory.addMenuItemFactory(
        //                new MenuActionFactory(new StatefulSoftwareAction()));

        // Add a command to look inside
        if (_configuration != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_lookInsideAction));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is used to open documentation files.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        if (_configuration != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_lookInsideAction));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The action that handles look inside.  This is accessed by
     *  by ActorViewerController to create a hot key for the editor.
     */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Access _access;

    // Error message used when we can't find the inside definition.
    private static String _CANNOT_FIND_MESSAGE =
    "Cannot find inside definition. "
    + "Perhaps source code is not installed? "
    + "You can obtain source code for Berkeley actors at: "
    + "http://ptolemy.eecs.berkeley.edu/ptolemyII";

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to invoke CHIC: Asynchronous I/O
     */
    private class AsynchronousIOAction extends FigureAction {

        public AsynchronousIOAction() {
            super("CHIC: Asynchronous I/O");
        }

        public void actionPerformed(ActionEvent e) {

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            try {
                ((ChicInvoker)object).checkInterfaceCompatibility(
                        ChicInvoker.ASYNCHRONOUS_IO,
                        false);
            } catch (IllegalActionException ex) {
                MessageHandler.error(ex.getMessage());
            } catch (NameDuplicationException ex) {
                MessageHandler.error(ex.getMessage());
            }
            return;
        }
    }

    //    /** An action to invoke CHIC: Bidirectional Syn A/G
    //     */
    //    private class BidirectionalSynAction extends FigureAction {
    //
    //        public BidirectionalSynAction() {
    //            super("CHIC: Bidirectional Syn A/G");
    //        }
    //
    //        public void actionPerformed(ActionEvent e) {
    //
    //            // Determine which entity was selected for the look inside action.
    //            super.actionPerformed(e);
    //            NamedObj object = getTarget();
    //            try {
    //                ((ChicInvoker)object).checkInterfaceCompatibility(
    //                        ChicInvoker.BIDIRECTIONAL_SYN_AG,
    //                        false);
    //            } catch (IllegalActionException ex) {
    //                MessageHandler.error(ex.getMessage());
    //            } catch (NameDuplicationException ex) {
    //                MessageHandler.error(ex.getMessage());
    //            }
    //            return;
    //        }
    //    }

    /** An action to look inside the Chic visible attribute.
     */
    private class LookInsideAction extends FigureAction {

        public LookInsideAction() {
            super("Look Inside (Ctrl+L)");
            // For some inexplicable reason, the I key doesn't work here.
            // Use L, which used to be used for layout.
            putValue(
                    GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot look inside without a configuration.");
                return;
            }

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);
            NamedObj object = getTarget();

            // Open the source code, if possible.
            String filename =
                object.getClass().getName().replace('.', '/') + ".java";
            try {
                URL toRead = getClass().getClassLoader().getResource(filename);
                if (toRead != null) {
                    _configuration.openModel(
                            null,
                            toRead,
                            toRead.toExternalForm());
                } else {
                    MessageHandler.error(_CANNOT_FIND_MESSAGE);
                }
            } catch (Exception ex) {
                MessageHandler.error(_CANNOT_FIND_MESSAGE, ex);
            }
            return;
        }
    }

    //    /** An action to invoke CHIC: Stateful Software
    //     */
    //    private class StatefulSoftwareAction extends FigureAction {
    //
    //        public StatefulSoftwareAction() {
    //            super("CHIC: Stateful Software");
    //        }
    //
    //        public void actionPerformed(ActionEvent e) {
    //
    //            // Determine which entity was selected for the look inside action.
    //            super.actionPerformed(e);
    //            NamedObj object = getTarget();
    //            try {
    //                ((ChicInvoker)object).checkInterfaceCompatibility(
    //                        ChicInvoker.STATEFUL_SOFTWARE,
    //                        false);
    //            } catch (IllegalActionException ex) {
    //                MessageHandler.error(ex.getMessage());
    //            } catch (NameDuplicationException ex) {
    //                MessageHandler.error(ex.getMessage());
    //            }
    //            return;
    //        }
    //    }

    //    /** An action to invoke CHIC: Stateless Software
    //     */
    //    private class StatelessSoftwareAction extends FigureAction {
    //
    //        public StatelessSoftwareAction() {
    //            super("CHIC: Stateless Software");
    //        }
    //
    //        public void actionPerformed(ActionEvent e) {
    //
    //            // Determine which entity was selected for the look inside action.
    //            super.actionPerformed(e);
    //            NamedObj object = getTarget();
    //            try {
    //                ((ChicInvoker)object).checkInterfaceCompatibility(
    //                        ChicInvoker.STATELESS_SOFTWARE,
    //                        false);
    //            } catch (IllegalActionException ex) {
    //                MessageHandler.error(ex.getMessage());
    //            } catch (NameDuplicationException ex) {
    //                MessageHandler.error(ex.getMessage());
    //            }
    //            return;
    //        }
    //    }

    /** An action to invoke CHIC: Synchronous A/G
     */
    private class SynchronousAGAction extends FigureAction {

        public SynchronousAGAction() {
            super("CHIC: Synchronous A/G");
        }

        public void actionPerformed(ActionEvent e) {

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            try {
                ((ChicInvoker)object).checkInterfaceCompatibility(
                        ChicInvoker.SYNCHRONOUS_AG,
                        false);
            } catch (IllegalActionException ex) {
                MessageHandler.error(ex.getMessage());
            } catch (NameDuplicationException ex) {
                MessageHandler.error(ex.getMessage());
            }
            return;
        }
    }
}
