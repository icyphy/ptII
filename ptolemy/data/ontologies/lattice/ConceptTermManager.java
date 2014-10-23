/** A class representing a concept term factory.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// PropertyTermManager.

/**
 A class representing a property term manager.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ConceptTermManager implements ConceptTermFactory {

    /**
     * Construct a new ptolemy.graph.InequalityTerm factory.
     *
     * @param solver the LatticeOntologySolver that contains this PropertyTermManager
     */
    public ConceptTermManager(LatticeOntologySolver solver) {
        _solver = solver;
    }

    /**
     * Return a list of all the inequality terms contained in the PropertyTermManager.
     *
     * @return The list of inequality terms for all objects in the model
     */
    public List<ptolemy.graph.InequalityTerm> terms() {
        List<ptolemy.graph.InequalityTerm> result = new LinkedList<ptolemy.graph.InequalityTerm>();
        result.addAll(_conceptTerms.values());
        return result;
    }

    /**
     * Return the property term for the given object.
     * If the given object is null or a property term itself,
     * it returns the given object. Otherwise, it checks
     * its cache if a term object was created previously. Returns
     * the property term if it is found; otherwise, it creates
     * and caches a new property term before returning it.
     *
     * @param object The given object.
     * @return The property term.
     */
    @Override
    public ptolemy.graph.InequalityTerm getConceptTerm(Object object) {
        if (object == null || object instanceof ptolemy.graph.InequalityTerm) {
            return (ptolemy.graph.InequalityTerm) object;
        }

        //        if (object instanceof NamedObj) {
        //
        //            // Use the property term for the ParameterPort, if it is connected.
        //            if (object instanceof PortParameter) {
        //                PortParameter parameter = (PortParameter) object;
        //                if (parameter.getPort().numLinks() > 0) {
        //                    return getPropertyTerm(parameter.getPort());
        //                }
        //            }
        //
        //            // The property term for an Attribute is its root ASTNode.
        //            //if (object instanceof Attribute) {
        //            //    ASTPtRootNode node = _solver.getParseTree((Attribute) object);
        //            //    return getPropertyTerm(node);
        //            //}
        //        }

        if (!_conceptTerms.containsKey(object)) {
            _conceptTerms.put(object, new InequalityTerm(object));
        }
        return _conceptTerms.get(object);
    }

    /**
     * Get the list of affected InequalityTerms from the PropertyTermManager.
     * FIXME: Not really sure what this method is used for. It appears to
     * always return an empty ArrayList.
     *
     * @param updateTerm This parameter doesn't appear to be used
     * @return The list of inequality terms that are affected by the OntologySolver
     * @exception IllegalActionException If an exception is thrown
     */
    public List<ptolemy.graph.InequalityTerm> getAffectedTerms(
            ptolemy.graph.InequalityTerm updateTerm)
                    throws IllegalActionException {
        return new ArrayList<ptolemy.graph.InequalityTerm>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The mapping between property-able objects and their ptolemy.graph.InequalityTerm. */
    private HashMap<Object, ptolemy.graph.InequalityTerm> _conceptTerms = new HashMap<Object, ptolemy.graph.InequalityTerm>();

    /** The LatticeOntologySolver that contains this PropertyTermManager. */
    protected LatticeOntologySolver _solver;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /**
     * An InequalityTerm class that is used for ontology analysis and contains
     * a reference to the associated object for the InequalityTerm.
     *
     */
    public class InequalityTerm implements ptolemy.graph.InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** The model object associated with the InequalityTerm. */
        protected Object _object;

        /** Represents whether or not the inequality term should be considered
         *  for the solver. */
        private boolean _isEffective;

        /**
         * Construct an InequalityTerm for the given model object.
         *
         * @param object The model object associated with this InequalityTerm
         */
        protected InequalityTerm(Object object) {
            _object = object;
            _isEffective = true;
        }

        /** Return the model object associated with the InequalityTerm.
         *
         *  @return The associated model object
         */
        @Override
        public Object getAssociatedObject() {
            return _object;
        }

        /** Return null if this term is not effective. Otherwise, return
         *  the resolved property of this ptolemy.graph.InequalityTerm.
         *
         * @return The resolved Concept of this InequalityTerm, or null if this
         * term is not effective
         * @see #setValue(Object)
         */
        @Override
        public Object getValue() {
            //if (_isEffective) {
            return _solver.getConcept(_object);
            // }
            // return null;
        }

        /** Return this ptolemy.graph.InequalityTerm in an array if this term represent
         *  a property variable. This term represents a property variable
         *  if the property of this port is not set through setEquals().
         *  If the property of this port is set, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return new InequalityTerm[0];
        }

        /**
         * Return an array of one element with this InequalityTerm if it is
         * a constant InequalityTerm that cannot be changed.  If it can be changed
         * then return an empty InequalityTerm array.
         *
         * @return The InequalityTerm array with either one or zero elements
         * that is returned.
         */
        public InequalityTerm[] getConstants() {
            if (!isSettable()) {
                InequalityTerm[] constant = new InequalityTerm[1];
                constant[0] = this;
                return constant;
            }

            return new InequalityTerm[0];
        }

        /** Reset the variable part of this type to the specified type.
         *  @param property A Type.
         *  @exception IllegalActionException If the type is not settable,
         *   or the argument is not a Type.
         */
        @Override
        public void initialize(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "ptolemy.graph.InequalityTerm.initialize: "
                                + "Cannot initialize a constant property.");
            }

            if (!(property instanceof Concept)) {
                throw new IllegalActionException(
                        "ptolemy.graph.InequalityTerm.initialize: "
                                + "The argument is not a Concept.");
            }

            _solver.setConcept(_object, (Concept) property);
        }

        /**
         * Return true if the InequalityTerm is an effective constraint for
         * the OntologySolver, and false otherwise. Effective means the constraint
         * will be used by the OntologySolver when it runs its algorithm.  If it
         * is not effective, the constraint will not be used by the OntologySolver.
         *
         * @return true if the InequalityTerm is effective, false otherwise
         */
        public boolean isEffective() {
            return _isEffective;
        }

        /** Test if the property of the port associated with this Term
         *  can be changed. The property can be changed if setEquals()
         *  is not called.
         *
         *  @return True if the property can be changed; false otherwise.
         */
        @Override
        public boolean isSettable() {
            return _solver.isSettable(_object);
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *
         *  @return True if the current value is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            Concept property = (Concept) getValue();

            if (property == null) {
                return true;
            }
            if (property.isValueAcceptable()) {
                return true;
            }

            return false;
        }

        /**
         * Sets whether the InequalityTerm constraint will be effective for
         * the OntologySolver's algorithm.
         *
         * @param isEffective true if the InequalityTerm should be effective,
         * false if it should be ineffective
         * @see #isEffective
         */
        public void setEffective(boolean isEffective) {
            _isEffective = isEffective;
        }

        /** Set the property value of this term.
         *
         *  @param property The given property.
         *  @exception IllegalActionException If the new type violates
         *   the declared property of this port.
         *  @see #getValue()
         */
        @Override
        public void setValue(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "The property is not settable.");
            }

            _solver.setConcept(_object, (Concept) property);
        }

        /** Override the base class to give a description of the port
         *  and its property.
         *  @return A description of the port and its property.
         */
        @Override
        public String toString() {

            //return "( " + _object.hashCode() + "--" + hashCode() +
            //" " + _object.toString() + ", " + getValue() + ")";
            return "(" + _object.toString() + ", " + getValue() + ")";
        }

    }
}
