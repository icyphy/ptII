/* Abstract base class for actors that read in data from a URL

@Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// URLReader
/**
This actor reads tokens from an URL, and output them. Each entry in
the file corresponds to one iteration. If there are multiple fires in
the iteration, the same token will be repeated.  This actor has a
multiport, where each port corresponds to one column in the data file.

<p> The file format at the URL is assumed as the following.  A newline
character separates the rows, and a tab or a space character separates
the columns.

<p> The <i>sourceURL</i> parameter should be set to the name of the
file, specified as a fully qualified URL.  If the <i>sourceURL</i>
parameter is an empty string, then the System.in is used for input.

It is possible to load a file from the local file system by using the
prefix "file://" instead of "http://". Relative file paths are
allowed. To specify a file relative to the current directory, use
"../" or "./". For example, if the current directory contains a file
called "test.txt", then <i>sourceURL</i> should be set to
"file:./test.txt". If the parent directory contains a file called
"test.txt", then <i>sourceURL</i> should be set to
"file:../test.txt". To reference the file test.txt, located at
"/tmp/test.txt", <i>sourceURL</i> should be set to
"file:///tmp/test.txt" The default value is "file:///tmp/test.txt".

<p>FIXME: The type of the output ports is set to Double for now.
       It should read a line in the prefire() and refer the type
       from there.
<p>FIXME: URLReader should read in expressions and serialized tokens

@see ptolemy.actor.lib.javasound.AudioReader
@author  Jie Liu
@version $Id$
@since Ptolemy II 2.0
*/
public abstract class URLReader extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public URLReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);

        sourceURL = new Parameter(this, "sourceURL", new StringToken(""));
        sourceURL.setTypeEquals(BaseType.STRING);

        refresh = new Parameter(this, "refresh", new BooleanToken(false));
        refresh.setTypeEquals(BaseType.BOOLEAN);

        if (_stdIn == null) {
            _stdIn = new BufferedReader(new InputStreamReader(System.in));
        }
        _setURLReader(_stdIn);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The URL of the file to read from. This parameter contains
     *  a StringToken.  By default, it contains an empty string, which
     *  is interpreted to mean that input should be directed to the
     *  standard input.
     *  FIXME: Should this bring up a dialog box to type (or select) a URL?
     */
    public Parameter sourceURL;

    /** The flag that indicates whether to refresh between each reading.
     *  This is a boolean, and defaults to false.
     */
    public Parameter refresh;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>URL</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == sourceURL) {
            StringToken urlToken = null;
            try {
                urlToken = (StringToken)sourceURL.getToken();
                if (urlToken == null) {
                    _source = null;
                    _setURLReader(null);
                } else {
                    _source = urlToken.stringValue();
                    if (_source.equals("")) {
                        _setURLReader(null);
                    } else {
                        URL url = new URL(_source);
                        java.io.BufferedReader reader = new BufferedReader(
                                new InputStreamReader(url.openStream()));
                        _setURLReader(reader);
                    }
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to evaluate sourceURL '"
                        + urlToken + "'");
            }
        } else if (attribute == refresh) {
            _refreshFlag = ((BooleanToken)refresh.getToken()).booleanValue();
        }
        super.attributeChanged(attribute);
    }

    /** Open the file at the URL, and set the width of the output.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        attributeChanged(sourceURL);
    }

    /** Close the reader if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void wrapup() throws IllegalActionException {
        try {
            if (_reader != null && _reader != _stdIn) {
                _reader.close();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "Failed to close");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                /////

    /** Set the reader.  If there was a previous reader, close it.
     *  To set standard input, call this method with argument null.
     *  @param reader The reader to read to.
     *  @exception IllegalActionException If an IO error occurs.
     */
    protected void _setURLReader(java.io.BufferedReader reader)
            throws IllegalActionException {
        try {
            if (_reader != null && _reader != _stdIn) {
                _reader.close();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "Failed to close");
        }
        if (reader != null) {
            _reader = reader;
        } else {
            _reader = _stdIn;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    // The reader to read from.
    protected java.io.BufferedReader _reader = null;

    // Standard in as a reader.
    protected static java.io.BufferedReader _stdIn = null;

    // String for the URL.
    protected String _source;

    // Flag to indicate whether or not to refresh the data between readings.
    protected boolean _refreshFlag;
}
