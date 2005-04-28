/* Director for the heterochronous dataflow model of computation.

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


////////////////////////////////////////////////////////////////////
//// HDFDirector

/**
   The Heterochronous Dataflow (HDF) domain is an extension of the
   Synchronous Dataflow (SDF) domain and implements the HDF model of
   computation [1]. In SDF, the set of port rates (called rate signatures)
   of an actor are constant. In HDF, however, rate signatures are allowed
   to change between iterations of the HDF schedule.
   <p>
   This director is often used with HDFFSMDirector. The HDFFSMDirector
   governs the execution of a modal model. The change of rate signatures can
   be modeled by state transitions of the modal model, in which each state
   refinement infers a set of rate signatures. Within each state, the HDF
   model behaves like an SDF model.
   <p>
   This director recomputes the schedules dynamically. Schedules are
   cached and labeled by their corresponding rate signatures, with the most
   recently used at the end of the queue. When a state is revisited,
   the schedule identified by its rate signatures in the cache is used.
   Therefore, we do not need to recompute the schedule.
   The size of the cache can be set by the <i>scheduleCacheSize</i>
   parameter. The default value of this parameter is 100. If the cache
   is full, the least recently used schedule (at the beginning of the
   cache) is discarded.
   <p>
   <b>References</b>
   <p>
   <OL>
   <LI>
   A. Girault, B. Lee, and E. A. Lee,
   ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">
   Hierarchical Finite State Machines with Multiple Concurrency Models</A>,
   '' April 13, 1998.</LI>
   </ol>

   @see HDFFSMDirector

   @author Ye Zhou and Brian K. Vogel
   @version $Id$
   @since Ptolemy II 5.0
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (cxh)
*/
public class HDFDirector extends SDFDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public HDFDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
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
     *  SDFScheduler.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public HDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                           parameters                           ////

    /** A parameter representing the size of the schedule cache to use.
     *  If the value is less than or equal to zero, then schedules
     *  will never be discarded from the cache. The default value is 100.
     *  <p>
     *  Note that the number of schedules in an HDF model can be
     *  exponential in the number of actors. Setting the cache size to a
     *  very large value is therefore not recommended if the
     *  model contains a large number of HDF actors.
     */
    public Parameter scheduleCacheSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the scheduling sequence as an instance of Schedule.
     *  For efficiency, this method maintains a schedule cache and
     *  will attempt to return a cached version of the schedule.
     *  If the cache does not contain the schedule for the current
     *  HDF graph, then the schedule will be computed by calling
     *  the getSchedule() method of the SDFScheduler.
     *  <p>
     *  The schedule cache uses a least-recently-used replacement policy,
     *  which means if the cache is full, the least-recently-used schedule
     *  will be discarded. The size of the cache is specified by the
     *  scheduleCacheSize parameter. The default cache size is 100.
     *  @return The Schedule for the current HDF graph.
     *  @exception IllegalActionException If there is a problem getting
     *   the schedule.
     */
    public Schedule getSchedule() throws IllegalActionException {
        Scheduler scheduler = getScheduler();
        Schedule schedule;

        if (isScheduleValid()) {
            // This will return a the current schedule.
            //System.out.println(getName() + " The schedule is valid");
            schedule = ((SDFScheduler) scheduler).getSchedule();
        } else {
            //System.out.println(getName() + " The schedule is invalid.");
            // The schedule is no longer valid, so check the schedule
            // cache.
            if (_inputPortList == null
                    || _workspaceVersion != workspace().getVersion()) {
                _inputPortList = _getInputPortList();
            }

            if (_outputPortList == null
                    || _workspaceVersion != workspace().getVersion()) {
                _outputPortList = _getOutputPortList();
            }
            _workspaceVersion = workspace().getVersion();

            Iterator inputPorts = _inputPortList.iterator();
            StringBuffer rates = new StringBuffer();

            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                int rate = DFUtilities.getTokenConsumptionRate(inputPort);
                rates.append(rate);
            }

            Iterator outputPorts = _outputPortList.iterator();

            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                int rate = DFUtilities.getTokenProductionRate(outputPort);
                rates.append(rate);

                int initRate = DFUtilities.getTokenInitProduction(outputPort);
                rates.append(rate);
            }

            String rateKey = rates.toString();
            int cacheSize = ((IntToken) (scheduleCacheSize.getToken()))
                .intValue();

            if (cacheSize != _cacheSize) {
                // cache size has changed. reset the cache.
                _scheduleCache = new HashMap();
                _scheduleKeyList = new ArrayList(cacheSize);
                _cacheSize = cacheSize;
            }

            if (rateKey.equals(_mostRecentRates)) {
                //System.out.println(getName() + " just use the current");
                schedule = ((SDFScheduler) scheduler).getSchedule();
            } else if (_scheduleCache.containsKey(rateKey)) {
                // cache hit.
                //System.out.println(getName() + " cache hit");
                _mostRecentRates = rateKey;

                if (cacheSize > 0) {
                    // Remove the key from its old position in
                    // the list and add it to the head of the list.
                    _scheduleKeyList.remove(rateKey);
                    _scheduleKeyList.add(0, rateKey);
                }

                schedule = (Schedule) _scheduleCache.get(rateKey);

            } else {
                //System.out.println(getName() + " cache miss");
                // cache miss.
                _mostRecentRates = rateKey;

                if (cacheSize > 0) {
                    while (_scheduleKeyList.size() >= cacheSize) {
                        // Cache is full. Remove tail of list.
                        //System.out.println(getName() + " cache is full.");
                        Object object = _scheduleKeyList.get(cacheSize - 1);
                        _scheduleKeyList.remove(cacheSize - 1);
                        _scheduleCache.remove(object);
                    }
                    // Add key to head of list.
                    _scheduleKeyList.add(0, rateKey);
                }

                // Add key/schedule to the schedule map.
                schedule = ((SDFScheduler) scheduler).getSchedule();
                _scheduleCache.put(rateKey, schedule);
            }
        }
        getScheduler().setValid(true);
        return schedule;
    }

    /** Send a request to the manager to get the HDF schedule if the schedule
     *  is not valide or this director is not at the top level.
     *  @exception IllegalActionException If no schedule can be found,
     *  or if the super class method throws it.
     */
    public boolean postfire() throws IllegalActionException {
        /*
        if (isScheduleValid()) {
            //System.out.println("before HDF postfire(): schedule valid");
        } else {
            //System.out.println("before HDF postfire(): schedule not valid");
        }*/
        // If this director is not at the top level, the HDFFSMDirector
        // of the modal model that it contains may change rates after
        // making a change request, which invalidates this HDF's schedule.
        // So we need to get the schedule of this HDFDirector also in a
        // change request.
        if (!isScheduleValid() || getContainer() != toplevel()) {
            CompositeActor container = (CompositeActor) getContainer();
            ChangeRequest request = new ChangeRequest(this, "reschedule") {
                protected void _execute() throws KernelException {
                    getSchedule();
                }
            };
            request.setPersistent(false);
            container.requestChange(request);
        }
        boolean postfire = super.postfire();
        /*if (isScheduleValid()) {
            //System.out.println("HDF postfire(): schedule valid");
        } else {
            //System.out.println("HDF postfire(): schedule not valid");
        }*/
        return postfire;
    }

    /** Preinitialize the actors associated with this director.
     *  The super class method will compute the schedule.
     *  @exception IllegalActionException If the super class
     *  preinitialize throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _scheduleKeyList.clear();
        _scheduleCache.clear();
        _mostRecentRates = "";
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a list of all the input ports contained by the
     *  deeply contained entities of the container of this director.
     *  @return The list of input ports.
     */
    private List _getInputPortList() {
        CompositeActor container = (CompositeActor) getContainer();
        List actors = container.deepEntityList();
        Iterator actorIterator = actors.iterator();
        List inputPortList = new LinkedList();

        while (actorIterator.hasNext()) {
            Actor containedActor = (Actor) actorIterator.next();
            List temporaryInputPortList = containedActor.inputPortList();
            Iterator inputPortIterator = temporaryInputPortList.iterator();

            while (inputPortIterator.hasNext()) {
                IOPort inputPort = (IOPort) inputPortIterator.next();
                inputPortList.add(inputPort);
            }
        }

        return inputPortList;
    }

    /** Return a list of all the output ports contained by the
     *  deeply contained entities of the container of this director.
     *  @return The list of output ports.
     */
    private List _getOutputPortList() {
        CompositeActor container = (CompositeActor) getContainer();
        List actors = container.deepEntityList();
        Iterator actorIterator2 = actors.iterator();
        List outputPortList = new LinkedList();

        while (actorIterator2.hasNext()) {
            Actor containedActor = (Actor) actorIterator2.next();
            List temporaryOutputPortList = containedActor.outputPortList();
            Iterator outputPortIterator = temporaryOutputPortList.iterator();

            while (outputPortIterator.hasNext()) {
                IOPort outputPort = (IOPort) outputPortIterator.next();
                outputPortList.add(outputPort);
            }
        }

        return outputPortList;
    }

    /** Initialize the object. In this case, we give the HDFDirector a
     *  default scheduler of the class SDFScheduler and a cacheSize parameter.
     */
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        try {
            SDFScheduler scheduler = new SDFScheduler(this,
                    uniqueName("Scheduler"));
            setScheduler(scheduler);
        } catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" + e.getMessage());
        }

        int cacheSize = 100;
        _cacheSize = cacheSize;
        scheduleCacheSize = new Parameter(this, "scheduleCacheSize",
                new IntToken(cacheSize));

        _scheduleCache = new HashMap();
        _scheduleKeyList = new ArrayList(cacheSize);

        allowRateChanges.setToken(BooleanToken.TRUE);
        allowRateChanges.setVisibility(Settable.EXPERT);
        allowRateChanges.setPersistent(false);
    }

    //////////////////////////////////////////////////////////////////
    ////                       private variables                  ////

    // Map for the schedule cache.
    private Map _scheduleCache;

    // List of the schedule keys, which are strings that represent
    // the rate signature.
    private List _scheduleKeyList;

    // A string that represents the most recent port rates.
    private String _mostRecentRates;

    // A list of the input ports.
    private List _inputPortList;

    // A list of the output ports.
    private List _outputPortList;

    // The cache size.
    private int _cacheSize = 100;

    // Local workspace version
    private long _workspaceVersion = 0;
}
