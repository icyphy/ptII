/* An actor that reads a URL naming a directory and outputs each
 element of the directory one at a time.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// URLDirectoryReader

/**
 This actor reads a URL and if the URL names a directory, it outputs
 the name of each file or subdirectory contained in the directory.
 If the URL names a file, then it outputs the name of that file.

 <p>If the <i>repeat</i> flag is true, then the sequence of file
 names is repeated indefinitely.
 If the <i>refresh</i> flag is true, and the <i>repeat</i> flag is
 true, then the directory is re-read before repeating the sequence of
 files and subdirectories.

 <p>If the <i>endsWith</i> String parameter is non-null and non-empty,
 then only file names or subdirectories that end with the value
 of the <i>endsWith</i> parameter are output.

 <p>One alternative implementation would be that if the URL named a file,
 then the actor would output the names of the files and subdirectories
 in the directory that contains the file.
 <br>Another alternative implementation would output the names of the
 files and subdirectories in an array.
 <br>An extension would be to include a filter parameter that could be
 a regular expression that would allow us to filter the file names.
 <br> Should this actor extend URLReader or SequenceActor?

 @author  Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (liuj)
 @deprecated Use DirectoryListing instead.
 */
@Deprecated
public class URLDirectoryReader extends URLReader {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public URLDirectoryReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        output.setTypeEquals(BaseType.STRING);

        // Set the endsWith String.
        endsWith = new Parameter(this, "endsWith", new StringToken(""));
        endsWith.setTypeEquals(BaseType.STRING);
        attributeChanged(endsWith);

        // Set the repeat Flag.
        repeat = new Parameter(this, "repeat", new BooleanToken(false));
        repeat.setTypeEquals(BaseType.BOOLEAN);
        attributeChanged(repeat);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If non-null and non-empty, then only output file names and sub
     *  directories that end with this String value.
     *        The default value of this parameter is the empty String "".
     */
    public Parameter endsWith;

