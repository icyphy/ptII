/* A server with a fixed or variable service time.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Server
/**
This actor models a server with a fixed or variable service time.
A server is either busy (serving a customer) or not busy at any given time.
If an input arrives when it is not busy, then the input token is produced
on the output with a delay given by the <i>serviceTime</i> parameter.
If an input arrives while the server is busy, then that input is
queued until the server becomes free, at which point it is produced
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
Like the TimedDelay actor, the output is produced with a future time
stamp (larger than current time by <i>serviceTime</i>).  That output
token cannot be retracted once produced, even if the server actor
is deleted from the topology.  If the service time is zero, then
the output event is queued to be processed in the next microstep,
after all events with the current time in the current microstep.
Thus, a service time of zero can be usefully viewed as an infinitesimal
service time.

@see ptolemy.domains.de.lib.TimedDelay
@see ptolemy.domains.de.lib.VariableDelay
@see ptolemy.domains.sdf.lib.SampleDelay

@author Lukito Muliadi, Edward A. Lee
@version $Id$
@since Ptolemy II 0.3
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
    public Server(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        serviceTime =
            new Parameter(this, "serviceTime", new DoubleToken(1.0));
        serviceTime.setTypeEquals(BaseType.DOUBLE);
        newServiceTime = new DEIOPort(this, "newServiceTime", true, false);
        newServiceTime.setTypeEquals(BaseType.DOUBLE);
        output.setTypeAtLeast(input);
//        input.delayTo(output);
//        newServiceTime.delayTo(output);
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
            double time =
                ((DoubleToken)(serviceTime.getToken())).doubleValue();
            if (time < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative service time: " + time);
            }
        } else {
            super.attributeChanged(attribute);
        }
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

    /** Indicate that the server is not busy.
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
        DEDirector director = (DEDirector)getDirector();
        if (director.getCurrentTime() >= _nextTimeFree) {
            return true;
        } else {
            // Schedule a firing if there is a pending
            // token so it can be served.
            if (input.hasToken(0)) {
                director.fireAt(this, _nextTimeFree);
            }
            return false;
        }
    }

    /** If a token was read in the fire() method, then produce it on
     *  the output and schedule a firing to occur when the service time elapses.
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

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        super.removeDependency(input, output);
        super.removeDependency(newServiceTime, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Current inputs.
    private Token _currentInput;

    // Next time the server becomes free.
    private double _nextTimeFree = Double.NEGATIVE_INFINITY;
}
