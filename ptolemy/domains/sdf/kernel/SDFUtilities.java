/* A Scheduler infrastructure for the SDF domain

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red(neuendor@eecs.berkeley.edu)
@AcceptedRating Red(neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import java.util.*;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DependencyDeclaration;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Fraction;

///////////////////////////////////////////////////////////
//// SDFUtilities
/**
This class factors code out of the SDF domain, for use in different
schedulers, so that they can be implemented in a consistent fashion.
This interface contains static methods that are often useful from
outside of an SDFDirector, and so are provided here in an interface
that can be imported.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class SDFUtilities {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the Variable with the specified name in the given port, or
     *  with the specified name preceded by an underscore.  If there
     *  is no such variable, return null;
     *  @param port The port.
     *  @param name The name of the variable.
     */
    public static Variable getRateVariable(Port port, String name)
            throws IllegalActionException {
        Variable parameter = (Variable)port.getAttribute(name);
        if (parameter == null) {
            String altName = "_" + name;
            parameter = (Variable)port.getAttribute(altName);
        }
    
        return parameter;
    }

    /** Get the number of tokens that are consumed on the given port.
     *  If the port is not an input port, then return zero.
     *  Otherwise, return the value of the port's
     *  <i>tokenConsumptionRate</i> parameter.  If this parameter does
     *  not exist, then assume the actor is homogeneous and return
     *  one.
     *  @return The number of tokens the scheduler believes will be consumed
     *  from the given input port during each firing.
     *  @exception IllegalActionException If the tokenConsumptionRate
     *  parameter has an invalid expression.
     */
    public static int getTokenConsumptionRate(IOPort port)
            throws IllegalActionException {
        if (!port.isInput()) {
            return 0;
        } else {
            return _getRateVariableValue(port, "tokenConsumptionRate", 1);
        }
    }

    /** Get the number of tokens that are produced on the given port
     *  during initialization.  If the port is not an
     *  output port, then return zero.  Otherwise, return the value of
     *  the port's <i>tokenInitProduction</i> parameter.   If the parameter
     *  does not exist, then assume the actor is zero-delay and return
     *  a value of zero.
     *  @return The number of tokens the scheduler believes will be produced
     *  from the given output port during initialization.
     *  @exception IllegalActionException If the tokenInitProduction
     *  parameter has an invalid expression.
     */
    public static int getTokenInitProduction(IOPort port)
            throws IllegalActionException {
        if (!port.isOutput()) {
            return 0;
        } else {
            return _getRateVariableValue(port, "tokenInitProduction", 0);
        }
    }
    
    /** Get the number of tokens that are produced on the given port.
     *  If the port is not an output port, then return zero.
     *  Otherwise, return the value of the port's
     *  <i>tokenProductionRate</i> parameter. If the parameter does
     *  not exist, then assume the actor is homogeneous and return a
     *  rate of one.
     *  @return The number of tokens the scheduler believes will be produced
     *   from the given output port during each firing.
     *  @exception IllegalActionException If the tokenProductionRate
     *   parameter has an invalid expression.
     */
    public static int getTokenProductionRate(IOPort port)
            throws IllegalActionException {
        if (!port.isOutput()) {
            return 0;
        } else {
            return _getRateVariableValue(port, "tokenProductionRate", 1);
        }
    }

    /** Set the <i>tokenConsumptionRate</i> parameter of the given port
     *  to the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is not persistent.
     *  That is, it will not be saved in the MoML file if the model is
     *  saved. The port is normally an input port, but this is not
     *  checked.
     *  @exception IllegalActionException If the rate is negative.
     */
    public static void setTokenConsumptionRate(IOPort port, int rate)
            throws IllegalActionException {
        _setRate(port, "tokenConsumptionRate", rate);
    }

    /** Set the <i>tokenInitProduction</i> parameter of the given port to
     *  the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is not persistent.
     *  That is, it will not be saved in the MoML file if the model is
     *  saved. The port is normally an output port, but this is not
     *  checked.
     *  @exception IllegalActionException If the rate is negative.
     */
    public static void setTokenInitProduction(IOPort port, int rate)
            throws IllegalActionException {
        _setRate(port, "tokenInitProduction", rate);
    }

    /** Set the <i>tokenProductionRate</i> parameter of the given port
     *  to the given rate.  If no parameter exists, then create a new one.
     *  The new one is an instance of Variable, so it is transient.
     *  That is, it will not be exported to MoML files.
     *  The port is normally an output port, but this is not checked.
     *  @exception IllegalActionException If the rate is negative.
     */
    public static void setTokenProductionRate(IOPort port, int rate)
            throws IllegalActionException {
        _setRate(port, "tokenProductionRate", rate);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the number of tokens that will be produced or consumed on the
     *  given port.   If the port is an input, then return its consumption
     *  rate, or if the port is an output, then return its production rate.
     *  @exception NotSchedulableException If the port is both an input and
     *  an output, or is neither an input nor an output.
     *  @exception IllegalActionException If a rate does not contain a
     *  valid expression.
     */
    protected static int _getRate(IOPort port)
            throws NotSchedulableException, IllegalActionException {
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
    
    /** Get the integer value stored in the Variable with the
     *  specified name.  If there is still no such variable, then
     *  return the specified default.
     *  @param port The port.
     *  @param name The name of the variable.
     *  @param defaultValue The default value of the variable.
     *  @return A rate.
     */
    public static int _getRateVariableValue(
            Port port, String name, int defaultValue)
            throws IllegalActionException {
        Variable parameter = getRateVariable(port, name);
        if (parameter == null) {
            return defaultValue;
        }
        Token token = parameter.getToken();
  
        if (token instanceof IntToken) {
            return ((IntToken)token).intValue();
        } else {
            throw new IllegalActionException("Variable "
                    + parameter.getFullName()
                    + " was expected "
                    + "to contain an IntToken, but instead "
                    + "contained a "
                    + token.getType()
                    + ".");
        }
    }
    
    /** If a variable with the given name does not exist, then create
     *  a variable with the given name and set the value of that
     *  variable to the specified value. The resulting variable is not
     *  persistent and not editable, but will be visible to the user.
     *  @param port The port.
     *  @param name Name of the variable.
     *  @param value The value.
     */
    protected static void _setIfNotDefined(Port port, String name, int value) 
            throws IllegalActionException {
        Variable rateParameter = (Variable)port.getAttribute(name);
        if (rateParameter == null) {
            try {
                String altName = "_" + name;
                rateParameter = (Variable)port.getAttribute(altName);
                if(rateParameter == null) {
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

    /** If a variable with the given name does not exist, then create
     *  a variable with the given name and set the value of that
     *  variable to the specified value. The resulting variable is not
     *  persistent and not editable, but will be visible to the user.
     *  @param port The port.
     *  @param name Name of the variable.
     *  @param value The value.
     */
    public static void setExpressionIfNotDefined(
            Port port, String name, String value) 
            throws IllegalActionException {
        Variable rateParameter = (Variable)port.getAttribute(name);
        if (rateParameter == null) {
            try {
                String altName = "_" + name;
                rateParameter = (Variable)port.getAttribute(altName);
                if(rateParameter == null) {
                    rateParameter = new Parameter(port, altName);
                    rateParameter.setVisibility(Settable.NOT_EDITABLE);
                    rateParameter.setPersistent(false);
                }
                rateParameter.setExpression(value); 
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
     *  @exception If the variable exists and its value cannot be set.
     */
    protected static void _setOrCreate(
            NamedObj container, String name, int value)
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
     *  @exception If the variable exists and its value cannot be set.
     */
    protected static void _setOrCreate(
            NamedObj container, String name, String expression) 
            throws IllegalActionException {
        Variable variable = _getOrCreate(container, name);
        variable.setExpression(expression);
    }

    /** Set the rate variable with the specified name to the specified
     *  value.  If it doesn't exist, create it.
     *  @param port The port.
     *  @param name The variable name.
     *  @param rate The rate value.
     */
    protected static void _setRate(Port port, String name, int rate)
            throws IllegalActionException {
        if (rate < 0) {
            throw new IllegalActionException(
                    "Negative rate is not allowed: " + rate);
        }
        Variable parameter = (Variable)port.getAttribute(name);
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected classes                   ////

    /** A comparator for named objects.
     */
    public static class NamedObjComparator implements Comparator {
        // Note: This is rather slow, because getFullName is not cached.
        public int compare(Object object1, Object object2) {
            if ((object1 instanceof NamedObj) &&
                    (object2 instanceof NamedObj)) {
                // Compare full names.
                NamedObj namedObject1 = (NamedObj) object1;
                NamedObj namedObject2 = (NamedObj) object2;
                int compare = namedObject1.getFullName().compareTo(
                        namedObject2.getFullName());
                if (compare != 0) return compare;
                // Compare class names.
                Class class1 = namedObject1.getClass();
                Class class2 = namedObject2.getClass();
                compare = class1.getName().compareTo(class2.getName());
                if (compare != 0) return compare;
                if (object1.equals(object2)) {
                    return 0;
                } else {
                    // FIXME This should never happen, hopefully.  Otherwise
                    // the comparator needs to be made more specific.
                    throw new InternalErrorException("Comparator not " +
                            "capable of comparing not equal objects.");
                }
            } else {
                throw new InternalErrorException("Arguments to comparator " +
                        "must be instances of NamedObj: " + object1 + ", " +
                        object2);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    // If a variable exists with the given container and given name,
    // then return it. Otherwise, create the variable and return it.
    private static Variable _getOrCreate(NamedObj container, String name) 
            throws IllegalActionException {
        Variable variable = (Variable)container.getAttribute(name);
        if (variable == null) {
            try {
                variable = new Variable(container, name);
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
