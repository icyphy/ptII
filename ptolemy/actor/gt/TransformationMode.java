/* A parameter that determines the transformation mode.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2014 The Regents of the University of California.
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
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.ScopeExtender;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 A parameter that determines the transformation mode. It also provides functions
 to perform transformation according to the chosen mode.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationMode extends ChoiceParameter implements
MatchCallback {

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public TransformationMode(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, Mode.class);
    }

    /** Clone the variable.  This creates a new variable containing the
     *  same token (if the value was set with setToken()) or the same
     *  (unevaluated) expression, if the expression was set with
     *  setExpression().  The list of variables added to the scope
     *  is not cloned; i.e., the clone has an empty scope.
     *  The clone has the same static type constraints (those given by
     *  setTypeEquals() and setTypeAtMost()), but none of the dynamic
     *  type constraints (those relative to other variables).
     *  @param workspace The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TransformationMode newObject = (TransformationMode) super
                .clone(workspace);
        newObject._masterRule = null;
        newObject._matchResults = new LinkedList<MatchResult>();
        newObject._matcher = new GraphMatcher();
        newObject._random = new Random();
        newObject._workingCopy = null;
        newObject._workingCopyVersion = -1;
        newObject._workspace = new Workspace();
        return newObject;
    }

    /** Find all the matches to the pattern in the workingCopy and return those
     *  matches in a list. When invoked a second time, the previously returned
     *  list would be invalidated. It is the caller's responsibility to
     *  duplicate the list if it needs to be preserved after the next
     *  invocation.
     *
     *  @param workingCopy The transformation rule that can be modified.
     *  @param model The model for pattern matching.
     *  @return The list of matches.
     */
    public List<MatchResult> findAllMatches(TransformationRule workingCopy,
            CompositeEntity model) {
        Pattern pattern = workingCopy.getPattern();

        _matcher.setMatchCallback(this);
        _matchResults.clear();
        _collectAllMatches = true;
        _matcher.match(pattern, model);

        return _matchResults;
    }

    /** A routine to be called when a match is found in the graph matching. The
     *  graph matcher is passed in as the parameter, and the current match can
     *  be obtained by calling {@link GraphMatcher#getMatchResult()}.
     *  This match result should not be kept, however, because it may be changed
     *  by future matching operations. To keep a copy of this result, invoke
     *  {@link MatchResult#clone()} and keep the cloned copy. The return value
     *  indicates whether the match is the one looked for. If it is
     *  <tt>true</tt>, the matching will terminate after this routine returns,
     *  and no more match result will be reported.
     *
     *  @param matcher The graph matcher.
     *  @return Whether the matching should terminate right away.
     */
    @Override
    public boolean foundMatch(GraphMatcher matcher) {
        _matchResults.add((MatchResult) matcher.getMatchResult().clone());
        return !_collectAllMatches;
    }

    /** Get a working copy for the transformation rule, which can be modified in
     *  pattern matching and transformation without affecting the original rule.
     *
     *  @param masterRule The transformation rule.
     *  @return The working copy.
     *  @exception IllegalActionException If a working copy cannot be obtained.
     */
    public TransformationRule getWorkingCopy(TransformationRule masterRule)
            throws IllegalActionException {
        if (_masterRule != masterRule || _workingCopy == null
                || _workingCopyVersion != masterRule.workspace().getVersion()) {
            if (_workingCopy != null) {
                _workspace.remove(_workingCopy);
                _workingCopy = null;
            }
            try {
                _workingCopy = (TransformationRule) masterRule
                        .clone(_workspace);
                new WorkingCopyScopeExtender(_workingCopy, "_scopeExtender",
                        masterRule);
                _masterRule = masterRule;
                _workingCopyVersion = masterRule.workspace().getVersion();
            } catch (Exception e) {
                throw new IllegalActionException(this, e, "Cannot get a "
                        + "working copy this transformation rule.");
            }
        }
        return _workingCopy;
    }

    /** Whether the transformation mode is match only.
     *
     *  @return true if the transformation mode is match only.
     */
    public boolean isMatchOnly() {
        return getChosenValue() == Mode.MATCH_ONLY;
    }

    /** Transform the model with the working copy. The change is done
     *  immediately without defer.
     *
     *  @param workingCopy The working copy.
     *  @param model The model to be transformed.
     *  @return true if the transformation was successful (i.e., any match to
     *   the pattern is found).
     *  @exception IllegalActionException If thrown in pattern matching or
     *   transformation.
     *  @see #transform(TransformationRule, CompositeEntity,
     *   TransformationListener, boolean)
     */
    public boolean transform(TransformationRule workingCopy,
            CompositeEntity model) throws IllegalActionException {
        return transform(workingCopy, model, null, false);
    }

    /** Transform the model with the working copy.
     *
     *  @param workingCopy The working copy.
     *  @param model The model to be transformed.
     *  @param listener The listener to be invoked during transformation.
     *  @param defer Whether the change should be deferred if necessary with a
     *   change request.
     *  @return true if the transformation was successful (i.e., any match to
     *   the pattern is found).
     *  @exception IllegalActionException If thrown in pattern matching or
     *   transformation.
     */
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
                @Override
                protected void _execute() throws Exception {
                    switch (mode) {
                    case REPLACE_FIRST:
                        MatchResult result = _matchResults.getFirst();
                        GraphTransformer.transform(workingCopy, result,
                                listener);
                        break;
                    case REPLACE_LAST:
                        result = _matchResults.getLast();
                        GraphTransformer.transform(workingCopy, result,
                                listener);
                        break;
                    case REPLACE_ANY:
                        result = _matchResults.get(_random
                                .nextInt(_matchResults.size()));
                        GraphTransformer.transform(workingCopy, result,
                                listener);
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

    ///////////////////////////////////////////////////////////////////
    //// Mode

    /**
     The enumeration of accepted modes.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public enum Mode {
        /** Replace only the first occurrence of the pattern.
         */
        REPLACE_FIRST {
            @Override
            public String toString() {
                return "replace first";
            }
        },
        /** Replace only the last occurrence of the pattern.
         */
        REPLACE_LAST {
            @Override
            public String toString() {
                return "replace last";
            }
        },
        /** Replace a randomly selected occurrence of the pattern.
         */
        REPLACE_ANY {
            @Override
            public String toString() {
                return "replace any";
            }
        },
        /** Replace all the occurrences of the pattern, if possible.
         */
        REPLACE_ALL {
            @Override
            public String toString() {
                return "replace all";
            }
        },
        /** Perform pattern matching only without transformation.
         */
        MATCH_ONLY {
            @Override
            public String toString() {
                return "match only";
            }
        }
    }

    /** Whether all matches should be collected in a list.
     */
    private boolean _collectAllMatches;

    /** The master transformation rule (not the working copy).
     */
    private TransformationRule _masterRule;

    /** The collected matches.
     */
    private LinkedList<MatchResult> _matchResults = new LinkedList<MatchResult>();

    /** The graph matcher.
     */
    private GraphMatcher _matcher = new GraphMatcher();

    /** The random number generator.
     */
    private Random _random = new Random();

    /** The current working copy.
     */
    private TransformationRule _workingCopy;

    /** Version of the current working copy.
     */
    private long _workingCopyVersion = -1;

    /** A new workspace for working copies.
     */
    private Workspace _workspace = new Workspace();

    ///////////////////////////////////////////////////////////////////
    //// WorkingCopyScopeExtender

    /**
     A scope extender to resolve names in the scope of the master transformation
     rule even though the expression is specified in a working copy.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class WorkingCopyScopeExtender extends Attribute implements
    ScopeExtender {

        /** Return a list of the attributes contained by this object.
         *  If there are no attributes, return an empty list.
         *  This method is read-synchronized on the workspace.
         *  @return An unmodifiable list of instances of Attribute.
         */
        @Override
        public List<?> attributeList() {
            NamedObj container = _masterRule;
            Set<?> names = ModelScope
                    .getAllScopedVariableNames(null, container);
            List<Variable> variables = new LinkedList<Variable>();
            for (Object name : names) {
                variables.add(ModelScope.getScopedVariable(null, container,
                        (String) name));
            }
            return variables;
        }

        /** Expand the scope of the container by creating any required attributes.
         *  This method does nothing.
         *  @exception IllegalActionException If any required attribute cannot be
         *   created.
         */
        @Override
        public void expand() throws IllegalActionException {
        }

        /** Get the attribute with the given name. The name may be compound,
         *  with fields separated by periods, in which case the attribute
         *  returned is contained by a (deeply) contained attribute.
         *  @param name The name of the desired attribute.
         *  @return The requested attribute if it is found, null otherwise.
         */
        @Override
        public Attribute getAttribute(String name) {
            NamedObj container = _masterRule;
            return ModelScope.getScopedVariable(null, container, name);
        }

        /** Validate contained settables.
         *  @exception IllegalActionException If any required attribute cannot be
         *   created.
         */
        @Override
        public void validate() throws IllegalActionException {
            List<Settable> settables = attributeList(Settable.class);
            for (Settable settable : settables) {
                settable.validate();
            }
        }

        /** Construct a scope extender.
         *
         *  @param container The container.
         *  @param name The name.
         *  @param masterRule The master transformation rule.
         *  @exception IllegalActionException If the attribute is not of an
         *   acceptable class for the container, or if the name contains a
         *   period.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        WorkingCopyScopeExtender(NamedObj container, String name,
                TransformationRule masterRule) throws IllegalActionException,
                NameDuplicationException {
            super(container, name);
            _masterRule = masterRule;
            setPersistent(false);
        }

        /** The master transformation rule.
         */
        private TransformationRule _masterRule;
    }
}
