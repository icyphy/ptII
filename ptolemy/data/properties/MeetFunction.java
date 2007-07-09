/* An actor that outputs the absolute value of the input.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// MeetFunction

/**
 Produce an output token on each firing with a value that is
 equal to the absolute value of the input. The input can have any
 scalar type. If the input type is not Complex, the output has the
 same type as the input. If the input type is Complex, the output
 type is Double, in which case, the output value is the magnitude
 of the input complex.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.3
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

// This class implements a monotonic function of the input port
// type. The result of the function is the same as the input type
// if is not Complex; otherwise, the result is Double.
public class MeetFunction extends MonotonicFunction {

    // FindBugs suggested making this class a static inner class:
    //
    // "This class is an inner class, but does not use its embedded
    // reference to the object which created it. This reference makes
    // the instances of the class larger, and may keep the reference
    // to the creator object alive longer than necessary. If
    // possible, the class should be made into a static inner class."


    // The constructor takes a port argument so that the clone()
    // method can construct an instance of this class for the
    // input port on the clone.
    public MeetFunction(PropertyConstraintSolver solver, Object[] functionTerms) {
        _solver = solver;
        _functionTerms = functionTerms;
    }

    public MeetFunction(PropertyConstraintSolver solver, List functionTerms) {
        this(solver, functionTerms.toArray());
    }

    public MeetFunction(PropertyConstraintSolver solver, Set functionTerms) {
        this(solver, functionTerms.toArray());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the function result.
     *  @return A Property.
     */
    public Object getValue() throws IllegalActionException {
        Property joinValue = null;
        Property termValue = null;
        

        Iterator iterator = Arrays.asList(_functionTerms).iterator();
        
        while (iterator.hasNext()) {

            Object object = iterator.next();
            
            if (object instanceof MonotonicFunction) {
            
                termValue = (Property) ((MonotonicFunction) object).getValue();

            } else {
                PropertyHelper helper = 
                    _solver.getHelper(object);

                termValue = helper.getProperty(object);                
            }
            
            joinValue = (joinValue == null) ? termValue : 
                _solver.getLattice().greatestLowerBound(joinValue, termValue);
        }
        return joinValue; 
    }

    /** Return the variables in this term. If the property of the input port
     *  is a variable, return a one element array containing the
     *  InequalityTerm of that port; otherwise, return an array of zero
     *  length.
     *  @return An array of InequalityTerm.
     */
    public InequalityTerm[] getVariables() {
        ArrayList<InequalityTerm> result = new ArrayList<InequalityTerm>();
        
        Iterator iterator = Arrays.asList(_functionTerms).iterator();
        while (iterator.hasNext()) {

            Object object = iterator.next();
            
            InequalityTerm term = null;
            
            if (object instanceof InequalityTerm) {

                term = (InequalityTerm) object; 
                
            } else {
                try {
                    PropertyConstraintHelper helper = 
                        (PropertyConstraintHelper) _solver.getHelper(object);

                    term = helper.getPropertyTerm(object);
                    
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException("Helper not found.");
                }
            } 

            if (term.isSettable()) {
                result.add(term);
            }
        }
        
        InequalityTerm[] array = new InequalityTerm[result.size()];
        System.arraycopy(result.toArray(), 0, array, 0, result.size() );
        
        return  array;
    }

    ///////////////////////////////////////////////////////////////
    ////                       private inner variable          ////
    
    private PropertyConstraintSolver _solver;

    private Object[] _functionTerms;

}

 