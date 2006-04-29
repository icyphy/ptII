/* Reconciler for Ptolemy semantic highlighting.

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
package ptolemy.backtrack.eclipse.plugin.editor;

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
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IColorManagerExtension;
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

import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;

import java.util.ArrayList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SemanticHighlightingReconciler

/**
   Reconciler for Ptolemy semantic highlighting.
   <p>
   This is the main class for Ptolemy semantic highlighting. It parses the Java
   source in the editor on-the-fly, and add Ptolemy semantic coloring to the
   presentation.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class SemanticHighlightingReconciler implements
        IJavaReconcilingListener, IPropertyChangeListener, ITextInputListener {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Prepare to reconcile the document. Nothing needs to be done in this
     *  method.
     */
    public void aboutToBeReconciled() {
        // Do nothing
    }

    /** Prepare for the change of the input document. This method is invoked
     *  before the document is changed.
     *  
     *  @param oldInput The old input document.
     *  @param newInput The new input document.
     */
    public void inputDocumentAboutToBeChanged(IDocument oldInput,
            IDocument newInput) {
        synchronized (_jobLock) {
            _cancelJobs = true;

            if (_job != null) {
                _job.cancel();
            }
        }
    }

    /** Handle the change of input document. This method is invoked after the
     *  document is changed.
     *  
     *  @param oldInput The old input document.
     *  @param newInput The new input document.
     */
    public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
        synchronized (_jobLock) {
            _cancelJobs = false;
        }

        if (newInput != null) {
            _scheduleJob();
        }
    }

    /** Install this reconciler to the editor, and enable it if it is set to be
     *  enabled.
     * 
     *  @param editor The editor.
     *  @param colorManager The color manager.
     */
    public void install(PtolemyEditor editor, IColorManager colorManager) {
        _editor = editor;
        _colorManager = colorManager;
        _preferenceStore = EclipsePlugin.getDefault().getPreferenceStore();
        _preferenceStore.addPropertyChangeListener(this);

        if (isEnabled()) {
            _enable();
        }
    }

    /** Return whether the semantic highlighting is enabled.
     * 
     *  @return true if the semantic highlighting is enabled.
     */
    public boolean isEnabled() {
        return SemanticHighlightings.isEnabled(_preferenceStore);
    }

    /** Handle the change of a property in the preference. This method is called
     *  if the user sets a preference in the preference dialog.
     * 
     *  @param event The property change event.
     */
    public void propertyChange(PropertyChangeEvent event) {
        _handlePropertyChangeEvent(event);
    }

    /** Perform operation after the reconcilation is finished.
     * 
     *  @param ast The compilation unit of the Java source.
     *  @param forced Whether the reconcilation is forced.
     *  @param progressMonitor The progress monitor.
     */
    public void reconciled(CompilationUnit ast, boolean forced,
            IProgressMonitor progressMonitor) {
        _jobPresenter = _presenter;
        _jobSemanticHighlightings = _semanticHighlightings;
        _jobHighlightings = _highlightings;

        if ((_jobPresenter == null) || (_jobSemanticHighlightings == null)
                || (_jobHighlightings == null)) {
            _jobPresenter = null;
            _jobSemanticHighlightings = null;
            _jobHighlightings = null;
            return;
        }

        _jobPresenter.setCanceled(progressMonitor.isCanceled());

        if ((ast == null) || _jobPresenter.isCanceled()) {
            return;
        }

        ASTNode[] subtrees = new ASTNode[] { ast };

        _startReconcilingPositions();

        if (!_jobPresenter.isCanceled()) {
            _reconcilePositions(subtrees);
        }

        TextPresentation textPresentation = null;

        if (!_jobPresenter.isCanceled()) {
            textPresentation = _jobPresenter.createPresentation(
                    _addedPositions, _removedPositions);
        }

        if (!_jobPresenter.isCanceled()) {
            _updatePresentation(textPresentation, _addedPositions,
                    _removedPositions);
        }

        _stopReconcilingPositions();

        _jobPresenter = null;
        _jobSemanticHighlightings = null;
        _jobHighlightings = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   protected inner classes                 ////

    //////////////////////////////////////////////////////////////////////////
    //// HighlightedPosition
    /**
       The position of a semantic highlighting.
    
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
    */
    protected static class HighlightedPosition extends Position {

        /** Initialize the styled positions with the given offset, length and
         *  foreground color.
         *
         *  @param offset The position offset.
         *  @param length The position length.
         *  @param highlighting The position's highlighting style.
         *  @param lock The lock object.
         */
        public HighlightedPosition(int offset, int length,
                HighlightingStyle highlighting, Object lock) {
            super(offset, length);
            _style = highlighting;
            _lock = lock;
        }

        /** Return a corresponding style range.
         * 
         *  @return The style range.
         */
        public StyleRange createStyleRange() {
            if (_style.isEnabled()) {
                return new StyleRange(getOffset(), getLength(), _style
                        .getTextAttribute().getForeground(), _style
                        .getTextAttribute().getBackground(), _style
                        .getTextAttribute().getStyle());
            } else {
                return new StyleRange(getOffset(), 0, _style.getTextAttribute()
                        .getForeground(), _style.getTextAttribute()
                        .getBackground(), _style.getTextAttribute().getStyle());
            }
        }

        /** Mark this position as deleted.
         * 
         *  @see #undelete()
         */
        public void delete() {
            synchronized (_lock) {
                super.delete();
            }
        }

        /** Return the highlighting style.
         * 
         *  @return The highlighting style.
         */
        public HighlightingStyle getHighlighting() {
            return _style;
        }

        /** Test whether this position is contained in the given range.
         *
         * @param offset The range offset.
         * @param length The range length.
         * @return true if this position is not delete and contained in the
         *  given range.
         */
        public boolean isContained(int offset, int length) {
            synchronized (_lock) {
                return !isDeleted() && (offset <= getOffset())
                        && ((offset + length) >= (getOffset() + getLength()));
            }
        }

        /** Test whether this highlighting's starting offset, length, and style
         *  is equal to the given parameters.
         *
         * @param offset The offset.
         * @param length The length.
         * @param highlighting The highlighting style.
         * @return true if the given offset, length and highlighting are equal
         *  to the internal ones.
         */
        public boolean isEqual(int offset, int length,
                HighlightingStyle highlighting) {
            synchronized (_lock) {
                return !isDeleted() && (getOffset() == offset)
                        && (getLength() == length) && (_style == highlighting);
            }
        }

        /** Set the length of this position.
         * 
         *  @param length The new length.
         */
        public void setLength(int length) {
            synchronized (_lock) {
                super.setLength(length);
            }
        }

        /** Set the offset of this position.
         * 
         *  @param offset The new offset.
         */
        public void setOffset(int offset) {
            synchronized (_lock) {
                super.setOffset(offset);
            }
        }

        /** Mark this position as not deleted.
         * 
         *  @see #delete()
         */
        public void undelete() {
            synchronized (_lock) {
                super.undelete();
            }
        }

        /** Update this position with the new offset and new length.
         * 
         *  @param offset The new offset.
         *  @param length The new length.
         */
        public void update(int offset, int length) {
            synchronized (_lock) {
                super.setOffset(offset);
                super.setLength(length);
            }
        }

        /** Highlighting of the position.
         */
        private HighlightingStyle _style;

        /** Lock object.
         */
        private Object _lock;
    }

    //////////////////////////////////////////////////////////////////////////
    //// HighlightingStyle
    /**
       The semantic highlighting style.
    
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
    */
    protected static class HighlightingStyle {

        /** Construct a highlighting style with the given text attribute.
         * 
         *  @param textAttribute The text attribute.
         *  @param isEnabled Whether this style is enabled.
         */
        public HighlightingStyle(TextAttribute textAttribute, boolean isEnabled) {
            setTextAttribute(textAttribute);
            setEnabled(isEnabled);
        }

        /** Return the text attribute.
         * 
         * @return The text attribute.
         * @see #setTextAttribute(TextAttribute)
         */
        public TextAttribute getTextAttribute() {
            return _textAttribute;
        }

        /** Return whether this style is enabled.
         * 
         *  @return true if this style is enabled.
         */
        public boolean isEnabled() {
            return _enabled;
        }

        /** Set whether this style is enabled.
         * 
         *  @param isEnabled true if this style is enabled.
         */
        public void setEnabled(boolean isEnabled) {
            _enabled = isEnabled;
        }

        /** Set the text attribute.
         * 
         *  @param textAttribute The text attribute.
         *  @see #getTextAttribute()
         */
        public void setTextAttribute(TextAttribute textAttribute) {
            _textAttribute = textAttribute;
        }

        /** Text attribute.
         */
        private TextAttribute _textAttribute;

        /** Enabled state.
         */
        private boolean _enabled;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    /** Adapt to a change on the enabled state.
     * 
     *  @param highlighting The highlighting style.
     *  @param event The event of the change.
     */
    private void _adaptToEnablementChange(HighlightingStyle highlighting,
            PropertyChangeEvent event) {
        Object value = event.getNewValue();
        boolean eventValue;

        if (value instanceof Boolean) {
            eventValue = ((Boolean) value).booleanValue();
        } else if (IPreferenceStore.TRUE.equals(value)) {
            eventValue = true;
        } else {
            eventValue = false;
        }

        highlighting.setEnabled(eventValue);
    }

    /** Adapt to a change on the text background.
     * 
     *  @param highlighting The highlighting style.
     *  @param event The event of the change.
     */
    private void _adaptToTextForegroundChange(HighlightingStyle highlighting,
            PropertyChangeEvent event) {
        RGB rgb = null;

        Object value = event.getNewValue();

        if (value instanceof RGB) {
            rgb = (RGB) value;
        } else if (value instanceof String) {
            rgb = StringConverter.asRGB((String) value);
        }

        if (rgb != null) {
            String property = event.getProperty();
            Color color = _colorManager.getColor(property);

            if (((color == null) || !rgb.equals(color.getRGB()))
                    && _colorManager instanceof IColorManagerExtension) {
                IColorManagerExtension ext = (IColorManagerExtension) _colorManager;
                ext.unbindColor(property);
                ext.bindColor(property, rgb);
                color = _colorManager.getColor(property);
            }

            TextAttribute oldAttr = highlighting.getTextAttribute();
            highlighting.setTextAttribute(new TextAttribute(color, oldAttr
                    .getBackground(), oldAttr.getStyle()));
        }
    }

    /** Adapt to a change on text style.
     * 
     *  @param highlighting The highlighting style.
     *  @param event The event of the change.
     */
    private void _adaptToTextStyleChange(HighlightingStyle highlighting,
            PropertyChangeEvent event, int styleAttribute) {
        boolean eventValue = false;
        Object value = event.getNewValue();

        if (value instanceof Boolean) {
            eventValue = ((Boolean) value).booleanValue();
        } else if (IPreferenceStore.TRUE.equals(value)) {
            eventValue = true;
        }

        TextAttribute oldAttr = highlighting.getTextAttribute();
        boolean activeValue = (oldAttr.getStyle() & styleAttribute) == styleAttribute;

        if (activeValue != eventValue) {
            highlighting.setTextAttribute(new TextAttribute(oldAttr
                    .getForeground(), oldAttr.getBackground(),
                    eventValue ? (oldAttr.getStyle() | styleAttribute)
                            : (oldAttr.getStyle() & ~styleAttribute)));
        }
    }

    /** Add a color with the given key.
     * 
     *  @param colorKey The color key.
     *  @see #_removeColor(String)
     */
    private void _addColor(String colorKey) {
        if ((_colorManager != null) && (colorKey != null)
                && (_colorManager.getColor(colorKey) == null)) {
            RGB rgb = PreferenceConverter.getColor(_preferenceStore, colorKey);

            if (_colorManager instanceof IColorManagerExtension) {
                IColorManagerExtension ext = (IColorManagerExtension) _colorManager;
                ext.unbindColor(colorKey);
                ext.bindColor(colorKey, rgb);
            }
        }
    }

    /** Disable the semantic highlightings.
     * 
     *  @see #_enable()
     */
    private void _disable() {
        if (_presenter != null) {
            _presenter.setCanceled(true);
        }

        if (_presenter != null) {
            _presenter.uninstall();
            _presenter = null;
        }

        _editor.removeJavaReconcileListener(this);

        if (_semanticHighlightings != null) {
            _disposeHighlightings();
        }
    }

    /** Dispose the resources used by the semantic highlightings.
     */
    private void _disposeHighlightings() {
        for (int i = 0, n = _semanticHighlightings.length; i < n; i++) {
            _removeColor(SemanticHighlightings
                    .getColorPreferenceKey(_semanticHighlightings[i]));
        }

        _semanticHighlightings = null;
        _highlightings = null;
    }

    /** Enable the semantic highlightings.
     *  
     *  @see #_disable()
     */
    private void _enable() {
        _initializeHighlightings();

        final String JAVA_PARTITIONING = "___java_partitioning";
        _configuration = new JavaSourceViewerConfiguration(_colorManager,
                _preferenceStore, _editor, JAVA_PARTITIONING);
        _presentationReconciler = (JavaPresentationReconciler) _configuration
                .getPresentationReconciler(_editor.getViewer());

        _presenter = new SemanticHighlightingPresenter();
        _presenter.install((JavaSourceViewer) _editor.getViewer(),
                _presentationReconciler);

        _jobSemanticHighlightings = _semanticHighlightings;

        _editor.addJavaReconcileListener(this);
    }

    /** Handle a property change evant.
     * 
     *  @param event The event of the change.
     *  @see #_adaptToEnablementChange(ptolemy.backtrack.eclipse.plugin.editor.SemanticHighlightingReconciler.HighlightingStyle, PropertyChangeEvent)
     *  @see #_adaptToTextForegroundChange(ptolemy.backtrack.eclipse.plugin.editor.SemanticHighlightingReconciler.HighlightingStyle, PropertyChangeEvent)
     *  @see #_adaptToTextStyleChange(ptolemy.backtrack.eclipse.plugin.editor.SemanticHighlightingReconciler.HighlightingStyle, PropertyChangeEvent, int)
     */
    private void _handlePropertyChangeEvent(PropertyChangeEvent event) {
        if (_preferenceStore == null) {
            return; // Uninstalled during event notification
        }

        if (SemanticHighlightings.affectsEnablement(_preferenceStore, event)) {
            if (isEnabled()) {
                _enable();
                _scheduleJob();
            } else {
                _disable();
            }
        }

        if (!isEnabled()) {
            return;
        }

        for (int i = 0, n = _semanticHighlightings.length; i < n; i++) {
            SemanticHighlighting semanticHighlighting = _semanticHighlightings[i];

            String colorKey = SemanticHighlightings
                    .getColorPreferenceKey(semanticHighlighting);

            if (colorKey.equals(event.getProperty())) {
                _adaptToTextForegroundChange(_highlightings[i], event);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }

            String boldKey = SemanticHighlightings
                    .getBoldPreferenceKey(semanticHighlighting);

            if (boldKey.equals(event.getProperty())) {
                _adaptToTextStyleChange(_highlightings[i], event, SWT.BOLD);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }

            String italicKey = SemanticHighlightings
                    .getItalicPreferenceKey(semanticHighlighting);

            if (italicKey.equals(event.getProperty())) {
                _adaptToTextStyleChange(_highlightings[i], event, SWT.ITALIC);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }

            String enabledKey = SemanticHighlightings
                    .getEnabledPreferenceKey(semanticHighlighting);

            if (enabledKey.equals(event.getProperty())) {
                _adaptToEnablementChange(_highlightings[i], event);
                _presenter.highlightingStyleChanged(_highlightings[i]);
                continue;
            }
        }
    }

    /** Initialize semantic highlightings.
     */
    private void _initializeHighlightings() {
        _semanticHighlightings = SemanticHighlightings
                .getSemanticHighlightings();
        _highlightings = new HighlightingStyle[_semanticHighlightings.length];

        for (int i = 0, n = _semanticHighlightings.length; i < n; i++) {
            SemanticHighlighting semanticHighlighting = _semanticHighlightings[i];
            String colorKey = SemanticHighlightings
                    .getColorPreferenceKey(semanticHighlighting);
            _addColor(colorKey);

            String boldKey = SemanticHighlightings
                    .getBoldPreferenceKey(semanticHighlighting);
            int style = _preferenceStore.getBoolean(boldKey) ? SWT.BOLD
                    : SWT.NORMAL;

            String italicKey = SemanticHighlightings
                    .getItalicPreferenceKey(semanticHighlighting);

            if (_preferenceStore.getBoolean(italicKey)) {
                style |= SWT.ITALIC;
            }

            boolean isEnabled = _preferenceStore
                    .getBoolean(SemanticHighlightings
                            .getEnabledPreferenceKey(semanticHighlighting));

            _highlightings[i] = new HighlightingStyle(new TextAttribute(
                    _colorManager.getColor(PreferenceConverter.getColor(
                            _preferenceStore, colorKey)), null, style),
                    isEnabled);
        }
    }

    /** Reconcile the positions of the AST subtrees.
     * 
     *  @param subtrees The subtrees to be reconciled.
     */
    private void _reconcilePositions(ASTNode[] subtrees) {
        for (int i = 0, n = subtrees.length; i < n; i++) {
            subtrees[i].accept(_collector);
        }

        List oldPositions = _removedPositions;
        List newPositions = new ArrayList(_removedPositionsNumber);

        for (int i = 0, n = oldPositions.size(); i < n; i++) {
            Object current = oldPositions.get(i);

            if (current != null) {
                newPositions.add(current);
            }
        }

        _removedPositions = newPositions;
    }

    /** Remove a color with the given key.
     * 
     *  @param colorKey The color key.
     *  @see #_addColor(String)
     */
    private void _removeColor(String colorKey) {
        if (_colorManager instanceof IColorManagerExtension) {
            ((IColorManagerExtension) _colorManager).unbindColor(colorKey);
        }
    }

    /** Schedule the reconcilation job in the background.
     */
    private void _scheduleJob() {
        final IJavaElement element = _editor.getInputJavaElement();

        if (element != null) {
            Job job = new Job("PtolemySemanticHighlighting.job") {
                protected IStatus run(IProgressMonitor monitor) {
                    synchronized (_jobLock) {
                        if (_job != null) {
                            try {
                                _jobLock.wait();
                            } catch (InterruptedException e) {
                                JavaPlugin.log(e);
                            }
                        }

                        if (_cancelJobs || (_job != null)) {
                            return Status.CANCEL_STATUS;
                        }

                        _job = this;
                    }

                    CompilationUnit ast = JavaPlugin.getDefault()
                            .getASTProvider().getAST(element, true, monitor);
                    reconciled(ast, false, monitor);

                    synchronized (_jobLock) {
                        _job = null;
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

    /** Start reconciling the positions.
     * 
     *  @see #_stopReconcilingPositions()
     */
    private void _startReconcilingPositions() {
        _jobPresenter.addAllPositions(_removedPositions);
        _removedPositionsNumber = _removedPositions.size();
    }

    /** Stop reconciling the positions.
     * 
     *  @see #_startReconcilingPositions()
     */
    private void _stopReconcilingPositions() {
        _removedPositions.clear();
        _removedPositionsNumber = 0;
        _addedPositions.clear();
    }

    /** Update the text presentation with semantic highlightings.
     * 
     *  @param textPresentation The text presentation.
     *  @param addedPositions The add positions.
     *  @param removedPositions The removed positions.
     *  @see SemanticHighlightingPresenter
     */
    private void _updatePresentation(TextPresentation textPresentation,
            List addedPositions, List removedPositions) {
        Runnable runnable = _presenter.createUpdateRunnable(textPresentation,
                addedPositions, removedPositions);

        if (runnable == null) {
            return;
        }

        JavaEditor editor = _editor;

        if (editor == null) {
            return;
        }

        IWorkbenchPartSite site = editor.getSite();

        if (site == null) {
            return;
        }

        Shell shell = site.getShell();

        if ((shell == null) || shell.isDisposed()) {
            return;
        }

        Display display = shell.getDisplay();

        if ((display == null) || display.isDisposed()) {
            return;
        }

        display.asyncExec(runnable);
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private inner classes                  ////

    //////////////////////////////////////////////////////////////////////////
    //// PositionCollector
    /**
       The AST visitor to collect positions to be reconciled.
    
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
    */
    private class PositionCollector extends GenericVisitor {

        /** Visit a simple name in the AST and record its position if it is
         *  part of the semantic highlighting.
         *  
         *  @param node The simple name node.
         *  @return Always false because the simple name node has no children to
         *   visit.
         */
        public boolean visit(SimpleName node) {
            _token.update(node);

            for (int i = 0, n = _jobSemanticHighlightings.length; i < n; i++) {
                SemanticHighlighting semanticHighlighting = _jobSemanticHighlightings[i];

                if (_jobHighlightings[i].isEnabled()
                        && semanticHighlighting.consumes(_token)) {
                    int offset = node.getStartPosition();
                    int length = node.getLength();

                    if ((offset > -1) && (length > 0)) {
                        _addPosition(offset, length, _jobHighlightings[i]);
                    }

                    break;
                }
            }

            _token.clear();
            return false;
        }

        /** Visit an AST node, and retain its offset and length if it is
         *  malformed.
         * 
         *  @param node The AST node.
         *  @return false if the node is malformed; true, otherwise. 
         */
        protected boolean visitNode(ASTNode node) {
            if ((node.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
                _retainPositions(node.getStartPosition(), node.getLength());
                return false;
            }

            return true;
        }

        /** Add a position with the given range and highlighting if it does not
         *  exist already.
         *  
         *  @param offset The range offset.
         *  @param length The range length.
         *  @param highlighting The highlighting style.
         */
        private void _addPosition(int offset, int length,
                HighlightingStyle highlighting) {
            boolean isExisting = false;

            // TODO: use binary search
            for (int i = 0, n = _removedPositions.size(); i < n; i++) {
                HighlightedPosition position =
                    (HighlightedPosition) _removedPositions.get(i);

                if (position == null) {
                    continue;
                }

                if (position.isEqual(offset, length, highlighting)) {
                    isExisting = true;
                    _removedPositions.set(i, null);
                    _removedPositionsNumber--;
                    break;
                }
            }

            if (!isExisting) {
                Position position = _jobPresenter.createHighlightedPosition(
                        offset, length, highlighting);
                _addedPositions.add(position);
            }
        }

        /** Retain the positions completely contained in the given range.
         * 
         *  @param offset The range offset.
         *  @param length The range length.
         */
        private void _retainPositions(int offset, int length) {
            // TODO: use binary search
            for (int i = 0, n = _removedPositions.size(); i < n; i++) {
                HighlightedPosition position =
                    (HighlightedPosition) _removedPositions.get(i);

                if ((position != null) && position.isContained(offset, length)) {
                    _removedPositions.set(i, null);
                    _removedPositionsNumber--;
                }
            }
        }

        /** The semantic token.
         */
        private SemanticToken _token = new SemanticToken();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The highlighted positions added by the background job.
     */
    private List _addedPositions = new ArrayList();

    /** Whether the background job is canceled.
     */
    private boolean _cancelJobs;

    /** The position collector.
     */
    private PositionCollector _collector = new PositionCollector();

    /** The color manager.
     */
    private IColorManager _colorManager;

    /** The Java source viewer configuration.
     */
    private JavaSourceViewerConfiguration _configuration;

    /** The editor whose content to be reconciled.
     */
    private PtolemyEditor _editor;

    /** The highlighting styles.
     */
    private HighlightingStyle[] _highlightings;

    /** The background job to reconcile highlightings.
     */
    private Job _job;

    /** The highlighting styles that the background job is working on, or null.
     */
    private HighlightingStyle[] _jobHighlightings;

    /** The lock for the background job.
     */
    private Object _jobLock = new Object();

    /** The highlighting presenter that the background job is using.
     */
    private SemanticHighlightingPresenter _jobPresenter;

    /** The semantic highlightings that the background job is working on, or
     *  null.
     */
    private SemanticHighlighting[] _jobSemanticHighlightings;

    /** The preference store.
     */
    private IPreferenceStore _preferenceStore;

    /** The Java presentation reconciler.
     */
    private JavaPresentationReconciler _presentationReconciler;

    /** The highlighting presenter.
     */
    private SemanticHighlightingPresenter _presenter;

    /** The highlighted positions removed by the background job.
     */
    private List _removedPositions = new ArrayList();

    /** Number of the removed positions.
     */
    private int _removedPositionsNumber;

    /** The semantic highlightings.
     */
    private SemanticHighlighting[] _semanticHighlightings;
}
