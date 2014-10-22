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

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.CalIOException;
import ptolemy.kernel.util.IllegalActionException;
import caltrop.interpreter.InputChannel;

//////////////////////////////////////////////////////////////////////////
//// DFInputChannel

/**
 @author J&#246;rn W. Janneck
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
class DFInputChannel implements InputChannel {
    public DFInputChannel(TypedIOPort port, int channel) {
        this.port = port;
        this.channel = channel;
        this.buffer = new ArrayList();
        this.tokensRead = 0;
    }

    /** Get the given token from this input channel.  If necessary,
     * consume tokens from the associated Ptolemy input port to make
     * the given token available.
     */
    @Override
    public Object get(int n) {
        int m = n - buffer.size() + 1;

        if (m <= 0) {
            tokensRead = Math.max(tokensRead, n + 1);
            return buffer.get(n);
        }

        try {
            //            System.out.println("Attempting to read " + n + " tokens from port with rate = " + ptolemy.actor.util.DFUtilities.getTokenConsumptionRate(port));
            if (!port.hasToken(channel, m)) {
                throw new CalIOException("Insufficient number of tokens.");
            }

            for (int i = 0; i < m; i++) {
                buffer.add(port.get(channel));
            }

            tokensRead = Math.max(tokensRead, n + 1);
            return buffer.get(n);
        } catch (IllegalActionException e) {
            throw new CalIOException("Could not read tokens.", e);
        }
    }

    /** Commit reads of this channel.  Any tokens read since the last
     * invocation of the rollback method are lost and no longer
     * available for reading.
     */
    @Override
    public void commit() {
        assert tokensRead <= buffer.size();

        if (tokensRead == buffer.size()) {
            buffer.clear();
        } else {
            for (int i = 0; i < tokensRead; i++) {
                buffer.remove(0);
            }
        }

        tokensRead = 0;
    }

    /** Rollback any reads from this channel, allowing the tokens to
     * be read again.
     */
    @Override
    public void rollback() {
        tokensRead = 0;
    }

    /** Return true if the given number of tokens are available to be
     * immediately read from the channel.
     */
    @Override
    public boolean hasAvailable(int n) {
        int m = n - buffer.size();

        if (m <= 0) {
            return true;
        }

        try {
            if (channel < port.getWidth()) {
                return port.hasToken(channel, m);
            } else {
                return n == 0;
            }
        } catch (IllegalActionException ex) {
            throw new CalIOException("Could not test for presence of tokens.",
                    ex);
        }
    }

    @Override
    public String toString() {
        return "(DFInputChannel " + channel + " at " + port.toString() + ")";
    }

    private TypedIOPort port;

    private int channel;

    private int tokensRead;

    private List buffer;
}
