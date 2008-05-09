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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
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
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class TransformationRule extends MultiCompositeActor implements
        MatchCallback, ValueListener {

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
        String modeString = mode.getExpression();
        boolean singleRunMode = modeString.equals(Mode.REPLACE_FIRST.toString())
                || modeString.equals(Mode.REPLACE_ANY.toString())
                || modeString.equals(Mode.REPLACE_ALL.toString());
        boolean repeat = ((BooleanToken) repeatUntilFixpoint.getToken())
                .booleanValue();
        if (repeat && !singleRunMode) {
            throw new IllegalActionException("When the mode is set to \""
                    + modeString + "\", "
                    + "repeatUntilFixpoint must be false.");
        }

        super.attributeChanged(attribute);
    }

    public Object clone() throws CloneNotSupportedException {
        TransformationRule actor = (TransformationRule) super.clone();
        actor._lastModel = null;
        actor._lastResults = new LinkedList<MatchResult>();
        actor._random = new Random();
        return actor;
    }

    public void fire() throws IllegalActionException {
        try {
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

            if (modelInput.hasToken(0)) {
                ActorToken token = (ActorToken) modelInput.get(0);
                _lastModel = (CompositeEntity) token.getEntity(new Workspace());
                _lastModel.setDeferringChangeRequests(false);

                Mode mode = _getMode();

                GraphMatcher matcher = new GraphMatcher();
                matcher.setMatchCallback(this);

                _collectAllMatches = mode != Mode.REPLACE_FIRST;
                _lastResults.clear();
                matcher.match(getPattern(), _lastModel);

                if (mode == Mode.REPLACE_FIRST || mode == Mode.REPLACE_ANY
                        || mode == Mode.REPLACE_ALL) {
                    boolean foundMatch = !_lastResults.isEmpty();
                    if (foundMatch) {
                        boolean untilFixpoint = ((BooleanToken)
                                repeatUntilFixpoint.getToken()).booleanValue();
                        long count = LongToken.convert(repeatCount.getToken())
                                .longValue();
                        while (!_lastResults.isEmpty()) {
                            if (count <= 0) {
                                break;
                            }

                            switch (mode) {
                            case REPLACE_FIRST:
                                MatchResult result = _lastResults.peek();
                                GraphTransformer.transform(this, result);
                                break;
                            case REPLACE_ANY:
                                result = _lastResults.get(_random
                                        .nextInt(_lastResults.size()));
                                GraphTransformer.transform(this, result);
                                break;
                            case REPLACE_ALL:
                                GraphTransformer.transform(this, _lastResults);
                                break;
                            }
                            if (!untilFixpoint && --count <= 0) {
                                break;
                            }
                            _lastResults.clear();
                            matcher.match(getPattern(), _lastModel);
                        }
                    }

                    modelOutput.send(0, new ActorToken(_lastModel));
                    modified.send(0, BooleanToken.getInstance(foundMatch));
                    return;
                }
            }

            if (matchInput.getWidth() > 0 && matchInput.hasToken(0)
                    && _lastModel != null) {
                ObjectToken token = (ObjectToken) matchInput.get(0);
                MatchResult match = (MatchResult) token.getValue();
                if (match != null) {
                    CompositeEntity host = (CompositeEntity) match
                            .get(getPattern());
                    if (_lastModel != host && !_lastModel.deepContains(host)) {
                        throw new IllegalActionException(this,
                                "The match result cannot be used with the "
                                        + "current model.");
                    }
                    GraphTransformer.transform(this, match);
                    modelOutput.send(0, new ActorToken(_lastModel));
                }
            }

            if (trigger.getWidth() > 0 && trigger.hasToken(0)
                    && !_lastResults.isEmpty()) {
                trigger.get(0);
                _lastResultsOperation = LastResultsOperation.REMOVE_FIRST;
                MatchResult result = _lastResults.peek();
                matchOutput.send(0, new ObjectToken(result));
            }
        } catch (TransformationException e) {
            throw new IllegalActionException(this, e,
                    "Unable to transform model.");
        }

        remaining.send(0, new IntToken(_lastResults.size()));
    }

    public boolean foundMatch(GraphMatcher matcher) {
        _lastResults.add((MatchResult) matcher.getMatchResult().clone());
        return !_collectAllMatches;
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
        switch (_lastResultsOperation) {
        case CLEAR:
            _lastResults.clear();
            break;
        case NONE:
            break;
        case REMOVE_FIRST:
            _lastResults.poll();
            break;
        }
        return true;
    }

    public boolean prefire() throws IllegalActionException {
        _lastResultsOperation = LastResultsOperation.NONE;
        String modeString = mode.getExpression();
        if (modeString.equals(Mode.REPLACE_FIRST.toString())
                || modeString.equals(Mode.REPLACE_ANY.toString())
                || modeString.equals(Mode.REPLACE_ALL.toString())) {
            return modelInput.hasToken(0);
        } else {
            return modelInput.hasToken(0) || matchInput.getWidth() > 0
                    && matchInput.hasToken(0) && _lastModel != null
                    || trigger.getWidth() > 0 && trigger.hasToken(0)
                    && !_lastResults.isEmpty();
        }
    }

    public List<?> typeConstraintList() {
        // Defeat the type constraints of all the actors inside.
        return _EMPTY_LIST;
    }

    public void valueChanged(Settable settable) {
        if (settable == mode) {
            String modeString = mode.getExpression();
            boolean singleRunMode = modeString.equals(Mode.REPLACE_FIRST
                    .toString())
                    || modeString.equals(Mode.REPLACE_ANY.toString())
                    || modeString.equals(Mode.REPLACE_ALL.toString());
            if (singleRunMode) {
                try {
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
                    if (modified == null) {
                        modified = new TypedIOPort(this, "modified", false,
                                true);
                        modified.setTypeEquals(BaseType.BOOLEAN);
                        modified.setPersistent(false);
                        new StringAttribute(modified, "_cardinal")
                                .setExpression("SOUTH");
                    }
                } catch (KernelException e) {
                    throw new InternalErrorException(this, e,
                            "Cannot remove port.");
                }
            } else if (modeString.equals(Mode.EXPERT.toString())) {
                try {
                    boolean repeat = ((BooleanToken) repeatUntilFixpoint
                            .getToken()).booleanValue();
                    if (repeat) {
                        // Repeat is not supported in EXPERT mode, so do not
                        // modify the ports; otherwise, the links to the
                        // original ports will be lost after cancellation.
                        return;
                    }
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
                try {
                    if (modified != null) {
                        modified.setContainer(null);
                        modified = null;
                    }
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
                } catch (KernelException e) {
                    throw new InternalErrorException(this, e,
                            "Cannot create port.");
                }
            } else {
                throw new InternalErrorException("Cannot set mode to "
                        + modeString + ".");
            }
        }
    }

    public TypedIOPort matchInput;

    public TypedIOPort matchOutput;

    public StringParameter mode;

    public TypedIOPort modelInput;

    public TypedIOPort modelOutput;

    public TypedIOPort modified;

    public TypedIOPort remaining;

    public Parameter repeatCount;

    public Parameter repeatUntilFixpoint;

    public TypedIOPort trigger;

    public static class TransformationDirector extends Director {

        /**
         * @param container
         * @param name
         * @exception IllegalActionException
         * @exception NameDuplicationException
         */
        public TransformationDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            setClassName("ptolemy.actor.gt.TransformationRule$GTDirector");
        }

        public void initialize() throws IllegalActionException {
        }

        public void preinitialize() throws IllegalActionException {
        }

        public void wrapup() throws IllegalActionException {
        }
    }

    public enum Mode {
        EXPERT {
            public String toString() {
                return "full control";
            }
        },
        REPLACE_ALL {
            public String toString() {
                return "replace all";
            }
        },
        REPLACE_ANY {
            public String toString() {
                return "replace any";
            }
        },
        REPLACE_FIRST {
            public String toString() {
                return "replace first";
            }
        }
    }

    protected void _init() throws IllegalActionException,
            NameDuplicationException {
        setClassName("ptolemy.actor.gt.TransformationRule");

        // Create the default refinement.
        new Pattern(this, "Pattern");
        new Replacement(this, "Replacement");

        // Create ports.
        modelInput = new TypedIOPort(this, "modelInput", true, false);
        modelInput.setTypeEquals(ActorToken.TYPE);
        modelOutput = new TypedIOPort(this, "modelOutput", false, true);
        modelOutput.setTypeEquals(ActorToken.TYPE);

        mode = new StringParameter(this, "mode");
        for (int i = Mode.values().length - 1; i >= 0; i--) {
            mode.addChoice(Mode.values()[i].toString());
        }
        mode.addValueListener(this);
        mode.setExpression(Mode.REPLACE_FIRST.toString());

        repeatUntilFixpoint = new Parameter(this, "repeatUntilFixpoint");
        repeatUntilFixpoint.setTypeEquals(BaseType.BOOLEAN);
        repeatUntilFixpoint.setToken(BooleanToken.FALSE);

        repeatCount = new Parameter(this, "repeatCount");
        repeatCount.setTypeAtMost(BaseType.LONG);
        repeatCount.setExpression("1");

        new TransformationDirector(this, "GTDirector");
    }

    private Mode _getMode() throws IllegalActionException {
        String modeString = mode.getExpression();
        if (modeString.equals(Mode.REPLACE_FIRST.toString())) {
            return Mode.REPLACE_FIRST;
        } else if (modeString.equals(Mode.REPLACE_ANY.toString())) {
            return Mode.REPLACE_ANY;
        } else if (modeString.equals(Mode.REPLACE_ALL.toString())) {
            return Mode.REPLACE_ALL;
        } else if (modeString.equals(Mode.EXPERT.toString())) {
            return Mode.EXPERT;
        } else {
            throw new IllegalActionException("Unexpected mode: " + modeString);
        }
    }

    private static final List<?> _EMPTY_LIST = new LinkedList<Object>();

    private boolean _collectAllMatches;

    private CompositeEntity _lastModel;

    private LinkedList<MatchResult> _lastResults =
        new LinkedList<MatchResult>();

    private LastResultsOperation _lastResultsOperation;

    private Random _random = new Random();

    private enum LastResultsOperation {
        CLEAR, NONE, REMOVE_FIRST
    }

}
