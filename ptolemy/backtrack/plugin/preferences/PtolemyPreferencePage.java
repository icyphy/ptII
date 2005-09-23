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

import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.widgets.DirectoryFieldEditor;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PtolemyPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PtolemyPreferencePage() {
		super(GRID);
		setPreferenceStore(EclipsePlugin.getDefault().getPreferenceStore());
		setDescription("General settings for Ptolemy II.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        
        DirectoryFieldEditor directoryFieldEditor =
            new DirectoryFieldEditor(PreferenceConstants.PTII, 
                "&Ptolemy home:", getFieldEditorParent()) {
            protected void fireValueChanged(String property, Object oldValue,
                    Object newValue) {
                if (property == VALUE && isValid()) {
                    String PTII = getStringValue();
                    File sourceList = new File(PTII +
                            "/ptolemy/backtrack/automatic/source.lst");
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
        directoryFieldEditor.setCanBeEmpty(true);
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
        ImageDescriptor descriptor =
            EclipsePlugin.getImageDescriptor(
                    "ptolemy/backtrack/plugin/images/ptolemy.gif");
        logo.setImage(descriptor.createImage());
	}

	public void init(IWorkbench workbench) {
	}
}
