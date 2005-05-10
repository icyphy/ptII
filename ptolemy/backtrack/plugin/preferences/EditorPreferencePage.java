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

package ptolemy.backtrack.plugin.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPreferencePage;

//////////////////////////////////////////////////////////////////////////
//// EditorPreferencePage
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EditorPreferencePage extends SectionPreferencePage
        implements IWorkbenchPreferencePage {
    
    public EditorPreferencePage() {
        super("Ptolemy II Java editor settings.");
    }

    public Control createContents(Composite parent) {
        super.createContents(parent);
        
        _createHighlightingSection();
        
        initialize();
        _checkEnabled();
        
        return parent;
    }
    
    private void _createHighlightingSection() {
        Composite composite = _createSection(
                "Ptolemy Semantic Highlighting",
                "Configure the semantic highlighting for Ptolemy source " +
                "files. The semantic highlighting, in addition to Java " +
                "semantic highlighting, colors different Ptolemy semantic " +
                "elements in the source files.");
        
        Composite currentComposite = _newComposite(composite);
        _highlightingEnabled = new BooleanFieldEditor(
                PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED,
                "&Use semantic highlighting",
                currentComposite) {
            protected void doLoadDefault() {
                super.doLoadDefault();
                _setEnabled(_colorGroup, getBooleanValue());
            }

            protected void valueChanged(boolean oldValue, boolean newValue) {
                super.valueChanged(oldValue, newValue);
                _setEnabled(_colorGroup, newValue);
            }
        };
        _setParent(_highlightingEnabled, currentComposite);
        addField(_highlightingEnabled);
        
        _colorGroup = _newGroup(composite, "Colors");
        
        Composite currentLine = _newComposite(_colorGroup, 3);
        
        currentComposite = _newComposite(currentLine);
        ColorFieldEditor color = new ColorFieldEditor(
                PreferenceConstants.EDITOR_STATE_COLOR,
                "&State variable color",
                currentComposite);
        addField(color);
        
        currentComposite = _newComposite(currentLine);
        BooleanFieldEditor bold = new BooleanFieldEditor(
                PreferenceConstants.EDITOR_STATE_BOLD,
                "Bold",
                currentComposite);
        addField(bold);
        
        currentComposite = _newComposite(currentLine);
        BooleanFieldEditor italic = new BooleanFieldEditor(
                PreferenceConstants.EDITOR_STATE_ITALIC,
                "Italic",
                currentComposite);
        addField(italic);
        
        currentComposite = _newComposite(currentLine);
        color = new ColorFieldEditor(
                PreferenceConstants.EDITOR_ACTOR_METHOD_COLOR,
                "&Actor method color",
                currentComposite);
        addField(color);
        
        currentComposite = _newComposite(currentLine);
        bold = new BooleanFieldEditor(
                PreferenceConstants.EDITOR_ACTOR_METHOD_BOLD,
                "Bold",
                currentComposite);
        addField(bold);
        
        currentComposite = _newComposite(currentLine);
        italic = new BooleanFieldEditor(
                PreferenceConstants.EDITOR_ACTOR_METHOD_ITALIC,
                "Italic",
                currentComposite);
        addField(italic);
    }
    
    private void _checkEnabled() {
        _colorGroup.setEnabled(
                _highlightingEnabled.getBooleanValue());
    }
    
    private BooleanFieldEditor _highlightingEnabled;
    
    private Group _colorGroup;
}
