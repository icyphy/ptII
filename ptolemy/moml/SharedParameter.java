/* A parameter that is shared globally in a model.

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.moml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// SharedParameter

/**
   This parameter is shared throughout a model. Changing the expression of
   any one instance of the parameter will result in all instances that
   are shared being changed to the same expression.  An instance elsewhere
   in the model (within the same top level) is shared if it has the
   same name and its container is of the class specified in the
   constructor (or of the container class, if no class is specified
   in the constructor).
   <p>
   One exception is that if this parameter is (deeply) within an
   instance of EntityLibrary, then the parameter is not shared.
   Were this not the case, then opening a library containing this
   parameter would force expansion of all the sublibraries of
   EntityLibrary, which would defeat the lazy instantiation
   of EntityLibrary.
   <p>
   When this parameter is constructed, the specified container
   will be used to infer the parameter value from the container.
   That is, if the container is within a model that has any
   parameters shared with this one, then the value will be
   set to the last of those encountered.
   If the container is subsequently changed, it is up to the
   user of this actor to use the inferValueFromContext()
   method to reset the value to match the new context.
   Note that this really needs to be done if the container
   of the container, or its container, or any container
   above this actor is changed.  It is recommended to use
   the four-argument constructor, so you can specify a default
   value to use if there are no shared parameters.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class SharedParameter extends Parameter {
    /** Construct a parameter with the given container and name.
     *  The container class will be used to determine which other
     *  instances of SharedParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public SharedParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null, "");
    }

    /** Construct a parameter with the given container, name, and
     *  container class. The specified class will be used to determine
     *  which other instances of SharedParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public SharedParameter(NamedObj container, String name, Class containerClass)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, containerClass, "");
    }

    /** Construct a parameter with the given container, name, and
     *  container class. The specified class will be used to determine
     *  which other instances of SharedParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @param defaultValue The default value to use if the container's
     *   model has no shared parameters.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container, or an empty string to specify no
     *   default value.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public SharedParameter(NamedObj container, String name,
            Class containerClass, String defaultValue)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        if (containerClass == null) {
            containerClass = container.getClass();
        }

        _containerClass = containerClass;
        inferValueFromContext(defaultValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the top level of the containment hierarchy, unless
     *  one of the containers is an instance of EntityLibrary,
     *  in which case, return null.
     *  @return The top level, or this if this has no container.
     */
    public NamedObj getRoot() {
        NamedObj result = this;

        while (result.getContainer() != null) {
            result = (NamedObj) result.getContainer();

            if (result instanceof EntityLibrary) {
                return null;
            }
        }

        return result;
    }

    /** Infer the value of this parameter from the container
     *  context. That is, search for parameters that are
     *  shared with this one, and set the value of this parameter
     *  to match the last one encountered (hopefully, they all
     *  have the same value, but this is not checked here).
     *  If there are no shared parameters, then assign the
     *  default value given as an argument.
     *  @param defaultValue The default parameter value to give.
     */
    public void inferValueFromContext(String defaultValue) {
        NamedObj root = getRoot();

        if (root != null) {
            Iterator sharedParameters = sharedParameterList(root).iterator();

            while (sharedParameters.hasNext()) {
                SharedParameter candidate = (SharedParameter) sharedParameters
                    .next();

                if (candidate != this) {
                    defaultValue = candidate.getExpression();
                }
            }
        }

        boolean previousSuppressing = _suppressingPropagation;
        _suppressingPropagation = true;
        setExpression(defaultValue);
        _suppressingPropagation = previousSuppressing;
    }

    /** Return true if this instance is suppressing propagation.
     *  Unless setSuppressingPropagation has been called, this
     *  returns false.
     *  @return Returns whether this instance is suppressing propagation.
     *  @see #setSuppressingPropagation(boolean)
     */
    public boolean isSuppressingPropagation() {
        return _suppressingPropagation;
    }

    /** Override the base class to also set the expression of shared
     *  parameters.
     */
    public void setExpression(String expression) {
        super.setExpression(expression);

        if (!_suppressingPropagation) {
            NamedObj toplevel = getRoot();

            // Do not do sharing if this is within an EntityLibrary.
            if (toplevel != null) {
                Iterator sharedParameters = sharedParameterList(toplevel)
                    .iterator();

                while (sharedParameters.hasNext()) {
                    SharedParameter sharedParameter = (SharedParameter) sharedParameters
                        .next();

                    if (sharedParameter != this) {
                        try {
                            sharedParameter._suppressingPropagation = true;

                            if (!sharedParameter.getExpression().equals(expression)) {
                                sharedParameter.setExpression(expression);
                            }
                        } finally {
                            sharedParameter._suppressingPropagation = false;
                        }
                    }
                }
            }
        }
    }

    /** Specify whether this instance should be suppressing
     *  propagation. If this is called with value true, then
     *  changes to the <i>seed</i> or <i>generatorClass</i> parameters of
     *  this instance will not propagate to other instances
     *  in the model.
     *  @param propagation True to suppress propagation.
     *  @see #isSuppressingPropagation()
     */
    public void setSuppressingPropagation(boolean propagation) {
        _suppressingPropagation = propagation;
    }

    /** Return a list of all the shared parameters deeply contained by
     *  the specified container.  If there are no such parameters, then
     *  return an empty list. The list will include this instance if
     *  this instance is deeply contained by the specified container.
     *  A shared parameter is one that is an instance of SharedParameter,
     *  has the same name as this one, and is contained by the container
     *  class specified in the constructor.
     *  @param container The container
     *  @return A list of actors.
     */
    public List sharedParameterList(NamedObj container) {
        LinkedList result = new LinkedList();

        if (_containerClass.isInstance(container)) {
            // If the attribute is not of the right class, get an exception.
            try {
                Attribute candidate = container.getAttribute(getName(),
                        SharedParameter.class);

                if (candidate != null) {
                    result.add(candidate);
                }
            } catch (IllegalActionException ex) {
                // Ignore. Candidate doesn't match.
            }
        }

        Iterator containedObjects = container.containedObjectsIterator();

        while (containedObjects.hasNext()) {
            NamedObj candidateContainer = (NamedObj) containedObjects.next();
            result.addAll(sharedParameterList(candidateContainer));
        }

        return result;
    }

    /** Override the base class to also validate the shared instances.
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    public void validate() throws IllegalActionException {
        super.validate();

        // NOTE: This is called by setContainer(), which is called from
        // within a base class constructor. That call occurs before this
        // object has been fully constructed. It doesn't make sense at
        // that time to propagate validation to shared instances, since
        // in fact the value of this shared parameter will be inferred
        // from those instances if there are any. So in that case, we
        // just return.
        if (_containerClass == null) {
            return;
        }

        if (!_suppressingPropagation) {
            NamedObj toplevel = getRoot();

            // Do not do sharing if this is within an EntityLibrary.
            if (toplevel != null) {
                Iterator sharedParameters = sharedParameterList(toplevel)
                    .iterator();

                while (sharedParameters.hasNext()) {
                    SharedParameter sharedParameter = (SharedParameter) sharedParameters
                        .next();

                    if (sharedParameter != this) {
                        try {
                            sharedParameter._suppressingPropagation = true;
                            sharedParameter.validate();
                        } finally {
                            sharedParameter._suppressingPropagation = false;
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The container class. */
    private Class _containerClass;

    /** Indicator to suppress propagation. */
    private boolean _suppressingPropagation = false;
}
