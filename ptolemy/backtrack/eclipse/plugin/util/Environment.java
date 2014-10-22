/* The class containing a set of static methods for the transformation
 environment.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.util;
import java.util.Locale;

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

import ptolemy.backtrack.eclipse.ast.Transformer;
import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;
import ptolemy.backtrack.eclipse.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.util.PathFinder;
import ptolemy.backtrack.util.Strings;

//////////////////////////////////////////////////////////////////////////
//// Environment

/**
 The class containing a set of static methods for the transformation
 environment.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Environment {

    /** Create the folder represented by the container object, and all its
     *  parent folders if necessary.
     *
     *  @param container The container representing the folder to be created.
     *  @exception CoreException If the folder creation fails.
     */
    public static void createFolders(IContainer container) throws CoreException {
        if (!container.exists()) {
            if (container instanceof IFolder) {
                createFolders(container.getParent());
                ((IFolder) container).create(true, true, null);
            }
        }
    }

    /** Get the folder where all the transformed classes are placed. It is
     *  a sub-folder rooted at the backtracking root (can be set in the
     *  preference page). The sub-path is appended to the root according to the
     *  backtracking prefix.
     *
     *  @return The container representing the folder where all the transformed
     *   classes are placed.
     */
    public static IContainer getAffectedFolder() {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        IPath path = new Path(store
                .getString(PreferenceConstants.BACKTRACK_ROOT));
        IContainer container = getContainer(path);

        if ((prefix != null) && !prefix.equals("")) {
            container = container.getFolder(new Path(prefix.replace('.',
                    File.separatorChar)));
        }

        return container;
    }

    /** Get the default class paths.
     *
     *  @param shell The shell object.
     *  @return The array of class paths, or null if Ptolemy is not found.
     */
    public static String[] getClassPaths(Shell shell) {
        // IPreferenceStore store = EclipsePlugin.getDefault()
        //         .getPreferenceStore();
        String PTII = getPtolemyHome(shell);

        if (PTII != null) {
            return new String[] { PTII };
        } else {
            return null;
        }
    }

    /** Given a path, return its container representation in Eclipse.
     *
     *  @param path The path.
     *  @return The container, or null if the path is invalid.
     */
    public static IContainer getContainer(IPath path) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String[] segments = path.segments();
        IContainer container = (segments.length == 1) ? (IContainer) root
                .getProject(segments[0])
                : ((segments.length > 1) ? (IContainer) root.getFolder(path)
                        : null);
        return container;
    }

    /** Get the Ptolemy home.
     *
     *  @return The Ptolemy home, or null if Ptolemy is not found.
     */
    public static String getPtolemyHome() {
        return getPtolemyHome(null);
    }

    /** Get the Ptolemy home. If Ptolemy cannot be found, an error message is
     *  popped up in the given shell.
     *
     *  @param shell The shell.
     *  @return The Ptolemy home, or null if Ptolemy is not found.
     */
    public static String getPtolemyHome(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String PTII = store.getString(PreferenceConstants.PTII);

        boolean valid = !PTII.equals("");

        if (!valid) {
            if (shell != null) {
                MessageDialog.openError(shell, "Ptolemy II Environment Error",
                        "Ptolemy home is invalid.\n"
                                + "Please set it in Ptolemy -> Options.");
            }

            return null;
        } else {
            return PTII;
        }
    }

    /** Given the source name of a Java file and its package name, return the
     *  path to the transformed file with backtracking prefix added to it.
     *
     *  @param source The Java source file.
     *  @param packageName The package name.
     *  @return The path to the transformed file.
     */
    public static IPath getRefactoredFile(String source, String packageName) {
        File oldFile = new File(source);
        String simpleName = oldFile.getName();

        if (simpleName.toLowerCase(Locale.getDefault()).endsWith(".java")) {
            simpleName = simpleName.substring(0, simpleName.length() - 5);
        }

        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        String newPackage;

        if ((packageName == null) || packageName.equals("")) {
            newPackage = prefix;
        } else if ((prefix == null) || prefix.equals("")) {
            newPackage = packageName;
        } else {
            newPackage = prefix + "." + packageName;
        }

        String fullClassName = (newPackage == null) ? simpleName : (newPackage
                + "." + simpleName);
        String fileName = fullClassName.replace('.', File.separatorChar)
                + ".java";

        String root = store.getString(PreferenceConstants.BACKTRACK_ROOT);
        IPath rootPath = new Path(root);
        return rootPath.append(fileName);
    }

    /** Get the root for the source transformer. This root can be set in the
     *  preference page.
     *
     *  @return The root, or null if the root is invalid.
     */
    public static String getRefactoringRoot() {
        return getRefactoringRoot(null);
    }

    /** Get the root for the source transformer. This root can be set in the
     *  preference page. If the root is invalid, an error message is popped up
     *  in the given shell.
     *
     *  @param shell The shell.
     *  @return The root, or null if the root is invalid.
     */
    public static String getRefactoringRoot(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String root = store.getString(PreferenceConstants.BACKTRACK_ROOT);

        boolean valid = !root.equals("");

        if (!valid) {
            if (shell != null) {
                MessageDialog.openError(shell, "Ptolemy II Environment Error",
                        "Refactoring root is invalid.\n"
                                + "Please set it in Ptolemy -> Options.");
            }

            return null;
        } else {
            IPath rootPath = new Path(root);
            String[] segments = rootPath.segments();

            if (segments.length == 1) {
                root = ResourcesPlugin.getWorkspace().getRoot().getProject(
                        segments[0]).getLocation().toOSString();
            } else {
                root = ResourcesPlugin.getWorkspace().getRoot().getFolder(
                        rootPath).getLocation().toOSString();
            }

            return root;
        }
    }

    /** Get the file name of the refactoring source list.
     *
     *  @return The file name of the refactoring source list, or null if the
     *   source list is invalid.
     */
    public static String getSourceList() {
        return getSourceList(null);
    }

    /** Get the file name of the refactoring source list. If the source list is
     *  invalid, an error message is popped up in the given shell.
     *
     *  @param shell The shell.
     *  @return The file name of the refactoring source list.
     */
    public static String getSourceList(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String sourceList = store
                .getString(PreferenceConstants.BACKTRACK_SOURCE_LIST);

        boolean valid = !sourceList.equals("");

        if (!valid) {
            if (shell != null) {
                MessageDialog.openError(shell, "Ptolemy II Environment Error",
                        "Refactored source list is invalid.\n"
                                + "Please set it in Ptolemy -> Options.");
            }

            return null;
        } else {
            return sourceList;
        }
    }

    /** Set up the transformation arguments according to the preferences set in
     *  the preference page. Static fields of the transformer are initialized
     *  with these arguments.
     *
     *  @param shell The shell.
     *  @param config Whether to generate MoML configuration.
     *  @param alwaysOverwrite Whether to overwrite existing files.
     *  @return false if the Refactoring root cannot be found, true
     *  otherwise.
     */
    public static boolean setupTransformerArguments(Shell shell,
            boolean config, boolean alwaysOverwrite) {
        // It is OK if PTII is not set.
        String PTII = Environment.getPtolemyHome(null);

        String root = getRefactoringRoot(shell);

        if (root == null) {
            return false;
        }

        //String sourceList = getSourceList(shell);
        //if (sourceList == null)
        //    return false;
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        boolean overwrite = alwaysOverwrite
                || store.getBoolean(PreferenceConstants.BACKTRACK_OVERWRITE);
        boolean generateConfiguration = store
                .getBoolean(PreferenceConstants.BACKTRACK_GENERATE_CONFIGURATION);
        String configuration = store
                .getString(PreferenceConstants.BACKTRACK_CONFIGURATION);

        String[] args = new String[] { "-prefix", prefix, "-output", root,
                overwrite ? "-overwrite" : "-nooverwrite" };

        if (config && generateConfiguration && (configuration != null)
                && !configuration.equals("")) {
            String[] extraArgs = new String[] { "-config", configuration };
            args = Strings.combineArrays(args, extraArgs);
        }

        if (PTII != null) {
            PathFinder.setPtolemyPath(PTII);
        } else {
            PathFinder.setPtolemyPath("");
        }

        int start = 0;

        while (start < args.length) {
            int newPosition = Transformer.parseArguments(args, start);

            if (newPosition != start) {
                start = newPosition;
            } else {
                break;
            }
        }

        return true;
    }
}
