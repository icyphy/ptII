/*
@Copyright (c) 2003 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)


*/
package ptolemy.caltrop.ddi;

import caltrop.interpreter.OutputChannel;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.CalIOException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DFOutputChannel
/**
@author Jörn W. Janneck <janneck@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
class DFOutputChannel implements OutputChannel {

    public void put(Object a) {
        try {
            port.send(channel, (Token)a);
        } catch (IllegalActionException ex) {
            throw new CalIOException("Could not send token.", ex);
        } catch (NoRoomException ex) {
            throw new CalIOException("No room for sending token.", ex);
        } catch (ClassCastException ex) {
            throw new CalIOException("Token not of valid token type.", ex);
        }
    }

    //

    public DFOutputChannel(TypedIOPort port, int channel) {
        this.port = port;
        this.channel = channel;
    }

    public String toString() {
        return "(DFOutputChannel " + channel + " at " + port.toString() + ")";
    }

    private TypedIOPort port;
    private int channel;
}
