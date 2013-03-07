/* The superclass for preference pages with multiple sections.

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
package ptolemy.backtrack.eclipse.plugin.preferences;

import java.util.Hashtable;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;

///////////////////////////////////////////////////////////////////
//// SectionPreferencePage

/**
 The superclass for preference pages with multiple sections.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SectionPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a preference page with multiple sections and with a page
     *  description.
     *
     *  @param description The description.
     */
    public SectionPreferencePage(String description) {
        super(GRID);
        setPreferenceStore(EclipsePlugin.getDefault().getPreferenceStore());
        setDescription(description);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the contents of the preference page with the parent as its
     *  container.
     *
     *  @param parent The parent container.
     *  @return The parent itself.
     */
    public Control createContents(Composite parent) {
        _toolkit = new FormToolkit(getShell().getDisplay());

        _form = _toolkit.createScrolledForm(parent);
        _form.setLayoutData(new GridData(GridData.FILL_BOTH));
        _form.setBackground(parent.getBackground());

        _form.getBody().setLayout(new TableWrapLayout());

        return parent;
    }

    /** Initialize. This method is inherited from the abstract superclass, and
     *  does nothing.
     *
     *  @param workbench The workbench.
     */
    public void init(IWorkbench workbench) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a section in this page with a title and a section description.
     *
     *  @param title The title.
     *  @param description The description.
     *  @return The container that can contain all the controls in the created
     *   section.
     */
    protected Composite _createSection(String title, String description) {
        Section section = _toolkit.createSection(_form.getBody(),
                Section.DESCRIPTION | ExpandableComposite.TWISTIE
                        | ExpandableComposite.CLIENT_INDENT);
        TableWrapData data = new TableWrapData();
        data.grabHorizontal = true;
        section.setLayoutData(data);
        section.setBackground(null);
        section.setText(title);
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

    /** Given a field editor, return its parent.
     *
     *  @param editor The field editor.
     *  @return The parent.
     *  @see #_setParent(FieldEditor, Composite)
     */
    protected Composite _getParent(FieldEditor editor) {
        return _composites.get(editor);
    }

    /** Create a new container with the given parent.
     *
     *  @param parent The parent container.
     *  @return The new container.
     */
    protected Composite _newComposite(Composite parent) {
        return _newComposite(parent, 1);
    }

    /** Create a new container with the given composite container as its parent,
     *  and use a grid layout with the specified number of columns.
     *
     *  @param parent The parent container.
     *  @param column The number of columns in the grid layout.
     *  @return The new container.
     */
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

    /** Create a new group with the given parent and the given title.
     *
     *  @param parent The parent container.
     *  @param title The title.
     *  @return The new group.
     */
    protected Group _newGroup(Composite parent, String title) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setBackground(null);

        GridLayout layout = new GridLayout(1, true);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(title);
        return group;
    }

    /** Set whether a container and all its children are enabled.
     *
     *  @param composite The container.
     *  @param enabled Whether the container and all its children are enabled.
     */
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

    /** Set the parent of a field editor.
     *
     *  @param editor The field editor.
     *  @param parent The parent.
     *  @see #_getParent(FieldEditor)
     */
    protected void _setParent(FieldEditor editor, Composite parent) {
        _composites.put(editor, parent);
    }

    /** Create field editors. This method is inherited from the abstract
     *  superclass, and does nothing.
     */
    protected void createFieldEditors() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The hash table that records the parent-children relation for all the
     *  sections. The keys are field editor objects; the values are their
     *  parents.
     */
    private Hashtable<FieldEditor, Composite> _composites = new Hashtable<FieldEditor, Composite>();

    /** The main form.
     */
    private ScrolledForm _form;

    /** The toolkit used to create the main form.
     */
    private FormToolkit _toolkit;
}
