/** An inequality over a CPO.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package pt.graph;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// Inequality
/**
An inequality over a CPO.
Each inequality consists of two <code>InequalityTerm</code>s, the relation
between them is <i>less than or equal to</i>.  In addition, an inequality
keeps a list of variables in it.  The variables are
<code>Inequalityterm</code>s.

@author Yuhong Xiong
$Id$
@see InequalityTerm
*/

public class Inequality {

    /** Constructs an inequality.
     *  @param lesserTerm an <code>InequalityTerm</code> that is less than or
     *   equal to the <code>greaterTerm</code> term.
     *  @param greaterTerm an <code>InequalityTerm</code> that is greater than
     *   or equal to the <code>lesserTerm</code> term.
     *  @exception IllegalArgumentException <code>lesserTerm</code> or
     *   <code>greaterTerm</code> is <code>null</code>.
     */
    public Inequality(InequalityTerm lesserTerm, InequalityTerm greaterTerm) {
	if (lesserTerm == null || greaterTerm == null) {
	    throw new IllegalArgumentException("Inequality.Inequality: " +
		"lesserTerm of greaterTerm is null.");
	}

        _lesserTerm = lesserTerm;
        _greaterTerm = greaterTerm;
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /*  Adds the specified <code>InequalityTerm</code> to the list of
     *  variabls in this inequality.
     *  @param variable an <code>InequalityTerms</code> representing
     *   a variable in this inequality.
     *  @exception IllegalArgumentException variable is <code>null</code>.
     */
    public void addVariable(InequalityTerm variable) {
	if (variable == null) {
	    throw new IllegalArgumentException("Inequality.addVariable: " +
			"the variable is null.");
	}
	_variables.addElement(variable);
    }

    /** Returns the greater term of this inequality.
     *  @return an <code>InequalityTerm</code>
     */
    public InequalityTerm greaterTerm() {
        return _greaterTerm;
    }
 
    /** Returns the term that is less than the other in this inequality.
     *  @return an <code>InequalityTerm</code>
     */
    public InequalityTerm lesserTerm() {
        return _lesserTerm;
    }

    /** Tests if this inequality is satisfied with the current value
     *  of variables.
     *  @param cpo a CPO over which this inequality is defined.
     *  @return <code>true</code> if this inequality is satisfied;
     *   <code>false</code> otherwise.
     */
    public boolean satisfied(CPO cpo) {
        int result = cpo.compare(_lesserTerm.value(),
				 _greaterTerm.value());
        return (result == CPO.STRICT_LESS || result == CPO.EQUAL);
    }
    
    /** Returns all the variables in this inequality.
     *  @return an array of <code>InequalityTerm</code>s that are 
     *   variables in this inequality.
     */
    public InequalityTerm[] variables() {
	// note: can't use toArray() since return type is InequalityTerm[]
	InequalityTerm[] result = new InequalityTerm[_variables.size()];
	for (int i = 0; i < _variables.size(); i++) {
	    result[i] = (InequalityTerm)_variables.elementAt(i);
	}
	return result;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                       private variables                        ////
    private InequalityTerm _lesserTerm = null;
    private InequalityTerm _greaterTerm = null;

    // list of InequalityTerms that are variables. initialCapacity is 2.
    private Vector _variables = new Vector(2);
}

