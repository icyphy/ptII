/* Director for the heterochronous dataflow model of computation.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;
// Don't import anything from vergil directly so that we avoid 
// compiling vergil when we compile this file
//import ptolemy.vergil.fsm.modal.ModalModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

////////////////////////////////////////////////////////////////////
//// HDFDirector
/**
The heterochronous dataflow (HDF) domain implements the HDF model
of computation [1]. The HDF model of computation is a generalization
of synchronous dataflow (SDF). In SDF, the set of port rates of an
actor (called the type signature) are constant. In HDF, however,
an actor has a finite number of type signatures which are allowed
to change between iterations of the HDF schedule.
<p>
An HDF actor has an initial type signature when execution begins.
The balance equations can then be solved to find a
periodic schedule, as in SDF. Unlike SDF, an HDF actor is allowed to
change its type signature after an iteration of the schedule.
If a port rate change occurs, a new schedule
corresponding to the new ports rates must then be obtained.
<p>
Since an HDF actor has a finite number of type signatures, it
may be useful to use an FSM to control when type signature changes
may occur. The HDFFSMDirector may be used to compose HDF with
 hierarchical FSMs according to the *charts [1] semantics.
<p>
Since an HDF actor has a finite number of possible type
signatures, the number of possible schedules is also finite.
As a result of this finite state space, deadlock and bounded
channel lengths are decidable in HDF. In principle, all possible
schedules could be computed at compile time. However, the number
of schedules can be exponential in the number of actors, so this
may not be practical.
<p>
This director makes use of an HDF scheduler that computes the
schedules dynamically, and caches them. The size of the cache
can be set by the <i>scheduleCacheSize</i> parameter. The default
value of this parameter is 100.
<p>
<b>References</b>
<p>
<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee,
``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>,'' April 13,
1998.</LI>
</ol>

@see HDFFSMDirector

@author Brian K. Vogel
@version $Id$
*/
public class HDFDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     */
    public HDFDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace for this object.
     */
    public HDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The HDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** A parameter representing the size of the schedule cache to
     *  use. If the value is less than
     *  or equal to zero, then schedules will never be discarded
     *  from the cache. The default value is 100.
     *  <p>
     *  Note that the number of schedules in an HDF model can be
     *  exponential in the number of actors. Setting the cache size to a
     *  very large value is therefore not recommended if the
     *  model contains a large number of HDF actors.
     */
    public Parameter scheduleCacheSize;

    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule. This
     *  method differes from the fire() method of SDFDirector in that
     *  this method uses cached schedules when possible. This leads to
     *  more efficient execution. The cache size to use is set by the
     *  scheduleCacheSize parameter.
     *  <p>
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's  prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    /*
      public void fire() throws IllegalActionException {
          if (_debug_info) {
              System.out.println(getName() + " : fire() invoked.");
          }
          TypedCompositeActor container = ((TypedCompositeActor)getContainer());

          if (container == null) {
              throw new InvalidStateException("HDFDirector " + getName() +
              " fired, but it has no container!");
          } else {
              if (_debug_info) {
                  System.out.println(getName() + " : fire(): " +
                  " Schedule is ");
                  Schedule tSched = getSchedule();
                  Iterator tFirings = tSched.firingIterator();
                  while (tFirings.hasNext()) {
                      Firing firing = (Firing)tFirings.next();
                      Actor actor = (Actor)firing.getActor();
                      System.out.println(" : " +
                          ((NamedObj)actor).getName() + " ");
                  }
              }

              Schedule sched = getSchedule();
              Iterator firings = sched.firingIterator();
              while (firings.hasNext()) {
                  Firing firing = (Firing)firings.next();
                  Actor actor = (Actor)firing.getActor();
                  if (_debug_info) {
                      System.out.println(getName() + " : fire(): " +
                      " firing actor : " +
                      ((NamedObj)actor).getName());
                  }
                  int iterationCount = firing.getIterationCount();

                  // FIXME: This is a hack. It does not even check if the
                  // SDF graph contains loops, and may be far from optimal when
                  // the SDF graph contains non-homogeneous actors. However,
                  // the default value of vectorizationFactor = 1,
                  // which should be completely safe for all models.
                  // TODO: I need to modify the scheduler to generate an
                  // optimum vectorized schedule. I.e., first try to
                  // obtain a single appearance schedule. Then, try
                  // to minimize the number of actor activations.
                  int factor =
                      ((IntToken) (vectorizationFactor.getToken())).intValue();
                  if (factor < 1) {
                      throw new IllegalActionException(this,
                          "The supplied vectorization factor is invalid " +
                          "Valid values consist of positive integers. " +
                          "The supplied value was: " + factor);
                  }
                  int returnVal =
                      actor.iterate(factor*iterationCount);
                  if (returnVal == COMPLETED) {
                      _postfirereturns = _postfirereturns && true;
                  } else if (returnVal == NOT_READY) {
                      throw new IllegalActionException(this,
                          (ComponentEntity) actor, "Actor " +
                          "is not ready to fire.");
                  } else if (returnVal == STOP_ITERATING) {
                      _postfirereturns = false;
                  }
              }
          }
      }
