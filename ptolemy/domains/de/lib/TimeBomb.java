/* An actor that sets a time bomb and fires when the time is up.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.math.Complex;

import java.util.Hashtable;

//////////////////////////////////////////////////////////////////////////
//// TimeBomb
/**
This actor works similar to a Delay actor, but the amount of delay
(or the firing time in the future) is computed from the input token 
rather than a paramter.
The actor has an input port that taks a double number which is a future
time. When that time is reached, a token is produced from the output.
The produced token is set by the parameter "bombValue".
<p>
The behavior on each firing is to read a token from the input,
if there is one, and registers a refire to the director at that time.
When the time is reached, it produces the token specified in the 
parameter. 

@author Jie Liu
@version $Id$
*/
public class TimeBomb extends DETransformer {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimeBomb(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.delayTo(output);
        input.setTypeEquals(BaseType.COMPLEX);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        TimeBomb newobj = (TimeBomb)super.clone(ws);
        newobj.input.setTypeEquals(BaseType.COMPLEX);
        newobj.output.setTypeEquals(BaseType.DOUBLE);
        try {
            newobj.input.delayTo(newobj.output);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Clone failed.");
        }
        return newobj;
    }

    /** Read one token from the input and register for future firing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Complex currentInput = ((ComplexToken)input.get(0)).complexValue();
            double time = currentInput.real;
            Double bombTime = new Double(time);
            DoubleToken bombValue = new DoubleToken(currentInput.imag);
            _tokens.put(bombTime, bombValue);
            getDirector().fireAt(this, time);
        } 
    }

    /** If the current time is one of the time bomb triggering time,
     *  produces the corresponding data.
     *  
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        double time = getDirector().getCurrentTime();
        Double currentTime = new Double(time);
        if (_tokens.containsKey(currentTime)) {
            output.send(0, (Token)_tokens.remove(currentTime));
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The hashtable stores all the setted time bombs.
    private Hashtable _tokens = new Hashtable();
}
