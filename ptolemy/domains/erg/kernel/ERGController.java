/*

@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.erg.kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.domains.erg.lib.SynchronizeToRealtime;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.StateEvent;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ERGController extends ModalController {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ERGController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public ERGController(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ERGController controller = (ERGController) super.clone(workspace);
        controller._executiveDirectorVersion = -1;
        controller._objectScopeVersion = -1;
        return controller;
    }

    public void fire() throws IllegalActionException {
        director.fire();
    }

    public Director getExecutiveDirector() {
        Workspace workspace = workspace();
        try {
            workspace.getReadAccess();
            if (_executiveDirectorVersion != workspace.getVersion()) {
                ERGModalModel modalModel = (ERGModalModel) getContainer();
                if (modalModel.getController() == this) {
                    _executiveDirector = super.getExecutiveDirector();
                } else {
                    for (Object atomicEntity
                            : modalModel.allAtomicEntityList()) {
                        if (atomicEntity instanceof Event) {
                            Event event = (Event) atomicEntity;
                            Actor[] refinements;
                            try {
                                refinements = event.getRefinement();
                            } catch (IllegalActionException e) {
                                throw new InternalErrorException(e);
                            }
                            if (refinements != null) {
                                for (Actor refinement : refinements) {
                                    if (refinement == this) {
                                        _executiveDirector = ((ERGController)
                                                event.getContainer()).director;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                _executiveDirectorVersion = workspace.getVersion();
            }
        } finally {
            workspace.doneReading();
        }
        return _executiveDirector;
    }

    public State getInitialState() {
        return null;
    }

    /** Return a scope object that has current values from input ports
     *  of this FSMActor in scope.  This scope is used to evaluate
     *  guard expressions and set and output actions.
     *  @return A scope object that has current values from input ports of
     *  this FSMActor in scope.
     */
    public ParserScope getPortScope() {
        if (_objectScopeVersion != workspace().getVersion()) {
            _objectScope = new ERGObjectScope();
            _objectScopeVersion = workspace().getVersion();
        }
        return _objectScope;
    }

    public boolean hasInput() throws IllegalActionException {
        Iterator<?> inPorts = ((ERGModalModel) getContainer()).inputPortList()
                .iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            Token token =
                (Token) _inputTokenMap.get(p.getName() + "_isPresent");
            if (token != null && BooleanToken.TRUE.equals(token)) {
                return true;
            }
        }
        return false;
    }

    public void initialize() throws IllegalActionException {
        director.initialize();
        super.initialize();
    }

    public boolean isFireFunctional() {
        return director.isFireFunctional();
    }

    public boolean isStrict() {
        return director.isStrict();
    }

    public int iterate(int count) throws IllegalActionException {
        return director.iterate(count);
    }

    /** Create a new instance of Transition with the specified name in
     *  this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return A transition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();

            SchedulingRelation relation = new SchedulingRelation(this, name);
            return relation;
        } finally {
            workspace().doneWriting();
        }
    }

    public boolean postfire() throws IllegalActionException {
        return director.postfire();
    }

    public boolean prefire() throws IllegalActionException {
        return director.prefire();
    }

    public void preinitialize() throws IllegalActionException {
        director.preinitialize();
        super.preinitialize();
    }

    public void stop() {
        director.stop();
    }

    public void stopFire() {
        director.stopFire();
    }

    public boolean synchronizeToRealtime() {
        List<?> synchronizeAttributes =
            attributeList(SynchronizeToRealtime.class);
        boolean synchronize = false;
        if (synchronizeAttributes.size() > 0) {
            SynchronizeToRealtime attribute =
                (SynchronizeToRealtime) synchronizeAttributes.get(0);
            try {
                synchronize = ((BooleanToken) attribute.getToken()).booleanValue();
            } catch (IllegalActionException e) {
                return false;
            }
        }
        return synchronize;
    }

    public void terminate() {
        director.terminate();
    }

    public Set<Inequality> typeConstraints() {
        Set<Inequality> constraintList = new HashSet<Inequality>(
                super.typeConstraints());
        List<?> events = entityList(Event.class);
        for (Object eventObject : events) {
            Event event = (Event) eventObject;
            List<?> attributes = event.attributeList(HasTypeConstraints.class);
            for (Object attributeObject : attributes) {
                HasTypeConstraints attribute =
                    (HasTypeConstraints) attributeObject;
                constraintList.addAll(attribute.typeConstraints());
            }
        }
        return constraintList;
    }

    public ERGDirector director;

    protected void _debug(Event event) {
        _debug(new StateEvent(this, event));
    }

    protected void _setCurrentEvent(Event event) {
        _currentState = event;
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        director = new ERGDirector(this, "_Director");
        new SingletonAttribute(director, "_hide");
    }

    private Director _executiveDirector;

    private long _executiveDirectorVersion = -1;

    private PortScope _objectScope = new ERGObjectScope();

    private long _objectScopeVersion = -1;

    /** This class implements a scope, which is used to evaluate the
     *  parsed expressions.  This class is currently rather simple,
     *  but in the future should allow the values of input ports to
     *  be referenced without having shadow variables.
     */
    private class ERGObjectScope extends PortScope {

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Token get(String name) throws IllegalActionException {
            Token token = super.get(name);
            if (token == null) {
                NamedObj object = ModelScope.getScopedObject(ERGController.this,
                        name);
                if (object instanceof Variable) {
                    token = ((Variable) object).getToken();
                } else if (object != null) {
                    token = new ObjectToken(object, object.getClass());
                }
            }
            return token;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Type getType(String name) throws IllegalActionException {
            Type type = super.getType(name);
            if (type == null) {
                NamedObj object = ModelScope.getScopedObject(ERGController.this,
                        name);
                if (object instanceof Variable) {
                    type = ((Variable) object).getType();
                } else if (object != null) {
                    type = new ObjectType(object, object.getClass());
                }
            }
            return type;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        @SuppressWarnings("unchecked")
        public Set<?> identifierSet() {
            Set<Object> set = super.identifierSet();
            set.addAll((Collection<?>) ModelScope.getAllScopedObjectNames(
                    ERGController.this));
            return set;
        }
    }
}
