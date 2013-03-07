/**
 * A composite entity that is the model contained by an OntologySolver.
 *
 * Copyright (c) 2007-2013 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
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
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 *
 */
package ptolemy.data.ontologies;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// OntologySolverModel

/**
 * A composite entity that is the model contained by an OntologySolver.
 *
 * @see OntologySolver
 * @author Charles Shelton
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cshelton)
 * @Pt.AcceptedRating Red (cshelton)
 */
public class OntologySolverModel extends CompositeEntity {

    /** Create a new OntologySolverModel with the specified name and container.
     *  @param container The container for the solver model.
     *  @param name The name for the ontology solver model.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the container already contains an
     *   entity with the specified name.
     */
    public OntologySolverModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _solverContainer = null;
    }

    /** Construct an OntologySolverModel in the specified workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public OntologySolverModel(Workspace workspace) {
        super(workspace);
        _solverContainer = null;
    }

    /** Create a new OntologySolverModel with the specified workspace and the specified
     *  OntologySolver.
     *  @param solver The OntologySolver that contains the model.
     *  @param workspace The workspace that will list the entity.
     */
    public OntologySolverModel(OntologySolver solver, Workspace workspace) {
        super(workspace);
        _solverContainer = solver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the OntologySolver that contains the model.
     *  @return The OntologySolver that contains this model.
     *  @see #setContainerSolver
     */
    public OntologySolver getContainerSolver() {
        return _solverContainer;
    }

    /** Set the ontology solver that contains this model.
     *  @param solver The ontology solver that should contain this model.
     *  @see #getContainerSolver
     */
    public void setContainerSolver(OntologySolver solver) {
        _solverContainer = solver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The OntologySolver that contains this model */
    private OntologySolver _solverContainer;
}
