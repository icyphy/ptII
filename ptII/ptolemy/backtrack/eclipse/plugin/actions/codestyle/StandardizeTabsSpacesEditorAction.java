/* Standardize tab spaces editor action.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2007-2009 The Regents of the University of California.
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
*/
package ptolemy.backtrack.eclipse.plugin.actions.codestyle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ptolemy.backtrack.eclipse.plugin.editor.MultiPageCompilationUnitEditor;

//////////////////////////////////////////////////////////////////////////
//// StandardizeTabSpacesEditorAction

/**
 * Standardize tab spaces editor action.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class StandardizeTabsSpacesEditorAction implements
        IWorkbenchWindowActionDelegate {

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        _window = window;
    }

    /** Sort the source code in the editor.
     *
     *  @param action The action proxy (not used in this method).
     */
    public void run(IAction action) {
        IEditorPart editorPart = _window.getActivePage().getActiveEditor();
        MultiPageCompilationUnitEditor editor = (MultiPageCompilationUnitEditor) editorPart;

        StandardizeTabsSpacesUtility.standardize(editor);
    }

    /** Handle the change of selection.
     *
     *  @param action The action proxy (not used in this method).
     *  @param selection The new selection (not used in this method).
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    private IWorkbenchWindow _window;

}
