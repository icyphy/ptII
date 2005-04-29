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

package ptolemy.backtrack.plugin.util;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import ptolemy.backtrack.ast.Transformer;
import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.util.PathFinder;

//////////////////////////////////////////////////////////////////////////
//// Environment
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Environment {

    public static String getPtolemyHome() {
        return getPtolemyHome(null);
    }
    
    public static String getPtolemyHome(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String PTII = store.getString(PreferenceConstants.PTII);
        IPath path = Path.fromOSString(PTII);
        IContainer container = getContainer(path);
        PTII = container.getLocation().toOSString();
        
        boolean valid = true;
        if (PTII == null || PTII.equals(""))
            valid = false;
        
        if (!valid) {
            if (shell != null)
                MessageDialog.openError(shell,
                        "Ptolemy II Environment Error",
                        "Ptolemy home is invalid.\n" +
                        "Please set it in Ptolemy -> Options.");
            return null;
        } else
            return PTII;
    }
    
    public static String[] getClassPaths(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
            .getPreferenceStore();
        String PTII = getPtolemyHome(shell);
        if (PTII != null)
            return new String[]{PTII};
        else
            return null;
    }
    
    public static String[] combineArrays(String[] array1, String[] array2) {
        if (array1 == null)
            return array2;
        else if (array2 == null)
            return array1;
        
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length,
                array2.length);
        return result;
    }
    
    public static IPath getRefactoredFile(String source, String packageName) {
        File oldFile = new File(source);
        String simpleName = oldFile.getName();
        if (simpleName.toLowerCase().endsWith(".java"))
            simpleName = simpleName.substring(0, simpleName.length() - 5);

        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        String newPackage;
        if (packageName == null || packageName.equals(""))
            newPackage = prefix;
        else if (prefix == null || prefix.equals(""))
            newPackage = packageName;
        else
            newPackage = prefix + "." + packageName;
        
        String fullClassName =
            newPackage == null ?
                    simpleName :
                    newPackage + "." + simpleName;
        String fileName = fullClassName.replace(".", "" + File.separator) + ".java";
        
        String root =
            store.getString(PreferenceConstants.BACKTRACK_ROOT);
        IPath rootPath = Path.fromOSString(root);
        return rootPath.append(fileName);
    }
    
    public static IContainer getAffectedFolder() {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix =
            store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        IPath path = Path.fromOSString(
                store.getString(PreferenceConstants.BACKTRACK_ROOT));
        IContainer container = getContainer(path);
        if (prefix != null && !prefix.equals(""))
            container =
                container.getFolder(Path.fromOSString(
                        prefix.replace('.', File.separatorChar)));
        return container;
    }
    
    public static IContainer getContainer(IPath path) {
        IWorkspaceRoot root =
            ResourcesPlugin.getWorkspace().getRoot();
        String[] segments = path.segments();
        IContainer container =
            segments.length == 1 ?
                    (IContainer)root.getProject(segments[0]) :
                    segments.length > 1 ?
                            (IContainer)root.getFolder(path) :
                            null;
        return container;
    }
    
    public static void createFolders(IContainer container) throws CoreException {
        if (!container.exists()) {
            if (container instanceof IFolder) {
                createFolders(container.getParent());
                ((IFolder)container).create(true, false, null);
            }
        }
    }
    
    public static boolean setupTransformerArguments(Shell shell,
            boolean config, boolean alwaysOverwrite) {
        String PTII = Environment.getPtolemyHome(shell);
        if (PTII == null)
            return false;
        
        IPreferenceStore store = EclipsePlugin.getDefault()
            .getPreferenceStore();
        String prefix =
            store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        String root;
        try {
            IPath path = Path.fromOSString(
                    store.getString(PreferenceConstants.BACKTRACK_ROOT));
            IContainer container = getContainer(path);
            root = container.getLocation().toOSString();
        } catch (Exception e) {
            return false;
        }
        String sourceList =
            store.getString(PreferenceConstants.BACKTRACK_SOURCE_LIST);
        boolean overwrite = alwaysOverwrite ||
            store.getBoolean(PreferenceConstants.BACKTRACK_OVERWRITE);
        boolean generateConfiguration =
            store.getBoolean(PreferenceConstants.BACKTRACK_GENERATE_CONFIGURATION);
        String configuration =
            store.getString(PreferenceConstants.BACKTRACK_CONFIGURATION);
        
        String[] args = new String[]{
                "-prefix", prefix,
                "-output", root,
                overwrite ? "-overwrite" : "-nooverwrite"
        };
        if (config &&
                generateConfiguration &&
                configuration != null &&
                !configuration.equals("")) {
            String[] extraArgs = new String[] {
                    "-config", configuration
            };
            args = combineArrays(args, extraArgs);
        }
        
        PathFinder.setPtolemyPath(PTII);
        
        int start = 0;
        while (start < args.length) {
            int newPosition = Transformer.parseArguments(args, start);
            if (newPosition != start)
                start = newPosition;
            else
                break;
        }
        
        return true;
    }
}
