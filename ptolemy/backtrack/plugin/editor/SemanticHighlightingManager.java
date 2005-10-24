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

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightings;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;

import org.eclipse.jface.preference.IPreferenceStore;

//////////////////////////////////////////////////////////////////////////
//// SemanticHighlightingManager

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SemanticHighlightingManager {
    public boolean isEnabled() {
        return true;
    }

    public void install(JavaEditor editor, JavaSourceViewer sourceViewer,
            IColorManager colorManager, IPreferenceStore preferenceStore) {
        final String JAVA_PARTITIONING = "___java_partitioning";

        _editor = editor;
        _sourceViewer = sourceViewer;
        _colorManager = colorManager;
        _preferenceStore = preferenceStore;

        if (editor != null) {
            _configuration = new JavaSourceViewerConfiguration(colorManager,
                    preferenceStore, editor, JAVA_PARTITIONING);
            _presentationReconciler = (JavaPresentationReconciler) _configuration
                    .getPresentationReconciler(sourceViewer);
        } else {
            _configuration = null;
            _presentationReconciler = null;
        }

        //_preferenceStore.addPropertyChangeListener(this);
        if (isEnabled()) {
            enable();
        }
    }

    public void enable() {
        _initializeHighlightings();
    }

    protected void _initializeHighlightings() {
        _semanticHighlightings = SemanticHighlightings
                .getSemanticHighlightings();
    }

    private JavaEditor _editor;

    private JavaSourceViewer _sourceViewer;

    private IPreferenceStore _preferenceStore;

    private IColorManager _colorManager;

    private JavaSourceViewerConfiguration _configuration;

    private JavaPresentationReconciler _presentationReconciler;

    private SemanticHighlighting[] _semanticHighlightings;
}
