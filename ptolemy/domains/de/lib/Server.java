/* A server with a fixed or variable service time.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import java.util.LinkedList;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

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
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Yellow (cxh)
*/
public class Server extends VariableDelay {

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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is an input token, read it to begin servicing it.
     *  Note that service actually begins in the postfire() method,
     *  which will produce the token that is read on the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        // update delay value
        delay.update();
        _delay = ((DoubleToken)delay.getToken()).doubleValue();
        
        Time currentTime = getDirector().getCurrentTime();
        // consume input and put it into the task queue: _delayedTokensList
        // NOTE: it is different from the _delayedTokens defined in the 
        // TimedDelay class. 
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
            _delayedTokensList.addLast(_currentInput);
        } else {
            _currentInput = null;
        }
        // produce output
        if (currentTime.compareTo(_nextTimeFree) == 0) {
            _currentOutput = (Token)_delayedTokens.get(
                new Double(currentTime.getTimeValue()));
            if (_currentOutput == null) {
                throw new InternalErrorException("Service time is " +
                    "reached, but output is not available.");
            }
            output.send(0, _currentOutput);
        }
    }

    /** Indicate that the server is not busy.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextTimeFree = getDirector().timeConstants.NEGATIVE_INFINITY;
        _delayedTokensList = new LinkedList();
    }

    /** If a token was read in the fire() method, then produce it on
     *  the output and schedule a firing to occur when the service time elapses.
     *  The output is produced with a time offset equal to the value
     *  of the <i>serviceTime</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getCurrentTime();

        // Remove the curent output token from _delayedTokens.
        // NOTE: In this server class, the _delayedTokens can have
        // at most one token inside (like a processer can execute
        // at most one process at any time.) 
        if (_currentOutput != null) {
            _delayedTokens.remove(new Double(currentTime.getTimeValue()));
        }
        // If the delayedTokensList is not empty, and the delayedTokens
        // is empty (ready for a new service), get the first token
        // and put it into service. Schedule a refiring to wave up 
        // after the service finishes.
        if (_delayedTokensList.size() != 0 && _delayedTokens.isEmpty()) {
            _nextTimeFree = currentTime.add(_delay);
            _delayedTokens.put(new Double(_nextTimeFree.getTimeValue()), 
                _delayedTokensList.removeFirst());
            getDirector().fireAt(this, _nextTimeFree);
        }
        return !_stopRequested;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    // Initialize parameters.
    protected void _init() 
        throws NameDuplicationException, IllegalActionException  {
        // FIXME: can not call super._init() and delay.setName(newName).
        // The port does not get the new name. This is a bug.
        delay = new PortParameter(this, "serviceTime");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);
        // Put the delay port at the bottom of the icon by default.
        StringAttribute cardinality
                = new StringAttribute(delay.getPort(), "_cardinal");
        cardinality.setExpression("SOUTH");
        output.setTypeSameAs(input);
    }

    // Update the private states and schedule future firings.
    protected void _updateStates() throws IllegalActionException {
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Next time the server becomes free.
    private Time _nextTimeFree;
    
    // List of delayed tokens, whose finishing times can not be decided.
    private LinkedList _delayedTokensList;
}
