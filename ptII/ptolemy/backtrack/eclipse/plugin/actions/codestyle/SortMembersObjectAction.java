/* Sort members object action.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2006-2009 The Regents of the University of California.
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

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;

//////////////////////////////////////////////////////////////////////////
//// SortMembersObjectAction

/**
 * Sort members object action.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class SortMembersObjectAction implements IObjectActionDelegate {

    public void run(IAction action) {
        if (_lastSelection != null && _lastSelection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) _lastSelection;
            Iterator<?> iterator = treeSelection.iterator();
            while (iterator.hasNext()) {
                ICompilationUnit compilationUnit = (ICompilationUnit) iterator
                        .next();
                try {
                    IEditorPart editor = EditorUtility.openInEditor(
                            compilationUnit, false);
                    if (editor != null) {
                        SortMembersUtility.sortICompilationUnit(
                                compilationUnit, editor);
                    }
                    if (!editor.isDirty()) {
                        ((ITextEditor) editor).close(false);
                    }
                } catch (CoreException e) {
                    OutputConsole.outputError(e.getMessage());
                }
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        _lastSelection = selection;
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    private ISelection _lastSelection;
}
