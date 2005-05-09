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

package ptolemy.backtrack.plugin.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditorMessages;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IColorManagerExtension;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

import ptolemy.backtrack.plugin.EclipsePlugin;

//////////////////////////////////////////////////////////////////////////
//// SemanticHighlightingReconciler
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SemanticHighlightingReconciler
        implements IJavaReconcilingListener, IPropertyChangeListener,
        ITextInputListener {

    public void install(PtolemyEditor editor, IColorManager colorManager) {
        _editor = editor;
        _colorManager = colorManager;
        _preferenceStore = EclipsePlugin.getDefault().getPreferenceStore();
        _preferenceStore.addPropertyChangeListener(this);
        
        if (isEnabled())
            _enable();
    }
    
    public void aboutToBeReconciled() {
        // Do nothing
    }
    
    public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
        synchronized (_jobLock) {
            _cancelJobs= true;
            if (_job != null)
                _job.cancel();
        }
    }

    /*
     * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
     */
    public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
        synchronized (_jobLock) {
            _cancelJobs= false;
        }
        if (newInput != null)
            _scheduleJob();
    }
    
    private void _scheduleJob() {
        final IJavaElement element = _editor.getInputJavaElement();
        if (element != null) {
            Job job = new Job(
                    JavaEditorMessages.getString(
                            "PtolemySemanticHighlighting.job")) {
                protected IStatus run(IProgressMonitor monitor) {
                    synchronized (_jobLock) {
                        if (_job != null)
                            try {
                                _jobLock.wait();
                            } catch (InterruptedException e) {
                                JavaPlugin.log(e);
                            }
                        if (_cancelJobs || _job != null)
                            return Status.CANCEL_STATUS;
                        _job= this;
                    }
                    CompilationUnit ast =
                        JavaPlugin.getDefault().getASTProvider().getAST(element,
                                ASTProvider.WAIT_YES, monitor);
                    reconciled(ast, false, monitor);
                    synchronized (_jobLock) {
                        _job= null;
                        _jobLock.notifyAll();
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setSystem(true);
            job.setPriority(Job.DECORATE);
            job.schedule();
        }
    }
    
    public void reconciled(CompilationUnit ast, boolean forced,
            IProgressMonitor progressMonitor) {
        _jobPresenter= _presenter;
        _jobSemanticHighlightings= _semanticHighlightings;
        _jobHighlightings= _highlightings;
        if (_jobPresenter == null || _jobSemanticHighlightings == null || _jobHighlightings == null) {
            _jobPresenter= null;
            _jobSemanticHighlightings= null;
            _jobHighlightings= null;
            return;
        }
        
        _jobPresenter.setCanceled(progressMonitor.isCanceled());
        
        if (ast == null || _jobPresenter.isCanceled())
            return;
        
        ASTNode[] subtrees= new ASTNode[]{ast};
        
        startReconcilingPositions();
        
        if (!_jobPresenter.isCanceled())
            _reconcilePositions(subtrees);
        
        TextPresentation textPresentation= null;
        if (!_jobPresenter.isCanceled())
            textPresentation = _jobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
        
        if (!_jobPresenter.isCanceled())
            updatePresentation(textPresentation, fAddedPositions, fRemovedPositions);

        stopReconcilingPositions();
        
        _jobPresenter= null;
        _jobSemanticHighlightings= null;
        _jobHighlightings= null;
    }
    
    private void startReconcilingPositions() {
        _jobPresenter.addAllPositions(fRemovedPositions);
        fNOfRemovedPositions= fRemovedPositions.size();
    }
    
    private void stopReconcilingPositions() {
        fRemovedPositions.clear();
        fNOfRemovedPositions= 0;
        fAddedPositions.clear();
    }
    
    private void updatePresentation(TextPresentation textPresentation, List addedPositions, List removedPositions) {
        Runnable runnable= _presenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
        if (runnable == null)
            return;
        
        JavaEditor editor= _editor;
        if (editor == null)
            return;
        
        IWorkbenchPartSite site= editor.getSite();
        if (site == null)
            return;
        
        Shell shell= site.getShell();
        if (shell == null || shell.isDisposed())
            return;
        
        Display display= shell.getDisplay();
        if (display == null || display.isDisposed())
            return;
        
        display.asyncExec(runnable);
    }
    
    private void _enable() {
        _initializeHighlightings();

        _configuration =
            new JavaSourceViewerConfiguration(_colorManager,
                    _preferenceStore, _editor,
                    IJavaPartitions.JAVA_PARTITIONING);
        _presentationReconciler = (JavaPresentationReconciler)
                _configuration.getPresentationReconciler(_editor.getViewer());
        
        _presenter = new SemanticHighlightingPresenter();
        _presenter.install((JavaSourceViewer)_editor.getViewer(),
                _presentationReconciler);
        
        _jobSemanticHighlightings = _semanticHighlightings;
        
        _editor.addJavaReconcileListener(this);

        //_scheduleJob();
    }
    
    private void _disable() {
        if (_presenter != null)
            _presenter.setCanceled(true);
        
        if (_presenter != null) {
            _presenter.uninstall();
            _presenter= null;
        }
        
        _editor.removeJavaReconcileListener(this);
        
        if (_semanticHighlightings != null)
            _disposeHighlightings();
    }
    
    private void _disposeHighlightings() {
        for (int i= 0, n= _semanticHighlightings.length; i < n; i++)
            removeColor(SemanticHighlightings.getColorPreferenceKey(_semanticHighlightings[i]));
        
        _semanticHighlightings= null;
        _highlightings= null;
    }

    private void _reconcilePositions(ASTNode[] subtrees) {
        for (int i= 0, n= subtrees.length; i < n; i++)
            subtrees[i].accept(_collector);
        List oldPositions= fRemovedPositions;
        List newPositions= new ArrayList(fNOfRemovedPositions);
        for (int i= 0, n= oldPositions.size(); i < n; i ++) {
            Object current= oldPositions.get(i);
            if (current != null)
                newPositions.add(current);
        }
        fRemovedPositions= newPositions;
    }
    
    private void removeColor(String colorKey) {
        if (_colorManager instanceof IColorManagerExtension)
            ((IColorManagerExtension) _colorManager).unbindColor(colorKey);
    }
    
    private void _addColor(String colorKey) {
        if (_colorManager != null && colorKey != null && _colorManager.getColor(colorKey) == null) {
            RGB rgb= PreferenceConverter.getColor(_preferenceStore, colorKey);
            if (_colorManager instanceof IColorManagerExtension) {
                IColorManagerExtension ext= (IColorManagerExtension) _colorManager;
                ext.unbindColor(colorKey);
                ext.bindColor(colorKey, rgb);
            }
        }
    }
    
    private void _initializeHighlightings() {
        _semanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
        _highlightings= new HighlightingStyle[_semanticHighlightings.length];
        
        for (int i= 0, n= _semanticHighlightings.length; i < n; i++) {
            SemanticHighlighting semanticHighlighting = _semanticHighlightings[i];
            String colorKey =
                SemanticHighlightings.getColorPreferenceKey(
                        semanticHighlighting);
            _addColor(colorKey);
            
            String boldKey= SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
            int style = _preferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;

            String italicKey= SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
            if (_preferenceStore.getBoolean(italicKey))
                style |= SWT.ITALIC;
            
            boolean isEnabled = _preferenceStore.getBoolean(
                    SemanticHighlightings.getEnabledPreferenceKey(
                            semanticHighlighting));

            _highlightings[i]= new HighlightingStyle(
                    new TextAttribute(
                            _colorManager.getColor(
                                    PreferenceConverter.getColor(_preferenceStore,
                                            colorKey)),
                            null, style), isEnabled);
        }
    }
    
    public boolean isEnabled() {
        return SemanticHighlightings.isEnabled(_preferenceStore);
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        handlePropertyChangeEvent(event);
    }

    private void handlePropertyChangeEvent(PropertyChangeEvent event) {
        if (_preferenceStore == null)
            return; // Uninstalled during event notification
        
        if (SemanticHighlightings.affectsEnablement(_preferenceStore, event)) {
            if (isEnabled()) {
                _enable();
                _scheduleJob();
            } else
                _disable();
        }
        
        if (!isEnabled())
            return;
        
        for (int i= 0, n= _semanticHighlightings.length; i < n; i++) {
            SemanticHighlighting semanticHighlighting= _semanticHighlightings[i];
            
            String colorKey= SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
            if (colorKey.equals(event.getProperty())) {
                adaptToTextForegroundChange(_highlightings[i], event);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }
            
            String boldKey= SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
            if (boldKey.equals(event.getProperty())) {
                adaptToTextStyleChange(_highlightings[i], event, SWT.BOLD);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }
            
            String italicKey= SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
            if (italicKey.equals(event.getProperty())) {
                adaptToTextStyleChange(_highlightings[i], event, SWT.ITALIC);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }
            
            String enabledKey= SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
            if (enabledKey.equals(event.getProperty())) {
                adaptToEnablementChange(_highlightings[i], event);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }
        }
    }
    
    private void adaptToEnablementChange(HighlightingStyle highlighting, PropertyChangeEvent event) {
        Object value= event.getNewValue();
        boolean eventValue;
        if (value instanceof Boolean)
            eventValue= ((Boolean) value).booleanValue();
        else if (IPreferenceStore.TRUE.equals(value))
            eventValue= true;
        else
            eventValue= false;
        highlighting.setEnabled(eventValue);
    }

    private void adaptToTextForegroundChange(HighlightingStyle highlighting, PropertyChangeEvent event) {
        RGB rgb= null;
        
        Object value= event.getNewValue();
        if (value instanceof RGB)
            rgb= (RGB) value;
        else if (value instanceof String)
            rgb= StringConverter.asRGB((String) value);
            
        if (rgb != null) {
            
            String property= event.getProperty();
            Color color= _colorManager.getColor(property);
            
            if ((color == null || !rgb.equals(color.getRGB())) && _colorManager instanceof IColorManagerExtension) {
                IColorManagerExtension ext= (IColorManagerExtension) _colorManager;
                ext.unbindColor(property);
                ext.bindColor(property, rgb);
                color= _colorManager.getColor(property);
            }
            
            TextAttribute oldAttr= highlighting.getTextAttribute();
            highlighting.setTextAttribute(new TextAttribute(color, oldAttr.getBackground(), oldAttr.getStyle()));
        }
    }
    
    private void adaptToTextStyleChange(HighlightingStyle highlighting, PropertyChangeEvent event, int styleAttribute) {
        boolean eventValue= false;
        Object value= event.getNewValue();
        if (value instanceof Boolean)
            eventValue= ((Boolean) value).booleanValue();
        else if (IPreferenceStore.TRUE.equals(value))
            eventValue= true;
        
        TextAttribute oldAttr= highlighting.getTextAttribute();
        boolean activeValue= (oldAttr.getStyle() & styleAttribute) == styleAttribute;
        
        if (activeValue != eventValue) 
            highlighting.setTextAttribute(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(), eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr.getStyle() & ~styleAttribute));
    }
    
    static class HighlightingStyle {
        
        /** Text attribute */
        private TextAttribute fTextAttribute;
        /** Enabled state */
        private boolean fIsEnabled;
        
        /**
         * Initialize with the given text attribute.
         * @param textAttribute The text attribute
         * @param isEnabled the enabled state
         */
        public HighlightingStyle(TextAttribute textAttribute, boolean isEnabled) {
            setTextAttribute(textAttribute);
            setEnabled(isEnabled);
        }
        
        /**
         * @return Returns the text attribute.
         */
        public TextAttribute getTextAttribute() {
            return fTextAttribute;
        }
        
        /**
         * @param textAttribute The background to set.
         */
        public void setTextAttribute(TextAttribute textAttribute) {
            fTextAttribute= textAttribute;
        }
        
        /**
         * @return the enabled state
         */
        public boolean isEnabled() {
            return fIsEnabled;
        }
        
        /**
         * @param isEnabled the new enabled state
         */
        public void setEnabled(boolean isEnabled) {
            fIsEnabled= isEnabled;
        }
    }

    static class HighlightedPosition extends Position {
        
        /** Highlighting of the position */
        private HighlightingStyle fStyle;
        
        /** Lock object */
        private Object fLock;
        
        /**
         * Initialize the styled positions with the given offset, length and foreground color.
         * 
         * @param offset The position offset
         * @param length The position length
         * @param highlighting The position's highlighting
         * @param lock The lock object
         */
        public HighlightedPosition(int offset, int length, HighlightingStyle highlighting, Object lock) {
            super(offset, length);
            fStyle= highlighting;
            fLock= lock;
        }
        
        /**
         * @return Returns a corresponding style range.
         */
        public StyleRange createStyleRange() {
            if (fStyle.isEnabled())
                return new StyleRange(getOffset(), getLength(), fStyle.getTextAttribute().getForeground(), fStyle.getTextAttribute().getBackground(), fStyle.getTextAttribute().getStyle());
            else
                return new StyleRange(getOffset(), 0, fStyle.getTextAttribute().getForeground(), fStyle.getTextAttribute().getBackground(), fStyle.getTextAttribute().getStyle());
        }
        
        /**
         * Uses reference equality for the highlighting.
         * 
         * @param off The offset
         * @param len The length
         * @param highlighting The highlighting
         * @return <code>true</code> iff the given offset, length and highlighting are equal to the internal ones.
         */
        public boolean isEqual(int off, int len, HighlightingStyle highlighting) {
            synchronized (fLock) {
                return !isDeleted() && getOffset() == off && getLength() == len && fStyle == highlighting;
            }
        }

        /**
         * Is this position contained in the given range (inclusive)? Synchronizes on position updater.
         * 
         * @param off The range offset
         * @param len The range length
         * @return <code>true</code> iff this position is not delete and contained in the given range.
         */
        public boolean isContained(int off, int len) {
            synchronized (fLock) {
                return !isDeleted() && off <= getOffset() && off + len >= getOffset() + getLength();
            }
        }

        public void update(int off, int len) {
            synchronized (fLock) {
                super.setOffset(off);
                super.setLength(len);
            }
        }
        
        /*
         * @see org.eclipse.jface.text.Position#setLength(int)
         */
        public void setLength(int length) {
            synchronized (fLock) {
                super.setLength(length);
            }
        }
        
        /*
         * @see org.eclipse.jface.text.Position#setOffset(int)
         */
        public void setOffset(int offset) {
            synchronized (fLock) {
                super.setOffset(offset);
            }
        }
        
        /*
         * @see org.eclipse.jface.text.Position#delete()
         */
        public void delete() {
            synchronized (fLock) {
                super.delete();
            }
        }
        
        /*
         * @see org.eclipse.jface.text.Position#undelete()
         */
        public void undelete() {
            synchronized (fLock) {
                super.undelete();
            }
        }
        
        /**
         * @return Returns the highlighting.
         */
        public HighlightingStyle getHighlighting() {
            return fStyle;
        }
    }
    
    class PositionCollector extends GenericVisitor {
        /** The semantic token */
        private SemanticToken fToken= new SemanticToken();
        
        /*
         * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visitNode(org.eclipse.jdt.core.dom.ASTNode)
         */
        protected boolean visitNode(ASTNode node) {
            if ((node.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
                retainPositions(node.getStartPosition(), node.getLength());
                return false; 
            }
            return true;
        }
        
        /*
         * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
         */
        public boolean visit(SimpleName node) {
            fToken.update(node);
            for (int i= 0, n= _jobSemanticHighlightings.length; i < n; i++) {
                SemanticHighlighting semanticHighlighting= _jobSemanticHighlightings[i];
                if (_jobHighlightings[i].isEnabled() && semanticHighlighting.consumes(fToken)) {
                    int offset= node.getStartPosition();
                    int length= node.getLength();
                    if (offset > -1 && length > 0)
                        addPosition(offset, length, _jobHighlightings[i]);
                    break;
                }
            }
            fToken.clear();
            return false;
        }
        
        /**
         * Add a position with the given range and highlighting iff it does not exist already.
         * @param offset The range offset
         * @param length The range length
         * @param highlighting The highlighting
         */
        private void addPosition(int offset, int length, HighlightingStyle highlighting) {
            boolean isExisting= false;
            // TODO: use binary search
            for (int i= 0, n= fRemovedPositions.size(); i < n; i++) {
                HighlightedPosition position= (HighlightedPosition) fRemovedPositions.get(i);
                if (position == null)
                    continue;
                if (position.isEqual(offset, length, highlighting)) {
                    isExisting= true;
                    fRemovedPositions.set(i, null);
                    fNOfRemovedPositions--;
                    break;
                }
            }

            if (!isExisting) {
                Position position = _jobPresenter.createHighlightedPosition(offset,
                        length, highlighting);
                fAddedPositions.add(position);
            }
        }

        /**
         * Retain the positions completely contained in the given range.
         * @param offset The range offset
         * @param length The range length
         */
        private void retainPositions(int offset, int length) {
            // TODO: use binary search
            for (int i= 0, n= fRemovedPositions.size(); i < n; i++) {
                HighlightedPosition position= (HighlightedPosition) fRemovedPositions.get(i);
                if (position != null && position.isContained(offset, length)) {
                    fRemovedPositions.set(i, null);
                    fNOfRemovedPositions--;
                }
            }
        }
    }
    
    /** Background job's added highlighted positions */
    private List fAddedPositions= new ArrayList();
    
    /** Background job's removed highlighted positions */
    private List fRemovedPositions= new ArrayList();
    
    /** Number of removed positions */
    private int fNOfRemovedPositions;
    
    private SemanticHighlighting[] _jobSemanticHighlightings;
    
    private SemanticHighlighting[] _semanticHighlightings;
    
    private HighlightingStyle[] _highlightings;
    
    private HighlightingStyle[] _jobHighlightings;
    
    private Job _job;

    private boolean _cancelJobs;

    private PtolemyEditor _editor;
    
    private Object _jobLock = new Object();
    
    private PositionCollector _collector = new PositionCollector();
    
    private IColorManager _colorManager;
    
    private IPreferenceStore _preferenceStore;
    
    private SemanticHighlightingPresenter _presenter;
    
    private SemanticHighlightingPresenter _jobPresenter;
    
    private JavaSourceViewerConfiguration _configuration;

    private JavaPresentationReconciler _presentationReconciler;
}
