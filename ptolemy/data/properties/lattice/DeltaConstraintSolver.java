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

package ptolemy.data.properties.lattice;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyResolutionException;
import ptolemy.data.properties.lattice.PropertyConstraintHelper.Inequality;
import ptolemy.kernel.util.IllegalActionException;
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
public class DeltaConstraintSolver extends PropertyConstraintSolver {

    /** Constructs a DeltaConstraintSolver with the given name
     *  contained by the specified entity. 
     * 
     *  @param container  T che container.
     *  @param name       The name of this DeltaConstraintSolver
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public DeltaConstraintSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Resolve the property values for the toplevel entity that contains this
     *  solver, given the model analyzer that invokes this.
     *  @param analyzer The given model analyzer.
     *  @exception KernelException If the superclass throws it.
     */
    protected void _resolveProperties(NamedObj analyzer) throws KernelException {

        NamedObj toplevel = _toplevel();
        PropertyConstraintHelper toplevelHelper = (PropertyConstraintHelper) getHelper(toplevel);

        toplevelHelper.reinitialize();

        toplevelHelper
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelHelper._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));

        // Collect and solve type constraints.
        List<Inequality> constraintList = toplevelHelper.constraintList();
        
        if (_resolvePropertiesHasErrors(toplevel, toplevelHelper,
                constraintList)) {
            //Only do delta iteration when an error is found.
            _doDeltaIteration(toplevel, toplevelHelper, constraintList);
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
            PropertyConstraintHelper toplevelHelper,
            List<Inequality> constraintList) throws TypeConflictException,
            PropertyResolutionException, IllegalActionException {

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
                        blockSize = Math.min(errorList.size()/2, blockSize);
                        continue WHILE_LOOP;
                    }
                }
            }

            blockSize /= 2;
            System.err.println("Blocksize " + blockSize);
        }

        System.out.println(errorList);
        _resolvedProperties.clear();
        _resolveProperties(toplevel, toplevelHelper, errorList);
    }

    /** Resolve the properties of the given top-level container,
     *  subject to the given constraint list, and then check if
     *  the resulting solution has errors.
     * 
     * @param toplevel The top-level container
     * @param toplevelHelper Must be toplevel.getHelper()
     * @param constraintList The constraint list that we are solving
     * @return True If the found solution has errors.
     * @exception IllegalActionException 
     */
    protected boolean _resolvePropertiesHasErrors(NamedObj toplevel,
            PropertyConstraintHelper toplevelHelper,
            List<Inequality> constraintList) throws IllegalActionException {
        boolean errorOccured = false;
        try {
            super._resolveProperties(toplevel, toplevelHelper, constraintList);
        } catch (TypeConflictException ex) {
            // Thrown in case of conflict in inequalities.
            errorOccured = true;
        }
        // Check for unacceptable solution properties.
        for (Object propertyable : getAllPropertyables()) {
            Property property = getProperty(propertyable);
            if (property != null && !property.isAcceptableSolution()) {
                errorOccured = true;
            }
        }
        return errorOccured;
    }
}
