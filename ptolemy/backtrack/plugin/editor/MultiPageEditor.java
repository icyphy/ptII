package ptolemy.backtrack.plugin.editor;


import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import ptolemy.backtrack.ast.Transformer;
import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.console.OutputConsole;
import ptolemy.backtrack.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.plugin.util.Environment;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class MultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener{
	
    public MultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
    protected void createPages() {
		_createPage0();
		_createPage1();
	}
	
    public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
    public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	
    public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	
    public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	
    public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}
	
    public boolean isSaveAsAllowed() {
		return true;
	}
	
    protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 1) {
			_update();
		}
	}
	
    public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((IFileEditorInput)_editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(_editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}
    
    protected void _update() {
        IFile file = (IFile)getEditorInput().getAdapter(IFile.class);
        IFile previewFile =
            ((IFileEditorInput)_preview.getEditorInput()).getFile();
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();

        boolean overwrite =
            store.getBoolean(PreferenceConstants.BACKTRACK_OVERWRITE);
        if (!overwrite && previewFile.exists() && previewFile.isLocal(0)) {
            OutputConsole.outputError("Preview file \"" +
                    previewFile.getLocation().toOSString() +
                    "\" already exists.\nTo overwrite it, please " +
                    "modify the overwrite option in the preference "+
                    "page.");
            return;
        }
        
        PipedOutputStream outputStream = new PipedOutputStream();
        OutputStreamWriter writer =
            new OutputStreamWriter(outputStream);
        try {
            PipedInputStream inputStream = new PipedInputStream(outputStream);
            CompilationUnit compilationUnit = _getCompilationUnit();
            new RefactoredOutputThread(previewFile, inputStream).start();
            
            String[] classPaths = null;
            String[] PTClassPaths = Environment.getClassPaths(null);
            String[] extraClassPaths = new String[0];
            if (file.getProject() != null)
                extraClassPaths = new String[]{file.getProject().getLocation().toOSString()};
            if (!Environment.setupTransformerArguments(getContainer().getShell(),
                    false, true)) {
                OutputConsole.outputError("Cannot setup Transformer environment.");
                return;
            }
            Environment.createFolders(previewFile.getParent());
            // FIXME: classpaths
            Transformer.transform(file.getName(), compilationUnit, writer,
                    Environment.combineArrays(PTClassPaths, extraClassPaths),
                    new String[]{});
        } catch (Exception e) {
            OutputConsole.outputError(e.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                OutputConsole.outputError(e.getMessage());
            }
        }
    }
    
    private void _createPage0() {
        try {
            IFile file = (IFile)getEditorInput().getAdapter(IFile.class);
            Class c = file.getClass();
            _editor = new CompilationUnitEditor();
            int index = addPage(_editor, getEditorInput());
            setPageText(index, "Raw");
        } catch (PartInitException e) {
            ErrorDialog.openError(
                getSite().getShell(),
                "Error creating nested text editor",
                null,
                e.getStatus());
        }
    }
    
    private void _createPage1() {
        IFile file = (IFile)getEditorInput().getAdapter(IFile.class);
        try {
            CompilationUnit compilationUnit = _getCompilationUnit();
            String packageName = null;
            if (compilationUnit.getPackage() != null)
                packageName = compilationUnit.getPackage().getName().toString();
            
            IPath refactoredFile =
                Environment.getRefactoredFile(file.getLocation().toOSString(),
                        packageName);
            IFile previewFile =
                Environment.getContainer(refactoredFile).getFile(null);
            IContainer parent = previewFile.getParent();
            Environment.createFolders(parent);
            if (!previewFile.exists())
                previewFile.create(null, true, null);
            
            _preview = new CompilationUnitEditor();
            int index = addPage(_preview, new FileEditorInput(previewFile));
            setPageText(index, "Preview");
        } catch (Exception e) {
            OutputConsole.outputError(e.getMessage());
        }
    }
    
    private CompilationUnit _getCompilationUnit() throws JavaModelException {
        IWorkingCopyManager manager =
            JavaPlugin.getDefault().getWorkingCopyManager();
        ICompilationUnit unit =
            manager.getWorkingCopy(getEditorInput());
        return AST.parseCompilationUnit(unit, false);
    }
    
    
    private class RefactoredFile extends File {
        RefactoredFile(IPath path, Workspace container) {
            super(path, container);
        }
    }
    
    
    private class RefactoredOutputThread extends Thread {
        RefactoredOutputThread(IFile file, PipedInputStream inputStream) {
            _file = file;
            _inputStream = inputStream;
        }
        
        public void run() {
            try {
                if (_file.exists())
                    _file.setContents(_inputStream, true, false, null);
                else
                    _file.create(_inputStream, true, null);
            } catch (Exception e) {
                OutputConsole.outputError(e.getMessage());
            } finally {
                try {
                    _inputStream.close();
                } catch (Exception e) {
                    OutputConsole.outputError(e.getMessage());
                }
            }
        }
        
        private PipedInputStream _inputStream;
        
        private IFile _file;
    }

    public boolean isDirty() {
        return _editor.isDirty();
    }
    
    private CompilationUnitEditor _editor;
    
    private CompilationUnitEditor _preview;
}