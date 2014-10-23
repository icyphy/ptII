/* A transformation rule that contains a pattern and a replacement.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.lib.hoc.MultiCompositeActor;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.ActorToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TransformationRule

/**
 A transformation rule that contains a pattern and a replacement. It has two
 purposes. As an abstraction of a transformation rule, it groups the pattern and
 the replacement and relate objects between them (with {@link
 PatternObjectAttribute} associated to objects in the replacement). As an actor,
 it has a port that accepts model tokens containing models to be transformed,
 and it produces transformed models in new model tokens to another port.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationRule extends MultiCompositeActor implements
GTCompositeActor, ValueListener {

    /** Construct a transformation rule with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TransformationRule(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a transformation rule in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public TransformationRule(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** React to a change in the mode parameter or the repeatUntilFixpoint
     *  parameter, and update the appearance of this actor.
     *
     *  @param attribute The attribute changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mode || attribute == repeatUntilFixpoint) {
            // Check whether the mode is not set to single run but
            // repeatUntilFixpoint is set to true. If so, raise exception.
            TransformationMode.Mode modeValue = (TransformationMode.Mode) mode
                    .getChosenValue();
            boolean singleRunMode = modeValue == TransformationMode.Mode.REPLACE_FIRST
                    || modeValue == TransformationMode.Mode.REPLACE_ANY
                    || modeValue == TransformationMode.Mode.REPLACE_ALL;
            boolean repeat = ((BooleanToken) repeatUntilFixpoint.getToken())
                    .booleanValue();
            if (repeat && !singleRunMode && !mode.isMatchOnly()) {
                throw new IllegalActionException("When the mode is set to \""
                        + modeValue + "\", repeatUntilFixpoint must be "
                        + "false.");
            }
        }

        super.attributeChanged(attribute);
    }

    /** Clone the object into the current workspace by calling the clone()
     *  method that takes a Workspace argument.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        TransformationRule actor = (TransformationRule) super.clone();
        actor._lastModel = null;
        actor._lastResults = new LinkedList<MatchResult>();
        return actor;
    }

    /** Fire this actor. Depending on the transformation mode, the action
     *  performed on the input and output is different.
     *  <ul>
     *    <li>If the mode is match only, then the actor has only an input port
     *        to receive input models and an output port to send out success
     *        flags in Booleans. When fired, a model token is read from the
     *        input port. Pattern matching is performed on it with a working
     *        copy of the encapsulated transformation rule. A true or false
     *        token is produced to the output port depending on the result of
     *        the pattern matching.</li>
     *    <li>If the mode is one if replace first, replace last, replace any and
     *        replace all, the actor has one more output port to send out the
     *        transformed models in model tokens. The transformation action is
     *        performed according to the transformation modes specified in
     *        {@link TransformationMode.Mode}.</li>
     *    <li>If the mode is full control, then the actor has a total of 6
     *        ports. A modelInput port receives input models. A modelOutput port
     *        sends out result models. A matchInput port receives matches to be
     *        used for the transformation. A matchOutput port to produce
     *        matches. A trigger port to trigger pattern matching. A remaining
     *        port to output integer numbers representing the numbers of
     *        remaining matches. In each firing, if the modelInput port has a
     *        token present, the token is read and pattern matching is performed
     *        on the contained model. All the matches are collected in a list.
     *        It matchInput port has a token present, the match is read from
     *        that port and is used to transform the model, with the output
     *        being sent to the modelOutput port. If the trigger port has a
     *        token present, the token is consumed and the next match in the
     *        collected list is sent to the matchOutput port. Finally, in all
     *        cases, the number of remaining matches in the list is sent to the
     *        remaining port.</li>
     *  </ul>
     *
     *  @exception IllegalActionException If error occurs in reading the input,
     *   in sending the output, or in the transformation.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Obtain updated value for any PortParameter before each firing.
        try {
            _workspace.getReadAccess();
            for (Object parameterObject : attributeList()) {
                if (parameterObject instanceof PortParameter) {
                    ((PortParameter) parameterObject).update();
                }
            }
        } finally {
            _workspace.doneReading();
        }

        TransformationMode.Mode modeValue = (TransformationMode.Mode) mode
                .getChosenValue();

        if (modelInput.hasToken(0)) {
            ActorToken token = (ActorToken) modelInput.get(0);
            _lastModel = (CompositeEntity) token.getEntity(new Workspace());
            _lastModel.setDeferringChangeRequests(false);

            _lastResults.clear();

            // Obtain a working copy of this transformation rule, which can
            // be safely modified in the process of pattern matching.
            TransformationRule workingCopy = mode.getWorkingCopy(this);

            // Transfer the most updated values of the PortParameters to the
            // working copy.
            for (Object parameterObject : attributeList()) {
                if (parameterObject instanceof PortParameter) {
                    PortParameter param = (PortParameter) parameterObject;
                    Token paramToken = param.getToken();
                    PortParameter paramCopy = (PortParameter) workingCopy
                            .getAttribute(param.getName());
                    do {
                        // Don't understand why do we need a while loop here.
                        // Maybe some bug in setToken() causes this. If you
                        // change this while to if or remove it altogether, then
                        // ptolemy/actor/gt/demo/MapReduce/MapReduceDDF.xml
                        // usually fails.
                        // -- tfeng (12/06/2008)
                        paramCopy.setToken(paramToken);
                    } while (paramCopy.getToken() == null && paramToken != null
                            || paramCopy.getToken() != null
                            && paramToken == null);
                }
            }

            if (modeValue == null) {
                // Full control.
                _lastResults = mode.findAllMatches(workingCopy, _lastModel);
            } else {
                boolean untilFixpoint = ((BooleanToken) repeatUntilFixpoint
                        .getToken()).booleanValue();
                long count = LongToken.convert(repeatCount.getToken())
                        .longValue();
                boolean matchOnly = mode.isMatchOnly();
                boolean foundMatch = count > 0;
                try {
                    while (foundMatch) {
                        foundMatch = mode.transform(workingCopy, _lastModel);
                        if (matchOnly || !untilFixpoint && --count <= 0) {
                            break;
                        }
                    }
                } catch (Throwable t) {
                    throw new IllegalActionException(this, t, "Error "
                            + "occurred in the transformation in "
                            + getFullName() + ".");
                }
                if (!matchOnly) {
                    modelOutput.send(0, new ActorToken(_lastModel));
                }
                matched.send(0, BooleanToken.getInstance(foundMatch));
            }
        }

        if (modeValue != null) {
            return;
        }

        if (matchInput.isOutsideConnected() && matchInput.hasToken(0)
                && _lastModel != null) {
            ObjectToken token = (ObjectToken) matchInput.get(0);
            MatchResult match = (MatchResult) token.getValue();
            if (match != null) {
                TransformationRule workingCopy = mode.getWorkingCopy(this);
                CompositeEntity host = (CompositeEntity) match.get(workingCopy
                        .getPattern());
                if (_lastModel != host && !_lastModel.deepContains(host)) {
                    throw new IllegalActionException(this,
                            "The match result cannot be used with the "
                                    + "current model.");
                }
                try {
                    GraphTransformer.transform(workingCopy, match);
                } catch (Throwable t) {
                    throw new IllegalActionException(this, t, "Error "
                            + "occurred in the transformation in "
                            + getFullName() + ".");
                }
                modelOutput.send(0, new ActorToken(_lastModel));
            }
        }

        if (trigger.isOutsideConnected() && trigger.hasToken(0)
                && !_lastResults.isEmpty()) {
            trigger.get(0);
            _removeFirst = true;
            MatchResult result = _lastResults.get(0);
            matchOutput.send(0, new ObjectToken(result));
        }

        remaining.send(0, new IntToken(_lastResults.size()));
    }

    /** Get the pattern of this transformation rule.
     *
     *  @return The pattern.
     */
    public Pattern getPattern() {
        return (Pattern) getEntity("Pattern");
    }

    /** Get the replacement of this transformation rule.
     *
     *  @return The replacement.
     */
    public Replacement getReplacement() {
        return (Replacement) getEntity("Replacement");
    }

    /** Initialize this actor.
     *
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastModel = null;
        _lastResults.clear();
    }

    /** If a trigger has been received in fire and the next match has been
     *  produced to the matchOutput port, remove the first match in the list.
     *
     *  @return The result from postfire of the superclass.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_removeFirst) {
            _lastResults.remove(0);
        }
        return super.postfire();
    }

    /** Test whether this actor can be fired, depending on the mode.
     *
     *  @return true if this actor can be fired.
     *  @exception IllegalActionException If thrown by the superclass or if the
     *   mode cannot be retrieved.
     *  @see #fire()
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (super.prefire()) {
            _removeFirst = false;
            TransformationMode.Mode modeValue = (TransformationMode.Mode) mode
                    .getChosenValue();
            if (modeValue == TransformationMode.Mode.REPLACE_FIRST
                    || modeValue == TransformationMode.Mode.REPLACE_ANY
                    || modeValue == TransformationMode.Mode.REPLACE_ALL) {
                return modelInput.hasToken(0);
            } else {
                return modelInput.hasToken(0)
                        || matchInput.isOutsideConnected()
                        && matchInput.hasToken(0) && _lastModel != null
                        || trigger.isOutsideConnected() && trigger.hasToken(0)
                        && !_lastResults.isEmpty();
            }
        } else {
            return false;
        }
    }

    /** Return an empty list.
     *
     *  @return An empty list.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public Set<Inequality> typeConstraints() throws IllegalActionException {
        return _EMPTY_SET;
    }

    /** React to the change of mode and change the ports of this actor.
     *
     *  @param settable The attribute changed.
     */
    @Override
    public void valueChanged(Settable settable) {
        // Create or remove ports depending on the mode.
        if (settable == mode) {
            try {
                boolean showFullControlPorts = false;
                boolean isMatchOnly = mode.isMatchOnly();
                if (!isMatchOnly) {
                    String modeString = mode.getExpression();
                    showFullControlPorts = modeString.equals(_FULL_CONTROL);
                }

                if (modelInput == null) {
                    modelInput = new TypedIOPort(this, "modelInput", true,
                            false);
                    modelInput.setTypeEquals(ActorToken.TYPE);
                    modelInput.setDerivedLevel(1);
                }

                if (showFullControlPorts) {
                    if (matchInput == null) {
                        matchInput = new TypedIOPort(this, "matchInput", true,
                                false);
                        matchInput.setTypeEquals(BaseType.OBJECT);
                        matchInput.setDerivedLevel(1);
                    }
                    if (matchOutput == null) {
                        matchOutput = new TypedIOPort(this, "matchOutput",
                                false, true);
                        matchOutput.setTypeEquals(BaseType.OBJECT);
                        matchOutput.setDerivedLevel(1);
                    }
                    if (modelOutput == null) {
                        modelOutput = new TypedIOPort(this, "modelOutput",
                                false, true);
                        modelOutput.setTypeEquals(ActorToken.TYPE);
                        modelOutput.setDerivedLevel(1);
                    }
                    if (trigger == null) {
                        trigger = new TypedIOPort(this, "trigger", true, false);
                        trigger.setTypeEquals(BaseType.BOOLEAN);
                        trigger.setDerivedLevel(1);
                        new StringAttribute(trigger, "_cardinal")
                        .setExpression("SOUTH");
                    }
                    if (remaining == null) {
                        remaining = new TypedIOPort(this, "remaining", false,
                                true);
                        remaining.setTypeEquals(BaseType.INT);
                        remaining.setDerivedLevel(1);
                        new StringAttribute(remaining, "_cardinal")
                        .setExpression("SOUTH");
                    }
                    if (matched != null) {
                        matched.setContainer(null);
                        matched = null;
                    }
                } else {
                    if (matchInput != null) {
                        matchInput.setContainer(null);
                        matchInput = null;
                    }
                    if (matchOutput != null) {
                        matchOutput.setContainer(null);
                        matchOutput = null;
                    }
                    if (trigger != null) {
                        trigger.setContainer(null);
                        trigger = null;
                    }
                    if (remaining != null) {
                        remaining.setContainer(null);
                        remaining = null;
                    }
                    if (isMatchOnly) {
                        if (modelOutput != null) {
                            modelOutput.setContainer(null);
                            modelOutput = null;
                        }
                    } else {
                        if (modelOutput == null) {
                            modelOutput = new TypedIOPort(this, "modelOutput",
                                    false, true);
                            modelOutput.setTypeEquals(ActorToken.TYPE);
                            modelOutput.setDerivedLevel(1);
                        }
                    }
                    if (matched == null) {
                        matched = new TypedIOPort(this, "matched", false, true);
                        matched.setTypeEquals(BaseType.BOOLEAN);
                        matched.setDerivedLevel(1);
                        new StringAttribute(matched, "_cardinal")
                        .setExpression("SOUTH");
                    }
                }
            } catch (KernelException e) {
                throw new InternalErrorException(this, e,
                        "Cannot add or remove port.");
            }
        }
    }

    /** Wrap up the actor after an execution.
     *
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _lastResults.clear();
    }

    /** The matchInput port.
     */
    public TypedIOPort matchInput;

    /** The matchOutput port.
     */
    public TypedIOPort matchOutput;

    /** The matched port.
     */
    public TypedIOPort matched;

    /** The mode.
     */
    public TransformationMode mode;

    /** The modelInput port.
     */
    public TypedIOPort modelInput;

    /** The modelOutput port.
     */
    public TypedIOPort modelOutput;

    /** The remaining port.
     */
    public TypedIOPort remaining;

    /** The count of repeated transformation in one firing.
     */
    public Parameter repeatCount;

    /** Whether the transformation in one firing should continue until a
     *  fixpoint is reached.
     */
    public Parameter repeatUntilFixpoint;

    /** The trigger port.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    //// TransformationDirector

    /**
     A director to be associated with this actor, which does nothing.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class TransformationDirector extends Director {

        /** Construct a director in the given container with the given name.
         *  The container argument must not be null, or a
         *  NullPointerException will be thrown.
         *  If the name argument is null, then the name is set to the
         *  empty string. Increment the version number of the workspace.
         *  Create the timeResolution parameter.
         *
         *  @param container The container.
         *  @param name The name of this director.
         *  @exception IllegalActionException If the name has a period in it, or
         *   the director is not compatible with the specified container, or if
         *   the time resolution parameter is malformed.
         *  @exception NameDuplicationException If the container already contains
         *   an entity with the specified name.
         */
        public TransformationDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            setClassName("ptolemy.actor.gt.TransformationRule"
                    + "$TransformationDirector");
        }

        /** Initialize clock. This is necessary for future calls to the prefire
         *  method of the director.
         *
         *  @exception IllegalActionException Not thrown in this class.
         */
        @Override
        public void initialize() throws IllegalActionException {
            // FIXME: Director.initialize() does quite a bit,
            // including calling methods on initializables,
            // setting up executionAspects, setting the localClock
            // etc.  Are we sure we don't need that here?
            localClock.resetLocalTime(getModelStartTime());
            localClock.start();
        }

        /** Set stop requested to be false, initialize zero time variable
         *  and initialize the local clock. This cannot be done earlier
         *  because the timeResolution might not be known. The base class
         *  implements this functionality and because this director
         *  does not call the base class we need to replicate the
         *  functionality here.
         *
         *  @exception IllegalActionException Not thrown in this class.
         */
        @Override
        public void preinitialize() throws IllegalActionException {
            // FIXME: Director.preinitialize() does quite a bit,
            // including calling methods on initializables, validating
            // settables, calling preinitialize on any actors.  etc.
            // Are we sure we don't need that here?
            _stopRequested = false;
            _zeroTime = new Time(this, 0.0);
            localClock.initialize();
        }

        /** Do nothing.
         *
         *  @exception IllegalActionException Not thrown in this class.
         */
        @Override
        public void wrapup() throws IllegalActionException {
        }
    }

    /** Initialize the pattern, the replacement and the parameters of this
     *  transformation rule.
     *
     *  @exception IllegalActionException If some objects cannot be contained by
     *   the proposed container.
     *  @exception NameDuplicationException If the name of an object coincides
     *   with a name already in the container.
     */
    protected void _init() throws IllegalActionException,
    NameDuplicationException {
        setClassName("ptolemy.actor.gt.TransformationRule");

        // Create the default refinement.
        new Pattern(this, "Pattern");
        new Replacement(this, "Replacement");

        mode = new TransformationMode(this, "mode");
        mode.addChoice(_FULL_CONTROL);
        mode.addValueListener(this);
        valueChanged(mode);

        repeatUntilFixpoint = new Parameter(this, "repeatUntilFixpoint");
        repeatUntilFixpoint.setTypeEquals(BaseType.BOOLEAN);
        repeatUntilFixpoint.setToken(BooleanToken.FALSE);

        repeatCount = new Parameter(this, "repeatCount");
        repeatCount.setTypeAtMost(BaseType.LONG);
        repeatCount.setExpression("1");

        new TransformationDirector(this, "GTDirector");
    }

    /** An empty list.
     */
    private static final Set<Inequality> _EMPTY_SET = new HashSet<Inequality>();

    /** The full control mode.
     */
    private static final String _FULL_CONTROL = "full control";

    /** The last received model.
     */
    private CompositeEntity _lastModel;

    /** The list of match results collected in the most recent pattern matching.
     */
    private List<MatchResult> _lastResults = new LinkedList<MatchResult>();

    /** Whether the first match result should be removed in postfire.
     */
    private boolean _removeFirst;

}
