/* An actor that outputs data read from a URL.

@Copyright (c) 1998-2000 The Regents of the University of California.
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

// Java imports.
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

// Ptolemy imports.
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;

/**
This actor reads tokens from an URL, and output them. Each entry
in the file corresponds to one iteration. If there are multiple
fires in the iteration, the same token will be repeated.
This actor has a multiport, where each port corresponds to
one column in the data file.
<P>
The file format at the URL is assumed as the following.
A newline character separates the rows, and a tab or a space character
separates the columns.
<P>
If the "URL" parameter is an empty string, then the System.in
is used for input.

FIXME: The type of the output ports is set to Double for now.
       We are waiting for the mechanism on the reflection of types.

@author  Jie Liu
@version $Id$
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
    public Reader(TypedCompositeActor container, String name)
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
                    System.out.println("URL=" + _source);
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
                System.out.println("URL not found..." + ex.getMessage());
                //throw new IllegalActionException(this, ex.getMessage());
                sourceURL.setToken(new StringToken(""));
            }
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Reader newobj = (Reader)super.clone(workspace);
        newobj.output.setMultiport(true);
        newobj.output.setTypeEquals(BaseType.DOUBLE);
        newobj.sourceURL = (Parameter)newobj.getAttribute("sourceURL");
        newobj.refresh = (Parameter)newobj.getAttribute("refresh");
        try {
            newobj.attributeChanged(newobj.sourceURL);
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException(ex.getMessage());
        }
        return newobj;
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
            throw new IllegalActionException(this, ex.getMessage());
        }
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        for (int i = 0; i < _dataSize; i++) {
            output.send(i, new DoubleToken(_data[i]));
        }
    }

    /** initialize the data array.
     */
    public void initialize() throws IllegalActionException {
        _dataSize = output.getWidth();
        _data = new double[_dataSize];
        attributeChanged(sourceURL);
    }

    /** Set the reader.  If there was a previous reader, close it.
     *  To set standard input, call this method with argument null.
     *  @param reader The reader to read to.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void setReader(java.io.BufferedReader reader)
            throws IllegalActionException {
        try {
            if (_reader != null && _reader != _stdIn) _reader.close();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex.getMessage());
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
            if (_reader != null && _reader != _stdIn) _reader.close();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex.getMessage());
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
