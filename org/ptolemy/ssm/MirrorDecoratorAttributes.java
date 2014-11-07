/* Attributes for the StateSpaceModel decorator.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
/**
An actor that implements state space attributes for the StateSpaceModel decorator.
This actor is a listener of the original Decorator to reflect any port/parameter 
changes to its container. The added parameters by contained by this class itself, 
whereas the ports are added/removed to/from the container actor.

@author Ilge Akkaya 
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public class MirrorDecoratorAttributes extends DecoratorAttributes implements MirrorDecoratorListener{

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public MirrorDecoratorAttributes(NamedObj target, MirrorDecorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator);
        _init(decorator);
    }

    public MirrorDecoratorAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name);
        _init(null);
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
                    new Parameter(this, p.getName());
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
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public void event(MirrorDecorator ssm, DecoratorEvent eventType, String portName) {
        ComponentEntity container = (ComponentEntity) this.getContainer();
        if (enabled()) {
            try {
                TypedIOPort port = (TypedIOPort) container.getPort(portName);
                if (eventType == DecoratorEvent.ADDED_PORT) { 
                    if (port == null) {
                        new TypedIOPort(container, portName, true, false);
                        _addedPorts.add(portName);
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
                } else if (eventType == DecoratorEvent.REMOVED_PORT) {
                    // if the container has a port of this name AND this port
                    // is known to have been added by this decorator, remove it.
                    if (port != null && _addedPorts.contains(portName)) {
                        port.setContainer(null);
                    }
                } 
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            } 
        } else {
            _addedPorts.add(portName);
        }
    }

    /**
     * Add all decorated ports to the container
     */
    private void _addAllPorts() {
        try{
            for (String portName : _addedPorts) {
                ComponentEntity container = (ComponentEntity) this.getContainer();
                if (container.getPort(portName) == null) {
                    new TypedIOPort(container, portName, true, false);
                }
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Remove all decorated ports from the container
     */
    private void _removeAllPorts() {
        try{
            for (String portName : _addedPorts) {
                ComponentEntity container = (ComponentEntity) this.getContainer();
                if (container.getPort(portName) != null) {
                    container.getPort(portName).setContainer(null);
                }
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }
    
    /** Create the parameters. Including any parameter the decorator already includes.
     */
    private void _init(MirrorDecorator decorator) {
        
        try {
            enable = new Parameter(this, "enable");
            enable.setExpression("false");
            enable.setTypeEquals(BaseType.BOOLEAN);
            
            if (decorator != null) {
                for (Object a : decorator.attributeList()) {
                    if (a instanceof Parameter) {
                        event(decorator, DecoratorEvent.ADDED_PARAMETER, ((Parameter) a).getName());
                    }
                }
                
                for (TypedIOPort p : decorator.portList()) {
                    _addedPorts.add(p.getName());
                } 
            }
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
        _addedPorts = new ArrayList<String>();
    }

    /** Boolean indicating  enable status of the decorator */
    private boolean _enabled;
    /** Cached list of added decorator ports. Assumed to be all input ports */
    private List<String> _addedPorts; 

}
