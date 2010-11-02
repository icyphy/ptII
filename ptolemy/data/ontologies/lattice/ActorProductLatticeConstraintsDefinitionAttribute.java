/* Attribute that defines the product lattice ontology solver constraints for an actor.

 Copyright (c) 2010 The Regents of the University of California.
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

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ActorProductLatticeConstraintsDefinitionAttribute

/** Attribute that defines the product lattice ontology solver constraints for an actor.
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ActorProductLatticeConstraintsDefinitionAttribute extends ActorConstraintsDefinitionAttribute {

    /** Construct the ActorProductLatticeConstraintsDefinitionAttribute attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ActorProductLatticeConstraintsDefinitionAttribute(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////
    
    /** String representing that the actor port or attribute should inherit
     *  its constraints from the tuple ontology solvers for the product
     *  lattice ontology solver.
     */
    public static final String INHERIT = "INHERIT";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Get the adapter defined by this attribute.
     *  @param component The model component for which the adapter will be created.
     *  @param solver The product lattice ontology solver for which this adapter is being created.
     *  @return The ActorConstraintsDefinitionAdapter specified by this attribute.
     *  @exception IllegalActionException If the container model's
     *   solver cannot be found or there is a problem initializing the
     *   adapter.
     */
    public ActorProductLatticeConstraintsDefinitionAdapter createAdapter(ComponentEntity component,
            ProductLatticeOntologySolver solver)
            throws IllegalActionException {
        if (!_validateComponentClass(component)) {
            throw new IllegalActionException(this, "The component "
                    + component
                    + " passed in for the adapter is not of class "
                    + actorClassName.getExpression() + ".");
        }
    
        // If the solver is null, throw an exception.
        if (solver == null) {
            throw new IllegalActionException(this, "The OntologySolverModel "
                    + " does not have an associated OntologySolver so no "
                    + " OntologyAdapter can be created.");
        }
    
        // Get the adapter for the actor.
        return new ActorProductLatticeConstraintsDefinitionAdapter(solver, component,
                _constraintTermExpressions);
    }
    
    /** Return true if the actor element is set to inherit its constraints from
     *  the tuple ontologies that comprise the product lattice ontology,
     *  false otherwise.
     *  @param actorElementConstraintExpression The constraint expression
     *   for the actor element.
     *  @return true if the actor element is set to inherit its constraints from
     *   the tuple ontologies that comprise the product lattice ontology,
     *   false otherwise.
     *  @throws IllegalActionException If the constrain expression parameter is null.
     */
    public static boolean areActorElementConstraintsInherited(StringParameter actorElementConstraintExpression)
        throws IllegalActionException {
        if (actorElementConstraintExpression == null) {
            throw new IllegalActionException("The constraint expression for the actor" +
                        " element cannot be null.");
        }
        return actorElementConstraintExpression.getExpression().trim().equals(INHERIT);
    }
}
