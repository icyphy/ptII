
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
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.Parameter;
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
The ports are added to the decorated container, whereas the parameters are contained
by the DecoratorAttributes object.

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
        _init();
    } 

    public void attributeChanged(Attribute attribute) 
            throws IllegalActionException {
        if (attribute instanceof PortParameter) {
            sendParameterEvent(DecoratorEvent.CHANGED_PORT_PARAMETER, (Parameter) attribute);
        }
        if (attribute instanceof Parameter) {
            sendParameterEvent(DecoratorEvent.CHANGED_PARAMETER, (Parameter) attribute);
        }
        super.attributeChanged(attribute);
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MirrorDecorator newObject = (MirrorDecorator) super
                .clone(workspace);
        newObject._listeners = null;
        newObject._addedPortNames = null;
        newObject._addedParameters = null;
        newObject._addedPortParameterNames = null;
        newObject._decoratedObjects = null;
        return newObject;
    }

    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {  
        if (target instanceof GaussianMeasurementModel) {
            try {
                MeasurementModelAttributes ssa = new MeasurementModelAttributes(target, this);
                registerListener(ssa);
                return ssa;
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else 
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
        if (container != null) {
            for (Object object : container.deepEntityList()) { 
                if (object instanceof StateSpaceActor) {
                    list.add((NamedObj)object); 
                }
            }
            _decoratedObjects = list;
        }
        return list;
    }

    /**
     * Returns the added port names.
     * @return a list containing the added port names
     */
    public List<String> getAddedPortNames() {
        return _addedPortNames;
    }

    /**
     * Returns the added port parameter names.
     * @return a list containing the added port parameter names
     */
    public List<String> getAddedPortParameterNames() {
        return _addedPortParameterNames;
    }

    /**
     * Returns the added paramters
     * @return a list containing the added parameters.
     */
    public List<Parameter> getAddedParameters() {
        return _addedParameters;
    }

    
    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    @Override
    public boolean isGlobalDecorator() {
        return false;
    }

    /** Add a communication aspect monitor to the list of listeners.
     *  @param monitor The communication aspect monitor.
     */
    public void registerListener(MirrorDecoratorListener monitor) {
        if (_listeners == null) {
            _listeners = new ArrayList<MirrorDecoratorListener>();
        }
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
     *  @param parameter Parameter to be added/removed
     */
    public void sendParameterEvent(DecoratorEvent eventType, Parameter parameter) {
        if (_listeners != null) {
            for (MirrorDecoratorListener ssl : _listeners) {
                ssl.event(this, eventType, parameter);
            } 
        }
    }


    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException { 
        super.setContainer(container);
        if (container == null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                MirrorDecoratorAttributes decAttributes = (MirrorDecoratorAttributes) decoratedObject.getDecoratorAttributes(this);
                decAttributes.removeDecorationsFromContainer(); 
            }
        } else {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                decoratedObject.getDecoratorAttributes(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _addPort(TypedIOPort port) throws IllegalActionException,
    NameDuplicationException { 
        super._addPort(port); 
        if (port instanceof ParameterPort) {
            _addedPortParameterNames.add(port.getName());
            // event handled when adding a port parameter
        } else {
            _addedPortNames.add(port.getName());
            sendPortEvent(DecoratorEvent.ADDED_PORT, port.getName()); 
        }

    }

    @Override
    protected void _removePort(Port port) { 
        super._removePort(port); 
        sendPortEvent(DecoratorEvent.REMOVED_PORT, port.getName()); 
        if (port instanceof ParameterPort) {
            _addedPortParameterNames.remove(port.getName());
        } else {
            _addedPortNames.remove(port.getName());
        }
    }

    @Override
    protected void _addAttribute(Attribute attr) 
            throws NameDuplicationException, IllegalActionException { 
        super._addAttribute(attr);
        if (attr instanceof ColorAttribute) {
            // do nothing.
        } else if (attr instanceof PortParameter) {
            sendParameterEvent(DecoratorEvent.ADDED_PORT_PARAMETER, (Parameter)attr);   
            _addedParameters.add((Parameter) attr); 
        } else if (attr instanceof Parameter) {
            sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, (Parameter)attr);  
            _addedParameters.add((Parameter) attr); 
        } 
    }

    @Override
    protected void _removeAttribute(Attribute attr) { 
        super._removeAttribute(attr);
        if (attr instanceof PortParameter) {
            sendParameterEvent(DecoratorEvent.REMOVED_PORT_PARAMETER, (Parameter)attr);
        } else if (attr instanceof Parameter) {
            sendParameterEvent(DecoratorEvent.REMOVED_PARAMETER, (Parameter)attr); 
        }   
        _addedParameters.remove((Parameter) attr);
    }

    private void _init() throws IllegalActionException, NameDuplicationException {
        _listeners = new ArrayList<>(); 
        _addedPortNames = new ArrayList<>();
        _addedPortParameterNames = new ArrayList<>();
        _addedParameters = new ArrayList<>(); 
        for (NamedObj n : decoratedObjects()) {
            MirrorDecoratorAttributes attributes = (MirrorDecoratorAttributes) n.getDecoratorAttributes(this);
            if (attributes != null) {
                registerListener((MirrorDecoratorListener)attributes);
            }
        }
    }

    /** Cached list of decorated objects. */
    protected List<NamedObj> _decoratedObjects;

    /** Version for _decoratedObjects. */
    protected long _decoratedObjectsVersion = -1L;

    /** Listeners registered to receive events from this object. */
    protected ArrayList<MirrorDecoratorListener> _listeners;

    /** A list containing the names of user added ports. */
    protected List<String> _addedPortNames = new ArrayList<>();  

    /** A list containing the names of user added port parameters. */
    protected List<String> _addedPortParameterNames = new ArrayList<>();

    /** A list containing the user added parameters. */
    protected List<Parameter> _addedParameters = new ArrayList<>(); 

}
