
/* A decorator that, when enabled, populates the container with its ports
 * and parameters.

 Copyright (c) 2014 The Regents of the University of California.
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
package org.ptolemy.ssm;

import java.util.ArrayList;
import java.util.List;





import org.ptolemy.ssm.MirrorDecoratorListener.DecoratorEvent;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort; 
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
/**
A  special decorator that mirrors its parameters and ports to the decorated actor.


@author Ilge Akkaya 
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public class MirrorDecorator extends TypedAtomicActor implements Decorator {


    /** Construct a MirrorDecorator with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MirrorDecorator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _listeners = new ArrayList<>(); 
        _addedPortNames = new ArrayList<>();
        _addedParameters = new ArrayList<>();
    }

    /** Construct a MirrorDecorator in the given workspace.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace. 
     */
    public MirrorDecorator(Workspace workspace) {
        super(workspace);
        _listeners = new ArrayList<>(); 
        _addedPortNames = new ArrayList<>();
        _addedParameters = new ArrayList<>();
    }

    public void attributeChanged(Attribute attribute) 
            throws IllegalActionException {
        if (attribute instanceof Parameter) {
            sendParameterEvent(DecoratorEvent.CHANGED_PARAMETER, (Parameter) attribute);
        }
        super.attributeChanged(attribute);
    }

    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof StateSpaceActor) {
            try {
                MirrorDecoratorAttributes ssa = new MirrorDecoratorAttributes(target, this);
                registerListener(ssa);
                return ssa;
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<NamedObj> decoratedObjects() throws IllegalActionException {
        if (workspace().getVersion() == _decoratedObjectsVersion) {
            return _decoratedObjects;
        }
        _decoratedObjectsVersion = workspace().getVersion();
        List<NamedObj> list = new ArrayList();
        CompositeEntity container = (CompositeEntity) getContainer();
        for (Object object : container.deepEntityList()) {
            if (object instanceof StateSpaceActor) {
                list.add((NamedObj)object); 
            }
        }
        _decoratedObjects = list;
        return list;
    }

    @Override
    public boolean isGlobalDecorator() throws IllegalActionException { 
        return false;
    }

    /** Add a communication aspect monitor to the list of listeners.
     *  @param monitor The communication aspect monitor.
     */
    public void registerListener(MirrorDecoratorListener monitor) {
        _listeners.add(monitor);
    }
    /** Notify the monitor that an event happened. 
     *  @param eventType Type of event.
     *  @param portName Name of port to be added/removed
     */
    public void sendPortEvent(DecoratorEvent eventType, String portName) {
        if (_listeners != null) {
            for (MirrorDecoratorListener ssl : _listeners) {
                ssl.event(this, eventType, portName);
            } 
        }
    }

    /** Notify the monitor that an event happened. 
     *  @param eventType Type of event.
     *  @param portName Name of port to be added/removed
     */
    public void sendParameterEvent(DecoratorEvent eventType, Parameter parameter) {
        if (_listeners != null) {
            for (MirrorDecoratorListener ssl : _listeners) {
                ssl.event(this, eventType, parameter);
            } 
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _addPort(TypedIOPort port) throws IllegalActionException,
    NameDuplicationException { 
        super._addPort(port);
        _addedPortNames.add(port.getName());
        sendPortEvent(DecoratorEvent.ADDED_PORT, port.getName());
    }

    @Override
    protected void _removePort(Port port) { 
        super._removePort(port);
        sendPortEvent(DecoratorEvent.REMOVED_PORT, port.getName()); 
        _addedPortNames.remove(port.getName());
    }

    @Override
    protected void _addAttribute(Attribute attr) 
            throws NameDuplicationException, IllegalActionException { 
        super._addAttribute(attr);
        if (attr instanceof Parameter) {
            sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, (Parameter)attr); 
            _addedParameters.add((Parameter) attr);
        }
    }

    @Override
    protected void _removeAttribute(Attribute attr) { 
        super._removeAttribute(attr);
        if (attr instanceof Parameter) {
            sendParameterEvent(DecoratorEvent.REMOVED_PARAMETER, (Parameter)attr); 
            _addedParameters.remove((Parameter) attr);
        }
    }




    /** Cached list of decorated objects. */
    private List<NamedObj> _decoratedObjects;

    /** Version for _decoratedObjects. */
    private long _decoratedObjectsVersion = -1L;

    /** Listeners registered to receive events from this object. */
    private ArrayList<MirrorDecoratorListener> _listeners;

    private List<String> _addedPortNames; 
    private List<Parameter> _addedParameters;

    public class MirrorDecoratorAttributes 
    extends DecoratorAttributes implements MirrorDecoratorListener{

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public MirrorDecoratorAttributes(NamedObj target, MirrorDecorator decorator)
                throws IllegalActionException, NameDuplicationException {
            super(target, decorator);
            _init();
        }

        public MirrorDecoratorAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** React to a change in an attribute.  If the attribute is
         *  <i>enable</i>, remember the value.
         *  @param attribute The attribute that changed.
         *  @exception IllegalActionException If the change is not acceptable
         *   to this container (not thrown in this base class).
         */
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            if (attribute == enable) {
                _enabled = ((BooleanToken) enable.getToken()).booleanValue();
                if (enabled()) {
                    _addAllPorts();
                } else {
                    _removeAllPorts();
                }
            }
            super.attributeChanged(attribute);
        }

        @Override
        public Object clone(Workspace workspace) throws CloneNotSupportedException {

            MirrorDecoratorAttributes result = (MirrorDecoratorAttributes) super.clone(workspace);
            result._enabled = false;
            return result;
        }


        /** Return whether the decorator associated with this attribute is
         *  enabled.
         *  @return True if enabled.
         */
        public boolean enabled() {
            return _enabled;
        }
        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** The enable parameter specifies whether the decorated actor uses
         *  the resource scheduler decorator.
         *  This is a boolean that defaults to false.
         */
        public Parameter enable;

        @Override
        public void event(MirrorDecorator ssm, DecoratorEvent eventType, Parameter p) {

            Parameter param = (Parameter) this.getAttribute(p.getName());
            try {
                if (eventType == DecoratorEvent.ADDED_PARAMETER) {
                    if (param == null) {
                        Parameter newP = new Parameter(this, p.getName());
                        newP.setExpression(p.getExpression());
                        
                    }
                } else if (eventType == DecoratorEvent.REMOVED_PARAMETER) {
                    if (param != null) {
                        param.setContainer(null);
                    }
                } else if (eventType == DecoratorEvent.CHANGED_PARAMETER) {
                    if (param != null) {
                        param.setExpression(p.getExpression());
                    }
                }
            } catch (IllegalActionException | NameDuplicationException e) {
                throw new InternalErrorException(e);
            } 
        }

        @Override
        public void event(MirrorDecorator ssm, DecoratorEvent eventType, String portName) {
            ComponentEntity container = (ComponentEntity) this.getContainer();

            try {
                TypedIOPort port = (TypedIOPort) container.getPort(portName);
                if (eventType == DecoratorEvent.ADDED_PORT) { 
                    if (enabled()) {
                        if (port == null) {
                            new TypedIOPort(container, portName, true, false);
                        } else {
                            // the decorator is attempting to add an input port
                            // which has the same name as an existing output port
                            if (port.isOutput()) {
                                throw new IllegalActionException(this, 
                                        "Decorator is attempting to add an "
                                                + "input port to the actor which has "
                                                + "the same name as an existing output port.");
                            }
                        }
                    }
                } else if (eventType == DecoratorEvent.REMOVED_PORT) {
                    // if the container has a port of this name AND this port
                    // is known to have been added by this decorator, remove it.
                    if (port != null && _addedPortNames.contains(portName)) {
                        port.setContainer(null);
                    }
                } 
            } catch (IllegalActionException | NameDuplicationException e) {
                throw new InternalErrorException(e);
            } 
        }   


        /**
         * Add all decorated ports to the container
         */
        private void _addAllPorts() {
            for (String decoratorPort : _addedPortNames) {
                event((MirrorDecorator)this._decorator,
                        DecoratorEvent.ADDED_PORT, decoratorPort); 
            }  
        }

        /**
         * Remove all decorated ports from the container
         */
        private void _removeAllPorts() {
            try{
                for (String port : _addedPortNames) {
                    ComponentEntity container = (ComponentEntity) this.getContainer();
                    if (container.getPort(port) != null) {
                        container.getPort(port).setContainer(null);
                    }
                }
            } catch (IllegalActionException | NameDuplicationException e) {
                throw new InternalErrorException(e);
            }  
        }

        /** Create the parameters. Including any parameter the decorator already includes.
         */
        private void _init() {

            try {
                enable = new Parameter(this, "enable");
                enable.setExpression("false");
                enable.setTypeEquals(BaseType.BOOLEAN);

                if (enabled()) {
                    _addAllPorts();
                } else {
                    _removeAllPorts();
                }
                for (Parameter p : _addedParameters) {
                    event((MirrorDecorator)this._decorator,
                            DecoratorEvent.ADDED_PARAMETER, p);                      
                }
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }

        /** Boolean indicating  enable status of the decorator */
        private boolean _enabled; 

    }
}
