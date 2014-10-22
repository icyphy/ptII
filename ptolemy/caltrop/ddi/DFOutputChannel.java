/*
 @Copyright (c) 2003-2014 The Regents of the University of California.
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



 */
package ptolemy.caltrop.ddi;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.CalIOException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import caltrop.interpreter.OutputChannel;

//////////////////////////////////////////////////////////////////////////
//// DFOutputChannel

/**
 @author J&#246;rn W. Janneck
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
class DFOutputChannel implements OutputChannel {
    public DFOutputChannel(TypedIOPort port, int channel) {
        this.port = port;
        this.channel = channel;
    }

    /** Send the given object (which is assumed to be a token in this
     * implementation) from the associated TypedIOPort port.
     */
    @Override
    public void put(Object a) {
        try {
            port.send(channel, (Token) a);
        } catch (IllegalActionException ex) {
            throw new CalIOException("Could not send token.", ex);
        } catch (NoRoomException ex) {
            throw new CalIOException("No room for sending token.", ex);
        } catch (ClassCastException ex) {
            throw new CalIOException("Token not of valid token type.", ex);
        }
    }

    @Override
    public String toString() {
        return "(DFOutputChannel " + channel + " at " + port.toString() + ")";
    }

    private TypedIOPort port;

    private int channel;
}
