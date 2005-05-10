package ptolemy.backtrack.plugin.preferences;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

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
            else
                store.setDefault(PreferenceConstants.BACKTRACK_SOURCE_LIST, "");
            
            store.setDefault(PreferenceConstants.BACKTRACK_ROOT,
                    ptIIProject.getFullPath().toOSString());
            
            store.setDefault(PreferenceConstants.BACKTRACK_GENERATE_CONFIGURATION,
                    true);
            
            IFile configuration = ptIIProject.getFile(
                    "ptolemy/backtrack/automatic/ptolemy/configs/output.xml");
            store.setDefault(PreferenceConstants.BACKTRACK_CONFIGURATION,
                    configuration.getLocation().toOSString());
        } else {
            store.setDefault(PreferenceConstants.PTII, "");
            
            store.setDefault(PreferenceConstants.BACKTRACK_SOURCE_LIST, "");
            
            store.setDefault(PreferenceConstants.BACKTRACK_ROOT, "");
            
            store.setDefault(PreferenceConstants.BACKTRACK_GENERATE_CONFIGURATION,
                    false);
            
            store.setDefault(PreferenceConstants.BACKTRACK_CONFIGURATION, "");
        }
        
        store.setDefault(PreferenceConstants.BACKTRACK_PREFIX,
                "ptolemy.backtrack.automatic");
        store.setDefault(PreferenceConstants.BACKTRACK_OVERWRITE, false);
        
        store.setDefault(PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED,
                true);
        
        PreferenceConverter.setDefault(store,
                PreferenceConstants.EDITOR_STATE_COLOR,
                new RGB(204, 40, 0));
        store.setDefault(PreferenceConstants.EDITOR_STATE_BOLD, false);
        store.setDefault(PreferenceConstants.EDITOR_STATE_ITALIC, false);
        
        PreferenceConverter.setDefault(store,
                PreferenceConstants.EDITOR_ACTOR_METHOD_COLOR,
                new RGB(255, 0, 0));
        store.setDefault(PreferenceConstants.EDITOR_ACTOR_METHOD_BOLD, true);
        store.setDefault(PreferenceConstants.EDITOR_ACTOR_METHOD_ITALIC,
                false);
	}

}
