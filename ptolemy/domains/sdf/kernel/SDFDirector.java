/* Director for the synchronous dataflow model of computation.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Green (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DependencyDeclaration;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SDFDirector
/**
<h1>SDF overview</h1>
The Synchronous Dataflow(SDF) domain supports the efficient
execution of Dataflow graphs that
lack control structures.   Dataflow graphs that contain control structures
should be executed using the Process Networks(PN) domain instead.
SDF allows efficient execution, with very little overhead at runtime.  It
requires that the rates on the ports of all actors be known before hand.
SDF also requires that the rates on the ports not change during
execution.  In addition, in some cases (namely systems with feedback) delays,
which are represented by initial tokens on relations must be explicitly
noted.  SDF uses this rate and delay information to determine
the execution sequence of the actors before execution begins.
<h2>Schedule Properties</h2>
<ul>
<li>The number of tokens accumulated on every relation is bounded, given
an infinite number of executions of the schedule.
<li>Deadlock will never occur, given an infinite number of executions of
the schedule.
</ul>
<h1>Class comments</h1>
An SDFDirector is the class that controls execution of actors under the
SDF domain.  By default, actor scheduling is handled by the SDFScheduler
class.  Furthermore, the newReceiver method creates Receivers of type
SDFReceiver, which extends QueueReceiver to support optimized gets
and puts of arrays of tokens.
<p>
Actors are assumed to consume and produce exactly one token per channel on
each firing.  Actors that do not follow this convention should set
the appropriate parameters on input and output ports to declare the number
of tokens they produce or consume.  See the
@link ptolemy.domains.sdf.kernel.SDFScheduler for more information.
The @link ptolemy.domains.sdf.lib.SampleDelay actor is usually used
in a model to specify the delay across a relation.
<p>
The <i>allowDisconnectedGraphs</i> parameter of this director determines
whether disconnected graphs are permitted.
A model may have two or more graphs of actors that
are not connected.  The schedule can jump from one graph to
another among the disconnected graphs. There is nothing to
force the scheduler to finish executing all actors on one
graph before firing actors on another graph. However, the
order of execution within an graph should be correct.
Usually, disconnected graphs in an SDF model indicates an
error.
The default value of the allowDisconnectedGraphs parameter is a
BooleanToken with the value false.
<p>
The <i>iterations</i> parameter of this director corresponds to a
limit on the number of times the director will fire its hierarchy
before it returns false in postfire.  If this number is not greater
than zero, then no limit is set and postfire will always return true.
The default value of the iterations parameter is an IntToken with value zero.
<p>
The <i>vectorizationFactor</i> parameter of this director sets the number
of times that the basic schedule is executed during each firing of this
director.  This might allow the director to execute the model more efficiently,
by combining multiple firings of each actor.  The default value of the
vectorizationFactor parameter is an IntToken with value one.


@see ptolemy.domains.sdf.kernel.SDFScheduler
@see ptolemy.domains.sdf.kernel.SDFReceiver

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class SDFDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     */
    public SDFDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     *
     *  @param workspace The workspace for this object.
     */
    public SDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A parameter representing whether disconnected graphs are
     *  permitted.  A model may have two or more graphs of actors that
     *  are not connected.  The schedule can jump from one graph to
     *  another among the disconnected graphs. There is nothing to
     *  force the scheduler to finish executing all actors on one
     *  graph before firing actors on another graph. However, the
     *  order of execution within an graph should be correct.
     *  Usually, disconnected graphs in an SDF model indicates an
     *  error.  The default value is a BooleanToken with the value
     *  false.
     */
    public Parameter allowDisconnectedGraphs;

    /** A parameter representing whether dynamic rate changes are
     *  permitted.  An SDF model may constructed such that the values
     *  of rate parameters are modified during the execution of the
     *  system.  If this parameter is false, then such models are
     *  valid and this class dynamically computes a new schedule at
     *  runtime.  If this parameter is true, then the SDF domain
     *  performs a static check to disallow such models.  Note that in
     *  order to generate code from an SDF model, this parameter must
     *  be set to true.  The default value is a BooleanToken with the
     *  value true.
     */
    public Parameter allowRateChanges;

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever. Note that the amount
     *  of data processed by the SDF model is a function of both this
     *  parameter and the value of parameter <i>vectorizationFactor</i>, since
     *  <i>vectorizationFactor</i> can influence the choice of schedule.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** A Parameter representing the requested vectorization factor.
     *  The director will attempt to construct a schedule where each
     *  actor fires <i>vectorizationFactor</i> times more often than
     *  it would in a minimal schedule.  This can allow actor executions
     *  to be grouped together, resulting in faster execution.  This is
     *  more likely to be possible in graphs without tight feedback.
     *  This parameter must be a positive integer.
     *  The default value is an IntToken with the value one.
     */
    public Parameter vectorizationFactor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == allowDisconnectedGraphs
                || attribute == vectorizationFactor ) {
            invalidateSchedule();
        }
        super.attributeChanged(attribute);
    }

    /** Initialize the actors associated with this director and then
     *  set the iteration count to zero.  The order in which the
     *  actors are initialized is arbitrary.  In addition, if actors
     *  connected directly to output ports have initial production,
     *  then copy that initial production to the outside of the
     *  composite actor.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;

        CompositeActor container = (CompositeActor)getContainer();
        for (Iterator ports = container.outputPortList().iterator();
             ports.hasNext();) {
            IOPort port = (IOPort)ports.next();

            // Create external initial production.
            int rate = SDFUtilities.getTokenInitProduction(port);
            boolean wasTransferred = false;
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    for (int k = 0; k < rate; k++) {
                        if (port.hasTokenInside(i)) {
                            Token t = port.getInside(i);
                            if (_debugging) _debug(getName(),
                                    "transferring output from "
                                    + getName());
                            port.send(i, t);
                            wasTransferred = true;
                        } else {
                            throw new IllegalActionException(this, port,
                                    "Port should produce " + rate
                                    + " tokens, but there were only "
                                    + k + " tokens available.");
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }
    }

    /** Return a new receiver consistent with the SDF domain.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do.  If there are no input ports, then also return true.
     *  Otherwise, return false.  Note that this does not call prefire()
     *  on the contained actors.
     *  @exception IllegalActionException If port methods throw it.
     *  @return true If all of the input ports of the container of this
     *  director have enough tokens.
     */
    public boolean prefire() throws IllegalActionException {
        // Set current time based on the enclosing model.
        super.prefire();

        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        Iterator inputPorts = container.inputPortList().iterator();
        int inputCount = 0;
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            // NOTE: If the port is a ParameterPort, then we should not
            // insist on there being an input.
            if (inputPort instanceof ParameterPort) continue;

            int threshold = SDFUtilities.getTokenConsumptionRate(inputPort);
            if (_debugging) {
                _debug("checking input " + inputPort.getFullName());
                _debug("Threshold = " + threshold);
            }

            for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                if (threshold > 0
                        && !inputPort.hasToken(channel, threshold)) {
                    if (_debugging) {
                        _debug("Port " + inputPort.getFullName()
                                + " does not have enough tokens: "
                                + threshold
                                + " Prefire returns false.");
                    }
                    return false;
                }
            }
        }
        if (_debugging) _debug("Director prefire returns true." );
        return true;
    }

    /** Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly, since the act of computing the
     *  schedule sets the rate parameters of the external ports.  In
     *  addition, performing scheduling during preinitialization
     *  enables it to be present during code generation.  The order in
     *  which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
              
        SDFScheduler scheduler = (SDFScheduler)getScheduler();
        if (scheduler == null)
            throw new IllegalActionException("Attempted to initialize " +
                    "SDF system with no scheduler");
        // force the schedule to be computed.
        if (_debugging) _debug("Computing schedule");
        try {
            Schedule sched = scheduler.getSchedule();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to compute schedule:");
        }

        // Declare the dependencies of rate parameters of external
        // ports.  Note that this must occurs after scheduling, since
        // rate parameters are assumed to exist.
        ConstVariableModelAnalysis analysis =
            ConstVariableModelAnalysis.getAnalysis(this);
        for(Iterator ports = 
                ((CompositeActor)getContainer()).portList().iterator();
            ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            if(!(port instanceof ParameterPort)) {
                if(port.isInput()) {
                    _declareDependency(analysis, port, "tokenConsumptionRate",
                            scheduler._rateVariables);
                } 
                if(port.isOutput()) {
                    _declareDependency(analysis, port, "tokenProductionRate",
                            scheduler._rateVariables);
                    _declareDependency(analysis, port, "tokenInitProduction",
                            scheduler._rateVariables);
                }
            }
        }
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.
     *  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the iterations parameter
     *  does not contain a legal value.
     */
    public boolean postfire() throws IllegalActionException {
        int iterationsValue = ((IntToken) (iterations.getToken())).intValue();
        _iterationCount++;
        if ((iterationsValue > 0) && (_iterationCount >= iterationsValue)) {
            _iterationCount = 0;
            return false;
        }
        return super.postfire();
    }

    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.  If there are not enough tokens,
     *  then throw an exception.  If the port is not connected on the
     *  inside, or has a narrower width on the inside than on the outside,
     *  then consume exactly one token from the corresponding outside
     *  channels and discard it.  Thus, a port connected on the outside
     *  but not on the inside can be used as a trigger for an SDF
     *  composite actor.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, or if there are not enough input tokens available.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque" +
                    "input port.");
        }
        int rate = SDFUtilities.getTokenConsumptionRate(port);
        boolean wasTransferred = false;
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {
                    for (int k = 0; k < rate; k++) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);
                            if (_debugging) _debug(getName(),
                                    "transferring input from "
                                    + getName());
                            port.sendInside(i, t);
                            wasTransferred = true;
                        } else {
                            throw new IllegalActionException(this, port,
                                    "Port should consume "
                                    + rate
                                    + " tokens, but there were only "
                                    + k
                                    + " tokens available.");
                        }
                    }
                } else {
                    // No inside connection to transfer tokens to.
                    // In this case, consume one input token if there is one.
                    if (port.hasToken(i)) {
                        port.get(i);
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return wasTransferred;
    }

    /** Override the base class method to transfer enough tokens to
     *  fulfill the output production rate.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port. If any channel of the output port has no data, then
     *  that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                    + "is not an opaque input port.");
        }
        int rate = SDFUtilities.getTokenProductionRate(port);
        boolean wasTransferred = false;
        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                for (int k = 0; k < rate; k++) {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        if (_debugging) _debug(getName(),
                                "transferring output from "
                                + getName());
                        port.send(i, t);
                        wasTransferred = true;
                    } else {
                        throw new IllegalActionException(this, port,
                                "Port should produce " + rate
                                + " tokens, but there were only "
                                + k + " tokens available.");
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return wasTransferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return false.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add a DependencyDeclaration (with the name
     * "_SDFRateDependencyDeclaration") to the variable with the given
     * name in the given port that declares the variable is dependent
     * on the given list of variables.  If a dependency declaration
     * with that name already exists, then simply set its dependents
     * list to the given list.
     */
    protected void _declareDependency(ConstVariableModelAnalysis analysis,
            Port port, String name, List dependents) 
            throws IllegalActionException {
        Variable variable = 
            (Variable)SDFUtilities._getRateVariable(port, name);
        DependencyDeclaration declaration = (DependencyDeclaration)
            variable.getAttribute(
                    "_SDFRateDependencyDeclaration", 
                    DependencyDeclaration.class);
        if(declaration == null) {
            try {
                declaration = new DependencyDeclaration(variable, 
                        "_SDFRateDependencyDeclaration");
            } catch (NameDuplicationException ex) {
                // Ignore... should not happen.
            }
        }
        declaration.setDependents(dependents);
        analysis.addDependencyDeclaration(declaration);
    }

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     */
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        SDFScheduler scheduler =
            new SDFScheduler(this, uniqueName("Scheduler"));

        allowDisconnectedGraphs = new Parameter(this,
                "allowDisconnectedGraphs", new BooleanToken(false));
        allowDisconnectedGraphs.setTypeEquals(BaseType.BOOLEAN);

        allowRateChanges = new Parameter(this,
                "allowRateChanges", new BooleanToken(false));
        allowRateChanges.setTypeEquals(BaseType.BOOLEAN);

        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);
        vectorizationFactor =
            new Parameter(this, "vectorizationFactor", new IntToken(1));
        vectorizationFactor.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iterationCount = 0;
}
