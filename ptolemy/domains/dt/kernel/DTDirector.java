/* Discrete Time (DT) domain director.

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

@ProposedRating Yellow (chf@eecs.berkeley.edu)
@AcceptedRating Yellow (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.dt.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.domains.sdf.lib.SampleDelay;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// DTDirector
/**
The Discrete Time (DT) domain director.

<h1>DT overview</h1>

The Discrete Time (DT) domain is a timed extension of the Synchronous
Dataflow (SDF) domain.  Like SDF, it has static scheduling of the
dataflow graph model. Similarly, DT requires that the data rates on
the ports of all actors be known beforehand and fixed. DT handles
feedback systems in the same way that SDF does, but with additional
constraints on initial tokens.  <p>

<h1>Local and Global Time</h1>

Because of the inherent concurrency occurring within SDF models, there
are two notions of time in DT -- global time and local time.  Global
time increases steadily as execution progresses.  Moreover, global
time increases by fixed discrete amounts given by the <i>period</i>
parameter. On the other hand, each receiver is associated with an
independent local time. All the receivers have distinct local times as
an iteration proceeds. The local time of a receiver during an
iteration depends on the global time, period, firing count, port
rates, and the schedule. These local times obey the following
constraint:

<center>Global Time  <=  Local Time <= (Global Time + period)</center>

The exact way that local time increments during an iteration is described in
detail in the DTReceiver documentation.
<p>
<h1> Period Parameter </hi>

The DT director has a <i>period</i> parameter which specifies the
amount of time per iteration. For hierarchical DT, this period
parameter only makes sense on the top-level. The user cannot
explicitly set the period parameter for a DT subsystem inside another
DT system. For heterogeneous hierarchies (e.g. DT inside DE or DT
inside CT), the period parameter specifies the time interval between
firings of the DT subsystem. The DT subsystem will not fire on times
that are not integer multiples of the period parameter.

<p>.
<h1>DT Features</h1>
The design of the DT domain is motivated by the following criteria:
<OL>

<LI>) Uniform Token Flow: The time interval between tokens should be
regular and unchanging.  This conforms to the idea of having sampled
systems with fixed rates. Although the tokens flowing in DT do not
keep internal time stamps, each actor can query the DT director for
its own local time.  This local time is uniformly increasing by a
constant fraction of the director's <i>period</I>.  Local time is
incremented every time the get() method is called to obtain a token.

<LI>) Causality: Tokens produced by an actor should only depend on
tokens produced or consumed in the past. This makes sense because we
don't expect an actor to produce a token before it can calculate the
token's value.  For example, if an actor needs three tokens A, B, and
C to compute token D, then the time when tokens A, B, and C are
consumed should be earlier than or equal to the time when token D is
produced.  Note that in DT, time does not get incremented due to
computation.  <LI>) SDF-style semantics: Ideally, we want DT to be a
timed-superset of SDF with compatible token flow and scheduling.
However, we can only approximate this behavior. It is not possible to
have uniform token flow, causality, and SDF-style semantics at the
same time.  Causality breaks for non-homogeneous actors in a feedback
system when fully-compatible SDF-style semantics is adopted.  To
remedy this situation, every actor in DT that has non-homogeneous
input ports should produce initial tokens at each of its output ports.

</OL>
</p>
<p>
<h1> Design Notes</h1>
DT (Discrete Time) is a timed model of computation.  In order
to benefit from the internal time-keeping mechanism of DT, one should
use actors aware of time. For example, one should use TimedPlotter or
TimedScope instead of SequencePlotter or SequenceScope.
<p>
Top-level DT Directors have a <i>period</i> parameter that can be set by the
user.  Setting the period parameter of a non-top-level DT Director
under hierarchical DT has no meaning; and hence will be ignored.
<p>

<p> Domain-polymorphic actors that want to take advantage of the
multi-rate timing capabilities of DT should call
getCurrentTime(channel_number) for every
get(channel_number). Moreover, the call sequence should be ordered as
follows: getCurrentTime(channel_number) before get(channel_number).

Known bugs:
<pre>
 1.) Put more tests on this case: when events come in faster than the period
     of a DT composite actor (e.g clock feeding DT)
 2.) Put more tests on this case: when DT composite actor doesn't fire because
     there aren't enough tokens.
 3.) Domain-polymorphic actors that use getCurrentTime() should be modified
     to use DT's multirate timing capabilities.
     CurrentTime  - modified
     TimedPlotter - modified
     TimedScope   - modified
     SequentialClock - no input ports, gets global time only
     PoissonClock - under investigation
     Clock        - under investigation
     Expression   - under investigation
</pre>

@see ptolemy.domains.dt.kernel.DTReceiver
@see ptolemy.domains.sdf.kernel.SDFDirector
@see ptolemy.domains.sdf.kernel.SDFReceiver
@see ptolemy.domains.sdf.kernel.SDFScheduler

 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
*/
public class DTDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DTDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public DTDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public DTDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The period of the model.  This parameter must contain a
     *  DoubleToken.  Its default value is 1.0 .
     *  For homogeneous hierarchical DT (i.e. DT inside DT) , the period
     *  of the inside director cannot be set explicitly by the user.
     *  Instead, it will have a fixed value: "outsidePeriod / repetitions ",
     *  where 'outsidePeriod' is the period of the outside director; and
     *  'repetitions' is the firing count of the composite actor that contains
     *  the inside director. For heterogeneous hierarchical DT (i.e. DT inside
     *  DE or CT), the
     *  period parameter is used to determine how often the fireAt()
     *  method is called to request firing from the outside director.
     */
    public Parameter period;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. For this director the only
     *  relevant attribute is the <i>period</i> parameter.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // -attributeChanged-
        // FIXME: handle period parameter mutations
        super.attributeChanged(attribute);
    }

    /** Go through the schedule and iterate every actor with calls to
     *  prefire() , fire() , and postfire().  If this director is not
     *  in the top-level, get the outside director's current time; and
     *  check whether the returned time is an integer multiple of the
     *  <i>period</i> parameter. If it is not, then don't fire.
     *  @exception IllegalActionException If an actor executed by this
     *  director returns false in its prefire().
     */
    public void fire() throws IllegalActionException {
        //TypedCompositeActor container = (TypedCompositeActor) getContainer();
        //Director outsideDirector = _getOutsideDirector();
        // Some timed directors (such as CT) increment time after prefire()
        // and during fire(), so time may not be properly updated
        // before this stage of the execution.
        _checkValidTimeIntervals();

        if (!_isFiringAllowed) {
            return;
        }

        if (! _inputTokensAvailable) {
            return;
        }

        // FIXME: this portion of code is currently commented out
        // because super.fire() is called. However, there are problems
        // with prefire return false in SDFDirector.fire()
        /*
          _debugViewSchedule();

          if (container == null) {
          throw new InvalidStateException("DTDirector " + getName() +
          " fired, but it has no container!");
          } else {

          Scheduler scheduler = getScheduler();
          if (scheduler == null)
          throw new IllegalActionException("Attempted to fire " +
          "DT system with no scheduler");
          Enumeration allActors = scheduler.schedule();
          while (allActors.hasMoreElements()) {

          Actor actor = (Actor)allActors.nextElement();

          boolean isFiringNonDTCompositeActor = false;

          if (actor instanceof CompositeActor) {
          CompositeActor compositeActor = (CompositeActor) actor;
          Director  insideDirector =
          compositeActor.getDirector();

          if ( !(insideDirector instanceof DTDirector)) {
          isFiringNonDTCompositeActor = true;
          _insideDirector = insideDirector;
          }
          }

          if (isFiringNonDTCompositeActor) {
          _pseudoTimeEnabled = true;
          }


          if (!actor.prefire()) {
          throw new IllegalActionException(this,
          (ComponentEntity) actor, "Actor " +
          "is not ready to fire.");
          }

          if (_debugging)
          _debug("Firing " + ((Nameable)actor).getFullName());

          actor.fire();

          // note: short circuit evaluation here
          _postFireReturns = actor.postfire() && _postFireReturns;

          if (isFiringNonDTCompositeActor) {
          _pseudoTimeEnabled = false;
          }

          }
          }*/

        super.fire();

        // fire_
    }


    /** Return the DT global time. Actors who wish to take
     *  advantage of DT's multi-rate time-keeping capabilities should
     *  call getCurrentTime(channel_number) on a specific IOPort instead.
     *
     *  @return the current time
     */
    public double getCurrentTime() {
        return _currentTime;
    }



    /** Return the time value of the next iteration.
     *
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        double period = 0.0;
        try {
            period = getPeriod();
        } catch (IllegalActionException exception) {
            // FIXME: handle this
        }
        return _currentTime + period;
    }


    /** Get the global time increment per iteration for this director.
     *  This is a convenience method for getting the period parameter.
     *  For hierarchical DT (DT inside DT), extra calculation is done
     *  to compute the period as a fraction of the outside period.
     *  @return The value of the period parameter.
     *  @exception IllegalActionException If the period parameter is
     *  is not of type DoubleToken or IntToken.
     */
    public double getPeriod() throws IllegalActionException {
        // FIXME: A faster implementation of this method should cache
        // a private local _period variable instead. Also the
        // implementation might need to update the inside DT
        // director's period value.

        // FIXME: It is inefficient to calculate and set the inside DT
        // director's period value at every call to this function
        Token periodToken;
        double periodValue = 0.0;
        Director outsideDirector;
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        boolean shouldUpdatePeriod = false;

        outsideDirector = _getOutsideDirector();
        if (outsideDirector instanceof DTDirector) {
            DTDirector outsideDTDirector = (DTDirector) outsideDirector;
            periodToken = outsideDTDirector.period.getToken();
            periodValue = 1.0 / outsideDTDirector._getRepetitions(container);
            // used as a multiplier to the actual value
            shouldUpdatePeriod = true;
        } else {
            periodToken = period.getToken();
            periodValue = 1.0;
            // used as a multiplier to the actual value
        }

        if (periodToken instanceof DoubleToken) {
            double storedValue = ((DoubleToken) periodToken).doubleValue();
            periodValue = periodValue * storedValue;
        } else if (periodToken instanceof IntToken) {
            double storedValue = ((IntToken) periodToken).intValue();
            periodValue = periodValue * storedValue;
        } else {
            throw new IllegalActionException(
                    "Illegal DT period parameter value");
        }
        if (shouldUpdatePeriod) {
            period.setToken(new DoubleToken(periodValue));
        }
        return periodValue;
    }


    /** Initialize all the actors associated with this director by calling
     *  super.initialize(). Determine which actors need to generate
     *  initial tokens for causality. All actors with nonhomogeneous input
     *  ports will need to generate initial tokens for all of their output
     *  ports. For example, if actor A has a nonhomogeneous input port and an
     *  output port with production rate 'm' then actor A needs to produce 'm'
     *  initial tokens on the output port.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        //  -initialize-

        _requestRefireAt(0.0);
        _actorTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _receiverTable = new ArrayList();
        _buildReceiverTable();
        _buildActorTable();
        _buildOutputPortTable();
        super.initialize();

        // This portion figures out which actors should generate initial tokens
        ListIterator receiverIterator = _receiverTable.listIterator();
        while (receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            IOPort currentPort = currentReceiver.getContainer();
            int rate = 0;
            Actor actor = (Actor) currentPort.getContainer();

            DTActor dtActor = (DTActor) _allActorsTable.get(actor);
            if (_debugging) {
                _debug("Checking for initial tokens:" + dtActor);
            }
            if (dtActor == null) {
                throw new IllegalActionException(
                        "DT internal error: unknown actor");
            }

            Parameter param =
                (Parameter) currentPort.getAttribute("tokenConsumptionRate");
            if ((param != null)&&(currentPort.isInput())) {
                rate = ((IntToken)param.getToken()).intValue();
                if (rate > 1) dtActor._shouldGenerateInitialTokens = true;
            }
        }
        _debugViewActorTable();

        // This portion generates the initial tokens for actors with
        // nonhomogeneous outputs.
        receiverIterator = _receiverTable.listIterator();
        while (receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            TypedIOPort currentPort =
                (TypedIOPort) currentReceiver.getContainer();

            TypedIOPort fromPort = currentReceiver.getSourcePort();
            Type fromType = fromPort.getType();
            Actor fromActor = (Actor) fromPort.getContainer();
            Parameter param =
                (Parameter) fromPort.getAttribute("tokenProductionRate");
            int outrate = 0;
            if ((param != null) && (fromPort.isOutput())) {
                outrate = ((IntToken)param.getToken()).intValue();
            }


            DTActor dtFromActor = (DTActor) _allActorsTable.get(fromActor);

            if (dtFromActor != null) {
                if (dtFromActor._shouldGenerateInitialTokens) {
                    if (_debugging) {
                        _debug("initial port: " + fromType
                                + " to " + currentPort.getType());
                    }
                    for (int j = 0; j < outrate; j++) {
                        // FIXME:  should check what token basetype
                        // for the port and generate such.
                        // move this out of the loop
                        // FIXME: It might be a better idea to overwrite
                        // the contents of port parameter tokenInitProduction
                        // to hold the correct integer value of init. tokens
                        // FIXME: Put a new parameter on the port for the
                        // user to be able to put their own initial tokens;
                        // however some specific SDF actors may have their
                        // own buffers parameters that actually keep this
                        // initial tokens (similar to SampleDelay)
                        if (fromType.equals(BaseType.BOOLEAN)) {
                            currentReceiver.put(new BooleanToken(false));
                        } else if (fromType.equals(BaseType.DOUBLE)) {
                            currentReceiver.put(new DoubleToken(0.0));
                        } else if (fromType.equals(BaseType.INT)) {
                            currentReceiver.put(new IntToken(0));
                        }
                    }
                }
            }
        }
        _debugViewActorTable();
        _debugViewReceiverTable();
    }


    /** Process the mutation that occurred.  Notify the parent class about
     *  the invalidated schedule.  This method is called when an entity
     *  is instantiated under this director. This method is also
     *  called when a link is made between ports and/or relations.
     *  see also other mutation methods:
     *
     *  @see ptolemy.kernel.util.NamedObj#attributeChanged
     *  @see ptolemy.kernel.util.NamedObj#attributeTypeChanged
     */
    public void invalidateSchedule() {
        //  -invalidateSchedule-
        super.invalidateSchedule();
    }

    /** Return a new receiver consistent with the DT domain.
     *
     *  @return A new DTReceiver.
     */
    public Receiver newReceiver() {
        return new DTReceiver();
    }

    /** Request the outside director to fire this director's container
     *  again for the next period.
     *
     *  @return true if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the parent class throws
     *  it.
     */
    public boolean postfire() throws IllegalActionException {
        _makeTokensAvailable();
        double timeIncrement = getPeriod();
        _currentTime = _formerValidTimeFired + timeIncrement;
        _requestRefireAt(_formerValidTimeFired + timeIncrement);
        if (! _isFiringAllowed) {
            return true;
        }

        return super.postfire();
        // When an actor's postfire_ returns false, whole model should stop.
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens. Always return
     *  true in order to allow firing or pseudo-firing. Pseudo-firing is
     *  needed when DT is interacting hierarchically with DE.
     *
     *  @exception IllegalActionException If the parent class throws
     *  it.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
        //  -prefire-
        _inputTokensAvailable = super.prefire();
        return true;
    }

    /** Set the local time of an actor in the  model under
     *  this director. This method is called by the DTReceiver
     *  class and doesn't need to be called by any other classes.
     *
     *  @param newTime The new current simulation time.
     *  @param actor The actor to be assigned a new local time
     */
    public void setActorLocalTime(double newTime, Actor actor) {
        DTActor dtActor = (DTActor) _allActorsTable.get(actor);
        dtActor._localTime = newTime;
    }

    /** Set the current time of the model under this director.
     *  Setting the time back to the past is allowed in DT.
     *
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) {
        // _currentTime is inherited from base Director
        _currentTime = newTime;
    }

    /** Override the base class method to make sure that enough tokens
     *  are available to complete one iteration.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  input port. If any channel of the input port has no data, then
     *  that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        //  -transferInputs-
        if (_inputTokensAvailable) {
            return super.transferInputs(port);
        } else {
            return false;
        }
    }

    /** This is called by the outside director to get tokens
     *  from an opaque composite actor. Return true if data is
     *  transferred from an output port of the container to the
     *  ports it is connected to on the outside. This method differs
     *  from the base class method in that this method will transfer
     *  all available tokens in the receivers, while the base class
     *  method will transfer at most one token. This behavior is
     *  required to handle the case of non-homogeneous opaque
     *  composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Boolean flag = (Boolean)_shouldTransferOutputs.get(port);
        if (_shouldDoInternalTransferOutputs && flag.booleanValue()) {
            if (container.getExecutiveDirector() instanceof DTDirector) {
                // If we have dt inside dt, then transfer all of the
                // tokens that were created.  The containing director
                // will figure out what time they should appear.
                return super.transferOutputs(port);
            } else {
                // We probably have something like DE on the outside...
                // Transfer one token and wait for refiring to transfer
                // the others.
                return domainPolymorphicTransferOutputs(port);
            }
        } else {
            return false;
        }
    }

    /**  Reset this director to an uninitialized state.
     *
     *  @exception IllegalActionException If the parent class
     *  throws it
     */
    public void wrapup() throws IllegalActionException {
        //  -wrapup-
        super.wrapup();
        _reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the number of times an actor repeats in the schedule of an
     *  SDF graph.  If the actor does not exist, throw an exception.
     *
     *  @param actor The actor whose firing count is needed
     *  @exception IllegalActionException If actor does not exist.
     */
    protected int _getRepetitions(Actor actor) throws IllegalActionException {
        ListIterator actorIterator = _actorTable.listIterator();
        int repeats = 0;

    foundRepeatValue:
        while (actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            if (actor.equals(currentActor._actor)) {
                repeats = currentActor._repeats;
                break foundRepeatValue;
            }
        }

        if (repeats == 0) {
            throw new IllegalActionException(
                    "internal DT error: actor with zero firing count");
        }
        return repeats;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *
     *  @exception IllegalActionException If the scheduler is null
     */
    private void _buildActorTable() throws IllegalActionException {
        SDFScheduler currentScheduler = (SDFScheduler) getScheduler();

        CompositeActor container = (CompositeActor) getContainer();
        if (container != null) {
            Iterator allActors = container.deepEntityList().iterator();
            while (allActors.hasNext()) {
                Actor actor = (Actor) allActors.next();
                DTActor dtActor = (DTActor) _allActorsTable.get(actor);
                if (dtActor == null) {
                    _allActorsTable.put(actor, new DTActor(actor));
                    dtActor = (DTActor) _allActorsTable.get(actor);
                    _actorTable.add(dtActor);
                }
                dtActor._repeats =
                    currentScheduler.getFiringCount((Entity)dtActor._actor);

            }
        }

        // Include the container as an actor.  This is needed for
        // TypedCompositeActors.

        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor, new DTActor((Actor)getContainer()));
        DTActor dtActor = (DTActor) _allActorsTable.get(actor);
        dtActor._repeats = 1;
        _actorTable.add(dtActor);

        _debugViewActorTable();
        ListIterator receiverIterator = _receiverTable.listIterator();
        while (receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver.determineEnds();
        }

        receiverIterator = _receiverTable.listIterator();

        while (receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver.calculateDeltaTime();
        }

        _debugViewActorTable();
        _debugViewReceiverTable();
    }

    /** Build the internal cache of all the receivers directed by this
     *  director.
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildReceiverTable() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        if (container != null) {
            Iterator allActors = container.deepEntityList().iterator();
            while (allActors.hasNext()) {
                Actor actor = (Actor) allActors.next();
                // Get all input ports
                Iterator allInputs = actor.inputPortList().iterator();
                while (allInputs.hasNext()){
                    IOPort inputPort = (IOPort)allInputs.next();
                    Receiver[][] receivers = inputPort.getReceivers();
                    if (receivers != null) {
                        for (int i = 0; i < receivers.length; i++) {
                            if (receivers[i] != null) {
                                for (int j = 0; j < receivers[i].length; j++) {
                                    if (receivers[i][j] != null) {
                                        _receiverTable.add(receivers[i][j]);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Also add the inside receivers in the ports of the
            // composite actor that contains this director.
            Iterator compositePorts = container.outputPortList().iterator();
            while (compositePorts.hasNext()) {
                IOPort outputPort = (IOPort)compositePorts.next();
                Receiver[][] receivers = outputPort.getInsideReceivers();
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            for (int j = 0; j < receivers[i].length; j++) {
                                if (receivers[i][j] != null) {
                                    _receiverTable.add(receivers[i][j]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /** Build the internal cache of all the ports directed by this director
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildOutputPortTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        _shouldTransferOutputs = new HashMap();
        Iterator outports = container.outputPortList().iterator();
        while (outports.hasNext()) {
            IOPort port = (IOPort)outports.next();

            _shouldTransferOutputs.put(port, Boolean.TRUE);
        }

    }

    /** Check if the current time is a valid time for execution. If the
     *  current time is not a integer multiple of the DT period, firing
     *  must not occur.
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private final void _checkValidTimeIntervals()
            throws IllegalActionException {
        //  -checkValidTimeIntervals-
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();
        _debug("shouldIgnoreFire subroutine called");

        // No need to check if this director is in the top level.
        if (outsideDirector == null) {
            _formerValidTimeFired = _currentTime;
            return;
        }

        // No need to check if the executive director is also a DTDirector
        if (outsideDirector instanceof DTDirector) {
            _formerValidTimeFired = _currentTime;
            return;
        }


        double currentTime = outsideDirector.getCurrentTime();
        double currentPeriod = getPeriod();
        double timeElapsed = currentTime - _formerValidTimeFired;

        _debug("DT Director just started fire----------------"
                + _formerValidTimeFired + " " + currentTime);


        if ((currentTime != 0) && (! _inputTokensAvailable) &&
                ((currentTime - _formerTimeFired) < _TOLERANCE )) {
            //  duplicate firings at the same time should be ignored
            //  unless there are input tokens
            _debug("duplicate firing");
            _isFiringAllowed = false;
            _shouldDoInternalTransferOutputs = false;
            _makeTokensUnavailable();
            return;
        }  else {
            _formerTimeFired = currentTime;
        }

        // this occurs during startup
        if (currentTime == 0) {
            _debug("first firing");
            _shouldDoInternalTransferOutputs = true;
            _formerValidTimeFired = currentTime;
            _issuePseudoFire(currentTime);
            _isFiringAllowed = true;
            return;
        }


        double timeRemaining = currentPeriod - timeElapsed;
        _debug("timeElapsed = " + timeElapsed);
        _debug("timeRemaining = " + timeRemaining);
        _debug("tolerance = " + _TOLERANCE);

        if (timeRemaining < -_TOLERANCE ) {
            // this case should not occur
            _debug("InternalErrorException time: "
                    + _formerValidTimeFired
                    + " " + currentTime);
            throw new InternalErrorException("unexpected time rollback");
        }

        if ((timeRemaining > _TOLERANCE)
                && (timeElapsed > _TOLERANCE)) {

            Iterator outputPorts = container.outputPortList().iterator();
            _isFiringAllowed = false;
            while (outputPorts.hasNext()) {
                Receiver[][] insideReceivers;
                TypedIOPort port = (TypedIOPort) outputPorts.next();

                insideReceivers = port.getInsideReceivers();
                double deltaTime =
                    ((DTReceiver)insideReceivers[0][0]).getDeltaTime();
                double ratio = timeElapsed / deltaTime;

                if (Math.abs(Math.round(ratio) - ratio) < _TOLERANCE) {
                    // firing at a time when transferOutputs should be called
                    _debug("*************** fractional fire ratio "
                            + ratio + " should transferOutputs");
                    _shouldTransferOutputs.put(port, Boolean.TRUE);
                    _isFiringAllowed = false;
                    _shouldDoInternalTransferOutputs = true;
                } else {
                    // firing at a time when transferOutputs should
                    // not be called
                    for (int i = 0; i < port.getWidth(); i++) {
                        for (int j = 0;
                             j < insideReceivers[i].length; j++) {
                            DTReceiver receiver;
                            receiver = (DTReceiver) insideReceivers[i][j];
                            receiver.overrideHasToken = true;
                        }
                    }
                    _debug("******* nonfractional fire ratio "
                            + ratio + " don't transferOutputs");
                    _shouldTransferOutputs.put(port, Boolean.FALSE);
                }
            }
        } else if (_inputTokensAvailable)  {
            // this case occurs during period intervals
            // and enough input tokens are available

            _issuePseudoFire(currentTime);
            _formerValidTimeFired = currentTime;
            _isFiringAllowed = true;
            _shouldDoInternalTransferOutputs = true;
        } else {
            // this case occurs during period intervals
            // but not enough input tokens are available
            _formerValidTimeFired = currentTime;
            _isFiringAllowed = false;
            _shouldDoInternalTransferOutputs = false;
        }
    }


    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException If there is a problem in
     *   obtaining the number of initial token for delay actors
     */
    private void _debugViewActorTable() throws IllegalActionException {

        _debug("---------------------------------------");
        _debug("\nACTOR TABLE with " + _actorTable.size()
                + " unique actors");
        ListIterator actorIterator = _actorTable.listIterator();
        while (actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            String actorName = ((Nameable) currentActor._actor).getName();

            _debug("Actor " + actorName + " repeats:" + currentActor._repeats);
            _debug(" initial_tokens? "
                    + currentActor._shouldGenerateInitialTokens);

            if (currentActor._actor instanceof SampleDelay) {
                SampleDelay delay = (SampleDelay) currentActor._actor;
                ArrayToken initialTokens =
                    (ArrayToken) delay.initialOutputs.getToken();
                int delayCount = initialTokens.length();

                _debug(" **DELAY** with " + delayCount
                        + " initial tokens");
            }

            if ( !((ComponentEntity) currentActor._actor).isAtomic() ) {
                _debug(" **COMPOSITE** ");
            }
        }
    }














    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _debugViewReceiverTable() {
        //  -displayReceiverTable-
        _debug("\nARC RECEIVER table with "+_receiverTable.size());
        _debug(" unique receivers");

        ListIterator receiverIterator = _receiverTable.listIterator();

        while (receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver._debugViewReceiverInfo();
        }
        _debug("\n");
    }





    /** Convenience method for getting the director of the container that
     *  holds this director.  If this director is inside a toplevel
     *  container, then the returned value is null.
     *  @return The executive director
     */
    private Director _getOutsideDirector() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        return outsideDirector;
    }







    /** Request the outside non-DT director to fire this TypedCompositeActor
     *  at time intervals equal to when the output tokens should be produced.
     *  No actual firing occurs of the inside actors will occur; hence the
     *  name 'pseudo-firing'
     */
    private void _issuePseudoFire(double currentTime)
            throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        Receiver[][] insideReceivers;

        while (listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            insideReceivers = port.getInsideReceivers();
            DTReceiver receiver = (DTReceiver) insideReceivers[0][0];
            double deltaTime = receiver.getDeltaTime();
            int periodDivider = receiver.getTokenFlowRate();
            _debug("request pseudo-fire at " + deltaTime
                    + " intervals. " + periodDivider);
            for (int n = 1; n < periodDivider; n++) {
                _requestRefireAt(currentTime + n * deltaTime);
                _debug(" request pseudo-fire at "
                        + (currentTime + n * deltaTime));
            }
        }
    }

    /** Enable the hasToken() method in the output ports of the
     *  TypedCompositeActor directed by this director.  This is
     *  used in composing DT with DE and CT.
     */
    private void _makeTokensAvailable() throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        while (listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            Receiver[][] portReceivers = port.getInsideReceivers();

            for (int i = 0; i < port.getWidth(); i++) {
                for (int j = 0; j < portReceivers[i].length; j++) {
                    ((DTReceiver) portReceivers[i][j]).overrideHasToken =
                        false;
                }
            }
        }
    }


    /** Disable the hasToken() method in the output ports of the
     *  TypedCompositeActor directed by this director.  This is
     *  used in composing DT with DE and CT.
     */
    private void _makeTokensUnavailable() throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        while (listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            Receiver[][] portReceivers = port.getInsideReceivers();

            for (int i = 0; i < port.getWidth(); i++) {
                for (int j = 0; j < portReceivers[i].length; j++) {
                    ((DTReceiver) portReceivers[i][j]).overrideHasToken =
                        true;
                }
            }
        }
    }


    /** Convenience method for asking the executive director to fire this
     *  director's container again at a specific time in the future.
     *  @param time The time when this director's container should be fired
     *  @exception IllegalActionException If getting the container or
     *  executive director fails
     */
    private void _requestRefireAt(double time) throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        if (outsideDirector != null) {
            outsideDirector.fireAt(container, time);
        }
    }


    /** Most of the constructor initialization is relegated to this method.
     *  Initialization process includes :
     *    - create a new actor table to cache all actors contained
     *    - create a new receiver table to cache all receivers contained
     *    - set default number of iterations
     *    - set period value
     */
    private void _init() {
        try {
            period = new Parameter(this, "period", new DoubleToken(1.0));
            _reset();
            iterations.setToken(new IntToken(0));
        } catch (Exception e) {
            throw new InternalErrorException(
                    "unable to initialize DT Director:\n" +
                    e.getMessage());
        }
    }

    private void _reset() {
        _actorTable = new ArrayList();
        _receiverTable = new ArrayList();
        _outputPortTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _currentTime = 0.0;
        _formerTimeFired = 0.0;
        _formerValidTimeFired = 0.0;
        _isFiringAllowed = true;
        _shouldDoInternalTransferOutputs = true;
    }





    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // ArrayList to keep track of all actors scheduled by DTDirector
    private ArrayList _actorTable;

    // ArrayList used to cache all receivers managed by DTDirector
    private ArrayList _receiverTable;

    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;

    // The time when the previous valid prefire() was called
    private double _formerValidTimeFired;

    // The time when the previous valid or invalid prefire() was called
    private double _formerTimeFired;



    // used to keep track of whether firing can be done at current time
    private boolean _isFiringAllowed;

    // ArrayList to keep track of all container output ports
    private ArrayList _outputPortTable;



    // used to determine whether the director should call transferOutputs()
    private boolean _shouldDoInternalTransferOutputs;

    private boolean _inputTokensAvailable;

    private Map _shouldTransferOutputs;

    // The tolerance value used when comparing time values.
    private static final double _TOLERANCE = 0.0000000001;




    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Inner class to cache important variables for contained actors
    private class DTActor {
        private Actor    _actor;
        private double   _localTime;
        private int      _repeats;
        private boolean  _shouldGenerateInitialTokens;

        /* Construct the information on the contained Actor
         * @param a The actor
         */
        public DTActor(Actor actor) {
            _actor = actor;
            _repeats = 0;
            _localTime = 0.0;
            _shouldGenerateInitialTokens = false;
        }
    }
}
