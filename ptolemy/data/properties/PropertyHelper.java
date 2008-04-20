package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.LocationAttribute;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.parameters.DoubleRangeParameter;
import ptolemy.actor.parameters.LocationParameter;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.ptalon.PtalonParameter;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.unit.BaseUnit;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.AttributeExpressionAttribute;
import ptolemy.vergil.kernel.attributes.AttributeValueAttribute;

public abstract class PropertyHelper {

    public Entity getContainerEntity(ASTPtRootNode node) {
        Attribute attribute = _solver.getAttribute(node);
        NamedObj container = attribute.getContainer();
        
        while (!(container instanceof Entity)) {
            container = container.getContainer();
        }
        return (Entity) container;
    }
    
    public String getName() {
        return "Helper_" + _component.toString();
    }
    
    public String toString() {
        return getName() + " " + super.toString();
    }

    /**
     * @param attribute
     * @return
     * @throws IllegalActionException
     */
    protected ASTPtRootNode getParseTree(Attribute attribute) 
            throws IllegalActionException {
        return _solver.getParseTree(attribute);
    }
    
    /**
     * Return the associated property solver
     * @return The property solver associated with this helper.
     */
    public PropertySolver getSolver() {
        return _solver;
    }
    

    /**
     * @param _attributes the _attributes to set
     */
    protected void putAttributes(ASTPtRootNode node, Attribute attribute) {
        _solver.getSharedUtilities().putAttributes(node, attribute);
    }

    
    public static void resetAll() {
    }
    

    /** The associated component of this helper. */
    private Object _component;
    
    /** The associated property lattice. */
    protected PropertySolver _solver;
    
    /**
     * Return a list of property-able object(s) for this helper.
     * @return a list of property-able objects.
     */
    public abstract List<Object> getPropertyables();

    /**
     * 
     * @return
     * @throws IllegalActionException
     */
    protected abstract List<PropertyHelper> _getSubHelpers() throws IllegalActionException;
    

    protected abstract List _getSourcePortList(IOPort port);

    protected abstract List _getSinkPortList(IOPort port);
    
    
    /**
     * Create a constraint that set the given object to be equal
     * to the given property. Mark the property of the given object
     * to be non-settable. 
     * @param object The given object.
     * @param property The given property.
     */
    public void setEquals(Object object, Property property) {
        _solver.setDeclaredProperty(object, property);
        _solver.setResolvedProperty(object, property);
        _solver.addNonSettable(object);        
    }

    /**
     * Reinitialize the helper before each round of property resolution.
     * It resets the resolved property of all propertyable objects. This
     * method is called in the beginning of the
     * PropertyConstraintSolver.resolveProperties() method.
     * @throws IllegalActionException Thrown if 
     */
    public void reinitialize() throws IllegalActionException {
        boolean record = _solver.isTraining() || _solver._isInvoked;
        boolean isManualAnnotate = _solver.isManualAnnotate();
        boolean clearShowInfo = _solver.isTraining() || isManualAnnotate;
        
        List propertyables = getPropertyables();
        Iterator iterator = propertyables.iterator();
        while (iterator.hasNext()) {
            Object propertyable = iterator.next();

            // Remove all PropertyAttributes.
            if (propertyable instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) propertyable; 
                PropertyAttribute attribute = (PropertyAttribute) 
                namedObj.getAttribute(getSolver().getExtendedUseCaseName());
                
                if (attribute != null) {
                    if (isManualAnnotate) {
                        // Indicate this to be non-settable.
                        setEquals(propertyable, attribute.getProperty());
                    }
                    
                    // Clear the property under two cases:
                    // 1. The (invoked or auxilary) solver is in trainingMode.
                    // 2. The solver is invoked under testing mode.
                    if (record) {
                        
                        _solver.recordPreviousProperty(
                                propertyable, attribute.getProperty());
                        
                        // Remove the property attribute.
                        try {
                            attribute.setContainer(null);
                            
                        } catch (NameDuplicationException e) {
                            // This shouldn't happen since we are removing it.
                            assert false;
                        }
                        
                    }
                } 
                                
                if (_solver.isSettable(propertyable)) {
                    _solver.clearResolvedProperty(propertyable);
                }
                
                if (clearShowInfo) {
                    // Clear the expression of the _showInfo attribute.
                    // It will be updated later.
                    StringParameter showAttribute = 
                        (StringParameter) namedObj.getAttribute("_showInfo");            
                    if (showAttribute != null) {                
                        showAttribute.setExpression("");
                    }
                }         
            }            
        }        
        
