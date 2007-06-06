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
