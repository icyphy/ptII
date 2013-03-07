/* An event to list all the files matching a filter in a directory.

 Copyright (c) 2008-2013 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ListDirectory

/**
 An event to list all the files matching a filter in a directory. Each time it
 is fired, it updates its {@link #files} parameter to contain names of all the
 files (and/or directories) in the specified directory. A filter can be defined
 to filter the included file names.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ListDirectory extends Event {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public ListDirectory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        directory = new FileParameter(this, "directory");
        directory.setExpression(".");
        Parameter allowFiles = new Parameter(directory, "allowFiles");
        allowFiles.setTypeEquals(BaseType.BOOLEAN);
        allowFiles.setToken(BooleanToken.FALSE);
        Parameter allowDirectories = new Parameter(directory,
                "allowDirectories");
        allowDirectories.setTypeEquals(BaseType.BOOLEAN);
        allowDirectories.setToken(BooleanToken.TRUE);

        filter = new StringParameter(this, "filter");
        filter.setExpression("*.xml");

        includeFiles = new Parameter(this, "includeFiles");
        includeFiles.setTypeEquals(BaseType.BOOLEAN);
        includeFiles.setExpression("true");

        includeDirectories = new Parameter(this, "includeDirectories");
        includeDirectories.setTypeEquals(BaseType.BOOLEAN);
        includeDirectories.setExpression("false");

        recursive = new Parameter(this, "recursive");
        recursive.setTypeEquals(BaseType.BOOLEAN);
        recursive.setExpression("false");

        files = new Parameter(this, "files");
        files.setExpression("{ }");
        files.setVisibility(Settable.NOT_EDITABLE);
        files.setPersistent(false);
        Variable variable = new Variable(files, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);
    }

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #refire(Token, RefiringData)
     */
    public RefiringData fire(Token arguments) throws IllegalActionException {
        File[] listedFiles = RecursiveFileFilter.listFiles(directory.asFile(),
                ((BooleanToken) recursive.getToken()).booleanValue(),
                ((BooleanToken) includeFiles.getToken()).booleanValue(),
                ((BooleanToken) includeDirectories.getToken()).booleanValue(),
                filter.stringValue());
        StringBuffer buffer = new StringBuffer("{ ");
        int i = 0;
        for (File file : listedFiles) {
            if (i++ > 0) {
                buffer.append(",\n  ");
            }
            buffer.append('\"');
            buffer.append(StringUtilities.escapeString(file.getPath()));
            buffer.append('\"');
        }
        if (listedFiles.length > 0) {
            buffer.append(' ');
        }
        buffer.append('}');
        files.setExpression(buffer.toString());

        return super.fire(arguments);
    }

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.  If this entity is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *  @see #getContainer()
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container != null && directory != null) {
            directory.setBaseDirectory(URIAttribute.getModelURI(container));
        }
    }

    /** The director in which files are to be listed.
     */
    public FileParameter directory;

    /** A parameter with an ArrayToken containing strings that are names of the
     *  files matching the given filter in the specified directory. It is
     *  updated every time this event is fired.
     */
    public Parameter files;

    /** The filter that all the included file names must match.
     */
    public StringParameter filter;

    /** Whether directory names should be included.
     */
    public Parameter includeDirectories;

    /** Whether file names should be included.
     */
    public Parameter includeFiles;

    /** Whether files in the subdirectories should be searched as well.
     */
    public Parameter recursive;

    ///////////////////////////////////////////////////////////////////
    //// RecursiveFileFilter

    /**
     A file name filter that can recursively list files in a directory,
     including those in subdirectories. When a file name referring to a
     directory is found, this filter lists the files within that directory again
     with a new filter in this class.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class RecursiveFileFilter implements FilenameFilter {

        /** Construct a recursive file filter.
         *
         *  @param recursive Whether the filter should recursively list
         *   subdirectories.
         *  @param includeFiles Whether files should be included.
         *  @param includeDirectories Whether directories should be included.
         */
        public RecursiveFileFilter(boolean recursive, boolean includeFiles,
                boolean includeDirectories) {
            this(recursive, includeFiles, includeDirectories, null);
        }

        /** Construct a recursive file filter.
         *
         *  @param recursive Whether the filter should recursively list
         *   subdirectories.
         *  @param includeFiles Whether files should be included.
         *  @param includeDirectories Whether directories should be included.
         *  @param fileFilter The filter (with ? and * as wildcards) to filter
         *   the accepted file names.
         */
        public RecursiveFileFilter(boolean recursive, boolean includeFiles,
                boolean includeDirectories, String fileFilter) {
            _recursive = recursive;
            _includeFiles = includeFiles;
            _includeDirectories = includeDirectories;
            if (fileFilter != null && !fileFilter.equals("")) {
                _pattern = Pattern.compile(_escape(fileFilter));
            }
        }

        /** Return whether the file or directory name in the given directory is
         *  accepted.
         *
         *  @param dir The directory.
         *  @param name The file or directory name within the given directory.
         *  @return Whether the name is accepted.
         */
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

        /** Return the list of all files and directories after the filtering.
         *  This must be called after all the directories are traversed.
         *
         *  @return The list of files and directories.
         */
        public List<File> getFiles() {
            return _files;
        }

        /** List all the files and directories within the given directory.
         *
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
                boolean includeFiles, boolean includeDirectories,
                String fileFilter) {
            RecursiveFileFilter filter = new RecursiveFileFilter(recursive,
                    includeFiles, includeDirectories, fileFilter);
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
            return escaped.replaceAll("\\\\\\*", ".*").replaceAll("\\\\\\?",
                    ".?");
        }

        /** The pattern for the wildcard conversion.
         */
        private final Pattern _ESCAPER = Pattern.compile("([^a-zA-z0-9])");

        /** The list the recently found files and directories.
         */
        private List<File> _files = new LinkedList<File>();

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

        //////////////////////////////////////////////////////////////////////////
        //// FileComparator

        /**
         A comparator to sort file names.

         @author Thomas Huining Feng
         @version $Id$
         @since Ptolemy II 8.0
         @Pt.ProposedRating Yellow (tfeng)
         @Pt.AcceptedRating Red (tfeng)
         */
        private static class FileComparator implements Comparator<File>,
                Serializable {

            /** Compare two files with their names.
             *
             *  @param file1 The first file.
             *  @param file2 The second file.
             *  @return -1, 0 or 1 if file1 is less than, equal to or greater
             *   than file2.
             */
            public int compare(File file1, File file2) {
                return file1.getAbsolutePath().compareTo(
                        file2.getAbsolutePath());
            }
        }
    }
}
