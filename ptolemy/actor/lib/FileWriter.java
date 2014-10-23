/* An actor that writes input data to the specified file.

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
import java.io.OutputStreamWriter;

import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 This actor reads tokens from any number of input channels and writes
 their string values to the specified output file. The input type
 can be anything. If a StringToken is received, then its stringValue()
 method will be used to get the string to write to the file. Otherwise,
 the toString() method of the received token will be used.  If no file name
 is given, then the values are written to the standard output.
 If multiple input channels are provided on the input port, then
 the values received are written separated by a tab character.
 Each time a new name is received on the <i>filename</i> input, a
 new file will be opened for writing. If no new filename is received,
 then the data will be appended to previously used file. When appending,
 the values received on subsequent firings are separated by a newline
 character (a newline character will be inserted if one is not already
 provide by the input string).
 Unlike @see{ExpressionWriter}, this actor makes no changes to the
 input string. It writes to the file exactly what it receives on its
 input.

 @author  Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (mudit)
 */
public class FileWriter extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FileWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        if (_stdOut == null) {
            _stdOut = new OutputStreamWriter(System.out);
        }

        _setWriter(_stdOut);

        filename = new FilePortParameter(this, "filename");
        filename.setExpression("");
        filename.setTypeEquals(BaseType.STRING);
        new SingletonParameter(filename.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-25\" y=\"-20\" " + "width=\"50\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polygon points=\"-15,-10 -12,-10 -8,-14 -1,-14 3,-10"
                + " 15,-10 15,10, -15,10\" " + "style=\"fill:red\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to.
     *  By default, this parameter contains an empty string, which
     *  is interpreted to mean that output should be directed to the
     *  standard output.
     *  See {@link ptolemy.actor.parameters.FilePortParameter} for
     *  details about relative path names.
     */
    public FilePortParameter filename;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor.
     *  @exception CloneNotSupportedException If the superclass throws it.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FileWriter newObject = (FileWriter) super.clone(workspace);
        newObject._previousFilename = null;
        newObject._writer = null;
        return newObject;
    }

    /** Read at most one token from each input channel and write its
     *  string value.  If the filename input has changed since the
     *  last writing, then open the new file for writing. Otherwise,
     *  append to the previous file. If there are multiple channels
     *  connected to the input, then the output values from each
     *  channel are separated by tab characters.
     *  If an input channel has no data, then two consecutive tab
     *  characters are written.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        filename.update();
        try {
            // NOTE: getExpression() will not get the current value
            // of this sort of PortParameter. Instead, it gets the
            // default value. Have to use getToken().
            String filenameValue = ((StringToken) filename.getToken())
                    .stringValue();

            if (filenameValue == null || filenameValue.equals("\"\"")) {
                // See $PTII/ptolemy/domains/sdf/kernel/test/auto/zeroRate_delay5.xml, which sets
                // the filename to a string that has two doublequotes. ""
                _setWriter(null);
            } else if (!filenameValue.equals(_previousFilename)) {
                // New filename. Close the previous.
                _previousFilename = filenameValue;
                _setWriter(null);
                if (!filenameValue.trim().equals("")) {
                    java.io.Writer writer = filename.openForWriting();
                    // Findbugs warns about the writer being created but
                    // not closed. But it is closed in postfire().
                    _setWriter(writer);
                }
            }
            String last = "";
            int width = input.getWidth();

            for (int i = 0; i < width; i++) {
                if (i > 0) {
                    _writer.write("\t");
                }

                if (input.hasToken(i)) {
                    Token inputToken = input.get(i);
                    if (inputToken instanceof StringToken) {
                        last = ((StringToken) inputToken).stringValue();
                    } else {
                        last = inputToken.toString();
                    }
                    _writer.write(last);
                } else {
                    last = "";
                }
            }
            // Write a newline character only if the last
            // string does not already have one.
            if (!last.endsWith("\n")) {
                _writer.write("\n");
            }
            _writer.flush();
            return super.postfire();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "postfire() failed");
        }
    }

    /** Close the file, if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        try {
            if (_writer != null) {
                _writer.flush();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "wrapup(" + _writer
                    + ") failed");
        }

        // To get the file to close.
        _setWriter(null);

        // Since we have closed the writer, we also need to clear
        // _previousFilename, so that a new writer will be opened for this
        // filename if the model is executed again
        _previousFilename = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the writer.  If there was a previous writer, close it.
     *  To set standard output, call this method with argument null.
     *  @param writer The writer to write to.
     *  @exception IllegalActionException If an IO error occurs.
     */
    private void _setWriter(java.io.Writer writer)
            throws IllegalActionException {
        try {
            if (_writer != null && _writer != _stdOut) {
                _writer.close();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "setWriter(" + writer
                    + ") failed");
        }

        if (writer != null) {
            _writer = writer;
        } else {
            _writer = _stdOut;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The previously used filename, or null if none has been previously used. */
    private String _previousFilename = null;

    /** Standard out as a writer. */
    private static java.io.Writer _stdOut = null;

    /** The writer to write to. */
    private java.io.Writer _writer = null;
}
