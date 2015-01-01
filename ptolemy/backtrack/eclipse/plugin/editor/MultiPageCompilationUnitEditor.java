/* Multi-page editor with Ptolemy semantic highlighting and transformation tab.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.editor;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.ui.part.FileEditorInput;

import ptolemy.backtrack.eclipse.ast.Transformer;
import ptolemy.backtrack.eclipse.ast.Type;
import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;
import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;
import ptolemy.backtrack.eclipse.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.eclipse.plugin.util.Environment;
import ptolemy.backtrack.util.Strings;

///////////////////////////////////////////////////////////////////
//// MultiPageCompilationUnitEditor
/**
 Multi-page editor with Ptolemy semantic highlighting and transformation tab.
 This editor is the main user interface in the Eclipse plugin. It extends
 Eclipse's Java editor, with Ptolemy semantic highlighting added. It also
 creates two tabs in the editor: the "Raw" tab provides an ordinary Java
 editing environment to the user; the "Preview" tab shows the preview of
 backtracking transformation.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class MultiPageCompilationUnitEditor extends PtolemyEditor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the controls for this editor.
     *
     *  @param parent The parent of the editor.
     */
    public void createPartControl(Composite parent) {
        _container = _createContainer(parent);

        // Create two tabs.
        _createRawPage();
        _createPreviewPage();

        // Create _preview's part control first so that its key bindings do not
        // conflict with superclass' key bindings.
        _preview.createPartControl((Composite) _container.getItem(1)
                .getControl());
        super.createPartControl((Composite) _container.getItem(0).getControl());

        ISourceViewer sourceViewer = getSourceViewer();

        if (sourceViewer == null) {
            return;
        }

        IDocument document = sourceViewer.getDocument();

        if (document != null) {
            document.addDocumentListener(new IDocumentListener() {
                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                public void documentChanged(DocumentEvent event) {
                    _needRefactoring = true;
                }
            });
        }

        setActivePage(0);

        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        store.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();

                if ((property == PreferenceConstants.BACKTRACK_ROOT)
                        || (property == PreferenceConstants.BACKTRACK_PREFIX)) {
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

    /** Dispose the two views in this editor.
     */
    public void dispose() {
        _preview.dispose();
        super.dispose();
    }

    /** Set the active tab of the editor.
     *
     *  @param pageIndex The index of the tab to change to. 0 for "Raw" tab; 1
     *   for "Preview" tab.
     */
    public void setActivePage(int pageIndex) {
        _container.setSelection(pageIndex);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the preview tab so it is not editable. */
    public void test() {
        //_preview.setInput(new FileEditorInput(previewFile));
        _preview.getViewer().setEditable(false);
    }

    /** Update the "Preview" tab if the source code is changed in the
     * "Raw" tab.
     */
    protected void _update() {
        if (!_needRefactoring) {
            return;
        }

        String root = Environment.getRefactoringRoot(_container.getShell());

        if (root == null) {
            return;
        }

        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
        IFile previewFile = _getPreviewFile();

        PipedOutputStream outputStream = new PipedOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, java.nio.charset.Charset.defaultCharset());

        try {
            Environment.createFolders(previewFile.getParent());

            boolean overwrite = store
                    .getBoolean(PreferenceConstants.BACKTRACK_OVERWRITE);

            if (!overwrite && previewFile.exists()) {
                OutputConsole.outputError("Preview file \""
                        + previewFile.getLocation().toOSString()
                        + "\" already exists.\nTo overwrite it, please "
                        + "modify the overwrite option in the preference "
                        + "page.");
                return;
            }

            PipedInputStream inputStream = new PipedInputStream(outputStream);
            CompilationUnit compilationUnit = _getCompilationUnit();
            new RefactoringOutputThread(previewFile, inputStream).start();

            String[] PTClassPaths = Environment.getClassPaths(null);

            IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
            IProject[] projects = workspace.getProjects();
            String[] extraClassPaths = new String[projects.length];

            for (int i = 0; i < projects.length; i++) {
                extraClassPaths[i] = projects[i].getLocation().toOSString();
            }

            String extraClassPathsInOptions = store
                    .getString(PreferenceConstants.BACKTRACK_EXTRA_CLASSPATHS);

            if (!extraClassPathsInOptions.equals("")) {
                extraClassPaths = Strings.combineArrays(extraClassPaths,
                        Strings.decodeFileNames(extraClassPathsInOptions));
            }

            if (!Environment.setupTransformerArguments(_container.getShell(),
                    false, true)) {
                OutputConsole
                        .outputError("Cannot setup Transformer environment.");
                return;
            }

            BusyIndicator.showWhile(Display.getCurrent(),
                    new TransformerRunnable(file.getLocation().toOSString(),
                            compilationUnit, writer, Strings.combineArrays(
                                    PTClassPaths, extraClassPaths),
                            new String[] {}));
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

    /** Override the title image so that the Ptolemy icon is always used as the
     *  editor's title image.
     *
     *  @param titleImage The new title image to be set, which is always
     *   ignored.
     */
    protected void setTitleImage(Image titleImage) {
        ImageDescriptor descriptor = EclipsePlugin
                .getImageDescriptor("ptolemy/backtrack/eclipse/plugin/icons/ptolemy_icon.gif");
        super.setTitleImage(descriptor.createImage());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the top-level container for this editor.
     *
     *  @param parent The parent of the top-level container.
     *  @return The tab folder as the container of the two tabs.
     */
    private CTabFolder _createContainer(Composite parent) {
        final CTabFolder newContainer = new CTabFolder(parent, SWT.BOTTOM
                | SWT.FLAT);
        newContainer.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int newPageIndex = newContainer.indexOf((CTabItem) e.item);
                if (newPageIndex == 1) {
                    _update();
                }
            }
        });
        return newContainer;
    }

    /** Create an item for tab folder.
     *
     *  @param index The index of the item.
     *  @param control The control for the item.
     *  @return The item created in the tab folder.
     */
    private CTabItem _createItem(int index, Control control) {
        CTabItem item = new CTabItem(_container, SWT.NONE, index);
        item.setControl(control);
        return item;
    }

    /** Create the "Preview" tab.
     */
    private void _createPreviewPage() {
        int pageIndex = 1;

        Composite composite = new Composite(_container, SWT.NULL);
        composite.setLayout(new FillLayout());

        _createItem(pageIndex, composite);
        _setupPreviewPage();

        CTabItem item = _container.getItem(pageIndex);
        item.setText("Preview");
        item.setToolTipText("Preview for refactored Java source "
                + "(a build might be necessary to get the accurate result)");
    }

    /** Create the "Raw" tab.
     */
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

    /** Get the compilation unit for the Java code in the "Raw" tab.
     *
     *  @return The compilation unit.
     *  @exception JavaModelException If the compilation unit cannot be
     *   retrieved from the Eclipse Java editor.
     */
    private CompilationUnit _getCompilationUnit() throws JavaModelException {

        IWorkingCopyManager manager = JavaPlugin.getDefault()
                .getWorkingCopyManager();
        ICompilationUnit unit = manager.getWorkingCopy(getEditorInput());

        CompilerOptions options = new CompilerOptions(unit.getJavaProject()
                .getOptions(true));
        ASTParser parser = ASTParser.newParser(AST.JLS3); // FIXME
        parser.setCompilerOptions(options.getMap());
        parser.setSource(unit.getBuffer().getCharacters());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(false);
        CompilationUnit result = (CompilationUnit) parser.createAST(null);

        return result; //AST.parseCompilationUnit(unit, false);
    }

    /** Get the file containing the transformed code for preview.
     *
     *  @return The file.
     */
    private IFile _getPreviewFile() {
        try {
            IFile file = (IFile) getEditorInput().getAdapter(IFile.class);

            CompilationUnit compilationUnit = _getCompilationUnit();
            String packageName = null;

            if (compilationUnit.getPackage() != null) {
                packageName = compilationUnit.getPackage().getName().toString();
            }

            IPath refactoredFile = Environment.getRefactoredFile(file
                    .getLocation().toOSString(), packageName);
            IFile previewFile = Environment.getContainer(refactoredFile)
                    .getFile(null);

            return previewFile;
        } catch (Exception e) {
            // Ignore the errors, and return null.
            // OutputConsole.outputError(e.getMessage());
            return null;
        }
    }

    /** Initialize the "Preview" tab.
     */
    private void _setupPreviewPage() {
        _preview = new PtolemyEditor();

        // In Eclipse 3.2, always set up the editor site and editor input.
        // IFile previewFile = _getPreviewFile();
        // if (previewFile != null) {
        try {
            _preview.init(_editor.getEditorSite(), getEditorInput());
        } catch (Exception e) {
            OutputConsole.outputError(e.getMessage());
        }
        // }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private inner classes                  ////

    /** The container of the two tabs in the editor.
     */
    private CTabFolder _container;

    /** The editor.
     */
    private CompilationUnitEditor _editor;

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** Whether the Java source in the "Raw" tab needs to be refactored to
     *  update the "Preview" tab.
     */
    private boolean _needRefactoring = true;

    /** The "Preview" tab.
     */
    private PtolemyEditor _preview;

    ///////////////////////////////////////////////////////////////////
    //// RefactoringOutputThread
    /**
     The thread to output the refactoring result to the "Preview" tab, and
     output any error message to the backtracking console.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class RefactoringOutputThread extends Thread {

        /** Read the refactoring result from the input stream to the "Preview"
         *  tab.
         */
        public void run() {
            try {
                if (_file.exists()) {
                    _file.setContents(_inputStream, true, false, null);
                } else {
                    _file.create(_inputStream, true, null);
                }
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

        /** Construct a thread to output the refactoring result.
         *
         *  @param file The file object containing the "Preview" tab's content.
         *  @param inputStream The input stream to read refactoring result from.
         */
        RefactoringOutputThread(IFile file, PipedInputStream inputStream) {
            _file = file;
            _inputStream = inputStream;
        }

        /** The file object containing the "Preview" tab's content.
         */
        private IFile _file;

        /** The input stream to read refactoring result from.
         */
        private PipedInputStream _inputStream;
    }

    ///////////////////////////////////////////////////////////////////
    //// TransformerRunnable
    /**
     The runnable object that executes the transformation.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class TransformerRunnable implements Runnable {

        /** Execute the transformation.
         */
        public void run() {
            try {
                Transformer.transform(_fileName, _compilationUnit, _writer,
                        _classPaths, _crossAnalyzedTypes);
                Type.removeAllTypes();
            } catch (Exception e) {
                OutputConsole.outputError(e.getMessage());
            }
        }

        /** Construct a runnable object that executes the transformation.
         *
         *  @param fileName The name of the file to be refactored.
         *  @param compilationUnit The compilation unit of the Java code.
         *  @param writer The writer to output result to.
         *  @param classPaths Extra class paths.
         *  @param crossAnalyzedTypes Names of other types to be refactored at
         *   the same time.
         */
        TransformerRunnable(String fileName, CompilationUnit compilationUnit,
                Writer writer, String[] classPaths, String[] crossAnalyzedTypes) {
            _classPaths = classPaths;
            _compilationUnit = compilationUnit;
            _crossAnalyzedTypes = crossAnalyzedTypes;
            _fileName = fileName;
            _writer = writer;
        }

        /** Extra class paths.
         */
        private String[] _classPaths;

        /** The compilation unit of the Java code.
         */
        private CompilationUnit _compilationUnit;

        /** Names of other types to be refactored at the same time.
         */
        private String[] _crossAnalyzedTypes;

        /** The name of the file to be refactored.
         */
        private String _fileName;

        /** The writer to output result to.
         */
        private Writer _writer;
    }
}
