/** An algorithm to solve a set of inequality constraints.

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

package ptolemy.graph;
import collections.LinkedList;
import java.util.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// InequalitySolver
/**
An algorithm to solve a set of inequality constraints.

This algorithm is based on J. Rehof and T. Mogensen, "Tractable
Constraints in Finite Semilattices," Third International Static Analysis
Symposium, pp. 285-301, Vol 1145 of Lecture Notes in Computer Science,
Springer, Sept., 1996.<p>

The algorithm in Rehof works for definite inequalities.  This
class does not enforce this requirement.  However, if the inequalities
are not definite, this solver may not be able to find the solution even
when the set of inequalities is satisfiable.  See the above paper for
details.<p>

This solver supports finding both the least and greatest solutions (if
they exist).  It assumes that the CPO passed to the constructor is a
lattice, but it does not verify it.  If the algorithm finds that the
LUB or GLB of some elements does not exist, an Exception is thrown.

@author Yuhong Xiong
$Id$
*/

// Note: To make it easier to reference the above paper, some of the
// private methods and variables in this class have the same names that
// are used in the paper, which may violate the naming convention
// in some cases.

public class InequalitySolver {

    /** Construct an inequality solver.
     *  @param cpo the CPO over which the inequalities are defined.
     */
    public InequalitySolver(CPO cpo) {
        _cpo = cpo;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an <code>Inequality</code> to the set of constraints.
     *  @param ineq an <code>Inequality</code>
     */
    public void addInequality(Inequality ineq) {

	// put ineq. to _Ilist
	Integer indexWrap = new Integer(_Ilist.size());
	Info info = new Info(ineq);
	_Ilist.addElement(info);

        // add var->ineq to Hashtable
	_addToClist(ineq.getLesserTerm().getVariables(), indexWrap);
	_addToClist(ineq.getGreaterTerm().getVariables(), indexWrap);
    }

    /** Return an <code>Enumeration</code> of the variables whose current
     *  values are the bottom of the underline CPO. If none of the
     *  variables have its current value set to the bottom, an empty
     *  <code>Enumeration</code> is returned.
     *  @return an Enumeration of InequalityTerms
     *  @exception InvalidStateException the underline CPO does not have
     *   a bottom element.
     */
    public Enumeration bottomVariables() {
	Object bottom = _cpo.bottom();
	if (bottom == null) {
	    throw new InvalidStateException("InequalitySolver.bottomVariables:"
                    + " The underline CPO does not have a bottom"
                    + " element.");
	}

	LinkedList result = new LinkedList();
	for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
	    InequalityTerm variable = (InequalityTerm)e.nextElement();
	    if (variable.getValue().equals(bottom)) {
		result.insertLast(variable);
	    }
	}
	return result.elements();
    }

    /** Solve the set of inequalities for the least solution.
     *  If the set of inequalities is definite (when solving for the least
     *  solution, definite means that the greater terms of all the
     *  inequalities are either a constant of a single variable.),
     *  this method can always determine satisfiability. In this case, if
     *  the set of inequalities is satisfiable, this method returns
     *  <code>true</code>, and the variables are set to the least solution.
     *  If the set of inequalities is not satisfiable, this method returns
     *  <code>false</code>.
     *  <p>
     *  If the set of inequalities is not definite, this method cannot
     *  always determine satisfiability. In this case, if the set of
     *  inequalities is satisfiable, this method may or may not return
     *  <code>true</code>. If this method returns <code>true</code>,
     *  the variables are set to the least solution. If the set of
     *  inequalities is not satisfiable, this method returns
     *  <code>false</code>.
     *  <p>
     *  In any case, if this method returns <code>false</code>, the
     *  variables are set to the least solution for the subset of
     *  inequalities whose greater terms are a single variable.
     *  See the paper referred in the class document for details.
     *  @return <code>true</code> if a solution for the inequalities is found,
     *   <code>false</code> otherwise.
     *  @IllegalArgumentException the value of some of the terms in the
     *   inequalities is not a CPO element.
     *  @exception InvalidStateException the LUB of some elements does
     *   not exist in the underline CPO.
     */
    public boolean solveLeast() {
	return _solve(true);
    }

