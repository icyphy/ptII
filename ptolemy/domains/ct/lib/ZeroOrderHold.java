/* An actor that hold the last event and outputs a constant signal.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.lib.Transformer;

//////////////////////////////////////////////////////////////////////////
//// ZeroOrderHold
/**
An actor that converts event into continuous signal. This class act
as the zero order hold. It consume the token when the consumeCurrentEvent()
is called. This value will be hold and emitted every time it is
fired, until the next consumeCurrentEvent() is called. This actor has one
single input port of type DoubleToken, one single output port of type
DoubleToken, and no parameter.

@author Jie Liu
@version $Id$
*/

//FIXME: Consider make it type polymorphic.

public class ZeroOrderHold extends Transformer
    implements CTWaveformGenerator{

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ZeroOrderHold(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);        
        // FIXME: Are they always DOUBLE? 
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
     public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        ZeroOrderHold newobj = (ZeroOrderHold)super.clone(ws);
        newobj.input.setTypeEquals(BaseType.DOUBLE);
        newobj.output.setTypeEquals(BaseType.DOUBLE);
        return newobj;
    }


    /** consume the input event if there is any. This event will be
     *  hold for further firings until this method is called for the
     *  next time. If there is no input event, do nothing and
     *  the old token will be held.
     */
    public void consumeCurrentEvents() throws IllegalActionException{
        if(input.hasToken(0)) {
            _lastToken = input.get(0);
            CTDirector dir = (CTDirector) getDirector();
            _debug(getFullName() + " receives an event at: " +
                    dir.getCurrentTime() +
                    " with token " + _lastToken.toString());
        }
    }

    /** Output a DoubleToken of the last event token.
     *  @exception IllegalActionException If the token cannot be
     *  broadcasted.
     */
    public void fire() throws IllegalActionException{
        output.broadcast(_lastToken);
    }

    /** Initialize token. If there is no input, the initial token is
     *  a zero Double Token.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException{
        super.initialize();
        _lastToken = new DoubleToken(0.0);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Saved token.
    private Token _lastToken;
}
