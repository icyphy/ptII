/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.io.File;

/**
 * It is nice if storage policies suggest good pathnames.  Usually the
 * file they last opened.  This abstract class provides such functionality.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
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
                if (home != null)
                    dir = home;
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



