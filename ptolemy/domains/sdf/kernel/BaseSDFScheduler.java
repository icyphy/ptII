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

@author Stephen Neuendorffer, Shuvra S. Bhattacharyya 
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
        Variable variable = 
            (Variable)SDFUtilities._getRateVariable(port, name);
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
                    Object bufferSizeObject = 
                            minimumBufferSizes.get(relation);
                    if (bufferSizeObject instanceof Integer) {
                        int bufferSize = ((Integer)bufferSizeObject).intValue();
                        SDFUtilities._setOrCreate(relation, "bufferSize", bufferSize);
                        if (_debugging) {
                            _debug("Adding bufferSize parameter to "
                                    + relation.getName() +
                                    " with value " + bufferSize);
                        }
                    } else if (bufferSizeObject instanceof String) {
                        String bufferSizeExpression = (String)bufferSizeObject; 
                        SDFUtilities._setOrCreate(relation, "bufferSize", 
                                bufferSizeExpression);
                        if (_debugging) {
                            _debug("Adding bufferSize parameter to "
                                    + relation.getName() +
                                    " with expression " + bufferSizeExpression);
                        }
                    } else if (bufferSizeObject == null) {
                    } else {
                        throw new InternalErrorException("Invalid value found "
                                + "in buffer size map.\nValue is of type "
                                + bufferSizeObject.getClass().getName() 
                                + ".\nIt should be of type Integer or String.\n"                                );
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
                SDFUtilities._setIfNotDefined(
                        port, "tokenConsumptionRate", rate.intValue());
                if (_debugging && VERBOSE) {
                    _debug("Setting tokenConsumptionRate to "
                            + rate.intValue());
                }
            } else if (port.isOutput()) {
                SDFUtilities._setIfNotDefined(
                        port, "tokenProductionRate", rate.intValue());
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
                        newRate = 
                            SDFUtilities.getTokenInitProduction(connectedPort);
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
                SDFUtilities._setIfNotDefined(
                        port, "tokenInitProduction", inferredRate);
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
                    SDFUtilities._setOrCreate(entity, "firingsPerIteration", firingCount);
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

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    protected static final boolean VERBOSE = false;

}