        // The recursive case.
        for (PropertyHelper helper : _getSubHelpers()) {
            helper.reinitialize();
        }
    }

    /**
     * 
     * @return
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = new LinkedList<Attribute>();
        Iterator attributes = 
            ((Entity) getComponent()).attributeList().iterator();
    
        while (attributes.hasNext()) {
            Object attribute = attributes.next();

// FIXME: find right attributes: Variables, stringAttributes + guardExpression, ...? StringParameter?
// Assume all string attributes are NOT expression by default.
// Sub classes are responsible for picking out expression string
// attributes.
            
            if (attribute instanceof Variable) {
                if (((Variable) attribute)
                        .getVisibility()  == Settable.FULL) {

                	// filter Parameters with certain names
                	if ((attribute instanceof Parameter) && 
                		(((Parameter)attribute).getName().equals("firingCountLimit") ||
                		 ((Parameter)attribute).getName().equals("NONE") ||
                                 ((Parameter)attribute).getName().equals("_hideName") ||
                                 ((Parameter)attribute).getName().equals("_showName") ||
                                 ((Parameter)attribute).getName().equals("displayWidth")
                		)) {
                	
                		// do nothing, ignore the parameter
                	} else if (attribute instanceof PortParameter) {
                        PortParameter parameter = (PortParameter) attribute; 
                        if (parameter.getPort().numLinks() == 0) {
                            result.add((Attribute) attribute);
                        }
                    } else if ((!(attribute instanceof BaseUnit)) ||
                               (!(attribute instanceof ColorAttribute)) ||
                               (!(attribute instanceof DoubleRangeParameter)) ||
                               (!(attribute instanceof ExpertParameter)) ||
                               (!(attribute instanceof LocationParameter)) ||
                               (!(attribute instanceof LocationAttribute)) ||
                               (!(attribute instanceof PtalonParameter)) ||
                               (!(attribute instanceof SizeAttribute)) ||
                               (!(attribute instanceof TypeAttribute)) ||
                               (!(attribute instanceof AttributeValueAttribute)) ||
                               (!(attribute instanceof AttributeExpressionAttribute)) ||
                               (!(attribute instanceof WindowPropertiesAttribute)))
                            {
                        result.add((Attribute) attribute);
                    }
                }
            } else if (attribute instanceof StringAttribute) {
// FIXME: narrow guardTransitions (e.g. check container class)
                if ((((StringAttribute)attribute).getName().equalsIgnoreCase("guardTransition")) ||
                    ((((StringAttribute)attribute).getContainer() instanceof Expression)) && 
                     ((StringAttribute)attribute).getName().equalsIgnoreCase("expression")){

                    result.add((Attribute) attribute);
                }             
            }           
        }        
        
        return result;
    }

    public List<Object> getPropertyables(Class filter) {
        List<Object> list = new LinkedList<Object>();
        Iterator iterator = getPropertyables().iterator();
        
        
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (filter.isInstance(object)) {
                list.add(object);
            }
        }
        return list;
    }

    /**
     * @param _component the _component to set
     */
    public void setComponent(Object _component) {
        this._component = _component;
    }

    /**
     * @return the _component
     */
    public Object getComponent() {
        return _component;
    }

    protected List<PropertyHelper> _getASTNodeHelpers() {
        List<PropertyHelper> astHelpers = new ArrayList<PropertyHelper>();
        ParseTreeASTNodeHelperCollector collector = 
            new ParseTreeASTNodeHelperCollector();
        
        Iterator roots = _getAttributeParseTrees().iterator();
        
        while (roots.hasNext()) {
            ASTPtRootNode root = (ASTPtRootNode) roots.next();
            if (root != null) {
                try {
                    List<PropertyHelper> helpers = collector.collectHelpers(root, getSolver());
                    astHelpers.addAll(helpers);
                } catch (IllegalActionException ex) {
                    // This means the expression is not parse-able.
                    // FIXME: So, we will discard it for now.
                    throw new AssertionError(ex.stackTraceToString(ex));
                }
            }
        }
        return astHelpers;
    }

    /**
     * Return the list of parse trees for all settable Attributes
     * of the component. 
     * @return The list of ASTPtRootNodes.
     */
    protected List<ASTPtRootNode> _getAttributeParseTrees() {
        List<ASTPtRootNode> result = new ArrayList<ASTPtRootNode>();
        
        Iterator attributes = _getPropertyableAttributes().iterator();
        
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
    
            try {
                ASTPtRootNode pt = getParseTree(attribute);
                if (pt != null) {
                    result.add(pt);
                }                
            } catch (IllegalActionException ex) {
                // This means the expression is not parse-able.
                // FIXME: So, we will discard it for now.
                //System.out.println(KernelException.stackTraceToString(ex));
                throw new AssertionError(ex.stackTraceToString(ex));
            }
        }
        return result;
    }
    
    /////////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////

    /** A class that defines a channel object. A channel object is
     *  specified by its port and its channel index in that port.
     */
    public static class Channel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct the channel with the given port and channel number.
         * @param portObject The given port.
         * @param channel The channel number of this object in the given port.
         */
        public Channel(IOPort portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }

        /**
         * Whether this channel is the same as the given object.
         * @param object The given object.
         * @return True if this channel is the same reference as the given
         *  object, otherwise false;
         */
        public boolean equals(Object object) {
            return object instanceof Channel
            && port.equals(((Channel) object).port)
            && channelNumber == ((Channel) object).channelNumber;
        }

        /**
         * Return the string representation of this channel.
         * @return The string representation of this channel.
         */
        public String toString() {
            return port.getName() + "_" + channelNumber;
        }

        /**
         * Return the hash code for this channel. Implementing this method
         * is required for comparing the equality of channels.
         * @return Hash code for this channel.
         */
        public int hashCode() {
            return port.hashCode() + channelNumber;
        }

        /** The port that contains this channel.
         */
        public IOPort port;

        /** The channel number of this channel.
         */
        public int channelNumber;
    }

    
}
