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

import caltrop.interpreter.InputChannel;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.CalIOException;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// DFInputChannel
/**
@author Jörn W. Janneck <janneck@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
class DFInputChannel implements InputChannel {

   public Object get(int n) {
        int m = n - buffer.size() + 1;
        if (m <= 0)
            return buffer.get(n);
        try {
            if (!port.hasToken(channel, m)) {
                throw new CalIOException("Insufficient number of tokens.");
            }
            for (int i = 0; i < m; i++) {
                buffer.add(port.get(channel));
            }
            return buffer.get(n);
        } catch (IllegalActionException e) {
            throw new CalIOException("Could not read tokens.", e);
        }
    }

    public void reset() {
        buffer.clear();
    }

    public boolean hasAvailable(int n) {
        int m = n - buffer.size();
        if (m <= 0)
            return true;
        try {
            if (channel < port.getWidth())
                return port.hasToken(channel, m);
            else
                return n == 0;
        } catch (IllegalActionException ex) {
        throw new CalIOException("Could not test for presence of tokens.",
            ex);
        }
    }


    public DFInputChannel(TypedIOPort port, int channel) {
        this.port = port;
        this.channel = channel;
        this.buffer = new ArrayList();
    }

    public String toString() {
        return "(DFInputChannel " + channel + " at " + port.toString() + ")";
    }

    private TypedIOPort port;
    private int channel;
    private List buffer;
}
