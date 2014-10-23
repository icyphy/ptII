/* A Scheduler infrastructure for the SDF domain

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.sdf.kernel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.DependencyDeclaration;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// BaseSDFScheduler

/**
 This class factors code out of the SDF domain, for use in different
 schedulers, so that they can be implemented in a consistent fashion.

 @author Stephen Neuendorffer, Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
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

    /** Declare the rate dependency on any external ports of the model.
     *  SDF directors should invoke this method once during preinitialize.
     *  @exception IllegalActionException If there is a problem setting
     *  the rate dependency on an external port.
     */
    public abstract void declareRateDependency() throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a DependencyDeclaration (with the name
     *  "_SDFRateDependencyDeclaration") to the variable with the given
     *  name in the given port that declares the variable is dependent
     *  on the given list of variables.  If a dependency declaration
     *  with that name already exists, then simply set its dependents
     *  list to the given list.
     *  @param analysis The ConstVariableModelAnalysis
     *  @param port The port that gets the DependencyDeclaration.
     *  @param name The name of the DependencyDeclaration.
     *  @param dependents The dependents.
     *  @exception IllegalActionException If there is a problem setting
     *  the rate dependency on a port
     */
    @SuppressWarnings("unused")
    protected void _declareDependency(ConstVariableModelAnalysis analysis,
            Port port, String name, List dependents)
                    throws IllegalActionException {
        if (_debugging && VERBOSE) {
            _debug("declaring dependency for rate variable " + name
                    + " in port " + port.getFullName());
        }

        Variable variable = DFUtilities.getRateVariable(port, name);
        DependencyDeclaration declaration = (DependencyDeclaration) variable
                .getAttribute("_SDFRateDependencyDeclaration",
                        DependencyDeclaration.class);

        if (declaration == null) {
            try {
                declaration = new DependencyDeclaration(variable,
                        "_SDFRateDependencyDeclaration");
            } catch (NameDuplicationException ex) {
                // We used to ignore this, but FindBugs would complain
                // that declaration could still be null.
                throw new InternalErrorException("Failed to construct "
                        + "_SDFRateDependencyDeclaration");
            }
        }

        declaration.setDependents(dependents);
        analysis.addDependencyDeclaration(declaration);
    }

    /** Create and set a parameter in each relation according
     *  to the buffer sizes calculated for this system.
     *  @param minimumBufferSizes A map from relation
     *  to the minimum possible buffer size of that relation.
     */
    protected void _saveBufferSizes(final Map minimumBufferSizes) {
        Director director = (Director) getContainer();
        final CompositeActor container = (CompositeActor) director
                .getContainer();

        // FIXME: These buffer sizes should be properties of input ports,
        // not properties of relations.
        ChangeRequest request = new ChangeRequest(this, "Record buffer sizes") {
            @Override
            protected void _execute() throws KernelException {
                Iterator relations = container.relationList().iterator();

                while (relations.hasNext()) {
                    Relation relation = (Relation) relations.next();
                    Object bufferSizeObject = minimumBufferSizes.get(relation);

                    if (bufferSizeObject instanceof Integer) {
                        int bufferSize = ((Integer) bufferSizeObject)
                                .intValue();
                        DFUtilities.setOrCreate(relation, "bufferSize",
                                bufferSize);

                        if (_debugging) {
                            _debug("Adding bufferSize parameter to "
                                    + relation.getName() + " with value "
                                    + bufferSize);
                        }
                    } else if (bufferSizeObject instanceof String) {
                        String bufferSizeExpression = (String) bufferSizeObject;
                        DFUtilities.setOrCreate(relation, "bufferSize", "\""
                                + bufferSizeExpression + "\"");

                        if (_debugging) {
                            _debug("Adding bufferSize parameter to "
                                    + relation.getName() + " with expression "
                                    + bufferSizeExpression);
                        }
                    } else if (bufferSizeObject == null) {
                    } else {
                        throw new InternalErrorException(
                                "Invalid value found "
                                        + "in buffer size map.\nValue is of type "
                                        + bufferSizeObject.getClass().getName()
                                        + ".\nIt should be of type Integer or String.\n");
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
    @SuppressWarnings("unused")
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

            Integer rate = (Integer) externalRates.get(port);

            if (port.isInput() && port.isOutput()) {
                throw new NotSchedulableException(port,
                        "External port is both an input and an output, "
                                + "which is not allowed in SDF.");
            } else if (port.isInput()) {
                DFUtilities.setIfNotDefined(port, "tokenConsumptionRate",
                        rate.intValue());

                if (_debugging && VERBOSE) {
                    _debug("Setting tokenConsumptionRate to " + rate.intValue());
                }

                // External ports do not any initial consumption tokens
                // that are caused by the inside model, so we set this
                // parameter to zero.
                DFUtilities.setIfNotDefined(port, "tokenInitConsumption", 0);

                if (_debugging && VERBOSE) {
                    _debug("Setting tokenInitConsumption to 0.");
                }
            } else if (port.isOutput()) {
                DFUtilities.setIfNotDefined(port, "tokenProductionRate",
                        rate.intValue());

                if (_debugging && VERBOSE) {
                    _debug("Setting tokenProductionRate to " + rate.intValue());
                }

                // Infer init production.
                // Note that this is a very simple type of inference...
                // However, in general, we don't want to try to
                // flatten this model...
                Iterator connectedPorts = port.insideSourcePortList()
                        .iterator();
                IOPort foundOutputPort = null;
                int inferredRate = 0;

                while (connectedPorts.hasNext()) {
                    IOPort connectedPort = (IOPort) connectedPorts.next();

                    int newRate;

                    if (connectedPort.isOutput()) {
                        newRate = DFUtilities
                                .getTokenInitProduction(connectedPort);
                    } else {
                        newRate = 0;
                    }

                    // If we've already set the rate, then check that the
                    // rate for any other internal port is correct.
                    if (foundOutputPort != null && newRate != inferredRate) {
                        throw new NotSchedulableException(
                                port,
                                "External output port "
                                        + port
                                        + " is connected on the inside to ports "
                                        + "with different initial production: "
                                        + foundOutputPort + " and "
                                        + connectedPort);
                    }

                    foundOutputPort = connectedPort;
                    inferredRate = newRate;
                }

                // If this output port has had its tokenInitConsumption
                // parameter set to something other than zero, the this
                // means that it will receive a token on the inside from
                // some port that does initial production, such as PublisherPort.
                // These initial tokens become initial _production_ for this
                // port.
                int initConsumption = DFUtilities.getTokenInitConsumption(port);
                inferredRate += initConsumption;

                DFUtilities.setIfNotDefined(port, "tokenInitProduction",
                        inferredRate);

                if (_debugging && VERBOSE) {
                    _debug("Setting tokenInitProduction to " + inferredRate);
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
        final CompositeActor container = (CompositeActor) director
                .getContainer();

        ChangeRequest request = new ChangeRequest(this,
                "Record firings per iteration") {
            @Override
            protected void _execute() throws KernelException {
                Iterator entities = entityToFiringsPerIteration.keySet()
                        .iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    int firingCount = ((Integer) entityToFiringsPerIteration
                            .get(entity)).intValue();
                    DFUtilities.setOrCreate(entity, "firingsPerIteration",
                            firingCount);

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
    ////                         protected variables               ////

    /** If true, then print verbose messages.  By default, this variable
     *  is set to false.  To enable verbose messages, edit the source file
     *  and recompile.
     */
    protected static final boolean VERBOSE = false;
}