*/
    /** Return the scheduling sequence as an instance of Schedule.
     *  For efficiency, this method maintains a schedule cache and
     *  will attempt to return a cached version of the schedule.
     *  If the cache does not contain the schedule for the current
     *  hdf graph, then the schedule will be computed by calling
     *  the getSchedule() method of the SDFScheduler.
     *  <p>
     *  The schedule cache uses a least-recently-used replacement
     *  policy. The size of the cache is specified by the
     *  scheduleCacheSize parameter. The default cache size is
     *  100.
     *
     *  @return The Schedule for the current hdf graph.
     *
     *  @exception IllegalActionException If there is a problem getting
     *   the schedule.
     */
    // Called by getFiringCount(Actor) in HDFDirector
    public Schedule getSchedule() throws IllegalActionException{
        Scheduler scheduler =
            getScheduler();
        Schedule schedule;
        //return scheduler.getSchedule();
        if (isScheduleValid()) {
            // This will return a the current schedule.
            //System.out.println("called in HDF getSchedule return new scheduler");
            schedule = scheduler.getSchedule();
        } else {
            // The schedule is no longer valid, so check the schedule
            // cache.
            if (_inputPortList == null) {
                _inputPortList = _getInputPortList();
            }
            if (_outputPortList == null) {
                _outputPortList = _getOutputPortList();
            }
            Iterator inputPorts = _inputPortList.iterator();
            String rates = new String();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort)inputPorts.next();
                int rate =
                    SDFScheduler.getTokenConsumptionRate(inputPort);
                rates = rates + String.valueOf(rate);
            }
            Iterator outputPorts = _outputPortList.iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort)outputPorts.next();
                int rate =
                    SDFScheduler.getTokenProductionRate(outputPort);
                rates = rates + String.valueOf(rate);
            }
            if (_debug_info) {
                System.out.println("Port rates = " + rates);
            }
            String rateKey = rates;
            int cacheSize =
                ((IntToken)(scheduleCacheSize.getToken())).intValue();
            if (cacheSize != _cacheSize) {
                // cache size has changed. reset the cache.
                _scheduleCache = new HashMap();
                _scheduleKeyList = new ArrayList(cacheSize);
                _cacheSize = cacheSize;
            }
            if (_scheduleCache.containsKey(rateKey)) {
                // cache hit.
                if (_debug_info) {
                    System.out.println(getName() +
                            " : Cache hit!");
                }
                if (cacheSize > 0) {
                    // Remove the key from its old position in
                    // the list.
                    _scheduleKeyList.remove(rateKey);
                    // and add the key to head of list.
                    _scheduleKeyList.add(0, rateKey);
                }
                schedule = (Schedule)_scheduleCache.get(rateKey);
            } else {
                // cache miss.
                if (_debug_info) {
                    System.out.println(getName() +
                            " : Cache miss.");
                }
                if (cacheSize > 0) {
                    while (_scheduleKeyList.size() >= cacheSize) {
                        // cache is  full.
                        // remove tail of list.
                        Object object = _scheduleKeyList.get(cacheSize - 1);
                        _scheduleKeyList.remove(cacheSize - 1);
                        _scheduleCache.remove(object);
                    }
                    // Add key to head of list.
                    _scheduleKeyList.add(0, rateKey);
                }
                // Add key/schedule to the schedule map.
                schedule = scheduler.getSchedule();
                _scheduleCache.put(rateKey, schedule);
            }
        }
        return schedule;
    }
    
    /** Return the firing count of the specified actor in the schedule.
     *  The specified actor must be director contained by this director.
     *  Otherwise an exception will occur.
     *
     *  @param actor The actor to return the firing count for.
     *  @exception IllegalActionException If there is a problem computing
     *   the firing count.
     */
    // called by _getFiringsPerSchedulIteration in HDFFSMDirector.
    public int getFiringCount(Actor actor) throws IllegalActionException {
        Schedule schedule = getSchedule();
        Iterator firings = schedule.firingIterator();
        int occurrence = 0;
        while (firings.hasNext()) {
            Firing firing = (Firing)firings.next();
            Actor actorInSchedule = (Actor)(firing.getActor());
            String actorInScheduleName =
                ((Nameable)actorInSchedule).getName();
            String actorName = ((Nameable)actor).getName();
            if (actorInScheduleName.equals(actorName)) {
                // Current actor in the static schedule is
                // the HDF composite actor containing this FSM.
                // Increment the occurrence count of this actor.
                occurrence += firing.getIterationCount();
            }

            if (_debug_info) {
                //System.out.println(getName() +
                //" :  _getFiringsPerSchedulIteration(): Actor in static schedule: " +
                //           ((Nameable)actor).getName());
                //System.out.println(getName() +
                //" : _getFiringsPerSchedulIteration(): Actors in static schedule:" +
                //           occurrence);
            }
        }
        return occurrence;
    }
    
    /** Initialize the actors associated with this director, set the
     *  size of the schedule cache, and then compute the schedule.
     *  The schedule is computed during initialization so that
     *  hierarchical opaque composite actors can be scheduled
     *  properly (since the act of computing the schedule sets the
     *  rate parameters of the external ports). The order in which
     *  the actors are initialized is arbitrary.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler, or if the cache size parameter is not set to
     *  a valid value.
     */
    public void initialize() throws IllegalActionException {
        //System.out.println("HDF Director initialize");
        super.initialize();
        
        SDFScheduler scheduler = (SDFScheduler)getScheduler();
        int cacheSize =
            ((IntToken)(scheduleCacheSize.getToken())).intValue();
    }
    
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        CompositeActor container = (CompositeActor)getContainer();
        //LinkedList allActorList = new LinkedList();
        for (Iterator entities = container.entityList().iterator();
                    entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();

            // Instead of refering to ptolemy.vergil.fsm.modal.ModalModel
            // directly, we use reflection so as to avoid a compile
            // time circular loop

            //if (entity instanceof _modalModelClass.class) {
            if (entity.getClass().isAssignableFrom(_modalModelClass)) {
                //System.out.println("preinitialize():" + entity.getName() + 
                //    "is a ModalModel controller.");
                //Director director =((ModalModel)entity).getDirector();
                Director director =((CompositeActor)entity).getDirector();
                //System.out.println("director = " + director.getFullName());
                if (director instanceof HDFFSMDirector) {
                    //System.out.println(" the director is HDFFSM");
                    int firingsPerScheduleIteration =
                    ((HDFFSMDirector)director).updateFiringsPerScheduleIteration();
                    //System.out.println("firingsPerScheduleIteration = " +
                    //    firingsPerScheduleIteration);
                    ((HDFFSMDirector)director).setFiringsPerScheduleIteration(
                        firingsPerScheduleIteration);   
                }
            }
            //allActorList.addLast(entity);
        }       
    }
    
    public boolean postfire() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        //LinkedList allActorList = new LinkedList();
        for (Iterator entities = container.entityList().iterator();
                    entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            // Instead of refering to ptolemy.vergil.fsm.modal.ModalModel

            // directly, we use reflection so as to avoid a compile
            // time circular loop

            //if (entity instanceof _modalModelClass.class) {
            if (entity.getClass().isAssignableFrom(_modalModelClass)) {
                //System.out.println(entity.getName() + 
                //    "is a ModalModel controller.");
                //Director director =((ModalModel)entity).getDirector();
                Director director =((CompositeActor)entity).getDirector();
                //System.out.println("director = " + director.getFullName());
                if (director instanceof HDFFSMDirector) {
                    //System.out.println(" the director is HDFFSM");
                    int firingsPerScheduleIteration =
                    ((HDFFSMDirector)director).updateFiringsPerScheduleIteration();
                    //System.out.println("firingsPerScheduleIteration = " +
                    //    firingsPerScheduleIteration);
                    ((HDFFSMDirector)director).setFiringsPerScheduleIteration(
                        firingsPerScheduleIteration);     
                }
            }
            //allActorList.addLast(entity);
        }        
        //System.out.println("SDF postfire");
        //int iterationsValue = ((IntToken) (iterations.getToken())).intValue();
        //_iterationCount++;
        //if ((iterationsValue > 0) && (_iterationCount >= iterationsValue)) {
        //    _iterationCount = 0;
        //    return false;
        //}
        return super.postfire();
    }
