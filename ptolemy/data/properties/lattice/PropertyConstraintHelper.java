/** A base class representing a property constraint helper.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.modal.ModalModel;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// PropertyConstraintHelper

/**
 A base class representing a property constraint helper.

 @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintHelper extends PropertyHelper {

    /** 
     * Construct the property constraint helper associated
     * with the given component.
     * @param solver TODO
     * @param component The associated component.
     * @throws IllegalActionException Thrown if 
     *  PropertyConstraintHelper(NamedObj, PropertyLattice, boolean)
     *  throws it. 
     */
    public PropertyConstraintHelper(PropertyConstraintSolver solver, Object component) throws IllegalActionException {
        this(solver, component, true);
    }
    
    /**
     * Construct the property constraint helper for the given
     * component and property lattice.
     * @param solver TODO
     * @param component The given component.
     * @param useDefaultConstraints Indicate whether this helper
     *  uses the default actor constraints. 
     * @param solver The given property lattice.
     * @throws IllegalActionException Thrown if the helper cannot
     *  be initialized.
     */
    public PropertyConstraintHelper(PropertyConstraintSolver solver, Object component, boolean useDefaultConstraints)
            throws IllegalActionException {
        
        setComponent(component);
        _useDefaultConstraints = useDefaultConstraints;
        _solver = solver;
        
        //if (!useDefaultConstraints) {
        //    _reinitialize();
        //}
    }

    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        _setEffectiveTerms();
        
        boolean constraintSource = 
            (interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (interconnectConstraintType == ConstraintType.SRC_EQUALS_GREATER);

        List portList1 = _getConstraintedPorts(constraintSource);

        Iterator ports = portList1.iterator();
        
        while (ports.hasNext()) {                    
            TypedIOPort port = (TypedIOPort) ports.next();

            List portList2 = _getConstraintingPorts(constraintSource, port);

            _constraintObject(interconnectConstraintType, port, portList2);
        }
        
        _constraintAttributes();

        _addSubHelperConstraints();
        
        return _union(_ownConstraints, _subHelperConstraints);
    }

    /*
    private void _removeConstraints() {
        Set<Inequality> removeConstraints = new HashSet<Inequality>();
        
        Iterator inequalities = _constraints.iterator();
        while (inequalities.hasNext()) {                    
            Inequality inequality = (Inequality) inequalities.next();
            List<InequalityTerm> variables = 
                _deepGetVariables(inequality.getGreaterTerm().getVariables());
            
            variables.addAll(
                    _deepGetVariables(inequality.getLesserTerm().getVariables()));

            Iterator iterator = variables.iterator();
            
            while (iterator.hasNext()) {
                InequalityTerm term = (InequalityTerm) iterator.next();
                if (nonConstraintings.contains(term.getAssociatedObject())) {
                    removeConstraints.add(inequality);
                }
            }
        }
        _constraints.removeAll(removeConstraints);
    }
*/
    protected void _setEffectiveTerms() {
        Entity actor = (Entity) getComponent();

        Iterator ports = actor.portList().iterator();        
        while (ports.hasNext()) {                    
            TypedIOPort port = (TypedIOPort) ports.next();
            if (port.numLinks() <= 0) {
                (getPropertyTerm(port)).setEffective(false);
            }
        }
    }

    /**
     * @param constraintSource
     * @param port
     * @return
     */
    protected List _getConstraintingPorts(boolean constraintSource, TypedIOPort port) {
        List portList2 = (constraintSource) ? 
                _getSinkPortList(port) : _getSourcePortList(port);
        return portList2;
    }

    /**
     * @param actor
     * @param constraintSource
     * @return
     */
    protected List _getConstraintedPorts(boolean constraintSource) {
        Actor actor = (Actor) getComponent();
        List portList1 = (constraintSource) ?
                actor.outputPortList() : actor.inputPortList();
        return portList1;
    }

    protected void _constraintAttributes() {
        
        Iterator attributes = _getPropertyableAttributes().iterator();
        
        while (attributes.hasNext()) {

            Attribute attribute = (Attribute) attributes.next();

            try {

                ASTPtRootNode node = getParseTree(attribute);
                
                PropertyConstraintHelper astHelper = 
                    (PropertyConstraintHelper) _solver.getHelper(node);
                
                List list = new ArrayList();
                list.add(node);

                _constraintObject(astHelper.interconnectConstraintType, attribute, list);
                //setSameAs(attribute, getParseTree(attribute));
//                setAtLeast(attribute, getParseTree(attribute));
                
            } catch (IllegalActionException ex) {
                ex.printStackTrace();
                System.out.print(ex);
                // This means the expression is not parse-able.
            }
        }
    }

    /**
     * @param constraints
     * @throws IllegalActionException
     */
    protected void _addSubHelperConstraints() throws IllegalActionException {
        Iterator helpers = _getSubHelpers().iterator();
        
        while (helpers.hasNext()) {
            PropertyConstraintHelper helper = 
                (PropertyConstraintHelper) helpers.next();
            _subHelperConstraints.addAll(helper.constraintList());
        }
    }
    
    protected List<Port> _getSinkPortList(IOPort port) {
        List<Port> result = new ArrayList<Port>();
        
        Iterator iterator = port.connectedPortList().iterator();
        
        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();
            
            boolean isInput = connectedPort.isInput();
            boolean isCompositeOutput = 
                (connectedPort.getContainer() instanceof CompositeEntity)
                && !isInput &&            
                port.depthInHierarchy() > connectedPort.depthInHierarchy();            
            
            if (isInput || isCompositeOutput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    
    protected List<Port> _getSourcePortList(IOPort port) {
        List<Port> result = new ArrayList<Port>();
        
        Iterator iterator = port.connectedPortList().iterator();
        
        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();
            boolean isInput = connectedPort.isInput();
            boolean isCompositeInput = 
                (connectedPort.getContainer() instanceof CompositeEntity)
                && isInput &&
                port.depthInHierarchy() > connectedPort.depthInHierarchy();            

            if (!isInput || isCompositeInput) {
                result.add(connectedPort);
            }
        }
        return result;
    }
    protected static List<Inequality> _union(
            List<Inequality> list1, List<Inequality> list2) {
        
        List<Inequality> result = new LinkedList<Inequality>();
        result.addAll(list1);
        result.addAll(list2);
        return result;
    }
    
    public PropertyConstraintSolver getSolver() {
        return (PropertyConstraintSolver) _solver;
    }
    
    public void setAtLeast(Object object1, Object object2) {
        _setAtLeast(getPropertyTerm(object1), getPropertyTerm(object2), true);        
    }

    public void setAtLeast(Object object1, Object object2, boolean isBase) {
        _setAtLeast(getPropertyTerm(object1), getPropertyTerm(object2), isBase);        
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be same as the given function term.
     * @param object The given port.
     * @param term The given function term.
     * @throws IllegalActionException 
     */
    public void setSameAs(Object object1, Object object2) {
        setAtLeast(object1, object2);
        setAtLeast(object2, object1);
    }
     
    /**
     * 
     * @param object
     * @return
     */
    public List<PropertyTerm> getConstraintingTerms(Object object) {
        return (List<PropertyTerm>) getSolver().getConstraintManager()
        .getConstraintingTerms(object);
    }

    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();
        
        // Add all ports.
        list.addAll(((Entity) getComponent()).portList());
        
        // Add attributes.
        list.addAll(_getPropertyableAttributes());
        
        return list;
    }
    
    public PropertyTerm getPropertyTerm(Object object) {
        return getSolver().getPropertyTerm(object);
    }
    
    public Object getPropertyTermValue(Object object) {
        return getPropertyTerm(object).getValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /**
     * 
     * @param constraintType
     * @throws IllegalActionException
     */
    protected void _setConnectionConstraintType(
            ConstraintType constraintType, 
            ConstraintType compositeConstraintType,
            ConstraintType fsmConstraintType, 
            ConstraintType expressionASTNodeConstraintType)
            throws IllegalActionException {

        Iterator helpers = _getSubHelpers().iterator();
        
        while (helpers.hasNext()) {
            PropertyConstraintHelper helper = 
                (PropertyConstraintHelper) helpers.next();

            helper._setConnectionConstraintType(
                    constraintType, compositeConstraintType, 
                    fsmConstraintType, expressionASTNodeConstraintType);        
        }
        
        if (getComponent() instanceof ASTPtRootNode) {

            interconnectConstraintType = expressionASTNodeConstraintType;
            
        } else if (getComponent() instanceof ModalModel || 
                getComponent() instanceof FSMActor) {
            
            interconnectConstraintType = fsmConstraintType;
            
        } else if (getComponent() instanceof CompositeEntity) {
            
            interconnectConstraintType = compositeConstraintType;

        } else {
            interconnectConstraintType = constraintType;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of permanent property constraints. */
    protected List<Inequality> _subHelperConstraints = new LinkedList<Inequality>();
    protected List<Inequality> _ownConstraints = new LinkedList<Inequality>();
    
    /** Indicate whether this helper uses the default actor constraints. */
    protected boolean _useDefaultConstraints;

    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * 
     * @param actorConstraintType
     * @throws IllegalActionException
     */
    protected void _addDefaultConstraints(
            ConstraintType actorConstraintType) throws IllegalActionException {
        if (!_useDefaultConstraints || 
                !AtomicActor.class.isInstance(getComponent())) {
            return;
        }

        boolean constraintSource = 
            (actorConstraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (actorConstraintType == ConstraintType.SRC_EQUALS_GREATER);

        List<Object> portList1 = (constraintSource) ?
                ((AtomicActor) getComponent()).inputPortList() :
                ((AtomicActor) getComponent()).outputPortList();

        List<Object> portList2 = (constraintSource) ?
                ((AtomicActor) getComponent()).outputPortList() :
                ((AtomicActor) getComponent()).inputPortList();
                
        Iterator ports = portList1.iterator();
        
        while (ports.hasNext()) {                    
            IOPort port = (IOPort) ports.next();                    
            _constraintObject(actorConstraintType, port, portList2);
        }
    }

    protected void _constraintObject(
        ConstraintType constraintType, Object object, Set<Object> objectList) 
            throws IllegalActionException {
        _constraintObject(constraintType, object, new ArrayList<Object>(objectList));        
    }
    
    /**
     * @param constraintType
     * @param object
     * @param portList2
     * @throws IllegalActionException
     */
    protected void _constraintObject(
        ConstraintType constraintType, 
        Object object, List<Object> objectList) 
            throws IllegalActionException {
        
        boolean isEquals = 
            (constraintType == ConstraintType.EQUALS) ||  
            (constraintType == ConstraintType.SINK_EQUALS_MEET) ||  
            (constraintType == ConstraintType.SRC_EQUALS_MEET);         
        
        boolean useMeetFunction = 
            (constraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (constraintType == ConstraintType.SINK_EQUALS_MEET);

        Iterator constraintings = objectList.iterator();

        InequalityTerm term1 = getPropertyTerm(object);

        if (constraintType != ConstraintType.NONE) {
            if (!useMeetFunction) {
    
                while (constraintings.hasNext()) {
                    Object object2 = constraintings.next();
    
                    InequalityTerm term2 = getPropertyTerm(object2);
                    
                    if (isEquals) {
                        setSameAs(term1, term2);
                        
                    } else {
// Begin Change Thomas, 04/10/2008
                        if (object2 instanceof ASTPtRootNode) {
                            if (constraintType == ConstraintType.SRC_EQUALS_GREATER) {
                                setAtLeast(term1, term2);                                
                            } else {
                                setAtLeast(term2, term1);
                            }
                        } else {
                            setAtLeast(term1, term2);
                        }
// End Change Thomas, 04/10/2008
                    } 
                }
            } else {
                if (objectList.size() > 0) {
                    InequalityTerm term2 = new MeetFunction(getSolver(), objectList);
                    setSameAs(term1, term2);
                }
            }
        }
    }
    
    private List<InequalityTerm> _deepGetVariables(InequalityTerm[] variables) {
        List<InequalityTerm> result = new ArrayList<InequalityTerm>();
        
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getAssociatedObject() != null) {

                result.addAll(Arrays.asList(variables[i].getVariables()));                
            } else {
                
                result.addAll(_deepGetVariables(variables[i].getVariables()));
            }
        }
        return result;
    }

    /**
     * 
     * @return
     * @throws IllegalActionException
     */
    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        return _getASTNodeHelpers();
    }

    /**
     * Create a constraint that set the
     * first term to be at least the second term.
     * @param term1 The greater term.
     * @param term2 The lesser term.
     */
    protected void _setAtLeast(PropertyTerm term1, PropertyTerm term2, boolean isBase) {
        if (term1 != null && term2 != null) {
            _ownConstraints.add(new Inequality(term2, term1, isBase));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * 
     */
    public ConstraintType interconnectConstraintType;

    
    public class Inequality extends ptolemy.graph.Inequality {

        public Inequality(PropertyTerm lesserTerm, 
                PropertyTerm greaterTerm, boolean isBase) {
            super(lesserTerm, greaterTerm);
            
            _isBase = isBase;
            _helper = PropertyConstraintHelper.this; 
        }

        public PropertyHelper getHelper() {
            return _helper;
        }
        
        /**
         * Return true if this inequality is composable; otherwise, false.
         * @return Whether this inequality is composable.
         */
        public boolean isBase() {
            return _isBase;
        }
        
        /** Test if this inequality is satisfied with the current value
         *  of variables.
         *  @param cpo A CPO over which this inequality is defined.
         *  @return True if this inequality is satisfied;
         *  false otherwise.
         *  @exception IllegalActionException If thrown while getting
         *  the value of the terms.
         */
        public boolean isSatisfied(CPO cpo) throws IllegalActionException {
            PropertyTerm lesserTerm = (PropertyTerm) getLesserTerm(); 
            PropertyTerm greaterTerm = (PropertyTerm) getGreaterTerm(); 
            
            if (lesserTerm.isEffective() && greaterTerm.isEffective()) {  
                if (lesserTerm.getValue() == null) {
                    return true;
                } else if (greaterTerm.getValue() == null) {
                    return false;
                }

                return super.isSatisfied(cpo);
            }
            return true;
        }       
        
        /** 
         *  @return A string describing the inequality.
         */
        public String toString() {
            PropertyTerm lesserTerm = (PropertyTerm) getLesserTerm(); 
            PropertyTerm greaterTerm = (PropertyTerm) getGreaterTerm(); 
            
            if (lesserTerm.isEffective() && greaterTerm.isEffective()) {  
                return super.toString();
            }
            return "";
        }
        
        private boolean _isBase;
        
        private PropertyHelper _helper;
    }
}
