/* This actor implements a receiver that adds functionality to another receiver.

@Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.domains.ptides.demo.PtidesNetwork;

import java.util.ArrayList;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
 * Wraps a receiver when a QuantityManager ({@see QuantityManager}) is used 
 * on a relation. Tokens received by this intermediate receiver are forwarded to the QuantityManager 
 * together with the information of the target receiver. The QuantityManager then 
 * forwards the token to the target receiver.
 * 
 * @author Patricia Derler
 */
public class IntermediateReceiver extends AbstractReceiver {
    
    /** Construct an empty receiver with no container.
     * 
     * @param quantityManager The quantity manager that receives tokens received by this receiver.
     * @param receiver The receiver wrapped by this intermediate receiver.
     */
    public IntermediateReceiver(QuantityManager quantityManager, Receiver receiver) {
        _receiver = receiver;
        _quantityManager = quantityManager;
    }
    
    /**
     * Reset the quantity manager.
     */
    public void clear() throws IllegalActionException {
        _quantityManager.reset();
    }

    /**
     * Return the token that was sent last.
     */
    public Token get() throws NoTokenException {
        Token token = _token;
        _token = null;
        return token;
    }

    /**
     * Always return true.
     * 
     * FIXME: QuantityManager should manage buffer sizes.
     */
    public boolean hasRoom() {
        return true;
    }

    /**
     * Always return true.
     * 
     * FIXME: QuantityManager should manage buffer sizes.
     */
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /**
     * Return true if last received token has not been taken.
     */
    public boolean hasToken() {
        return _token != null;
    }

    /**
     * Return true if last received token has not been taken.
     */
    public boolean hasToken(int numberOfTokens) {
        return _token != null; 
    }

    /**
     * Forward token and target receiver to quantity manager and store the token.
     */
    public void put(Token token) throws NoRoomException, IllegalActionException {
        _quantityManager.sendToken(_receiver, token);
        _token = token;
    }
    
    /**
     * Last token that was sent to this receiver and has not been forwarded
     * to the quantityManager.
     */
    private Token _token;
    
    /**
     * Target receiver that is wrapped by this intermediate receiver.
     */
    private Receiver _receiver;
    
    /**
     * Quantity manager that receives tokens from this receiver.
     */
    private QuantityManager _quantityManager;

}
