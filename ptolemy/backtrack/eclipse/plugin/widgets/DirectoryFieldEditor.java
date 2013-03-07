/* A field editor for a directory.

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
package ptolemy.backtrack.eclipse.plugin.widgets;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import ptolemy.backtrack.eclipse.plugin.util.Environment;

///////////////////////////////////////////////////////////////////
//// DirectoryFieldEditor

/**
 A field editor for a directory.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DirectoryFieldEditor extends
        org.eclipse.jface.preference.DirectoryFieldEditor {

    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a directory field editor.
     *
     *  @param name The name of this editor.
     *  @param labelText The label.
     *  @param parent The parent.
     *  @param canBeEmpty Whether the file name can be left empty.
     */
    public DirectoryFieldEditor(String name, String labelText,
            Composite parent, boolean canBeEmpty) {
        super(name, labelText, parent);
        _canBeEmpty = canBeEmpty;
    }

    /** Construct a directory field editor with no name and no parent.
     *
     *  @param canBeEmpty Whether the file name can be left empty.
     */
    public DirectoryFieldEditor(boolean canBeEmpty) {
        _canBeEmpty = canBeEmpty;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set whether this directory field editor should only show the directories
     *  in the current workspace.
     *
     *  @param workspaceOnly Whether to show only the directories in the current
     *   workspace.
     */
    public void setWorkspaceOnly(boolean workspaceOnly) {
        _workspaceOnly = workspaceOnly;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Pop up the directory selection dialog after the change button is
     *  pressed, and retrieve the name of the selected directory.
     *
     *  @return The name of the selected directory, or null if the operation is
     *   canceled.
     */
    protected String changePressed() {
        String folderName = getTextControl().getText();
        IContainer container;

        if (folderName.equals("")) {
            container = ResourcesPlugin.getWorkspace().getRoot();
        } else {
            IPath path = new Path(folderName);
            container = Environment.getContainer(path);
        }

        if (_workspaceOnly) {
            ContainerSelectionDialog dialog = new ContainerSelectionDialog(
                    getShell(), container, false, "Select the Ptolemy home");

            if (dialog.open() == Window.OK) {
                IPath result = (IPath) dialog.getResult()[0];
                return result.toOSString();
            } else {
                return null;
            }
        } else {
            DirectoryDialog fileDialog = new DirectoryDialog(getShell(),
                    SWT.OPEN);
            fileDialog.setFilterPath(container.getLocation().toOSString());

            String dir = fileDialog.open();

            if (dir != null) {
                dir = dir.trim();

                if (dir.length() > 0) {
                    return dir;
                }
            }

            return null;
        }
    }

    /** Check whether the current state of this directory editor is valid.
     *
     *  @return true if the field editor's value is valid; false, otherwise.
     */
    protected boolean doCheckState() {
        String folderName = getTextControl().getText();

        if (_canBeEmpty && folderName.equals("")) {
            return true;
        }

        if (_workspaceOnly) {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IPath path = new Path(folderName);
            String[] segments = path.segments();

            if (segments.length == 1) {
                IProject project = root.getProject(segments[0]);
                return project.exists() && project.isOpen();
            } else if (segments.length > 1) {
                IFolder folder = root.getFolder(path);
                return folder.exists();
            } else {
                return false;
            }
        } else {
            File path = new File(folderName);
            return path.exists() && path.isDirectory();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Whether the field editor can be left empty.
     */
    private boolean _canBeEmpty = false;

    /** The allowed file extensions.
     */
    private boolean _workspaceOnly = true;
}
