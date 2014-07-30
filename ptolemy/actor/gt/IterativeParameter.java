/* A special parameter in the pattern that can take various values in pattern
   matching.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.gt;

import java.util.Collection;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.gt.IterativeParameterIcon;

/**
 A special parameter in the pattern that can take various values in pattern.
 matching. In a transformation, the pattern matching algorithm may try to use
 all the allowed values one by one by invoking the methods {@link #initial()}
 and {@link #next()}, until either the latter method throws an {@link
 IllegalActionException} or the pattern matching terminates. With this
 parameter, it is possible to define a pattern with variable structures, which
 depend on the values of this parameter.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class IterativeParameter extends Parameter implements MatchCallback,
ValueIterator {

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
    public IterativeParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initial = new Parameter(this, "initial");
        constraint = new Parameter(this, "constraint");
        constraint.setTypeAtMost(BaseType.BOOLEAN);
        next = new Parameter(this, "next");
        mode = new ChoiceParameter(this, "mode", Mode.class);

        new IterativeParameterIcon(this, "_icon");

        setTypeAtLeast(initial);
        setTypeAtLeast(next);
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == initial) {
            setToken(initial.getToken());
            validate();
        } else if (attribute == constraint) {
            _validateConstraint();
        }
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
        IterativeParameter newObject = (IterativeParameter) super
                .clone(workspace);
        newObject.setTypeAtLeast(newObject.initial);
        newObject.setTypeAtLeast(newObject.next);
        return newObject;
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
        _foundMatch = true;
        return true;
    }

    /** Set the value of this parameter to be the initial value, and return that
     *  initial value.
     *
     *  @return The initial value.
     *  @exception IllegalActionException If the initial value cannot be set.
     */
    @Override
    public Token initial() throws IllegalActionException {
        Token initialToken = initial.getToken();
        setToken(initialToken);
        _foundMatch = false;
        return initialToken;
    }

    /** Set the value of this parameter to be the next acceptable value, and
     *  return that value.
     *
     *  @return The next value.
     *  @exception IllegalActionException If the next value cannot be set.
     */
    @Override
    public Token next() throws IllegalActionException {
        Object mode = this.mode.getChosenValue();
        if (mode == Mode.STOP_WHEN_MATCH && _foundMatch) {
            throw new IllegalActionException("Stop because the last match "
                    + "was successful.");
        } else if (mode == Mode.STOP_WHEN_NOT_MATCH && !_foundMatch) {
            throw new IllegalActionException("Stop because the last match "
                    + "was not successful.");
        }

        Token nextToken = next.getToken();
        setToken(nextToken);
        validate();
        _foundMatch = false;
        return nextToken;
    }

    /** If this variable is not lazy (the default) then evaluate
     *  the expression contained in this variable, and notify any
     *  value dependents. If those are not lazy, then they too will
     *  be evaluated.  Also, if the variable is not lazy, then
     *  notify its container, if there is one, by calling its
     *  attributeChanged() method.
     *  <p>
     *  If this variable is lazy, then mark this variable and any
     *  of its value dependents as needing evaluation and for any
     *  value dependents that are not lazy, evaluate them.
     *  Note that if there are no value dependents,
     *  or if they are all lazy, then this will not
     *  result in evaluation of this variable, and hence will not ensure
     *  that the expression giving its value is valid.  Call getToken()
     *  or getType() to accomplish that.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    @Override
    public Collection<?> validate() throws IllegalActionException {
        Collection<?> result = super.validate();
        _validateConstraint();
        return result;
    }

    /** The constraint that all values must satisfy.
     */
    public Parameter constraint;

    /** The initial value.
     */
    public Parameter initial;

    /** The mode of this parameter.
     *  @see Mode
     */
    public ChoiceParameter mode;

    /** The next value (computed based on the current value).
     */
    public Parameter next;

    ///////////////////////////////////////////////////////////////////
    //// ConstraintViolationException

    /**
     The exception to denote that the constraint is violated and no more values
     are available.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    @SuppressWarnings("serial")
    public class ConstraintViolationException extends IllegalActionException {

        /** Construct an exception.
         */
        ConstraintViolationException() {
            super("Constraint " + constraint.getExpression()
                    + " is not satisfied.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// Mode

    /**
     The modes of this parameter.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public enum Mode {
        /** Try all values (until constraint is violated).
         */
        ALL_VALUES {
            @Override
            public String toString() {
                return "try all values";
            }
        },
        /** Once a match is found, stop returning more values.
         */
        STOP_WHEN_MATCH {
            @Override
            public String toString() {
                return "stop when match";
            }
        },
        /** Once a match is not found, stop returning more values.
         */
        STOP_WHEN_NOT_MATCH {
            @Override
            public String toString() {
                return "stop when not match";
            }
        },
    }

    /** Validate the constraint.
     *
     *  @exception IllegalActionException If the constraint is violated, or if
     *   the current value of the parameter cannot be retrieved.
     */
    protected void _validateConstraint() throws IllegalActionException {
        String constraintExpression = constraint.getExpression();
        if (constraintExpression != null && !constraintExpression.equals("")) {
            if (!((BooleanToken) constraint.getToken()).booleanValue()) {
                throw new ConstraintViolationException();
            }
        }
    }

    /** Whether a match has been found.
     */
    private boolean _foundMatch = false;
}
