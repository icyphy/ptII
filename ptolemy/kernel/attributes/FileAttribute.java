/* An attribute that specifies a file or URL.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.attributes;

import ptolemy.kernel.util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// FileAttribute
/**
This is an attribute that specifies a file or URL.  The value of this
attribute, accessed by getExpression(), is a string that names a file
or URL. If the model containing this attribute has been saved to a
MoML file, then the file name can be given relative to the directory
containing that MoML file.  If the model has not been saved to a file,
then the classpath is used for identifying relative file names.
<p>
The following special file names are understood:
<ul>
<li> System.in: Standard input.
<li> System.out: Standard output.
</ul>
Note, however, that these file names cannot be converted to URLs
using the asURL() method.
<p>
A file name can also contain the string "$PTII", which refers to
the home directory of the Ptolemy II installation, or the string
"$CWD", which refers the current working directory, or "$HOME",
which refers to the user's home directory.  These values are
obtained from the Java properties <i>ptolemy.ptII.dir</i>,
<i>user.dir</i>, and <i>user.home</i>, respectively.  These
properties are normally set when a Ptolemy II application starts.
<p>
@author Edward A. Lee
@version $Id$
*/
public class FileAttribute extends StringAttribute {

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
    public FileAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the file as a File object.  This method first attempts
     *  to directly use the file name to construct the File. If the
     *  resulting File is not absolute, then it attempts to resolve it
     *  relative to relative to the base, where the base is
     *  the URI of the first container above this one
     *  that has a URIAttribute.  If there is no such base URI,
     *  then it simply returns the relative File object.
     *  The file need not exist for this to succeed, so this method
     *  can be used prior to calling openForWriting() to determine whether
     *  the file exists (call the exists() method on the return value)
     *  to query the user for overwrite.
     *  @return A File, or null if no file name has been specified.
     *  @exception IllegalActionException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in).
     */
    public File asFile() {
        String name = _substituteSpecialStrings(getExpression());
        if (name == null || name.trim().equals("")) {
            return null;
        }
        File file = new File(name);
        if (!file.isAbsolute()) {
            // Try to resolve the URI.
            URI modelURI = URIAttribute.getModelURI(this);
            if (modelURI != null) {
                URI newURI = modelURI.resolve(name);
                file = new File(newURI);
            }
        }
        return file;
    }

    /** Return the file as a URL.  This method first attempts to directly
     *  use the file name to identify a readable file, and if it finds one,
     *  returns that file as a URL.  If this fails, then it tries to open
     *  the file relative to the URI of the first container above this one
     *  that has a URIAttribute.  If there is no such base URI,
     *  then it tries to open the file relative to the classpath.
     *  If that fails, then it throws an exception.
     *  @return A URL, or null if no file name or URL has been specified.
     *  @exception IllegalActionException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in).
     */
    public URL asURL() throws IllegalActionException {
        String name = _substituteSpecialStrings(getExpression());

        if (name == null || name.trim().equals("")) {
            return null;
        }
        File file = new File(name);
        if (file.exists()) {
            if (!file.canRead()) {
                throw new IllegalActionException(this,
                        "Cannot read file: " + name);
            }
            try {
                return file.toURL();
            } catch (MalformedURLException ex) {
                throw new IllegalActionException(this,
                        "Cannot open file: " + ex.toString());
            }
        } else {
            // Try relative to the URIAttribute.
            URI modelURI = URIAttribute.getModelURI(this);
            if (modelURI != null) {
                try {
                    // Try to resolve the URI.
                    URI newURI = modelURI.resolve(name);
                    return newURI.toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalActionException(this,
                            "Unable to open as a file or URL: " + name);
                }
            }

            // NOTE: This doesn't seem right.  This code will never be
            // reached if there is a URIAttribute.  But there seems to
            // be no way to decide between these.  It won't work
            // to try to open the URI, because it is premature.
            // This method gets called when the model is opened,
            // not when it is run.  We do not want to require a net
            // connection to be present to open a model that refers
            // to a URI.

            // Try relative to classpath.
            URL result = getClass().getClassLoader().getResource(name);
            if (result != null) {
                return result;
            } else {
                try {
                    // Try an absolute URL
                    return new URL(name);
                } catch (MalformedURLException e) {
                    throw new IllegalActionException(this,
                            "Unable to open as a file or URL: " + name);
                }
            }
        }
    }

    /** Clone the attribute into the specified workspace.  This loses
     *  the base directory name and references to any open streams.
     *  @return A new attribute.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        FileAttribute newObject = (FileAttribute)super.clone(workspace);
        newObject._baseDirectory = null;
        newObject._reader = null;
        return newObject;
    }

    /** Close the file. If it has not been opened using openForReading()
     *  or openForWriting(), then do nothing.  Also, if the file is
     *  System.in or System.out, then do not close it (it does not make
     *  sense to close these files).
     *  @exception IllegalActionException If the file or URL cannot be
     *   closed.
     */
    public void close() throws IllegalActionException {
        if (_reader != null) {
            if (_reader != _stdIn) {
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
                if (_writer != _stdOut) {
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
     *  by getModelURI() is returned.
     *  @return A directory name.
     */
    public URI getBaseDirectory() {
        if (_baseDirectory != null) {
            return _baseDirectory;
        } else {
            return URIAttribute.getModelURI(this);
        }
    }

    /** Open the file or URL for reading.
     *  @return A buffered reader.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened.
     */
    public BufferedReader openForReading() throws IllegalActionException {
        if (getExpression().trim().equals("System.in")) {
            if (_stdIn == null) {
                _stdIn = new BufferedReader(new InputStreamReader(System.in));
            }
            _reader = _stdIn;
            return _reader;
        }
        // Not standard input. Try URL mechanism.
        URL url = asURL();
        try {
            _reader = new BufferedReader(
                    new InputStreamReader(url.openStream()));
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
     *  method to check whether this is OK.
     *  @see #asFile()
     *  @return A writer, or null if no file name has been specified.
     *  @exception IllegalActionException If the file cannot be opened
     *   or created.
     */
    public Writer openForWriting() throws IllegalActionException {
        String name = getExpression();
        if (name.trim().equals("System.out")) {
            if (_stdOut == null) {
                _stdOut = new PrintWriter(System.out);
            }
            _writer = _stdOut;
            return _writer;
        }
        if (name == null || name.trim().equals("")) {
            return null;
        }
        File file = asFile();
        try {
            _writer = new FileWriter(file);
            return _writer;
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot open file for writing: " + name);
        }
    }

    /** Set the directory to use as the base for relative file or URL names.
     *  If this is not called, then the default is the directory
     *  containing the file returned by getModelURI() is returned.
     *  @param directory The base directory.
     */
    public void setBaseDirectory(URI directory) {
        _baseDirectory = directory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a string that is the current value of this attribute
     *  with the strings "$PTII", "$CWD" and "$HOME" replaced by their
     *  respective values.
     *  @param string The string in which to do the substitution.
     *  @return A new string.
     */
    private static String _substituteSpecialStrings(String string) {
        String result = string;
        if (result.indexOf("$PTII") >= 0) {
            result = StringUtilities.substitute(result,
                    "$PTII",
                    System.getProperty("ptolemy.ptII.dir"));
        }
        if (result.indexOf("$HOME") >= 0) {
            result = StringUtilities.substitute(result,
                    "$HOME",
                    System.getProperty("user.home"));
        }
        if (result.indexOf("$CWD") >= 0) {
            result = StringUtilities.substitute(result,
                    "$CWD",
                    System.getProperty("user.dir"));
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The base directory to use for relative file names. */
    private URI _baseDirectory;

    /** The current reader for the input file. */
    private BufferedReader _reader;

    /** The current writer for the output file. */
    private Writer _writer;

    /** Standard in as a reader. */
    private static BufferedReader _stdIn = null;

    /** Standard out as a writer. */
    private static PrintWriter _stdOut = null;
}
