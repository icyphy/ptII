/* Generate discrete events by periodically sampling a CT signal.

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
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;


//////////////////////////////////////////////////////////////////////////
//// CTPeriodicSampler
/**
This actor periodically sample the input signal and generate events
which has the value of the input signal. The sampling rate is given by
parameter "SamplePeriod", which has default value 0.1.
@author Jie Liu
@version $Id$
*/
public class CTPeriodicSampler extends CTActor
    implements CTEventGenerator {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param CompositeActor The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public CTPeriodicSampler(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output");
        output.setMultiport(false);
        output.setInput(false);
        output.setOutput(true);
        output.setTypeEquals(BaseType.DOUBLE);

        _samplePeriod = (double)0.1;
        SamplePeriod = new Parameter(this,
                "SamplePeriod", new DoubleToken(_samplePeriod));
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    /** The single input port with type double.
     */
    public TypedIOPort input;

    /** The single output port with type double.
     */
    public TypedIOPort output;

    /** The parameter for the sampling period; the type is double; the
     *  default value is 1.0.
     */
    public Parameter SamplePeriod;


    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////


    /** Emit the current event, which has the token of the latest input
     *  token.
     */
    public void emitCurrentEvents() {
        if(_hasCurrentEvent) {
            try {
                if(input.hasToken(0)) {
                    output.broadcast(input.get(0));
                    _hasCurrentEvent = false;
                }
            }catch (IllegalActionException e) {
                throw new InternalErrorException("Token mismatch.");
            }
        }
    }

    /** If the current time is the event time, set the flag indicating
     *  that there is a current event.
     */
    public void fire() {
        CTDirector dir = (CTDirector)getDirector();
        double tnow = dir.getCurrentTime();
        _hasCurrentEvent = false;
        if(Math.abs(tnow - _nextSamplingTime)<dir.getTimeResolution()) {
            _hasCurrentEvent = true;
        }
    }

    /** Return true if there is a current event.
     */
    public boolean hasCurrentEvent() {
        return _hasCurrentEvent;
    }

    /** Request the first sampling time as a director refire.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // clear receivers
        if(input.hasToken(0)) {
            input.get(0);
        }
        _hasCurrentEvent = false;
        updateParameters();
        CTDirector dir = (CTDirector) getDirector();
        _nextSamplingTime = dir.getCurrentTime() + _samplePeriod;
        dir.fireAt(this, _nextSamplingTime);
        if(_debugging) _debug(getFullName() + ": next sampling time = "
                + _nextSamplingTime);
    }

    /** Return true always. If the current time is greater than the next
     *  sampling time, increase the next sample time until it is
     *  greater than the current time. Request a director refire at the
     *  next sampling time.
     *  @return True always.
     *  @exception IllegalActionException If parameter update throws it.
     */
    public boolean prefire() throws IllegalActionException {
        updateParameters();
        CTDirector dir = (CTDirector) getDirector();
        boolean hasjump = false;
        while (_nextSamplingTime <
                (dir.getCurrentTime()-dir.getTimeResolution())) {
            hasjump = true;
            _nextSamplingTime += _samplePeriod;
        }
        if(hasjump) {
            dir.fireAt(this, _nextSamplingTime);
        }
        _debug(getFullName() + ": next sampling time = "
                + _nextSamplingTime);
        return true;
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException If the sampling rate set is
     *  less than or equal to 0.
     */
    public void updateParameters() throws IllegalActionException{
        double p = ((DoubleToken)SamplePeriod.getToken()).doubleValue();
        if(p <= 0) {
            throw new IllegalActionException(this,
                    " Sample period must be greater than 0.");
        }
        _samplePeriod = p;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // the local copy of the sample period.
    private double _samplePeriod;

    // flag indicating if there is a current event.
    private boolean _hasCurrentEvent = false;

    // the next sampling time.
    private double _nextSamplingTime;
}
