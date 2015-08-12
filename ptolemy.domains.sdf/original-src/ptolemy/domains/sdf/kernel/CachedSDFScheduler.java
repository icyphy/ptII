/* A extension of the SDFScheduler that caches the schedules.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CachedSDFScheduler

/**
 The CachedSDFScheduler extends the SDFScheduler by caching schedules.
 The cached schedules are labeled by their corresponding rate signatures,
 with the most recently used at the beginning of the cache queue. If the
 rate signatures are contained in the cache keys, then the corresponding
 schedule in the cache is used. Therefore, we do not need to recompute the
 schedule again.
 <p>
 The size of the cache in the CachedSDFScheduler is usually set by its
 containing director when constructing this scheduler. If the cache is
 full, the least recently used schedule (at the end of the cache) is
 discarded.

 @see SDFScheduler

 @author Ye Zhou. Contributor: Brian K. Vogel
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class CachedSDFScheduler extends SDFScheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler". The cache size is the default value of 0.
     */
    public CachedSDFScheduler() {
        super();
        constructCaches(0);
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler". The cache size is the default value of 0.
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public CachedSDFScheduler(Workspace workspace) {
        super(workspace);
        constructCaches(0);
    }

    /** Construct a scheduler in the given container with the given name
     *  and given cache size. The container argument must not be null, or a
     *  NullPointerException will be thrown. This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param cacheSize The cache size of this scheduler.
     *  @exception IllegalActionException If the super class throws it.
     *  @exception NameDuplicationException If the super class throws it.
     */
    public CachedSDFScheduler(Director container, String name, int cacheSize)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        constructCaches(cacheSize);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the schedule cache, cache keys and cache for external rates
     *  of this scheduler.
     */
    public void clearCaches() {
        if (_cacheSize > 0) {
            _scheduleKeyList.clear();
        }

        _scheduleCache.clear();
        _externalRatesCache.clear();
        _mostRecentRates = "";
    }

    /** Construct the caches of this scheduler with the given cache size.
     *  @param cacheSize The given cache sie.
     */
    public void constructCaches(int cacheSize) {
        _scheduleCache = new HashMap();

        if (cacheSize > 0) {
            _scheduleKeyList = new ArrayList(cacheSize);
        }

        _externalRatesCache = new TreeMap();
        _cacheSize = cacheSize;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence. If the schedule exist in the
     *  cache (schedules are identified by the rate signatures of ports),
     *  then return the corresponding schedule in the cache. Otherwise,
     *  compute the schedule and return it.
     *  @return A schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If the super class throws it.
     *  @exception IllegalActionException If the super class throws it.
     */
    @Override
    protected Schedule _getSchedule() throws NotSchedulableException,
    IllegalActionException {
        Schedule schedule;

        if (_inputPortList == null
                || _workspaceVersion != workspace().getVersion()) {
            _inputPortList = _getInputPortList();
        }

        if (_outputPortList == null
                || _workspaceVersion != workspace().getVersion()) {
            _outputPortList = _getOutputPortList();
        }

        _workspaceVersion = workspace().getVersion();

        StringBuffer rates = new StringBuffer();

        Iterator inputPorts = _inputPortList.iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int rate = DFUtilities.getTokenConsumptionRate(inputPort);
            rates.append(rate);

            int initRate = DFUtilities.getTokenInitConsumption(inputPort);
            rates.append("_");
            rates.append(initRate);
        }

        Iterator outputPorts = _outputPortList.iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            int rate = DFUtilities.getTokenProductionRate(outputPort);
            rates.append(rate);

            int initRate = DFUtilities.getTokenInitProduction(outputPort);
            rates.append("_");
            rates.append(initRate);
        }

        String rateKey = rates.toString();

        if (_scheduleCache.containsKey(rateKey)) {
            // cache hit.
            schedule = (Schedule) _scheduleCache.get(rateKey);

            if (!rateKey.equals(_mostRecentRates)) {
                _mostRecentRates = rateKey;

                if (_cacheSize > 0) {
                    // Remove the key from its old position in
                    // the list and add it to the head of the list.
                    _scheduleKeyList.remove(rateKey);
                    _scheduleKeyList.add(0, rateKey);
                }

                Map externalRates = (Map) _externalRatesCache.get(rateKey);
                _saveContainerRates(externalRates);
            }
        } else {
            // cache miss.
            _mostRecentRates = rateKey;

            if (_cacheSize > 0) {
                while (_scheduleKeyList.size() >= _cacheSize) {
                    // Cache is full. Remove the end of the caches.
                    Object key = _scheduleKeyList.get(_cacheSize - 1);
                    _scheduleKeyList.remove(_cacheSize - 1);
                    _scheduleCache.remove(key);
                    _externalRatesCache.remove(key);
                }

                // Add key to head of list.
                _scheduleKeyList.add(0, rateKey);
            }

            // Compute the SDF schedule.
            schedule = super._getSchedule();

            // Add key/schedule to the schedule map.
            _scheduleCache.put(rateKey, schedule);

            // Add external rates map to the external rates cache.
            Map externalRates = getExternalRates();
            _externalRatesCache.put(rateKey, externalRates);

            // Note: we do not need to set the external rates of
            // the container here. When the SDFSchedule is recomputed,
            // it will set the external rates.
        }

        setValid(true);
        return schedule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a list of all the input ports contained by the
     *  deeply contained entities of the container of this director.
     *  @return The list of input ports.
     */
    private List _getInputPortList() {
        CompositeActor container = (CompositeActor) getContainer()
                .getContainer();
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
        CompositeActor container = (CompositeActor) getContainer()
                .getContainer();
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The size of the cache.
    private int _cacheSize;

    // Map for the schedule cache.
    private Map _scheduleCache;

    // List of the schedule keys, which are strings that represent
    // the rate signature.
    private List _scheduleKeyList;

    // Map for the cache of the external rates.
    private Map _externalRatesCache;

    // A string that represents the most recent port rates.
    private String _mostRecentRates;

    // A list of the input ports.
    private List _inputPortList;

    // A list of the output ports.
    private List _outputPortList;

    // Local workspace version
    private long _workspaceVersion = 0;
}
