/* An actor that writes input data to the specified writer.

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

import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Writer

/**
 This actor reads tokens from any number of input channels and writes
 their string values to the specified writer.  A newline character
 is written between firings.  If the width of the input port is greater
 than one, then values read from the distinct channels are separated
 by tab characters. If no writer is specified using setWriter(), then
 this actor writes to the standard output.

 @deprecated FileWriter actor replaces this.
 @author  Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (mudit)
 */
@Deprecated
public class Writer extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Writer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        if (_stdOut == null) {
            _stdOut = new OutputStreamWriter(System.out);
        }

        setWriter(_stdOut);

        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input channel and write its
     *  string value.  These values are separated by tab characters.
     *  If an input channel has no data, then two consecutive tab
     *  characters are written.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        try {
            String last = "";
            int width = input.getWidth();

            for (int i = 0; i < width; i++) {
                if (i > 0) {
                    _writer.write("\t");
                }

                if (input.hasToken(i)) {
                    StringToken inputToken = (StringToken) input.get(i);
                    last = inputToken.stringValue();
                    _writer.write(last);
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

    /** Set the writer.  If there was a previous writer, close it.
     *  To set standard output, call this method with argument null.
     *  @param writer The writer to write to.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void setWriter(java.io.Writer writer) throws IllegalActionException {
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

    /** Flush the writer, if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        try {
            if (_writer != null) {
                // FIXME: Findbugs warns out derived classes such as
                // FileWriter.attributeChanged() not closing the
                // _writer.  Should wrapup() close the _writer?
                _writer.flush();
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "wrapup(" + _writer
                    + ") failed");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The writer to write to.
    private java.io.Writer _writer = null;

    // Standard out as a writer.
    private static java.io.Writer _stdOut = null;
}
