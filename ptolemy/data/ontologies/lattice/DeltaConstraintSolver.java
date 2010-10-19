/* ConstraintSolver that finds minimal erroneous constraint set on error.

Copyright (c) 2009-2010 The Regents of the University of California.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.OntologyResolutionException;
import ptolemy.data.properties.PropertyResolutionException;
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
* @since Ptolemy II 8.0
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
        
        _identifiedConflicts = new HashMap<Object, FiniteConcept>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Resolve the concept values for the toplevel entity that contains this
     *  solver, given the model analyzer that invokes this.  Then, if some
     *  concepts resolved to unacceptable values, calculate the set of 
     *  inequality terms that cause the unacceptable values.
     *  @exception KernelException If the _resolveProperties method throws it.
     */
    
    public void identifyConflicts() throws KernelException {
        
        // Reset the list of resolved constraints before executing the ontology solver resolution. 
        _clearLists();       

        // FIXME: The code from here to constraintList() doesn't really
        // belong here. The constraintList() method of the Adapter should
        // ensure that the constraint list it returns is valid.
        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = (LatticeOntologyAdapter) getAdapter(toplevel);

        toplevelAdapter.reinitialize();

        toplevelAdapter
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelAdapter._setConnectionConstraintType(
                _getConstraintType(actorConstraintType.stringValue()));

        // Collect and solve type constraints.
        List<Inequality> constraintList = toplevelAdapter.constraintList();
        
        if (_resolvePropertiesHasErrors(toplevel, toplevelAdapter,
                constraintList)) {
            // Only do delta iteration when an error is found.
            _doDeltaIteration(toplevel, toplevelAdapter, constraintList);
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
     * @param toplevel
     * @param toplevelHelper
     * @param constraintList A list of constraints that causes an error
     * @exception TypeConflictException
     * @exception PropertyResolutionException
     * @exception IllegalActionException
     */   
    
    private void _doDeltaIteration(NamedObj toplevel,
            LatticeOntologyAdapter toplevelHelper,
            List<Inequality> constraintList) throws TypeConflictException, IllegalActionException {

        // Save original set _resolvedProperties
        HashMap originalResolvedProperties = new HashMap<Object, FiniteConcept>(_resolvedProperties);
        
        List<Inequality> errorList = constraintList;
        int blockSize = errorList.size() / 2;

        WHILE_LOOP: while (blockSize >= 1) {

            for (int i = 0; i < errorList.size(); i += blockSize) {

                //modify the list of constraints
                List<Inequality> testList = new LinkedList<Inequality>(
                        errorList);
                testList.removeAll(errorList.subList(i, Math.min(errorList
                        .size(), i + blockSize)));

                if (testList.size() > 0) {
                    _resolvedProperties.clear();
                    if (_resolvePropertiesHasErrors(toplevel, toplevelHelper,
                            testList)) {
                        errorList = testList;
                        blockSize = Math.min(errorList.size() / 2, blockSize);
                        continue WHILE_LOOP;
                    }
                }
            }

            blockSize /= 2;
            System.err.println("Blocksize " + blockSize);
        }

        System.out.println(errorList);
        _resolvedProperties.clear();
        // This will store the objects with unacceptable concepts in 
        // _resolvedProperties.  Save to _identifiedConflicts
        _resolveConcepts(toplevel, toplevelHelper, errorList);
        if (_resolvedProperties != null && !_resolvedProperties.isEmpty()){
            _identifiedConflicts = 
                new HashMap<Object, FiniteConcept>(_resolvedProperties);
        } 
        
        // Restore original set _resolvedProperties
        _resolvedProperties = originalResolvedProperties;
    }

    /** Resolve the properties of the given top-level container,
     *  subject to the given constraint list, and then check if
     *  the resulting solution has errors.
     * 
     * @param toplevel The top-level container
     * @param toplevelHelper Must be toplevel.getHelper()
     * @param constraintList The constraint list that we are solving
     * @return True If the found solution has errors.
     * @exception IllegalActionException  If the superclass method getAllPropertyables() throws it
     * @exception OntologyResolutionException  If the superclass method _resolveProperties() throws it
     */
    
    protected boolean _resolvePropertiesHasErrors(NamedObj toplevel,
            LatticeOntologyAdapter toplevelHelper,
            List<Inequality> constraintList) throws IllegalActionException 
    {
        boolean errorOccured = false;
        try {
            super._resolveConcepts(toplevel, toplevelHelper, constraintList);
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
        if (hasUnacceptableTerms())
        {
            errorOccured = true;
        }
                
        return errorOccured;
    }
    
    /** Returns the objects (and their concepts) which are the cause of 
     *  unacceptable concepts in the model.  Can be null.
     *  
     * @return  The objects (and their concepts) which are the cause of 
     *  unacceptable concepts in the model.  Can be null.
     */
    public HashMap<Object, FiniteConcept> getIdentifiedConflicts()
    {
        return _identifiedConflicts;
    }
    
    /** Returns true if unacceptable concepts exist in the model and the 
     *  causing objects have been identified; false otherwise.
     *  
     *  @return True if unacceptable concepts exist in the model and the 
     *  causing objects have been identified; false otherwise.
     */
    public boolean hasIdentifiedConflicts()
    {
        if (_identifiedConflicts != null && !_identifiedConflicts.isEmpty()) {
            return true;
        }
        return false;
    }
    
    /**
     *  A set of objects (and their concepts) which are the cause of 
     *  unacceptable concepts in the model.
     */
    private HashMap<Object, FiniteConcept> _identifiedConflicts;
}
