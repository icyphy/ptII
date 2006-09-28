package ptolemy.backtrack.eclipse.plugin.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.codemanipulation.SortMembersOperation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.jdt.internal.ui.dialogs.SortMembersMessageDialog;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;
import ptolemy.backtrack.eclipse.plugin.editor.MultiPageCompilationUnitEditor;

public class SortMembersAction implements IEditorActionDelegate {
	
	private MultiPageCompilationUnitEditor _editor;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (!(targetEditor instanceof MultiPageCompilationUnitEditor)) {
			return;
		}
		
		_editor = (MultiPageCompilationUnitEditor) targetEditor;
	}

    private boolean containsRelevantMarkers(IEditorPart editor) {
		IAnnotationModel model= JavaUI.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		Iterator iterator= model.getAnnotationIterator();
		while (iterator.hasNext()) {
			Object element= iterator.next();
			if (element instanceof IJavaAnnotation) {
				IJavaAnnotation annot= (IJavaAnnotation) element;
				if (!annot.isMarkedDeleted() && annot.isPersistent() && !annot.isProblem())
					return true;
			}
		}		
		return false;
	}

    /** Sort the source code in the editor.
     * 
     *  @param action The action proxy (not used in this method).
     */
    public void run(IAction action) {
    	Shell shell= _editor.getSite().getShell();
		IWorkingCopyManager manager = JavaPlugin.getDefault().getWorkingCopyManager();
		ICompilationUnit cu= manager.getWorkingCopy(_editor.getEditorInput());
		if (cu == null) {
			return;
		}
		
		if (!ActionUtil.isProcessable(shell, cu)) {
			return;
		}
		
		SortMembersMessageDialog dialog= new SortMembersMessageDialog(shell);
		if (dialog.open() != Window.OK) {
			return;
		}
		
		if (!ElementValidator.check(cu, shell, ActionMessages.SortMembersAction_dialog_title, false)) {
			return;
		}
		
		if (containsRelevantMarkers(_editor)) {
			int returnCode= OptionalMessageDialog.open(
					"ptolemy.backtrack.eclipse.plugin.actions.SortMembersAction", 
					shell, 
					ActionMessages.SortMembersAction_dialog_title,
					null,
					ActionMessages.SortMembersAction_containsmarkers,  
					MessageDialog.WARNING,
					new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
					0);
			if (returnCode != OptionalMessageDialog.NOT_SHOWN && 
					returnCode != Window.OK ) return;	
		}
		
		ISchedulingRule schedulingRule = ResourcesPlugin.getWorkspace().getRoot();
		SortMembersOperation op= new SortMembersOperation(cu, null, false);
		try {
			BusyIndicatorRunnableContext context= new BusyIndicatorRunnableContext();
			PlatformUI.getWorkbench().getProgressService().runInUI(context,
				new WorkbenchRunnableAdapter(op, schedulingRule),
				schedulingRule);
		} catch (InvocationTargetException e) {
			OutputConsole.outputError(e.getMessage());
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled by user.
		}
    }

    /** Handle the change of selection.
     * 
     *  @param action The action proxy (not used in this method).
     *  @param selection The new selection (not used in this method).
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
