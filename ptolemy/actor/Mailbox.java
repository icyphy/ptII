/* A Token holder with capacity one.

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
@ProposedRating Red (liuj@eecs.berkeley.edu)

*/

package pt.actors;
import pt.data.*;
import pt.kernel.Nameable;
import pt.kernel.NoSuchItemException;

//////////////////////////////////////////////////////////////////////////
//// Mailbox
/** 
The Receiver with capacity one. Used in MailboxPort to hold incoming 
token. Implement the Receiver interface.

@author Jie Liu
@version $Id$
*/
public class Mailbox implements Receiver {
    /** Construct an empty Mailbox. The Mailbox must have a container, 
     * which is an IOPort. The container, once set, can't be changed.
     * FIXME: what if container = null?
     * @param container 
     */	
    public Mailbox(IOPort container) {
        _container = container;
        _isEmpty = true;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Get the contained Token.
     * FIXME: synchronized on workspace?
     * @see put
     * @return token
     * @exception NoSuchItemException if the Mailbox is empty.
     */	
    public Token get() throws NoSuchItemException {
        if(_isEmpty) {
            throw new NoSuchItemException(getContainer(),
            "Attempt to get data from an empty mailbox.");
        }
        _isEmpty = true;
        return _token;
    }

    /** return the IOPort that contains this Mailbox
     * @return container
     */	
    public Nameable getContainer() {
        return _container;
    }

    /** Return ture if the Mailbox is empty.
     * @return ture if the Mailbox is empty.
     */	
    public boolean isEmpty() {
        return _isEmpty;
    }

    /** Put a token in the Mailbox
     * @see get
     * @param token The token to be put in.
     * @exception TokenHolderFullException if the Mailbox has 
     *            unconsumed token.
     */	
    public void put(Token token) throws TokenHolderFullException{
        if(!_isEmpty) {
            throw new TokenHolderFullException();
        }
        _token = token;
        _isEmpty = false;    
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // container is not changable.
    private final IOPort _container;
    // the token held.
    private Token _token;
    private boolean _isEmpty;
}
