/* A file parameter that has an associated port.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.attributes.FileOrURLAccessor;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ClassUtilities;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// FilePortParameter

/**
 This file parameter creates an associated port that can be used to update
 the current value of the parameter. The value of this
 parameter, accessed by getExpression(), is a string that names a file
 or URL, possibly containing references to variables defined in scope
 using the syntax $ID, ${ID} or $(ID). The value returned by getToken()
 is name of the file with such references resolved.
 <p>
 If the model containing this port
 parameter has been saved to a MoML file, then the file name can be
 given relative to the directory containing that MoML file.
 If the model has not been saved to a file,
 then the classpath is used for identifying relative file names.
 <p>
 Files can be given relative to a <i>base</i>, where the base is
 the URI of the first container above this one that has a URIAttribute.
 Normally, this URI specifies the file or URL containing the model
 definition. Thus, files that are referred to here can be kept in the
 same directory as the model, or in a related directory, and can
 moved together with the model.
 <p>
 The following special file names are understood:
 <ul>
 <li> System.in: Standard input.
 <li> System.out: Standard output.
 </ul>
 Note, however, that these file names cannot be converted to URLs
 using the asURL() method.
 A file name can also contain the following strings that start
 with "$", which get substituted
 with the appropriate values.
 <table>
 <caption>Variables that get substituted.</caption>
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
 The above properties are normally set when a Ptolemy II application starts.
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
 of the Java classpath.
 <p>
 This parameter has two values,
 which may not be equal, a <i>current value</i> and a <i>persistent value</i>.
 The persistent value is returned by
 getExpression() and is set by any of three different mechanisms:
 <ul>
 <li> calling setExpression();
 <li> calling setToken(); and
 <li> specifying a value as a constructor argument.
 </ul>
 All three of these will also set the current value, which is then
 equal to the persistent value.
 The current value is returned by get getToken()
 and is set by any of three different mechanisms:
 <ul>
 <li> calling setCurrentValue();
 <li> calling update() sets the current value if there is an associated
 port, and that port has a token to consume; and
 </ul>
 These three techniques do not change the persistent value, so after
 these are used, the persistent value and current value may be different.
 <p>
 When using this parameter in an actor, care must be exercised
 to call update() exactly once per firing prior to calling getToken().
 Each time update() is called, a new token will be consumed from
 the associated port (if the port is connected and has a token).
 If this is called multiple times in an iteration, it may result in
 consuming tokens that were intended for subsequent iterations.
 Thus, for example, update() should not be called in fire() and then
 again in postfire().  Moreover, in some domains (such as DE),
 it is essential that if a token is provided on a port, that it
 is consumed.  In DE, the actor will be repeatedly fired until
 the token is consumed.  Thus, it is an error to not call update()
 once per iteration.  For an example of an actor that uses this
 mechanism, see Ramp.
 <p>
 If this actor is placed in a container that does not implement
 the TypedActor interface, then no associated port is created,
 and it functions as an ordinary file parameter.  This is useful,
 for example, if this is put in a library, where one would not
 want the associated port to appear.

 <p>There are a few situations where FilePortParameter might not do what
 you expect:

 <ol>
 <li> If it is used in a transparent composite actor, then a token provided
 to a FilePortParameter will never be read.  A transparent composite actor
 is one without a director.

 <br>Workaround: Put a director in the composite.

 <li> Certain actors read parameter
 values only during initialization.  During initialization, a
 FilePortParameter can only have a value set via the parameter (it
 can't have yet received a token).  So if the initial value
 is set to the value of the FilePortParameter, then it will
 see only the parameter value, never the value provided via the
 port.

 <br>Workaround: Use a RunCompositeActor to contain the model.

 </ol>

 @see ptolemy.data.expr.FileParameter
 @see ParameterPort
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class FilePortParameter extends PortParameter implements
FileOrURLAccessor {
    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will create
     *  an associated port in the same container.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public FilePortParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setStringMode(true);
        setTypeEquals(BaseType.STRING);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The token defines the initial persistent and current values.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public FilePortParameter(NamedObj container, String name,
            ptolemy.data.Token token) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, token);
        setStringMode(true);
        setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // NOTE: This code is duplicated from FileParameter, but without
    // multiple inheritance, I see no way around this.

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
     *  @return A File, or null if no file name has been specified.
     *  @see #getBaseDirectory()
     *  @exception IllegalActionException If a parse error occurs
     *   reading the file name.
     */
    @Override
    public File asFile() throws IllegalActionException {
        String name = stringValue();

        try {
            File file = FileUtilities.nameToFile(name, getBaseDirectory());
            if (file != null) {
                if (file.toString().indexOf("!/") != -1
                        || file.toString().indexOf("!\\") != -1) {
                    // We have a jar url, try dereferencing it.
                    // ModelReference.xml needed this under Webstart.
                    try {
                        URL possibleJarURL = ClassUtilities
                                .jarURLEntryResource(name);

                        if (possibleJarURL != null) {
                            file = new File(possibleJarURL.getFile());
                        }
                    } catch (Throwable throwable) {
                        //Ignored, our attempt failed
                    }
                }
            }
            return file;
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
     *  @param workspace The workspace for the cloned object.
     *  @return A new attribute.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FilePortParameter newObject = (FilePortParameter) super
                .clone(workspace);
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
     *  @return A directory name, or null if there is none.
     *  @see URIAttribute#getModelURI(NamedObj)
     *  @see #setBaseDirectory(URI)
     */
    @Override
    public URI getBaseDirectory() {
        if (_baseDirectory != null) {
            return _baseDirectory;
        } else {
            return URIAttribute.getModelURI(this);
        }
    }

    /** Open the file or URL for reading. If the name begins with
     *  "$CLASSPATH", then search for the file relative to the classpath.
     *  If the name is relative, then it is relative to the directory
     *  returned by getBaseDirectory().
     *  @return A buffered reader.
     *  @see #getBaseDirectory()
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened.
     */
    @Override
    public BufferedReader openForReading() throws IllegalActionException {
        try {
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
     *  and calling exists() on the returned value).
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
            _writer = FileUtilities.openForWriting(stringValue(),
                    getBaseDirectory(), append);
            return _writer;
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot open file for writing");
        }
    }

    /** Set the directory to use as the base for relative file or URL names.
     *  If this is not called, then the default is the directory
     *  containing the file returned by URIAttribute.getModelURI().
     *  @param directory The base directory.
     *  @see URIAttribute#getModelURI(NamedObj)
     *  @see #getBaseDirectory()
     */
    @Override
    public void setBaseDirectory(URI directory) {
        _baseDirectory = directory;
    }

    /** Return the string value of this parameter.  This is
     *  equivalent to
     *  <pre>
     *     ((StringToken)this.getToken()).stringValue()
     *  </pre>
     *  @return The string value of this parameter.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public String stringValue() throws IllegalActionException {
        return ((StringToken) getToken()).stringValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The base directory to use for relative file names. */
    private URI _baseDirectory;

    /** The current reader for the input file. */
    private BufferedReader _reader;

    /** The current writer for the output file. */
    private Writer _writer;
}
