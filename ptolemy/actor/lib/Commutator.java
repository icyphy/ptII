/* A polymorphic commutator.

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

@ProposedRating Yellow (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Commutator
/**
A polymorphic commutator, which merges a set of input sequences into
a single output sequence.  The commutator has an input port
(a multiport) and an output port (a single port).
The types of the ports are undeclared and will be resolved by the type
resolution mechanism, with the constraint that the output type must be
greater than or equal to the input type. On each call to the fire method, the
actor reads one token from each input channel and send the token
to the output port. The order in which the tokens are produced
is the order of the channels in the input multiport. If any input
channel has no token, then the fire method returns at that point.
In the next iteration,
the actor will begin reading at this channel that had no input token
in the previous iteration.  If no input token is available on the
first channel being read, then no output is produced.
<p>
For the benefit of domains like SDF, which need to know the token consumption
or production rate for all ports before they can construct a firing schedule,
this actor sets the tokenProductionRate parameter for the output port
to equal the number of input channels.
This parameter is set each time that a link is established with
the input port, or when a link is removed.  The director is notified
that the schedule is invalid, so that if the link is modified at
run time, the schedule will be recalculated if necessary.

@author Mudit Goel, Edward A. Lee
@version $Id$
*/
public class Commutator extends Transformer implements SequenceActor {

    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport. Create
     *  the actor parameters.
     *
     *  @param container The container.
     *  @param name This is the name of this commutator within the container.
     *  @exception NameDuplicationException If an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public Commutator(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(true);
        new Parameter(input, "tokenConsumptionRate", new IntToken(1));
        _productionRate = new Parameter(output,"tokenProductionRate",
                new IntToken(0));
        new Parameter(output,"tokenInitProduction", new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the base
     *  class method and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   attributes that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        Commutator newobj = (Commutator)super.clone(ws);
        newobj._productionRate = (Parameter)
            (newobj.output.getAttribute("tokenProductionRate"));
        return newobj;
    }

    /** Notify this entity that the links to the specified port have
     *  been altered.  This sets the production rate of the output port
     *  and notifies the director that the schedule is invalid, if there
     *  is a director.
     */
    public void connectionsChanged(Port port) {
        if (port == input) {
            try {
                _productionRate.setToken(new IntToken(input.getWidth()));
                Director director = getDirector();
                if (director != null) {
                    director.invalidateSchedule();
                }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex.toString());
            }
        }
    }

    /** Read one token from each input channel and
     *  send it to the output port. If an input
     *  channel has no token, suspend firing and return. In this case,
     *  the actor makes a record of the input channel that it last
     *  attempted to read so that it can start reading at that channel
     *  in the next iteration.
     *  The order in which the tokens are
     *  produced is the order of the channels in the input port.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        _tentativeInputPosition = _currentInputPosition;
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (!input.hasToken(_tentativeInputPosition)) {
                break;
            }
            output.send(0, input.get(_tentativeInputPosition++));
            if (_tentativeInputPosition >= width) {
                _tentativeInputPosition = 0;
            }
        }
    }

    /** Begin execution by setting the current input channel to zero.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentInputPosition = 0;
    }

    /** Update the input position to equal that determined by the most
     *  recent invocation of the fire() method.  The input position is
     *  the channel number of the input port from which the next input
     *  will be read.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _currentInputPosition = _tentativeInputPosition;
        return super.postfire();
    }

    /** Return false if the current input position does not have an
     *  input token.
     *  @return False if the current input position has no token.
     *  @exception IllegalActionException If input.hasToken() throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(_currentInputPosition)) {
            return false;
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The parameter controlling the output port production rate.
    private Parameter _productionRate;

    // The channel number for the next input.
    private int _currentInputPosition;

    // The new channel number for the next input as determined by fire().
    private int _tentativeInputPosition;
}
