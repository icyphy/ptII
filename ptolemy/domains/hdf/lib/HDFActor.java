/* A prototype actor that shows how rate can be changed during execution.

 Copyright (c) 2003 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.hdf.kernel.HDFDirector;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// HDFActor
/**
This actor is a prototype actor that show how rate can be changed and the
schedule can be re-computed correctly. The consumption rate of the input
port and the production rate of the output port are same, both set by the
portParameter <i>rate</i>. At the end of each iteration, the actor takes
the most recent tokens from its port, according to which the director that
contains it (which should be HDFDirector) will re-compute the schedule.
Tokens received by the portParameter <i>rate</i> during other postfires will
be ignored. The schedule cannot be changed in the middle of one iteration.
<p>
@author Rachel Zhou
@version $Id$
@since Ptolemy II 3.1
*/
public class HDFActor extends Transformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HDFActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        rate = new PortParameter(this, "rate");
        rate.setTypeEquals(BaseType.INT);
        rate.setExpression("1");

        // Declare data types, consumption rate and production rate.
        _inputRate = new Parameter(input, "tokenConsumptionRate",
                new IntToken(1));
        _outputRate = new Parameter(output, "tokenProductionRate",
                new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Consumption and production rate of this actor.
     */
    public PortParameter rate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>rate</i>, then verify
     *  that it is a positive integer.
     *  @exception IllegalActionException If <i>rate</i> is not a
     *  positive integer.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rate) {
            int rateValue = ((IntToken)rate.getToken()).intValue();
            if (rateValue < 0 ) {
                throw new IllegalActionException(this,
                        "rate must be non-negative.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read <i>_rateValue</i> tokens from the input port and produce
     *  them at the output port.
     */
    public void fire() throws IllegalActionException {

        Director director = getDirector();
        if (director instanceof HDFDirector) {
            Scheduler scheduler =
                ((SDFDirector)director).getScheduler();
            //_firingCount =
              //  ((HDFDirector)director).getDirectorFiringsPerIteration()
                //    * ((SDFScheduler)scheduler).getFiringCount(this);

        }
        Token[] inputToken = (Token[])input.get(0, _rateValue);
        output.broadcast(inputToken, _rateValue);
    }

    /** If it is end of one complete iteration, the postfire method
     *  takes in the most recent token from the <i>rate</i>
     *  portParameter, and invalidate the schedule.
     *  @exception IllegalActionException If the base class throws it
     */
    public boolean postfire() throws IllegalActionException {
        //_firingSoFar ++ ;
        rate.update();
        int rateValue = ((IntToken)rate.getToken()).intValue();
        Director director = getDirector();
        CompositeActor container = (CompositeActor)getContainer();
        if (director instanceof HDFDirector) {
            if (_requestChange){
                _requestChange = false;
                ChangeRequest request =
                    new ChangeRequest(this, "change rates") {
                    protected void _execute() throws KernelException {
                        Director director = getDirector();
                       ((HDFDirector)director).invalidateSchedule();
                       int rateValue = ((IntToken)rate.getToken()).intValue();
                       _rateValue = rateValue;
                       _outputRate.setToken(new IntToken(_rateValue));
                       _inputRate.setToken(new IntToken(_rateValue));
                    _requestChange = true;
                    }
               };
               request.setPersistent(false);
               container.requestChange(request); 
            } else {
                _outputRate.setToken(new IntToken(_rateValue));
                _inputRate.setToken(new IntToken(_rateValue));
            }
        }
        return super.postfire();
    }

    /** Preinitialize the actor by setting the port rate in the first
     *  iteration to be the initial value of the <i>rate</i> portParameter.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _rateValue = ((IntToken)rate.getToken()).intValue();
        _outputRate.setToken(new IntToken(_rateValue));
        _inputRate.setToken(new IntToken(_rateValue));
        _requestChange = true;
        //Director director = getDirector();
        //director.invalidateSchedule();
        super.preinitialize();
    }

    //////////////////////////////////////////////////////////////
    ////           private variables                          ////

    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // The value of token received from the portParameter.
    // It does get updated if it is in the middle of one iteration.
    private int _rateValue;

    // Number of firings so far in one iteration.
    private boolean _requestChange = true;

    // Number of firings of this actor per iteration.
    //private int _firingCount;
}