/*
    public void preinitialize() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        //LinkedList allActorList = new LinkedList();
        for (Iterator entities = container.entityList().iterator();
                entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            if (entity instanceof ModalModel) {
                System.out.println(entity.getName() + 
                   "is a ModalModel controller.");
                Director director =((ModalModel)entity).getDirector();
                System.out.println("director = " + director.getName());
                if (director instanceof HDFFSMDirector) {
                    System.out.println(" the director is HDFFSM");
                    ((HDFFSMDirector)director).preinitialize();
                }
            }
            //allActorList.addLast(entity);
        }        
    }
  */
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object. In this case, we give the HDFDirector a
     *  default scheduler of the class HDFScheduler.
     */
    private void _init() {
        try {
            SDFScheduler scheduler =
                new SDFScheduler(this, uniqueName("Scheduler"));
            setScheduler(scheduler);
        }
        catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }
        try {
            int cacheSize = 100;
            _cacheSize = cacheSize;
            scheduleCacheSize
                = new Parameter(this,"scheduleCacheSize",new IntToken(cacheSize));

            _scheduleCache = new HashMap();
            _scheduleKeyList = new ArrayList(cacheSize);
        }
        catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    }

    /** Return a list of all the input ports contained by the
     *  deeply contained entities of the container of this director.
     *
     *  @return The list of input ports.
     */
    private List _getInputPortList() {
        CompositeActor container =  (CompositeActor)getContainer();
        List actors = container.deepEntityList();
        Iterator actorIterator = actors.iterator();
        List inputPortList = new LinkedList();;
        List inputPortRateList = new LinkedList();
        while (actorIterator.hasNext()) {
            Actor containedActor = (Actor)actorIterator.next();
            List temporaryInputPortList =
                containedActor.inputPortList();
            Iterator inputPortIterator =
                temporaryInputPortList.iterator();
            while (inputPortIterator.hasNext()) {
                IOPort inputPort = (IOPort)inputPortIterator.next();
                if (_debug_info) {
                    System.out.println(getName() +
                            "Found input port : " +
                            inputPort.getName());
                }
                inputPortList.add(inputPort);
            }
        }
        return inputPortList;
    }

    /** Return a list of all the output ports contained by the
     *  deeply contained entities of the container of this director.
     *
     *  @return The list of output ports.
     */
    private List _getOutputPortList() {
        CompositeActor container =  (CompositeActor)getContainer();
        List actors = container.deepEntityList();
        Iterator actorIterator2 = actors.iterator();
        List outputPortList = new LinkedList();;
        List outputPortRateList = new LinkedList();
        while (actorIterator2.hasNext()) {
            Actor containedActor = (Actor)actorIterator2.next();
            List temporaryOutputPortList =
                containedActor.outputPortList();
            Iterator outputPortIterator =
                temporaryOutputPortList.iterator();
            while (outputPortIterator.hasNext()) {
                IOPort outputPort = (IOPort)outputPortIterator.next();
                if (_debug_info) {
                    System.out.println(getName() +
                            "Found output port : " +
                            outputPort.getName());
                }
                outputPortList.add(outputPort);
            }
        }
        return outputPortList;
    }

    // Instead of refering to ptolemy.vergil.fsm.modal.ModalModel
    // directly, we use reflection so as to avoid a compile
    // time circular loop.

    private static Class _modalModelClass;
    static {
        try {
            _modalModelClass =
                Class.forName("ptolemy.vergil.fsm.modal.ModalModel");
        } catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }
    }


    // The hashmap for the schedule cache.
    private Map _scheduleCache;
    private List _scheduleKeyList;
    private List _inputPortList;
    private List _outputPortList;
    private int _cacheSize = 100;

    // Set to true to enable debugging.
    //private boolean _debug_info = true;
    private boolean _debug_info = false;
}
