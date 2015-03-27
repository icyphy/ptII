/* An actor that converts a SmoothToken (one that has a double value and an array of derivatives to a DoubleToken.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import ptolemy.actor.lib.conversions.Converter;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SmoothTDouble

/**
 Convert a {@link SmoothToken} (one that has a double value and an array of
 derivatives) to a DoubleToken, discarding the derivative information.
 Normally, such a conversion is not necessary
 because any actor that can accept a DoubleToken can also transparently
 accept a SmoothToken. However, when an input port receives a SmoothToken,
 the port becomes <i>persistent</i>, meaning that it will always have a token.
 If an actor reads from this input port at a time when no SmoothToken has arrived,
 then the most recently received SmoothToken will be extrapolated, using its
 derivative information, to obtain a value at that time. If you wish for a
 downstream port to not be persistent, then you can use this actor to convert
 the signal.  Downstream input ports will be absent at all times except those
 when an actual token is sent.

 @author Thierry S. Nouidui, Christopher Brooks
 @version $Id$
 @Pt.ProposedRating Green (thn)
 @Pt.AcceptedRating Red (thn)
 */
public class SmoothToDouble extends Converter {
    
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SmoothToDouble(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read exactly one token from the input and output the token
     *  the double value of the token if the token is a SmoothToken.
     *  The derivatives array of the SmoothToken is discarded.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        DoubleToken inputToken = (DoubleToken) input.get(0);
        DoubleToken result = new DoubleToken(inputToken.doubleValue());
        if (_debugging) {
            _debug("Transferring input " + inputToken
        	    + " to output " + result
        	    + " at time " + getDirector().getModelTime());
        }
        output.send(0, result);
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasNewToken(0)) {
            if (_debugging) {
        	_debug("No new input at time " + getDirector().getModelTime());
            }
            return false;
        }
        return super.prefire();
    }
}
