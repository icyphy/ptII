/* An actor that outputs doubles read from a URL.

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

import java.io.IOException;
import java.util.StringTokenizer;

import ptolemy.data.DoubleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DoubleReader

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

 @author  Jie Liu, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (liuj)
 @deprecated Use ExpressionReader instead.
 */
@Deprecated
public class DoubleReader extends URLReader {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        for (int i = 0; i < _dataSize; i++) {
            output.send(i, new DoubleToken(_data[i]));
        }
    }

    /** Open the file at the URL, and set the width of the output.
     *  @exception IllegalActionException Not thrown in this base class
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _dataSize = output.getWidth();
        _data = new double[_dataSize];
        attributeChanged(sourceURL);
    }

    /** Read one row from the input and prepare for output them.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
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

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Cache of one row.
    // FIXME: Should we clone this?
    private double[] _data;

    // Valid enties in the data array.
    // FIXME: Should we clone this?
    private int _dataSize;
}
