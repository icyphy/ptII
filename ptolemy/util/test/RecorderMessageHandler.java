/** A message handler that records messages.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

package ptolemy.util.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// RecorderMessageHandler

/** A message handler that records messages.

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)

 */
public class RecorderMessageHandler extends MessageHandler {

    /** Get the messages recorded so far.
     *  @return The messages.
     */
    public String getMessages() {
        return _buffer.toString();
    }

    /** Clear the buffer.
     */
    public void reset() {
        _buffer = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Show the specified message and throwable information and record.
     *
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see ptolemy.util.CancelException
     */
    @Override
    protected void _error(String info, Throwable throwable) {
        //super._error(info, throwable);
        System.err.println("RecorderMessageHandler: " + info + " " + throwable);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        if (throwable != null) {
            throwable.printStackTrace(printWriter);
        }

        _buffer.append("RecorderMessageHandler: Error encountered in:\n" + info
                + "\n" + stringWriter);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private StringBuffer _buffer = new StringBuffer();
}
