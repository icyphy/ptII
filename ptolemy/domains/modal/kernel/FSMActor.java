/* An actor containing a finite state machine (FSM).

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.ActorExecutionAspect;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.ExecutionAspectHelper;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.RandomSource;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Function;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.ModalRefinement;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// FSMActor

/**
 An FSMActor contains a set of states and transitions. A transition has
 a <i>guard expression</i>, any number of <i>output actions</i>, and any
 number of <i>set actions</i>. It has an <i>initial state</i>, which is
 the unique state whose <i>isInitialState</i> parameter is true.
 In outline, a firing of this actor is a sequence of steps as
 follows. In the fire() method:
 <ol>
 <li> Read inputs.
 <li> Evaluate guards on outgoing transitions of the current state.
 <li> Choose a transitions whose guard is true.
 <li> Execute the output actions.
 </ol>
 In the postfire() method:
 <ol>
 <li> Execute the set actions of the chosen transition.
 <li> Change the current state to the destination of the chosen transition.
 </ol>
 The fire() method may be invoked more than once
 in an iteration, for example in a fixedpoint iteration.
 This actor makes no persistent state changes in
 its fire()  method, so actor conforms
 with the <i>actor abstract semantics</i>, and hence can be used in any
 Ptolemy II domain.
 <p>
 After reading the inputs, this actor examines
 the outgoing transitions of the current state, evaluating their
 guard expressions. A transition is <i>enabled</i> if its guard
 expression evaluates to true. A blank guard expression is
 interpreted to be always true. The guard expression may refer to any
 input port and any variable in scope.
 <p>
 If an input port name <i>portName</i> is used in a guard expression,
 it refers to the current input on that port on channel zero.
 If the input port status is not known, or if the input is absent,
 then a guard expression referring to <i>portName</i> will not be evaluated.
 The guard expression may alternatively refer to <i>portName</i>_<i>isPresent</i>,
 which is a boolean that is true if an input is present on the specified
 port. Again, if the input port status is not known, such a guard
 would not be evaluated. The status of an input port may not be
 known during firings under a director with fixed-point semantics,
 such as SR or Continuous.
 <p>
 To refer to a channel specifically, a guard expression may use
 <i>portName</i>_<i>channelIndex</i>, which has value equal to the token
 received on the port on the given channel. Similarly, it may refer
 to <i>portName</i>_<i>channelIndex</i>_<i>isPresent</i>.
 <p>
 FIXME: Document multirate behavior.
 <p>
 The identifier <i>portName</i>Array or
 <i>portName</i>_<i>channelIndex</i>Array refers the
 array of all tokens consumed from the port in the last firing.  This
 identifier has an array type whose element type is the type of the
 corresponding input port.
 <p>
 Nondeterministic transitions are allowed if all enabled transitions
 are marked <i>nondeterministic</i>. If more than one transition is
 enabled and they are all marked nondeterministic, then one is chosen
 at random in the fire() method. Note that this class provides no
 guarantees about the probability of selecting a particular
 nondeterministic transition. It is perfectly valid to always
 choose the same one, for example. To provide such a guarantee,
 we would have to impose the constraint that no nondeterministic
 transition can be chosen until the guards of all nondeterministic
 transitions can be evaluated. This would rule out certain models,
 in particular those that illustrate the celebrated Brock-Ackerman
 anomaly.  Hence, in this implementation,
 if the fire() method is invoked more
 than once in an iteration, then subsequent invocations in the same
 iteration will always choose the same transition, if it is still
 enabled. If more transitions become enabled in subsequent firings and
 they are not all marked nondeterminate, then an
 exception will thrown. All of this means that
 if some input is unknown on the first invocation
 of fire(), and a guard refers to that input, then that transition
 will not be chosen. As a consequence, for nondeterministic state
 machines, the behavior may depend on the order of firings in
 a fixed-point iteration. This is in fact unavoidable (it is
 related to the celebrated Brock-Ackerman anomaly, which demonstrates
 that the input/output relations of a nondeterministic system do
 not completely determine its behavior; the context in which it
 is used can also affect the behavior; specifically, the context
 may make it impossible to know the value of input on the first
 invocation of fire() because of a feedback loop). Thus, to
 correctly realize all nondeterministic systems, we cannot provide
 probabilistic execution of nondeterministic transitions.
 <p>
 If no transition is
 enabled and all their guard expressions have been evaluated (all relevant
 inputs are known), then if there is a transition marked as a
 <i>default transition</i>, then that transition is chosen. If
 there is more than one default transition and they are all marked
 nondeterministic, then one is chosen at random.
 <p>
 Once a transition is chosen, its output actions are executed.
 Typically, these will write values to output ports. The form of an output
 action is typically <i>y</i> = <i>expression</i>, where expression may
 refer to any variable defined as above or any parameter in scope
 (and also to outputs of state refinements, see below).
 This gives the behavior of a Mealy machine, where
 outputs are produced by transitions rather than by states. Moore machine
 behavior is also achievable using state refinements that produce
 outputs (see FSMDirector documentation).
 Multiple output actions may be given by separating them with semicolons.
 Also, output actions may take the form of <i>d.p</i> = <i>expression</i>,
 where <i>d</i> is the name of the destination state and <i>p</i> is a
 parameter of the destination refinement.
  <p>
 After a transition is taken, this actor calls fireAtCurrentTime()
 on its enclosing director. This ensures that if the destination
 state has an enabled transition, that transition will be taken
 at the same time (in the next superdense time index). It also
 supports continuous-time models, where the destination state
 refinement, if any, should produce an output at the next superdense
 time index.
 <p>
 A final state is a state that has its <i>isFinalState</i> parameter
 set to true. When the actor reaches a final state, then the
 postfire method will return false, indicating that the actor does not
 wish to be fired again.
 <p>
 An FSMActor can be used in a modal model to represent the mode
 control logic.  In this case, the states and transitions have
 refinements, and this actor works in concert with the FSMDirector
 to execute those refinements. See the documentation for
 FSMDirector for details on how that works.
 <p>
 By default, this actor has a conservative causality interface,
 implemented by the {@link DefaultCausalityInterface}, which declares
 that all outputs depend on all inputs. If, however, the enclosing
 director and all state refinement directors implement the
 strict actor semantics (as indicated by their
 implementsStrictActorSemantics() method), then the returned
 causality interface is
 implemented by the {@link FSMCausalityInterface} class. If
 the <i>stateDependentCausality</i> is false (the default),
 then this causality interface in conservative and valid in all
 states. If it is true, then the causality interface will show
 different input/output dependencies depending on the state.
 See {@link FSMCausalityInterface} for details.

 @author Edward A. Lee, Xiaojun Liu, Haiyang Zheng, Ye Zhou, Christian Motika
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Yellow (kienhuis)
 @see State
 @see Transition
 @see Action
 @see FSMDirector
 */
