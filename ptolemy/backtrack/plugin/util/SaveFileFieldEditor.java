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

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

//////////////////////////////////////////////////////////////////////////
//// SaveFileFieldEditor
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SaveFileFieldEditor extends FileFieldEditor {

    public SaveFileFieldEditor(boolean canBeEmpty) {
        _canBeEmpty = canBeEmpty;
    }
    
    public SaveFileFieldEditor(String name, String labelText, Composite parent,
            boolean canBeEmpty) {
        super(name, labelText, parent);
        _canBeEmpty = canBeEmpty;
    }
    
    protected boolean checkState() {
        String name = getTextControl().getText();
        
        if (_canBeEmpty && (name == null || name.equals("")))
            return true;
            
        File file = new File(name);
        try {
            file.getCanonicalPath();
        } catch (Exception e) {
            return false;
        }
        return !file.isDirectory();
    }
    
    protected String changePressed() {
        File file = new File(getTextControl().getText());
        if (!file.exists())
            file = null;
        File newFile = _getFile(file);
        if (newFile == null)
            return null;

        return newFile.getAbsolutePath();
    }
    
    private File _getFile(File startingDirectory) {

        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        if (startingDirectory != null)
            dialog.setFileName(startingDirectory.getPath());
        if (_extensions != null)
            dialog.setFilterExtensions(_extensions);
        String file = dialog.open();
        if (file != null) {
            file = file.trim();
            if (file.length() > 0)
                return new File(file);
        }

        return null;
    }
    
    public void setFileExtensions(String[] extensions) {
        _extensions = extensions;
        super.setFileExtensions(extensions);
    }
    
    private String[] _extensions;
    
    private boolean _canBeEmpty;
}
