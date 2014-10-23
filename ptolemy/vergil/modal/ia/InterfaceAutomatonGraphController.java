/* The graph controller for interface automata models.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal.ia;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.modal.kernel.ia.InterfaceAutomaton;
import ptolemy.gui.JFileChooserBugFix;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.modal.FSMGraphController;

///////////////////////////////////////////////////////////////////
//// InterfaceAutomatonGraphController

/**
 A Graph Controller for interface automata models.  This controller adds
 the "Compose With" menu item to the Graph menu.

 @author Steve Neuendorffer, Yuhong Xiong, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (johnr)
 */
public class InterfaceAutomatonGraphController extends FSMGraphController {
    /** Create a new controller with the specified directory of the current
     *  model. The directory is for setting the current directory of
     *  the file chooser invoked by the "Compose With" menu item.
     *  @param directory An instance of File that specifies the directory
     *   of the current model.
     */
    public InterfaceAutomatonGraphController(File directory) {
        super();
        _directory = directory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this class, commands are added to create
     *  ports and relations.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    @Override
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        // Add an item that does composition.
        // menu.addSeparator();
        diva.gui.GUIUtilities.addMenuItem(menu, _composeWithAction);

        // diva.gui.GUIUtilities.addToolBarButton(toolbar, _newStateAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The action for composing with another interface automaton.
    private ComposeWithAction _composeWithAction = new ComposeWithAction();

    // The directory of the current model.
    private File _directory;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// ComposeWithAction

    /** An action to perform composition. */
    @SuppressWarnings("serial")
    public class ComposeWithAction extends AbstractAction {
        /** Create a new action to perform composition. */
        public ComposeWithAction() {
            super("Compose With");
            putValue("tooltip", "Compose with another interface automaton");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_C));
        }

        /** Compose with another interface automaton by first opening a file
         *  chooser dialog and then composing with the specified model.
         *  The specified model and the composition result are shown in
         *  two new interface automaton graph frames.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // NOTE: This code is mostly copied from Top.

            // Avoid white boxes in file chooser, see
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3801
            JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
            Color background = null;
            try {
                background = jFileChooserBugFix.saveBackground();
                JFileChooser fileDialog = new JFileChooser();
                fileDialog
                .setDialogTitle("Select an interface automaton to compose with.");

                if (_directory != null) {
                    fileDialog.setCurrentDirectory(_directory);
                } else {
                    // The default on Windows is to open at user.home, which is
                    // typically an absurd directory inside the O/S installation.
                    // So we use the current directory instead.
                    // FIXME: Could this throw a security exception in an applet?
                    String cwd = StringUtilities.getProperty("user.dir");

                    if (cwd != null) {
                        fileDialog.setCurrentDirectory(new File(cwd));
                    }
                }

                int returnValue = fileDialog.showOpenDialog(getFrame());

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    _directory = fileDialog.getCurrentDirectory();

                    try {
                        // NOTE: It would be nice if it were possible to enter
                        // a URL in the file chooser, but Java's file chooser does
                        // not permit this, regrettably.  So we have a separate
                        // menu item for this.
                        File file = fileDialog.getSelectedFile()
                                .getCanonicalFile();
                        URL url = file.toURI().toURL();

                        // NOTE: Used to use for the first argument the following,
                        // but it seems to not work for relative file references:
                        // new URL("file", null, _directory.getAbsolutePath()
                        Configuration configuration = getConfiguration();
                        Tableau newAutomatonTableau = configuration.openModel(
                                url, url, url.toExternalForm());

                        // compose the two interface automata and show result
                        InterfaceAutomaton model1 = (InterfaceAutomaton) getFrame()
                                .getModel();
                        InterfaceAutomatonGraphFrame graphFrame2 = (InterfaceAutomatonGraphFrame) newAutomatonTableau
                                .getFrame();
                        InterfaceAutomaton model2 = (InterfaceAutomaton) graphFrame2
                                .getModel();

                        InterfaceAutomaton composition = model1.compose(model2);
                        configuration.openModel(composition);
                    } catch (Exception ex) {
                        getFrame().report("Error reading input", ex);
                    }
                }
            } finally {
                jFileChooserBugFix.restoreBackground(background);
            }
        }
    }
}
