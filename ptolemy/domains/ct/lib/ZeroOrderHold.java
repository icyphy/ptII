/* An actor that hold the last event and outputs a constant signal.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.actor.lib.Transformer;

//////////////////////////////////////////////////////////////////////////
//// ZeroOrderHold
/**
Convert discrete events at the input to a continuous-time
signal at the output by holding the value of the discrete
event until the next discrete event arrives.

@author Jie Liu
@version $Id$
*/

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
    public ZeroOrderHold(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeSameAs(input);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        ZeroOrderHold newObject = (ZeroOrderHold)super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** consume the input event if there is any. This event will be
     *  hold for further firings until this method is called again.
     *  If there is no input event, do nothing and
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

    /** Output the latest token consumed from the consumeCurrentEvents()
     *  call.
     *  @exception IllegalActionException If the token cannot be sent.
     */
    public void fire() throws IllegalActionException{
        output.send(0, _lastToken);
    }

    /** Initialize token. If there is no input, the initial token is
     *  a zero Double Token.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException{
        super.initialize();
        Type outtype = output.getType();
        if (outtype.equals(BaseType.BOOLEAN)) {
            _lastToken = new BooleanToken(false);
        } else if(outtype.equals(BaseType.DOUBLE)) {
            _lastToken = new DoubleToken(0.0);
        } else if(outtype.equals(BaseType.INT)) {
            _lastToken = new IntToken(0);
        } else {
            _lastToken = new StringToken("");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Saved token.
    private Token _lastToken;
}
