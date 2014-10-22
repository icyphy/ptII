/* Director for the distributed version of the synchronous dataflow
 model of computation.

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.distributed.domains.sdf.kernel;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.jini.core.lookup.ServiceItem;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.distributed.client.ClientServerInteractionManager;
import ptolemy.distributed.client.ClientThread;
import ptolemy.distributed.client.ThreadSynchronizer;
import ptolemy.distributed.common.DistributedActor;
import ptolemy.distributed.util.DistributedUtilities;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////DistributedSDFDirector

/**
 Director for the distributed version of the synchronous dataflow (SDF)
 model of computation.
 <h1>Distributed-SDF overview</h1>
 The Distributed-SDF domain is an extended version of the existing SDF
 Domain that performs the simulation in a distributed manner.
 <h2>Requirements</h2>
 <ul>
 <li>A distributed platform is required to perform the simulation.
 <li>Every node in the distributed platform has to run a server
 application (DistributedServerRMIGeneric).
 <li>A Jini service locator must be running on one of the nodes (peer
 discovery).
 </ul>
 <h2>Features</h2>
 <ul>
 <li>Exploits the degree of parallelism that many models expose in
 their topology.
 <li>The distributed execution is transparent to the user.
 <li>Keeps the existing software architecture untouched, only extending
 it. (Except some minor modifications in the SDFScheduler).
 <li>It achieves smaller simulation times for models with certain degree
 of parallelism, specially those were:
 cost(computation) >>>> cost(communication).
 <li>Allows for bigger models (in terms of memory consumption).
 </ul>
 <p>
 A DistributedSDFDirector is the class that controls execution of actors
 under the distributed version of the SDF domain. It extends SDFDirector.
 <p>
 By default, actor scheduling is handled by the DistributedSDFScheduler
 class. Furthermore, the newReceiver method creates Receivers of type
 DistributedSDFReceiver, which extends SDFReceiver.
 <p>
 See ptolemy.domains.sdf.kernel.SDFScheduler for more information about
 the SDF Domain.
 <p>
 <h2>Parameters</h2>
 <ul>
 <li>The <i>parallelSchedule</i> parameter of this director determines
 whether a sequential or parallel schedule will be performed.
 The default value of the parallelSchedule parameter is a
 BooleanToken with the value false.
 <li>The <i>parallelExecution</i> parameter of this director determines
 whether a sequential or parallel execution will be performed.
 The default value of the parallelExecution parameter is a
 BooleanToken with the value false.
 </ul>
 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.distributed.rmi.DistributedServerRMIGeneric
 @see ptolemy.distributed.domains.sdf.kernel.DistributedSDFReceiver
 @see ptolemy.distributed.domains.sdf.kernel.DistributedSDFScheduler
 @see ptolemy.domains.sdf.kernel.SDFDirector
 @see ptolemy.domains.sdf.kernel.SDFScheduler
 */
