/* Dataflow utilities

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Comparator;

import ptolemy.actor.IOPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.TemporaryVariable;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// DFUtilities

/**
 This class factors code out of the SDF domain, for use in different
 schedulers, so that they can be implemented in a consistent fashion.
 This interface contains static methods that are often useful from
 outside of an SDFDirector, and so are provided here in an interface
 that can be imported.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class DFUtilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

    /** A comparator for named objects.
     */
    public static class NamedObjComparator implements Comparator {
        /** Compare two objects.
         *  If the objects are not NamedObjs, then an InternalErrorException
         *  is thrown.
         *  @param object1 The first object to be compared.
         *  @param object2 The second object to be compared.
         *  @return 0 if the objects are the same.
         */
        @Override
        public int compare(Object object1, Object object2) {
            // Note: This is rather slow, because getFullName is not cached.

            if (object1 instanceof NamedObj && object2 instanceof NamedObj) {
                // Compare full names.
                NamedObj namedObject1 = (NamedObj) object1;
                NamedObj namedObject2 = (NamedObj) object2;
                int compare = namedObject1.getFullName().compareTo(
                        namedObject2.getFullName());

                if (compare != 0) {
                    return compare;
                }

                // Compare class names.
                Class class1 = namedObject1.getClass();
                Class class2 = namedObject2.getClass();
                compare = class1.getName().compareTo(class2.getName());

                if (compare != 0) {
                    return compare;
                }

                if (object1.equals(object2)) {
                    return 0;
                } else {
                    // FIXME This should never happen, hopefully.  Otherwise
                    // the comparator needs to be made more specific.
                    throw new InternalErrorException("Comparator not "
                            + "capable of comparing not equal objects.");
                }
            } else {
                throw new InternalErrorException("Arguments to comparator "
                        + "must be instances of NamedObj: " + object1 + ", "
                        + object2);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of tokens that will be produced or consumed on the
     *  given port.   If the port is an input, then return its consumption
     *  rate, or if the port is an output, then return its production rate.
     *  @param port The given port.
     *  @return The number of tokens that will be produced or consumed on the
     *  given port.
     *  @exception NotSchedulableException If the port is both an input and
     *  an output, or is neither an input nor an output.
     *  @exception IllegalActionException If a rate does not contain a
     *  valid expression.
     *  @see #setRate
     */
    public static int getRate(IOPort port) throws NotSchedulableException,
    IllegalActionException {
        if (port.isInput() && port.isOutput()) {
            throw new NotSchedulableException(port,
                    "Port is both an input and an output, which is not"
                            + " allowed in SDF.");
        } else if (port.isInput()) {
            return getTokenConsumptionRate(port);
        } else if (port.isOutput()) {
            return getTokenProductionRate(port);
        } else {
            throw new NotSchedulableException(port,
                    "Port is neither an input and an output, which is not"
                            + " allowed in SDF.");
        }
    }

    /** Get the Variable with the specified name in the given port, or
     *  with the specified name preceded by an underscore.  If there
     *  is no such variable, return null.
     *  @param port The port.
     *  @param name The name of the variable.
     *  @return The variable with the specified name in the given port.
     *  @see #setRateVariable(Port, String, int)
     */
    public static Variable getRateVariable(Port port, String name) {
        Variable parameter = (Variable) port.getAttribute(name);

        if (parameter == null) {
            String altName = "_" + name;
            parameter = (Variable) port.getAttribute(altName);
        }

        return parameter;
    }

    /** Get the integer value stored in the Variable with the
     *  specified name.  If there is still no such variable, then
     *  return the specified default.
     *  @param port The port.
     *  @param name The name of the variable.
     *  @param defaultValue The default value of the variable.
     *  @return A rate.
     *  @exception IllegalActionException If the variable does not contain
     *  a valid token, or the token is not an IntToken.
     */
    public static int getRateVariableValue(Port port, String name,
            int defaultValue) throws IllegalActionException {
        Variable parameter = getRateVariable(port, name);

        if (parameter == null) {
            return defaultValue;
        }

        Token token = parameter.getToken();

        if (token == null) {
            // The tokenConsumptionRate parameter is present, but was
            // not set.  BooleanSelect had this problem.
            return defaultValue;
        }

        if (token.isNil()) {
            throw new IllegalActionException(port,
                    "Port rate parameter value is missing (is nil).");
        }

        if (token instanceof IntToken) {
            return ((IntToken) token).intValue();
        } else {
            throw new IllegalActionException("Variable "
                    + parameter.getFullName() + " was expected "
                    + "to contain an IntToken, but instead " + "contained a "
                    + token.getType() + ".");
        }
    }

    /** Get the number of tokens that are consumed on the given port.
     *  If the port is not an input port, then return zero.
     *  Otherwise, return the value of the port's
     *  <i>tokenConsumptionRate</i> parameter.  If this parameter does
     *  not exist, then assume the actor is homogeneous and return
     *  one.
     *  @param port The given port.
     *  @return The number of tokens the scheduler believes will be consumed
     *  from the given input port during each firing.
     *  @exception IllegalActionException If the tokenConsumptionRate
     *  parameter has an invalid expression.
     *  @see #setTokenConsumptionRate
     */
    public static int getTokenConsumptionRate(IOPort port)
            throws IllegalActionException {
        if (!port.isInput()) {
            return 0;
        } else {
            return getRateVariableValue(port, "tokenConsumptionRate", 1);
        }
    }

    /** Get the number of tokens that are initially
     *  available on the given input port
     *  after initialization.  If the port is not an
     *  input port, then presumably any initial tokens
     *  will be available on the inside.  Return the value of
     *  the port's <i>tokenInitConsumption</i> parameter.   If the parameter
     *  does not exist, then assume the port has no initial tokens.
     *  a value of zero.
     *  @param port The given port.
     *  @return The number of tokens the scheduler believes will be available
     *   at the given input port after initialization.
     *  @exception IllegalActionException If the tokenInitConsumption
     *   parameter has an invalid expression.
     *  @see #setTokenInitConsumption
     */
    public static int getTokenInitConsumption(IOPort port)
            throws IllegalActionException {
        return getRateVariableValue(port, "tokenInitConsumption", 0);
    }

    /** Get the number of tokens that are produced on the given port
     *  during initialization.  If the port is not an
     *  output port, then the number of tokens is presumably the number
     *  of initial tokens produced on the inside of the port.
     *  The number of tokens returned is the value of
     *  the port's <i>tokenInitProduction</i> parameter.   If the parameter
     *  does not exist, then assume the actor is zero-delay and return
     *  a value of zero.
     *  @param port The given port.
     *  @return The number of tokens the scheduler believes will be produced
     *  from the given output port during initialization.
     *  @exception IllegalActionException If the tokenInitProduction
     *  parameter has an invalid expression.
     *  @see #setTokenInitProduction
     */
    public static int getTokenInitProduction(IOPort port)
            throws IllegalActionException {
        return getRateVariableValue(port, "tokenInitProduction", 0);
    }

    /** Get the number of tokens that are produced on the given port.
     *  If the port is not an output port, then return zero.
     *  Otherwise, return the value of the port's
     *  <i>tokenProductionRate</i> parameter. If the parameter does
     *  not exist, then assume the actor is homogeneous and return a
     *  rate of one.
     *  @param port The given port.
     *  @return The number of tokens the scheduler believes will be produced
     *   from the given output port during each firing.
     *  @exception IllegalActionException If the tokenProductionRate
     *   parameter has an invalid expression.
     *  @see #setTokenProductionRate
     */
    public static int getTokenProductionRate(IOPort port)
            throws IllegalActionException {
        if (!port.isOutput()) {
            return 0;
        } else {
            return getRateVariableValue(port, "tokenProductionRate", 1);
        }
    }

    /** If a variable with the given name does not exist, then create
     *  a variable with the given name and set the value of that
     *  variable to the specified value. The resulting variable is not
     *  persistent and not editable, but will be visible to the user.
     *  @param port The port.
     *  @param name Name of the variable.
     *  @param value The value.
     *  @exception IllegalActionException If a new parameter can not be
     *  created for the give port.
     */
    public static void setExpressionIfNotDefined(Port port, String name,
            String value) throws IllegalActionException {
        Variable rateParameter = (Variable) port.getAttribute(name);

        if (rateParameter == null) {
            try {
                String altName = "_" + name;
                rateParameter = (Variable) port.getAttribute(altName);

                if (rateParameter == null) {
                    rateParameter = new Parameter(port, altName);
                    rateParameter.setVisibility(Settable.NOT_EDITABLE);
                    rateParameter.setPersistent(false);
                }

                rateParameter.setExpression(value);
                rateParameter.validate();
            } catch (KernelException ex) {
                throw new InternalErrorException(port, ex, "Should not occur");
            }
        }
    }

    /** If a variable with the given name does not exist, then create
     *  a variable with the given name and set the value of that
     *  variable to the specified value. The resulting variable is not
     *  persistent and not editable, but will be visible to the user.
     *  @param port The port.
     *  @param name Name of the variable.
     *  @param value The value.
     *  @exception IllegalActionException If a new parameter can not be
     *  created for the given port, or the given value is not an acceptable.
     */
    public static void setIfNotDefined(Port port, String name, int value)
            throws IllegalActionException {
        Variable rateParameter = (Variable) port.getAttribute(name);

        if (rateParameter == null) {
            try {
                String altName = "_" + name;
                rateParameter = (Variable) port.getAttribute(altName);

                if (rateParameter == null) {
                    rateParameter = new Parameter(port, altName);
                    rateParameter.setVisibility(Settable.NOT_EDITABLE);
                    rateParameter.setPersistent(false);
                }

                rateParameter.setToken(new IntToken(value));
            } catch (KernelException ex) {
                throw new InternalErrorException(port, ex, "Should not occur");
            }
        }
    }

    /** If the specified container does not contain a variable with
     *  the specified name, then create such a variable and set its
     *  value to the specified integer.  The resulting variable is not
     *  persistent and not editable, but will be visible to the user.
     *  If the variable does exist, then just set its value.
     *  @param container The container.
     *  @param name Name of the variable.
     *  @param value The value.
     *  @exception IllegalActionException If the variable exists and
     *  its value cannot be set.
     */
    public static void setOrCreate(NamedObj container, String name, int value)
            throws IllegalActionException {
        Variable variable = _getOrCreate(container, name);
        variable.setToken(new IntToken(value));
    }

    /** If the specified container does not contain a variable with
     *  the specified name, then create such a variable and set its
     *  expression to the specified string.  The resulting variable is not
     *  persistent and not editable, but will be visible to the user.
     *  If the variable does exist, then just set its expression.
     *  @param container The container.
     *  @param name Name of the variable.
     *  @param expression The expression.
     *  @exception IllegalActionException If the variable exists and
     *  its value cannot be set.
     */
    public static void setOrCreate(NamedObj container, String name,
            String expression) throws IllegalActionException {
        Variable variable = _getOrCreate(container, name);
        variable.setExpression(expression);
    }

    /** Set the rate variable with the specified name to the specified
     *  value.  If it doesn't exist, create it.
     *  @param port The port.
     *  @param name The variable name.
     *  @param rate The rate value.
     *  @exception IllegalActionException If the rate is a negative integer,
     *  or the rate can not be set.
     *  @return The rate parameter.
     *  @see #getRate(IOPort)
     */
    public static Variable setRate(Port port, String name, int rate)
            throws IllegalActionException {
        if (rate < 0) {
            throw new IllegalActionException("Negative rate is not allowed: "
                    + rate);
        }

        Variable parameter = (Variable) port.getAttribute(name);

        if (parameter != null) {
            parameter.setToken(new IntToken(rate));
        } else {
            try {
                // Use Variable rather than Parameter so the
                // value is transient.
                parameter = new Variable(port, name, new IntToken(rate));
                parameter.setVisibility(Settable.NOT_EDITABLE);
                parameter.setPersistent(false);
            } catch (KernelException ex) {
                throw new InternalErrorException(port, ex, "Should not occur");
            }
        }
        return parameter;
    }

    /** If a variable with the given name does not exist, then create
     *  a variable with the given name. Then set the value of the
     *  variable to the specified value.
     *  @param port The port.
     *  @param name Name of the variable.
     *  @param value The value.
     *  @exception IllegalActionException If a new parameter can not be
     *  created for the given port, or the given value is not an acceptable.
     *  @see #getRateVariable(Port, String)
     */
    public static void setRateVariable(Port port, String name, int value)
            throws IllegalActionException {
        Variable rateParameter = (Variable) port.getAttribute(name);

        if (rateParameter == null) {
            try {
                String altName = "_" + name;
                rateParameter = (Variable) port.getAttribute(altName);

                if (rateParameter == null) {
                    rateParameter = new Parameter(port, altName);
                    rateParameter.setVisibility(Settable.NOT_EDITABLE);
                    rateParameter.setPersistent(false);
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(port, ex, "Should not occur");
            }
        }
        rateParameter.setToken(new IntToken(value));
    }

    /** Set the <i>tokenConsumptionRate</i> parameter of the given port
     *  to the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is not persistent.
     *  That is, it will not be saved in the MoML file if the model is
     *  saved. The port is normally an input port, but this is not
     *  checked.
     *  @param port The given port.
     *  @param rate The given rate.
     *  @exception IllegalActionException If the rate is negative.
     *  @see #getTokenConsumptionRate
     */
    public static void setTokenConsumptionRate(IOPort port, int rate)
            throws IllegalActionException {
        setRate(port, "tokenConsumptionRate", rate);
    }

    /** Set the <i>tokenInitConsumption</i> parameter of the given port to
     *  the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is not persistent.
     *  That is, it will not be saved in the MoML file if the model is
     *  saved. The port is normally an output port, but this is not
     *  checked.
     *  @param port The given port.
     *  @param rate The given rate.
     *  @exception IllegalActionException If the rate is negative.
     *  @see #getTokenInitConsumption(IOPort)
     */
    public static void setTokenInitConsumption(IOPort port, int rate)
            throws IllegalActionException {
        setRate(port, "tokenInitConsumption", rate);
    }

    /** Set the <i>tokenInitProduction</i> parameter of the given port to
     *  the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is not persistent.
     *  That is, it will not be saved in the MoML file if the model is
     *  saved. The port is normally an output port, but this is not
     *  checked.
     *  @param port The given port.
     *  @param rate The given rate.
     *  @exception IllegalActionException If the rate is negative.
     *  @see #getTokenInitProduction
     */
    public static void setTokenInitProduction(IOPort port, int rate)
            throws IllegalActionException {
        setRate(port, "tokenInitProduction", rate);
    }

    /** Set the <i>tokenProductionRate</i> parameter of the given port
     *  to the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is transient.
     *  That is, it will not be exported to MoML files.
     *  The port is normally an output port, but this is not checked.
     *  @param port The given port.
     *  @param rate The given rate.
     *  @exception IllegalActionException If the rate is negative.
     *  @see #getTokenProductionRate
     */
    public static void setTokenProductionRate(IOPort port, int rate)
            throws IllegalActionException {
        setRate(port, "tokenProductionRate", rate);
    }

    /** Depending on the given flag, add an invisible, persistent
     *  variable named "_showRate" with value true to the given port
     *  that indicates to the user interface that rate parameters on
     *  the given port should be displayed in the user interface.
     *  @param port The port.
     *  @param flag The flag.
     *  @exception IllegalActionException If a new parameter can not be
     *  created for the given port, or the given flag is not an acceptable.
     */
    public static void showRate(Port port, boolean flag)
            throws IllegalActionException {
        String name = "_showRate";

        // Look for an existing parameter.
        Variable variable = (Variable) port.getAttribute(name);

        if (variable == null) {
            try {
                variable = new Parameter(port, name);
                variable.setVisibility(Settable.EXPERT);
                variable.setPersistent(false);
            } catch (KernelException ex) {
                throw new InternalErrorException(port, ex, "Should not occur");
            }
        }

        variable.setToken(BooleanToken.getInstance(flag));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // If a variable exists with the given container and given name,
    // then return it. Otherwise, create the variable and return it.
    private static Variable _getOrCreate(NamedObj container, String name) {
        Variable variable = (Variable) container.getAttribute(name);

        if (variable == null) {
            try {
                variable = new TemporaryVariable(container, name);
                variable.setVisibility(Settable.NOT_EDITABLE);
                variable.setPersistent(false);
            } catch (KernelException ex) {
                throw new InternalErrorException(container, ex,
                        "Should not occur");
            }
        }

        return variable;
    }
}
