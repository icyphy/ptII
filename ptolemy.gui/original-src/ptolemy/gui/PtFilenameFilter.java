/* A file filter that determines what files are displayed by the file dialog.

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

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

///////////////////////////////////////////////////////////////////
//// PtFilenameFilter

/** A file filter that determines what files are displayed by the
 * file dialog.
 *
 * <p>Note that the containing class can use either java.awt.FileDialog
 * or javax.swing.JFileChooser, so classes should extend
 * FilenameFilter, which can be used with either.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtFilenameFilter extends FileFilter implements FilenameFilter {
    /** Return true if the file is acceptable.
     *  @param file The file to be checked.
     *  @return true if the file is acceptable
     */
    @Override
    public boolean accept(File file) {
        // For FileFilter
        return true;
    }

    /** Return true if the file is acceptable.
     *  @param directory The directory that contains the file.
     *  @param name The name of the file.
     *  @return true if the file is acceptable.
     */
    @Override
    public boolean accept(File directory, String name) {
        // For FilenameFilter
        return true;
    }

    /** A description of this FilenameFilter.
     *  @return In this base class, return the string "All Files".
     */
    @Override
    public String getDescription() {
        // For FileFilter
        return "All Files";
    }
}
