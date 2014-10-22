/* A parameter to encapsulate a tableau.

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
package ptolemy.domains.ptera.lib;

import ptolemy.actor.Initializable;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ObjectType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TableauParameter

/**
 A parameter to encapsulate a tableau. The tableau can be used by different
 events for user interaction.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see Report
 @see SetTableau
 */
public class TableauParameter extends Parameter implements Initializable {

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
    public TableauParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(new ObjectType(Tableau.class));
        setToken(new ObjectToken(null, Tableau.class));
    }

    /** Not implemented. Do nothing.
     *
     *  @param initializable The initializable.
     */
    @Override
    public void addInitializable(Initializable initializable) {
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
        TableauParameter newObject = (TableauParameter) super.clone(workspace);
        try {
            newObject.setToken(new ObjectToken(null, Tableau.class));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Return an empty string because a tableau cannot be specified with an
     *  expression.
     *
     *  @return An empty string.
     *  @see #setExpression(String)
     */
    @Override
    public String getExpression() {
        return "";
    }

    /** Initialize the tableau with null and close any existing tableau.
     *
     *  @exception IllegalActionException If the existing tableau cannot be
     *   retrieved.
     */
    @Override
    public void initialize() throws IllegalActionException {
        final Tableau tableau = (Tableau) ((ObjectToken) getToken()).getValue();
        if (tableau != null) {
            setToken(new ObjectToken(null, Tableau.class));
            EventUtils.closeTableau(tableau);
        }
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
    }

    /** Not implemented. Do nothing.
     *
     *  @param initializable The initializable.
     */
    @Override
    public void removeInitializable(Initializable initializable) {
    }

    /** Specify the container, and add this variable to the list
     *  of attributes in the container. If this variable already has a
     *  container, remove this variable from the attribute list of the
     *  current container first. Otherwise, remove it from the directory
     *  of the workspace, if it is there. If the specified container is
     *  null, remove this variable from the list of attributes of the
     *  current container. If the specified container already contains
     *  an attribute with the same name, then throw an exception and do
     *  not make any changes. Similarly, if the container is not in the
     *  same workspace as this variable, throw an exception. If this
     *  variable is already contained by the specified container, do
     *  nothing.
     *  <p>
     *  If this method results in a change of container (which it usually
     *  does), then remove this variable from the scope of any
     *  scope dependent of this variable.
     *  <p>
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param container The proposed container of this variable.
     *  @exception IllegalActionException If the container will not accept
     *   a variable as its attribute, or this variable and the container
     *   are not in the same workspace, or the proposed container would
     *   result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this variable.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }

        super.setContainer(container);

        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    /** Do nothing because a tableau cannot be specified with an exception.
     *
     *  @param expression The expression.
     *  @see #getExpression()
     */
    @Override
    public void setExpression(String expression) {
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
    }
}
