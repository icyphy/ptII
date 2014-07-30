/* A JFileChooser or FileDialog.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptolemy.gui;

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// PtFileChooser

/**
 * A JFileChooser or FileDialog.
 *
 * <p>If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} returns
 * true, then a java.awt.FileDialog is used.  Otherwise a
 * javax.swing.JFileChooser is used.  In general Mac OS X is the only
 * platform that where useFileDialog() will return true.</p>
 *
 * <p> See {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} for how
 * to set a runtime Java property to control whether FileDialog or
 * JFileChooser is used.</p>
 *
 * <p> Note that this class should be wrapped in a try/finally block,
 * otherwise, under Windows, white boxes will appear in the common pane.
 * See {@link ptolemy.gui.JFileChooserBugFix}.  Below is an example: </p>
 * <pre>
 * // Swap backgrounds and avoid white boxes in "common places" dialog
 * JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
 * Color background = null;
 * PtFileChooser ptFileChooser;
 * try {
 *     ptFileChooser = new PtFileChooser(_basicGraphFrame, title,
 *              JFileChooser.OPEN_DIALOG);
 *     ptFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 *     ptFileChooser.setCurrentDirectory(modelDirectory);
 *     ptFileChooser.addChoosableFileFilter(new DirectoryFilter());
 *     int returnVal = ptFileChooser.showDialog(_basicGraphFrame, "Export HTML");
 *
 *     if (returnVal == JFileChooser.APPROVE_OPTION) {
 *         directory = ptFileChooser.getSelectedFile();
 *     }
 * } finally {
 *     try {
 *         if (ptFileChooser != null) {
 *             ptFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 *          }
 *     } finally {
 *          jFileChooserBugFix.restoreBackground(background);
 *     }
 * }
 * </pre>
 *
 * <p>Only a subset of the methods in JFileChooser are declared.
 * The method names follow the JFileChoose API because that API is more
 * common.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class PtFileChooser extends Container {
    // java.awt.Container is the common baseclass between
    // java.awt.FileDialog and javax.swing.JFileChooser
    // Maybe someday this should extend JFileChooser, but only if the
    // method implementations make sense when used with a FileDialog.

    /** Construct a PtFileChooser.
     *
     *  <p>If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()}
     *  returns true, then a java.awt.FileDialog is used.  Otherwise a
     *  javax.swing.JFileChooser is used.  The effect of the mode
     *  argument varies depending on which type of dialog is used.</p>
     *
     *  @param parent The parent component.  Used with FileDialog,
     *  ignored with JFileChooser.
     *  @param title The title of the dialog
     *  @param mode  JFileChooser.OPEN_DIALOG, JFileChooser.SAVE_DIALOG,
     *  or JFileChooser.CUSTOM_DIALOG.  CUSTOM_DIALOG is ignored by FileDialog.
     *  OPEN_DIALOG is a good default.
     */
    public PtFileChooser(Frame parent, String title, int mode) {
        _mode = mode;
        if (PtGUIUtilities.useFileDialog()) {
            _useFileDialog = true;
            if (mode == 2) {
                mode = FileDialog.LOAD;
            }
            _fileDialog = new FileDialog(parent, title, mode);
        } else {
            _jFileChooser = new JFileChooser();
            _jFileChooser.setDialogTitle(title);
            _jFileChooser.setDialogType(mode);
        }
    }

    /** Set the file name filter for the dialog.
     *  @param filter The FilenameFilter to be used.
     */
    public void addChoosableFileFilter(PtFilenameFilter filter) {
        if (_useFileDialog) {
            _fileDialog.setFilenameFilter(filter);
        } else {
            _jFileChooser.addChoosableFileFilter(filter);
        }
    }

    /** Return the current directory.
     *  @return The current directory.
     *  @see #setCurrentDirectory(File)
     */
    public File getCurrentDirectory() {
        if (_useFileDialog) {
            return new File(_fileDialog.getDirectory());
        } else {
            return _jFileChooser.getCurrentDirectory();
        }
    }

    /** Return the selected file as an absolute File (a File that is not relative).
     *  @return the selected file.
     *  @see #setSelectedFile(File)
     */
    public File getSelectedFile() {
        if (_useFileDialog) {
            return new File(_fileDialog.getDirectory(), _fileDialog.getFile());
        } else {
            return _jFileChooser.getSelectedFile();
        }
    }

    /** Set the current directory.
     *  If the directory parameter is null, then the value of the "user.dir"
     *  property is used.
     *  @param directory The current directory.
     *  @see #getCurrentDirectory()
     */
    public void setCurrentDirectory(File directory) {
        boolean fail = false;
        if (directory != null) {
            if (_useFileDialog) {
                try {
                    _fileDialog.setDirectory(directory.getCanonicalPath());
                } catch (IOException ex) {
                    fail = true;
                }
            } else {
                _jFileChooser.setCurrentDirectory(directory);
            }
        }
        if (fail || directory == null) {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // This will throw a security exception in an applet.
            // FIXME: we should support users under applets opening files
            // on the server.
            String currentWorkingDirectory = StringUtilities
                    .getProperty("user.dir");

            if (currentWorkingDirectory != null) {
                if (_useFileDialog) {
                    _fileDialog.setDirectory(currentWorkingDirectory);
                } else {
                    _jFileChooser.setCurrentDirectory(new File(
                            currentWorkingDirectory));
                }
            }
        }
    }

    /** Set the file selection mode.
     *
     *  <p>If FileDialog is being used, then
     *  DIRECTORIES_ONLY sets the apple.awt.fileDialogForDirectories parameter.
     *  See <a href="https://developer.apple.com/library/mac/#documentation/Java/Reference/Java_PropertiesRef/Articles/JavaSystemProperties.html">https://developer.apple.com/library/mac/#documentation/Java/Reference/Java_PropertiesRef/Articles/JavaSystemProperties.html</a>.</p>
     *
     *  <p>If this method is called with
     *  JFileChooser.DIRECTORIES_ONLY, then it should be called again
     *  with JFileChooser.FILES_AND_DIRECTORIES.  Typically, the first
     *  call is in a try block and the second is in a finally
     *  block.</p>
     *
     *  <p> Note that if FileDialog is used, and mode is
     *  DIRECTORIES_ONLY, then this class must have been instantiated
     *  with a mode of FileBrowser.LOAD or JFileChooser.OPEN_DIALOG in
     *  the constructor.</p>

     *  @param mode One of JFileChooser.FILES_ONLY, JFileChooser.DIRECTORIES_ONLY or
     *  JFileChooser.FILES_AND_DIRECTORIES.
     */
    public void setFileSelectionMode(int mode) {
        if (_useFileDialog) {
            if (mode == JFileChooser.DIRECTORIES_ONLY) {
                if (_mode != 0) {
                    if (!_printedDirectoryWarning) {
                        _printedDirectoryWarning = true;
                        System.out
                        .println("Warning: The PtFileChooser was instantiated with "
                                + "a mode other than 0, but setFileSelectionMode(DIRECTORIES_ONLY) "
                                + "was called.  This is likely to not work.");
                    }
                }
                // Mac Specific: allow the user to select a directory.
                // Note that apple.awt.fileDialogForDirectories only
                // works with FileDialog.LOAD, not FileDialog.SAVE.
                // See
                // https://developer.apple.com/library/mac/#documentation/Java/Reference/Java_PropertiesRef/Articles/JavaSystemProperties.html
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
            } else {
                System.setProperty("apple.awt.fileDialogForDirectories",
                        "false");
            }
        } else {
            _jFileChooser.setFileSelectionMode(mode);
        }
    }

    /** Set the selected file.
     *  @param file The file to be selected
     *  @see #getSelectedFile()
     */
    public void setSelectedFile(File file) {
        if (_useFileDialog) {
            _fileDialog.setFile(file.getName());
        } else {
            _jFileChooser.setSelectedFile(file);
        }
    }

    /** Show the dialog.
     *  @param parent Ignored with FileDialog, used with JFileChooser.
     *  @param approveButtonText The text for the approve button if JFileChooser is used.
     *  If FileDialog is used, then this argument is ignored.
     *  @return One of
     *  JFileChooser.CANCEL_OPTION, JFileChooser.APPROVE_OPTION
     *  JFileChooser.ERROR_OPTION is returned.  If FileDialog is used, then either CANCEL_OPTION
     *  or APPROVE_OPTION is returned.
     */
    public int showDialog(Container parent, String approveButtonText) {
        if (_useFileDialog) {
            _fileDialog.show();
            if (_fileDialog.getFile() == null) {
                return JFileChooser.CANCEL_OPTION;
            } else {
                return JFileChooser.APPROVE_OPTION;
            }
        } else {
            return _jFileChooser.showDialog(parent, approveButtonText);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The java.awt.FileDialog that is used if _useFileDialog is true. */
    private FileDialog _fileDialog;

    /** The javax.swing.JFileChooser that is used if _useFileDialog is false. */
    private JFileChooser _jFileChooser;

    /** The mode of the dialog.  One of
     *  JFileChooser.OPEN_DIALOG, JFileChooser.SAVE_DIALOG, JFileChooser.CUSTOM_DIALOG.
     *  CUSTOM_DIALOG is not supported with FileDialogs.
     */
    private int _mode;

    /** True if the directory warning was printed. */
    private static boolean _printedDirectoryWarning;

    /** True if PtGUIUtilities.useFileDialog() returned true. */
    private boolean _useFileDialog;

}
