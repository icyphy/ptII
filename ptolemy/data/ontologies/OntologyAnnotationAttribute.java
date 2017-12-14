/*
 * An annotation specifying a single ontology constraints in a model.
 *
 * Below is the copyright agreement for the Ptolemy II system.
 *
 * Copyright (c) 2008-2013 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.ontologies;

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

/**
 * An annotation specifying a single ontology constraints in a model.
 *
 * The name of the ontology is specified as a StringAttribute, but it
 * will also fall back to a name-convention based method for backward
 * compatibility.
 *
 * @author Ben Lickly, Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class OntologyAnnotationAttribute extends StringAttribute {

    /** Construct an OntologyAnnotationAttribute with the specified name
     *  and container.
     *  If a reasonable default exists, initialize the ontology solver name,
     *  as well.
     *  @param container Container
     *  @param name The given name for the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public OntologyAnnotationAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        ontologySolverName = new StringAttribute(this, "ontologySolverName");
        _initializeSolverName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Name of the OntologySolver to which this annotation belongs.
     */
    public StringAttribute ontologySolverName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the name of the ontology solver for which this annotation
     *  attribute is a constraint.
     *
     *  First try to look in the {@link #ontologySolverName} attribute, and
     *  then fall back to the naming convention based on the annotation name.
     *
     *  @return A String representing the name of the referred ontology solver.
     *  @exception IllegalActionException If an OntologySolver name cannot
     *   be determined.
     */
    public String getOntologySolverIdentifier() throws IllegalActionException {
        String solverName = ontologySolverName.getExpression();
        if (solverName != null && !solverName.isEmpty()) {
            return solverName;
        }
        solverName = _getIdentifierFromNamingConvention();
        if (solverName != null && !solverName.isEmpty()) {
            return solverName;
        }
        throw new IllegalActionException(this,
                "Cannot determine " + OntologySolver.class.getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If there is only one OntologySolver in the model, initialize the
     *  annotation to refer to that solver.
     *  @exception IllegalActionException If the {@link #ontologySolverName}
     *   attribute cannot be modified.
     */
    private void _initializeSolverName() throws IllegalActionException {
        String solverName = "";
        List<OntologySolver> solvers = toplevel()
                .attributeList(OntologySolver.class);
        if (solvers != null && solvers.size() == 1) {
            solverName = solvers.get(0).getDisplayName();
        }
        ontologySolverName.setExpression(solverName);
    }

    /** Return the OntologySolver using the naming-convention method.
     *  @return A String representing the name of the referred ontology solver.
     */
    private String _getIdentifierFromNamingConvention() {
        String[] tokens = getName().split("::");

        if (tokens.length == 2) {
            return tokens[0];
        }

        return null;
        //        throw new IllegalActionException(
        //                "Invalid ontology annotation attribute name: " + getName()
        //                        + ". (should have form ONTOLOGY_SOLVER_NAME::LABEL)");
    }

}
