/*  Thrown when an ontology solver fails to resolve correctly.

 Copyright (c) 2008-2013 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// OntologyResolutionException

/**
 Thrown when an ontology solver fails to resolve correctly.

 @author Man-kit (Jackie) Leung, Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class OntologyResolutionException extends IllegalActionException {

    /** Construct an exception that includes the OntologySolver
     *  that was involved.
     *
     *  @param solver The OntologySolver, which must not be null.
     *  @param detail The message.
     */
    public OntologyResolutionException(OntologySolverBase solver, String detail) {
        this(solver, null, null, detail);
    }

    /** Construct an exception that includes the OntologySolver
     *  that was involved and the cause.
     *  @param solver The OntologySolver, which must not be null.
     *  @param nameable The Nameable object that was involved.
     *  @param detail The message.
     */
    public OntologyResolutionException(OntologySolverBase solver,
            Nameable nameable, String detail) {
        this(solver, nameable, null, detail);
    }

    /** Construct an exception that includes the OntologySolver
     *  that was involved, the Nameable and the cause.
     *  @param solver The OntologySolver, which must not be null.
     *  @param nameable The Nameable object that was involved.
     *  @param cause The cause of this exception, or null if the cause
     *  is not known or nonexistent
     *  @param detail The message.
     */
    public OntologyResolutionException(OntologySolverBase solver,
            Nameable nameable, Throwable cause, String detail) {
        super(solver, nameable, cause, detail);

        assert (solver != null);

        _solver = solver;
    }

    /** Return the OntologySolver with which this exception was
     *  constructed.
     *  @return the solver with which this exception was constructed.
     *  Guaranteed to be non-null.
     */
    public OntologySolverBase getSolver() {
        return _solver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The OntologySolver with which this exception was constructed.
     *  Guaranteed to be non-null.
     */
    private OntologySolverBase _solver;
}
