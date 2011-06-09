/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.gui;

import java.io.File;

/**
 * It is nice if storage policies suggest good pathnames.  Usually the
 * file they last opened.  This abstract class provides such functionality.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public abstract class AbstractStoragePolicy implements StoragePolicy {
    private String _directory = null;

    /** Return a reasonable directory for file choosers to use.  If a
     *  directory has been set using setDirectory, then use that.
     *  Otherwise use the user's current working directory.
     *  If all else fails, then use user's home directory.  If THAT
     *  fails, then just dump them into the root of the file system.
     */
    public String getDirectory() {
        String dir = "";

        if (_directory != null) {
            dir = _directory;
        } else {
            String cwd = System.getProperty("user.dir");

            if (cwd != null) {
                dir = cwd;
            } else {
                String home = System.getProperty("user.home");

                if (home != null) {
                    dir = home;
                }
            }
        }

        return dir;
    }

    /** Set the current browsed directory to that given in the file.
     */
    public void setDirectory(File file) {
        if (file.exists() && file.isDirectory()) {
            _directory = file.getAbsolutePath();
        } else if (file.exists() && file.isFile()) {
            _directory = file.getParentFile().getAbsolutePath();
        }
    }

    /** Set the current browsed directory to that given directory.  The
     *  directory is assumed to exist.
     */
    public void setDirectory(String directory) {
        _directory = directory;
    }
}
