/* Interface for objects that can store tokens.

 Copyright (c) 1997- The Regents of the University of California.
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

*/

package pt.actor;
import pt.data.*;
import pt.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Receiver
/**
Interface for objects that can hold tokens. An implementation of this
interface has two key methods: put and get. Put deposits a token into the
receiver. Get retrieves a token that has been put. The order of
the retrieved tokens depends on specific implementations, and does not
necessarily match the order in which tokens have been put.
<p>
In addition, objects that implement this interface can only be contained
by an instance of IOPort.

@author Jie Liu, Edward A. Lee
@version $Id$
*/
public interface Receiver {

    /////////////////////////////////////////////////////////////////////
    ////                      public methods                         ////

    /** Get a token from this receiver.
     *  @exception NoSuchItemException If there is no token.
     */
    public Token get() throws NoSuchItemException;

    /** Return the container. */
    public Nameable getContainer();

    /** Put a token into this receiver.
     *  @exception IllegalActionException If the token cannot be put.
     */
    public void put(Token t) throws IllegalActionException;

    /** Set the container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort.
     */
    public void setContainer(IOPort port) throws IllegalActionException;
}
