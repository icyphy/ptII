/** An inequality over a CPO.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (yuhong@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.graph;

//////////////////////////////////////////////////////////////////////////
//// Inequality
/**
An inequality over a CPO.
Each inequality consists of two <code>InequalityTerms</code>, the lesser
term and the greater term. The relation between them is <i>less than or
equal to</i>.  In addition, an inequality keeps a list of variables in it.
The variables are <code>InequalityTerms</code> that consist of a single
variable.

@author Yuhong Xiong
@version $Id$
@see InequalityTerm
*/

public class Inequality {

    /** Construct an inequality.
     *  @param lesserTerm an <code>InequalityTerm</code> that is less than or
     *   equal to the second argument.
     *  @param greaterTerm an <code>InequalityTerm</code> that is greater than
     *   or equal to the first argument.
     *  @exception IllegalArgumentException If the <code>lesserTerm</code> or
     *   the <code>greaterTerm</code> is <code>null</code>.
     */
    public Inequality(InequalityTerm lesserTerm, InequalityTerm greaterTerm) {
	if (lesserTerm == null || greaterTerm == null) {
	    throw new IllegalArgumentException("Inequality.Inequality: " +
                    "lesserTerm or greaterTerm is null.");
	}

        _lesserTerm = lesserTerm;
        _greaterTerm = greaterTerm;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the greater term of this inequality.
     *  @return an <code>InequalityTerm</code>
     */
    public InequalityTerm getGreaterTerm() {
        return _greaterTerm;
    }

    /** Return the lesser term of this inequality.
     *  @return an <code>InequalityTerm</code>
     */
    public InequalityTerm getLesserTerm() {
        return _lesserTerm;
    }

    /** Test if this inequality is satisfied with the current value
     *  of variables.
     *  @param cpo a CPO over which this inequality is defined.
     *  @return <code>true</code> if this inequality is satisfied;
     *   <code>false</code> otherwise.
     */
    public boolean isSatisfied(CPO cpo) {
        int result = cpo.compare(_lesserTerm.getValue(),
                _greaterTerm.getValue());
        return (result == CPO.LOWER || result == CPO.SAME);
    }

    /** Override the base class to describe the inequality.
     *  @return A string describing the inequality.
     */
    public String toString() {
        return _lesserTerm.toString() + " <= " + _greaterTerm.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private InequalityTerm _lesserTerm = null;
    private InequalityTerm _greaterTerm = null;
}
