package ptolemy.backtrack.plugin.editor;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import ptolemy.backtrack.ast.Transformer;
import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.console.OutputConsole;
import ptolemy.backtrack.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.plugin.util.Environment;
import ptolemy.backtrack.util.Strings;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class MultiPageCompilationUnitEditor extends PtolemyEditor {
    
    protected void _createPages() {
		_createRawPage();
		_createPreviewPage();
	}
    
    public void createPartControl(Composite parent) {
        _container = _createContainer(parent);
        
        _createPages();
        
        // Create _preview's part control first so that its key bindings do not
        // conflict with superclass' key bindings.
        _preview.createPartControl((Composite)_container.getItem(1).getControl());
        super.createPartControl((Composite)_container.getItem(0).getControl());

        ISourceViewer sourceViewer= getSourceViewer();
        if (sourceViewer == null)
            return;
        IDocument document= sourceViewer.getDocument();
        if (document != null)
            document.addDocumentListener(new IDocumentListener() {
                public void documentAboutToBeChanged(
                        DocumentEvent event) {
                }

                public void documentChanged(DocumentEvent event) {
                    _needRefactoring = true;
                }
            });

        _setActivePage(0);

        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        store.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                if (property == PreferenceConstants.BACKTRACK_ROOT ||
                        property == PreferenceConstants.BACKTRACK_PREFIX) {
                    try {
                        IFile previewFile = _getPreviewFile();
                        _preview.setInput(new FileEditorInput(previewFile));
                    } catch (Exception e) {
                        OutputConsole.outputError(e.getMessage());
                    }
                }
            }
        });
    }
    
    private CTabFolder _createContainer(Composite parent) {
        final CTabFolder newContainer = new CTabFolder(parent, SWT.BOTTOM
                | SWT.FLAT);
        newContainer.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int newPageIndex = newContainer.indexOf((CTabItem) e.item);
                pageChange(newPageIndex);
            }
        });
        return newContainer;
    }
	
    protected void pageChange(int newPageIndex) {
		if (newPageIndex == 1) {
            if (_preview == null)
                _setupPreviewPage();
			_update();
		}
	}
	
    protected void _update() {
        if (!_needRefactoring)
            return;

        String PTII = Environment.getPtolemyHome(_container.getShell());
        if (PTII == null)
            return;
        
        String root = Environment.getRefactoringRoot(_container.getShell());
        if (root == null)
            return;
        
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        IFile file = (IFile)getEditorInput().getAdapter(IFile.class);
        IFile previewFile =
            ((IFileEditorInput)_preview.getEditorInput()).getFile();

        PipedOutputStream outputStream = new PipedOutputStream();
        OutputStreamWriter writer =
            new OutputStreamWriter(outputStream);

        try {
            Environment.createFolders(previewFile.getParent());
    
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
            
            PipedInputStream inputStream = new PipedInputStream(outputStream);
            CompilationUnit compilationUnit = _getCompilationUnit();
            new RefactoredOutputThread(previewFile, inputStream).start();
            
            String[] classPaths = null;
            String[] PTClassPaths = Environment.getClassPaths(null);
            String[] extraClassPaths = new String[0];
            if (file.getProject() != null)
                extraClassPaths = new String[]{
                    file.getProject().getLocation().toOSString()
                };
            String extraClassPathsInOptions = store.getString(
                    PreferenceConstants.BACKTRACK_EXTRA_CLASSPATHS);
            if (!extraClassPathsInOptions.equals(""))
                extraClassPaths = Strings.combineArrays(extraClassPaths,
                        Strings.decodeFileNames(extraClassPathsInOptions));
            if (!Environment.setupTransformerArguments(_container.getShell(),
                    false, true)) {
                OutputConsole.outputError("Cannot setup Transformer environment.");
                return;
            }

            BusyIndicator.showWhile(Display.getCurrent(),
                    new TransformerRunnable(file.getLocation().toOSString(),
                            compilationUnit, writer,
                            Strings.combineArrays(PTClassPaths, extraClassPaths),
                            new String[]{}));
            _needRefactoring = false;
        } catch (Exception e) {
            OutputConsole.outputError(e.getMessage());
        } finally {
            try {
                writer.close();
                _preview.setInput(new FileEditorInput(previewFile));
                _preview.getViewer().setEditable(false);
            } catch (Exception e) {
                OutputConsole.outputError(e.getMessage());
            }
        }
    }

    protected void _setActivePage(int pageIndex) {
        _container.setSelection(pageIndex);
    }
    
    protected void setTitleImage(Image titleImage) {
        // TODO: Set different title images for different states.
    }

    private void _createRawPage() {
        int pageIndex = 0;
        
        Composite composite = new Composite(_container, SWT.NULL);
        composite.setLayout(new FillLayout());
        //super.createPartControl(composite);
        
        _editor = this;
        
        CTabItem item = _createItem(pageIndex, composite);
        item.setText("Raw");
        item.setToolTipText("Editor for raw Java source file");
    }
    
    private void _createPreviewPage() {
        int pageIndex = 1;
        
        Composite composite = new Composite(_container, SWT.NULL);
        composite.setLayout(new FillLayout());
        
        _createItem(pageIndex, composite);
        _setupPreviewPage();
        
        CTabItem item = _container.getItem(pageIndex);
        item.setText("Preview");
        item.setToolTipText("Preview for refactored Java source " +
                "(a build might be necessary to get the accurate result)");
    }
    
    private void _setupPreviewPage() {
        int pageIndex = 1;

        _preview = new PtolemyEditor();
        IFile previewFile = _getPreviewFile();
        if (previewFile != null) {
            try {
                _preview.init(_editor.getEditorSite(),
                        new FileEditorInput(previewFile) {
                    public boolean exists() {
                        return true;
                    }
                });
            } catch (Exception e) {
                OutputConsole.outputError(e.getMessage());
            }
        }
    }
    
    private CompilationUnit _getCompilationUnit() throws JavaModelException {
        IWorkingCopyManager manager =
            JavaPlugin.getDefault().getWorkingCopyManager();
        ICompilationUnit unit =
            manager.getWorkingCopy(getEditorInput());
        return AST.parseCompilationUnit(unit, false);
    }
    
    private CTabItem _createItem(int index, Control control) {
        CTabItem item = new CTabItem(_container, SWT.NONE, index);
        item.setControl(control);
        return item;
    }
    
    private IFile _getPreviewFile() {
        try {
            IFile file = (IFile)getEditorInput().getAdapter(IFile.class);
            
            CompilationUnit compilationUnit = _getCompilationUnit();
            String packageName = null;
            if (compilationUnit.getPackage() != null)
                packageName = compilationUnit.getPackage().getName().toString();
            
            IPath refactoredFile =
                Environment.getRefactoredFile(file.getLocation().toOSString(),
                        packageName);
            IFile previewFile =
                Environment.getContainer(refactoredFile).getFile(null);
            
            return previewFile;
        } catch (Exception e) {
            OutputConsole.outputError(e.getMessage());
            return null;
        }
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

    private class TransformerRunnable implements Runnable {
        TransformerRunnable(String fileName, CompilationUnit compilationUnit,
                Writer writer, String[] classPaths, String[] crossAnalyzedTypes) {
            _fileName = fileName;
            _compilationUnit = compilationUnit;
            _writer = writer;
            _classPaths = classPaths;
            _crossAnalyzedTypes = crossAnalyzedTypes;
        }
        
        public void run() {
            try {
                Transformer.transform(_fileName, _compilationUnit, _writer,
                        _classPaths, _crossAnalyzedTypes);
            } catch (Exception e) {
                OutputConsole.outputError(e.getMessage());
            }
        }
        
        private String _fileName;
        
        private CompilationUnit _compilationUnit;
        
        private Writer _writer;
        
        private String[] _classPaths;
        
        private String[] _crossAnalyzedTypes;
    }
    
    private CompilationUnitEditor _editor;
    
    private PtolemyEditor _preview;
    
    private boolean _needRefactoring = true;
    
    private CTabFolder _container;
}