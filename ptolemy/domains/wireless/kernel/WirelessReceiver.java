/* A receiver for use in the wireless domain.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;



//////////////////////////////////////////////////////////////////////////
//// WirelessReceiver
/**
A receiver for use in the wireless domain.

<p>This receiver overrides the base class to handle transmit properties.
This receiver works by bundling the token and its properties into a
record token, and then unbundling at the corresponding get() methods.

@author Edward A. Lee
@version $Id$
*/

public class WirelessReceiver extends DEReceiver {

    /** Construct an empty WirelessReceiver with no container.
     */
    public WirelessReceiver() {
        super();
    }

    /** Construct an empty DEReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public WirelessReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to unbundle the token and its properties.
     *  After this is called, the properties are available via the
     *  getProperties() method.
     *  @return The next token in the receiver.
     *  @see #getProperties()
     *  @see ptolemy.actor.Receiver#get()
     *  @exception NoTokenException If there are no more tokens. This is
     *   a runtime exception, so it need not be declared explicitly.
     */
    public synchronized Token get() throws NoTokenException {
        RecordToken bundled = (RecordToken)super.get();
        _properties = bundled.get("properties");
        return bundled.get("value");
    }

    /** Return the properties token associated with the most recent
     *  call to get(), or null if there was none.
     *  @return A properties token or null.
     */
    public synchronized Token getProperties() {
        return _properties;
    }

    /** Override the base class to attach an empty properties token.
     *  @param token The token to put.
     *  @param time The time in the future.
     *  @see ptolemy.domains.de.kernel.DEReceiver#put(ptolemy.data.Token, double)
     */
    public synchronized void put(Token token, double time)
            throws IllegalActionException {
        // Bundle the two into one token.
        // Construct the message about the input signal detected.
        String[] labels = {"properties", "value"};
        // The following should not occur, but just in case...
        if (token == null) {
            token = _dummy;
        }
        Token[] values = {
            _dummy,
            token
        };
        Token result = new RecordToken(labels, values);
        super.put(result, time);
    }

    /** Override the base class to attach an empty properties token.
     *  @param token The token to put.
     *  @see ptolemy.domains.de.kernel.DEReceiver#put(ptolemy.data.Token)
     */
    public synchronized void put(Token token) {
        put(token, null);
    }

    /** Put the specified token bundled with the specified properties.
     *  @param token The token to put.
     *  @param properties The associated properties, or null to not
     *   put any in.
     */
    public synchronized void put(Token token, Token properties) {
        // Bundle the two into one token.
        // Construct the message about the input signal detected.
        String[] labels = {"properties", "value"};
        if (properties == null) {
            properties = _dummy;
        }
        // The following should not occur, but just in case...
        if (token == null) {
            token = _dummy;
        }
        Token[] values = {
            properties,
            token
        };
        Token result = null;
        try {
            result = new RecordToken(labels, values);
        } catch (IllegalActionException e) {
            // Should not occur since we've ensured above that
            // nothing is null and the arrays have the same size.
            throw new InternalErrorException(e);
        }
        super.put(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Dummy token used when no properties are sent.
    private Token _dummy = new Token();

    // Most recently seen properties.
    private Token _properties;
}
