/* A parameter for coordinated numbering in the PtinyOS domain.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.kernel;

import java.util.Iterator;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtinyOSNodeParameter

/**
 A parameter for coordinated numbering in the PtinyOS domain.  This
 parameter is shared throughout a model for coordinated management of
 node ID's and TOSSIM port numbers (command and event ports for
 connecting to TinyViz and other external tools).  Changing the
 expression of any one instance of the parameter will result in all
 instances that are shared being changed.  If the value is being set,
 then all other non-zero values are set to unique numbers by
 incrementing by a value specified by the user (default value of 1)
 while iterating through the shared parameters.  Note that if the
 current value is 0, the value is left unchanged.

 <p> An instance elsewhere in the model (within the same top level) is
 shared if it has the same type and its container is of the class
 specified in the constructor (or of the container class, if no class
 is specified in the constructor).

 <p> One exception is that if this parameter is (deeply) within an
 instance of EntityLibrary, then the parameter is not shared.
 Were this not the case, then opening a library containing this
 parameter would force expansion of all the sublibraries of
 EntityLibrary, which would defeat the lazy instantiation
 of EntityLibrary.

 <p> This parameter is always of type int.

 @author Elaine Cheong
 @version $Id$
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 */
public class PtinyOSNodeParameter extends SharedParameter {
    /** Construct a parameter with the given container and name.
     *  The container class will be used to determine which other
     *  instances of PtinyOSNodeParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtinyOSNodeParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Construct a parameter with the given container and name.
     *  The container class will be used to determine which other
     *  instances of PtinyOSNodeParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param incrementValue The value with which to increment
     *  subsequent parameters.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtinyOSNodeParameter(NamedObj container, String name,
            int incrementValue) throws IllegalActionException,
            NameDuplicationException {
        this(container, name, null);
        _incrementValue = incrementValue;
    }

    /** Construct a parameter with the given container, name, and
     *  container class, using the default value. The specified class
     *  will be used to determine which other instances of
     *  PtinyOSNodeParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtinyOSNodeParameter(NamedObj container, String name,
            Class containerClass) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, containerClass, String.valueOf(_defaultValue));
        setTypeEquals(BaseType.INT);
    }

    /** Construct a parameter with the given container, name, and
     *  container class, using the default value. The specified class
     *  will be used to determine which other instances of
     *  PtinyOSNodeParameter are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @param incrementValue The value with which to increment
     *  subsequent parameters.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtinyOSNodeParameter(NamedObj container, String name,
            Class containerClass, int incrementValue)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, containerClass, String.valueOf(_defaultValue));
        setTypeEquals(BaseType.INT);

        _incrementValue = incrementValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set the declared type before
     *  attempting to infer the value.  This is necessary because
     *  this method is called in the constructor of the base class,
     *  before the declared type has been set.
     *  @param defaultValue The default parameter value to use.
     *  @exception InternalErrorException If there are multiple
     *   shared parameters in the model, but their values
     *   do not match.
     */
    public void inferValueFromContext(String defaultValue) {
        try {
            setTypeEquals(BaseType.INT);
        } catch (IllegalActionException e) {
            // This should have been caught before.
            throw new InternalErrorException(e);
        }
        super.inferValueFromContext(defaultValue);
    }

    /** Set the expression of the shared parameters.
     */
    public void setExpression(String expression) {
        boolean previousSuppress = isSuppressingPropagation();

        try {
            setSuppressingPropagation(true);
            super.setExpression(expression);
        } finally {
            setSuppressingPropagation(previousSuppress);
        }

        // Have to evaluate the expression using getToken(),
        // rather than parsing the string. Unfortunately,
        // the Variable base class doesn't allow us to throw
        // IllegalActionException.  Too bad...
        try {
            IntToken token = (IntToken) getToken();
            int value = _defaultValue;

            if (token != null) {
                value = token.intValue();
            }


            // Need to assign unique values.
            if (!isSuppressingPropagation()) {
                // Ensure that when the model is saved, that this
                // parameter value, and only this one, is saved.
                // The shared parameters are made non-persistent below.
                setPersistent(true);
                Iterator sharedParameters =
                    sharedParameterSet().iterator();

                while (sharedParameters.hasNext()) {
                    PtinyOSNodeParameter sharedParameter =
                        (PtinyOSNodeParameter) sharedParameters.next();

                    if (sharedParameter != this) {
                        try {
                            sharedParameter.setSuppressingPropagation(
                                    true);
                            if (((IntToken) sharedParameter.getToken())
                                    .intValue() != 0) {
                                // Only auto increment value if
                                // the current value is not 0.
                                value += _incrementValue;

                                String newExpression = String
                                    .valueOf(value);

                                if (!sharedParameter.getExpression().equals(
                                            newExpression)) {
                                    sharedParameter.setExpression(
                                            newExpression);

                                    // Make sure the new value is not
                                    // persistent.
                                    sharedParameter.setPersistent(false);
                                }
                            }
                        } finally {
                            sharedParameter.setSuppressingPropagation(
                                    previousSuppress);
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // FIXME: This is a lousy way to report a syntax error.
            throw new InternalErrorException(ex);
        }
    }

    /** Value by which to increment other occurrences of this parameter.
     */
    private int _incrementValue = 1;

    /** Default value of this parameter.
     */
    private static int _defaultValue = 1;
}
