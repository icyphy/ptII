/** A base class representing a property constraint helper.

 Copyright (c) 1997-2008 The Regents of the University of California.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.properties.ParseTreeASTNodeHelperCollector;
import ptolemy.data.properties.ParseTreeNodeCollector;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

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
     * @exception IllegalActionException Thrown if 
     *  PropertyConstraintHelper(NamedObj, PropertyLattice, boolean)
     *  throws it. 
     */
    public PropertyConstraintHelper(PropertySolver solver, Object component)
            throws IllegalActionException {
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
     * @exception IllegalActionException Thrown if the helper cannot
     *  be initialized.
     */
    public PropertyConstraintHelper(PropertySolver solver, Object component,
            boolean useDefaultConstraints) throws IllegalActionException {

        _component = component;
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
    public List constraintList() throws IllegalActionException {
        List constraints = new ArrayList();
        constraints.addAll(_constraints);

        /*
        Iterator astHelpers = _getASTNodeHelpers().iterator();
        
        while (astHelpers.hasNext()) {
            PropertyConstraintASTNodeHelper helper = 
                (PropertyConstraintASTNodeHelper) astHelpers.next();
            constraints.addAll(helper.constraintList());
        }
        //*/
        Iterator parseTrees = _getAttributeParseTrees().iterator();
        ParseTreePropertyConstraintCollector collector = new ParseTreePropertyConstraintCollector();

        while (parseTrees.hasNext()) {
            ASTPtRootNode root = (ASTPtRootNode) parseTrees.next();

            constraints.addAll(collector.collectConstraints(root,
                    (NamedObj) _component, getSolver()));
        }

        AtomicActor actor = (AtomicActor) _component;

        boolean constraintSource = (interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET)
                || (interconnectConstraintType == ConstraintType.SRC_LESS);

        // Mark all the disconnected ports.
        _nonConstraintings.clear();
        Iterator ports = actor.portList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            if (port.numLinks() <= 0) {
                _nonConstraintings.add(port);
            }
        }

        List portList1 = (constraintSource) ? actor.outputPortList() : actor
                .inputPortList();

        ports = portList1.iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            if (!_nonConstraintings.contains(port)) {
                // Eliminate duplicates.
                Set portList2 = (constraintSource) ? new HashSet(
                        _getSinkPortList(port)) : new HashSet(
                        _getSourcePortList(port));

                portList2.removeAll(_nonConstraintings);

                constraints.addAll(_constraintObject(
                        interconnectConstraintType, port, portList2));
            }
        }

        Set removeConstraints = new HashSet();

        Iterator inequalities = _constraints.iterator();
        while (inequalities.hasNext()) {
            Inequality inequality = (Inequality) inequalities.next();
            List variables = _deepGetVariables(inequality.getGreaterTerm()
                    .getVariables());

            variables.addAll(_deepGetVariables(inequality.getLesserTerm()
                    .getVariables()));

            Iterator iterator = variables.iterator();

            while (iterator.hasNext()) {
                InequalityTerm term = (InequalityTerm) iterator.next();
                if (_nonConstraintings.contains(term.getAssociatedObject())) {
                    removeConstraints.add(inequality);
                }
            }
        }
        constraints.removeAll(removeConstraints);

        return constraints;
    }

    protected List _getSinkPortList(IOPort port) {
        List result = new ArrayList();

        Iterator iterator = port.connectedPortList().iterator();

        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();

            boolean isInput = connectedPort.isInput();
            boolean isCompositeOutput = (connectedPort.getContainer() instanceof CompositeEntity)
                    && !isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (isInput || isCompositeOutput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    protected List _getSourcePortList(IOPort port) {
        List result = new ArrayList();

        Iterator iterator = port.connectedPortList().iterator();

        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();
            boolean isInput = connectedPort.isInput();
            boolean isCompositeInput = (connectedPort.getContainer() instanceof CompositeEntity)
                    && isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (!isInput || isCompositeInput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    /**
     * Return the property term from the given object.
     * @param object The given object.
     * @return The property term of the given object.
     * @exception IllegalActionException 
     */
    public InequalityTerm getPropertyTerm(Object object)
            throws IllegalActionException {
        if (object instanceof InequalityTerm) {
            return (InequalityTerm) object;
        }

        if (object instanceof NamedObj) {
            NamedObj container = ((NamedObj) object).getContainer();

            // First, make sure that this is the right helper.
            if (container != _component) {
                return getSolver().getHelper(container).getPropertyTerm(object);
            }

            // Use the property term for the ParameterPort, if it is connected.
            if (object instanceof PortParameter) {
                PortParameter parameter = (PortParameter) object;
                if (parameter.getPort().numLinks() > 0) {
                    return getPropertyTerm(parameter.getPort());
                }
            }

            // The property term for an Attribute is its root ASTNode.
            if (object instanceof Attribute) {
                ASTPtRootNode node = getParseTree((Attribute) object);
                return getSolver().getHelper(node).getPropertyTerm(node);
            }
        }

        if (!_propertyTerms.containsKey(object)) {
            _propertyTerms.put(object, new PropertyTerm(object));
        }

        return (InequalityTerm) _propertyTerms.get(object);
    }

    /**
     * 
     * @param node
     * @return
     */
    public Attribute getAttribute(ASTPtRootNode node) {
        ASTPtRootNode root = node;
        while (root.jjtGetParent() != null)
            ;
        return _attributes.get(root);
    }

    /**
     * 
     * @param attribute
     * @return
     * @exception IllegalActionException
     */
    public ASTPtRootNode getParseTree(Attribute attribute)
            throws IllegalActionException {
        if (!_parseTrees.containsKey(attribute)) {

            if (_parser == null) {
                _parser = new PtParser();
            }

            String expression = ((Settable) attribute).getExpression();

            ASTPtRootNode parseTree = _parser.generateParseTree(expression);

            _parseTrees.put(attribute, parseTree);
            _attributes.put(parseTree, attribute);
        }
        return _parseTrees.get(attribute);
    }

    public PropertyConstraintSolver getSolver() {
        return (PropertyConstraintSolver) _solver;
    }

    public Inequality setAtLeast(Object object1, Object object2)
            throws IllegalActionException {
        return _setAtLeast(getPropertyTerm(object1), getPropertyTerm(object2),
                true);
    }

    /**
     * Create a constraint that set the port1 property to be at least
     * the port2 property.
     * @param object1 The first given port.
     * @param object2 The second given port.
     * @param lattice The given lattice.
     * @exception IllegalActionException 
     */
    public Inequality setAtLeast(Object object1, Object object2,
            boolean isPermanent) throws IllegalActionException {
        return _setAtLeast(getPropertyTerm(object1), getPropertyTerm(object2),
                isPermanent);
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be same as the given function term.
     * @param object The given port.
     * @param term The given function term.
     * @exception IllegalActionException 
     */
    public List setSameAs(Object object1, Object object2)
            throws IllegalActionException {
        return setSameAs(object1, object2, true);
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be same as the given function term.
     * @param object The given port.
     * @param term The given function term.
     * @exception IllegalActionException 
     */
    public List setSameAs(Object object1, Object object2, boolean isPermanent)
            throws IllegalActionException {
        List inequalities = new ArrayList();
        inequalities.add(setAtLeast(getPropertyTerm(object1),
                getPropertyTerm(object2), isPermanent));
        inequalities.add(setAtLeast(getPropertyTerm(object2),
                getPropertyTerm(object1), isPermanent));
        return inequalities;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /**
     * 
     * @param constraintType
     * @exception IllegalActionException
     */
    protected void _setConnectionConstraintType(ConstraintType constraintType,
            ConstraintType compositeConstraintType,
            ConstraintType expressionASTNodeConstraintType)
            throws IllegalActionException {

        Iterator astHelpers = _getASTNodeHelpers().iterator();

        while (astHelpers.hasNext()) {
            PropertyConstraintASTNodeHelper helper = (PropertyConstraintASTNodeHelper) astHelpers
                    .next();
            helper.interconnectConstraintType = expressionASTNodeConstraintType;
        }

        interconnectConstraintType = constraintType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of permanent property constraints. */
    protected List _constraints = new ArrayList();

    /**
     * The set of property-able objects (i.e. disconnected ports)
     * that has no effects in the constraints.
     */
    protected HashSet _nonConstraintings = new HashSet();

    /** The mapping between property-able objects and their PropertyTerm. */
    protected HashMap _propertyTerms = new HashMap();

    /** Indicate whether this helper uses the default actor constraints. */
    protected boolean _useDefaultConstraints;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    protected class PropertyTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////
        protected Object _object;

        protected PropertyTerm(Object object) {
            _object = object;
        }

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return _object;
        }

        /** Return the type of this TypedIOPort.
         */
        public Object getValue() {
            return getProperty(_object);
        }

        /** Return this PropertyTerm in an array if this term represent
         *  a property variable. This term represents a property variable
         *  if the property of this port is not set through setEquals().
         *  If the property of this port is set, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return (new InequalityTerm[0]);
        }

        /** Reset the variable part of this type to the specified type.
         *  @param property A Type.
         *  @exception IllegalActionException If the type is not settable,
         *   or the argument is not a Type.
         */
        public void initialize(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException("PropertyTerm.initialize: "
                        + "Cannot initialize a constant property.");
            }

            if (!(property instanceof Property)) {
                throw new IllegalActionException("PropertyTerm.initialize: "
                        + "The argument is not a Property.");
            }

            _resolvedProperties.put(_object, property);
        }

        /** Test if the property of the port associated with this Term
         *  can be changed. The property can be changed if setEquals()
         *  is not called.
         *  @return True if the property can be changed; false otherwise.
         */
        public boolean isSettable() {
            return !_nonSettables.contains(_object);
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            if (getProperty(_object).isInstantiable()) {
                return true;
            }

            // For a disconnected port, any property is acceptable.
            if (_object instanceof IOPort) {
                if (((IOPort) _object).numLinks() == 0) {
                    return true;
                }
            }

            return false;
        }

        /** Set the property of this port.
         *  @parameter property The given property.
         *  @exception IllegalActionException If the new type violates
         *   the declared property of this port.
         */
        public void setValue(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "The property is not settable.");
            }

            Property declaredProperty = (Property) _declaredProperties
                    .get(_object);
            if (declaredProperty != null
                    && !declaredProperty
                            .isSubstitutionInstance((Property) property)) {
                throw new IllegalActionException("Property conflict on object "
                        + _object.toString() + ".\n" + "Declared property is "
                        + _declaredProperties.get(_object).toString() + ".\n"
                        + "The connection or property constraints, however, "
                        + "require property " + property.toString());
            }

            _resolvedProperties.put(_object, property);
        }

        /** Override the base class to give a description of the port
         *  and its property.
         *  @return A description of the port and its property.
         */
        public String toString() {
            return "(" + _object.toString() + ", " + getProperty(_object) + ")";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Add the given inequality to the list of constraint.
     * @param inequality The given inequality to be added.
     */
    private void _addConstraint(Inequality inequality) {
        _constraints.add(inequality);
    }

    /**
     * 
     * @param actorConstraintType
     * @exception IllegalActionException
     */
    protected void _changeDefaultConstraints(ConstraintType actorConstraintType)
            throws IllegalActionException {
        if (!_useDefaultConstraints) {
            return;
        }

        _constraints.clear();

        boolean constraintSource = (actorConstraintType == ConstraintType.SRC_EQUALS_MEET)
                || (actorConstraintType == ConstraintType.SRC_LESS);

        List portList1 = (constraintSource) ? ((AtomicActor) _component)
                .inputPortList() : ((AtomicActor) _component).outputPortList();

        List portList2 = (constraintSource) ? ((AtomicActor) _component)
                .outputPortList() : ((AtomicActor) _component).inputPortList();

        Iterator ports = portList1.iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            _constraints.addAll(_constraintObject(actorConstraintType, port,
                    portList2));
        }
    }

    protected List _constraintObject(ConstraintType constraintType,
            Object object, Set objectList) throws IllegalActionException {
        return _constraintObject(constraintType, object, new ArrayList(
                objectList));
    }

    /**
     * @param constraintType
     * @param object
     * @param portList2
     * @exception IllegalActionException
     */
    protected List _constraintObject(ConstraintType constraintType,
            Object object, List objectList) throws IllegalActionException {
        List result = new ArrayList();

        boolean isEquals = (constraintType == ConstraintType.EQUALS)
                || (constraintType == ConstraintType.SINK_EQUALS_MEET)
                || (constraintType == ConstraintType.SRC_EQUALS_MEET);

        boolean useMeetFunction = (constraintType == ConstraintType.SRC_EQUALS_MEET)
                || (constraintType == ConstraintType.SINK_EQUALS_MEET);

        Iterator constraintings = objectList.iterator();

        InequalityTerm term1 = getPropertyTerm(object);

        if (constraintType != ConstraintType.NONE) {
            if (!useMeetFunction) {

                while (constraintings.hasNext()) {
                    Object object2 = constraintings.next();

                    InequalityTerm term2 = getPropertyTerm(object2);

                    if (isEquals) {
                        //setSameAs(port, port2);
                        result.add(new Inequality(term1, term2));
                        result.add(new Inequality(term2, term1));

                    } else {
                        //setAtMost(port, port2);
                        result.add(new Inequality(term1, term2));
                    }
                }
            } else {
                InequalityTerm term2 = new MeetFunction(getSolver(), objectList);

                if (objectList.size() > 0) {
                    //setSameAs(port, new MeetFunction(_lattice, portList2));
                    result.add(new Inequality(term1, term2));
                    result.add(new Inequality(term2, term1));
                }
            }
        }
        return result;
    }

    private List _deepGetVariables(InequalityTerm[] variables) {
        List result = new ArrayList();

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
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    protected List _getPropertyables() {
        List list = new ArrayList();

        // Add all ports.
        list.addAll(((Entity) _component).portList());

        // Add attributes.
        Iterator parseTrees = _getAttributeParseTrees().iterator();

        ParseTreeNodeCollector collector = new ParseTreeNodeCollector();

        while (parseTrees.hasNext()) {
            ASTPtRootNode parseTree = (ASTPtRootNode) parseTrees.next();

            try {
                Set nodes = collector.collectNodes(parseTree);
                list.addAll(nodes);

            } catch (IllegalActionException e) {
                // FIXME:
            }
        }
        return list;
    }

    /**
     * Return the list of parse trees for all settable Attributes
     * of the component. 
     * @return The list of ASTPtRootNodes.
     */
    protected List _getAttributeParseTrees() {
        List result = new ArrayList();

        Iterator attributes = ((Entity) _component).attributeList(
                Attribute.class).iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            // FIXME: We should do some sort of filtering here.            
            if (attribute instanceof Settable) {
                if (((Settable) attribute).getVisibility() == Settable.FULL) {
                    try {
                        result.add(getParseTree(attribute));

                    } catch (IllegalActionException ex) {
                        // This means the expression is not parse-able.
                    }
                }
            }
        }
        return result;
    }

    /**
     * Reinitialize the helper before each round of property resolution.
     * It resets the resolved property of all propertyable objects. This
     * method is called in the beginning of the
     * PropertyConstraintSolver.resolveProperties() method.
     * @exception IllegalActionException Thrown if 
     */
    public void reinitialize() throws IllegalActionException {
        List propertyables = _getPropertyables();

        Iterator iterator = propertyables.iterator();

        while (iterator.hasNext()) {
            Object propertyable = iterator.next();

            if (!_nonSettables.contains(propertyable)) {
                _resolvedProperties.remove(propertyable);
                //_declaredProperties.put(propertyable, _solver.getLattice().getInitialProperty());
            }
        }
    }

    protected List _getASTNodeHelpers() {
        List astHelpers = new ArrayList();
        ParseTreeASTNodeHelperCollector collector = new ParseTreeASTNodeHelperCollector();

        Iterator attributes = ((Entity) _component).attributeList(
                Attribute.class).iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            // FIXME: We should do some sort of filtering here.            
            if (attribute instanceof Settable) {

                ASTPtRootNode node;
                try {
                    node = getParseTree(attribute);
                    Set helpers = collector.collectHelpers(node, getSolver());

                    astHelpers.addAll(helpers);

                } catch (IllegalActionException e) {
                    // This means the expression is not parse-able.
                    // FIXME: So, we will discard it for now.
                }
            }
        }
        return astHelpers;
    }

    /**
     * Create a constraint that set the
     * first term to be at least the second term.
     * @param term1 The greater term.
     * @param term2 The lesser term.
     */
    protected Inequality _setAtLeast(InequalityTerm term1,
            InequalityTerm term2, boolean isPermanent) {
        Inequality inequality = new Inequality(term2, term1);
        if (isPermanent) {
            _addConstraint(inequality);
        }
        return inequality;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * 
     */
    public ConstraintType interconnectConstraintType;

    private Map<Attribute, ASTPtRootNode> _parseTrees = new HashMap<Attribute, ASTPtRootNode>();

    private Map<ASTPtRootNode, Attribute> _attributes = new HashMap<ASTPtRootNode, Attribute>();

    static PtParser _parser;

}
