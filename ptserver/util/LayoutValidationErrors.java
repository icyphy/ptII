/*
 Handle different errors during layout validation.
 
 Copyright (c) 2011 The Regents of the University of California.
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
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class LayoutValidationErrors {

    public void addException(Exception e) {
        _allExceptions.add(e);
        _haveErrors = true;
    }

    public HashSet<Exception> getExceptions() {
        return _allExceptions;
    }
    
    public void addEntityWithoutProxy(ComponentEntity entity) {
        _entitiesWithoutProxies.add(entity);
        _haveErrors = true;
    }
    
    public HashSet<ComponentEntity> getEntitiesWithoutProxies() {
        return _entitiesWithoutProxies;
    }
    
    public void addObjectMissingFromModel(NamedObj object) {
        _objectsMissingFromModel.add(object);
        _haveErrors = true;
    }
    
    public HashSet<NamedObj> getObjectsMissingFromModel() {
        return _objectsMissingFromModel;
    }

    public void addProxyWithInvalidTarget(ProxyActor object) {
        _proxiesWithInvalidTargets.add(object);
        _haveErrors = true;
    }
    
    public HashSet<ProxyActor> getProxiesWithInvalidTargets() {
        return _proxiesWithInvalidTargets;
    }

    public void addPortWithNoOrInvalidTarget(Port port) {
        _portsWithNoOrInvalidTargets.add(port);
        _haveErrors = true;
    }
    
    public HashSet<Port> getPortsWithNoOrInvalidTargets() {
        return _portsWithNoOrInvalidTargets;
    }

    public void addPositionableWithInvalidTab(NamedObj object) {
        _positionablesWithInvalidTabs.add(object);
        _haveErrors = true;
    }
    
    public HashSet<NamedObj> getPositionablesWithInvalidTabs() {
        return _positionablesWithInvalidTabs;
    }

    public void addPositionableWithInvalidLocation(NamedObj object) {
        _positionablesWithInvalidLocations.add(object);
        _haveErrors = true;
    }
    
    public HashSet<NamedObj> getPositionablesWithInvalidLocations() {
        return _positionablesWithInvalidLocations;
    }

    public boolean haveErrors() {
        return _haveErrors;
    }

    private HashSet<Exception> _allExceptions = new HashSet<Exception>();
    private HashSet<ComponentEntity> _entitiesWithoutProxies = new HashSet<ComponentEntity>();
    private HashSet<NamedObj> _objectsMissingFromModel = new HashSet<NamedObj>();
    private HashSet<ProxyActor> _proxiesWithInvalidTargets = new HashSet<ProxyActor>();
    private HashSet<Port> _portsWithNoOrInvalidTargets = new HashSet<Port>();
    private HashSet<NamedObj> _positionablesWithInvalidTabs = new HashSet<NamedObj>();
    private HashSet<NamedObj> _positionablesWithInvalidLocations = new HashSet<NamedObj>();
    private boolean _haveErrors = false;
}

