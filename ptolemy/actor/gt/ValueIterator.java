/* An interface for parameters whose accepted values can be iterated from the
initial value with the next method.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ValueIterator

/**
 An interface for parameters whose accepted values can be iterated from the
 initial value with the next method.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface ValueIterator extends Settable {

    /** Get the current value of the parameter that implements this interface.
     *
     *  @return The current value.
     *  @exception IllegalActionException Throws if the current value cannot be
     *   obtained.
     *  @see #setToken(Token)
     */
    public Token getToken() throws IllegalActionException;

    /** Set the parameter that implements this interface with its initial value,
     *  and return that value.
     *
     *  @return The initial value.
     *  @exception IllegalActionException Thrown when trying to compute the
     *  initial value or to set the parameter with that value.
     */
    public Token initial() throws IllegalActionException;

    /** Set the parameter that implements this interface with its next value,
     *  and return that value. If the initial value has not been set with {@link
     *  #initial()}, or no next value exists, {@link IllegalActionException} is
     *  thrown.
     *
     *  @return The next value, or null.
     *  @exception IllegalActionException Thrown when trying to compute the next
     *  value or to set the parameter with that value, or if the initial value
     *  has not been set, or no next value exists.
     */
    public Token next() throws IllegalActionException;

    /** Set the value of the parameter that implements this interface. This
     *  method should be used with care because the value may not be one of the
     *  values that can be obtained by repeatedly calling the {@link #next()}
     *  method starting from the initial value.
     *
     *  @param token The value to be set.
     *  @exception IllegalActionException Thrown when trying to set the value.
     *  @see #getToken()
     */
    public void setToken(Token token) throws IllegalActionException;

}
