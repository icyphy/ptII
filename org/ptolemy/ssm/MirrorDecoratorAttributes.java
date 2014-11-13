package org.ptolemy.ssm;


import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class MirrorDecoratorAttributes extends DecoratorAttributes implements MirrorDecoratorListener{

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
                        new PortParameter(this.getContainer(), p.getName());
                    }  
                    if (param == null) {
                        new Parameter(this, p.getName());
                    }
                }
            } else if (eventType == DecoratorEvent.REMOVED_PORT_PARAMETER) {
                if (containerParam!=null) {
                    containerParam.setContainer(null);
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
                if (port != null && 
                        ((MirrorDecorator)this._decorator).getAddedPortNames().contains(portName)) {
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
        if (this._decorator != null) {
            for (String decoratorPort : ((MirrorDecorator)this._decorator).getAddedPortNames()) {
                event((MirrorDecorator)this._decorator,
                        DecoratorEvent.ADDED_PORT, decoratorPort); 
            }
            for (String decoratorPort : ((MirrorDecorator)this._decorator).getAddedPortParameterNames()) {
                event((MirrorDecorator)this._decorator,
                        DecoratorEvent.ADDED_PORT_PARAMETER, 
                        (Parameter)((MirrorDecorator)this._decorator).getAttribute(decoratorPort)); 
            }
        }
    }

    /**
     * Remove all decorated ports from the container
     */
    private void _removeAllPorts() {
        try{
            if (this._decorator != null) {
                for (String port : ((MirrorDecorator)this._decorator).getAddedPortNames()) {
                    ComponentEntity container = (ComponentEntity) this.getContainer();
                    if (container.getPort(port) != null) {
                        container.getPort(port).setContainer(null);
                    }
                }
                for (String decoratorPort : ((MirrorDecorator)this._decorator).getAddedPortParameterNames()) {
                    event((MirrorDecorator)this._decorator,
                            DecoratorEvent.REMOVED_PORT_PARAMETER, 
                            (Parameter)((MirrorDecorator)this._decorator).getAttribute(decoratorPort)); 
                }
            }
        } catch (IllegalActionException | NameDuplicationException e) {
            throw new InternalErrorException(e);
        }  
    }

    private void _addAllParameters() {
        if (this._decorator != null) {
            for (Parameter p : ((MirrorDecorator)this._decorator).getAddedParameters()) {
                event((MirrorDecorator)this._decorator,
                        DecoratorEvent.ADDED_PARAMETER, p);                      
            }
        }
    }


    /** Create the parameters. Including any parameter the decorator already includes.
     */
    private void _init() {

        try {
            enable = new Parameter(this, "enable");
            enable.setExpression("false");
            enable.setTypeEquals(BaseType.BOOLEAN);
            enable.setPersistent(true);

            if (enabled()) {
                _addAllPorts();
            } else {
                _removeAllPorts();
            } 

            _addAllParameters();



        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }

    /** Boolean indicating  enable status of the decorator */
    private boolean _enabled; 

}
