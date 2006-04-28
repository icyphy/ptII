/* Editor with Ptolemy syntax highlighting.

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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Composite;

import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;

//////////////////////////////////////////////////////////////////////////
//// PtolemyEditor

/**
   Editor with Ptolemy syntax highlighting. Ptolemy syntax highlighting is added
   to Eclipse's Java syntax highlighting as an extension.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class PtolemyEditor extends CompilationUnitEditor {
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Notify all the reconciling listeners before reconciliation is performed.
     */
    public void aboutToBeReconciled() {
        super.aboutToBeReconciled();

        Object[] listeners = _reconcilingListeners.getListeners();

        for (int i = 0, length = listeners.length; i < length; ++i) {
            ((IJavaReconcilingListener) listeners[i]).aboutToBeReconciled();
        }
    }

    /** Add a reconciling listener to the list of reconciling listeners to be
     *  notified.
     *  
     *  @param listener The reconciling listener to be added.
     */
    public void addJavaReconcileListener(IJavaReconcilingListener listener) {
        synchronized (_reconcilingListeners) {
            _reconcilingListeners.add(listener);
        }
    }

    /** Create the controls for this editor, and install the syntax highlighting
     *  handler.
     * 
     *  @param parent The parent of the editor.
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        _installSemanticHighlighting(JavaPlugin.getDefault().getJavaTextTools()
                .getColorManager());
    }

    /** Notify all the reconsiling listeners after reconciliation is performed.
     * 
     *  @param ast The compilation unit of the Java source.
     *  @param forced Whether the reconciliation is forced.
     *  @param progressMonitor The progress monitor to handle the reconcilation
     *   progress.
     */
    public void reconciled(CompilationUnit ast, boolean forced,
            IProgressMonitor progressMonitor) {
        super.reconciled(ast, forced, progressMonitor);

        // Notify listeners
        Object[] listeners = _reconcilingListeners.getListeners();

        for (int i = 0, length = listeners.length; i < length; ++i) {
            ((IJavaReconcilingListener) listeners[i]).reconciled(ast, forced,
                    progressMonitor);
        }
    }

    /** Remove a reconciling listener from the list of reconciling listeners
     *  to be notified.
     *  @param listener The reconciling listener to be removed.
     */
    public void removeJavaReconcileListener(IJavaReconcilingListener listener) {
        synchronized (_reconcilingListeners) {
            _reconcilingListeners.remove(listener);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Install the syntax highlighting handler.
     * 
     *  @param colorManager The color manager with the color settings.
     */
    protected void _installSemanticHighlighting(IColorManager colorManager) {
        IPreferenceStore preferenceStore = EclipsePlugin.getDefault()
                .getPreferenceStore();

        _reconciler = new SemanticHighlightingReconciler();
        _reconciler.install(this, colorManager);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The reconciler.
     */
    private SemanticHighlightingReconciler _reconciler;

    /** The list of reconciling listeners to be notified.
     */
    private ListenerList _reconcilingListeners = new ListenerList();
}