public class DistributedSDFDirector extends SDFDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The DistributedSDFDirector will have a default scheduler of type
     *  DistributedSDFScheduler.
     *
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DistributedSDFDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The DistributedSDFDirector will have a default scheduler of type
     *  DistributedSDFScheduler.
     *
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DistributedSDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The DistributedSDFDirector will have a default scheduler of type
     *  DistributedSDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public DistributedSDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A Parameter representing whether a sequential or parallel schedule
     *  will be computed.
     *  This parameter must be a boolean.
     *  The default value is false BooleanToken.
     */
    public Parameter parallelSchedule;

    /** A Parameter representing whether a pipelined parallel execution
     *  will be performed. A pipelined execution only makes sense when
     *  parallel execution is true.
     *  This parameter must be a boolean.
     *  The default value is false BooleanToken.
     */
    public Parameter pipelining;

    /** A Parameter representing whether a sequential or parallel execution
     *  will be performed.
     *  This parameter must be a boolean.
     *  The default value is false BooleanToken.
     */
    public Parameter parallelExecution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  If the attribute that changes is <i>parallelSchedule</i> the schedule
     *  is invalidated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == parallelSchedule) {
            invalidateSchedule();
        }

        if (attribute == parallelExecution) {
            invalidateSchedule();

            if ((BooleanToken) parallelExecution.getToken() == BooleanToken.FALSE) {
                System.out.println("equals FALSE");
                pipelining.setToken(BooleanToken.FALSE);
            }
        }

        super.attributeChanged(attribute);
    }

    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule. Depending
     *  on the value of <i>parallelSchedule</i> either a sequential or
     *  a parallel schedule will be computed. Depending on the value of
     *  <i>parallelExecution</i> either a sequential or a parallel
     *  execution of the schedule will be computed.
     *  No internal state of the director is updated during fire, so it
     *  may be used with domains that require this property, such as
     *  CT. <p>
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration. <p>
     *
     *  This method may be overridden by some domains to perform additional
     *  domain-specific operations.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.

     */
    @Override
    public void fire() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("> Director: fire");
        }

        boolean parallelExecutionValue = ((BooleanToken) parallelExecution
                .getToken()).booleanValue();
        boolean pipeliningValue = ((BooleanToken) pipelining.getToken())
                .booleanValue();

        if (VERBOSE) {
            System.out.println("parallelExecution: " + parallelExecutionValue);
            System.out.println("pipelining: " + pipelining);
        }

        if (!parallelExecutionValue) {
            super.fire();
        } else {
            if (pipeliningValue) {
                pipelinedParallelFire();
            } else {
                parallelFire();
            }
        }
    }

    /** Initialize the actors associated with this director (super).
     *  If <i>parallelExecution</i> is true, the infrastructure for a
     *  distributed execution is initialized. Once the required number
     *  of services are discovered, the actors are mapped on to them,
     *  sent to the distributed services and virtually connected over
     *  the network.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("> DistributedSDFDirector: initialize");
        }

        super.initialize();

        boolean parallelExecutionValue = ((BooleanToken) parallelExecution
                .getToken()).booleanValue();

        if (parallelExecutionValue) {
            initializeJini();
            mapActorsOntoServers();
            distributeActorsOntoServers();
            connectActors();
        }

        if (((BooleanToken) pipelining.getToken()).booleanValue()) {
            bufferingPhase();
        }
    }

    /** Return a new receiver consistent with the Distributed-SDF domain.
     *  @return A new DistributedSDFReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new DistributedSDFReceiver();
    }

    /** Preinitialize the actors associated with this director and
     *  compute the schedule (super).
     *  The schedule is computed during preinitialization so that hierarchical
     *  opaque composite actors can be scheduled properly, since the
     *  act of computing the schedule sets the rate parameters of the external
     *  ports.
     *  In addition, performing scheduling during preinitialization
     *  enables it to be present during code generation.  The order in
     *  which the actors are preinitialized is arbitrary.
     *  The schedule computed will be either sequential or parallel depending
     *  on the value of <i>parallelSchedule</i>.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("> DistributedSDFDirector: preinitialize");
        }

        super.preinitialize();

        if (VERBOSE) {
            System.out.println("parallelSchedule: "
                    + ((BooleanToken) parallelSchedule.getToken())
                            .booleanValue());
        }

        //System.out.println(getScheduler().getSchedule().toString());
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container (super).
     *  In case of <i>parallelExecution</i> being true, exit from all the
     *  client threads is performed.
     *
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @see ptolemy.distributed.client.ClientThread
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (VERBOSE) {
            System.out.println(">Director: wrapup");
        }

        boolean parallelExecutionValue = ((BooleanToken) parallelExecution
                .getToken()).booleanValue();

        if (parallelExecutionValue) {
            exitClientThreads();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Fills the queues with data tokens so that a fully parallel execution
     *  can be performed. It performs firings of the different levels of the
     *  schedule, adding one more level in every round. For example for a
     *  parallel schedule consisting of three levels, first if fires the
     *  actors in level 1, followed by actors in levels 1 and 2.
     *
     *  @exception IllegalActionException If there is no scheduler.
     */
    private void bufferingPhase() throws IllegalActionException {
        System.out.println("Buffering...");

        int iterationsValue = ((IntToken) iterations.getToken()).intValue();

        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();
        Iterator levels = schedule.iterator();

        int levelNumber = 0;

        commandsMap = new HashMap();

        while (levels.hasNext() && !_stopRequested) {
            ScheduleElement level = (Schedule) levels.next();

            Iterator firings = level.firingIterator();

            while (firings.hasNext()) {
                Firing firing = (Firing) firings.next();
                Actor actor = firing.getActor();
                ClientThread clientThread = (ClientThread) actorsThreadsMap
                        .get(actor);
                clientThread.setIterationCount(firing.getIterationCount());
                commandsMap.put(clientThread,
                        Integer.valueOf(ClientThread.ITERATE));
            }

            int aux = levelNumber - iterationsValue;

            if (aux >= 0) {
                firings = schedule.get(aux).firingIterator();

                while (firings.hasNext()) {
                    Firing firing = (Firing) firings.next();
                    Actor actor = firing.getActor();

                    System.out.println("removing: " + actor.getFullName());
                    ClientThread clientThread = (ClientThread) actorsThreadsMap
                            .get(actor);
                    clientThread.setIterationCount(firing.getIterationCount());
                    commandsMap.remove(clientThread);
                }
            }

            levelNumber = levelNumber + 1;

            if (levels.hasNext()) {
                synchronizer.setCommands(commandsMap);

                // Here is where the synchronization takes place.
                synchronizer.commandsProcessed();
            }
        }

        System.out.println("Finished Buffering...");
    }

    /** Interconnect all the remote actors in the same manner as the
     *  model's topology. In other words, the connections defined by the
     *  model's topology are created virtually over the distributed
     *  platform.
     *  For each actor, a portReceiverMap is created.
     *  A portReceiverMap is a data structure representing for a given
     *  port the receivers it contains.
     *  In case the port is and input port it consists of a set of receivers
     *  ID's i.e. (inputport, (ID1, ..., IDn).
     *  In case of an outputport, it contains a map of services to receiver's
     *  IDs, i.e. (outputport, ((service1, (ID1, ..., IDi), ...,
     *  (servicen, (IDj, ..., IDr))).
     *  This structure is sent over the network to the corresponding service.
     *  The types of the port are also set on the remote actor.
     *
     *  @exception IllegalActionException If the remote receivers can't be created.
     */
    private void connectActors() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("Connecting Actors");
            System.out.println(">> Creating Ports Receivers Map: ");
        }

        for (Iterator keysIterator = actorsThreadsMap.keySet().iterator(); keysIterator
                .hasNext();) {
            ComponentEntity actor = (ComponentEntity) keysIterator.next();

            HashMap portsReceiversMap = new HashMap();
            HashMap portTypes = new HashMap();

            Iterator allPorts = actor.portList().iterator();

            while (allPorts.hasNext()) {
                IOPort currentPort = (IOPort) allPorts.next();
                Receiver[][] receivers = new Receiver[0][0];

                if (currentPort.isOutput()) {
                    receivers = currentPort.getRemoteReceivers();
                }

                if (currentPort.isInput()) {
                    receivers = currentPort.getReceivers();
                }

                if (!currentPort.connectedPortList().isEmpty()) {
                    portTypes.put(currentPort.getName(),
                            ((TypedIOPort) currentPort).getType());
                }

                if (receivers.length > 0) {
                    if (VERBOSE) {
                        System.out.print("Port: "
                                + currentPort.getFullName()
                                + "\n"
                                + DistributedUtilities
                                        .receiversArrayToString(receivers));
                    }

                    if (currentPort.isOutput()) {
                        portsReceiversMap.put(currentPort.getName(),
                                createServicesReceiversMap(receivers));
                    }

                    if (currentPort.isInput()) {
                        portsReceiversMap.put(currentPort.getName(),
                                DistributedUtilities
                                        .convertReceiversToIntegers(receivers));
                    }
                }
            }

            ServiceItem server = ((ClientThread) actorsThreadsMap.get(actor))
                    .getService();
            DistributedActor distributedActor = (DistributedActor) server.service;

            try {
                if (VERBOSE) {
                    System.out.println("Setting connections to: "
                            + actor.getFullName() + " in: "
                            + server.serviceID.toString());
                    System.out.println("Setting port Types: "
                            + actor.getFullName() + " in: "
                            + server.serviceID.toString());
                }

                distributedActor.setConnections(portsReceiversMap);
                distributedActor.setPortTypes(portTypes);
            } catch (RemoteException e) {
                KernelException.stackTraceToString(e);
            }
        }
    }

    /** Create a map containing the services and Receivers ID's corresponding
     *  to a given bidimensional array of Receiver.
     *  i.e. ((service1, (ID1, ..., IDi), ..., (servicen, (IDj, ..., IDr)).
     *
     *  @param receivers The bidimensional array of Receivers.
     *  @return A HashMap containing services and lists of Receiver IDs.
     */
    private HashMap createServicesReceiversMap(Receiver[][] receivers) {
        HashMap servicesReceiversMap = new HashMap();

        for (Receiver[] receiver : receivers) {
            for (int j = 0; j < receiver.length; j++) {
                if (receiver[j] != null) {
                    IOPort port = receiver[j].getContainer();
                    Actor actor = (Actor) port.getContainer();

                    if (!servicesReceiversMap
                            .containsKey(((ClientThread) actorsThreadsMap
                                    .get(actor)).getService())) {
                        servicesReceiversMap.put(
                                ((ClientThread) actorsThreadsMap.get(actor))
                                        .getService(), new LinkedList());
                    }

                    LinkedList list = (LinkedList) servicesReceiversMap
                            .get(((ClientThread) actorsThreadsMap.get(actor))
                                    .getService());
                    Integer ID = ((DistributedSDFReceiver) receiver[j]).getID();
                    list.add(ID);
                }
            }
        }

        return servicesReceiversMap;
    }

    /** Distribute the actors to the corresponding Service specified in the
     *  actorsThreadsMap and initialize them remotely.
     */
    private void distributeActorsOntoServers() {
        if (VERBOSE) {
            System.out.println("Distributing Actors Onto Servers");
        }

        for (Iterator keysIterator = actorsThreadsMap.keySet().iterator(); keysIterator
                .hasNext();) {
            ComponentEntity actor = (ComponentEntity) keysIterator.next();
            ServiceItem server = ((ClientThread) actorsThreadsMap.get(actor))
                    .getService();

            DistributedActor distributedActor = (DistributedActor) server.service;

            try {
                if (VERBOSE) {
                    System.out.println("Loading class: "
                            + actor.getClass().getName() + " in: "
                            + server.serviceID.toString());
                }

                distributedActor.loadMoML(actor.exportMoML());

                // Is this needed?
                distributedActor.initialize();
            } catch (RemoteException e) {
                KernelException.stackTraceToString(e);
            }
        }
    }

    /** Create a commandMap containing and EXIT command for all the existing
     *  clientThreads and issue it to the synchronizer. This will results in
     *  all the clientThreads to terminate.
     *
     *  @see ptolemy.distributed.client.ThreadSynchronizer
     *  @see ptolemy.distributed.client.ClientThread
     */
    private void exitClientThreads() {
        HashMap commands = new HashMap();

        for (Iterator actorsIterator = actorsThreadsMap.keySet().iterator(); actorsIterator
                .hasNext();) {
            Object auxActor = actorsIterator.next();
            commands.put(actorsThreadsMap.get(auxActor),
                    Integer.valueOf(ClientThread.EXIT));
        }

        synchronizer.setCommands(commands);

        // For synchronization, we wait for all the threads to finish.
        synchronizer.commandsProcessed();
    }

    /** Create a LinkedList containing all the instances of Actor contained by
     *  the CompositeActor in which this director is embedded.
     *
     *  @return A LinkedList containing all the instances of Actor contained by
     *  the CompositeActor in which this director is embedded.
     */
    private LinkedList getActors() {
        if (VERBOSE) {
            System.out.println("Getting actors");
        }

        CompositeActor container = (CompositeActor) getContainer();

        // A linked list containing all the actors.
        LinkedList allActorList = new LinkedList();

        // Populate it.
        for (Iterator entities = container.deepEntityList().iterator(); entities
                .hasNext();) {
            ComponentEntity entity = (ComponentEntity) entities.next();

            // Fill allActorList with the list of things that we can schedule
            if (entity instanceof Actor) {
                allActorList.addLast(entity);
            }
        }

        return allActorList;
    }

    /** Get the Jini Services discovered.
     *
     *  @see ptolemy.distributed.client.ClientServerInteractionManager
     *  @return A LinkedList containing Jini Services discovered.
     */
    private LinkedList getServers() {
        if (VERBOSE) {
            System.out.println("Getting servers");
        }

        return clientServerInteractionManager.getServices();
    }

    /** Initialize the object. In this case, we give the
     *  DistributedSDFDirector a default scheduler of the class
     *  DistributedSDFScheduler, a parallelSchedule parameter, a
     *  pipelining parameter and parallelExecution parameter.
     */
    private void init() throws IllegalActionException, NameDuplicationException {
        /*DistributedSDFScheduler scheduler = */new DistributedSDFScheduler(
                this, uniqueName("Scheduler"));

        // We create the new parameter here.
        parallelSchedule = new Parameter(this, "parallelSchedule",
                new BooleanToken(true));
        parallelSchedule.setTypeEquals(BaseType.BOOLEAN);

        pipelining = new Parameter(this, "pipelining", new BooleanToken(true));
        pipelining.setTypeEquals(BaseType.BOOLEAN);

        parallelExecution = new Parameter(this, "parallelExecution",
                new BooleanToken(true));
        parallelExecution.setTypeEquals(BaseType.BOOLEAN);
    }

    /** Initializes Jini. It creates an instance of
     *  ClientServerInteractionManager to ease the discovery.
     *  Set the required number of services to run the simulation, being the
     *  number of Actors in the model.
     *  This includes:
     *  <menu>
     *  <li> Prepare for discovery
     *  <li> Discover a Service Locator
     *  <li> Looking up Services
     *  <li> Filtering Services
     *  </menu>
     *
     *  @see ptolemy.distributed.client.ClientServerInteractionManager
     *  @exception IllegalActionException If Jini cannot be initialized.
     */
    private void initializeJini() throws IllegalActionException {
        try {
            if (VERBOSE) {
                System.out.println("Initializing Jini");
            }

            clientServerInteractionManager = new ClientServerInteractionManager(
                    VERBOSE);

            // A linked list containing all the actors.
            LinkedList allActorList = new LinkedList();

            // Container of the Director (Composite Actor that holds
            // the model).
            CompositeEntity container = (CompositeEntity) getContainer();

            // Populate it.
            for (Iterator entities = container.deepEntityList().iterator(); entities
                    .hasNext();) {
                ComponentEntity entity = (ComponentEntity) entities.next();

                // Fill allActorList with the list of things that we
                // can schedule.
                // FIXME: What if other things can be scheduled than actors?
                if (entity instanceof Actor) {
                    allActorList.addLast(entity);
                }
            }

            if (VERBOSE) {
                System.out.println("Required services: " + allActorList.size());
            }

            clientServerInteractionManager.setRequiredServices(allActorList
                    .size());

            clientServerInteractionManager.init(StringUtilities
                    .getProperty("ptolemy.ptII.dir") + configFileName);
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to initialize Jini");
        }
    }

    /** Map the actors on to the Services. The mapping is trivial in the sense
     *  that no effort is made to optimize the mapping. ALL the actors are
     *  distributed to a Service not taking into account any QoS.
     *  One might want to create smarter ways to do the mapping... this is the
     *  place to do so.
     *  Create the client threads. For every actor that is distributed (ALL) a
     *  local clientThread is created to allow for parallel execution.
     *
     *  @see ptolemy.distributed.client.ClientThread
     */
    private void mapActorsOntoServers() {
        if (VERBOSE) {
            System.out.println("Mapping Actors Onto Servers");
        }

        LinkedList actors = getActors();
        LinkedList servers = getServers();

        if (actors.size() <= servers.size()) {
            Iterator serversIterator = servers.iterator();

            for (Iterator actorsIterator = actors.iterator(); actorsIterator
                    .hasNext();) {
                Object auxActor = actorsIterator.next();
                Object auxServer = serversIterator.next();
                ClientThread auxClientThread = new ClientThread(synchronizer,
                        (ServiceItem) auxServer);
                actorsThreadsMap.put(auxActor, auxClientThread);
                auxClientThread.start();
            }
        } else {
            System.out.println("Not enough servers");
        }

        if (true) {
            printActorsOntoServersMap();
        }
    }

    /** Perform the dispatching of the schedule in parallel to the distributed
     *  platform.
     *  For each level of the Schedule, a commandMap is created and issued to
     *  the synchronizer.
     *  //TODO: This can be made real static, precalculate and issue might
     *  yield slight better results? Is it worth the effort?
     *
     *  @see ptolemy.distributed.client.ThreadSynchronizer
     *  @exception IllegalActionException If port methods throw it.
     */
    private void parallelFire() throws IllegalActionException {
        //        System.out.println("ParallelFire");
        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();
        Iterator levels = schedule.iterator();

        while (levels.hasNext() && !_stopRequested) {
            Schedule level = (Schedule) levels.next();
            Iterator firings = level.firingIterator();

            HashMap commandsMap = new HashMap();

            while (firings.hasNext()) {
                Firing firing = (Firing) firings.next();
                Actor actor = firing.getActor();
                ClientThread clientThread = (ClientThread) actorsThreadsMap
                        .get(actor);
                clientThread.setIterationCount(firing.getIterationCount());
                commandsMap.put(clientThread,
                        Integer.valueOf(ClientThread.ITERATE));
            }

            synchronizer.setCommands(commandsMap);

            // Here is where the synchronization takes place.
            synchronizer.commandsProcessed();
        }
    }

    /** Perform the dispatching of the schedule in a pipelined parallel
     *  manner on to the distributed platform.
     *  For each level of the Schedule, a commandMap is created and issued to
     *  the synchronizer.
     *
     *  @exception IllegalActionException If there is no scheduler.
     *  @see ptolemy.distributed.client.ThreadSynchronizer
     *  @exception IllegalActionException If port methods throw it.
     */
    private void pipelinedParallelFire() throws IllegalActionException {
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();

        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();

        //        System.out.println("Schedule size:" + schedule.size());
        int aux = iterationsValue - _iterationCount;

        if (aux < schedule.size()) {
            Iterator firings = schedule.get(schedule.size() - aux - 1)
                    .firingIterator();

            while (firings.hasNext()) {
                Firing firing = (Firing) firings.next();
                Actor actor = firing.getActor();

                //                System.out.println("removing: " + actor.getFullName());
                ClientThread clientThread = (ClientThread) actorsThreadsMap
                        .get(actor);
                clientThread.setIterationCount(firing.getIterationCount());
                commandsMap.remove(clientThread);
            }
        }

        synchronizer.setCommands(commandsMap);

        // Here is where the synchronization takes place.
        synchronizer.commandsProcessed();
    }

    /** Print the actors-services mapping.
     */
    private void printActorsOntoServersMap() {
        System.out.println("Actors-Servers Map:");

        for (Iterator keysIterator = actorsThreadsMap.keySet().iterator(); keysIterator
                .hasNext();) {
            ComponentEntity actor = (ComponentEntity) keysIterator.next();
            ServiceItem server = ((ClientThread) actorsThreadsMap.get(actor))
                    .getService();
            System.out.println(server.serviceID.toString() + "\t <--- "
                    + actor.getFullName());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** It states whether debugging messages should be printed. */
    private boolean VERBOSE = false;

    /** The name of the Jini configuration file to be provided to the
     *  ClientServerInteractionManager.  The name should be relative
     *  to $PTII and start with a "/".
     */
    private String configFileName = "\\ptolemy\\distributed\\config\\"
            + "ClientServerInteractionManager.config";

    /** Encapsulates Jini functionality. */
    private ClientServerInteractionManager clientServerInteractionManager = null;

    /** Map of Actors to Threads (that contain the Service). */
    private HashMap actorsThreadsMap = new HashMap();

    /** Performs synchronization of the ClientThreads and used to issue
     *  commandMaps.
     */
    private ThreadSynchronizer synchronizer = new ThreadSynchronizer();

    /** Map of commands to be executed. This is used by the pipelined parallel
     *  execution for efficiency. Since new levels are added every time to the
     *  commands map, after buffering, a sequence of a fully parallel
     *  commandsMap is used several times. There is no point in recalculating
     *  it again and it is reused from the buffering phase.
     */
    HashMap commandsMap = new HashMap();
}
