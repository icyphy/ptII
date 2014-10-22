/* ConstraintSolver that finds minimal erroneous constraint set on error.

Copyright (c) 2009-2014 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.ontologies.OntologyResolutionException;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DeltaConstraintSolver

/**
 * An implementation of PropertyConstraintSolver that tries to minimize
 * error cases.  It does this by searching for a set of constraints
 * that produce an error but for whom removing any constraint remove
 * the error.
 *
 * @author Ben Lickly, Dai Bui
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (blickly)
 * @Pt.AcceptedRating Red (blickly)
 */

public class DeltaConstraintSolver extends LatticeOntologySolver {

    /** Constructs a DeltaConstraintSolver with the given name
     *  contained by the specified entity.
     *
     *  @param container  The container.
     *  @param name       The name of this DeltaConstraintSolver
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public DeltaConstraintSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Resolve the concept values for the toplevel entity that contains this
     *  solver, given the model analyzer that invokes this.  Then, if some
     *  concepts resolved to unacceptable values, calculate the set of
     *  inequality terms that cause the unacceptable values.
     *  Note:  This has different behavior from resolveConcepts() in the
     *  superclass.  Call the superclass resolveConcepts() to calculate concepts
     *  for all applicable elements in the model.
     *  @exception KernelException If the _resolveProperties method throws it.
     */
    // public void resolveConcepts() throws KernelException {
    public void resolveConflicts() throws KernelException {

        // Reset the list of resolved constraints before executing the ontology solver resolution.
        super.reset();
        super.initialize();
        NamedObj toplevel = _toplevel();

        boolean errorOccurred = false;
        try {
            super.resolveConcepts();
        } catch (KernelException ex) {
            errorOccurred = true;
        }

        if (errorOccurred || hasUnacceptableTerms()) {
            _doDeltaIteration(toplevel, _resolvedConstraintList);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Iterate on the given list of constraints to find a minimal
     * subset that still contains an error.
     *
     * This can be done efficiently in a manner similar to binary search.
     * The pseudocode is as follows:
       <pre>
       errorList = constraintList
       size = errorList.size()/2
       while (size >= 1):
         for (testList of size size in errorList):
           (i.e. errorlist[0:size] , errorlist[size+1, 2*size], ...)
           if _resolveProperties(errorList - testList) == error:
              errorList = errorList - testList;
              size = errorList.size()/2
              continue while loop;
         size = size/2;
       </pre>
     * @param toplevel The toplevel NamedObj of the model.
     * @param constraintList A list of constraints that causes an error
     * @exception TypeConflictException Thrown if there is a type conflict
     *  during the execution of the delta iteration.
     * @exception IllegalActionException Thrown if there is a problem
     *  executing the delta iteration.
     */
    private void _doDeltaIteration(NamedObj toplevel,
            List<Inequality> constraintList) throws TypeConflictException,
            IllegalActionException {

        List<Inequality> errorList = constraintList;
        int blockSize = errorList.size() / 2;

        WHILE_LOOP: while (blockSize >= 1) {
            System.err.println("Blocksize " + blockSize);
            for (int i = 0; i < errorList.size(); i += blockSize) {

                //modify the list of constraints
                List<Inequality> testList = new LinkedList<Inequality>(
                        errorList);
                testList.removeAll(errorList.subList(i,
                        Math.min(errorList.size(), i + blockSize)));

                if (testList.size() > 0) {
                    _clearLists();
                    if (_resolvePropertiesHasErrors(toplevel, testList)) {
                        errorList = testList;
                        blockSize = Math.min(errorList.size() / 2, blockSize);
                        continue WHILE_LOOP;
                    }
                }
            }

            blockSize /= 2;
        }

        System.out.println(errorList);
        _resolvedProperties.clear();

        // This will store the objects with unacceptable concepts in
        // _resolvedProperties.
        _resolveConcepts(toplevel, errorList);

    }

    /** Resolve the properties of the given top-level container,
     *  subject to the given constraint list, and then check if
     *  the resulting solution has errors.
     *
     * @param toplevel The top-level container
     * @param constraintList The constraint list that we are solving
     * @return True If the found solution has errors.
     * @exception IllegalActionException  If the superclass method getAllPropertyables() throws it
     * @exception OntologyResolutionException  If the superclass method _resolveProperties() throws it
     */

    protected boolean _resolvePropertiesHasErrors(NamedObj toplevel,
            List<Inequality> constraintList) throws IllegalActionException {
        boolean errorOccured = false;
        try {
            super._resolveConcepts(toplevel, constraintList);
        } catch (InternalErrorException ex) {
            // Thrown when there is a conflict with the concept resolution.
            errorOccured = true;
        } catch (OntologyResolutionException ex) {
            // Beth - is this still the case?
            // Thrown when there is a conflict .  FIXME:  Should these exceptions be the other way around??
            // An ontology resolution exception here really means that the function is not monotonic or there
            // is some other problem with the way the constraints are specified
            errorOccured = true;
        }

        // Check for unacceptable solution properties.
        if (hasUnacceptableTerms()) {
            errorOccured = true;
        }

        return errorOccured;
    }
}
