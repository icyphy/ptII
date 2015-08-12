/* An interface for Attributes and Parameters that access files or URLs.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.kernel.attributes;

import java.io.BufferedReader;
import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// FileOrURLAccessor

/**
 An interface for Attributes and Parameters that access files or URLs.

 <p>This interface is necessary because Java does not support multiple
 inheritance and we have two classes (FileParameter and FilePortParameter)
 that have a common interface, but do not have an immediate parent class.
 In addition,
 {@link ptolemy.kernel.attributes.FileAttribute} shares this interface.

 <p>This interface is implemented by an Attribute or Parameter that
 specifies a file or URL.  The value of this Attribute or Parameter,
 accessed by getExpression(), is a string that names a file or URL. If
 the model containing this attribute has been saved to a MoML file,
 then the file name can be given relative to the directory containing
 that MoML file.  If the model has not been saved to a file, then the
 classpath is used for identifying relative file names.

 <p> Files can be given relative to a <i>base</i>, where the base is
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
 <p>
 A file name can also contain the following strings that start
 with "$", which get substituted
 with the appropriate values
 <table>
 <caption>Preset values</caption>
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
 </table>
 The above properties are normally set when a Ptolemy II application starts.
 <p>
 If a file name begins with the string "$CLASSPATH", followed by either
 "/" or "\", then when the file
 is opened for reading, the openForReading() method
 will search for the file relative to the classpath (using the
 getResource() method of the current class loader).  This will only
 work for a file that exists, and thus the openForWriting() method
 will not understand the "$CLASSPATH" string; this makes sense
 since the classpath typically has several directories, and it
 would not be obvious where to create the file.  The asURL()
 method also recognizes the "$CLASSPATH" string, but not the asFile()
 method (which is typically used when accessing a file for writing).
 <p>
 @author Christopher Brooks, based on FileAttribute by Edward A. Lee
 @version $Id$
 @see URIAttribute
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface FileOrURLAccessor extends Settable {
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
     *      FileAttribute fileAttribute;
     *      ...
     *      File file = fileAttribute.asFile();
     *      if (file.exists()) {
     *         ... Ask the user if it's OK to overwrite...
     *         ... Throw an exception if not...
     *      }
     *      // The following will overwrite an existing file.
     *      Writer writer = new PrintWriter(fileAttribute.openForWriting());
     *  </pre>
     *  @return A File, or null if no file name has been specified.
     *  @see #getBaseDirectory()
     *  @exception IllegalActionException If a parse error occurs
     *   reading the file name.
     */
    public File asFile() throws IllegalActionException;

    /** Return the file as a URL.  If the file name is relative, then
     *  it is interpreted as being relative to the directory returned
     *  by getBaseDirectory(). If the name begins with "$CLASSPATH",
     *  then search for the file relative to the classpath.
     *  If no file is found, then it throws an exception.
     *  @return A URL, or null if no file name or URL has been specified.
     *  @exception IllegalActionException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in).
     */
    public URL asURL() throws IllegalActionException;

    /** Close the file. If it has not been opened using openForReading()
     *  or openForWriting(), then do nothing.  Also, if the file is
     *  System.in or System.out, then do not close it (it does not make
     *  sense to close these files).
     *  @exception IllegalActionException If the file or URL cannot be
     *   closed.
     */
    public void close() throws IllegalActionException;

    /** Return the directory to use as the base for relative file or URL names.
     *  If setBaseDirectory() has been called, then that directory is
     *  returned.  Otherwise, the directory containing the file returned
     *  by URIAttribute.getModelURI() is returned, which is the URI
     *  of the first container above this attribute in the hierarchy that
     *  has a URIAttribute, or null if there none.
     *  @return A directory name, or null if there is none.
     *  @see #setBaseDirectory(URI)
     *  @see URIAttribute#getModelURI(NamedObj)
     */
    public URI getBaseDirectory();

    /** Open the file or URL for reading. If the name begins with
     *  "$CLASSPATH", then search for the file relative to the classpath.
     *  If the name is relative, then it is relative to the directory
     *  returned by getBaseDirectory().
     *  @return A buffered reader.
     *  @see #getBaseDirectory()
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened.
     */
    public BufferedReader openForReading() throws IllegalActionException;

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
    public Writer openForWriting() throws IllegalActionException;

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
    public Writer openForWriting(boolean append) throws IllegalActionException;

    /** Set the directory to use as the base for relative file or URL names.
     *  If this is not called, then the default is the directory
     *  containing the file returned by URIAttribute.getModelURI().
     *  @param directory The base directory.
     *  @see #getBaseDirectory()
     *  @see URIAttribute#getModelURI(NamedObj)
     */
    public void setBaseDirectory(URI directory);
}
