/* A test suite parameter that is shared globally in a model.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.parameters.test;

import java.util.Collection;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SharedParameter

/**
 A test suite parameter that is shared globally in a model.
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (acataldo)
 */
public class TestSharedParameter extends SharedParameter {
    /** Construct a parameter with the given container and name.
     *  The container class will be used to determine which other
     *  instances of TestSharedParameter are shared with this one.
     *  NOTE: Do not use this constructor if you plan to set
     *  a default value. Use the four argument constructor instead.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public TestSharedParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null, "");
    }

    /** Construct a parameter with the given container, name, and
     *  container class. The specified class will be used to determine
     *  which other instances of TestSharedParameter are shared with this one.
     *  NOTE: Do not use this constructor if you plan to set
     *  a default value. Use the four argument constructor instead.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public TestSharedParameter(NamedObj container, String name,
            Class containerClass) throws IllegalActionException,
            NameDuplicationException {
        this(container, name, containerClass, "");
    }

    /** Construct a parameter with the given container, name,
     *  container class, and default value.  This is the preferred
     *  constructor to use.
     *  The specified class will be used to determine
     *  which other instances of TestSharedParameter are shared with this one.
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
    public TestSharedParameter(NamedObj container, String name,
            Class containerClass, String defaultValue)
                    throws IllegalActionException, NameDuplicationException {
        super(container, name, containerClass, defaultValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Infer the value of this parameter from the container
     *  context. That is, search for parameters that are
     *  shared with this one, and set the value of this parameter
     *  to match the last one encountered.
     *  If there are no shared parameters, then assign the
     *  default value given as an argument.
     *  @param defaultValue The default parameter value to give.
     *  @exception InternalErrorException If there are multiple
     *   shared parameters in the model, but their values
     *   do not match.
     */
    @Override
    public void inferValueFromContext(String defaultValue) {
        super.inferValueFromContext(defaultValue);
        inferValueFromContextCount++;
    }

    /** Return true if this instance is suppressing propagation.
     *  Unless setSuppressingPropagation() has been called, this
     *  returns false.
     *  @return Returns whether this instance is suppressing propagation.
     *  @see #setSuppressingPropagation(boolean)
     */
    @Override
    public boolean isSuppressingPropagation() {
        isSuppressingPropagationCount++;
        return super.isSuppressingPropagation();
    }

    /** Override the base class to also set the expression of shared
     *  parameters.
     *  @param expression The expression.
     */
    @Override
    public void setExpression(String expression) {
        setExpressionCount++;
        super.setExpression(expression);
    }

    /** Return a collection of all the shared parameters within the
     *  same model as this parameter.  If there are no such parameters
     *  or if this parameter is deeply contained within an EntityLibrary, then
     *  return an empty collection. The list will include this instance if
     *  this instance.
     *  A shared parameter is one that is an instance of TestSharedParameter,
     *  has the same name as this one, and is contained by the container
     *  class specified in the constructor.
     *  @return A list of parameters.
     */
    @Override
    public synchronized Collection sharedParameterSet() {
        sharedParameterSetCount++;
        //new Exception("TestSharedParameter.sharedParameterSet(): "
        //    + getFullName() + " " + sharedParameterSetCount).printStackTrace();

        return super.sharedParameterSet();
    }

    /** Override the base class to also validate the shared instances.
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        validateCount++;
        //new Exception("TestSharedParameter.validate(): "
        //    + getFullName() + " " + validateCount).printStackTrace();
        return super.validate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to do the propagation only if
     *  the specified destination is not shared.
     *  @param destination Object to which to propagate the
     *   value.
     *  @exception IllegalActionException If the value cannot
     *   be propagated.
     */
    @Override
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
        propagateValueCount++;
        super._propagateValue(destination);
    }

    /** Return the current counts.
     *  @return The current counts.
     */
    public String getCounts() {
        return "inferValueFromContextCount: " + inferValueFromContextCount
                + "\nisSuppressingPropagationCount: "
                + isSuppressingPropagationCount + "\nsetExpressionCount: "
                + setExpressionCount + "\nsharedParameterSetCount: "
                + sharedParameterSetCount + "\nvalidateCount: " + validateCount
                + "\npropagateValueCount: " + propagateValueCount;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public int inferValueFromContextCount;

    public int isSuppressingPropagationCount;

    public int setExpressionCount;

    public int sharedParameterSetCount;

    public int validateCount;

    public int propagateValueCount;
}
