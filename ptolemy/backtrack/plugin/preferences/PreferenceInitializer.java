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

import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.about.AboutBundleGroupData;

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
        
        IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
        LinkedList groups = new LinkedList();
        boolean enableHighlighting = true;
        if (providers != null)
            for (int i = 0; i < providers.length; ++i) {
                IBundleGroup[] bundleGroups = providers[i].getBundleGroups();
                for (int j = 0; j < bundleGroups.length; ++j)
                    if (bundleGroups[j].getIdentifier().equals("org.eclipse.jdt")) {
                        if (bundleGroups[j].getVersion().compareTo("3.1") < 0) {
                            enableHighlighting = false;
                            break;
                        }
                    }
            }
        store.setDefault(PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED,
                enableHighlighting);
        
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
