/* Action to trigger the help center of the Eclipse backtracking plugin.

@Copyright (c) 1998-2007 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.backtrack.eclipse.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

//////////////////////////////////////////////////////////////////////////
//// HelpAction

/**
 Action to trigger the help center of the Eclipse backtracking plugin.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class HelpAction implements IWorkbenchWindowActionDelegate {

    /** Dispose of system resources allocated for this actions.
     */
    public void dispose() {
    }

    /** Initialize the action with a window as its parent.
     *
     *  @param window The parent window.
     */
    public void init(IWorkbenchWindow window) {
        _window = window;
    }

    /** Activate the action and show the Eclipse help center. The help center
     *  shows the Eclipse backtracking plugin documentation, whose table of
     *  contents page is "ptolemy/backtrack/eclipse/plugin/doc/toc.xml".
     *
     *  @param action The action proxy (not used in this method).
     */
    public void run(IAction action) {
        _window.getWorkbench().getHelpSystem().displayHelpResource(
                HELP_RESOURCE);
    }

    /** Handle the change of selection.
     *
     *  @param action The action proxy (not used in this method).
     *  @param selection The new selection (not used in this method).
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /** The table of contents of the Eclipse backtracking plugin help.
     */
    public static final String HELP_RESOURCE = "/ptolemy.backtrack/ptolemy/backtrack/eclipse/plugin/doc/toc.xml";

    /** The parent window.
     */
    private IWorkbenchWindow _window;
}
