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
//// BaseSDFScheduler
/**
This class factors code out of the SDF domain, for use in different
schedulers, so that they can be implemented in a consistent fashion.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public abstract class BaseSDFScheduler extends Scheduler {

    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public BaseSDFScheduler() {
        super();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public BaseSDFScheduler(Workspace workspace) {
        super(workspace);
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
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
    public BaseSDFScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

    /** Add a DependencyDeclaration (with the name
     * "_SDFRateDependencyDeclaration") to the variable with the given
     * name in the given port that declares the variable is dependent
     * on the given list of variables.  If a dependency declaration
     * with that name already exists, then simply set its dependents
     * list to the given list.
     */
    protected void _declareDependency(ConstVariableModelAnalysis analysis,
            Port port, String name, List dependents) 
            throws IllegalActionException {
        if(_debugging && VERBOSE) {
            _debug("declaring dependency for rate variable " + 
                    name + " in port " + port.getFullName());
        }
        Variable variable = (Variable)_getRateVariable(port, name);
        DependencyDeclaration declaration = (DependencyDeclaration)
            variable.getAttribute(
                    "_SDFRateDependencyDeclaration", 
                    DependencyDeclaration.class);
        if(declaration == null) {
            try {
                declaration = new DependencyDeclaration(variable, 
                        "_SDFRateDependencyDeclaration");
            } catch (NameDuplicationException ex) {
                // Ignore... should not happen.
            }
        }
        declaration.setDependents(dependents);
        analysis.addDependencyDeclaration(declaration);
    }

    /** Find the channel number of the given port that corresponds to the
     *  given receiver.  If the receiver is not contained within the port,
     *  throw an InternalErrorException.
     */
    // FIXME: Move this functionality to the kernel.
    protected int _getChannel(IOPort port, Receiver receiver)
            throws IllegalActionException {
        int width = port.getWidth();
        Receiver[][] receivers = port.getReceivers();
        int channel;
        if (_debugging && VERBOSE) {
            _debug("-- getting channels on port " + port.getFullName());
            _debug("port width = " + width);
            _debug("number of channels = " + receivers.length);
        }
        for (channel = 0;
             channel < receivers.length;
             channel++) {
            if (_debugging && VERBOSE) {
                _debug("number of receivers in channel " + channel
                        + " = " + receivers[channel].length);
            }
            for (int destinationIndex = 0;
                 destinationIndex < receivers[channel].length;
                 destinationIndex++) {
                if (receivers[channel][destinationIndex] == receiver) {
                    if (_debugging && VERBOSE) {
                        _debug("-- returning channel number:" + channel);
                    }
                    return channel;
                }
            }
        }
        // Hmm...  didn't find it yet.  Port might be connected on the inside,
        // so try the inside relations.
        receivers = port.getInsideReceivers();
        for (channel = 0;
             channel < receivers.length;
             channel++) {
            if (_debugging && VERBOSE) {
                _debug("number of inside receivers = "
                        + receivers[channel].length);
            }
            for (int destinationIndex = 0;
                 destinationIndex < receivers[channel].length;
                 destinationIndex++) {
                if (receivers[channel][destinationIndex] == receiver) {
                    return channel;
                }
            }
        }

        throw new InternalErrorException("Receiver for port " +
                receiver.getContainer() + " not found in the port " +
                port.getFullName());
    }

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
    
    /** Get the Variable with the specified name in the given port, or
     *  with the specified name preceded by an underscore.  If there
     *  is no such variable, return null;
     *  @param port The port.
     *  @param name The name of the variable.
     */
    protected static Variable _getRateVariable(Port port, String name)
            throws IllegalActionException {
        Variable parameter = (Variable)port.getAttribute(name);
        if (parameter == null) {
            String altName = "_" + name;
            parameter = (Variable)port.getAttribute(altName);
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
     */
    protected static int _getRateVariableValue(
            Port port, String name, int defaultValue)
            throws IllegalActionException {
        Variable parameter = _getRateVariable(port, name);
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
    
    /** Create and set a parameter in each relation according
     *  to the buffer sizes calculated for this system.
     *  @param minimumBufferSizes A map from relation
     *  to the minimum possible buffer size of that relation.
     */
    protected void _saveBufferSizes(final Map minimumBufferSizes) {
        Director director = (Director) getContainer();
        final CompositeActor container =
            (CompositeActor)director.getContainer();
        ChangeRequest request = 
            new ChangeRequest(this, "Record buffer sizes") {
            protected void _execute() throws KernelException {
                Iterator relations = container.relationList().iterator();
                while (relations.hasNext()) {
                    Relation relation = (Relation)relations.next();
                    int bufferSize =
                        ((Integer)minimumBufferSizes.get(relation)).intValue();
                    _setOrCreate(relation, "bufferSize", bufferSize);
                    if (_debugging) {
                        _debug("Adding bufferSize parameter to "
                                + relation.getName() +
                                " with value " + bufferSize);
                    }
                }
            }
        };
        // Indicate that the change is non-persistent, so that
        // the UI doesn't prompt to save.
        request.setPersistent(false);
        container.requestChange(request);
    }

    /** Push the rates calculated for this system up to the contained Actor,
     *  but only if the ports do not have a set rates.
     *  This allows the container to be properly scheduled if it is
     *  in a hierarchical system and the outside system is SDF.
     *  @param externalRates A map from external port to the rate of that
     *   port.
     *  @exception IllegalActionException If any called method throws it.
     *  @exception NotSchedulableException If an external port is both
     *   an input and an output, or neither an input or an output, or
     *   connected on the inside to ports that have different
     *   tokenInitProduction.
     */
    protected void _saveContainerRates(Map externalRates)
            throws NotSchedulableException, IllegalActionException {
        Director director = (Director) getContainer();
        CompositeActor container = (CompositeActor) director.getContainer();
        Iterator ports = container.portList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            if (_debugging && VERBOSE) {
                _debug("External Port " + port.getName());
            }
            Integer rate = (Integer)externalRates.get(port);
            if (port.isInput() && port.isOutput()) {
                throw new NotSchedulableException(port,
                        "External port is both an input and an output, "
                        + "which is not allowed in SDF.");
            } else if (port.isInput()) {
                _setIfNotDefined(port, "tokenConsumptionRate", rate.intValue());
                if (_debugging && VERBOSE) {
                    _debug("Setting tokenConsumptionRate to "
                            + rate.intValue());
                }
            } else if (port.isOutput()) {
                _setIfNotDefined(port, "tokenProductionRate", rate.intValue());
                if (_debugging && VERBOSE) {
                    _debug("Setting tokenProductionRate to "
                            + rate.intValue());
                }
                // Infer init production.
                // Note that this is a very simple type of inference...
                // However, in general, we don't want to try to
                // flatten this model...
                Iterator connectedPorts =
                    port.insideSourcePortList().iterator();
                IOPort foundOutputPort = null;
                int inferredRate = 0;
                while (connectedPorts.hasNext()) {
                    IOPort connectedPort = (IOPort) connectedPorts.next();
                    
                    int newRate;
                    if (connectedPort.isOutput()) {
                        newRate = getTokenInitProduction(connectedPort);
                    } else {
                        newRate = 0;
                    }
                    // If we've already set the rate, then check that the
                    // rate for any other internal port is correct.
                    if (foundOutputPort != null &&
                            newRate != inferredRate) {
                        throw new NotSchedulableException(
                                "External output port " + port
                                + " is connected on the inside to ports "
                                + "with different initial production: "
                                + foundOutputPort + " and "
                                + connectedPort);
                    }
                    foundOutputPort = connectedPort;
                    inferredRate = newRate;
                }
                _setIfNotDefined(port, "tokenInitProduction",
                        inferredRate);
                if (_debugging && VERBOSE) {
                    _debug("Setting tokenInitProduction to "
                            + inferredRate);
                }
            } else {
                throw new NotSchedulableException(port,
                        "External port is neither an input and an output, "
                        + "which is not allowed in SDF.");
            }
        }
    }

    /** Create and set a parameter in each actor according
     *  to the number of times it will fire in one execution of the schedule.
     *  @param entityToFiringsPerIteration A map from actor to firing count.
     */
    protected void _saveFiringCounts(final Map entityToFiringsPerIteration) {
        Director director = (Director) getContainer();
        final CompositeActor container =
                (CompositeActor)director.getContainer();
            
        ChangeRequest request = 
            new ChangeRequest(this, "Record firings per iteration") {
            protected void _execute() throws KernelException {
                Iterator entities = entityToFiringsPerIteration.keySet().iterator();
                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    int firingCount =
                            ((Integer)entityToFiringsPerIteration.get(entity)).intValue();
                    _setOrCreate(entity, "firingsPerIteration", firingCount);
                    if (_debugging) {
                        _debug("Adding firingsPerIteration parameter to " 
                                + entity.getName() + " with value " 
                                + firingCount);
                    }
                }
            }
        };
        // Indicate that the change is non-persistent, so that
        // the UI doesn't prompt to save.
        request.setPersistent(false);
        container.requestChange(request);
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
        Variable rateParameter = (Variable)container.getAttribute(name);
        if (rateParameter == null) {
            try {
                rateParameter = new Variable(container, name);
                rateParameter.setVisibility(Settable.NOT_EDITABLE);
                rateParameter.setPersistent(false);
            } catch (KernelException ex) {
                throw new InternalErrorException(container, ex,
                        "Should not occur");
            }
        }
        rateParameter.setToken(new IntToken(value));
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
    protected class NamedObjComparator implements Comparator {
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
    ////                        protected variables                ////

    protected static final boolean VERBOSE = false;
}
