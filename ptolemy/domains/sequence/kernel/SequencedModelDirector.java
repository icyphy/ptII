/* Director for the sequencing model of computation.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * An abstract base class for SequenceDirector and ProcessDirector.
 *
 * <p>The SequencedModelDirector
 * <br>- Computes the sequenced actors and passes these in two lists to the
 * SequenceScheduler (one list for independent sequenced actors, and one list
 * for sequenced actors that are dependent on other actors e.g. control actors)
 *
 * @author Elizabeth Latronico (Bosch), rrs1pal
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 */
public abstract class SequencedModelDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The SequencedModelDirector will have a default scheduler of type SequenceScheduler.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SequencedModelDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The SequencedModelDirector will have a default scheduler of type SequenceScheduler.
     *
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SequencedModelDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SequencedModelDirector will have a default scheduler of type
     *   SequenceScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SequencedModelDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever. Note that the amount
     *  of data processed by the Sequence model is a function of both this
     *  parameter and the value of parameter <i>vectorizationFactor</i>, since
     *  <i>vectorizationFactor</i> can influence the choice of schedule.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** If true, enable user defined output initial values.  The default value
     *  is a boolean true.
     */
    public Parameter userDefinedOutputInitialValue;

    /** The user defined default output initial value.  The default type
     *  is BaseType.GENERAL.
     */
    public Parameter userDefinedDefaultOutputInitialValue;

    /** If true, enable default output initial values.  The default value
     *  is a boolean true.
     */
    public Parameter defaultOutputInitialValue;

    /** If true, fire any unexecuted actors that were not fired during
     *  the sequence schedule.
     */
    public Parameter fireUnexecutedActors;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Code copied from StaticSchedulingDirector
     *  FIXME:  Do we just want a SequencedModelDirector to be a subclass of
     *  StaticSchedulingDirector?  But, the complete schedule is not statically computable
     *  if control actors are present
     *  FIXME:  I am not sure if this works correctly in all situations...
     *
     *  Clone is needed in the director to clone the scheduler
     *
     *  Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SequencedModelDirector newObject = (SequencedModelDirector) super
                .clone(workspace);
        SequenceScheduler scheduler = getScheduler();

        // Note that _setScheduler() invalidates the schedule (which is what we want)
        // That way, the new clone will need to re-compute the schedule, so the new schedule
        // will include references to the new cloned model actors instead of the original model actors
        if (scheduler == null) {
            newObject._setScheduler(null);
        } else {
            newObject._setScheduler((SequenceScheduler) newObject
                    .getAttribute(getScheduler().getName()));
        }
        return newObject;
    }

    /**
     *  Return the scheduler that is responsible for scheduling the
     *  directed actors.  This method is read-synchronized on the
     *  workspace.
     *
     *  @return The contained scheduler.
     *  @see #setScheduler(SequenceScheduler)
     */
    public SequenceScheduler getScheduler() {
        try {
            workspace().getReadAccess();
            return _scheduler;
        } finally {
            workspace().doneReading();
        }
    }

    /** Indicate that a schedule for the model may no longer be valid.
     *  This method should be called when topology changes are made,
     *  or for that matter when any change that may invalidate the
     *  schedule is made.  In this base class, this method sets a flag
     *  that forces scheduling to be redone at the next opportunity.
     *  If there is no scheduler, do nothing.
     */
    @Override
    public void invalidateSchedule() {
        _debug("Invalidating schedule.");
        if (_scheduler != null) {
            _scheduler.setValid(false);
        }
    }

    /** Return true if the current (cached) schedule is valid.
     *  This calls the valid() method of Scheduler.
     *  @return true if the schedule is valid.
     *  @exception IllegalActionException If there's no scheduler.
     */
    public boolean isScheduleValid() throws IllegalActionException {
        if (_scheduler == null) {
            throw new IllegalActionException(this, "has no scheduler.");
        }

        return _scheduler.isValid();
    }

    /** Initialize the actors associated with this director and then
     *  set the iteration count to zero.  The order in which the
     *  actors are initialized is arbitrary.  In addition, if actors
     *  connected directly to output ports have initial production,
     *  then copy that initial production to the outside of the
     *  composite actor.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;

        // Note:  Dependent actors with sequence numbers are also initialized
        // This is since, even though they are dependent, perhaps they are part of
        // a loop or something

        for (SequenceAttribute attribute : _sequencedList) {
            Entity actorEntity = (Entity) attribute.getContainer();

            // FIXME:  How to initialize control actors?
            //         Only initialize the branch that should be fired?
            if (!(actorEntity instanceof ControlActor)) {
                setOutputInitialValues(actorEntity);
            }
        }
    }

    /** Set the initial values for output ports.
     *
     * FIXME:  There are problems with the current way some of these settings are defined in the
     * director.  We discussed a solution but did not have time to implement it yet.
     * Check the open item list for comments about this.
     *
     * Actors with SequenceAttributes/ProcessAttributes are required to have initial values
     * defined for the output ports.  This is to ensure that if another actor is executed
     * first which consumes data from a sequenced actor that is fired later in the schedule,
     * then a value will be available for the first actor to use.
     *
     * @param actorEntity The entity
     * @exception IllegalActionException If thrown while getting the width of a port or
     * getting the value of a parameter.
     */
    public void setOutputInitialValues(Entity actorEntity)
            throws IllegalActionException {

        String actorName = ((Actor) actorEntity).getName();
        Parameter initialValueParameter = null;
        for (TypedIOPort port : (List<TypedIOPort>) ((Actor) actorEntity)
                .outputPortList()) {

            for (int channel = 0; channel < port.getWidth(); channel++) {
                if (userDefinedOutputInitialValue.getToken().equals(
                        BooleanToken.FALSE)
                        && defaultOutputInitialValue.getToken().equals(
                                BooleanToken.TRUE)) {
                    initialValueParameter = new Parameter();
                    if (userDefinedDefaultOutputInitialValue.getToken() != null) {
                        port.send(channel,
                                userDefinedDefaultOutputInitialValue.getToken());
                    } else {
                        initialValueParameter.setExpression(port.getType()
                                .zero().toString());
                        port.send(channel, initialValueParameter.getToken());
                    }

                } else if (userDefinedOutputInitialValue.getToken().equals(
                        BooleanToken.TRUE)
                        && defaultOutputInitialValue.getToken().equals(
                                BooleanToken.TRUE)) {
                    initialValueParameter = (Parameter) actorEntity
                            .getAttribute(_getInitialValueParameterName(port,
                                    channel).trim());
                    if (initialValueParameter == null) {
                        if (userDefinedDefaultOutputInitialValue.getToken() != null) {
                            port.send(channel,
                                    userDefinedDefaultOutputInitialValue
                                            .getToken());
                        } else {
                            initialValueParameter = new Parameter();
                            initialValueParameter.setExpression(port.getType()
                                    .zero().toString());
                            port.send(channel, initialValueParameter.getToken());
                        }
                    } else {
                        initialValueParameter.setTypeAtMost(port.getType());
                        port.send(channel, initialValueParameter.getToken());
                    }
                } else if (userDefinedOutputInitialValue.getToken().equals(
                        BooleanToken.TRUE)
                        && defaultOutputInitialValue.getToken().equals(
                                BooleanToken.FALSE)) {
                    initialValueParameter = (Parameter) actorEntity
                            .getAttribute(_getInitialValueParameterName(port,
                                    channel).trim());
                    if (initialValueParameter == null) {
                        if (port.isMultiport()) {
                            throw new IllegalActionException(
                                    "Please provide the outputPortName_channelNumber_InitialValue parameters [out_0_InitialValue] for all the connected outputPorts of the sequenceActor"
                                            + actorName);
                        } else {
                            throw new IllegalActionException(
                                    "Please provide the outputPortName_InitialValue parameter ["
                                            + port.getName()
                                            + "_InitialValue] for the sequenceActor"
                                            + actorName);
                        }
                    }
                    initialValueParameter.setTypeAtMost(port.getType());
                    port.send(channel, initialValueParameter.getToken());

                } else {
                    // FIXME:  If both of the settings are false, there is no way to run the model
                    // This error message does not tell the user what the problem is.
                    if (port.isMultiport()) {
                        throw new IllegalActionException(
                                "Please provide the outputPortName_channelNumber_InitialValue parameters [out_0_InitialValue] for all the connected outputPorts of the sequenceActor"
                                        + actorName);
                    } else {
                        throw new IllegalActionException(
                                "Please provide the outputPortName_InitialValue parameter ["
                                        + port.getName()
                                        + "_InitialValue] for the sequenceActor"
                                        + actorName);
                    }
                }
            }
        }
    }

    /** Return a new receiver consistent with the Sequence domain.
     *  @return A new SequenceReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new RegisterReceiver();
    }

    /** Fire is overridden in subclasses
     *  This class does not call super.fire()
     *
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration. <p>
     *
     *  This method may be overridden by some domains to perform additional
     *  domain-specific operations.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */

    /** Preinitialize will be added to in subclasses
     *
     *  Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly.  In addition, performing scheduling
     *  during preinitialization enables it to be present during code generation.
     *  The order in which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */

    // FIXME: Also support this??
    //     public void preinitialize(List<SequenceAttribute> _independentList) throws IllegalActionException {

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // force the schedule to be computed.
        if (_debugging) {
            _debug("Preinitialize : ");
        }

        // Assemble a list of all sequenced actors in the model
        CompositeActor compositeActor = (CompositeActor) getContainer();
        _sequencedList = new ArrayList<SequenceAttribute>();

        // Get all relevant actors and place in _sequenceList
        getContainedEntities(compositeActor);

        // There must be at least one actor with a sequence or process attribute
        // If not, throw an exception
        if (_sequencedList == null || _sequencedList.isEmpty()) {
            throw new IllegalActionException(this,
                    "There are no actors in the models with sequence numbers.");
        }

        // The ProcessDirector and SequenceDirector should:
        // Separate the list into processes, for the ProcessDirector
        // Sort the list or lists
        // Get a schedule for each list
        // Check for unreachable actors in each list
    }

    /** The SequencedModelDirector adds all actors with sequence numbers to the
     *  _sequencedList, regardless of whether or not the actors have a
     *  process attribute.
     *  In some models, actors with sequence numbers, but without process
     *  attributes, are in fact dependent actors.
     *  The scheduler will later need to ascertain which actors are dependent
     *  on control actors, and remove them from the _independentList.
     *
     * @param compositeActor The composite actor to be searched for entities.
     * @exception IllegalActionException If thrown while checking the attribute type.
     */
    public void getContainedEntities(CompositeActor compositeActor)
            throws IllegalActionException {

        //System.out.println("Getting contained entities of: " + compositeActor.getFullName());
        List sequenceAttributes = null;
        List processAttributes = null;

        // Beth - Removed code here
        // Transparent composite entities should NOT have sequence attributes
        // If they do, these sequence attributes are ignored

        /*
        List<Entity> compositeActorList = (List<Entity>)compositeActor.allCompositeEntityList();
        boolean isTransparentCompositeActor = false;
        //Check for attribute types of transparent composite Actor
        for (Entity actorEntity : compositeActorList) {
            if ( !((CompositeEntity)actorEntity).isOpaque() ) {
                sequenceAttributes = actorEntity.attributeList(SequenceAttribute.class);
                processAttributes = actorEntity.attributeList(ProcessAttribute.class);
                isTransparentCompositeActor = true;
                checkAttributeType(actorEntity, sequenceAttributes, processAttributes,isTransparentCompositeActor);
            }

        }
         */

        //Check for attribute types of Opaque composite Actor and non-composite Actors and create a list of all the actors in model
        List<Entity> deepEntityList = compositeActor.deepEntityList();
        for (Entity actorEntity : deepEntityList) {
            Actor actor = (Actor) actorEntity;
            sequenceAttributes = ((Entity) actor)
                    .attributeList(SequenceAttribute.class);
            processAttributes = ((Entity) actor)
                    .attributeList(ProcessAttribute.class);

            // Check for actors with multiple sequence attributes
            // This is not allowed:  Each actor can have at most one sequence attribute
            // or at most one process attribute (and can not have both)
            checkAttributeType(actor, sequenceAttributes, processAttributes);

            if (actor instanceof TypedCompositeActor) {

                if (_debugging) {
                    _debug("Actor: " + actor + " depthInHierarchy = "
                            + actorEntity.depthInHierarchy());
                }

                // FIXME:  This is true of any opaque actor, not just a composite actor
                if (((CompositeEntity) actor).isOpaque()) {

                    // Beth - added check for no director in current entity
                    // FIXME:  Throw an exception if there is no director in opaque
                    // composite entity
                    // FIXME:  Treat process attributes and sequence attributes separately in
                    // the future?  How to sort models where only some things have a
                    // process attribute and some just have sequence attributes?
                    // FIXME:  Sequence director should not know about process attributes?

                    if (compositeActor.getDirector() != null
                            && !sequenceAttributes.isEmpty()) {

                        if (compositeActor.getDirector().getClass() == SequenceDirector.class
                                && sequenceAttributes.get(0).getClass() == ProcessAttribute.class) {
                            System.out.println("Warning: " + actor.getName()
                                    + "'s Process Attribute will be ignored");
                        }
                        if (compositeActor.getDirector().getClass() == ProcessDirector.class
                                && sequenceAttributes.get(0).getClass() == SequenceAttribute.class) {

                            System.out.println("Warning: " + actor.getName()
                                    + "'s Sequence Attribute will be ignored");
                        }
                    }

                    // FIXME: Now in checkAttributeType, BUT, ProcessDirector should require
                    // ProcessAttributes on actors.  This should go in the ProcessDirector.
                    // Possibly, refactor all checking code into a separate function

                    /*
                    else {
                        if ( sequenceAttributes.isEmpty() && processAttributes.isEmpty() ) {
                            if ( ( compositeActor.getDirector().getClass() == SequenceDirector.class ) ) {

                                throw new IllegalActionException(this,"Composite Actor " + actor.getName() +" should have Sequence Attribute");
                            }

                            if ( ( compositeActor.getDirector().getClass() == ProcessDirector.class ) ) {

                                throw new IllegalActionException(this,"Composite Actor " + actor.getName() +" should have Process Attribute");
                            }
                        }
                    }
                     */
                }

            }

            // If entity has a process attribute or a sequence attribute, add it
            // to the sequenced list
            // FIXME:  Beth comment 01/26/09 - handle sequence numbers of zero
            if (!processAttributes.isEmpty()) {
                _sequencedList.add((ProcessAttribute) processAttributes.get(0));
            }

            else if (!sequenceAttributes.isEmpty()) {
                _sequencedList.add((SequenceAttribute) sequenceAttributes
                        .get(0));
            }
        }
    }

    /** Check for SequenceAttribute or ProcessAttribute for multiple
     * or wrong occurrence in the model.
     * @param actor The actor to be checked.
     * @param sequenceAttributes The list of sequence attributes to be checked.
     * @param processAttributes The list of process attributes to be checked
     * @exception IllegalActionException If sequenceAttributes has a length greater than one
     * or if actor is not an instance of ControlActor.
     */
    public void checkAttributeType(Actor actor, List sequenceAttributes,
            List processAttributes) throws IllegalActionException {
        // FIXME: can we use Generics here for the lists?

        /*  Beth removed - There should not be any transparent composite actors
         *  with sequence numbers.  If there are, these sequence numbers are ignored.
        // FIXME:  Need to check for these though, since the upstream actor calculations
        // will not process something with a sequence attribute


        if ( isTransparentCompositeActor ) {

            if ( !sequenceAttributes.isEmpty() ) {
                if ( sequenceAttributes.get(0).getClass() == ProcessAttribute.class ) {

                    System.out.println("Warning: " + actorEntity.getName() +"'s Process Attribute will be ignored");
                } else if ( sequenceAttributes.get(0).getClass() == SequenceAttribute.class ) {

                    System.out.println("Warning: " + actorEntity.getName() +"'s Sequence Attribute will be ignored");
                }

            }

        }

         */
        // Note that a ProcessAttribute is also a SequenceAttribute

        // MultipleFireMethodsInterface can potentially have more than one Sequence or Process Attribute,
        // but other actors cannot.
        if (!sequenceAttributes.isEmpty()) {
            if (sequenceAttributes.size() > 1
                    && !(actor instanceof MultipleFireMethodsInterface && ((MultipleFireMethodsInterface) actor)
                            .numFireMethods() > 1)) {
                throw new IllegalActionException(
                        this,
                        " Actor "
                                + actor.getName()
                                + " can have only one Sequence Attribute or Process Attribute");
            }
        }

        // Check for control actors that do not have sequence/process attributes
        else // else, if the sequence attribute list is empty
        {
            if (actor instanceof ControlActor) {
                throw new IllegalActionException(
                        this,
                        " Control Actor "
                                + actor.getName()
                                + " must have a Sequence Attribute or Process Attribute");
            }

            // Check for opaque composite actors that do not have sequence attributes
            // These must have sequence attributes in order to be handled correctly by
            // the scheduler (since we must 'fire' the opaque composite to call the
            // director inside)
            // Beth commented out 02/04/09 - An opaque composite actor is not required to have a process
            // attribute or sequence attribute.  In many models, they do not and are scheduled
            // as upstream.  However we still want a director inside, since the entities inside the
            // composite actors should be fired locally and not scheduled globally.

            /*
            if (actor instanceof TypedCompositeActor && ((CompositeEntity)actor).isOpaque())
            {
                throw new IllegalActionException(this, " Opaque Composite Actor " + actor.getName() + " must have a Sequence Attribute or Process Attribute");
            }
             */

        }

        //FIXME:  Add more checks here, for actors that need sequence attributes
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  If the <i>period</i> parameter is greater than 0.0, then
     *  if this director is at the top level, then increment time
     *  by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the iterations parameter
     *  does not contain a legal value.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        _iterationCount++;

        if (iterationsValue > 0 && _iterationCount >= iterationsValue) {
            _iterationCount = 0;
            return false;
        }

        return super.postfire();
    }

    /** Return an array of suggested ModalModel directors  to use with
     *  SequencedModelDirector. The default director is HDFFSMDirector, which supports
     *  multirate actors and only allows state transitions on each iteration.
     *  This is the most safe director to use with Sequence models.
     *  MultirateFSMDirector supports multirate actors and allows state
     *  transitions on each firing of the modal model. MultirateFSMDirector
     *  can be used with Sequence if rate signatures for all the states in the
     *  modal model are same. If rate signatures change during an iteration,
     *  the SequenceDirector will throw an exception.
     *  FSMDirector can be used with SequenceDirector only when rate signatures
     *  for modal model are all 1.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        return new String[] { "ptolemy.domains.modal.kernel.FSMDirector",
                "ptolemy.domains.modal.kernel.MultirateFSMDirector",
                "ptolemy.domains.hdf.kernel.HDFFSMDirector" };
    }

    /** Return true to indicate that a ModalModel under control
     *  of this director supports multirate firing.
     *  @return True indicating a ModalModel under control of this director
     *  supports multirate firing.
     */
    @Override
    public boolean supportMultirateFiring() {
        return false;
    }

    /** Copied from SDFScheduler; also, same as in StaticSchedulingDirector
     *  Set the scheduler for this SequenceDirector.
     *  The container of the specified scheduler is set to this director.
     *  If there was a previous scheduler, the container of that scheduler
     *  is set to null. This method is write-synchronized on the workspace.
     *  If the scheduler is not compatible with the director, an
     *  IllegalActionException is thrown.
     *  @param scheduler The scheduler that this director will use.
     *  @exception IllegalActionException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     *  @exception NameDuplicationException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     *  @see #getScheduler()
     */

    public void setScheduler(SequenceScheduler scheduler)
            throws IllegalActionException, NameDuplicationException {
        if (scheduler != null) {
            scheduler.setContainer(this);
        } else {
            if (_scheduler != null) {
                _scheduler.setContainer(null);
            }
        }
        _setScheduler(scheduler);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Copied from SDFDirector; also, same as in StaticSchedulingDirector
     *  Set the local scheduler for execution of this Director.
     *  This should not be called be directly.  Instead, call setContainer()
     *  on the scheduler.  This method removes any previous scheduler
     *  from this container, and caches a local reference to the scheduler
     *  so that this composite does not need to search its attributes each
     *  time the scheduler is accessed.
     *  @param scheduler The Scheduler responsible for execution.
     */
    protected void _setScheduler(SequenceScheduler scheduler) {
        // If the scheduler is not changed, do nothing.
        if (_scheduler != scheduler) {
            _scheduler = scheduler;
            invalidateSchedule();
        }
    }

    /** Fire the given SequenceSchedule.
     *  This is the same for the ProcessDirector and SequenceDirector
     *  (although how the schedule is determined is different)
     *
     *  @param seqSchedule   The SequenceSchedule to fire
     *  @exception IllegalActionException  From actor.iterate()
     */

    protected void fireSchedule(SequenceSchedule seqSchedule)
            throws IllegalActionException {

        if (seqSchedule == null) {
            throw new IllegalActionException(this,
                    "Null schedule in ProcessDirector or SequenceDirector");
        }

        // Get a firing iterator for this schedule
        Iterator firings = seqSchedule.firingIterator();

        while (firings.hasNext() && !_stopRequested) {
            SequenceFiring firing = (SequenceFiring) firings.next();
            Actor actor = firing.getActor();

            // If the actor is a MultipleFireMethodsInterface, set
            // its fire method before firing it.
            if (actor instanceof MultipleFireMethodsInterface
                    && ((MultipleFireMethodsInterface) actor).numFireMethods() > 1) {
                String methodName = firing.getMethodName();
                ((MultipleFireMethodsInterface) actor)
                        .setFireMethod(methodName);
            }

            int iterationCount = firing.getIterationCount();

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE,
                        iterationCount));
            }

            int returnValue = actor.iterate(iterationCount);

            if (returnValue == STOP_ITERATING) {
                _postfireReturns = false;
            } else if (returnValue == NOT_READY) {
                throw new IllegalActionException(this, actor, "Actor "
                        + "is not ready to fire.");
            }

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.AFTER_ITERATE,
                        iterationCount));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SequencedModelDirector a
     *  default scheduler of the class SequenceScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     *
     *  @exception IllegalActionException If there is a problem instantiating
     *   the director's parameters.
     *  @exception NameDuplicationException If there is a problem instantiating
     *   the director's parameters.
     */
    protected void _init() throws IllegalActionException,
            NameDuplicationException {
        // Create a new SequenceScheduler object
        // This sets the container at the same time
        SequenceScheduler scheduler = new SequenceScheduler(this,
                uniqueName("SequenceScheduler"));
        setScheduler(scheduler);

        iterations = new Parameter(this, "iterations");
        iterations.setTypeEquals(BaseType.INT);
        iterations.setExpression("0");

        userDefinedOutputInitialValue = new Parameter(this,
                "Enable User Defined Output Initial Values", BooleanToken.TRUE);
        userDefinedOutputInitialValue.setTypeEquals(BaseType.BOOLEAN);

        userDefinedDefaultOutputInitialValue = new Parameter(this,
                "User Defined Default Output Initial Value");
        userDefinedDefaultOutputInitialValue.setTypeEquals(BaseType.GENERAL);

        defaultOutputInitialValue = new Parameter(this,
                "Enable Default Output Initial Value", BooleanToken.TRUE);
        defaultOutputInitialValue.setTypeEquals(BaseType.BOOLEAN);

        fireUnexecutedActors = new Parameter(this, "fireUnexecutedActors",
                BooleanToken.FALSE);
        fireUnexecutedActors.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return the initialValueParameter Name for each of the port.
     * @param port The port to be analyzed.
     * @param channel The channel of the port to be analyzed.
     * @return The initial value parameter name.
     */
    protected static String _getInitialValueParameterName(TypedIOPort port,
            int channel) {

        if (port.isMultiport()) {
            return port.getName() + "_" + channel + "_InitialValue";
        } else {
            return port.getName() + "_InitialValue";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////package friendly variables                 ////

    // FIXME:  Currently disconnected graphs (or, connected graphs with unschedulable
    // upstream actors) are not allowed.
    // In the future they should be
    /** Cache of the value of allowDisconnectedGraphs. */
    // boolean _allowDisconnectedGraphs = false;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of sequenced actors in the model. */
    protected List<SequenceAttribute> _sequencedList;

    /** The scheduler for this director. */
    protected SequenceScheduler _scheduler;

    /** The value that the postfire method will return. */
    protected boolean _postfireReturns;

    /** The iteration count. */
    protected int _iterationCount = 0;

}
