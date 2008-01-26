/* The graph controller for FmvAutomaton (FSM supporting verification using formal methods) models.

 Copyright (c) 1998-2008 The Regents of the University of California.
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
package ptolemy.vergil.fsm.fmv;

import java.io.File;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import ptolemy.vergil.fsm.FSMGraphController;

// ////////////////////////////////////////////////////////////////////////
// // FmvAutomatonGraphController

/**
 * A Graph Controller for Fmv automata models. This controller adds the "Invoke NuSMV" menu item to the Graph menu.
 *
 * @author Chihhong Patrick Cheng Contributor: Edward A. Lee
 * @version $Id: FmvAutomatonGraphController.java,v 1.00 2007/04/12 03:59:41 cxh
 *          Exp $
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red ()
 */
public class FmvAutomatonGraphController extends FSMGraphController {
    /**
     * Create a new controller with the specified directory of the current
     * model. The directory is for setting the current directory of the file
     * chooser invoked by the "Compose With" menu item.
     *
     * @param directory
     *        An instance of File that specifies the directory of the current
     *        model.
     */
    public FmvAutomatonGraphController(File directory) {
        super();
        // _directory = directory;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add commands to the specified menu and toolbar, as appropriate for this
     * controller. In this class, commands are added to create ports and
     * relations.
     *
     * @param menu
     *        The menu to add to, or null if none.
     * @param toolbar
     *        The toolbar to add to, or null if none.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        // Add an item that does composition.
        // menu.addSeparator();

        // _ruleMenu = new JMenu("Rule");
        // _ruleMenu.setMnemonic(KeyEvent.VK_R);
        // _menubar.add(_ruleMenu);

        // diva.gui.GUIUtilities.addMenuItem(menu, _translateSmvAction);
        // diva.gui.GUIUtilities.addMenuItem(menu, _invokeNuSMVAction);
        // diva.gui.GUIUtilities.addToolBarButton(toolbar,_invokeNuSMVAction );
    }

}
