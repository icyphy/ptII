/* A scheduler for multidimensional dataflow.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

package ptolemy.domains.pthales.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DFUtilities;
import ptolemy.domains.pthales.lib.PthalesAtomicActor;
import ptolemy.domains.pthales.lib.PthalesIOPort;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * The scheduler for the Pthales model of computation.
 *
 * @author R&eacute;mi Barr&egrave;re
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PthalesScheduler extends SDFScheduler {

    // FIXME: To do:
    // The tokenInitProduction parameter is not being used. ArrayOL equivalent?

    /** Construct a scheduler in the given container with the given name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PthalesScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Declare the rate dependency on any external ports of the model.
     *  SDF directors should invoke this method once during preinitialize.
     *  @exception IllegalActionException
     */
    @Override
    public void declareRateDependency() throws IllegalActionException {
        ConstVariableModelAnalysis analysis = ConstVariableModelAnalysis
                .getAnalysis(this);
        SDFDirector director = (SDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        for (Iterator ports = model.portList().iterator(); ports.hasNext();) {
            IOPort port = (IOPort) ports.next();

            if (!(port instanceof ParameterPort)) {
                if (port.isInput()) {
                    DFUtilities.setTokenConsumptionRate(
                            port,
                            PthalesIOPort.getArraySize(port)
                                    * PthalesIOPort.getNbTokenPerData(port));
                    _declareDependency(analysis, port, "tokenConsumptionRate",
                            _rateVariables);
                }

                if (port.isOutput()) {

                    int size = 0;

                    List<Actor> actors = model.deepEntityList();

                    // External ports
                    List<TypedIOPort> externalPorts = model.outputPortList();
                    for (TypedIOPort externalPort : externalPorts) {
                        // Dispatch to all input ports using output port
                        for (Actor actor : actors) {
                            List<IOPort> outputPorts = actor.outputPortList();
                            for (IOPort outputPort : outputPorts) {
                                if (outputPort.connectedPortList().contains(
                                        externalPort)) {
                                    size = PthalesIOPort
                                            .getArraySize(outputPort)
                                            * PthalesIOPort
                                                    .getNbTokenPerData(outputPort);
                                }
                            }
                        }
                    }

                    DFUtilities.setTokenProductionRate(port, size);
                    DFUtilities.setTokenInitProduction(port, 0);

                    _declareDependency(analysis, port, "tokenInitProduction",
                            _rateVariables);
                    _declareDependency(analysis, port, "tokenProductionRate",
                            _rateVariables);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * @exception IllegalActionException
     * @exception NotSchedulableException
     */
    @Override
    protected Schedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        // Context of this scheduler.
        PthalesDirector director = (PthalesDirector) getContainer();
        CompositeActor compositeActor = (CompositeActor) director
                .getContainer();
        List<Actor> actors = compositeActor.deepEntityList();

        CompositeActor model = (CompositeActor) director.getContainer();
        _checkDynamicRateVariables(model, _rateVariables);

        List<IOPort> inPorts = model.inputPortList();
        for (IOPort port : inPorts) {

            // FIXME: The following method looks for a stride as well,
            // which does not make sense for a tiling spec.

            // Now we need to set capacities of each of the receivers.
            // Notify the destination receivers of the write pattern.

            Receiver[][] receivers = port.deepGetReceivers();
            if (receivers != null && receivers.length > 0) {
                for (Receiver[] receiverss : receivers) {
                    if (receiverss != null && receiverss.length > 0) {
                        for (Receiver receiver : receiverss) {
                            //FIXME: Should we do this?
                            if (receiver instanceof PthalesReceiver) {
                                ((PthalesReceiver) receiver).setOutputArray(
                                        port, model);
                            }
                        }
                    }
                }
            }
        }

        //FIXME Remove this part?
        // Iterate over the actors.
        for (Actor actor : actors) {

            // Next do the output ports.
            List<IOPort> ports = actor.outputPortList();
            for (IOPort port : ports) {

                // FIXME: The following method looks for a stride as well,
                // which does not make sense for a tiling spec.

                // Now we need to set capacities of each of the receivers.
                // Notify the destination receivers of the write pattern.

                Receiver[][] receivers = port.getRemoteReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                //FIXME: Should we do this?
                                if (receiver instanceof PthalesReceiver) {
                                    ((PthalesReceiver) receiver)
                                            .setOutputArray(port, actor);
                                }
                            }
                        }
                    }
                }
            }
            // FIXME: Need to do the input ports of the container of
            // this director, treating them as output ports, in order
            // to support hierarchical nestings of this director
            // inside other models.

            // Next do the input ports.
            ports = actor.inputPortList();
            for (IOPort port : ports) {

                // Notify the receivers of the read pattern.  This
                // will have the side effect of setting the capacity
                // of the receivers.

                Receiver[][] receivers = port.getReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                // FIXME: Is the cast to LinkedHashSet
                                // safe?  Depends on the Java
                                // implementation of LinkedHashMap.
                                if (receiver instanceof PthalesReceiver) {
                                    ((PthalesReceiver) receiver).setInputArray(
                                            port, actor);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Populate the schedule with a subclass of Firing
        // that keeps track of the dimensions for the firing.
        // FIXME: Brute force technique here assumes an acyclic graph.
        // It executes all firings of upstream actors before any firing
        // of a downstream actor.
        Schedule schedule = new Schedule();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) compositeActor
                .getCausalityInterface();
        List<Actor> sortedActors = causality.topologicalSort();
        for (Actor actor : sortedActors) {
            Firing firing = new Firing(actor);

            // Iteration is only done on external loops
            firing.setIterationCount(PthalesAtomicActor
                    .getIteration((ComponentEntity) actor));

            schedule.add(firing);
        }
        return schedule;
    }
}
