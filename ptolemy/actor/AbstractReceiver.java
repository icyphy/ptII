/* An abstract implementation of the Receiver interface

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.FIFOQueue;	/* Needed by javadoc */


import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.util.Collections;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// AbstractReceiver
/**
An abstract implementation of the Receiver interface.  The container methods
and some of the more esoteric methids are implemented, while the most
domain-specific methods are left undefined.

@author Steve Neuendorffer
@version $Id$
@see ptolemy.actor.Receiver
*/
public abstract class AbstractReceiver implements Receiver {

    /** Construct an empty receiver with no container.
     */
    public AbstractReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     */
    public AbstractReceiver(IOPort container) {
        super();
	_container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from this receiver. Note that the thrown exception
     *  is a runtime exception.
     *  @exception NoTokenException If there is no token.
     */
    public abstract Token get();

    /** Return the container of this receiver, or null if there is none.
     *  @return The IOPort containing this receiver.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return true if put() will succeed in accepting a token.
     *  @return A boolean indicating whether a token can be put in this
     *   receiver.
     *  @exception IllegalActionException If the Receiver implementation
     *    does not support this query.
     */
    public abstract boolean hasRoom() throws IllegalActionException;

    /** Return true if the receiver has room for putting the given number of 
     *  tokens into it (via the put() method).
     *  Returning true in this method should also guarantee that calling
     *  the put() method will not result in an exception.
     *  In this base class, if the number of tokens equals one, 
     *  then call the zero argument method instead.  If the number of
     *  tokens is greater than 1, then return false, since domains are
     *  not required to provide more than one token.  
     *  @exception IllegalActionException If the Receiver implementation
     *  does not support this query, or the number of tokens is less
     *  than one.
     */
    public boolean hasRoom(int tokens) throws IllegalActionException {
	if(tokens < 1) 
	    throw new IllegalActionException("The number of " + 
					     "tokens must be greater than 0");
	if(tokens == 1) return hasRoom();
	return false;
    }

    /** Return true if get() will succeed in returning a token.
     *  @return A boolean indicating whether there is a token in this
     *   receiver.
     *  @exception IllegalActionException If the Receiver implementation
     *    does not support this query.
     */
    public abstract boolean hasToken() throws IllegalActionException;

    /** Return true if get() will succeed in returning a token the given
     *  number of times.
     *  In this base class, if the number of tokens equals one, 
     *  then call the zero argument method instead.  If the number of
     *  tokens is greater than 1, then return false, since domains are
     *  not required to provide more than one token.  
     *  @return A boolean indicating whether there are the given number of
     *  tokens in this receiver.
     *  @exception IllegalActionException If the Receiver implementation
     *  does not support this query, or the number of tokens is less
     *  than one.
     */
    public boolean hasToken(int tokens) throws IllegalActionException {
	if(tokens < 1) 
	    throw new IllegalActionException("The number of " + 
					     "tokens must be greater than 0");
	if(tokens == 1) return hasRoom();
	return false;
    }
    
    /** Put a token to the receiver. If the receiver is full, throw an
     *  exception.
     *  @param token The token to be put to the receiver.
     *  @exception NoRoomException If the receiver is full.
     */
    public abstract void put(Token token);
    
    /** Set the container.
     *  @param port The IOPort containing this receiver.
     *  @exception IllegalActionException If the container is not of
     *  an appropriate subclass of IOPort.   Not thrown in this base class,
     *  but may be thrown in derived classes.
     */
    public void setContainer(IOPort port) throws IllegalActionException {
        _container = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _container;
}
