/* An attribute that contains a parameter that specifies its value.

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

import java.util.Collection;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/**
 An attribute that contains a parameter that specifies its value. This is
 different from {@link Parameter} because the attribute itself does not contain
 a value.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class ParameterAttribute extends GTAttribute
        implements Settable {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ParameterAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _initParameter();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public ParameterAttribute(Workspace workspace) {
        super(workspace);

        try {
            _initParameter();
        } catch (KernelException e) {
            throw new InternalErrorException(this, e,
                    "Unable to initialize parameters.");
        }
    }

    /** Add a listener to be notified when the value of this settable
     *  object changes. An implementation of this method should ignore
     *  the call if the specified listener is already on the list of
     *  listeners.  In other words, it should not be possible for the
     *  same listener to be notified twice of a value update.
     *  @param listener The listener to add.
     *  @see #removeValueListener(ValueListener)
     */
    public void addValueListener(ValueListener listener) {
        parameter.addValueListener(listener);
    }

    public String getDefaultExpression() {
        return parameter.getDefaultExpression();
    }

    /** Get the expression of the contained parameter that has been set by
     *  setExpression(), or null if there is none.
     *  @return The expression.
     *  @see #setExpression(String)
     */
    public String getExpression() {
        return parameter.getExpression();
    }

    /** Get the value of the contained parameter, which is the evaluated
     *  expression.
     *  @return The value.
     *  @see #getExpression()
     */
    public String getValueAsString() {
        return parameter.getValueAsString();
    }

    /** Get the visibility of the container parameter, as set by
     *  setVisibility(). If setVisibility() has not been called, then the
     *  default visibility, which is {@link Settable#EXPERT}, is returned.
     *  @return The visibility of the container parameter.
     *  @see #setVisibility(Settable.Visibility)
     */
    public Visibility getVisibility() {
        return parameter.getVisibility();
    }

    /** Remove a listener from the list of listeners that are
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     *  @see #addValueListener(ValueListener)
     */
    public void removeValueListener(ValueListener listener) {
        parameter.removeValueListener(listener);
    }

    /** Set the value of the contained parameter by giving some expression.
     *  In some implementations, the listeners and the container will
     *  be notified immediately.  However, some implementations may
     *  defer notification until validate() is called.
     *  @param expression The value of the parameter.
     *  @exception IllegalActionException If the expression is invalid.
     *  @see #getExpression()
     */
    public void setExpression(String expression) throws IllegalActionException {
        parameter.setExpression(expression);
    }

    /** Set the visibility of the contained parameter.  The argument should be
     *  one of the public static instances in Settable.
     *  @param visibility The visibility of this variable.
     *  @see #getVisibility()
     */
    public void setVisibility(Visibility visibility) {
        parameter.setVisibility(visibility);
    }

    /** If the contained parameter is not lazy (the default) then evaluate
     *  the expression contained in this variable, and notify any
     *  value dependents. If those are not lazy, then they too will
     *  be evaluated.  Also, if contained parameter is not lazy, then
     *  notify its container, if there is one, by calling its
     *  attributeChanged() method.
     *  <p>
     *  If the contained parameter is lazy, then mark the contained parameter
     *  and any of its value dependents as needing evaluation and for any
     *  value dependents that are not lazy, evaluate them.
     *  Note that if there are no value dependents,
     *  or if they are all lazy, then this will not
     *  result in evaluation of the contained parameter, and hence will not
     *  ensure that the expression giving its value is valid.  Call getToken()
     *  or getType() to accomplish that.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If the contained parameter or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    public Collection<?> validate() throws IllegalActionException {
        return parameter.validate();
    }

    /** The parameter contained in this attribute.
     */
    public Parameter parameter;

    /** Initialize the parameter used to contain the value of this attribute.
     *
     *  @exception IllegalActionException If value of the parameter cannot be
     *   set.
     *  @exception NameDuplicationException If the parameter cannot be created.
     */
    protected abstract void _initParameter() throws IllegalActionException,
            NameDuplicationException;

}