public class FSMActor extends CompositeEntity implements TypedActor,
ExplicitChangeContext {
    /** Construct an FSMActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public FSMActor() {
        super();
        _init();
    }

    /** Create an FSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public FSMActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an FSMActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public FSMActor(Workspace workspace) {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Parameter containing the cause of an exception thrown
     *  while executing a refinement if the exception is a
     *  KernelException that specified a Nameable.
     *  This parameter may be used in a guard
     *  expression or output or set action to access the object
     *  that originated an exception on an error transition.
     *  This is an object token that defaults to null.
     *  This parameter is not persistent. It will not be exported to MoML.
     */
    public Parameter errorCause;

    /** Parameter containing the name of the class of exception thrown
     *  while executing a refinement. This parameter may be used in a guard
     *  expression or output or set action to access the class of an
     *  exception on an error transition.
     *  This is a string that defaults to the empty string.
     *  This parameter is not persistent. It will not be exported to MoML.
     */
    public StringParameter errorClass;

    /** Parameter containing the message of exception thrown
     *  while executing a refinement. This parameter may be used in a guard
     *  expression or output or set action to access the message of an
     *  exception on an error transition.
     *  This is a string that defaults to the empty string.
     *  This parameter is not persistent. It will not be exported to MoML.
     */
    public StringParameter errorMessage;

    /** Parameter that is a function which evaluates to true
     * when the randomly generated token value is within the probability
     * range expressed by a transition.
     */
    public Parameter probability;

    /** Boolean parameter to determine whether seeds are reset on each run.
     */
    public SharedParameter resetOnEachRun;

    /** The seed to be used for random token generation, to evaluate
     * probabilistic transitions between states.
     */
    public SharedParameter seed;

    /** Parameter that is a function that evaluates to true when the
     *  time elapsed in the current state equals the argument to the
     *  function. This can be used in a guard to trigger a transition.
     */
    public Parameter timeout;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a chosen transition to the set of chosen transitions.
     *  There may be more than one chosen transition because the destination
     *  state of a chosen transition may have immediate transitions emerging
     *  from it.
     *  @param state The state that has the last chosen transition.
     *  @param transition The last chosen transition.
     *  @see #getLastChosenTransitions()
     *  @exception IllegalActionException If there is already a chosen
     *   transition associated with the specified state and it is not
     *   the same transition.
     */
    public void addChosenTransition(State state, Transition transition)
            throws IllegalActionException {
        Transition previouslyChosenTransition = _lastChosenTransitions
                .get(state);
        if (previouslyChosenTransition != null
                && previouslyChosenTransition != transition) {
            throw new IllegalActionException(this, transition,
                    "Cannot change chosen transition within a firing.");
        }
        if (previouslyChosenTransition != transition) {
            _lastChosenTransitions.put(state, transition);
        }
    }

    /** Add the specified object to the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** If the attribute is <i>seed</i>
     *  then create the base random number generator.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == seed) {
            long seedValue;
            if (seed != null) {
                seedValue = ((LongToken) seed.getToken()).longValue();
                _seed = seedValue;
                _createRandomGenerator();
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return false because backward type inference is not implemented
     *  for this actor.
     *  @return false
     */
    @Override
    public boolean isBackwardTypeInferenceEnabled() {
        return false;
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {

        // Thomas Feng writes: "Set the _instantiables in
        // super.clone() so that it contains the instantiables newly
        // added, whose setContainer() is called with the cloned
        // object as its container."
        // See $PTII/ptolemy/actor/gt/test/auto/InitializationBug.xml
        List<Initializable> oldInitializables = _initializables;
        _initializables = null;
        FSMActor newObject = (FSMActor) super.clone(workspace);
        _initializables = oldInitializables;
        // If the next line is uncommented, the InitializationBug.xml fails.
        // newObject._initializables = null;

        newObject._currentState = null;
        newObject._disabledRefinements = new HashSet<Actor>();
        newObject._inputTokenMap = new HashMap();
        newObject._lastChosenTransitions = new HashMap<State, Transition>();
        newObject._lastChosenTransition = null;
        newObject._lastTakenTransitions = new LinkedList<Transition>();
        newObject._stateRefinementsToPostfire = new LinkedList<Actor>();

        if (_initialState != null) {
            newObject._initialState = (State) newObject.getEntity(_initialState
                    .getName());
        }

        newObject._cachedInputPorts = null;
        newObject._cachedOutputPorts = null;
        newObject._causalityInterface = null;
        newObject._causalityInterfaces = null;
        newObject._causalityInterfacesVersions = null;
        newObject._causalityInterfaceDirector = null;
        newObject._connectionMaps = null;
        newObject._connectionMapsVersion = -1;
        newObject._currentConnectionMap = null;
        newObject._identifierToPort = new HashMap<String, IOPort>();
        newObject._identifierToPortVersion = -1;
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        newObject._receiversVersion = -1;
        newObject._tokenListArrays = null;
        newObject._transitionsPreviouslyChosenInIteration = new HashSet<Transition>();
        newObject._transitionRefinementsToPostfire = new LinkedList<Actor>();

        try {
            newObject.probability
            .setToken(newObject.new ProbabilityFunctionToken());
            newObject.timeout.setToken(newObject.new TimeoutFunctionToken());
        } catch (IllegalActionException e) {
            // Should not occur, because it didn't occur in the object being cloned.
            throw new CloneNotSupportedException(e.getMessage());
        }
        newObject._transitionEvaluatedTo = new HashMap<Transition, BooleanToken>();
        newObject._oldThreshold = 0.0;
        newObject._randomToken = null;

        return newObject;
    }

    /** Create receivers for each input port. In case the receivers
     *  don't need to be created they are reset
     *  @exception IllegalActionException If any port throws it.
     */
    @Override
    public void createReceivers() throws IllegalActionException {
        if (_receiversVersion != workspace().getVersion()) {
            _createReceivers();
            _receiversVersion = workspace().getVersion();
        } else {
            _resetReceivers();
        }

        _receiversVersion = workspace().getVersion();
    }

    /** Return the current state of this actor.
     *  @return The current state of this actor.
     */
    public State currentState() {
        return _currentState;
    }

    /** Return a list of enabled transitions among the given list of
     *  transitions. This includes all transitions whose guards can
     *  can be evaluated and evaluate to true, plus, if all guards can
     *  be evaluated and evaluate to false, all default transitions.
     *  <p>
     *  After calling this method, you can call foundUnknown()
     *  to determine whether any guard expressions
     *  were found in the specified transition list that
     *  referred to input ports that are not currently known.
     *  @param transitionList A list of transitions.
     *  @param preemptive True to consider only preemptive transitions,
     *   false to consider only non-preemptive transitions.
     *  @param immediateOnly True to consider only immediate transitions,
     *   false to consider both immediate and non-immediate transitions.
     *  @return A list of enabled transition.
     *  @exception IllegalActionException If the guard expression of any
     *  transition can not be evaluated.
     */
    public List enabledTransitions(List transitionList, boolean preemptive,
            boolean immediateOnly) throws IllegalActionException {
        LinkedList enabledTransitions = new LinkedList();
        LinkedList defaultTransitions = new LinkedList();

        Iterator transitionRelations = transitionList.iterator();

        _foundUnknown = false;
        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            if (immediateOnly) {
                boolean isImmediate = transition.isImmediate();
                if (!isImmediate) {
                    continue;
                }
            }
            if (preemptive && !transition.isPreemptive() || !preemptive
                    && transition.isPreemptive()) {
                continue;
            }
            boolean transitionRefersToUnknownInputs = !_referencedInputPortsByGuardKnown(transition);
            _foundUnknown = _foundUnknown || transitionRefersToUnknownInputs;
            if (transition.isDefault()) {
                if (_isTransitionEnabled(transition)) {
                    defaultTransitions.add(transition);
                }
            } else {
                if (_isTransitionEnabled(transition)) {
                    enabledTransitions.add(transition);
                }
            }
        }

        // NOTE: It is the chooseTransition method that decides which
        // enabled transition is actually taken. This method simply returns
        // all enabled transitions.
        if (enabledTransitions.size() > 0) {
            if (_debugging) {
                _debug("Enabled transitions: " + enabledTransitions);
            }
            return enabledTransitions;
        } else {
            // No enabled regular transitions. Check for default transitions.
            // Default transitions cannot become enabled until all
            // guard expressions can be evaluated.
            if (!_foundUnknown) {
                if (_debugging) {
                    if (defaultTransitions.size() > 0) {
                        _debug("Enabled default transitions: "
                                + defaultTransitions);
                    } else {
                        _debug("No enabled transitions.");
                    }
                }
                return defaultTransitions;
            }
        }
        // No enabled transitions were found, but some are not yet
        // known to disabled, so we cannot return a transition (even the
        // default transition).
        if (_debugging) {
            _debug("No enabled transitions.");
            if (_foundUnknown) {
                _debug("(some are not known to be disabled).");
            }
        }
        return new LinkedList();
    }

    /** Write this FSMActor into the output writer as a submodel. All
     *  refinements of the events in this FSMActor will be exported as
     *  configurations of those events, not as composite entities belonging to
     *  the closest modal model.
     *
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     */
    public void exportSubmodel(Writer output, int depth, String name)
            throws IOException {
        try {
            List<State> stateList = deepEntityList();
            for (State state : stateList) {
                state.saveRefinementsInConfigurer.setToken(BooleanToken.TRUE);
            }
            if (depth == 0 && getContainer() != null) {
                output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + _elementName + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");
            }
            super.exportMoML(output, depth, name);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex, "Unable to set "
                    + "attributes for the states.");
        } finally {
            List<State> stateList = deepEntityList();
            for (State state : stateList) {
                try {
                    state.saveRefinementsInConfigurer
                    .setToken(BooleanToken.FALSE);
                } catch (IllegalActionException e) {
                    // Ignore.
                }
            }
        }
    }

    /** Set the values of input variables. Choose the enabled transition
     *  among the outgoing transitions of the current state. Throw an
     *  exception if there is more than one transition enabled.
     *  Otherwise, execute the output actions contained by the chosen
     *  transition.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("************ Firing FSM. Current state: "
                    + _currentState.getName());
        }
        if (_firstFire) {
            _schedule(_currentState, getDirector().getModelTime());
            _firstFire = false;
        }

        Time environmentTime = _getEnvironmentTime();
        Director director = getDirector();
        boolean inModalModel = false;
        if (director instanceof FSMDirector) {
            inModalModel = true;
            director.setModelTime(environmentTime);
        }

        readInputs();

        // To support continuous-time models, we need to clear
        // the list of chosen transitions because, during an
        // iteration, the solver may backtrack, and transitions
        // that were previously enabled in the iteration will
        // no longer be enabled. Presumably, if the guards are
        // still enabled, then the same transitions will be
        // chosen again.
        _lastChosenTransitions.clear();
        _lastChosenTransition = null;

        // Some outputs may have to be forced to be unknown
        // if they are set on transitions that are not known
        // to be enabled.
        _outputsThatMustBeUnknown.clear();

        // Keep track during firing of all refinements
        // that are fired so that they can later be postfired.
        _transitionRefinementsToPostfire.clear();
        _stateRefinementsToPostfire.clear();

        if (_debugging) {
            _debug("** Checking preemptive transitions.");
        }

        // Choose transitions from the preemptive transitions,
        // including any immediate transitions that these lead to.
        List<Transition> transitionList = _currentState
                .nonErrorNonTerminationTransitionList();

        // The last argument ensures that we look at all transitions
        // not just those that are marked immediate.
        // Third to last argument ensures that we look only at preemptive transitions.
        // The following has the side effect of putting the chosen
        // transitions into the _lastChosenTransitions map of the controller.
        _chooseTransitions(transitionList, true, false, false, false);

        // If there is an enabled preemptive transition, then we know
        // that the current refinements cannot generate outputs, so we
        // may be able to assert that some outputs are absent.
        if (_lastChosenTransitions.size() > 0) {
            // In case the refinement port somehow accesses time, set it.
            if (inModalModel) {
                director.setModelTime(environmentTime);
            }

            // If the current (preempted) state has refinements, then
            // we know they cannot produce any outputs. All outputs of
            // this state must be cleared so that at least they do not
            // remain unknown at the end of the fixed point iteration.
            // If an output port is known because these preemptive
            // transitions already set it we do not send a clear.
            TypedActor[] refinements = _currentState.getRefinement();
            if (refinements != null) {
                for (Actor refinementActor : refinements) {
                    if (refinementActor instanceof CompositeActor) {
                        CompositeActor refinement = (CompositeActor) refinementActor;
                        for (IOPort refinementPort : (List<IOPort>) refinement
                                .outputPortList()) {
                            for (int i = 0; i < refinementPort.getWidth(); i++) {
                                if (!refinementPort.isKnown(i)) {
                                    if (_debugging) {
                                        _debug("--- Asserting absent output on "
                                                + refinementPort.getName()
                                                + ", channel " + i);
                                    }
                                    refinementPort.sendClear(i);
                                }
                            }
                        }// end for all ports
                    }// end if CompositeActor
                }// end for all refinements
            }// end if has refinement
        } else {
            // ASSERT: At this point, there are no enabled preemptive transitions.
            // It may be that some preemptive transition guards cannot be evaluated yet.
            if (!foundUnknown()) {
                // ASSERT: At this point, there are no enabled preemptive transitions,
                // and all preemptive transition guards, if any, have evaluated to false.
                // We can now fire the refinements.
                Actor[] stateRefinements = _currentState.getRefinement();

                if (stateRefinements != null) {
                    try {
                        for (int i = 0; i < stateRefinements.length; ++i) {
                            if (_stopRequested
                                    || _disabledRefinements
                                    .contains(stateRefinements[i])) {
                                continue;
                            }
                            _setTimeForRefinement(stateRefinements[i]);
                            if (stateRefinements[i].prefire()) {
                                if (_debugging) {
                                    _debug("Fire state refinement:",
                                            stateRefinements[i].getName());
                                }
                                // NOTE: If the state refinement is an FSMActor, then the following
                                // fire() method doesn't do the right thing. That fire() method does
                                // much less than this fire() method, and in particular, does not
                                // invoke refinements! This is fixed by using ModalModel in a
                                // hierarchical state.
                                if (_modelErrorHandled == null) {
                                    stateRefinements[i].fire();
                                    if (_modelErrorHandled == null) {
                                        _stateRefinementsToPostfire
                                        .add(stateRefinements[i]);
                                    }
                                }
                            }
                        }
                    } catch (Throwable ex) {
                        // Handle exceptions if there are error transitions.
                        _chooseErrorTransition(ex);
                        if (_lastChosenTransitions.size() > 0) {
                            // An error transition was chosen. We are done.
                            // Restore time before returning.
                            if (inModalModel) {
                                director.setModelTime(environmentTime);
                            }
                            return;
                        }
                        throw new IllegalActionException(this, ex,
                                "Exception occurred executing refinement.");
                    }
                    if (_modelErrorHandled != null) {
                        // A model error was thrown.
                        // Note that if it was a model errors, then the transition
                        // to take has already been identified in handleModelError().
                        // However, we need to choose again so that the output actions
                        // overwrite any outputs produced by the refinement.
                        _chooseErrorTransition(_modelErrorHandled);
                        // Restore time before returning.
                        if (inModalModel) {
                            director.setModelTime(environmentTime);
                        }
                        return;
                    }
                }
                if (inModalModel) {
                    director.setModelTime(environmentTime);
                }
                readOutputsFromRefinement();

                // Choose transitions from the nonpreemptive transitions,
                // including any immediate transitions that these lead to.
                if (_debugging) {
                    _debug("** Checking nonpreemptive transitions.");
                }
                // As a special case, if there are termination transitions
                // and all refinements of the current state are FSM refinements,
                // then we allow an termination transition to be chosen now if
                // all refinements have reached a final state. Allowing this now
                // means that the termination transition can produce outputs before
                // postfire, which is essential in SR and Continuous domains.
                List<Transition> terminationTransitions = _currentState
                        .terminationTransitionList();
                // Assume until proven otherwise that termination transitions should
                // not be checked.
                boolean checkTerminationTransitions = false;
                if (terminationTransitions.size() > 0) {
                    // There are termination transitions. Check the refinements.
                    TypedActor[] refinements = _currentState.getRefinement();
                    if (refinements != null && refinements.length > 0) {
                        // There are refinements. Assume until proven
                        // otherwise that termination transitions should be
                        // checked.
                        checkTerminationTransitions = true;
                        for (Actor refinementActor : refinements) {
                            if (refinementActor instanceof ModalRefinement) {
                                // We will check the guards of termination transitions only
                                // if all refinements are transitioning to a final state
                                // or are already in a final state.
                                ModalRefinement refinement = (ModalRefinement) refinementActor;
                                FSMActor refinementController = refinement
                                        .getController();
                                State destinationState = refinementController
                                        ._destinationState();
                                // If the current state is not a final state and we are not transitioning
                                // to a final state, then no termination transition can be enabled.
                                if (!((BooleanToken) refinementController
                                        .currentState().isFinalState.getToken())
                                        .booleanValue()
                                        && (destinationState == null || !((BooleanToken) destinationState.isFinalState
                                                .getToken()).booleanValue())) {
                                    // No chosen transition, or the destination
                                    // state is not final.
                                    // Cannot take termination transition.
                                    checkTerminationTransitions = false;
                                    break;
                                }
                            } else {
                                checkTerminationTransitions = false;
                                break;
                            }
                        }
                    }
                }

                if (checkTerminationTransitions) {
                    // The second to last argument ensures that we look at all transitions
                    // not just those that are marked immediate.
                    // The third from the last ensures that we look only at
                    // non-preemptive transitions.
                    _chooseTransitions(
                            _currentState.nonpreemptiveTransitionList(), false,
                            false, false, false);
                } else {
                    // The second to last argument ensures that we look at all transitions
                    // not just those that are marked immediate.
                    // The third from last ensures that we look only at
                    // non-preemptive transitions.
                    _chooseTransitions(transitionList, false, false, false,
                            false);
                }
            }
        }
        // Finally, assert any absent outputs that can be asserted absent.
        _assertAbsentOutputs(this);
    }

    /** Return true if the most recent call to enabledTransition()
     *  or chooseTransition() found guard expressions or output value
     *  expressions that could not be evaluated due to unknown inputs.
     *  Specifically, after calling {@link #enabledTransitions(List, boolean, boolean)},
     *  call this method to see whether there were guard expressions
     *  in the specified list that could not be evaluated. After
     *  calling
     *  {@link #_chooseTransitions(List, boolean, boolean, boolean, boolean)},
     *  call this to determine whether any guard expressions or output
     *  value expressions on a transition whose guard evaluates to
     *  true were found in the specified transition list that referred
     *  to input ports that are not currently known.
     *  @return True If guards or output value expressions could
     *   not be evaluated.
     */
    public boolean foundUnknown() {
        return _foundUnknown;
    }

    /** Return a causality interface for this actor. This
     *  method returns an instance of class
     *  {@link FSMCausalityInterface} if the enclosing director
     *  returns true in its implementsStrictActorSemantics() method.
     *  Otherwise, it returns an interface of class
     *  {@link DefaultCausalityInterface}.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        Director director = getDirector();
        Dependency defaultDependency = BooleanDependency.OTIMES_IDENTITY;
        if (director != null) {
            defaultDependency = director.defaultDependency();
            if (!director.implementsStrictActorSemantics()) {
                if (_causalityInterface != null
                        && _causalityInterfaceDirector == director) {
                    return _causalityInterface;
                }
                _causalityInterface = new DefaultCausalityInterface(this,
                        defaultDependency);
                _causalityInterfaceDirector = director;
                return _causalityInterface;
            }
        }
        boolean stateDependent = false;
        try {
            stateDependent = ((BooleanToken) stateDependentCausality.getToken())
                    .booleanValue();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to get the value of the stateDependentCausality parameter.");
        }
        if (!stateDependent) {
            if (_causalityInterface != null
                    && _causalityInterfaceDirector == director) {
                return _causalityInterface;
            }
            _causalityInterface = new FSMCausalityInterface(this,
                    defaultDependency);
            _causalityInterfaceDirector = director;
            return _causalityInterface;
        }
        // We need to return a different causality interface for each state.
        // Construct one for the current state if necessary.
        if (_causalityInterfacesVersions == null) {
            _causalityInterfacesVersions = new HashMap<State, Long>();
            _causalityInterfaces = new HashMap<State, FSMCausalityInterface>();
        }
        Long version = _causalityInterfacesVersions.get(_currentState);
        FSMCausalityInterface causality = _causalityInterfaces
                .get(_currentState);
        if (version == null || causality == null
                || version.longValue() != workspace().getVersion()) {
            // Need to create or update a causality interface for the current state.
            causality = new FSMCausalityInterface(this, defaultDependency);
            _causalityInterfaces.put(_currentState, causality);
            _causalityInterfacesVersions.put(_currentState,
                    Long.valueOf(workspace().getVersion()));
        }
        return causality;
    }

    /**
     * Return the change context being made explicit.  This class returns
     * this.
     * @return The change context being made explicit
     */
    @Override
    public Entity getContext() {
        return this;
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    @Override
    public Director getDirector() {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container instanceof CompositeActor) {
            return ((CompositeActor) container).getDirector();
        }

        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    @Override
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the initial state of this actor. The initial state is
     *  the unique state with its <i>isInitialState</i> parameter set
     *  to true. An exception is thrown if this actor does not contain
     *  an initial state.
     *  This method is read-synchronized on the workspace.
     *  @return The initial state of this actor.
     *  @exception IllegalActionException If this actor does not contain
     *   a state with the specified name.
     */
    public State getInitialState() throws IllegalActionException {
        // For backward compatibility, if the initialStateName
        // parameter and has been given, then use it to determine
        // the initial state.
        String name = initialStateName.getExpression();
        if (!name.equals("")) {
            try {
                workspace().getReadAccess();
                State state = (State) getEntity(name);
                if (state == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "initial state with name \"" + name + "\".");
                }
                state.isInitialState.setToken("true");
                state.isInitialState.setPersistent(true);
                _initialState = state;
                return _initialState;
            } finally {
                workspace().doneReading();
            }
        }
        if (_initialState == null) {
            throw new IllegalActionException(this,
                    "No initial state has been specified.");
        }
        return _initialState;
    }

    /** Get the last chosen transition from the current state.
     *  Note that this does not include chosen immediate transitions
     *  from the destination of the returned transition.
     *  @return The last chosen transition from the current state.
     *  @deprecated Use {@link #getLastChosenTransitions()} instead.
     *  @see #setLastChosenTransition(Transition)
     */
    @Deprecated
    public Transition getLastChosenTransition() {
        return _lastChosenTransitions.get(currentState());
    }

    /** Get the last chosen transitions.
     *  @return A map of last chosen transitions.
     *  @see #addChosenTransition(State,Transition)
     */
    public Map<State, Transition> getLastChosenTransitions() {
        return _lastChosenTransitions;
    }

    /** Get the last taken transitions.
     *  @return A list of last taken transition.
     *  @see #setLastChosenTransition(Transition)
     */
    public List<Transition> getLastTakenTransitions() {
        return _lastTakenTransitions;
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    @Override
    public Manager getManager() {
        try {
            _workspace.getReadAccess();

            CompositeEntity container = (CompositeEntity) getContainer();

            if (container instanceof CompositeActor) {
                return ((CompositeActor) container).getManager();
            }

            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of variables that this entity modifies.  The
     * variables are assumed to have a change context of the given
     * entity.  This method returns the destinations of all choice and
     * commit identifiers that are deeply contained by this actor.
     * Note that this actor is also used as the controller of modal
     * models and FSMDirector reports destinations of all choice and
     * commit identifiers, even those not contained by the finite
     * state machine.
     * @return A list of variables.
     * @exception IllegalActionException If a valid destination object can not
     * be found.
     * @see FSMDirector#getModifiedVariables()
     */
    @Override
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();

        // Collect assignments from FSM transitions
        for (Iterator states = entityList().iterator(); states.hasNext();) {
            State state = (State) states.next();

            for (Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator(); transitions.hasNext();) {
                Transition transition = (Transition) transitions.next();

                for (Iterator actions = transition.choiceActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();

                    for (Iterator names = action.getDestinationNameList()
                            .iterator(); names.hasNext();) {
                        String name = (String) names.next();
                        NamedObj object = action.getDestination(name);

                        if (object instanceof Variable && deepContains(object)) {
                            list.add(object);
                        }
                    }
                }

                for (Iterator actions = transition.commitActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();

                    for (Iterator names = action.getDestinationNameList()
                            .iterator(); names.hasNext();) {
                        String name = (String) names.next();
                        NamedObj object = action.getDestination(name);

                        if (object instanceof Variable && deepContains(object)) {
                            list.add(object);
                        }
                    }
                }
            }
        }

        return list;
    }

    /** Return a scope object that has current values from input ports
     *  of this FSMActor in scope.  This scope is used to evaluate
     *  guard expressions and set and output actions.
     *  @return A scope object that has current values from input ports of
     *  this FSMActor in scope.
     */
    public ParserScope getPortScope() {
        // FIXME: this could be cached.
        return new PortScope();
    }

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false if the
     *   error is not handled.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.
     */
    @Override
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {
        _chooseErrorTransition(exception);
        if (_lastChosenTransitions.size() > 0) {
            // An error transition is enabled.
            // To prevent the model error from being passed up the hierarchy,
            // we have to return true. Set a flag so that the fire() method
            // knows about this.
            _modelErrorHandled = exception;
            return true;
        }
        // There is no error transition enabled.
        // Pass the model up the hierarchy.
        return false;
    }

    /** Test whether new input tokens have been received at the input ports.
     *
     *  @return true if new input tokens have been received.
     */
    public boolean hasInput() {
        Iterator<?> inPorts = ((CompositeActor) getContainer()).inputPortList()
                .iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            Port port = (Port) inPorts.next();
            if (hasInput(port)) {
                return true;
            }
        }
        return false;
    }

    /** Test whether new input tokens have been received at the given input
     *  port.
     *
     *  @param port The input port.
     *  @return true if new input tokens have been received.
     */
    public boolean hasInput(Port port) {
        Token token = (Token) _inputTokenMap.get(port.getName() + "_isPresent");
        return token != null && BooleanToken.TRUE.equals(token);
    }

    /** Initialize this actor by setting the current state to the
     *  initial state.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {

        if (_debugging) {
            _debug("************ Initializing FSM.");
        }
        // Ensure previous input values are not available.
        _inputTokenMap.clear();
        errorMessage.setExpression("");
        errorClass.setExpression("");
        errorCause.setToken((Token) null);
        _modelErrorHandled = null;

        _transitionTaken = false;

        _transitionEvaluatedTo.clear();
        _oldThreshold = 0.0;

        // create generator
        if (_randomToken == null
                || ((BooleanToken) resetOnEachRun.getToken()).booleanValue()) {
            _createRandomGenerator();
        }
        _randomValue = _randomToken.nextDouble();

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }

        // Even though reset() is called in preinitialize(),
        // we have to call it again because if a reset transition is
        // taken, preinitialize() is not called.
        reset();

        _timeOfNextTimeoutExpiration = null;
        _timeEnteredCurrentState = getDirector().getModelTime();

        // Check for immediate transitions out of the initial state,
        // and also for any enabled transition (to request a refiring
        // at the current time.
        // NOTE: There is no current state when the FSMActor is in fact a Ptera
        // controller. (tfeng 05/12/2009)
        if (_currentState != null) {
            List transitionList = _currentState.outgoingPort
                    .linkedRelationList();
            if (_debugging) {
                _debug("** Checking immediate preemptive transitions.");
            }
            _chooseTransitions(transitionList, true, true, true, false);
            if (_lastChosenTransitions.size() > 0) {
                _transitionTaken = true;
            } else {
                if (_debugging) {
                    _debug("** Checking immediate non-preemptive transitions.");
                }
                _chooseTransitions(transitionList, false, true, true, false);
                if (_lastChosenTransitions.size() > 0) {
                    _transitionTaken = true;
                } else {
                    _transitionTaken = false;
                }
            }
            _commitLastChosenTransition(false, true);
            // Need to clear this again.
            _transitionsPreviouslyChosenInIteration.clear();

            // If there is a non-immediate transition enabled in the initial state,
            // then request a refiring at the current time.
            try {
                // State may have changed. Get new transition list.
                transitionList = _currentState.outgoingPort
                        .linkedRelationList();
                // Check both preemptive and non-preemptive transitions.
                if (_debugging) {
                    _debug("** Checking preemptive transitions to see whether to request a firing at the current time.");
                }
                List enabledTransitions = enabledTransitions(transitionList,
                        true, false);
                if (_debugging) {
                    _debug("** Checking non-preemptive transitions to see whether to request a firing at the current time.");
                }
                enabledTransitions.addAll(enabledTransitions(transitionList,
                        false, false));
                if (enabledTransitions.size() > 0) {
                    if (_debugging) {
                        _debug("A transition from the initial state is enabled. FSMActor requesting refiring by at "
                                + getDirector().getModelTime());
                    }
                    getDirector().fireAtCurrentTime(this);
                }
            } catch (IllegalActionException ex) {
                // Guards cannot yet be evaluated. Ignore.
            }
        }
        _firstFire = true;
    }

    /** Return a list of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input IOPort objects.
     */
    @Override
    public List inputPortList() {
        if (_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();

                // Update the cache.
                LinkedList inPorts = new LinkedList();
                Iterator ports = portList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    if (p.isInput()) {
                        inPorts.add(p);
                    }
                }

                _cachedInputPorts = inPorts;
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }

        return _cachedInputPorts;
    }

    /** Return false. During the fire() method, if a transition is enabled,
     *  it will be taken and the actions associated with this transition are
     *  executed. We assume the actions will change states of this actor.
     *
     *  @return False.
     */
    @Override
    public boolean isFireFunctional() {
        return false;
    }

    /** Return true.
     *  @return True.
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    /** Return false. This actor checks inputs to see whether
     *  they are known before evaluating guards, so it can fired
     *  even if it has unknown inputs.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean isStrict() throws IllegalActionException {
        return false;
        /* NOTE: This used to return a value as follows based
         * on the causality interface. But this is conservative
         * and prevents using the actor in some models.
        CausalityInterface causality = getCausalityInterface();
        int numberOfOutputs = outputPortList().size();
        Collection<IOPort> inputs = inputPortList();
        for (IOPort input : inputs) {
            // If the input is also output, skip it.
            // This is the output of a refinement.
            if (input.isOutput()) {
                continue;
            }
            try {
                if (causality.dependentPorts(input).size() < numberOfOutputs) {
                    return false;
                }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(this, ex,
                "Failed to get the dependent ports.");
            }
        }
        return true;
         */
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED. If stop() is called while
     *  this is executing, then cease executing and return STOP_ITERATING.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        int n = 0;

        while (n++ < count && !_stopRequested) {
            if (prefire()) {
                fire();

                if (!postfire()) {
                    return STOP_ITERATING;
                }
            } else {
                return NOT_READY;
            }
        }

        if (_stopRequested) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            //TypedIOPort p = new TypedIOPort(this, name);
            //return p;
            return new TypedIOPort(this, name);
        } catch (IllegalActionException ex) {
            // This exception should not occur.
            throw new InternalErrorException(this, ex,
                    "Failed to create a port named \"" + name + "\"");
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a new receiver obtained from the director.
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    @Override
    public Receiver newReceiver() throws IllegalActionException {
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }

        return director.newReceiver();
    }

    /** Create a new instance of Transition with the specified name in
     *  this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return A transition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    @Override
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();

            //Director director = getDirector();
            Transition tr = new Transition(this, name);
            return tr;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return a list of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output IOPort objects.
     */
    @Override
    public List outputPortList() {
        if (_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                _cachedOutputPorts = new LinkedList();

                Iterator ports = portList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    if (p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }

                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }

        return _cachedOutputPorts;
    }

    /** Execute actions on the last chosen transition. Change state
     *  to the destination state of the last chosen transition.
     *  @return True, unless stop() has been called, in which case, false.
     *  @exception IllegalActionException If any action throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("************ Postfiring FSM.");
        }

        Director director = getDirector();
        boolean inModalModel = false;
        if (director instanceof FSMDirector) {
            inModalModel = true;
        }

        // First, postfire any state refinements that were fired.
        Time environmentTime = _getEnvironmentTime();
        for (Actor stateRefinement : _stateRefinementsToPostfire) {
            if (_debugging) {
                _debug("Postfiring state refinment:", stateRefinement.getName());
            }
            _setTimeForRefinement(stateRefinement);
            if (!stateRefinement.postfire()) {
                _disabledRefinements.add(stateRefinement);
                // It is not correct for the modal model to return false
                // just because the refinement doesn't want to be fired anymore.
                // result = false;
            }
            if (inModalModel) {
                director.setModelTime(environmentTime);
            }
        }
        // Suspend all refinements of the current state, whether they were fired
        // or not. This is important because if a preemptive transition was taken,
        // then the refinement was not fired, but it should still be suspended.
        Actor[] refinements = _currentState.getRefinement();
        if (refinements != null) {
            for (Actor stateRefinement : refinements) {
                Director refinementDirector = stateRefinement.getDirector();
                if (_lastChosenTransitions.size() != 0
                        && refinementDirector != director) {
                    refinementDirector.suspend();
                }
            }
        }

        // Check for termination transitions.
        if (refinements != null && refinements.length > 0
                && _disabledRefinements.size() == refinements.length) {
            // All refinements have terminated. If no other
            // transition is enabled, see whether there is a termination transition.
            if (_lastChosenTransitions.size() == 0) {
                // Choose transitions from the termination transitions,
                // including any immediate transitions that these lead to.
                List<Transition> transitionList = _currentState
                        .terminationTransitionList();
                // The second last argument ensures that we look at all transitions
                // not just those that are marked immediate.
                // Third from last argument ensures that we look only at non-preemptive transitions.
                // The following has the side effect of putting the chosen
                // transitions into the _lastChosenTransitions map of the controller.
                _chooseTransitions(transitionList, false, false, false, false);
            }
        }

        // Notify all the refinements of the destination state that they are being
        // resumed.
        if (_lastChosenTransitions.size() != 0) {
            _transitionTaken = true;
            State destinationState = _destinationState();
            if (destinationState != null) {
                TypedActor[] destinationRefinements = destinationState
                        .getRefinement();
                if (destinationRefinements != null) {
                    for (TypedActor destinationRefinement : destinationRefinements) {
                        Director refinementDirector = destinationRefinement
                                .getDirector();
                        if (refinementDirector != director) {
                            refinementDirector.resume();
                        }
                    }
                }
            }
        } else {
            _transitionTaken = false;
        }

        // To ensure that nondeterministic transitions result in the
        // same choice anytime during an iteration, but that different
        // choices can be made in subsequent transitions, clear the
        // set of previously chosen transitions.
        _transitionsPreviouslyChosenInIteration.clear();

        // Commit transitions on the _lastChosenTransitions map.
        _commitLastChosenTransition(false, false);

        // Postfire any transition refinements that were fired in fire().
        for (Actor transitionRefinement : _transitionRefinementsToPostfire) {
            if (_debugging) {
                _debug("Postfiring transition refinment:",
                        transitionRefinement.getName());
            }
            if (!transitionRefinement.postfire()) {
                _disabledRefinements.add(transitionRefinement);
                // It is not correct for the modal model to return false
                // just because the refinement doesn't want to be fired anymore.
                // result = false;
            }
        }

        // Ensure previous input values are not available.
        _inputTokenMap.clear();
        errorMessage.setExpression("");
        errorClass.setExpression("");
        errorCause.setToken((Token) null);
        _modelErrorHandled = null;

        // generate a new random token for the next transition to be taken

        _transitionEvaluatedTo.clear();
        _oldThreshold = 0.0;
        _randomValue = _randomToken.nextDouble();

        if (_debugging) {
            _debug("** Finished postfire. New random number is :"
                    + _randomValue);
        }

        return !_reachedFinalState && !_stopRequested;
    }

    /** Return true.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Create receivers and input variables for the input ports of
     *  this actor, and validate attributes of this actor, and
     *  attributes of the ports of this actor. Set current state to
     *  the initial state.
     *  @exception IllegalActionException If this actor does not contain an
     *   initial state.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }

        _stopRequested = false;
        _reachedFinalState = false;

        _newIteration = true;
        _tokenListArrays = new Hashtable();

        _inputTokenMap.clear();
        errorMessage.setExpression("");
        errorClass.setExpression("");
        errorCause.setToken((Token) null);
        _modelErrorHandled = null;

        // In case any further static analysis depends on the initial
        // state, reset to that state here. For example, HDF sets
        // the production/consumption rates based on the refinement
        // of the initial state.
        reset();

        // The actual initial state may depend on immediate transitions
        // out of the initial state, and those transitions may set
        // relevant variables, so we have to traverse them here.
        // NOTE: There is no current state when the FSMActor is in fact a Ptera
        // controller. (tfeng 05/12/2009)
        if (_currentState != null) {
            List transitionList = _currentState.outgoingPort
                    .linkedRelationList();
            if (_debugging) {
                _debug("** Checking immediate preemptive transitions.");
            }
            _chooseTransitions(transitionList, true, true, true, true);
            if (_lastChosenTransitions.size() == 0) {
                if (_debugging) {
                    _debug("** Checking immediate non-preemptive transitions.");
                }
                _chooseTransitions(transitionList, false, true, true, true);
            }
            // NOTE: Have to be very careful here. This needs
            // to be an incomplete commit in that it cannot
            // initialize the destination refinement nor produce
            // outputs, because we are in preinitialize().
            // The destination refinement hasn't been preinitialized
            // yet.  See HDF Fibonnaci demo.
            _commitLastChosenTransition(true, true);
            // Need to clear this again.
            _transitionsPreviouslyChosenInIteration.clear();
        }
    }

    /** Set the value of the shadow variables for input ports of this actor.
     *  This method skips over ports that connected to outputs of a refinement.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    public void readInputs() throws IllegalActionException {
        Iterator inPorts = inputPortList().iterator();

        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            int width = p.getWidth();

            for (int channel = 0; channel < width; ++channel) {
                if (!_isRefinementOutput(p, channel)) {
                    _readInputs(p, channel);
                }
            }
        }
    }

    /** Set the input variables for channels that are connected to an
     *  output port of the refinement of current state.
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    public void readOutputsFromRefinement() throws IllegalActionException {
        Iterator inPorts = inputPortList().iterator();

        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            int width = p.getWidth();

            for (int channel = 0; channel < width; ++channel) {
                if (_isRefinementOutput(p, channel)) {
                    _readInputs(p, channel);
                }
            }
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Reset current state to the initial state.
     *  @exception IllegalActionException If thrown while
     *  getting the initial state or setting the current connection map.
     */
    public void reset() throws IllegalActionException {
        _reachedFinalState = false;
        _newIteration = true;
        _currentState = getInitialState();
        if (_debugging && _currentState != null) {
            _debug("Resetting to initial state: " + _currentState.getName());
        }
        _setCurrentConnectionMap();

        // To ensure that nondeterministic transitions result in the
        // same choice anytime during an iteration, but that different
        // choices can be made in subsequent transitions, clear the
        // set of previously chosen transitions.
        _transitionsPreviouslyChosenInIteration.clear();

        // Although this will be cleared at the start of fire(),
        // we need to clear it here as well before we potentially
        // choose immediate transitions.
        _lastChosenTransitions.clear();
        _lastChosenTransition = null;

        // Clear the list of refinements whose postfire() methods
        // have returned false.
        _disabledRefinements.clear();

        _transitionEvaluatedTo.clear();
        _oldThreshold = 0.0;

    }

    /** Set the last chosen transition. Note that this erases
     *  any previously set chosen transitions and makes the specified
     *  transition the only chosen transition, ignoring immediate
     *  transitions that this might lead to.
     *  @param transition The last chosen transition.
     *  @deprecated Use addChosenTransition(State, Transition)
     *  @see #getLastChosenTransition()
     */
    @Deprecated
    public void setLastChosenTransition(Transition transition) {
        _lastChosenTransitions.clear();
        _lastChosenTransition = null;
        if (transition != null) {
            _lastChosenTransitions.put(currentState(), transition);
            _lastChosenTransition = transition;
        }
    }

    /** Set the flag indicating whether we are at the start of
     *  a new iteration (firing).  Normally, the flag is set to true.
     *  It is only set to false in HDF.
     *  @param newIteration A boolean variable indicating whether this is
     *  a new iteration.
     */
    public void setNewIteration(boolean newIteration) {
        _newIteration = newIteration;
    }

    /** Set true indicating that this actor supports multirate firing.
     *  @param supportMultirate A boolean variable indicating whether this
     *  actor supports multirate firing.
     */
    public void setSupportMultirate(boolean supportMultirate) {
        _supportMultirate = supportMultirate;
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible.  In this class, we set a flag indicating that
     *  this request has been made (the protected variable _stopRequested).
     *  This will result in postfire() returning false.
     */
    @Override
    public void stop() {
        _stopRequested = true;
    }

    /** Do nothing.
     */
    @Override
    public void stopFire() {
    }

    /** Call stop().
     */
    @Override
    public void terminate() {
        stop();
    }

    /** Return the type constraints of this actor. The constraints
     *  have the form of a set of inequalities. This method first
     *  creates constraints such that the type of any input port that
     *  does not have its type declared must be less than or equal to
     *  the type of any output port that does not have its type
     *  declared. Type constraints from the contained Typeables
     *  (ports, variables, and parameters) are collected. In addition,
     *  type constraints from all the transitions are added. These
     *  constraints are determined by the guard and trigger expressions
     *  of transitions, and actions contained by the transitions.
     *  This method is read-synchronized on the workspace.
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public Set<Inequality> typeConstraints() {
        try {
            _workspace.getReadAccess();

            Set<Inequality> result = new HashSet<Inequality>();

            // Collect constraints from contained Typeables.
            Iterator ports = portList().iterator();

            while (ports.hasNext()) {
                Typeable port = (Typeable) ports.next();
                result.addAll(port.typeConstraints());
            }

            // Collect constraints from contained HasTypeConstraints
            // attributes.
            Iterator attributes = attributeList(HasTypeConstraints.class)
                    .iterator();

            while (attributes.hasNext()) {
                HasTypeConstraints typeableAttribute = (HasTypeConstraints) attributes
                        .next();
                result.addAll(typeableAttribute.typeConstraints());
            }

            // Collect constraints from all transitions.
            Iterator transitionRelations = relationList().iterator();

            while (transitionRelations.hasNext()) {
                Relation tr = (Relation) transitionRelations.next();
                attributes = tr.attributeList(HasTypeConstraints.class)
                        .iterator();

                while (attributes.hasNext()) {
                    HasTypeConstraints typeableAttribute = (HasTypeConstraints) attributes
                            .next();
                    result.addAll(typeableAttribute.typeConstraints());
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if a transition was taken in the previous
     *  iteration, or if this is before the first iteration concludes,
     *  true if during initialize an immediate transition was taken.
     *  @return True if a transition was taken.
     */
    public boolean wasTransitionTaken() {
        return _transitionTaken;
    }

    /** Do nothing except invoke the wrapup method of any objects
     *  that have been added using addInitializable().
     *  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying the names of the final states of this
     *  actor. This attribute is kept for backward compatibility only,
     *  and is set to expert visibility. To set the final states,
     *  set the <i>isFinalState</i> parameter of a States.
     */
    public StringAttribute finalStateNames = null;

    /** Attribute specifying the name of the initial state of this
     *  actor. This attribute is kept for backward compatibility only,
     *  and is set to expert visibility. To set the initial state,
     *  set the <i>isInitialState</i> parameter of a State.
     */
    public StringAttribute initialStateName = null;

    /** Indicate whether input/output dependencies can depend on the
     *  state. By default, this is false (the default), indicating that a conservative
     *  dependency is provided by the causality interface. Specifically,
     *  if there is a dependency in any state, then the causality interface
     *  indicates that there is a dependency. If this is true, then a less
     *  conservative dependency is provided, indicating a dependency only
     *  if there can be one in the current state.  If this is true, then
     *  upon any state transition, this actor issues a change request, which
     *  forces causality analysis to be redone. Note that this can be expensive.
     */
    public Parameter stateDependentCausality;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a state to this FSMActor. This overrides the base-class
     *  method to make sure the argument is an instance of State.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to contain.
     *  @exception IllegalActionException If the state has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of State.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the state list.
     */
    @Override
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof State)) {
            throw new IllegalActionException(this, entity,
                    "FSMActor can only contain entities that "
                            + "are instances of State.");
        }

        super._addEntity(entity);
    }

    /** Add a transition to this FSMActor. This method should not be used
     *  directly.  Call the setContainer() method of the transition instead.
     *  This method does not set the container of the transition to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Transition to contain.
     *  @exception IllegalActionException If the transition has no name, or
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    @Override
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof Transition)) {
            throw new IllegalActionException(this, relation,
                    "FSMActor can only contain instances of Transition.");
        }

        super._addRelation(relation);

        if (_debugging) {
            relation.addDebugListener(new StreamListener());
        }
    }

    /** Return true if all immediate transitions from
     *  the specified state have guards that can be evaluated
     *  and that evaluate to false. Note that this will return
     *  true if there are no immediate transitions.
     *  @param state The state to check for immediate transitions.
     *  @return true If there are no immediate transitions or if
     *   they are all disabled.
     * @exception IllegalActionException If the guard expression cannot be parsed
     *  or if it cannot yet be evaluated.
     */
    protected boolean _areAllImmediateTransitionsDisabled(State state)
            throws IllegalActionException {
        List<Transition> transitionList = state.outgoingPort
                .linkedRelationList();
        for (Transition transition : transitionList) {
            if (transition.isImmediate()) {
                if (!_referencedInputPortsByGuardKnown(transition)) {
                    return false;
                }
                if (_isTransitionEnabled(transition)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Choose zero or more transitions enabled in the current
     *  state from the list of specified transitions. This method
     *  follows chains of immediate transitions, if there are any.
     *  As a side effect, the controller's _lastChosenTransitions
     *  protected variable will contain the chosen transitions.
     *  @param transitionList The candidate transitions.
     *  @param preemptive True to consider only preemptive transitions,
     *   and false to consider only non-preemptive transitions.
     *  @param immediateOnly If true, look only at immediate
     *   transitions from the current state. Otherwise, look
     *   at both immediate and non-immediate transitions.
     *  @param inInitialize True when this method is called
     *   from initialize or preinitialize, in which case, firing and initializing
     *   refinements is not allowed; note that the refinements
     *   will be initialized by the initialize method, but this
     *   prevents them from being initialized more than once.
     *   This could be important if, for example, the refinement
     *   produces an output during initialize in a domain where
     *   outputs are consumed, such as SDF.
     *  @param inPreinitialize True if this is being called in
     *   preinitialize. Outputs must not be produced in preinitialize,
     *   so we don't execute output actions if this argument is true.
     *  @exception IllegalActionException If something goes wrong.
     */
    protected void _chooseTransitions(List<Transition> transitionList,
            boolean preemptive, boolean immediateOnly, boolean inInitialize,
            boolean inPreinitialize) throws IllegalActionException {
        Transition chosenTransition = _chooseTransition(_currentState,
                transitionList, preemptive, immediateOnly, inInitialize,
                inPreinitialize);

        // A self-loop that is immediate is not allowed, because if it is enabled,
        // it implied an infinite number of traversals.
        if (chosenTransition != null && chosenTransition.isImmediate()
                && chosenTransition.destinationState() == _currentState) {
            throw new IllegalActionException(_currentState, this,
                    "Self loop cannot be an immediate transition.");
        }

        // The destination of the chosen transition may be transient,
        // so we should also choose transitions on the destination state.
        // The following set is used
        // to detect cycles of immediate transitions.
        // NOTE: cmot, should the visitedStates not be part of the fire function and reset
        // once a normal transition is taken? Maybe a fire method is called twice and that
        // would hide an immediate cycle (because visitedStates is reset for the second call). Both
        // calls and consecutive ones may happen in the same fixed point iteration.
        // NOTE: eal, No, this seems OK to me. A repeated firing will choose at least
        // the same transitions. The repeated firing also starts from the same _currentState.
        HashSet<State> visitedStates = new HashSet<State>();
        while (chosenTransition != null) {
            State nextState = chosenTransition.destinationState();
            if (visitedStates.contains(nextState)) {
                throw new IllegalActionException(nextState, this,
                        "Cycle of immediate transitions found.");
            }
            visitedStates.add(nextState);

            transitionList = nextState.outgoingPort.linkedRelationList();
            // The last argument ensures that we look only at transitions
            // that are marked immediate.
            if (_debugging) {
                _debug("** Checking for immediate transitions out of the next state: "
                        + nextState.getName());
            }

            // Try preemptive transitions first, then non-preemptive.
            chosenTransition = _chooseTransition(nextState, transitionList,
                    true, true, inInitialize, inPreinitialize);
            if (chosenTransition == null) {
                // Only try non-preemptive transitions if no preemptive transition
                // is enabled.
                chosenTransition = _chooseTransition(nextState, transitionList,
                        false, true, inInitialize, inPreinitialize);
            }
        }
    }

    /** Fire all refinements of the state if it has any refinements.
     *  @exception IllegalActionException If any commit action throws it,
     *   or the last chosen transition does not have a destination state.
     */
    private void _fireStateRefinements(State state)
            throws IllegalActionException {
        // Execute state refinement here: If the chosenTransition is not null and weak,
        // and the current state has a refinement, we need to fire it
        Actor[] stateRefinements = state.getRefinement();
        if (stateRefinements != null && stateRefinements.length > 0) {
            // ASSERT: At this point, there are no enabled preemptive transitions,
            // and all preemptive transition guards, if any, have evaluated to false.
            // We can now fire the refinements.
            for (int i = 0; i < stateRefinements.length; ++i) {
                if (_stopRequested
                        || _disabledRefinements.contains(stateRefinements[i])) {
                    break;
                }
                _setTimeForRefinement(stateRefinements[i]);
                if (stateRefinements[i].prefire()) {
                    if (_debugging) {
                        _debug("Fire transient state refinement:",
                                stateRefinements[i].getName());
                    }
                    // NOTE: If the state refinement is an FSMActor, then the following
                    // fire() method doesn't do the right thing. That fire() method does
                    // much less than this fire() method, and in particular, does not
                    // invoke refinements! This is fixed by using ModalModel in a
                    // hierarchical state.
                    stateRefinements[i].fire();
                    _stateRefinementsToPostfire.add(stateRefinements[i]);
                }
            }
        }
    }

    /** Return the chosen destination state. This method follows
     *  the chain of chosen transitions from the current state
     *  to the new state, possibly traversing several immediate
     *  transitions.
     *  @return The state that will be the current state after
     *   all chosen transitions are taken, or null if there are no
     *   chosen transitions.
     * @exception IllegalActionException If no controller is found.
     */
    protected State _destinationState() throws IllegalActionException {
        Transition chosenTransition = _lastChosenTransitions.get(_currentState);
        if (chosenTransition == null) {
            return null;
        }
        State destinationState = chosenTransition.destinationState();
        Transition nextTransition = _lastChosenTransitions
                .get(destinationState);
        while (nextTransition != null) {
            State newDestinationState = nextTransition.destinationState();
            if (newDestinationState == destinationState) {
                // Found a self loop.
                return destinationState;
            }
            nextTransition = _lastChosenTransitions.get(newDestinationState);

        }
        return destinationState;
    }

    /** Given an identifier, return a channel number i if the identifier is of
     *  the form portName_i, portName_i_isPresent, portName_iArray.
     *  Otherwise, return -1.
     *  @param identifier An identifier.
     *  @return A channel index, if the identifier refers to one.
     *  @exception IllegalActionException If getting the width of the port fails.
     */
    protected int _getChannelForIdentifier(String identifier)
            throws IllegalActionException {
        Port port = _getPortForIdentifier(identifier);
        if (port != null) {
            String portName = port.getName();
            if (identifier.startsWith(portName + "_")) {
                String channel = identifier.substring(portName.length() + 1);
                if (channel.endsWith("Array")) {
                    channel = channel.substring(0, channel.length() - 5);
                }
                if (channel.endsWith("isPresent")) {
                    channel = channel.substring(0, channel.length() - 9);
                }
                // Apparently, the syntax has been variably name_index_isPresent
                // and name_indexisPresent (without the second underscore).
                // Tolerate both syntaxes.
                if (channel.endsWith("_")) {
                    channel = channel.substring(0, channel.length() - 1);
                }
                if (channel.length() > 0) {
                    return Integer.decode(channel);
                }
            }
        }
        return -1;
    }

    /** Get the port for the specified identifier, which may be of
     *  form portName, portName_isPresent, portName_i, portName_i_isPresent,
     *  etc.
     *  @param identifier The specified identifier.
     *  @return The port that corresponds with the specified identifier.
     *  @exception IllegalActionException If getting the width of the port fails.
     */
    protected IOPort _getPortForIdentifier(String identifier)
            throws IllegalActionException {
        if (workspace().getVersion() != _identifierToPortVersion) {
            _setIdentifierToPort();
            _identifierToPortVersion = workspace().getVersion();
        }
        return _identifierToPort.get(identifier);
    }

    /** Return the list used to keep track of refinements that have been
     *  fired. This is protected so that FSMDirector can mirror it with
     *  its own protected method so that subclasses of FSMDirector can
     *  access it.
     *  @return A list of actors to postfire.
     */
    protected List<Actor> _getStateRefinementsToPostfire() {
        return _stateRefinementsToPostfire;
    }

    /** Return the list used to keep track of refinements that have been
     *  fired. This is protected so that FSMDirector can mirror it with
     *  its own protected method so that subclasses of FSMDirector can
     *  access it.
     *  @return A list of actors to postfire.
     */
    protected List<Actor> _getTransitionRefinementsToPostfire() {
        return _transitionRefinementsToPostfire;
    }

    /** Initialize the refinements of the specified state.
     *  @param state The state.
     *  @exception IllegalActionException If initialization fails.
     */
    protected void _initializeRefinements(State state)
            throws IllegalActionException {
        Actor[] actors = state.getRefinement();
        if (actors != null) {
            Director executiveDirector = getExecutiveDirector();
            for (int i = 0; i < actors.length; ++i) {
                if (_debugging) {
                    _debug(getFullName() + " initialize refinement: "
                            + ((NamedObj) actors[i]).getName());
                }
                // NOTE: For a modal model, the executive director will normally
                // be an FSMDirector. Here we communicate with that director
                // to ensure that it reports the correct superdense time index
                // during initialization, which is one greater than the current
                // superdense index of its context. If the enclosing director
                // is not an FSMDirector, then the initialize() method below
                // will likely set the index to one less than it should be.
                // I don't have a solution for this, but this situation is
                // unlikely to arise except in very weird models, since the
                // standard pattern is for FSMDirector and FSMActor to work
                // together.
                if (executiveDirector instanceof FSMDirector) {
                    try {
                        ((FSMDirector) executiveDirector)._indexOffset = 1;
                        actors[i].initialize();
                    } finally {
                        ((FSMDirector) executiveDirector)._indexOffset = 0;
                    }
                    _disabledRefinements.remove(actors[i]);
                } else {
                    actors[i].initialize();
                }
            }
        }
    }

    /** Return true if the channel of the port is connected to an output
     *  port of the refinement of current state. If the current state
     *  does not have refinement, return false.
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @return True if the channel of the port is connected to an output
     *   port of the refinement of current state.
     *  @exception IllegalActionException If the refinement specified for
     *   one of the states is not valid.
     */
    protected boolean _isRefinementOutput(IOPort port, int channel)
            throws IllegalActionException {
        // Ptera may not have a _currentState.
        if (_currentState == null) {
            return false;
        }
        TypedActor[] refinements = _currentState.getRefinement();

        if (refinements == null || refinements.length == 0) {
            return false;
        }

        if (_connectionMapsVersion != workspace().getVersion()) {
            _setCurrentConnectionMap();
        }

        boolean[] flags = (boolean[]) _currentConnectionMap.get(port);
        return flags[channel];
    }

    /** Given an output port and channel, determine whether the
     *  output port must be absent on the specified channel, given whatever
     *  current information about the inputs is available (the inputs
     *  may be known or unknown).
     *  <p>
     *  The way this works is that it examines all the outgoing
     *  transitions of the current state. If the guard on the transition
     *  can be evaluated to false, then as far as this transition is
     *  concerned, the output port can be absent.
     *  Otherwise, two things happen. First, we check to see whether
     *  the output actions of a transition writes to the specified
     *  port. Second, we look at the destination state of the
     *  transition and examine all immediate transition emanating
     *  from that state. If none of the transitions makes an assignment
     *  to the output port, then we can safely
     *  assert that the output is absent, with one exception.
     *  If the output port already has a value, that value was probably
     *  set by another refinement. Thus, we should leave it alone.
     *  <p>
     *  This method ignores any state refinements, and consequently
     *  its analysis is valid only if all state refinements also assert
     *  that the output is absent.
     *
     *  @param port The IOPort in question.
     *  @param channel The channel in question.
     *  @param state The state whose transitions are examined.
     *  @param immediateOnly True to examine only immediate transitions.
     *  @param visitedStates The set of states already visited, or null
     *   if none have yet been visited.
     *  @return True, if successful.
     *  @exception IllegalActionException If we cannot determine whether
     *   the port is known and absent.
     */
    protected boolean _isSafeToClear(IOPort port, int channel, State state,
            boolean immediateOnly, HashSet<State> visitedStates)
                    throws IllegalActionException {
        if (_debugging) {
            _debug("Calling _isSafeToClear() on port: " + port.getFullName());
        }

        // If this FSMActor is inside a refinement, then it's possible
        // that another refinement wants to set the value of an output port.
        // We need to allow it to.
        if (getContainer() instanceof RefinementActor) {
            // Check to see whether the output port destinations are known.
            if (port.isKnown(channel)) {
                // Check to see whether the destinations are present.
                // Can't use port.hasToken(channel) to determine whether the
                // know value is absent because this is not an input port.
                // Have to traverse to the destinations to find out.
                Receiver[][] receivers = port.getRemoteReceivers();

                if (receivers.length <= channel) {
                    throw new IllegalActionException(this,
                            "Channel index is out of range: " + channel);
                }

                // Presumably, if one destination has a token, then they all
                // should. This assumes we are inside modal model
                if (receivers[channel] != null) {
                    for (int j = 0; j < receivers[channel].length; j++) {
                        if (!receivers[channel][j].hasToken()) {
                            return false;
                        }
                    }
                }
            }
        }

        List<Transition> transitionList = state.outgoingPort
                .linkedRelationList();
        for (Transition transition : transitionList) {
            
            _transitionBeingTested = transition;
            if (immediateOnly && !transition.isImmediate()) {
                // Skip the transition.
                continue;
            }
            // Next check to see whether the transition can be
            // evaluated to false. This will throw an exception
            // if there is not enough information to evaluate
            // the guard.
            try {
                if (!transition.isEnabled()) {
                    // Transition is assured of not being
                    // enabled, so we can ignore it.
                    continue;
                }
            } catch (IllegalActionException ex) {
                // Guard cannot be evaluated. Therefore,
                // we have to check it.
            }
            // ASSERT: Either the guard cannot be evaluated
            // or it evaluates to true at this point.

            // First, recursively check immediate transitions
            // emanating from the destination state.
            State destinationState = transition.destinationState();
            // Guard against cycles!!
            if (visitedStates == null) {
                visitedStates = new HashSet<State>();
                visitedStates.add(state);
            }
            if (!visitedStates.contains(destinationState)) {
                visitedStates.add(destinationState);
                // Have not checked the destination state. Check it now.
                // The "true" argument asks for only immediate transitions to be checked.
                if (!_isSafeToClear(port, channel, destinationState, true,
                        visitedStates)) {
                    // An immediate transition somewhere downstream may
                    // assign a value to the port, so it is not safe
                    // to clear the port.
                    return false;
                }
            }
            // ASSERT: At this point, the transition may be
            // enabled (now or later), and all downstream immediate
            // transitions assert that as far as they are concerned,
            // the port is safe to clear. So now, we should check
            // to see whether this transition assigns a value to the
            // port.

            // FIXME: The implementation should not re-parse the output
            // actions and get the information from the parsed AST.
            // This code already exists somewhere... Where?
            String outputActionsExpression = transition.outputActions
                    .getExpression();
            String regexp = "(^|((.|\\s)*\\W))" + port.getName()
                    + "\\s*=[^=](.|\\s)*";
            boolean transitionWritesToThePort = outputActionsExpression.trim()
                    .matches(regexp);

            if (transitionWritesToThePort) {
                // The transition does include an assignement to the port, so
                // it is not safe to clear the port.
                return false;
            }
        } // Continue to the next transition.

        // ASSERT: At this point, no transition can possibly write
        // to the output, so it is safe to clear.
        return true;
    }

    /** Read tokens from the given channel of the given input port and
     *  make them accessible to the expressions of guards and
     *  transitions through the port scope.  If the specified port is
     *  not an input port, then do nothing.
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @exception IllegalActionException If the port is not contained by
     *   this actor.
     */
    protected void _readInputs(IOPort port, int channel)
            throws IllegalActionException {
        String portName = port.getName();

        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot read inputs from port "
                            + "not contained by this FSMActor.");
        }

        if (!port.isInput()) {
            return;
        }

        // Do not overwrite the port if it already has a token.
        // This is essential for he imperative behavior of multiple
        // refinements.
        if (port.isKnown(channel)) {
            if (_supportMultirate) {
                // FIXME: The following implementation to support multirate is
                // rather expensive. Try to optimize it.

                // FIXME: This does not look right. It reads all available tokens.
                // Shouldn't it read exactly the number to consume?
                // It could end up consuming tokens that will be needed on a
                // subsequent firing!
                int width = port.getWidth();

                // If we're in a new iteration, reallocate arrays to keep
                // track of HDF data.
                if (_newIteration && channel == 0) {
                    List[] tokenListArray = new LinkedList[width];

                    for (int i = 0; i < width; i++) {
                        tokenListArray[i] = new LinkedList();
                    }

                    _tokenListArrays.put(port, tokenListArray);
                }

                // Get the list of tokens for the given port.
                List[] tokenListArray = (LinkedList[]) _tokenListArrays
                        .get(port);

                // Update the value variable if there is/are token(s) in
                // the channel. The HDF(SDF) schedule will guarantee there
                // are always enough tokens.
                // Limit the number of tokens read to the consumption rate
                // of the port, which by default is 1. If the MultirateFSMDirector
                // is used, however, it may set the consumption rate other than one.
                int numberOfTokensToRead = DFUtilities
                        .getTokenConsumptionRate(port);
                int count = 0;
                while (port.hasToken(channel) && count < numberOfTokensToRead) {
                    Token token = port.get(channel);
                    count++;

                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel + ") has ",
                                token.toString() + " at time "
                                        + getDirector().getModelTime());
                    }

                    tokenListArray[channel].add(0, token);
                }

                int length = tokenListArray[channel].size();
                if (_debugging) {
                    _debug("Total tokens available at port: "
                            + port.getFullName() + " is  " + length);
                }

                if (length > 0) {
                    Token[] tokens = new Token[length];
                    tokenListArray[channel].toArray(tokens);
                    _setInputTokenMap(port, channel, tokens[0], tokens);
                } else {
                    // There is no data. Just set the _isPresent variables to false.
                    _setInputTokenMap(port, channel, null, null);
                }
            } else {
                // If not supporting multirate firing,
                // Update the value variable if there is a token in the channel.
                if (port.hasToken(channel)) {
                    Token token = port.get(channel);

                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel + ") has ",
                                token.toString() + " at time "
                                        + getDirector().getModelTime());
                    }
                    _setInputTokenMap(port, channel, token, null);
                } else {
                    // There is no data. Check to see whether the token
                    // map has been previously set, and if not,
                    // set the _isPresent variables to false.
                    // NOTE: The test will only work if the _inputTokenMap is reset
                    // between iterations. Ptera does not do this!!!
                    // if (_inputTokenMap.get(portName + "_isPresent") == null) {
                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel + ") has ",
                                "no tokens at time "
                                        + getDirector().getModelTime());
                    }
                    _setInputTokenMap(port, channel, null, null);
                    // }
                }
            }
        } else {
            // Remove identifiers so that previous values are not erroneously
            // read.
            _removePortVariables(portName, channel);
        }
    }

    /** Set the map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    protected void _setCurrentConnectionMap() throws IllegalActionException {
        if (_connectionMapsVersion != workspace().getVersion()) {
            _buildConnectionMaps();
        }

        _currentConnectionMap = (Map) _connectionMaps.get(_currentState);
    }

    /** Set the refinements current time equal to the matching environment,
     *  or if there is no environment, do nothing.
     *  @param refinement The refinement.
     *  @exception IllegalActionException If setModelTime() throws it.
     */
    protected void _setTimeForRefinement(Actor refinement)
            throws IllegalActionException {
        Actor container = (Actor) getContainer();
        Director director = getDirector();
        if (!(director instanceof FSMDirector)) {
            throw new IllegalActionException(this,
                    "State refinements are only supported within ModalModel.");
        }
        Director executiveDirector = container.getExecutiveDirector();
        if (executiveDirector != null) {
            Time environmentTime = executiveDirector.getModelTime();
            /* FIXME: This is now handled by the director.
            Director refinementDirector = refinement.getDirector();
            if (refinementDirector instanceof Suspendable && refinementDirector != director) {
                // Adjust current time to be the environment time minus
                // the accumulated suspended time of the refinement.
                Time suspendedTime = ((Suspendable) refinementDirector)
                        .accumulatedSuspendTime();
                if (suspendedTime != null) {
                    director.setModelTime(environmentTime.subtract(suspendedTime));
                    ((FSMDirector)director)._currentOffset = suspendedTime;
                    return;
                }
            }
             */
            director.setModelTime(environmentTime);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** Current state. */
    protected State _currentState = null;

    /** State and transition refinements that have returned false in postfire(). */
    protected Set<Actor> _disabledRefinements = new HashSet<Actor>();

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    protected transient List<Initializable> _initializables;

    /** A map from ports to corresponding input variables. */
    protected Map _inputTokenMap = new HashMap();

    /** The most recently chosen transition within the fire() method. */
    protected Transition _lastChosenTransition;

    /** The last chosen transitions, by state from which these transitions emerge. */
    protected HashMap<State, Transition> _lastChosenTransitions = new HashMap<State, Transition>();

    /** The last taken transitions, by state from which these transitions emerge. */
    protected List<Transition> _lastTakenTransitions = new LinkedList<Transition>();

    /** State refinements to postfire(), as determined by the fire() method. */
    protected List<Actor> _stateRefinementsToPostfire = new LinkedList<Actor>();

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                package friendly variables                 ////

    /** The initial state. This is package friendly so that State can
     *  access it.
     */
    State _initialState = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * For the given controller FSM, set all outputs that are
     * currently unknown to absent if it
     * can be determined to be absent given the current state and possibly
     * partial information about the inputs (some of the inputs may be
     * unknown). If the current state has any refinements that are not
     * FSMs, then return false. It is not safe to assert absent outputs
     * because we have no visibility into what those refinements do with
     * the outputs.
     * <p>
     * This method first explores any FSM refinements of the current
     * state. If those refinements are all FSMs and they are all able
     * to assert that an output is absent, then explore this FSM
     * to determine whether it also can assert that the output is absent.
     * If all the refinements and the specified FSM agree that an
     * output is absent, then this method sets it to absent.
     * Otherwise, it leaves it unknown.
     * <p>
     * In addition, if any port is listed in _outputsThatMustBeUnknown,
     * then that output is forced to be unkown, regardless of its
     * previous state.
     *
     * @param controller The controller FSM.
     * @return True if after this method is called, any output port is absent.
     * @exception IllegalActionException If something goes wrong.
     */
    private boolean _assertAbsentOutputs(FSMActor controller)
            throws IllegalActionException {
        // First, if any output port must be forced to be unknown,
        // do so here. We have to be careful to not make these same
        // ports known later.
        for (IOPort port : controller._outputsThatMustBeUnknown.keySet()) {
            // Get the channel number.
            int i = controller._outputsThatMustBeUnknown.get(port).intValue();
            _forceUnknownOutput(port, i);
        }

        // First check the refinements.
        TypedActor[] refinements = controller._currentState.getRefinement();
        if (refinements != null) {
            for (Actor refinementActor : refinements) {
                Director refinementDirector = refinementActor.getDirector();

                // The second check below guards against a refinement with no director.
                if (refinementDirector instanceof FSMDirector
                        && refinementDirector != getDirector()) {
                    // The refinement is an FSM, so we perform must/may analysis
                    // to identify outputs that can be determined to be absent.
                    FSMActor refinementController = ((FSMDirector) refinementDirector)
                            .getController();
                    if (!_assertAbsentOutputs(refinementController)) {
                        // The refinement has no absent outputs (they are all either
                        // unknown or present), therefore we cannot assert any outputs
                        // to be absent at this level either.
                        return false;
                    }
                } else {
                    // Refinement is not an FSM. We can't say anything about
                    // outputs.
                    return false;
                }
            }
        }
        // At this point, either there are no refinements, or all refinements
        // are FSMs, those refinements have asserted at least one output
        // to be absent.

        boolean foundAbsentOutputs = false;
        if (getContainer() instanceof ModalModel) {
            // Inside a modal model.
            // We now iterate over all output ports of the container
            // of this director, and for each such output port p,
            // on each channel c,
            // if all the refinements and the controller FSM agree that
            // p on c is absent, then we assert it to be absent.
            Actor container = (Actor) getContainer();
            List<IOPort> outputs = container.outputPortList();
            if (outputs.size() == 0) {
                // There are no outputs, so in effect, all outputs
                // are absent !!
                return true;
            }
            for (IOPort port : outputs) {
                IOPort[] refinementPorts = null;
                if (refinements != null) {
                    refinementPorts = new IOPort[refinements.length];
                    int i = 0;
                    for (TypedActor refinement : refinements) {
                        refinementPorts[i++] = (IOPort) ((Entity) refinement)
                                .getPort(port.getName());
                    }
                }
                for (int channel = 0; channel < port.getWidthInside(); channel++) {
                    // If the channel is known, we don't need to do any
                    // further checks.
                    if (!port.isKnownInside(channel)) {
                        // First check whether all refinements agree that the channel is
                        // absent.
                        boolean channelIsAbsent = true;
                        if (refinementPorts != null) {
                            for (int i = 0; i < refinementPorts.length; i++) {
                                // Note that _transferOutputs(refinementPorts[i]
                                // has not been called, or the inside of port would
                                // be known and we would not be here. Hence, we
                                // have to check the inside of this refinement port.
                                if (refinementPorts[i] != null
                                        && channel < refinementPorts[i]
                                                .getWidthInside()
                                                && (!refinementPorts[i]
                                                        .isKnownInside(channel) || refinementPorts[i]
                                                                .hasTokenInside(channel))) {
                                    // A refinement has either unknown or non-absent
                                    // output. Give up on this channel. It cannot be
                                    // asserted absent.
                                    channelIsAbsent = false;
                                    break;
                                }
                            }
                        }
                        if (!channelIsAbsent) {
                            // A refinement has either unknown or non-absent
                            // output. Give up on this channel. It cannot be
                            // asserted absent.
                            break;
                        }
                        // If we get here, all refinements (if any) agree that
                        // the current channel of the current port is absent. See
                        // whether this controller FSM also agrees.
                        IOPort controllerPort = (IOPort) controller
                                .getPort(port.getName());
                        // NOTE: If controllerPort is null, then presumably we should
                        // be able to set the output port to absent, but how to do that?
                        // We can't do it by sending null from controllerPort, because
                        // there is no controllerPort!
                        if (controllerPort != null) {
                            channelIsAbsent = controller._isSafeToClear(
                                    controllerPort, channel,
                                    controller._currentState, false, null);
                            if (channelIsAbsent) {
                                Integer channelThatMustBeUnknown = controller._outputsThatMustBeUnknown
                                        .get(controllerPort);
                                if (channelThatMustBeUnknown == null
                                        || channelThatMustBeUnknown.intValue() != channel) {
                                    foundAbsentOutputs = true;
                                    controllerPort.send(channel, null);
                                    if (_debugging) {
                                        _debug("Asserting absent output: "
                                                + port.getName()
                                                + ", on channel " + channel);
                                    }
                                }
                            }
                        }
                    } else {
                        if (!port.hasTokenInside(channel)) {
                            foundAbsentOutputs = true;
                        }
                    }
                }
            }
        } else {
            // Not inside a modal model.
            List<IOPort> outputs = outputPortList();
            for (IOPort port : outputs) {
                for (int channel = 0; channel < port.getWidth(); channel++) {
                    if (_isSafeToClear(port, channel, _currentState, false,
                            null)) {
                        if (_debugging) {
                            _debug("Asserting absent output: " + port.getName()
                                    + ", on channel " + channel);
                        }
                        Integer channelThatMustBeUnknown = controller._outputsThatMustBeUnknown
                                .get(port);
                        if (channelThatMustBeUnknown == null
                                || channelThatMustBeUnknown.intValue() != channel) {
                            // Send absent.
                            port.send(channel, null);
                            foundAbsentOutputs = true;
                            if (_debugging) {
                                _debug("Asserting absent output: "
                                        + port.getName() + ", on channel "
                                        + channel);
                            }
                        }
                    }
                }
            }
        }

        // Return true if any output channel is absent.
        return foundAbsentOutputs;
    }

    /*  Build for each state a map from input ports to boolean flags
     *  indicating whether a channel is connected to an output port
     *  of the refinement of the state.
     *  This method is read-synchronized on the workspace.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    private void _buildConnectionMaps() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            if (_connectionMaps == null) {
                _connectionMaps = new HashMap();
            } else {
                // Remove any existing maps.
                _connectionMaps.clear();
            }

            // Create a map for each state.
            Iterator states = entityList().iterator();
            State state = null;

            while (states.hasNext()) {
                state = (State) states.next();

                Map stateMap = new HashMap();
                TypedActor[] actors = state.getRefinement();

                // Determine the boolean flags for each input port.
                Iterator inPorts = inputPortList().iterator();

                while (inPorts.hasNext()) {
                    IOPort inPort = (IOPort) inPorts.next();
                    boolean[] flags = new boolean[inPort.getWidth()];

                    if (actors == null || actors.length == 0) {
                        java.util.Arrays.fill(flags, false);
                        stateMap.put(inPort, flags);
                        continue;
                    }

                    Iterator relations = inPort.linkedRelationList().iterator();
                    int channelIndex = 0;

                    while (relations.hasNext()) {
                        IORelation relation = (IORelation) relations.next();
                        boolean linked = false;

                        for (int i = 0; i < actors.length; ++i) {
                            Iterator outports = actors[i].outputPortList()
                                    .iterator();

                            while (outports.hasNext()) {
                                IOPort outport = (IOPort) outports.next();
                                linked = linked | outport.isLinked(relation);
                            }
                        }

                        for (int j = 0; j < relation.getWidth(); ++j) {
                            flags[channelIndex + j] = linked;
                        }

                        channelIndex += relation.getWidth();
                    }

                    stateMap.put(inPort, flags);
                }

                _connectionMaps.put(state, stateMap);
            }

            _connectionMapsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /** Choose an error transition to handle the specified exception,
     *  if there is one. As a side effect, this will set
     *  _lastChosenTransitions.
     *  @param ex The exception.
     *  @exception IllegalActionException If something goes wrong.
     */
    private void _chooseErrorTransition(Throwable ex)
            throws IllegalActionException {
        if (_currentState != null) {
            List<Transition> errorTransitionList = _currentState
                    .errorTransitionList();
            if (errorTransitionList.size() > 0) {
                if (_debugging) {
                    _debug("** Exception occurred executing refinement. Checking error transitions.");
                }
                // Set variables exposing the exception.
                // Escape any $ in the error message.
                String message = (ex.getMessage().replaceAll("\\$", "\\$\\$"));
                errorMessage.setExpression(message);
                errorClass.setExpression(ex.getClass().getName());
                if (ex instanceof KernelException) {
                    Nameable cause = ((KernelException) ex).getNameable1();
                    if (cause != null) {
                        errorCause.setToken(new ObjectToken(cause, cause
                                .getClass()));
                    }
                }

                // It makes no sense for error transitions to be preemptive,
                // so we look only at non-preemptive error transitions.
                _chooseTransitions(errorTransitionList, false, false, false,
                        false);
            }
        }
    }

    /** Return an enabled transition among the given list of transitions
     *  for which both the guard expression and the output actions can
     *  be evaluated (the inputs referred by these are known).
     *  If there is only one transition enabled, return that transition.
     *  In case there are multiple enabled transitions, if any of
     *  them is not marked nondeterministic, throw an exception.
     *  Otherwise, randomly choose one from the enabled transitions
     *  and return it if the output actions can be evaluated.
     *  Execute the output actions contained by the returned
     *  transition before returning. Also, fire the transition
     *  refinements, if any, and the refinements of any transient
     *  states.
     *  <p>
     *  After calling this method, you can call foundUnknown()
     *  to determine whether any guard expressions or output value
     *  expressions on a transition whose guard evaluates to true
     *  were found in the specified transition list that
     *  referred to input ports that are not currently known.
     *  @param currentState The state from which transitions are examined.
     *  @param transitionList A list of transitions.
     *  @param preemptive True to consider only preemptive transitions,
     *   false to consider only non-preemptive transitions.
     *  @param immediateOnly True to consider only immediate transitions,
     *   false to consider both immediate and non-immediate transitions.
     *  @param inInitialize True when this method is called
     *   from initialize or preinitialize, in which case, firing and initializing
     *   refinements is not allowed; note that the refinements
     *   will be initialized by the initialize method, but this
     *   prevents them from being initialized more than once.
     *   This could be important if, for example, the refinement
     *   produces an output during initialize in a domain where
     *   outputs are consumed, such as SDF.
     *  @param inPreinitialize True if this is being called in
     *   preinitialize. Outputs must not be produced in preinitialize,
     *   so we don't execute output actions if this argument is true.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled and not all of them are nondeterministic.
     */
    private Transition _chooseTransition(State currentState,
            List transitionList, boolean preemptive, boolean immediateOnly,
            boolean inInitialize, boolean inPreinitialize)
                    throws IllegalActionException {

        // Get the transitions enabled from the current state.
        List<Transition> enabledTransitions = enabledTransitions(
                transitionList, preemptive, immediateOnly);
        int numberOfEnabledTransitions = enabledTransitions.size();

        Transition chosenTransition = null;

        if (numberOfEnabledTransitions == 1) {
            chosenTransition = enabledTransitions.get(0);
            // Record the chosen transition.
            _transitionsPreviouslyChosenInIteration.add(chosenTransition);
        } else if (numberOfEnabledTransitions > 1) {
            // Ensure that if there are multiple enabled transitions, all of them
            // are nondeterministic.
            for (Transition enabledTransition : enabledTransitions) {
                if (!enabledTransition.isNondeterministic()) {
                    // Construct an informative error message.
                    StringBuffer enabled = new StringBuffer();
                    for (Transition transition : enabledTransitions) {
                        if (enabled.length() == 0) {
                            enabled.append("{");
                        } else {
                            enabled.append(", ");
                        }
                        enabled.append(transition.getName());
                    }
                    enabled.append("}");
                    throw new MultipleEnabledTransitionsException(
                            currentState,
                            "Nondeterministic FSM error: "
                                    + "Multiple enabled transitions found but not all"
                                    + " of them are nondeterministic. Transition "
                                    + enabledTransition.getName()
                                    + " is deterministic. "
                                    + "The following transitions are all enabled: "
                                    + enabled);
                }
            }
            // If one of these transitions has been previously chosen in
            // this iteration, then choose it again. This ensures the same
            // results for nondeterministic choices.
            if (_transitionsPreviouslyChosenInIteration.size() > 0) {
                for (Transition previouslyChosenTransition : _transitionsPreviouslyChosenInIteration) {
                    if (enabledTransitions.contains(previouslyChosenTransition)) {
                        chosenTransition = previouslyChosenTransition;
                    }
                }
            }
            if (chosenTransition == null) {
                // No previously chosen transition.
                // Randomly choose one transition from the list of the
                // enabled transitions. Note that it is possible that the
                // chosen transition cannot be executed because inputs needed
                // by its output actions are not known.
                // In that case, we have to choose a different transition.
                while (enabledTransitions.size() > 0) {
                    // Since the size of the list of enabled transitions usually (almost
                    // always) is less than the maximum value of integer. We can safely
                    // do the cast from long to int in the following statement.
                    int randomChoice = (int) Math.floor(Math.random()
                            * numberOfEnabledTransitions);

                    // There is a tiny chance that randomChoice equals length.
                    // When this happens, we deduct 1 from the randomChoice.
                    if (randomChoice == numberOfEnabledTransitions) {
                        randomChoice--;
                    }

                    chosenTransition = enabledTransitions.get(randomChoice);
                    if (_referencedInputPortsByOutputKnown(chosenTransition)) {
                        // The chosen transition has an output action that
                        // references an unknown input.
                        _foundUnknown = true;
                        break;
                    } else {
                        // Cannot make this choice.
                        enabledTransitions.remove(chosenTransition);
                        chosenTransition = null;
                    }
                }
                // Record the chosen transition.
                _transitionsPreviouslyChosenInIteration.add(chosenTransition);
            }
        }

        if (chosenTransition != null) {
            if (_debugging) {
                _debug("Choose transition: ", chosenTransition.getFullName());
            }

            // cmot:
            // If non-preemptive and immediate transition, fire destination
            // state refinement.
            if (!chosenTransition.isPreemptive()
                    && chosenTransition.isImmediate()) {
                if (chosenTransition.destinationState() != _currentState) {
                    // reset probability threshold and generate new random
                    // in case there is an immediate transition.
                    _oldThreshold = 0.0;
                    _randomValue = _randomToken.nextDouble();
                }
                // Check for initial state with a refinement and an immediate transition,
                // which is not allowed because we can't fire the refinement in initialize.
                if (inInitialize) {
                    Actor[] stateRefinements = currentState.getRefinement();
                    if (stateRefinements != null && stateRefinements.length > 0) {
                        throw new IllegalActionException(
                                this,
                                "Initial state with a refinement and an enabled "
                                        + "immediate transition is not allowed, "
                                        + "because the refinement would have to execute during the initialize phase.");
                    }
                }
                // If the transition into the current state is a reset transition,
                // then initialize the source state refinements. Note that this is safe to do
                // in the fire() method because a transition cannot be unchosen later.
                //
                // FIXME: The above statement is not true in the Continuous domain!!!
                // The ContinuousDirector may later refine the step size.
                //
                // Note that at this point, _lastChosenTransition is the transition _into_
                // the current state, if there is one.
                if (!inInitialize && _lastChosenTransition != null
                        && !_lastChosenTransition.isHistory()) {
                    _initializeRefinements(currentState);
                }

                // Fire the state refinements.
                _fireStateRefinements(currentState);
            }

            // Execute the choice actions.
            Iterator actions = chosenTransition.choiceActionList().iterator();
            while (actions.hasNext()) {
                Action action = (Action) actions.next();
                if (!inPreinitialize) {
                    // Produce output tokens here.
                    // This should not be done in preinitialize, but is OK in initialize.
                    // Outputs cannot be produced in preinitialize because type resolution has not occurred
                    // and receivers have not been created.
                    action.execute();
                    if (_debugging) {
                        _debug("--- Transition action executed: " + action);
                    }
                } else {
                    // For the benefit of SDF, if there is an output action writing to an
                    // output port, then set the tokenInitProduction parameter of that port.
                    List<NamedObj> destinations = action.getDestinations();
                    for (NamedObj destination : destinations) {
                        if (destination instanceof IOPort) {
                            // This sets the parameter only for this FSMActor. If this actor
                            // is contained by a ModalModel, we need to set it for that port too.
                            DFUtilities.setTokenInitProduction(
                                    (IOPort) destination, 1);
                            if (getContainer() instanceof ModalModel) {
                                IOPort port = (IOPort) ((ModalModel) getContainer())
                                        .getPort(destination.getName());
                                if (port != null) {
                                    DFUtilities.setTokenInitProduction(port, 1);
                                }
                            }
                        }
                    }
                }
            }

            // Execute the refinements of the transition.
            // This is kept for backward compatibility.
            Actor[] transitionRefinements = chosenTransition.getRefinement();
            if (transitionRefinements != null) {
                for (int i = 0; i < transitionRefinements.length; ++i) {
                    if (_stopRequested
                            || _disabledRefinements
                            .contains(transitionRefinements[i])) {
                        break;
                    }
                    if (_debugging) {
                        _debug("Fire transition refinement:",
                                transitionRefinements[i].getName());
                    }
                    // NOTE: What should model time be for transition refinements?
                    // It is not reasonable for it to be the time of the originating
                    // refinement because multiple transitions may share a refinement
                    // and time will end up bouncing around...
                    // Transition refinements are deprecated anyway, so let's not
                    // worry about that.
                    if (transitionRefinements[i].prefire()) {
                        transitionRefinements[i].fire();
                        _transitionRefinementsToPostfire
                        .add(transitionRefinements[i]);
                    }
                }
            }

            // If the current state has no refinement and there are
            // outputs that remain unknown, make them absent.
            // NOTE: Even if there is a refinement, it might be
            // reasonable to assert that outputs are absent.
            // We can't do that here, however, because the outputs
            // from the refinement have not been transferred.
            // This case has to be handled by the fire method.
            if (_areAllImmediateTransitionsDisabled(chosenTransition
                    .destinationState())
                    && currentState.getRefinement() == null) {
                List<IOPort> outputs = outputPortList();
                for (IOPort port : outputs) {
                    for (int channel = 0; channel < port.getWidth(); channel++) {
                        if (!port.isKnown(channel)) {
                            port.send(channel, null);
                            if (_debugging) {
                                _debug("--- Asserting that output "
                                        + port.getName() + " is absent.");
                            }
                        }
                    }
                }
            }

            // Commit to this transition, if it is != null.
            _lastChosenTransitions.put(currentState, chosenTransition);
            // Set this only if there is a non-null chosen transition, since
            // we want to remember the last chosen transition even if there
            // no preemptive immediate transition.
            _lastChosenTransition = chosenTransition;
        }
        return chosenTransition;
    }

    /** Schedule an actor for execution on a ExecutionAspect. If the actor can
     *  execute this method returns true. If resources are not available this
     *  method returns false.
     *  @param actor The actor.
     *  @param timestamp The time the actor requests to be scheduled.
     *  @return True if actor was scheduled and can be fired.
     *  @exception IllegalActionException Thrown if parameters cannot be read, actor cannot be
     *   scheduled or container cannot be fired at future time.
     */
    protected boolean _schedule(NamedObj actor, Time timestamp)
            throws IllegalActionException {
        ActorExecutionAspect aspect = getDirector().getExecutionAspect(actor);
        Time time = null;
        Boolean finished = true;
        if (timestamp == null) {
            timestamp = getDirector().getModelTime();
        }
        if (aspect != null) {
            Time environmentTime = ((CompositeActor) aspect.getContainer())
                    .getDirector().getEnvironmentTime();
            time = ExecutionAspectHelper.schedule(aspect, actor,
                    environmentTime, getDirector()
                            .getDeadline(actor, timestamp));
            //            if (_nextScheduleTime == null) {
            //                _nextScheduleTime = new HashMap<ActorExecutionAspect, Time>();
            //            }
            //            _nextScheduleTime.put(aspect, time);
            //            finished = _actorFinished(actor);
            if (time != null && time.getDoubleValue() > 0.0) {
                CompositeActor container = (CompositeActor) aspect
                        .getContainer();
                Time fireAtTime = environmentTime;
                if (!time.equals(Time.POSITIVE_INFINITY)) {
                    fireAtTime = fireAtTime.add(time);
                    container.getDirector().fireContainerAt(fireAtTime);
                }
            }
        }
        boolean schedule = time == null || finished;
        //        if (!schedule) {
        //            ActorExecutionAspect scheduler = getExecutionAspect(actor);
        //            if (scheduler != null) {
        //                ((CompositeActor) scheduler.getContainer()).getDirector().fireAt(
        //                        (Actor) scheduler,
        //                        getModelTime().add(_nextScheduleTime.get(scheduler)));
        //            } else {
        //                throw new InternalErrorException(this, null,
        //                        "_getExecutionAspect(" + actor.getFullName()
        //                        + ") returned null?");
        //            }
        //        }
        return schedule;
    }

    /** Execute all set actions contained by the transition chosen
     *  from the current state. Change current state
     *  to the destination state of the last of these
     *  chosen transitions. If the new current state is a transient
     *  state that has a chosen transition emanating from it, then
     *  also execute the set actions on that transition.
     *  Reset the refinement
     *  of the destination state if the <i>reset</i> parameter of the
     *  chosen transition is true.
     *  @param inPreinitialize If true, then only set the current
     *   state after following immediate transitions. Note that the
     *   guards on the immediate transitions need to be evaluatable.
     *  @param inInitialize True when this method is called
     *   from initialize or preinitialize, in which case, firing and initializing
     *   refinements is not allowed; note that the refinements
     *   will be initialized by the initialize method, but this
     *   prevents them from being initialized more than once.
     *   This could be important if, for example, the refinement
     *   produces an output during initialize in a domain where
     *   outputs are consumed, such as SDF.
     *  @exception IllegalActionException If any commit action throws it,
     *   or the last chosen transition does not have a destination state.
     */
    private void _commitLastChosenTransition(boolean inPreinitialize,
            boolean inInitialize) throws IllegalActionException {
        Transition currentTransition = _lastChosenTransitions
                .get(_currentState);
        if (currentTransition == null) {
            return;
        }

        // Add this transition to the last taken transitions
        _lastTakenTransitions.add(currentTransition);

        // Remove the entry from the map of chosen transitions to prevent
        // a stack overflow from cycling forever around a directed cycle.
        _lastChosenTransitions.remove(_currentState);

        if (_debugging) {
            _debug("Commit transition ", currentTransition.getFullName()
                    + " at time " + getDirector().getModelTime());
            _debug("  Guard evaluating to true: "
                    + currentTransition.guardExpression.getExpression());
        }
        if (currentTransition.destinationState() == null) {
            throw new IllegalActionException(this, currentTransition,
                    "The transition is enabled but does not have a "
                            + "destination state.");
        }

        // Next execute the commit actions.
        // This needs to occur before resetting the destination
        // refinement because the commit actions may initialize
        // the destination refinement.
        Iterator actions = currentTransition.commitActionList().iterator();
        while (actions.hasNext() && !_stopRequested) {
            Action action = (Action) actions.next();
            action.execute();
        }

        // If the chosen transition is not a history transition, initialize the destination
        // refinement. Note that initializing the director will normally also have
        // the side effect of setting its time and time to match the enclosing
        // director. This is done before invoking the set actions because (1)
        // the initialization may reverse the set actions or, (2)
        // the set actions may trigger attributeChanged() calls that depend on
        // the current time or index.
        // If the currentTransition is the last in the chain of chosen
        // transitions and the transition is a reset transition, then
        // initialize the destination refinement.
        State nextState = currentTransition.destinationState();
        // NOTE: This is too late to initialize refinements
        // of transient states, as those have already executed!!
        // So we only initialize if this is the last transition.
        if (_lastChosenTransitions.get(nextState) == null) {
            // If this is a reset transition, then we also need to initialize
            // the destination refinement.
            // Do not do this if we are in preinitialize(), as the refinement
            // has not been preinitialized yet.
            if (!currentTransition.isHistory() && !inPreinitialize
                    && !inInitialize) {
                _initializeRefinements(nextState);
            }
        }

        // Commit to the new state.
        // Before committing the new state, record whether it changed.
        boolean stateChanged = _currentState != currentTransition
                .destinationState();
        _schedule(nextState, getExecutiveDirector().getModelTime());
        _currentState = nextState;
        if (stateChanged) {
            // reset threshold for probabilistic transitions
            _oldThreshold = 0.0;
        }

        if (_debugging) {
            _debug(new StateEvent(this, _currentState));
        }

        _timeOfNextTimeoutExpiration = null;
        _timeEnteredCurrentState = getDirector().getModelTime();

        // If we have reached a final state, make a record of that fact
        // for the postfire() method.
        if (((BooleanToken) _currentState.isFinalState.getToken())
                .booleanValue()) {
            _reachedFinalState = true;
        }

        _setCurrentConnectionMap();

        // If the causality interface is state-dependent and the state
        // has changed, invalidate the schedule. This is done in a ChangeRequest
        // because the current iteration (processing all events with the same
        // time stamp and microstep) has to be allowed to complete. Otherwise,
        // the analysis for causality loops will be redone before other state
        // machines have been given a chance to switch states.
        boolean stateDependent = ((BooleanToken) stateDependentCausality
                .getToken()).booleanValue();
        if (stateDependent && stateChanged) {
            // The third argument indicates that this is not a structural
            // change, and therefore should not trigger a prompt to save
            // on closing the model.
            ChangeRequest request = new ChangeRequest(this,
                    "Invalidate schedule", false) {
                @Override
                protected void _execute() {
                    // Indicate to the director that the current schedule is invalid.
                    getDirector().invalidateSchedule();
                }
            };
            // This is also required to prevent a prompt to save on close.
            request.setPersistent(false);
            requestChange(request);
        }

        // Finally, request a refiring at the current time. This ensures that
        // if the new state is transient, zero time is spent in it.
        // If the new state has a refinement, it is up to that refinement
        // to examine the superdense time index if appropriate and not produce
        // output if it should not produce output. This has implications on
        // the design of actors like DiscreteClock.
        // Do not do this in preinitialize.
        if (!inPreinitialize) {
            Director director = getDirector();
            director.fireAt(this, director.getModelTime());
        }

        // If we have not reached a final state, and the state
        // changed, then recursively call this
        // same method in case the destination state is transient. If it is,
        // then there will be another chosen transition emerging from the
        // new _currentState, and we have to execute set actions on that
        // transition. If there is no chosen transition from the new
        // _currentState, then the recursive call will return immediately.
        if (!_reachedFinalState && stateChanged) {
            _commitLastChosenTransition(inPreinitialize, inInitialize);
        } else {
            _lastChosenTransitions.clear();
        }
    }

    private void _createRandomGenerator() throws IllegalActionException {

        _seed = ((LongToken) seed.getToken()).longValue();
        if (_seed == 0L) {
            _seed = System.currentTimeMillis() + hashCode();
        } else {
            _seed = _seed + getFullName().hashCode();
        }
        _randomToken = new Random(_seed);
    }

    /** Create receivers for each input port.
     *  This method gets write permission on the workspace.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            Iterator inputPorts = inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inPort = (IOPort) inputPorts.next();
                inPort.createReceivers();
            }
        } finally {
            // Note that this does not increment the workspace version.
            // We have not changed the structure of the model.
            workspace().doneTemporaryWriting();
        }
    }

    /** Force the output of the specified port on the specified channel
     *  to be unknown. This is done because there is a transition that
     *  may set the value of this output, possibly overwriting some
     *  previously set value, but the guard on that transition cannot
     *  be evaluated because some inputs are unknown. This method
     *  propagates the forcing of unknown outward through layers
     *  of ModalModels.
     *  @param port The port.
     *  @param channel The channel.
     *  @exception IllegalActionException If getting remove receivers or
     *   resetting them fails.
     */
    private void _forceUnknownOutput(IOPort port, int channel)
            throws IllegalActionException {
        Receiver[][] receivers = port.getRemoteReceivers();
        if (receivers != null) {
            if (receivers.length > channel && receivers[channel] != null) {
                for (int j = 0; j < receivers[channel].length; j++) {
                    if (receivers[channel][j] instanceof FSMReceiver) {
                        receivers[channel][j].reset();
                        // Propagate outwards until there are no more FSMReceivers.
                        IOPort destinationPort = receivers[channel][j]
                                .getContainer();
                        if (_debugging) {
                            _debug("--- Setting inside of port "
                                    + destinationPort.getFullName()
                                    + " unknown on channel "
                                    + channel
                                    + ", because a guard cannot be evaluated due to unknown inputs.");
                        }
                        if (destinationPort != null) {
                            _forceUnknownOutput(destinationPort, channel);
                        }
                    }
                }
            }
        }
    }

    /** Return the environment time.
     *  If this actor is the controller for a modal model,
     *  then return the model time of the modal model's executive director,
     *  if there is one. Otherwise, return the model of the director
     *  for this actor.
     *  @return The current environment time.
     */
    private Time _getEnvironmentTime() {
        Director director = getDirector();
        if (director instanceof FSMDirector) {
            // In a modal model.
            Actor container = (Actor) director.getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                Time environmentTime = executiveDirector.getModelTime();
                return environmentTime;
            }
        }
        return director.getModelTime();
    }

    /*  Initialize the actor.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _init() {
        // Create a more reasonable default icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:red\"/>\n"
                + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
                + "height=\"36\" style=\"fill:lightgrey\"/>\n"
                + "<ellipse cx=\"0\" cy=\"0\"" + " rx=\"15\" ry=\"10\"/>\n"
                + "<circle cx=\"-15\" cy=\"0\""
                + " r=\"5\" style=\"fill:white\"/>\n"
                + "<circle cx=\"15\" cy=\"0\""
                + " r=\"5\" style=\"fill:white\"/>\n" + "</svg>\n");

        try {
            stateDependentCausality = new Parameter(this,
                    "stateDependentCausality");
            stateDependentCausality.setTypeEquals(BaseType.BOOLEAN);
            stateDependentCausality.setExpression("false");

            errorClass = new StringParameter(this, "errorClass");
            errorClass.setVisibility(Settable.EXPERT);
            errorClass.setPersistent(false);
            errorMessage = new StringParameter(this, "errorMessage");
            errorMessage.setVisibility(Settable.EXPERT);
            errorMessage.setPersistent(false);
            errorCause = new Parameter(this, "errorCause");
            errorCause.setVisibility(Settable.EXPERT);
            errorCause.setPersistent(false);

            initialStateName = new StringAttribute(this, "initialStateName");
            initialStateName.setExpression("");
            initialStateName.setVisibility(Settable.EXPERT);

            finalStateNames = new StringAttribute(this, "finalStateNames");
            finalStateNames.setExpression("");
            finalStateNames.setVisibility(Settable.EXPERT);

            timeout = new Parameter(this, "timeout");
            timeout.setToken(new TimeoutFunctionToken());
            timeout.setVisibility(Settable.EXPERT);
            timeout.setPersistent(false);

            probability = new Parameter(this, "probability");
            probability.setToken(new ProbabilityFunctionToken());
            probability.setVisibility(Settable.EXPERT);
            probability.setPersistent(false);

            seed = new SharedParameter(this, "seed", RandomSource.class, "0L");
            seed.setTypeEquals(BaseType.LONG);

            resetOnEachRun = new SharedParameter(this, "resetSeedOnEachRun",
                    RandomSource.class, "false");
            resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);

        } catch (KernelException ex) {
            // This should never happen.
            throw new InternalErrorException(this, ex, "Constructor error.");
        }

        _identifierToPort = new HashMap<String, IOPort>();
        _transitionEvaluatedTo = new HashMap<Transition, BooleanToken>();

    }

    /** Check to see whether the specified transition is enabled.
     *  This method attempts to evaluate the guard of a transition.
     *  If an exception occurs, then it checks to see whether the
     *  guard expression referenced any unknown inputs, and if so,
     *  it returns false (the transition is not (yet) enabled).
     *  @param transition The transition to check.
     *  @return True if the transition is enabled.
     *  @exception IllegalActionException If the guard expression cannot be parsed.
     */
    private boolean _isTransitionEnabled(Transition transition)
            throws IllegalActionException {
        try {
            _transitionBeingTested = transition;
            return transition.isEnabled();
        } catch (UndefinedConstantOrIdentifierException ex) {
            // If the node refers to a port, then it may be that the
            // port is absent.  Check that it matches a port name.
            String name = ex.nodeName();
            IOPort portForIdentifier = _getPortForIdentifier(name);
            if (portForIdentifier != null) {
                // NOTE: We would like to make sure at this point that the
                // expression is well formed, but the issue simply that
                // port has absent, and hence the port name is undefined.
                // This is pretty tricky. How can we check that the
                // expression could be evaluated if the input were present?

                // If the referenced port is now known, and
                // the transition includes any output actions,
                // then we need to make sure that those outputs are
                // unknown in this firing, even if they have been
                // previously set to known in this firing.
                // See $PTII/ptolemy/domains/modal/test/auto/SeeminglyNondeterminate2.xml

                Integer channelForIdentifier = _getChannelForIdentifier(name);
                if (channelForIdentifier < 0) {
                    channelForIdentifier = 0;
                }
                if (channelForIdentifier != null
                        && !portForIdentifier.isKnown(channelForIdentifier)) {
                    List<IOPort> portsAssigned = transition.outputActions
                            .getDestinations();
                    List<Integer> channelsAssigned = transition.outputActions
                            .getChannelNumberList();
                    int i = 0;
                    if (portsAssigned != null) {
                        for (IOPort port : portsAssigned) {
                            // The channelsAssigned list had better have the same length.
                            Integer channel = channelsAssigned.get(i);
                            if (channel == null) {
                                // Interpret this to mean all channels.
                                for (int j = 0; j < port.getWidth(); j++) {
                                    _outputsThatMustBeUnknown.put(port,
                                            Integer.valueOf(j));
                                }
                            } else {
                                _outputsThatMustBeUnknown.put(port, channel);
                            }
                        }
                    }
                }
                return false;
            }
            throw ex;
        }
    }

    /** Remove all variable definitions associated with the specified
     *  port name and channel number.
     *  @param portName The name of the port
     *  @param channel The channel number
     */
    private void _removePortVariables(String portName, int channel) {
        String portChannelName = portName + "_" + channel;
        _inputTokenMap.remove(portName);
        _inputTokenMap.remove(portChannelName);
        _inputTokenMap.remove(portName + "_isPresent");
        _inputTokenMap.remove(portName + "Array");
        _inputTokenMap.remove(portChannelName + "Array");
    }

    /** Reset receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _resetReceivers() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inPort = (IOPort) inputPorts.next();
            Receiver[][] receivers = inPort.getReceivers();
            for (Receiver[] receiver : receivers) {
                if (receiver != null) {
                    for (int j = 0; j < receiver.length; j++) {
                        if (receiver[j] != null) {
                            receiver[j].reset();
                        }
                    }
                }
            }
        }
    }

    /** Given a transition, return false if the guard includes any
     *  reference to an input port value and that input port is known
     *  to be absent, or any reference to an input port whose status
     *  is unknown. Otherwise, return true.  A reference to
     *  an input port with "_isPresent" appended is not considered.
     *  Note that you have to be very careful using this.
     *  E.g., if the guard expression is (!in_isPresent || in == 1)
     *  then this method would return false when in is absent, even
     *  though the guard can be evaluated and evaluates to true.
     *  Thus, the correct usage is to attempt first to evaluate the
     *  guard, and only if the evaluation fails, then use this method
     *  to determine whether the cause is the absence of input.
     *  @param transition A transition
     *  @return False if the guard includes references to values of input
     *   ports that are absent.
     *  @exception IllegalActionException If the guard expression cannot
     *   be parsed.
     */
    /*
     * NOTE: This method is no longer used, but might prove
     * useful in the future.
    private boolean _referencedInputPortValuesByGuardPresent(
            Transition transition) throws IllegalActionException {


        // If the port identifier does
        // not end with "_isPresent", then return false if port
        // identifier with "_isPresent" appended is false. There is no data on
        // the port "in" then the identifier "in" will be undefined, or worse,
        //  will resolve to the port object itself.

        String string = transition.getGuardExpression();
        if (string.trim().equals("")) {
            return true;
        }
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = parser.generateParseTree(string);
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        ParserScope scope = getPortScope();
        // Get a set of free variable names.
        Set<String> nameSet = variableCollector.collectFreeVariables(parseTree,
                scope);

        for (String name : nameSet) {
            Port port = _getPortForIdentifier(name);
            if (port instanceof IOPort) {
                int channel = _getChannelForIdentifier(name);
                if (channel >= 0) {
                    if (!((IOPort) port).isKnown(channel)) {
                        return false;
                    }
                    if (!name.endsWith("_isPresent")) {
                        Token token = scope.get(port.getName() + "_" + channel
                                + "_isPresent");
                        if (!(token instanceof BooleanToken)
                                || !((BooleanToken) token).booleanValue()) {
                            // isPresent symbol is either undefined or false..
                            return false;
                        }
                    }
                } else {
                    // No specified channel.
                    if (!((IOPort) port).isKnown()) {
                        return false;
                    }
                    if (!name.endsWith("_isPresent")) {
                        Token token = scope.get(port.getName() + "_isPresent");
                        if (!(token instanceof BooleanToken)
                                || !((BooleanToken) token).booleanValue()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
     */

    /** Given a transition, find any input ports
     *  referenced in the guard expressions of the
     *  transition, and if any of those input ports has status
     *  unknown, return false.
     *  Otherwise, return true.
     *  These are the input ports whose status must be known
     *  for this transition to be enabled.
     *  @param transition A transition
     *  @return True if all input ports referenced by the guard on
     *   the transition have known status.
     *  @exception IllegalActionException If the guard expression cannot
     *   be parsed.
     */
    private boolean _referencedInputPortsByGuardKnown(Transition transition)
            throws IllegalActionException {

        String string = transition.getGuardExpression();
        if (string.trim().equals("")) {
            return true;
        }
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = parser.generateParseTree(string);
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        ParserScope scope = getPortScope();
        // Get a set of free variable names.
        Set<String> nameSet = variableCollector.collectFreeVariables(parseTree,
                scope);

        for (String name : nameSet) {
            Port port = _getPortForIdentifier(name);
            if (port instanceof IOPort) {
                int channel = _getChannelForIdentifier(name);
                if (channel >= 0) {
                    if (!((IOPort) port).isKnown(channel)) {
                        return false;
                    }
                } else {
                    // No specified channel.
                    if (!((IOPort) port).isKnown()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Given a transition, find any input ports
     *  referenced in the output actions of the
     *  transitions, and if any of those input ports has status
     *  unknown, return false. Otherwise, return true.
     *  These are the input ports whose status must be known
     *  to choose this transition.
     *  @param transition A transition
     *  @return A set of input ports.
     *  @exception IllegalActionException If the guard expression cannot
     *   be parsed.
     */
    private boolean _referencedInputPortsByOutputKnown(Transition transition)
            throws IllegalActionException {
        String outputActionsExpression = transition.outputActions
                .getExpression();
        PtParser parser = new PtParser();
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        ParserScope scope = getPortScope();
        if (!outputActionsExpression.trim().equals("")) {
            Map map = parser.generateAssignmentMap(outputActionsExpression);
            for (Iterator names = map.entrySet().iterator(); names.hasNext();) {
                Map.Entry entry = (Map.Entry) names.next();
                ASTPtAssignmentNode node = (ASTPtAssignmentNode) entry
                        .getValue();
                ASTPtRootNode parseTree = node.getExpressionTree();
                Set<String> nameSet = variableCollector.collectFreeVariables(
                        parseTree, scope);

                for (String name : nameSet) {
                    Port port = _getPortForIdentifier(name);
                    if (port instanceof IOPort) {
                        int channel = _getChannelForIdentifier(name);
                        if (channel >= 0) {
                            if (!((IOPort) port).isKnown(channel)) {
                                return false;
                            }
                        } else {
                            if (!((IOPort) port).isKnown()) {
                                return false;
                            }
                        }
                        // Port status is known, but the referenced
                        // identifier may be undefined (e.g. "in" when
                        // in is absent).
                        /* NOTE: Bogus. Could be in a part of the
                         * output that will not be evaluated.
                        if (scope.get(name) == null) {
                            return false;
                        }
                         */
                    }
                }
            }
        }
        return true;
    }

    /** For each input port of this actor, associate all identifiers
     *  with that port.
     *  @exception IllegalActionException If getting the width of the port fails.
     */
    private void _setIdentifierToPort() throws IllegalActionException {
        _identifierToPort.clear();

        for (Iterator inputPorts = inputPortList().iterator(); inputPorts
                .hasNext();) {
            IOPort inPort = (IOPort) inputPorts.next();
            String portName = inPort.getName();
            _identifierToPort.put(portName, inPort);
            _identifierToPort.put(portName, inPort);
            _identifierToPort.put(portName + "_isPresent", inPort);
            _identifierToPort.put(portName + "Array", inPort);

            for (int i = 0; i < inPort.getWidth(); i++) {
                _identifierToPort.put(portName + "_" + i, inPort);
                _identifierToPort
                .put(portName + "_" + i + "_isPresent", inPort);
                _identifierToPort.put(portName + "_" + i + "Array", inPort);
            }
        }
    }

    /** Set the port variables for the specified port as follows:
     *  The portName_isPresent variable is set to true if the token
     *  argument is non-null, and false otherwise.
     *  If the token argument is non-null, then the portName variable
     *  is set to have the value of the token.
     *  If the tokenArray variable is non-null, then the portNameArray
     *  variable is set to have its value.  In addition, for each
     *  of these cases, another (up to) three variables are set
     *  with portName replaced by portName_i, where i is the channel
     *  number. If token is null, then the variable portName
     *  is unset, so that an access to it results in the name
     *  resolving to the port itself, which can be very confusing.
     *  @param port The port.
     *  @param channel The channel.
     *  @param token If not null, the data token at the port.
     *  @param tokenArray If not null, an array of tokens at the port.
     *  @exception IllegalActionException If the identifier is
     *   already associated with another port.
     */
    private void _setInputTokenMap(Port port, int channel, Token token,
            Token[] tokenArray) throws IllegalActionException {
        String portName = port.getName();

        String portChannelName = portName + "_" + channel;

        String name = portName + "_isPresent";
        if (token != null) {
            _inputTokenMap.put(name, BooleanToken.TRUE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to true.");
            }
        } else {
            _inputTokenMap.put(name, BooleanToken.FALSE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to false.");
            }
        }

        name = portChannelName + "_isPresent";
        if (token != null) {
            _inputTokenMap.put(name, BooleanToken.TRUE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to true.");
            }
        } else {
            _inputTokenMap.put(name, BooleanToken.FALSE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to false.");
            }
        }

        name = portName;
        if (token != null) {
            _inputTokenMap.put(name, token);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + token);
            }
        } else {
            // Remove the identifier.
            _inputTokenMap.remove(name);
        }

        name = portChannelName;
        if (token != null) {
            _inputTokenMap.put(name, token);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + token);
            }
        } else {
            // Remove the identifier.
            // FIXME: Should we set this to null instead???
            _inputTokenMap.remove(name);
        }

        name = portName + "Array";
        if (tokenArray != null) {
            ArrayToken arrayToken = new ArrayToken(tokenArray);
            _inputTokenMap.put(name, arrayToken);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + arrayToken);
            }
        } else {
            // Remove the identifier.
            // FIXME: Should we set this to null instead???
            _inputTokenMap.remove(name);
        }

        name = portChannelName + "Array";
        if (tokenArray != null) {
            ArrayToken arrayToken = new ArrayToken(tokenArray);
            _inputTokenMap.put(name, arrayToken);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + arrayToken);
            }
        } else {
            // Remove the identifier.
            // FIXME: Should we set this to null instead???
            _inputTokenMap.remove(name);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private transient LinkedList _cachedInputPorts;

    private transient LinkedList _cachedOutputPorts;

    /** The causality interface, if it has been created,
     *  for the case where the causality interface is not state
     *  dependent.
     */
    private CausalityInterface _causalityInterface;

    /** The director for which the causality interface was created. */
    private Director _causalityInterfaceDirector;

    /** The causality interfaces by state, for the case
     *  where the causality interface is state dependent.
     */
    private Map<State, FSMCausalityInterface> _causalityInterfaces;

    /** The workspace version for causality interfaces by state, for the case
     *  where the causality interface is state dependent.
     */
    private Map<State, Long> _causalityInterfacesVersions;

    // Stores for each state a map from input ports to boolean flags
    // indicating whether a channel is connected to an output port
    // of the refinement of the state.
    private Map _connectionMaps = null;

    /** Version of the connection maps. */
    private long _connectionMapsVersion = -1;

    /** The map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state.
     */
    private Map _currentConnectionMap = null;

    /** True before the first fire, then false. Used to communicate
     *  with resource schedulers.
     */
    private boolean _firstFire = false;

    /** A flag indicating that unknown inputs were referenced in guards
     *  and/or output value expressions (when guards evaluate to true)
     *  in the most recently called enabledTransition() or
     *  chooseTransition().
     */
    private boolean _foundUnknown = false;

    /** A map that associates each identifier with the unique port that the
     *  identifier describes.  This map is used to detect port names that result
     *  in ambiguous identifier bindings.
     */
    private HashMap<String, IOPort> _identifierToPort;

    /** Version number for _identifierToPort. */
    private long _identifierToPortVersion = -1;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;

    // Flag indicating that a model error occurred
    // and was handled.
    private Exception _modelErrorHandled = null;

    // A flag indicating whether this is at the beginning
    // of one iteration (firing). Normally it is set to true.
    // It is only set to false in HDF.
    private boolean _newIteration = true;

    /** Memory for the probabilistic threshold to be used in evaluating probabilistic transitions **/
    private double _oldThreshold;

    private transient long _outputPortsVersion = -1;

    /** Set of outputs that must be made unknown at the end
     *  of a firing because they are set on transitions that
     *  are not known to be enabled or disabled.
     */
    private Map<IOPort, Integer> _outputsThatMustBeUnknown = new HashMap<IOPort, Integer>();

    /** Random Object to govern generated random numbers **/
    private Random _randomToken;

    /** Random number to decide on the transition to be taken **/
    private double _randomValue;

    // True if the current state is a final state.
    private boolean _reachedFinalState;

    // Indicator of when the receivers were last updated.
    private long _receiversVersion = -1;

    // Seed value to generate random tokens internally, for probabilistic transitions.
    private long _seed = 0L;

    // A flag indicating whether this actor supports multirate firing.
    private boolean _supportMultirate = false;

    // Time at which the current state was entered.
    private Time _timeEnteredCurrentState;

    // Time of next experation of a timeout.
    private Time _timeOfNextTimeoutExpiration;

    // Argument type for the timeout function.
    private static Type[] _TIMEOUT_FUNCTION_ARGUMENT_TYPE = { BaseType.DOUBLE };

    // Hashtable to save an array of tokens for each port.
    // This is used in HDF when multiple tokens are consumed
    // by the FSMActor in one iteration.
    private Hashtable _tokenListArrays;

    /** Indicator that a transition was taken in the last iteration.
     *  This gets set to true in initialize if immediate transitions are
     *  taken, and to true in postfire if any transition is taken.
     */
    private boolean _transitionTaken;

    /** Set of nondeterministic transitions previously chosen
     *  in an iteration. There may be more than one because of
     *  immediate transitions.
     */
    private Set<Transition> _transitionsPreviouslyChosenInIteration = new HashSet<Transition>();

    /** Transition refinements to postfire(), as determined by the fire() method. */
    private List<Actor> _transitionRefinementsToPostfire = new LinkedList<Actor>();

    /** the current transition  tested for being enabled or not **/
    private Transition _transitionBeingTested;

    /** values of the already-evaluated probabilistic transitions within a single firing.
     * Used to keep probabilistic transitions invariant within the firing **/
    private HashMap<Transition, BooleanToken> _transitionEvaluatedTo;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class implements a scope, which is used to evaluate the
     *  parsed expressions.  This class is currently rather simple,
     *  but in the future should allow the values of input ports to
     *  be referenced without having shadow variables.
     */
    public class PortScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @param name The name of the variable to be looked up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            // Check to see if it is something we refer to.
            Token token = (Token) _inputTokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null, FSMActor.this, name);

            if (result != null) {
                return result.getToken();
            } else {
                // If we still can't find a name, try to resolve it with
                // ModelScope. This will look up the names of all states, as
                // well as the names in refinements and those at higher levels
                // of the model hierarchy.
                // -- tfeng (09/26/2008)
                NamedObj object = ModelScope.getScopedObject(FSMActor.this,
                        name);
                if (object instanceof Variable) {
                    token = ((Variable) object).getToken();
                } else if (object != null) {
                    // If the object is an IOPort contained by this actor,
                    // then do not return it. IOPort names refer to the shadow
                    // variables.
                    // -- eal (10/8/2011)
                    if (object instanceof IOPort) {
                        if (object.getContainer() == FSMActor.this) {
                            return null;
                        }
                    }
                    token = new ObjectToken(object, object.getClass());
                }
                return token;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The name of the variable to be looked up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public Type getType(String name) throws IllegalActionException {
            // Check to see if this is something we refer to.
            Port port = _getPortForIdentifier(name);

            if (port != null && port instanceof Typeable) {
                if (name.endsWith("_isPresent")) {
                    return BaseType.BOOLEAN;

                } else if (name.endsWith("Array")) {

                    // We need to explicit return an ArrayType here
                    // because the port type may not be an ArrayType.
                    String portName = name.substring(0, name.length() - 5);
                    if (port == _getPortForIdentifier(portName)) {
                        Type portType = ((Typeable) port).getType();
                        return new ArrayType(portType);
                    }
                }
                return ((Typeable) port).getType();
            }

            Variable result = getScopedVariable(null, FSMActor.this, name);

            if (result != null) {
                return result.getType();
            } else {
                // If we still can't find a name, try to resolve it with
                // ModelScope. This will look up the names of all states, as
                // well as the names in refinements and those at higher levels
                // of the model hierarchy.
                // -- tfeng (09/26/2008)
                Type type = null;
                NamedObj object = ModelScope.getScopedObject(FSMActor.this,
                        name);
                if (object instanceof Variable) {
                    type = ((Variable) object).getType();
                } else if (object != null) {
                    type = new ObjectType(object, object.getClass());
                }
                return type;
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the variable to be looked up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            // Check to see if this is something we refer to.
            Port port = _getPortForIdentifier(name);

            if (port != null && port instanceof Typeable) {
                return ((Typeable) port).getTypeTerm();
            }

            Variable result = getScopedVariable(null, FSMActor.this, name);

            if (result != null) {
                return result.getTypeTerm();
            } else {
                return null;
            }
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         *  @exception IllegalActionException If getting the width of
         *   some port fails.
         */
        @Override
        public Set identifierSet() throws IllegalActionException {
            Set set = getAllScopedVariableNames(null, FSMActor.this);

            // Make sure the identifier set is up to date.
            if (workspace().getVersion() != _identifierToPortVersion) {
                _setIdentifierToPort();
                _identifierToPortVersion = workspace().getVersion();
            }

            set.addAll(_identifierToPort.keySet());
            // If we still can't find a name, try to resolve it with
            // ModelScope. This will look up the names of all states, as
            // well as the names in refinements and those at higher levels
            // of the model hierarchy.
            // -- tfeng (09/26/2008)
            set.addAll(ModelScope.getAllScopedObjectNames(FSMActor.this));
            return set;
        }
    }

    /** The implementation of probabilistic transitions */
    private class ProbabilityFunction implements Function {
        @Override
        public Token apply(Token[] arguments) throws IllegalActionException {

            if (arguments[0] instanceof DoubleToken
                    || arguments[0] instanceof IntToken) {
                if (arguments[0] instanceof IntToken) {
                    // to allow expresions 0 and 1 (instead of 0.0 and 1.0)
                    _guardProbability = ((IntToken) arguments[0]).intValue();
                } else {
                    _guardProbability = ((DoubleToken) arguments[0])
                            .doubleValue();
                }

                if (_transitionBeingTested == null) {
                    return BooleanToken.FALSE;
                }

                // First, check if the transition has already been evaluated. If so, return the result. If not,
                // change threshold and evaluate.

                if (_transitionEvaluatedTo.get(_transitionBeingTested) != null) {
                    // means we have already evaluated this transition,
                    return _transitionEvaluatedTo.get(_transitionBeingTested);
                } else {
                    // no record has been found; evaluate.
                    if (_guardProbability + _oldThreshold > 1.0
                            || (_guardProbability > 1.0)) {
                        System.err.println(
                                "Probability range exceeds [0.0,1.0]");
                    } else if ((_oldThreshold <= _randomValue)
                            && _randomValue <= (_oldThreshold + _guardProbability)) {
                        _oldThreshold += _guardProbability;
                        _transitionEvaluatedTo.put(_transitionBeingTested,
                                BooleanToken.TRUE);
                        return BooleanToken.TRUE;
                    } else {
                        _oldThreshold += _guardProbability;
                        _transitionEvaluatedTo.put(_transitionBeingTested,
                                BooleanToken.FALSE);
                        return BooleanToken.FALSE;
                    }
                }
            }
            return BooleanToken.FALSE;
        }

        @Override
        public int getNumberOfArguments() {
            // TODO Auto-generated method stub
            return 1;
        }

        @Override
        public boolean isCongruent(Function function) {
            // TODO Auto-generated method stub
            return function instanceof ProbabilityFunction;
        }

        @Override
        public String toString() {
            return "function(p:double):boolean";
        }

        private double _guardProbability;

    }

    /** The implementation of the probability function as a token. */
    private class ProbabilityFunctionToken extends FunctionToken {
        public ProbabilityFunctionToken() {
            super(new ProbabilityFunction(), new FunctionType(
                    _TIMEOUT_FUNCTION_ARGUMENT_TYPE, BaseType.BOOLEAN));
        }
    }

    /** The implementation of the timeout function as a function. */
    private class TimeoutFunction implements Function {
        @Override
        public ptolemy.data.Token apply(ptolemy.data.Token[] arguments)
                throws IllegalActionException {
            if (!(arguments[0] instanceof DoubleToken)) {
                // Try to convert to a double.
                arguments[0] = DoubleToken.convert(arguments[0]);
            }
            Time targetTime = _timeEnteredCurrentState
                    .add(((DoubleToken) arguments[0]).doubleValue());
            Director director = getDirector();
            Time currentTime = director.getModelTime();

            if (targetTime.compareTo(currentTime) <= 0) {
                // Note that current time may have passed the target time if
                // the director was unable to honor the exact timing request.
                // Presumably we are going to take a transition in this
                // firing, so there should be no time for next expiration.

                // If the director supports superdense time, then we need to
                // ensure that we are not at microstep zero.
                if (director instanceof SuperdenseTimeDirector) {
                    int microstep = ((SuperdenseTimeDirector) director)
                            .getIndex();
                    if (microstep == 0) {
                        // Request a refiring at the current time.
                        director.fireAtCurrentTime(FSMActor.this);
                        return BooleanToken.FALSE;
                    }
                }
                _timeOfNextTimeoutExpiration = null;
                return BooleanToken.TRUE;
            }

            // If the time of next expiration doesn't exist or is greater
            // than this target time, the request a firing.
            if (_timeOfNextTimeoutExpiration == null
                    || _timeOfNextTimeoutExpiration.compareTo(targetTime) > 0) {
                director.fireAt(FSMActor.this, targetTime);
                _timeOfNextTimeoutExpiration = targetTime;
            }
            return BooleanToken.FALSE;
        }

        @Override
        public int getNumberOfArguments() {
            return 1;
        }

        @Override
        public boolean isCongruent(Function function) {
            return function instanceof TimeoutFunction;
        }

        @Override
        public String toString() {
            return "function(t:double):boolean";
        }
    }

    /** The implementation of the timeout function as a token. */
    private class TimeoutFunctionToken extends FunctionToken {
        public TimeoutFunctionToken() {
            super(new TimeoutFunction(), new FunctionType(
                    _TIMEOUT_FUNCTION_ARGUMENT_TYPE, BaseType.BOOLEAN));
        }
    }
}
