/* A class that represents a meet function term.

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
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.data.properties.Property;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.graph.InequalityTerm;

//////////////////////////////////////////////////////////////////////////
//// MeetFunction

/**
 A class that represents the property term of a meet function. 
 A meet function is defined to return the least upper bound values 
 of all its inputs, assuming the inputs are elements from a common
 lattice.   

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.3
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public class MeetFunction extends MonotonicFunction implements PropertyTerm {

    public MeetFunction(PropertyConstraintSolver solver, List<Object> objects) {
        this(solver, objects.toArray());
    }

    public MeetFunction(PropertyConstraintSolver solver, Set<Object> objects) {
        this(solver, objects.toArray());
    }
    
    public MeetFunction(PropertyConstraintSolver solver, Object ... objects) {
        _solver = solver;
        for (Object object : objects) {
            _terms.add(_solver.getPropertyTerm(object));            
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add variables to the meet function.
     * @param variables The list of variables to be added.
     */
    public void addVariables(List<PropertyTerm> variables) {
        _terms.addAll(variables);
    }
    
    
    /** Return the function result.
     *  @return A Property.
     */
    public Object getValue() {
        Property meetValue = null;
        Property termValue = null;
        

        Iterator iterator = _terms.iterator();
        
        while (iterator.hasNext()) {

            PropertyTerm term = (PropertyTerm) iterator.next();
            
            if (term.isEffective()) {
                termValue = (Property) term.getValue();
                
                meetValue = (meetValue == null) ? termValue : 
                    _solver.getLattice().greatestLowerBound(meetValue, termValue);
            }
        }
        return meetValue; 
    }

    /** Return the variables in this term. If the property of the input port
     *  is a variable, return a one element array containing the
     *  InequalityTerm of that port; otherwise, return an array of zero
     *  length.
     *  @return An array of InequalityTerm.
     */
    public InequalityTerm[] getVariables() {
        ArrayList<InequalityTerm> result = new ArrayList<InequalityTerm>();
        
        Iterator iterator = _terms.iterator();
        while (iterator.hasNext()) {

            PropertyTerm term = (PropertyTerm) iterator.next();
            
            if (term.isSettable()) {
                result.addAll(Arrays.asList(term.getVariables()));
            }
        }
        
        InequalityTerm[] array = new InequalityTerm[result.size()];
        System.arraycopy(result.toArray(), 0, array, 0, result.size() );
        
        return  array;
    }
    
    public String toString() {
        String result = "meet(";
        
        Iterator<PropertyTerm> terms = _terms.iterator();
        while (terms.hasNext()) {
            PropertyTerm term = terms.next();
            if (term.isEffective()) {
                result += term;
                break;
            }
        }
        
        while (terms.hasNext()) {
            PropertyTerm term = terms.next();
            if (term.isEffective()) {
                result += " /\\ " + term;
            }
        }
        
        return result + ")";
    }

    public boolean isEffective() {
        Iterator iterator = _terms.iterator();
        
        while (iterator.hasNext()) {

            PropertyTerm term = (PropertyTerm) iterator.next();
            
            if (term.isEffective()) {
                return true;
            }
        }
        return false;
    }

    public void setEffective(boolean isEffective) {
        throw new AssertionError(
                "Cannot set the effectiveness of a MeetFunction term.");
    }

    ///////////////////////////////////////////////////////////////
    ////                       private inner variable          ////
    
    private PropertyConstraintSolver _solver;

    private List<PropertyTerm> _terms = new LinkedList<PropertyTerm>();

}

 