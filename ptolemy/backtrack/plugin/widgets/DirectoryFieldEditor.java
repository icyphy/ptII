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

package ptolemy.backtrack.plugin.widgets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import ptolemy.backtrack.plugin.util.Environment;

//////////////////////////////////////////////////////////////////////////
//// DirectoryFieldEditor
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DirectoryFieldEditor extends
        org.eclipse.jface.preference.DirectoryFieldEditor {

    public DirectoryFieldEditor() {
    }
    
    public DirectoryFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }
    
    public boolean canBeEmpty() {
        return _canBeEmpty;
    }
    
    public void setCanBeEmpty(boolean canBeEmpty) {
        _canBeEmpty = canBeEmpty;
    }
    
    protected String changePressed() {
        String folderName = getTextControl().getText();
        IPath path = Path.fromOSString(folderName);
        IContainer container = Environment.getContainer(path);
        ContainerSelectionDialog dialog =
            new ContainerSelectionDialog(
                    getShell(), container, false,
                    "Select the Ptolemy home");
        
        if (dialog.open() == ContainerSelectionDialog.OK) {
            IPath result = (IPath)dialog.getResult()[0];
            return result.toOSString();
        } else
            return null;
    }
    
    protected boolean doCheckState() {
        String folderName = getTextControl().getText();
        
        if (_canBeEmpty && folderName.equals(""))
            return true;
        
        IWorkspaceRoot root =
            ResourcesPlugin.getWorkspace().getRoot();
        IPath path = Path.fromOSString(folderName);
        String[] segments = path.segments();
        if (segments.length == 1) {
            IProject project = root.getProject(segments[0]);
            return project.exists() && project.isOpen();
        } else if (segments.length > 1){
            IFolder folder =
                root.getFolder(path);
            return folder.exists();
        } else
            return false;
    }
    
    private boolean _canBeEmpty = false;
}
