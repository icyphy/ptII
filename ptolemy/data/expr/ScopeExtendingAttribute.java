/* An attribute that extends its container's scope.

 Copyright (c) 2001-2014 The Regents of the University of California.
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

 @ProposedRating Red (liuxj)
 @AcceptedRating Red (liuxj)

 */
package ptolemy.data.expr;

import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.ScopeExtender;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ScopeExtendingAttribute

/**
 An attribute that extends its container's scope. Any
 parameter contained by such an attribute has the same
 visibility as parameters of the container of the attribute.
 They are shadowed, however, by parameters of the container.
 That is, if the container has a parameter with the same name
 as one in the parameter set, the one in the container provides
 the value to any observer.

 @author Xiaojun Liu
 @version $Id$
 @see ptolemy.data.expr.Variable
 */
public class ScopeExtendingAttribute extends Attribute implements ScopeExtender {
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
    public ScopeExtendingAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Expand the scope of the container by creating any required attributes.
     *  This method does nothing, assuming that the derived classes will
     *  create the attributes in their constructor.
     *  @exception IllegalActionException If any required attribute cannot be
     *   created.
     */
    @Override
    public void expand() throws IllegalActionException {
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  Notify parameters that
     *  depends on any parameter of this attribute about the change in
     *  scope.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        Nameable oldContainer = getContainer();
        super.setContainer(container);

        if (oldContainer != container) {
            // Every variable in the new scope that may be shadowed by
            // a variable inside this attribute must be invalidated.
            // This does not include variables inside the container itself,
            // which take precedence.
            if (container != null) {
                _invalidateShadowedSettables(container.getContainer());
            }

            // Every variable inside this attribute, and anything that
            // had been depending on them, must still be valid.
            validate();
        }
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _invalidateShadowedSettables(NamedObj object)
            throws IllegalActionException {
        if (object == null) {
            // Nothing to do.
            return;
        }

        for (Object element : object.attributeList(Variable.class)) {
            Variable variable = (Variable) element;

            if (getAttribute(variable.getName()) != null) {
                variable.invalidate();
            }
        }

        // Also invalidate the variables inside any
        // scopeExtendingAttributes.
        Iterator scopeAttributes = object.attributeList(
                ScopeExtendingAttribute.class).iterator();

        while (scopeAttributes.hasNext()) {
            ScopeExtendingAttribute attribute = (ScopeExtendingAttribute) scopeAttributes
                    .next();
            Iterator variables = attribute.attributeList(Variable.class)
                    .iterator();

            while (variables.hasNext()) {
                Variable variable = (Variable) variables.next();

                if (getAttribute(variable.getName()) != null) {
                    variable.invalidate();
                }
            }
        }

        NamedObj container = object.getContainer();

        if (container != null) {
            _invalidateShadowedSettables(container);
        }
    }
}
