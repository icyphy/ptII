/* An actor that produces an array that lists the contents of a directory.

   @Copyright (c) 2003-2014 The Regents of the University of California.
   All rights reserved.

   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the
   above copyright notice and the following two paragraphs appear in all
   copies of this software.

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

   PT_COPYRIGHT_VERSION 2
   COPYRIGHTENDKEY
 */
package ptolemy.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

///////////////////////////////////////////////////////////////////
//// RecursiveFileFilter

/**
   A file name filter that can recursively list files in a directory,
   including those in subdirectories. When a file name referring to a
   directory is found, this filter lists the files within that directory again
   with a new filter in this class.

   @author Thomas Huining Feng, Christopher Brooks
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (tfeng)
   @Pt.AcceptedRating Red (tfeng)
 */
public class RecursiveFileFilter implements FilenameFilter {

    /** Construct a recursive file filter.
     *
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     */
    public RecursiveFileFilter(boolean recursive, boolean includeFiles,
            boolean includeDirectories) {
        this(recursive, includeFiles, includeDirectories, false, false, null);
    }

    /** Construct a recursive file filter.  This method has four
     *  parameters to control whether files and directories are
     *  included.  Not all combinations make sense, but are required
     *  because this code was refactored from two classes that had
     *  similar functionality.
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     *  @param filesOnly Whether only files should be included
     *  @param directoriesOnly Whether only directories should be included.
     *  @param fileFilter The filter (with ? and * as wildcards) to filter
     *   the accepted file names.
     */
    public RecursiveFileFilter(boolean recursive, boolean includeFiles,
            boolean includeDirectories, boolean filesOnly,
            boolean directoriesOnly, String fileFilter) {
        this(recursive, includeFiles, includeDirectories, false, false, null,
                false);
    }

    /** Construct a recursive file filter.  This method has four
     *  parameters to control whether files and directories are
     *  included.  Not all combinations make sense, but are required
     *  because this code was refactored from two classes that had
     *  similar functionality.
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     *  @param filesOnly Whether only files should be included
     *  @param directoriesOnly Whether only directories should be included.
     *  @param fileFilter The filter (with ? and * as wildcards) to filter
     *   the accepted file names.
     *  @param escape True if a string with ? and * as wildcards is to
     *  be converted into a Java regular expression.  The DirectoryListing
     *  actor calls this with a false value.
     */
    public RecursiveFileFilter(boolean recursive, boolean includeFiles,
            boolean includeDirectories, boolean filesOnly,
            boolean directoriesOnly, String fileFilter, boolean escape) {
        _recursive = recursive;
        _includeFiles = includeFiles;
        _includeDirectories = includeDirectories;
        _filesOnly = filesOnly;
        _directoriesOnly = directoriesOnly;
        if (fileFilter != null && !fileFilter.equals("")) {
            if (escape) {
                // gt style, but gt calls listFiles()
                _pattern = Pattern.compile(_escape(fileFilter));
            } else {
                // DirectoryListin style, but DirectoryListing calls listFiles()
                _pattern = Pattern.compile(fileFilter);
            }
        } else {
            // Empty pattern for glob, which is .*
            _pattern = Pattern.compile(".*");
        }

    }

    /** Return whether the file or directory name in the given directory is
     *  accepted.
     *
     *  @param dir The directory.  If directory is null, then it is
     *  likely that this method is being called to accept a URL and no
     *  File object is instantiated.  If directory is not null, then a
     *  File object is instantiated.
     *  @param name The file or directory name within the given directory.
     *  @return Whether the name is accepted.
     */
    @Override
    public boolean accept(File dir, String name) {
        File file = null;
        boolean isDirectory = false;
        boolean isFile = false;
        if (dir != null) {
            file = new File(dir, name);
            isDirectory = file.isDirectory();
            isFile = file.isFile();
        } else {
            // Could be a URL.
            file = new File(name);
            if (name.endsWith("/")) {
                isDirectory = true;
            } else {
                isFile = true;
            }
        }
        if (_match(isDirectory, isFile, name, file)) {
            return true;
        }

        // file will be null if we are trying to accept a URL.
        if (file != null && _recursive && isDirectory) {
            file.list(this);
        }
        return false;
    }

    /** Return the list of all files and directories after the filtering.
     *  This must be called after all the directories are traversed.
     *
     *  @return The list of files and directories.
     */
    public List<File> getFiles() {
        return _files;
    }

    /** List all the files and directories within the given directory.
     *  This method has four parameters to control whether
     *  files and directories are included.  Not all combinations make sense.
     *  @param directory The directory.
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     *  @param fileFilter The filter (with ? and * as wildcards) to filter
     *   the accepted file names.
     *  @return The array of all the files and directories found.
     */
    public static File[] listFiles(File directory, boolean recursive,
            boolean includeFiles, boolean includeDirectories, String fileFilter) {
        // gt uses this method.
        return RecursiveFileFilter.listFiles(directory, recursive,
                includeFiles, includeDirectories, false, false, fileFilter,
                true);
    }

