package ptolemy.backtrack.plugin.preferences;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ptolemy.backtrack.plugin.EclipsePlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = EclipsePlugin.getDefault()
				.getPreferenceStore();
        
        IProject ptIIProject =
            ResourcesPlugin.getWorkspace().getRoot().getProject("ptII");
        if (ptIIProject.exists()) {
            store.setDefault(PreferenceConstants.PTII,
                    ptIIProject.getLocation().toOSString());
            
            IFile sourceList = ptIIProject.getFile(
                    "ptolemy/backtrack/automatic/source.lst");
            if (sourceList.exists())
                store.setDefault(PreferenceConstants.BACKTRACK_SOURCE_LIST,
                        sourceList.getLocation().toOSString());
            
            store.setDefault(PreferenceConstants.BACKTRACK_ROOT,
                    ptIIProject.getLocation().toOSString());
            
            store.setDefault(PreferenceConstants.BACKTRACK_PREFIX,
                    "ptolemy.backtrack.automatic");
        }
        
        store.setDefault(PreferenceConstants.BACKTRACK_OVERWRITE, false);
	}

}