    /** Solve the set of inequalities for the greatest solution.
     *  If the set of inequalities is definite (when solving for the greatest
     *  solution, definite means that the lesser terms of all the
     *  inequalities are either a constant of a single variable.),
     *  this method can always determine satisfiability. In this case, if
     *  the set of inequalities is satisfiable, this method returns
     *  <code>true</code>, and the variables are set to the greatest solution.
     *  If the set of inequalities is not satisfiable, this method returns
     *  <code>false</code>.
     *  <p>
     *  If the set of inequalities is not definite, this method cannot
     *  always determine satisfiability. In this case, if the set of
     *  inequalities is satisfiable, this method may or may not return
     *  <code>true</code>. If this method returns <code>true</code>,
     *  the variables are set to the greatest solution. If the set of
     *  inequalities is not satisfiable, this method returns
     *  <code>false</code>.
     *  <p>
     *  In any case, if this method returns <code>false</code>, the
     *  variables are set to the greatest solution for the subset of
     *  inequalities whose lesser terms are a single variable.
     *  See the paper referred in the class document for details.
     *  @return <code>true</code> if a solution for the inequalities is found,
     *   <code>false</code> otherwise.
     *  @IllegalArgumentException the value of some of the terms in the
     *   inequalities is not a CPO element.
     *  @exception InvalidStateException the GLB of some elements does
     *   not exist in the underline CPO.
     */
    public boolean solveGreatest() {
	return _solve(false);
    }

