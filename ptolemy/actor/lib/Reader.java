/* An actor that outputs data read from a URL.

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
import ptolemy.data.DoubleToken;
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
import java.util.StringTokenizer;

//import java.io.Reader;

//////////////////////////////////////////////////////////////////////////
//// Reader
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
<p>FIXME: Reader should read in expressions and serialized tokens

@author  Jie Liu
@version $Id$
@since Ptolemy II 1.0
*/
public class Reader extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Reader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the input port.
        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);
        sourceURL = new Parameter(this, "sourceURL", new StringToken(""));
        sourceURL.setTypeEquals(BaseType.STRING);

        refresh = new Parameter(this, "refresh", new BooleanToken(false));
        refresh.setTypeEquals(BaseType.BOOLEAN);

        if (_stdIn == null) {
            _stdIn = new BufferedReader(new InputStreamReader(System.in));
        }
        setReader(_stdIn);
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

    /** Refresh between each readings. Default is false.
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
            try {
                StringToken URLToken = (StringToken)sourceURL.getToken();
                if (URLToken == null) {
                    _source = null;
                    setReader(null);
                } else {
                    _source = URLToken.stringValue();
                    if (_source.equals("")) {
                        setReader(null);
                    } else {
                        URL url = new URL(_source);
                        java.io.BufferedReader reader = new BufferedReader(
                                new InputStreamReader(url.openStream()));
                        setReader(reader);
                    }
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "attributeChanged(" + attribute + ") failed");
            }
        }
        super.attributeChanged(attribute);
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < _dataSize; i++) {
            output.send(i, new DoubleToken(_data[i]));
        }
    }

    /** Open the file at the URL, and set the width of the output.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public void initialize() throws IllegalActionException {
        System.out.println("actor.lib.Reader is obsolete, "
                + "use actor.lib.DoubleReader instead");
        _dataSize = output.getWidth();
        _data = new double[_dataSize];
        attributeChanged(sourceURL);
    }

    /** Read one row from the input and prepare for output them.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public boolean prefire() throws IllegalActionException {
        try {
            _dataSize = output.getWidth();
            if (_data.length != _dataSize) {
                _data = new double[_dataSize];
            }
            String oneRow = _reader.readLine();
            if (oneRow == null) {
                return false;
            }
            StringTokenizer tokenizer = new StringTokenizer(oneRow);
            int columnCount = tokenizer.countTokens();
            if (_dataSize > columnCount) {
                _dataSize = columnCount;
            }
            for (int i = 0; i < _dataSize; i++) {
                _data[i] = Double.valueOf(tokenizer.nextToken()).doubleValue();
            }
            return super.prefire();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "prefire() failed");
        }
    }

    /** Set the reader.  If there was a previous reader, close it.
     *  To set standard input, call this method with argument null.
     *  @param reader The reader to read to.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void setReader(java.io.BufferedReader reader)
            throws IllegalActionException {
        try {
            if (_reader != null && _reader != _stdIn) {
                _reader.close();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "setReader(" + reader + ") failed");
        }
        if (reader != null) {
            _reader = reader;
        } else {
            _reader = _stdIn;
        }
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
            throw new IllegalActionException(this, ex,
                    "wrapup(" + _reader + ") failed");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The writer to write to.
    private java.io.BufferedReader _reader = null;

    // Standard out as a writer.
    private static java.io.BufferedReader _stdIn = null;

    // String for the URL.
    private String _source;

    // Cache of one row.
    private double[] _data;

    // Valid enties in the data array.
    private int _dataSize;
}
