/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

//////////////////////////////////////////////////////////////////////////
//// RecursiveFileFilter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RecursiveFileFilter implements FilenameFilter {

    public RecursiveFileFilter(boolean recursive, boolean includeFiles,
            boolean includeDirectories) {
        this(recursive, includeFiles, includeDirectories, null);
    }

    public RecursiveFileFilter(boolean recursive, boolean includeFiles,
            boolean includeDirectories, String fileFilter) {
        _recursive = recursive;
        _includeFiles = includeFiles;
        _includeDirectories = includeDirectories;
        if (fileFilter != null && !fileFilter.equals("")) {
            _pattern = Pattern.compile(_escape(fileFilter));
        }
    }

    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        boolean isDirectory = file.isDirectory();
        boolean isFile = file.isFile();
        if (isFile && _includeFiles || isDirectory && _includeDirectories) {
            if (_pattern == null || _pattern.matcher(name).matches()) {
                _files.add(file);
            }
        }
        if (_recursive && isDirectory) {
            file.list(this);
        }
        return false;
    }

    public List<File> getFiles() {
        return _files;
    }

    public static File[] listFiles(File directory, boolean recursive,
            boolean includeFiles, boolean includeDirectories, String fileFilter) {
        RecursiveFileFilter filter = new RecursiveFileFilter(recursive,
                includeFiles, includeDirectories, fileFilter);
        directory.list(filter);
        List<File> files = filter.getFiles();
        Collections.sort(files, new FileComparator());
        return files.toArray(new File[files.size()]);
    }

    private String _escape(String string) {
        String escaped = _ESCAPER.matcher(string).replaceAll("\\\\$1");
        return escaped.replaceAll("\\\\\\*", ".*").replaceAll("\\\\\\?", ".?");
    }

    private final Pattern _ESCAPER = Pattern.compile("([^a-zA-z0-9])");

    private List<File> _files = new LinkedList<File>();

    private boolean _includeDirectories;

    private boolean _includeFiles;

    private Pattern _pattern;

    private boolean _recursive;

    private static class FileComparator implements Comparator<File>,
            Serializable {

        public int compare(File file1, File file2) {
            return file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
        }
    }

}