    /** Return an <code>Enumeration</code> of the variables whose current
     *  values are the top of the underline CPO. If none of the
     *  variables have the current value set to the top, an empty
     *  <code>Enumeration</code> is returned.
     *  @return an Enumeration of InequalityTerms
     *  @exception InvalidStateException the underline CPO does not have
     *   a top element.
     */
    public Enumeration topVariables() {
	Object top = _cpo.top();
	if (top == null) {
	    throw new InvalidStateException("InequalitySolver.topVariables:"
                    + " The underline CPO does not have a top element.");
	}

	LinkedList result = new LinkedList();
	for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
	    InequalityTerm variable = (InequalityTerm)e.nextElement();
	    if (variable.getValue().equals(top)) {
		result.insertLast(variable);
	    }
	}
	return result.elements();
    }

    /** Return an <code>Enumeration</code> of <code>Inequalities</code>
     *  that are not satisfied with the current value of variables.
     *  If all the inequalities are satisfied, an empty
     *  <code>Enumeration</code> is returned.
     *  @return an Enumeration of Inequalities
     */
    public Enumeration unsatisfiedInequalities() {
	LinkedList result = new LinkedList();

        for (int i = 0; i < _Ilist.size(); i++) {
	    Info info = (Info)_Ilist.elementAt(i);
            if ( !info._ineq.isSatisfied(_cpo)) {
                result.insertLast(info._ineq);
	    }
        }
	return result.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // Each instance of this class is an entry in _Ilist.
    private class Info {
        private Info(Inequality ineq) {
            _ineq = ineq;
        }
 
        private Inequality _ineq;

	// If this ineq. is in Cvar, i.e., if looking for least solution
	// and greaterTerm is settable, or looking for greatest solution
	// and lesserTerm is settable.
	private boolean _inCvar = false;

	// If this ineq. is in _NS
        private boolean _inserted = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // Add the InequalityTerms in the specified array as keys and the
    // index as value to _Clist.  The InequalityTerms are variables
    // and the index is the index of the Inequality in _Ilist that
    // contains the variables.
    private void _addToClist(InequalityTerm[] variables, Integer indexWrap) {
        for (int i = 0; i < variables.length; i++) {
	    if ( !variables[i].isSettable()) {
		throw new InvalidStateException(
		    "InequalitySolver._addToClist: An InequalityTerm returns "
		    + "a variable that is not settable.");
	    }

            Vector entry = (Vector)(_Clist.get(variables[i]));
            if (entry == null) {
		// variable not in Hashtable
                entry = new Vector();
                _Clist.put(variables[i], entry);
	    }
            entry.addElement(indexWrap);
        }
    }

    // The solver used by solveLeast() and solveGreatest().
    // If the argument is true, solve for the least solution;
    // otherwise, solve for the greatest solution.
    private boolean _solve(boolean least) {
 
        // initialize all variables
	Object init = least ? _cpo.bottom() : _cpo.top();
	if (init == null) {
	    throw new InvalidStateException("InequalitySolver.solve: " +
                    "The underline CPO is not a lattice.");
	}

	for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
	    InequalityTerm variable = (InequalityTerm)e.nextElement();
	    try {
	        variable.setValue(init);
	    } catch (IllegalActionException ex) {
		throw new RuntimeException("InequalitySolver.solve: " +
			"Cannot set variable value(when Initialize variable). "
			+ ex.getMessage());
	    }
	}

	// initialize _NS(not satisfied) list; set _inCvar and _inserted flags. 

        // Not Satisfied list.  Each entry is an Integer storing index to
	// _Ilist.
    	// Note: removal in jdk1.2 LinkedList is not an O(1) operation, but
    	// an O(n) operation, where n is the number of elements in list.
    	// If the size of _NS is large, writing our own linked list class
    	// with a Cell class might be better.
    	LinkedList _NS = new LinkedList();

	for (int i = 0; i < _Ilist.size(); i++) {
	    Info info = (Info)_Ilist.elementAt(i);
	    info._inCvar = least ? info._ineq.getGreaterTerm().isSettable()
                : info._ineq.getLesserTerm().isSettable();

	    if (info._inCvar) {
	    	if (info._ineq.isSatisfied(_cpo)) {
		    info._inserted = false;
		} else { 	// insert to _NS
		    // FIXME: restore this line for jdk1.2
                    //                  _NS.addLast(new Integer(i));

		    // FIXME: delete this line for jdk1.2
                    _NS.insertLast(new Integer(i));

		    info._inserted = true;
		}
	    }
	}

	// solve the inequalities
        while (_NS.size() > 0) {

	    // FIXME: restore this line for jdk1.2
            //            int index = ((Integer)(_NS.removeFirst())).intValue();

	    // FIXME: delete the following 2 lines for jdk1.2
            int index = ((Integer)(_NS.first())).intValue();
            _NS.removeFirst();
	    // end last FIXME

            Info info = (Info)(_Ilist.elementAt(index));
            info._inserted = false;
            Object value = null;
	    InequalityTerm updateTerm = null;
	    if (least) {
		updateTerm = info._ineq.getGreaterTerm();
	        value = _cpo.leastUpperBound(
				info._ineq.getLesserTerm().getValue(),
                        	updateTerm.getValue());
	    } else {
		updateTerm = info._ineq.getLesserTerm();
	        value = _cpo.greatestLowerBound(updateTerm.getValue(),
                        	info._ineq.getGreaterTerm().getValue());
	    }

            if (value == null) {
                throw new InvalidStateException("The CPO over which " +
                        "the inequalities are defined is not a lattice.");
            }

	    try {
		updateTerm.setValue(value);
	    } catch (IllegalActionException ex) {
		throw new RuntimeException("InequalitySolver.solve: " +
			"Can't set variable value(when update variable). " +
			ex.getMessage());
	    }
            
            // insert or drop the inequalities affected
            Vector affected = (Vector)_Clist.get(updateTerm);
            for (int i = 0; i < affected.size(); i++) {
                Integer index1Wrap = (Integer)(affected.elementAt(i));
                int index1 = index1Wrap.intValue();
		Info affectedInfo = (Info)_Ilist.elementAt(index1);
                if (index1 != index && affectedInfo._inCvar) {
                    if (affectedInfo._ineq.isSatisfied(_cpo)) {    // drop
                        if (affectedInfo._inserted) {

			    // FIXME: restore this line for jdk1.2
                            //                            _NS.remove(index1Wrap);

			    // FIXME: delete this line for jdk1.2
                            _NS.removeOneOf(index1Wrap);

                        }
                    } else {                        // insert
                        if ( !affectedInfo._inserted) {

			    // FIXME: restore this line for jdk1.2
                            //                _NS.addFirst(index1Wrap);

			    // FIXME: delete this line for jdk1.2
                            _NS.insertFirst(index1Wrap);

                        }
                    }
                }
            }
        }
        
        // check if the inequalities in Ccnst are satisfied
        for (int i = 0; i < _Ilist.size(); i++) {
	    Info info = (Info)_Ilist.elementAt(i);
	    if ( !info._inCvar) {
                if ( !info._ineq.isSatisfied(_cpo)) {
                    return false;
		}
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CPO _cpo = null;
 
    // Vector representation of Ilist. Each entry is an instance of the
    // inner class Info. This vector effectively gives each inequality an
    // index, _Clist and _NS use that index.
    private Vector _Ilist = new Vector();
    
    // Mapping from variable to the Inequalities containing them.
    // Each entry in _Clist is a vector of Integers containing the
    // index of inequalities in _Ilist.
    private Hashtable _Clist = new Hashtable();
}

