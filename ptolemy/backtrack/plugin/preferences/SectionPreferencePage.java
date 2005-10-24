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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ptolemy.backtrack.plugin.EclipsePlugin;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SectionPreferencePage

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SectionPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {
    public SectionPreferencePage(String description) {
        super(GRID);
        setPreferenceStore(EclipsePlugin.getDefault().getPreferenceStore());
        setDescription(description);
    }

    protected void createFieldEditors() {
    }

    protected Composite _createSection(String text, String description) {
        Section section = _toolkit.createSection(_form.getBody(),
                Section.DESCRIPTION | Section.TWISTIE | Section.CLIENT_INDENT);
        TableWrapData data = new TableWrapData();
        data.grabHorizontal = true;
        section.setLayoutData(data);
        section.setBackground(null);
        section.setText(text);
        section.setDescription(description);
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                _form.reflow(false);
            }
        });

        Composite composite = _newComposite(section);
        section.setClient(composite);

        return composite;
    }

    public void init(IWorkbench workbench) {
    }

    protected Composite _getParent(FieldEditor editor) {
        return (Composite) _composites.get(editor);
    }

    protected void _setParent(FieldEditor editor, Composite parent) {
        _composites.put(editor, parent);
    }

    public Control createContents(Composite parent) {
        _toolkit = new FormToolkit(getShell().getDisplay());

        _form = _toolkit.createScrolledForm(parent);
        _form.setLayoutData(new GridData(GridData.FILL_BOTH));
        _form.setBackground(parent.getBackground());

        _form.getBody().setLayout(new TableWrapLayout());

        return parent;
    }

    protected void addField(FieldEditor editor) {
        _fields.add(editor);
        super.addField(editor);
    }

    protected List getFields() {
        return _fields;
    }

    protected Composite _newComposite(Composite parent) {
        return _newComposite(parent, 1);
    }

    protected Composite _newComposite(Composite parent, int column) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setBackground(null);

        GridLayout layout = new GridLayout(column, false);
        composite.setLayout(layout);

        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);
        return composite;
    }

    protected Group _newGroup(Composite parent, String text) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setBackground(null);

        GridLayout layout = new GridLayout(1, true);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(text);
        return group;
    }

    protected static void _setEnabled(Composite composite, boolean enabled) {
        composite.setEnabled(enabled);

        Control[] children = composite.getChildren();

        for (int i = 0; i < children.length; i++) {
            Control child = children[i];

            if (child instanceof Composite) {
                _setEnabled((Composite) child, enabled);
            }

            child.setEnabled(enabled);
        }
    }

    protected FormToolkit _toolkit;

    protected ScrolledForm _form;

    private Hashtable _composites = new Hashtable();

    private List _fields = new LinkedList();
}
