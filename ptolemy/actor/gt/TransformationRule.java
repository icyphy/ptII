/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// TransformationRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class TransformationRule extends MultiCompositeActor implements
        GTCompositeActor, ValueListener {

    public TransformationRule(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public TransformationRule(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mode || attribute == repeatUntilFixpoint) {
            // Check whether the mode is not set to single run but
            // repeatUntilFixpoint is set to true. If so, raise exception.
            TransformationMode.Mode modeValue =
                (TransformationMode.Mode) mode.getChosenValue();
            boolean singleRunMode =
                modeValue == TransformationMode.Mode.REPLACE_FIRST
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

    public Object clone() throws CloneNotSupportedException {
        TransformationRule actor = (TransformationRule) super.clone();
        actor._lastModel = null;
        actor._lastResults = new LinkedList<MatchResult>();
        return actor;
    }

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

        TransformationMode.Mode modeValue =
            (TransformationMode.Mode) mode.getChosenValue();

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
                            || paramCopy.getToken() != null &&
                            paramToken == null);
                }
            }

            if (modeValue == null) {
                // Full control.
                _lastResults = mode.findAllMatches(workingCopy, _lastModel);
            } else {
                boolean untilFixpoint = ((BooleanToken)
                        repeatUntilFixpoint.getToken()).booleanValue();
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
                    throw new IllegalActionException(this, t, "Error " +
                            "occurred in the transformation in " + getFullName()
                            + ".");
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
                CompositeEntity host = (CompositeEntity) match
                        .get(workingCopy.getPattern());
                if (_lastModel != host && !_lastModel.deepContains(host)) {
                    throw new IllegalActionException(this,
                            "The match result cannot be used with the "
                                    + "current model.");
                }
                try {
                    GraphTransformer.transform(workingCopy, match);
                } catch (Throwable t) {
                    throw new IllegalActionException(this, t, "Error " +
                            "occurred in the transformation in " + getFullName()
                            + ".");
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

    public Pattern getPattern() {
        return (Pattern) getEntity("Pattern");
    }

    public Replacement getReplacement() {
        return (Replacement) getEntity("Replacement");
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastModel = null;
        _lastResults.clear();
    }

    public boolean postfire() throws IllegalActionException {
        if (_removeFirst) {
            _lastResults.remove(0);
        }
        return super.postfire();
    }

    public boolean prefire() throws IllegalActionException {
        if (super.prefire()) {
            _removeFirst = false;
            TransformationMode.Mode modeValue =
                (TransformationMode.Mode) mode.getChosenValue();
            if (modeValue == TransformationMode.Mode.REPLACE_FIRST
                    || modeValue == TransformationMode.Mode.REPLACE_ANY
                    || modeValue == TransformationMode.Mode.REPLACE_ALL) {
                return modelInput.hasToken(0);
            } else {
                return modelInput.hasToken(0) || matchInput.isOutsideConnected()
                                && matchInput.hasToken(0) && _lastModel != null
                        || trigger.isOutsideConnected() && trigger.hasToken(0)
                                && !_lastResults.isEmpty();
            }
        } else {
            return false;
        }
    }

    public Set<Inequality> typeConstraints() throws IllegalActionException {
        return _EMPTY_SET;
    }

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
                    modelInput.setPersistent(false);
                }

                if (showFullControlPorts) {
                    if (matchInput == null) {
                        matchInput = new TypedIOPort(this, "matchInput", true,
                                false);
                        matchInput.setTypeEquals(BaseType.OBJECT);
                        matchInput.setPersistent(false);
                    }
                    if (matchOutput == null) {
                        matchOutput = new TypedIOPort(this, "matchOutput",
                                false, true);
                        matchOutput.setTypeEquals(BaseType.OBJECT);
                        matchOutput.setPersistent(false);
                    }
                    if (modelOutput == null) {
                        modelOutput = new TypedIOPort(this, "modelOutput",
                                false, true);
                        modelOutput.setTypeEquals(ActorToken.TYPE);
                        modelOutput.setPersistent(false);
                    }
                    if (trigger == null) {
                        trigger = new TypedIOPort(this, "trigger", true, false);
                        trigger.setTypeEquals(BaseType.BOOLEAN);
                        trigger.setPersistent(false);
                        new StringAttribute(trigger, "_cardinal")
                                .setExpression("SOUTH");
                    }
                    if (remaining == null) {
                        remaining = new TypedIOPort(this, "remaining", false,
                                true);
                        remaining.setTypeEquals(BaseType.INT);
                        remaining.setPersistent(false);
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
                            modelOutput.setPersistent(false);
                        }
                    }
                    if (matched == null) {
                        matched = new TypedIOPort(this, "matched", false,
                                true);
                        matched.setTypeEquals(BaseType.BOOLEAN);
                        matched.setPersistent(false);
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

    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _lastResults.clear();
    }

    public TypedIOPort matchInput;

    public TypedIOPort matchOutput;

    public TypedIOPort matched;

    public TransformationMode mode;

    public TypedIOPort modelInput;

    public TypedIOPort modelOutput;

    public TypedIOPort remaining;

    public Parameter repeatCount;

    public Parameter repeatUntilFixpoint;

    public TypedIOPort trigger;

    public static class TransformationDirector extends Director {

        public TransformationDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            setClassName("ptolemy.actor.gt.TransformationRule" +
                    "$TransformationDirector");
        }

        public void initialize() throws IllegalActionException {
        }

        public void preinitialize() throws IllegalActionException {
            _stopRequested = false;
        }

        public void wrapup() throws IllegalActionException {
        }
    }

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

    private static final Set<Inequality> _EMPTY_SET = new HashSet<Inequality>();

    private static final String _FULL_CONTROL = "full control";

    private CompositeEntity _lastModel;

    private List<MatchResult> _lastResults = new LinkedList<MatchResult>();

    private boolean _removeFirst;

}