    /** Repeat after outputting all elements of the directory.
     *        The default value of this parameter is a false BooleanToken.
     */
    public Parameter repeat;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>URL</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == repeat) {
            _repeatFlag = ((BooleanToken) repeat.getToken()).booleanValue();
        } else if (attribute == endsWith) {
            StringToken endsWithToken = (StringToken) endsWith.getToken();

            if (endsWithToken == null) {
                _endsWithValue = null;
            } else {
                _endsWithValue = endsWithToken.stringValue();
            }
        }

        super.attributeChanged(attribute);
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new StringToken(_data[_iterationCount]));
    }

    /** Open the file at the URL, and set the width of the output.
     *  @exception IllegalActionException Not thrown in this base class
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
    }

    /** Update the iteration counter until it exceeds the number of
     *  elements in the directory.  If the <i>repeat</i> parameter
     *  is true, then repeat the same sequence of directory elements
     *  again.  If the <i>repeat</i> and <i>refresh</i> parameters
     *  are both true, then reread the directory before repeating
     *  the sequence of directory elements
     *
     *  @exception IllegalActionException If the sourceURL is not valid.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;

        if (_iterationCount >= _data.length) {
            if (!_repeatFlag) {
                return false;
            } else {
                _iterationCount = 0;

                if (_refreshFlag) {
                    _data = _list(_source, _endsWithValue);
                }
            }
        }

        return super.postfire();
    }

    /** Read one row from the input and prepare for output them.
     *  @exception IllegalActionException If the <i>sourceURL</i> is invalid.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        try {
            _data = _list(_source, _endsWithValue);
            return super.prefire();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "prefire() failed");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the URL names a directory return an array containing
     *  the names of the files and subdirectories contained in the
     *  directory.  If the URL names a file, then return an array
     *  of size 1 containing the name of the file.  If the URL
     *  names neither a file or directory, return null.
     *
     *  @param source The filename or URL to open
     *  @param endsWith If non-null, then only files or subdirectories
     *  that end with this string are reported.
     *  @return An array of Strings where each element of the array
     *  names a file or subdirectory.
     *  @exception IllegalActionException If the source is a malformed
     *  URL
     */
    private String[] _list(String source, String endsWith)
            throws IllegalActionException {
        if (source.startsWith("file:")) {
            return _listFile(source, endsWith);
        } else {
            try {
                return _listFileOrURL(source, endsWith);
            } catch (Exception ex) {
                throw new IllegalActionException("Could not open '" + source
                        + ": " + KernelException.stackTraceToString(ex));
            }
        }
    }

    /** Return files and directories contained in the source url.
     *  @param source The source URL to query for files and subdirectories.
     *  The source url must be a String using the "file:" protocol.
     *  @param endsWith If non-null and of length greater than 0,
     *  then only files or subdirectories that end with this string
     *  are reported.
     *  @return An array containing the files and subdirectories in
     *  the source URL.
     *  @exception IllegalActionException If the source does not have
     *  the file: protocol, or if the source is neither a file
     *  nor a directory, or if there is some other problem.
     */
    private String[] _listFile(String source, String endsWith)
            throws IllegalActionException {
        try {
            URL sourceURL = new URL(source);

            if (sourceURL.getProtocol().equals("file")) {
                // First, try opening the source as a file.
                File file = new File(sourceURL.getFile());

                if (file.isDirectory()) {
                    if (!source.endsWith("/")) {
                        source = source + "/";
                    }

                    // Note: we could use listFiles(FileFilter) here.
                    // but since the filter is fairly simple, we don't
                    File[] files = file.listFiles();
                    List resultsList = new LinkedList();

                    for (File file2 : files) {
                        String filename = file2.getName();

                        if (endsWith == null || endsWith.length() == 0
                                || filename.endsWith(endsWith)) {
                            resultsList.add(source + filename);
                        }
                    }

                    String[] results = new String[resultsList.size()];
                    return (String[]) resultsList.toArray(results);
                } else if (file.isFile()) {
                    return new String[] { file.toString() };
                } else {
                    throw new IllegalActionException("'" + source
                            + "' is neither a file " + "or a directory?");
                }
            } else {
                // FIXME: handle urls here.
                throw new IllegalActionException("'" + source + "' does not "
                        + "have the file: protocol");
            }
        } catch (Exception ex) {
            throw new IllegalActionException("Could not open '" + source
                    + "' :" + ex);
        }
    }

    /** Return files and directories contained in the source url.
     *  This method attempts to parse the html results returned by
     *  reading a URL connection, so the parsing may fail.  If the URL
     *  uses the http: protocol, then the remote webserver
     *  configuration determines whether it is possible to read the
     *  contents of a directory.  Usually, the server has to have
     *  directory listing enabled, and the default html file
     *  (index.htm, index.html, default.htm etc. ) must not be present.
     *
     *  @param source The source URL to query for files and subdirectories.
     *  The source url must be a String using the "file:" protocol.
     *  @param endsWith If non-null and of length greater than 0,
     *  then only files or subdirectories that end with this string
     *  are reported.
     *  @return An array containing the files and subdirectories in
     *  the source URL.
     *  @exception IllegalActionException If the source does not have
     *  the file: protocol, or if the source is neither a file
     *  nor a directory, or if there is some other problem.  */
    private static String[] _listFileOrURL(String source, String endsWith)
            throws MalformedURLException, IOException {
        URL url = new URL(source);
        URLConnection urlConnection = url.openConnection();
        String contentType = urlConnection.getContentType();

        if (!contentType.startsWith("text/html")
                && !contentType.startsWith("text/plain")) {
            throw new RuntimeException("Could not parse '" + source
                    + "', it is not \"text/html\", "
                    + "or \"text/plain\", it is: "
                    + urlConnection.getContentType());
        }

        List resultsList = new LinkedList();

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));

            if (!contentType.startsWith("text/plain")
                    && !urlConnection.getURL().toString().endsWith("/")) {
                // text/plain urls need not end with /, but
                // text/html urls _must_ end with / since the web server
                // will rewrite them for us.
                throw new RuntimeException("Could not parse '" + source
                        + "', it does not end with '/'");
            }

            if (!source.endsWith("/")) {
                source += "/";
            }

            // Parse the contents in a haphazard fashion.
            // The idea is that we look for the <BODY> line and
            // then looks for lines that contain HREF
            // If we find a line like HREF="foo">foo, then we report
            // foo as being a file.
            // A more robust way would be to use a spider, see
            // http://www.acme.com/java/software/WebList.html
            String line;
            String target = null;
            boolean sawBody = false;
            boolean sawHREF = false;

            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("<BODY") || line.startsWith("<body")) {
                    sawBody = true;
                } else {
                    if (sawBody) {
                        StringTokenizer tokenizer = new StringTokenizer(line,
                                "<\" >=");

                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();

                            if (token.compareToIgnoreCase("HREF") == 0) {
                                sawHREF = true;
                                target = null;
                            } else {
                                if (sawHREF) {
                                    if (target == null) {
                                        // Here, we should check that target
                                        // is a relative pathname.
                                        target = token;
                                    } else {
                                        // Check to see if the token is
                                        // the same as the last token.
                                        if (token.compareTo(target) != 0) {
                                            sawHREF = false;
                                        } else {
                                            // If we were really brave, we
                                            // could try opening a connection
                                            // here to verify that the target
                                            // exists.
                                            if (endsWith == null
                                                    || endsWith.length() == 0
                                                    || target
                                                    .endsWith(endsWith)) {
                                                resultsList
                                                .add(source + target);
                                            }

                                            sawHREF = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        String[] results = new String[resultsList.size()];
        return (String[]) resultsList.toArray(results);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // If non-null and non-empty, then we only output file names and
    // subdirectories that match this String.
    private String _endsWithValue;

    // Count of the iterations.
    private int _iterationCount = 0;

    // An array containing the files and subdirectories in the directory
    // named by sourceURL.
    // FIXME: Should we clone this?
    private String[] _data;

    // Flag to indicate whether or not to repeat the sequence.
    private boolean _repeatFlag;
}
