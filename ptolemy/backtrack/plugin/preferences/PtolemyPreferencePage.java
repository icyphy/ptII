package ptolemy.backtrack.plugin.preferences;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
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