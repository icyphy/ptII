/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2008-2009 The Regents of the University of California.
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
*/
package ptolemy.actor.gt;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.ScopeExtender;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class TransformationMode extends ChoiceParameter
implements MatchCallback {

    public TransformationMode(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name, Mode.class);
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TransformationMode newObject = (TransformationMode) super.clone(
                workspace);
        newObject._masterRule = null;
        newObject._random = new Random();
        newObject._workingCopy = null;
        newObject._workingCopyVersion = -1;
        return newObject;
    }

    public List<MatchResult> findAllMatches(TransformationRule workingCopy,
            CompositeEntity model) throws IllegalActionException {
        Pattern pattern = workingCopy.getPattern();

        _matcher.setMatchCallback(this);
        _matchResults.clear();
        _collectAllMatches = true;
        _matcher.match(pattern, model);

        return _matchResults;
    }

    public boolean foundMatch(GraphMatcher matcher) {
        _matchResults.add((MatchResult) matcher.getMatchResult().clone());
        return !_collectAllMatches;
    }

    public TransformationRule getWorkingCopy(TransformationRule masterRule)
    throws IllegalActionException {
        if (_masterRule != masterRule || _workingCopy == null
                || _workingCopyVersion != masterRule.workspace().getVersion()) {
            if (_workingCopy != null) {
                _workspace.remove(_workingCopy);
                _workingCopy = null;
            }
            try {
                _workingCopy = (TransformationRule) masterRule.clone(
                        _workspace);
                new WorkingCopyScopeExtender(_workingCopy, "_scopeExtender",
                        masterRule);
                _masterRule = masterRule;
                _workingCopyVersion = masterRule.workspace().getVersion();
            } catch (Exception e) {
                throw new IllegalActionException(this, e, "Cannot get a " +
                        "working copy this transformation rule.");
            }
        }
        return _workingCopy;
    }

    public boolean isMatchOnly() {
        return getChosenValue() == Mode.MATCH_ONLY;
    }

    public boolean transform(TransformationRule workingCopy,
            CompositeEntity model) throws IllegalActionException {
        return transform(workingCopy, model, null, false);
    }

    public boolean transform(final TransformationRule workingCopy,
            CompositeEntity model, final TransformationListener listener,
            boolean defer) throws IllegalActionException {
        Pattern pattern = workingCopy.getPattern();
        final Mode mode = (Mode) getChosenValue();

        _matcher.setMatchCallback(this);
        _matchResults.clear();
        _collectAllMatches = mode != Mode.REPLACE_FIRST;
        _matcher.match(pattern, model);

        if (_matchResults.isEmpty()) {
            return false;
        } else {
            ChangeRequest request = new ChangeRequest(this, "") {
                protected void _execute() throws Exception {
                    switch (mode) {
                    case REPLACE_FIRST:
                        MatchResult result = _matchResults.getFirst();
                        GraphTransformer.transform(workingCopy, result, listener);
                        break;
                    case REPLACE_LAST:
                        result = _matchResults.getLast();
                        GraphTransformer.transform(workingCopy, result, listener);
                        break;
                    case REPLACE_ANY:
                        result = _matchResults.get(_random.nextInt(
                                _matchResults.size()));
                        GraphTransformer.transform(workingCopy, result, listener);
                        break;
                    case REPLACE_ALL:
                        GraphTransformer.transform(workingCopy,
                                new LinkedList<MatchResult>(_matchResults),
                                listener);
                        break;
                    case MATCH_ONLY:
                        break;
                    }
                }
            };
            if (defer) {
                model.requestChange(request);
            } else {
                request.execute();
            }
            return true;
        }
    }

    public enum Mode {
        REPLACE_FIRST {
            public String toString() {
                return "replace first";
            }
        },
        REPLACE_LAST {
            public String toString() {
                return "replace last";
            }
        },
        REPLACE_ANY {
            public String toString() {
                return "replace any";
            }
        },
        REPLACE_ALL {
            public String toString() {
                return "replace all";
            }
        },
        MATCH_ONLY {
            public String toString() {
                return "match only";
            }
        }
    }

    private boolean _collectAllMatches;

    private TransformationRule _masterRule;

    private LinkedList<MatchResult> _matchResults =
        new LinkedList<MatchResult>();

    private GraphMatcher _matcher = new GraphMatcher();

    private Random _random = new Random();

    private TransformationRule _workingCopy;

    private long _workingCopyVersion = -1;

    private Workspace _workspace = new Workspace();

    private static class WorkingCopyScopeExtender extends Attribute
    implements ScopeExtender {

        public List<?> attributeList() {
            NamedObj container = _masterRule;
            Set<?> names = VariableScope.getAllScopedVariableNames(null,
                    container);
            List<Variable> variables = new LinkedList<Variable>();
            for (Object name : names) {
                variables.add(VariableScope.getScopedVariable(null, container,
                        (String) name));
            }
            return variables;
        }

        public Attribute getAttribute(String name) {
            NamedObj container = _masterRule;
            return VariableScope.getScopedVariable(null, container, name);
        }

        WorkingCopyScopeExtender(NamedObj container, String name,
                TransformationRule masterRule) throws IllegalActionException,
                NameDuplicationException {
            super(container, name);
            _masterRule = masterRule;
            setPersistent(false);
        }

        private TransformationRule _masterRule;
    }
}