    /** List all the files and directories within the given directory.
     *  This method has four parameters to control whether
     *  files and directories are included.  Not all combinations make sense.
     *  @param directory The directory.
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     *  @param fileFilter The filter (with ? and * as wildcards) to filter
     *   the accepted file names.
     *  @param escape True if a string with ? and * as wildcards is to
     *  be converted into a Java regular expression.  The DirectoryListing
     *  actor calls this with a false value.
     *  @return The array of all the files and directories found.
     */
    public static File[] listFiles(File directory, boolean recursive,
            boolean includeFiles, boolean includeDirectories,
            String fileFilter, boolean escape) {
        // DirectoryListing uses this method.
        return RecursiveFileFilter.listFiles(directory, recursive,
                includeFiles, includeDirectories, false, false, fileFilter,
                escape);
    }

    /** List all the files and directories within the given directory.
     *  This method has four parameters to control whether
     *  files and directories are included.  Not all combinations make sense.
     *  @param directory The directory.
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     *  @param filesOnly Whether only files should be included
     *  @param directoriesOnly Whether only directories should be included.
     *  @param fileFilter The filter (with ? and * as wildcards) to filter
     *   the accepted file names.
     *  @return The array of all the files and directories found.
     */
    public static File[] listFiles(File directory, boolean recursive,
            boolean includeFiles, boolean includeDirectories,
            boolean filesOnly, boolean directoriesOnly, String fileFilter) {
        return RecursiveFileFilter.listFiles(directory, recursive,
                includeFiles, includeDirectories, filesOnly, directoriesOnly,
                fileFilter, true);

    }

    /** List all the files and directories within the given directory.
     *  This method has four parameters to control whether
     *  files and directories are included.  Not all combinations make sense.
     *  @param directory The directory.
     *  @param recursive Whether the filter should recursively list
     *   subdirectories.
     *  @param includeFiles Whether files should be included.
     *  @param includeDirectories Whether directories should be included.
     *  @param filesOnly Whether only files should be included
     *  @param directoriesOnly Whether only directories should be included.
     *  @param fileFilter The filter (with ? and * as wildcards) to filter
     *   the accepted file names.
     *  @param escape True if a string with ? and * as wildcards is to
     *  be converted into a Java regular expression.  The DirectoryListing
     *  actor calls this with a false value.
     *  @return The array of all the files and directories found.
     */
    public static File[] listFiles(File directory, boolean recursive,
            boolean includeFiles, boolean includeDirectories,
            boolean filesOnly, boolean directoriesOnly, String fileFilter,
            boolean escape) {
        RecursiveFileFilter filter = new RecursiveFileFilter(recursive,
                includeFiles, includeDirectories, filesOnly, directoriesOnly,
                fileFilter, escape);
        directory.list(filter);
        List<File> files = filter.getFiles();
        Collections.sort(files, new FileComparator());
        return files.toArray(new File[files.size()]);
    }

    /** Convert a string with ? and * as wildcards into a Java regular
     *  expression.
     *
     *  @param string The string with file name wildcards.
     *  @return A regular expression.
     */
    private String _escape(String string) {
        String escaped = _ESCAPER.matcher(string).replaceAll("\\\\$1");
        return escaped.replaceAll("\\\\\\*", ".*").replaceAll("\\\\\\?", ".?");
    }

    /** Return true if there is a match.
     *  @param isDirectory True if the name is a directory.
     *  @param isFile True if the name is a file
     *  @param name The name to check.
     *  @param file The file to be added if there is a match.
     */
    private boolean _match(boolean isDirectory, boolean isFile, String name, File file) {
        if (((!_directoriesOnly && !_filesOnly) && ((isFile && _includeFiles) || (isDirectory && _includeDirectories)))
                || ((_filesOnly && isFile) || (_directoriesOnly && isDirectory) || (!_directoriesOnly && !_filesOnly))) {
            // ptolemy/domains/sdf/test/auto/filePortParameter.xml wants match.matches() here.
            // ptolemy/actor/lib/test/auto/ExecRunDemos.xml wants match.find() here

            // Avoid a NPE if the pattern is empty.
            // See ptolemy/actor/lib/io/test/auto/DirectoryListingEmptyPattern.xml
            // and https://projects.ecoinformatics.org/ecoinfo/issues/6233
            if (_pattern != null) {
                Matcher match = _pattern.matcher(name);
                if (match.matches() || match.find()) {
                    _files.add(file);
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /** The pattern for the wildcard conversion.
     */
    private final Pattern _ESCAPER = Pattern.compile("([^a-zA-z0-9])");

    /** Whether only directories should be included.
     */
    private boolean _directoriesOnly;

    /** The list the recently found files and directories.
     */
    private List<File> _files = new LinkedList<File>();

    /** Whether only files should be included.
     */
    private boolean _filesOnly;

    /** Whether directories should be included.
     */
    private boolean _includeDirectories;

    /** Whether files should be included.
     */
    private boolean _includeFiles;

    /** The pattern for the filter.
     */
    private Pattern _pattern;

    /** Whether the filter should recursively list subdirectories.
     */
    private boolean _recursive;

    ///////////////////////////////////////////////////////////////////
    //// FileComparator

    /**
       A comparator to sort file names.

       @author Thomas Huining Feng
       @version $Id$
       @since Ptolemy II 10.0
       @Pt.ProposedRating Yellow (tfeng)
       @Pt.AcceptedRating Red (tfeng)
     */
    private static class FileComparator implements Comparator<File> {

        /** Compare two files with their names.
         *
         *  @param file1 The first file.
         *  @param file2 The second file.
         *  @return -1, 0 or 1 if file1 is less than, equal to or greater
         *   than file2.
         */
        @Override
        public int compare(File file1, File file2) {
            return file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
        }
    }
}
