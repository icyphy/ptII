/* A field editor for a file to be saved to.

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

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

///////////////////////////////////////////////////////////////////
//// SaveFileFieldEditor

/**
 A field editor for a file to be saved to.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SaveFileFieldEditor extends FileFieldEditor {

    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a save file field editor.
     *
     *  @param name The name of this editor.
     *  @param labelText The label.
     *  @param parent The parent.
     *  @param canBeEmpty Whether the file name can be left empty.
     */
    public SaveFileFieldEditor(String name, String labelText, Composite parent,
            boolean canBeEmpty) {
        super(name, labelText, parent);
        _canBeEmpty = canBeEmpty;
    }

    /** Construct a save file field editor with no name and no parent.
     *
     *  @param canBeEmpty Whether the file name can be left empty.
     */
    public SaveFileFieldEditor(boolean canBeEmpty) {
        _canBeEmpty = canBeEmpty;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the allowed extensions.
     *
     *  @param extensions The extensions.
     */
    public void setFileExtensions(String[] extensions) {
        _extensions = extensions;
        super.setFileExtensions(extensions);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Pop up the file selection dialog after the change button is pressed, and
     *  retrieve the name of the selected file.
     *
     *  @return The name of the selected file, or null if the operation is
     *   canceled.
     */
    protected String changePressed() {
        File file = new File(getTextControl().getText());

        if (!file.exists()) {
            file = null;
        }

        File newFile = _getFile(file);

        if (newFile == null) {
            return null;
        }

        return newFile.getAbsolutePath();
    }

    /** Check whether the current state of this field editor is valid.
     *
     *  @return true if the field editor's value is valid; false, otherwise.
     */
    protected boolean checkState() {
        String name = getTextControl().getText();

        if (_canBeEmpty && ((name == null) || name.equals(""))) {
            return true;
        }

        File file = new File(name);

        try {
            file.getCanonicalPath();
        } catch (Exception e) {
            return false;
        }

        return !file.isDirectory();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Pop up the file selection dialog, and return the file object of the
     *  selected file.
     *
     *  @param startingDirectory The directory that the file selection dialog
     *   shows initially.
     *  @return The file object of the selected file, or null if the dialog is
     *   canceled.
     */
    private File _getFile(File startingDirectory) {
        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);

        if (startingDirectory != null) {
            dialog.setFileName(startingDirectory.getPath());
        }

        if (_extensions != null) {
            dialog.setFilterExtensions(_extensions);
        }

        String file = dialog.open();

        if (file != null) {
            file = file.trim();

            if (file.length() > 0) {
                return new File(file);
            }
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** Whether the field editor can be left empty.
     */
    private boolean _canBeEmpty;

    /** The allowed file extensions.
     */
    private String[] _extensions;
}
