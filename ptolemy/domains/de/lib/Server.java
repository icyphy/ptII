/* A server with a fixed service time.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;

//////////////////////////////////////////////////////////////////////////
//// Server
/**
This actor models a server with a fixed or variable service time.
If an input arrives when it is not busy, then the input token is produced
on the output with a delay given by the <i>serviceTime</i> parameter.
If an input arrives while the server is busy, then that input is
ignored until the server becomes free, at which point it is produced
on the output with a delay given by the <i>serviceTime</i> parameter.
If several inputs arrive while the server is busy, then they are
served on a first-come, first-served basis.
<p>
If the <i>serviceTime</i> parameter is not set, it defaults to 1.0.
The value of the parameter can be changed at any time during execution
of the model by providing an input event at the <i>newServiceTime</i>
input port.  The token read at that port replaces the value of the
<i>serviceTime</i> parameter.
<p>
This actor declares that there is delay between the <i>input</i>
and the <i>output</i> ports and between <i>newServiceTime</i>
and <i>output</i>.  The director uses this information for
assigning priorities to firings.
<p>
Like the Delay actor, the output is produced with a future time
stamp (larger than current time by <i>serviceTime</i>).  That output
token cannot be retracted once produced, even if the server actor
is deleted from the topology.  If the service time is zero, then
the output event is queued to be processed in the next microstep,
after all events with the current time in the current microstep.
Thus, a service time of zero can be usefully viewed as an infinitessimal
service time.

@author Lukito Muliadi, Edward A. Lee
@version $Id$
@see Delay
*/
public class Server extends DETransformer {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Server(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        serviceTime =
            new Parameter(this, "serviceTime", new DoubleToken(1.0));
        serviceTime.setTypeEquals(BaseType.DOUBLE);
        newServiceTime = new DEIOPort(this, "newServiceTime", true, false);
        newServiceTime.setTypeEquals(BaseType.DOUBLE);
        output.setTypeAtLeast(input);
        input.delayTo(output);
        newServiceTime.delayTo(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** A port to supply new service times.  This port has type DoubleToken.
     */
    public DEIOPort newServiceTime;

    /** The service time.  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter serviceTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>serviceTime</i>, then check that
     *  the value is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTime) {
            if (((DoubleToken)(serviceTime.getToken())).doubleValue()
                    < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative service time.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Server newobj = (Server)super.clone(ws);
        newobj.serviceTime = (Parameter)newobj.getAttribute("serviceTime");
        newobj.newServiceTime = (DEIOPort)newobj.getPort("newServiceTime");
        return newobj;
    }

    /** If there is an input token, read it to begin servicing it.
     *  Note that service actually begins in the postfire() method,
     *  which will produce the token that is read on the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (newServiceTime.getWidth() > 0 && newServiceTime.hasToken(0)) {
            DoubleToken time = (DoubleToken)(newServiceTime.get(0));
            serviceTime.setToken(time);
        }
        if (input.getWidth() > 0 && input.hasToken(0)) {
            _currentInput = input.get(0);
            double delay =
                ((DoubleToken)serviceTime.getToken()).doubleValue();
            _nextTimeFree = ((DEDirector)getDirector()).getCurrentTime()
                + delay;
        } else {
            _currentInput = null;
        }
    }

    /** Indicate that the server is free.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextTimeFree = Double.NEGATIVE_INFINITY;
    }

    /** If the server is not busy, return true.  Otherwise return false.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        if (dir.getCurrentTime() >= _nextTimeFree) {
            return true;
        } else {
            // Schedule a firing if there is a pending
            // token so it can be served.
            if (input.hasToken(0)) {
                dir.fireAt(this, _nextTimeFree);
            }
            return false;
        }
    }

    /** Produce token that was read in the fire() method, if one was read,
     *  and schedule a firing when the service time elapses.
     *  The output is produced with a time offset equal to the value
     *  of the <i>serviceTime</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        if (_currentInput != null) {
            double delay =
                ((DoubleToken)serviceTime.getToken()).doubleValue();
            output.send(0, _currentInput, delay);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Current inputs.
    private Token _currentInput;

    // Next time the server becomes free.
    private double _nextTimeFree = Double.NEGATIVE_INFINITY;
}
