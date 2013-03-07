/* The class to create the Ptolemy preference page.

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

import java.io.File;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;
import ptolemy.backtrack.eclipse.plugin.widgets.DirectoryFieldEditor;

///////////////////////////////////////////////////////////////////
//// PtolemyPreferencePage
/**
 The class to create the Ptolemy preference page.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PtolemyPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a Ptolemy preference page.
     */
    public PtolemyPreferencePage() {
        super(GRID);
        setPreferenceStore(EclipsePlugin.getDefault().getPreferenceStore());
        setDescription("General settings for Ptolemy II.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the contents in the Ptolemy preference. A field editor will be
     *  created for the path of Ptolemy home.
     */
    public void createFieldEditors() {
        Composite parent = getFieldEditorParent();

        DirectoryFieldEditor directoryFieldEditor = new DirectoryFieldEditor(
                PreferenceConstants.PTII, "&Ptolemy home:",
                getFieldEditorParent(), true) {
            protected void fireValueChanged(String property, Object oldValue,
                    Object newValue) {
                if ((property.equals(VALUE)) && isValid()) {
                    String PTII = getStringValue();
                    File sourceList = new File(PTII
                            + "/ptolemy/backtrack/automatic/source.lst");

                    if (sourceList.exists()) {
                        IPreferenceStore store = EclipsePlugin.getDefault()
                                .getPreferenceStore();
                        store.setValue(
                                PreferenceConstants.BACKTRACK_SOURCE_LIST,
                                sourceList.getPath());
                    }
                }

                super.fireValueChanged(property, oldValue, newValue);
            }
        };

        directoryFieldEditor.setWorkspaceOnly(false);
        addField(directoryFieldEditor);

        Label space = new Label(parent, 0);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        gridData.grabExcessVerticalSpace = true;
        space.setLayoutData(gridData);

        Label logo = new Label(parent, 0);
        gridData = new GridData();
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = SWT.CENTER;
        logo.setLayoutData(gridData);

        ImageDescriptor descriptor = EclipsePlugin
                .getImageDescriptor("ptolemy/backtrack/eclipse/plugin/images/ptolemy.gif");
        logo.setImage(descriptor.createImage());
    }

    /** Initialize. This method is inherited from the abstract superclass, and
     *  does nothing.
     *
     *  @param workbench The workbench.
     */
    public void init(IWorkbench workbench) {
    }
}
