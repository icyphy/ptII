/*  Mirror decorator attributes

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
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
///////////////////////////////////////////////////////////////////
////MirrorDecoratorAttributes

/**Attribute generator class for the MirrorDecorator

@see org.ptolemy.ssm.MirrorDecorator.java

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
*/
public class MirrorDecoratorAttributes extends DecoratorAttributes 
implements MirrorDecoratorListener{

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public MirrorDecoratorAttributes(NamedObj target, Decorator decorator)
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
                decorateContainer();
            } else {
                try {
                    removeDecorationsFromContainer();
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            }
        } 
        super.attributeChanged(attribute);
    }

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {

        MirrorDecoratorAttributes result = (MirrorDecoratorAttributes) super.clone(workspace); 
        result._enabled = false; 
        result._cachedDecoratorPortParameters = null;
        result._cachedDecoratorPorts = null;
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
        Parameter containerParam = (Parameter) this.getContainer().getAttribute(p.getName());
        try {
            if (eventType == DecoratorEvent.ADDED_PARAMETER) {
                if (param == null) {
                    Parameter newP = new Parameter(this, p.getName());
                    newP.setExpression(p.getExpression());
                    newP.setVisibility(p.getVisibility());
                }
            } else if (eventType == DecoratorEvent.REMOVED_PARAMETER) {
                if (param != null) {
                    param.setContainer(null);
                }
            } else if (eventType == DecoratorEvent.CHANGED_PARAMETER) {
                if (param != null) {
                    param.setExpression(p.getExpression());
                    param.setVisibility(p.getVisibility());
                }
            } else if (eventType == DecoratorEvent.CHANGED_PORT_PARAMETER) { 
                if (containerParam != null) {
                    containerParam.setExpression(p.getExpression());
                    containerParam.setVisibility(p.getVisibility()); 
                }
            } else if (eventType == DecoratorEvent.ADDED_PORT_PARAMETER) {
                if (enabled()) {
                    if (containerParam == null) {  
                        _cachedDecoratorPortParameters.put(p.getName(),
                                new PortParameter(this.getContainer(), p.getName()));
                    } else {
                        _cachedDecoratorPortParameters.put(p.getName(), containerParam);
                    }
                }  
                if (param == null) {
                    new Parameter(this, p.getName());
                }
            } else if (eventType == DecoratorEvent.REMOVED_PORT_PARAMETER) {
                if (containerParam!=null) {
                    containerParam.setContainer(null); 
                    _cachedDecoratorPorts.remove(p.getName());
                    _cachedDecoratorPortParameters.remove(p.getName());
                }
                if (param != null) {
                    param.setContainer(null);
                } 
            }

        } catch (IllegalActionException | NameDuplicationException e) {
            throw new InternalErrorException(e);
        } 
    }

    /**
     * Send out an
     */
    @Override
    public void event(MirrorDecorator ssm, DecoratorEvent eventType, String portName) {
        ComponentEntity container = (ComponentEntity) this.getContainer();

        try {

            String decoratorName = this.getDecorator().getName();
            String targetPortName = decoratorName + "_" + portName;
            TypedIOPort port = (TypedIOPort) container.getPort(targetPortName); 
            if (eventType == DecoratorEvent.ADDED_PORT) { 
                if (enabled()) {
                    if (port == null) {
                        port = new TypedIOPort(container, targetPortName, true, false); 
                        SingletonParameter showNameParam = ((SingletonParameter)port.getAttribute("_showName"));
                        if (showNameParam == null) {
                            showNameParam = new SingletonParameter(port,"_showName");
                        }
                        showNameParam.setExpression("true");
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
                _cachedDecoratorPorts.put(portName,port);
            } else if (eventType == DecoratorEvent.REMOVED_PORT) {
                // if the container has a port of this name AND this port
                // is known to have been added by this decorator, remove it.
                boolean portAddedByDecorator = _cachedDecoratorPorts.keySet().contains(portName);
                boolean paramPortAddedByDecorator = _cachedDecoratorPortParameters.keySet().contains(portName);
                if (port != null ) {
                    if (portAddedByDecorator) {
                        port.setContainer(null); 
                    }   
                } else {
                    // this could be a parameter port carrying the same name as the original port.
                    port = (TypedIOPort) container.getPort(portName); 
                    if (port != null && paramPortAddedByDecorator) {
                        port.setContainer(null);
                    }
                } 
                _cachedDecoratorPorts.remove(portName);
            } 
        } catch (IllegalActionException | NameDuplicationException e) {
            throw new InternalErrorException(e);
        } 
    }   


    /**
     * Add all decorated ports and necessary parameters to the container.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public void decorateContainer() {
        if (this._decorator != null) {
            for (String decoratorPort : ((MirrorDecorator)this._decorator).getAddedPortParameterNames()) {
                event((MirrorDecorator)this._decorator,
                        DecoratorEvent.ADDED_PORT_PARAMETER, 
                        (Parameter)((MirrorDecorator)this._decorator).getAttribute(decoratorPort)); 
            }
            for (String decoratorPort : ((MirrorDecorator)this._decorator).getAddedPortNames()) {
                event((MirrorDecorator)this._decorator,
                        DecoratorEvent.ADDED_PORT, decoratorPort); 
            } 
        }
    }




    /**
     * Remove all decorated ports from the container
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public void removeDecorationsFromContainer() 
            throws IllegalActionException, NameDuplicationException { 
        if (this._decorator != null) {
            for (Port p : _cachedDecoratorPorts.values()) {
                p.setContainer(null);
            }
            
            for (Parameter p : _cachedDecoratorPortParameters.values()) {
                ParameterPort expectedPort = (ParameterPort) ((ComponentEntity)this.
                        getContainer()).getPort(p.getName());
                if (expectedPort != null) {
                    expectedPort.setContainer(null);
                }
                p.setContainer(null);
            }
             
            _cachedDecoratorPorts.clear();
            _cachedDecoratorPortParameters.clear();
            
            //            List<String> addedPortParNames =  ((MirrorDecorator)this._decorator).getAddedPortParameterNames();
            //            if (addedPortNames != null) {
            //                for (String decoratorPort : addedPortParNames) { 
            //                    event((MirrorDecorator)this._decorator,
            //                            DecoratorEvent.REMOVED_PORT, 
            //                            (Parameter)((MirrorDecorator)this._decorator).getAttribute(decoratorPort));
            //                }
            //            }
        } 
    }

    private void _addAllParameters() {
        if (this._decorator != null) {
            List<Parameter> addedParameters =  ((MirrorDecorator)this._decorator).getAddedParameters();
            if (addedParameters != null) {
                for (Parameter p : addedParameters) {
                    event((MirrorDecorator)this._decorator,
                            DecoratorEvent.ADDED_PARAMETER, p);                      
                }
            }
        }
    }


    /** Create the parameters. Including any parameter the decorator already includes.
     */
    private void _init() {

        _cachedDecoratorPorts = new HashMap<>();

        _cachedDecoratorPortParameters = new HashMap<>();
        
        
        try {
            enable = new Parameter(this, "enable");
            enable.setExpression("false");
            enable.setTypeEquals(BaseType.BOOLEAN);
            enable.setPersistent(true);
            _addAllParameters(); 

        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }

    /** Boolean indicating  enable status of the decorator */
    protected boolean _enabled;  

    /** Cached list of decorator ports that are added to the container by this
     * class
     */
    protected Map<String,Port> _cachedDecoratorPorts;

    /** Cached list of decorator parameters that are added to the container by this
     * class
     */
    protected Map<String,Parameter> _cachedDecoratorPortParameters;

}
