/* A parameter that specifies a file or URL.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import ptolemy.kernel.attributes.FileOrURLAccessor;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// FileParameter

/**
 <p>This is an attribute that specifies a file or URL.  The value of this
 attribute, accessed by getExpression(), is a string that names a file
 or URL, possibly containing references to variables defined in scope
 using the syntax $ID, ${ID} or $(ID). The value returned by getToken()
 is name of the file with such references resolved.</p>

 <p>If this attribute contains a parameter named <i>allowFiles</i> with
 value false, then when a file browser is used to select a file,
 that file browser will be set to not show files (only directories
 will be shown).  If this attribute contains a parameter named
 <i>allowDirectories</i> with value true, then the file browser
 will permit the user to select directories (the default behavior
 is that when a directory is selected, that directory is opened
 and its contained files and directories are listed).</p>

 <p>If the model containing this
 attribute has been saved to a MoML file, then the file name can be
 given relative to the directory containing that MoML file.
 If the model has not been saved to a file,
 then the classpath is used for identifying relative file names.</p>

 <p>Files can be given relative to a <i>base</i>, where the base is
 the URI of the first container above this one that has a URIAttribute.
 Normally, this URI specifies the file or URL containing the model
 definition. Thus, files that are referred to here can be kept in the
 same directory as the model, or in a related directory, and can
 moved together with the model.</p>


 <p>The following special file names are understood:</p>
 <ul>
 <li> System.in: Standard input.
 <li> System.out: Standard output.
 </ul>

 <p> Note, however, that these file names cannot be converted to URLs
 using the asURL() method.
 A file name can also contain the following strings that start
 with "$", which get substituted
 with the appropriate values.</p>
 <table>
 <caption>Predefined values</caption>
 <tr>
 <th>String</th>
 <th>Description</th>
 <th>Property</th>
 </tr>
 <tr>
 <td><code>$CWD</code></td>
 <td>The current working directory</td>
 <td><code>user.dir</code></td>
 </tr>
 <tr>
 <td><code>$HOME</code></td>
 <td>The user's home directory</td>
 <td><code>user.home</code></td>
 </tr>
 <tr>
 <td><code>$PTII</code></td>
 <td>The home directory of the Ptolemy II installation</td>
 <td><code>ptolemy.ptII.dir</code></td>
 </tr>
 <tr>
 <td><code>$TMPDIR</code></td>
 <td>The temporary directory</td>
 <td><code>java.io.tmpdir</code></td>
 </tr>
 <tr>
 <td><code>$USERNAME</code></td>
 <td>The user's account name</td>
 <td><code>user.name</code></td>
 </tr>
 </table>
 <p>The above properties are normally set when a Ptolemy II application starts.</p>
 <p>
 If a file name begins with the reference "$CLASSPATH", then when
 the file is opened for reading, the openForReading() method
 will search for the file relative to the classpath (using the
 getResource() method of the current class loader).  This will only
 work for a file that exists, and thus the openForWriting() method
 will not understand the "$CLASSPATH" string; this makes sense
 since the classpath typically has several directories, and it
 would not be obvious where to create the file.  The asURL()
 method also recognizes the "$CLASSPATH" string, but not the asFile()
 method (which is typically used when accessing a file for writing).
 NOTE: If the container of this parameter also contains a variable
 named CLASSPATH, then the value of that variable is used instead
 of the Java classpath.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (cxh)
 @see URIAttribute
 */
