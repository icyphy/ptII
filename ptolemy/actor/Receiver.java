/* Interface for objects that can put and get tokens.

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
//// Receiver
/** 
Interface for objects that can hold tokens. The implementation of this 
interface should support two key methods: put and get. Put is the method
that put a token in. Get is the method that can get a token out. The order 
the tokens are stored and the order of retreiving depends on specific
implementations. In addtion, objects that implement this method are supposed
to be contained in an IOPort. So the getContainer method is defined.

@author Jie Liu
@version $Id$
*/
public interface Receiver {

    /////////////////////////////////////////////////////////////////////
    ////                      public methods                         ////

    /** Get a token from the object
     * @exception NoSuchItemException Thrown by derived classes.
     */
    public Token get() throws NoSuchItemException;

    /** Return the container (IOPort) of the object.*/
    public Nameable getContainer();

    /** Put a token into the object
     * @exception TokenHolderFullException Thrown by derived classes.
     */
    public void put(Token t) throws TokenHolderFullException;

}
