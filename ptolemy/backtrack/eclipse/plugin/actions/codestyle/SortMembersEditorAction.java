package ptolemy.backtrack.eclipse.plugin.actions.codestyle;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ptolemy.backtrack.eclipse.plugin.editor.MultiPageCompilationUnitEditor;

public class SortMembersEditorAction implements IWorkbenchWindowActionDelegate {

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

        IWorkingCopyManager manager = JavaPlugin.getDefault()
                .getWorkingCopyManager();
        ICompilationUnit compilationUnit = manager.getWorkingCopy(editor
                .getEditorInput());

        SortMembersUtility.sortICompilationUnit(compilationUnit, editor);
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