public class FileParameter extends StringParameter implements FileOrURLAccessor {
    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FileParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, false);
    }

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param isOutput Whether the file is to be written to.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FileParameter(NamedObj container, String name, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _isOutput = isOutput;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the file as a File object.  This method first attempts
     *  to directly use the file name to construct the File. If the
     *  resulting File is not absolute, then it attempts to resolve it
     *  relative to the base directory returned by getBaseDirectory().
     *  If there is no such base URI, then it simply returns the
     *  relative File object.
     *  <p>
     *  The file need not exist for this method to succeed.  Thus,
     *  this method can be used to determine whether a file with a given
     *  name exists, prior to calling openForWriting().
     *  A typical usage looks like this:
     *  <pre>
     *      FileParameter fileParameter;
     *      ...
     *      File file = fileParameter.asFile();
     *      if (file.exists()) {
     *         ... Ask the user if it's OK to overwrite...
     *         ... Throw an exception if not...
     *      }
     *      // The following will overwrite an existing file.
     *      Writer writer = new PrintWriter(fileParameter.openForWriting());
     *  </pre>
     *  <p>If the name begins with "$CLASSPATH",
     *  then search for the file relative to the classpath.
     *  If the name begins with $CLASSPATH and a file is not found
     *  in the CLASSPATH, then the value of $PTII is substituted
     *  in a returned.  If this is not done, then the File that is
     *  created would have $CLASSPATH or xxxxxxCLASSPATHxxxxxx in
     *  the file pathname, which is unlikely to be useful.
     *
     *  @return A File, or null if no file name has been specified.
     *  @see #getBaseDirectory()
     *  @exception IllegalActionException If a parse error occurs
     *   reading the file name.
     */
    @Override
    public File asFile() throws IllegalActionException {
        String name = stringValue();

        try {
            return FileUtilities.nameToFile(name, getBaseDirectory());
        } catch (IllegalArgumentException ex) {
            // Java 1.4.2 some times reports:
            //  java.lang.IllegalArgumentException: URI is not absolute
            throw new IllegalActionException(this, ex,
                    "Failed to create a file with name '" + name + "'.");
        }
    }

    /** Return the file as a URL.  If the file name is relative, then
     *  it is interpreted as being relative to the directory returned
     *  by getBaseDirectory(). If the name begins with "$CLASSPATH",
     *  then search for the file relative to the classpath.
     *  If no file is found, then it throws an exception.
     *  @return A URL, or null if no file name or URL has been specified.
     *  @exception IllegalActionException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in), or
     *   the name specification cannot be parsed.
     */
    @Override
    public URL asURL() throws IllegalActionException {
        String name = stringValue();

        try {
            return FileUtilities.nameToURL(name, getBaseDirectory(), getClass()
                    .getClassLoader());
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "Cannot read file '"
                    + name + "'");
        }
    }

    /** Clone the attribute into the specified workspace.  The resulting
     *  object has no base directory name nor any reference to any open stream.
     *  @param workspace The workspace for the new object.
     *  @return A new attribute.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FileParameter newObject = (FileParameter) super.clone(workspace);
        newObject._baseDirectory = null;
        newObject._reader = null;
        newObject._writer = null;
        return newObject;
    }

    /** Close the file. If it has not been opened using openForReading()
     *  or openForWriting(), then do nothing.  Also, if the file is
     *  System.in or System.out, then do not close it (it does not make
     *  sense to close these files).
     *  @exception IllegalActionException If the file or URL cannot be
     *   closed.
     */
    @Override
    public void close() throws IllegalActionException {
        if (_reader != null) {
            if (_reader != FileUtilities.STD_IN) {
                try {
                    _reader.close();
                } catch (IOException ex) {
                    // This is typically caused by the stream being
                    // already closed, so we ignore.
                }
            }
        }

        if (_writer != null) {
            try {
                _writer.flush();

                if (_writer != FileUtilities.STD_OUT) {
                    _writer.close();
                }
            } catch (IOException ex) {
                // This is typically caused by the stream being
                // already closed, so we ignore.
            }
        }
    }

    /** Return the directory to use as the base for relative file or URL names.
     *  If setBaseDirectory() has been called, then that directory is
     *  returned.  Otherwise, the directory containing the file returned
     *  by URIAttribute.getModelURI() is returned, which is the URI
     *  of the first container above this attribute in the hierarchy that
     *  has a URIAttribute, or null if there none.
     *  @see #setBaseDirectory(URI)
     *  @see URIAttribute#getModelURI(NamedObj)
     *  @return A directory name, or null if there is none.
     */
    @Override
    public URI getBaseDirectory() {
        if (_baseDirectory != null) {
            return _baseDirectory;
        } else {
            return URIAttribute.getModelURI(this);
        }
    }

    /** Return whether the file is to be written to.
     *  @return True if the file is to be written and false otherwise.
     */
    public boolean isOutput() {
        return _isOutput;
    }

    /** Open the file or URL for reading. If the name begins with
     *  "$CLASSPATH", then search for the file relative to the classpath.
     *  If the name is relative, then it is relative to the directory
     *  returned by getBaseDirectory(). This method will first close
     *  any previously opened file, whether it was opened for reading
     *  or writing.
     *  @return A buffered reader.
     *  @see #getBaseDirectory()
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened.
     */
    @Override
    public BufferedReader openForReading() throws IllegalActionException {
        try {
            // In case there is anything open, close it.
            close();
            _reader = FileUtilities.openForReading(stringValue(),
                    getBaseDirectory(), getClass().getClassLoader());
            return _reader;
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot open file or URL");
        }
    }

    /** Open the file for writing.  If the file does not exist, then
     *  create it.  If the file name is not absolute, the it is assumed
     *  to be relative to the base directory returned by getBaseDirectory().
     *  If permitted, this method will return a Writer that will simply
     *  overwrite the contents of the file. It is up to the user of this
     *  method to check whether this is OK (by first calling asFile()
     *  and calling exists() on the returned value). This method will first close
     *  any previously opened file, whether it was opened for reading
     *  or writing.
     *  @see #getBaseDirectory()
     *  @see #asFile()
     *  @return A writer, or null if no file name has been specified.
     *  @exception IllegalActionException If the file cannot be opened
     *   or created.
     */
    @Override
    public Writer openForWriting() throws IllegalActionException {
        return openForWriting(false);
    }

    /** Open the file for writing or appending.
     *  If the file does not exist, then
     *  create it.  If the file name is not absolute, the it is assumed
     *  to be relative to the base directory returned by getBaseDirectory().
     *  If permitted, this method will return a Writer that will simply
     *  overwrite the contents of the file. It is up to the user of this
     *  method to check whether this is OK (by first calling asFile()
     *  and calling exists() on the returned value).
     *  This method will first close
     *  any previously opened file, whether it was opened for reading
     *  or writing.
     *  @see #getBaseDirectory()
     *  @see #asFile()
     *  @param append If true, then append to the file rather than
     *   overwriting.
     *  @return A writer, or null if no file name has been specified.
     *  @exception IllegalActionException If the file cannot be opened
     *   or created.
     */
    @Override
    public Writer openForWriting(boolean append) throws IllegalActionException {
        try {
            // In case there is anything open, close it.
            close();
            _writer = FileUtilities.openForWriting(stringValue(),
                    getBaseDirectory(), append);
            return _writer;
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot open file for writing");
        }
    }

    /** Set the directory to use as the base for relative file or URL names.
     *  @param directory The base directory.
     *  @see #getBaseDirectory()
     *  @see URIAttribute#getModelURI(NamedObj)
     */
    @Override
    public void setBaseDirectory(URI directory) {
        _baseDirectory = directory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The base directory to use for relative file names. */
    private URI _baseDirectory;

    /** The current reader for the input file. */
    private BufferedReader _reader;

    /** The current writer for the output file. */
    private Writer _writer;

    /** Whether the file is to be written to. */
    private boolean _isOutput;
}
