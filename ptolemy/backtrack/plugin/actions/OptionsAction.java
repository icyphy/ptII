/*

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ptolemy.backtrack.plugin.preferences.PreferenceConstants;

//////////////////////////////////////////////////////////////////////////
//// OptionsAction

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class OptionsAction implements IWorkbenchWindowActionDelegate {
    /**
     *
     */
    public void dispose() {
    }

    /**
     *  @param window
     */
    public void init(IWorkbenchWindow window) {
        _window = window;
    }

    /**
     *  @param action
     */
    public void run(IAction action) {
        IPreferenceNode node = _window.getWorkbench().getPreferenceManager()
                .find(PreferenceConstants.PTII_PREFERENCE_ID);
        PreferenceManager manager = new PreferenceManager();
        manager.addToRoot(node);

        PreferenceDialog dialog = new PreferenceDialog(_window.getShell(),
                manager);
        dialog.open();
    }

    /**
     *  @param action
     *  @param selection
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    private IWorkbenchWindow _window;
}
