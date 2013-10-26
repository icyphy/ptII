/*
 Handle different errors during layout validation.

 Copyright (c) 2011-2012 The Regents of the University of California.
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

package ptserver.util;

import java.util.HashSet;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;
import ptserver.actor.ProxyActor;

/**
 * Handle different errors during layout validation.
 * @author Peter Foldes
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class LayoutValidationErrors {

    /**
     * Add exception.
     * @param e The exception to add.
     */
    public void addException(Exception e) {
        _allExceptions.add(e);
        _haveErrors = true;
    }

    /**
     * Return the set of exceptions.
     * @return  the set of exceptions.
     */
    public HashSet<Exception> getExceptions() {
        return _allExceptions;
    }

    /**
     * Add entity without proxy.
     * @param entity the entity without proxy.
     */
    public void addEntityWithoutProxy(ComponentEntity entity) {
        _entitiesWithoutProxies.add(entity);
        _haveErrors = true;
    }

    /**
     * Return the set of entities without proxies.
     * @return the set of entities without proxies.
     */
    public HashSet<ComponentEntity> getEntitiesWithoutProxies() {
        return _entitiesWithoutProxies;
    }

    /**
     * Add an object that's missing from the model.
     * @param object The object that's missing from the model.
     */
    public void addObjectMissingFromModel(NamedObj object) {
        _objectsMissingFromModel.add(object);
        _haveErrors = true;
    }

    /**
     * Return the set of objects missing from the model.
     * @return the set of objects missing from the model.
     */
    public HashSet<NamedObj> getObjectsMissingFromModel() {
        return _objectsMissingFromModel;
    }

    /**
     * Add a proxy with invalid target.
     * @param object The proxy with invalid target.
     */
    public void addProxyWithInvalidTarget(ProxyActor object) {
        _proxiesWithInvalidTargets.add(object);
        _haveErrors = true;
    }

    /**
     * Get a set of proxies with invalid targets.
     * @return  a set of proxies with invalid targets.
     */
    public HashSet<ProxyActor> getProxiesWithInvalidTargets() {
        return _proxiesWithInvalidTargets;
    }

    /**
     * Add a port without or invalid target.
     * @param port a port without or invalid target.
     */
    public void addPortWithNoOrInvalidTarget(Port port) {
        _portsWithNoOrInvalidTargets.add(port);
        _haveErrors = true;
    }

    /**
     * Get a set of ports without or invalid targets.
     * @return a set of ports without or invalid targets.
     */
    public HashSet<Port> getPortsWithNoOrInvalidTargets() {
        return _portsWithNoOrInvalidTargets;
    }

    /**
     * Add positionable with invalid tab.
     * @param object The positionable with invalid tab.
     */
    public void addPositionableWithInvalidTab(NamedObj object) {
        _positionablesWithInvalidTabs.add(object);
        _haveErrors = true;
    }

    /**
     * Get a set of positionables with invalid tabs.
     * @return a set of positionables with invalid tabs.
     */
    public HashSet<NamedObj> getPositionablesWithInvalidTabs() {
        return _positionablesWithInvalidTabs;
    }

    /**
     * Add positionable with invalid location.
     * @param object positionable entity to add.
     */
    public void addPositionableWithInvalidLocation(NamedObj object) {
        _positionablesWithInvalidLocations.add(object);
        _haveErrors = true;
    }

    /**
     * Get a set of positionables with invalid locations.
     * @return a set of positionables with invalid locations.
     */
    public HashSet<NamedObj> getPositionablesWithInvalidLocations() {
        return _positionablesWithInvalidLocations;
    }

    /**
     * Return a boolean indicator showing whether there are any validation errors.
     * @return a boolean indicator showing whether there are any validation errors.
     */
    public boolean haveErrors() {
        return _haveErrors;
    }

    /**
     * Set of all exceptions.
     */
    private HashSet<Exception> _allExceptions = new HashSet<Exception>();
    /**
     * Set of entities without proxies.
     */
    private HashSet<ComponentEntity> _entitiesWithoutProxies = new HashSet<ComponentEntity>();
    /**
     * Set of objects missing from the model.
     */
    private HashSet<NamedObj> _objectsMissingFromModel = new HashSet<NamedObj>();
    /**
     * Set of proxies with invalid targets.
     */
    private HashSet<ProxyActor> _proxiesWithInvalidTargets = new HashSet<ProxyActor>();
    /**
     * Set of ports with or invalid targets.
     */
    private HashSet<Port> _portsWithNoOrInvalidTargets = new HashSet<Port>();
    /**
     * Set of positionables with invalid tabs.
     */
    private HashSet<NamedObj> _positionablesWithInvalidTabs = new HashSet<NamedObj>();
    /**
     * Set of positionables with invalid locations.
     */
    private HashSet<NamedObj> _positionablesWithInvalidLocations = new HashSet<NamedObj>();
    /**
     * Boolean indicator showing whether there are any validation errors.
     */
    private boolean _haveErrors = false;
}
