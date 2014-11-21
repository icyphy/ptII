package org.ptolemy.ssm; 


import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator; 
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class MeasurementModelAttributes extends MirrorDecoratorAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public MeasurementModelAttributes(NamedObj target, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator); 
    }

    public MeasurementModelAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  If the attribute is
     *  <i>enable</i>, remember the value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     * @throws NameDuplicationException 
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException{
        if (attribute == enable) {
            _enabled = ((BooleanToken) enable.getToken()).booleanValue();
            try {
                if (enabled()) { 
                    _addStateSpaceVariablesToContainer();
                } else { 
                    _removeStateSpaceVariablesFromContainer();
                }
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
        }
        super.attributeChanged(attribute);
    } 

    //    @Override
    //    public void event(MirrorDecorator ssm, DecoratorEvent eventType, Parameter p) {
    //
    //        if (p.getName().equals("noiseCovariance")) {
    //            Parameter param = (Parameter) this.getAttribute(p.getName()); 
    //            String decoratorName = _decorator.getName();
    //            String measurementAttributeName = decoratorName + "_" + p.getName();
    //            Parameter containerParam = (Parameter) this.getContainer().
    //                    getAttribute(measurementAttributeName);
    //            try { 
    //                if (eventType == DecoratorEvent.ADDED_PARAMETER) { 
    //                    if (param == null) {
    //                        Parameter newP = new Parameter(this, measurementAttributeName);
    //                        newP.setExpression(p.getExpression());
    //                        newP.setVisibility(p.getVisibility());
    //                    }
    //                } else if (eventType == DecoratorEvent.REMOVED_PARAMETER) {
    //                    if (param != null) {
    //                        param.setContainer(null);
    //                    }
    //                } else if (eventType == DecoratorEvent.CHANGED_PARAMETER) {
    //                    if (param != null) {
    //                        param.setExpression(p.getExpression());
    //                        param.setVisibility(p.getVisibility());
    //                    }
    //                }  
    //            } catch (NameDuplicationException e) {
    //                throw new InternalErrorException(e);
    //            } catch (IllegalActionException e) {
    //                throw new InternalErrorException(e);
    //            } 
    //        }
    //    }

    private void _addStateSpaceVariablesToContainer() throws IllegalActionException, NameDuplicationException {
        Parameter stateVariableNames = (Parameter) this.getAttribute("stateVariableNames");
        if (stateVariableNames != null) {
            if (stateVariableNames.getToken() != null) {
                Token[] tokens = ((ArrayToken)stateVariableNames.getToken()).arrayValue();
                for (Token t : tokens) {
                    String name = ((StringToken)t).stringValue();
                    Parameter containerParam = (Parameter) this.getContainer().getAttribute(name);
                    Parameter thisParam = (Parameter) this.getAttribute(name);

                    if (thisParam != null && containerParam == null) {
                        containerParam = new Parameter(this.getContainer(), name);
                        containerParam.setExpression(thisParam.getExpression());
                        containerParam.setVisibility(Settable.NONE); 
                        thisParam.setVisibility(Settable.NONE);
                    } 
                }
            }
        }
    }

    private void _removeStateSpaceVariablesFromContainer() throws IllegalActionException, NameDuplicationException {
        Parameter stateVariableNames = (Parameter) this.getAttribute("stateVariableNames");
        if (stateVariableNames != null) {
            if (stateVariableNames.getToken() != null) {
                Token[] tokens = ((ArrayToken)stateVariableNames.getToken()).arrayValue(); 
                for (Token t : tokens) {
                    String name = ((StringToken)t).stringValue();
                    Parameter containerParam = (Parameter) this.getContainer().getAttribute(name); 

                    if (containerParam != null) {
                        containerParam.setContainer(null);
                    } 
                } 
            }
        }
    }



}

