/* A debug listener that records messages in a string buffer.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// RecorderListener
/**
A debug listener that records messages in a string buffer.

@author  Edward A. Lee, Christopher Hylands
@version $Id$
@see NamedObj
@see StreamListener

*/
public class RecorderListener implements DebugListener {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a listener.
     */
    public RecorderListener() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a string representation of the event to the current
     *  set of messages.
     */
    public void event(DebugEvent event) {
	_buffer.append(event.toString() + "\n");
    }

    /** Get the messages recorded so far.
     */
    public String getMessages() {
        return _buffer.toString();
    }

    /** Append the message to the current set of messages.
     *  A newline is automatically appended to the message.
     */
    public void message(String message) {
        _buffer.append(message + "\n");
    }

    /** Clear the buffer.
     */
    public void reset() {
        _buffer = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private StringBuffer _buffer = new StringBuffer();
}
