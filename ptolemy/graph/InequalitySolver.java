/** An algorithm to solve a set of inequalities.

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
import collections.LinkedList;
import java.util.*;
import pt.kernel.util.IllegalActionException;
import pt.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// InequalitySolver
/**
An algorithm to solve a set of inequalities.
This algorithm is based on
<a href=http://www.diku.dk/topps/personal/rehof/publications.html>
<i>Tractable Constraints in Finite Semilattices</i></a> by Jakob Rehof
and Torben Mogensen.<p>

The algorithm in Rehof works for definite inequalities, which are
inequalities that the greater term is a constant or a variable.  This
solver doesn't enforce this requirement.  However, if the inequalities
are not definite, this solver may not be able to find the solution even
when the set of inequalities is satisfiable.  See the paper for
details.<p>

This solver supports finding both the least and greatest solutions (if
they exist).  It assumes that the CPO passed to the constructor is a
lattice, but it does not verify it.  If the LUB or GLB of some
elements doesn't exist during the execution of the algorithm, an
exception is thrown.

@author Yuhong Xiong
$Id$
*/

// Note: To make it easier to reference the above paper, this class
// uses the same names for methods and variables as in the paper, which
// may violate the naming convention in some cases.

public class InequalitySolver {

    /** Constructs and initializes this inequality solver.
     *  @param cpo a CPO over which the inequalities are defined.
     */
    public InequalitySolver(CPO cpo) {
        _cpo = cpo;
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Adds all of the inequalities in the specified
     *  <code>Enumeration </code> to the set of constraints.
     *  @param ineqs an <code>Enumeration</code> of <code>Inequality</code>
     *  @exception IllegalArgumentException the specified
     *   <code>Enumeration</code> contains an object that is not an
     *   <code>Inequality</code>.
     */
    public void addInequalities(Enumeration ineqs) {
	while (ineqs.hasMoreElements()) {
	    Object element = ineqs.nextElement();
	    if ( !(element instanceof Inequality)) {
		throw new IllegalArgumentException(
			"InequalitySolver.addInequalities: the specified " +
			"enumeration contains an object that is not an " +
			"Inequality.");
	    }
	    addInequality((Inequality)element);
	}
    }

    /** Adds an inequality to the set of constraints.
     *  @param ineq an <code>Inequality</code>.
     */
    public void addInequality(Inequality ineq) {

	// put ineq. to _Ilist
	Integer indexWrap = new Integer(_Ilist.size());
	Info info = new Info(ineq);
	_Ilist.addElement(info);

        // add var->ineq to Hashtable
	InequalityTerm[] vars = ineq.variables();
        for (int i = 0; i < vars.length; i++) {
            Vector entry = (Vector)(_Clist.get(vars[i]));
            if (entry == null) {	// variable not in Hashtable
                entry = new Vector();
                _Clist.put(vars[i], entry);
	    }
            entry.addElement(indexWrap);
        }
    }

    /** Returns an Enumeration of the variables whose current values are
     *  the bottom of the underlining CPO. If none of the variables have
     *  the current value set to bottom, an empty Enumeration is returned.
     *  @return an Enumeration of InequalityTerm
     *  @exception InvalidStateException the underlining CPO does not have
     *   a bottom element.
     */
    public Enumeration bottomVariables() {
	Object bottom = _cpo.bottom();
	if (bottom == null) {
	    throw new InvalidStateException("InequalitySolver.bottomVariables:"
			+ " The underlining CPO does not have a bottom"
			+ " element.");
	}

	LinkedList result = new LinkedList();
	for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
	    InequalityTerm variable = (InequalityTerm)e.nextElement();
	    if (variable.value().equals(bottom)) {
		result.insertLast(variable);
	    }
	}
	return result.elements();
    }

    /** Solves the set of inequalities and updates the variables.
     *  If the set of inequalities is satisfiable, this method returns
     *  <code>true</code> and the variables are set to the least or
     *  greatest solution depending on the <code>least</code> parameter;
     *  if the set of inequalities is definite (see class document), and
     *  the set of inequalities is not satisfiable, this method returns
     *  <code>false</code>; if the set of inequalities is not definite,
     *  a <code>false</code> return value doesn't guarantee the set of
     *  inequalities is not satisfiable. 
     *  @param least if <code>true</code>, this method will try to
     *   find the least solution; otherwise, this method will try to find
     *   the greatest solution.
     *  @return <code>true</code> if the set of inequalities is
     *   satisfiable; <code>false</code> if not satisfiable, or
     *   the set of inequalities is not definite.
     *  @exception InvalidStateException the CPO over which the
     *   inequalities are defined is not a lattice.
     */
    public boolean solve(boolean least) {
 
        // initialize all variables
	Object init = least ? _cpo.bottom() : _cpo.top();
	if (init == null) {
	    throw new InvalidStateException("InequalitySolver.solve: " +
			"The underlining CPO is not a lattice.");
	}

	for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
	    InequalityTerm variable = (InequalityTerm)e.nextElement();
	    try {
	        variable.set(init);
	    } catch (IllegalActionException ex) {
		throw new RuntimeException("InequalitySolver.solve: " +
			"Can't set variable value(when Initialize variable). "
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
	    info._inCvar = least ? info._ineq.greaterTerm().settable()
				 : info._ineq.lesserTerm().settable();

	    if (info._inCvar) {
	    	if (info._ineq.satisfied(_cpo)) {
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
		updateTerm = info._ineq.greaterTerm();
	        value = _cpo.lub(info._ineq.lesserTerm().value(),
                                 updateTerm.value());
	    } else {
		updateTerm = info._ineq.lesserTerm();
	        value = _cpo.glb(updateTerm.value(),
                                 info._ineq.greaterTerm().value());
	    }

            if (value == null) {
                throw new InvalidStateException("The CPO over which " +
                          "the inequalities are defined is not a lattice.");
            }

	    try {
		updateTerm.set(value);
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
                    if (affectedInfo._ineq.satisfied(_cpo)) {    // drop
                        if (affectedInfo._inserted) {

			    // FIXME: restore this line for jdk1.2
//                            _NS.remove(index1Wrap);

			    // FIXME: delete this line for jdk1.2
                            _NS.removeOneOf(index1Wrap);

                        }
                    } else {                        // insert
                        if ( !affectedInfo._inserted) {

			    // FIXME: restore this line for jdk1.2
//                            _NS.addFirst(index1Wrap);

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
                if ( !info._ineq.satisfied(_cpo)) {
                    return false;
		}
            }
        }
        return true;
    }

    /** Returns an Enumeration of the variables whose current values are
     *  the top of the underlining CPO. If none of the variables have
     *  the current value set to top, an empty Enumeration is returned.
     *  @return an Enumeration of InequalityTerm
     *  @exception InvalidStateException the underlining CPO does not have
     *   a top element.
     */
    public Enumeration topVariables() {
	Object top = _cpo.top();
	if (top == null) {
	    throw new InvalidStateException("InequalitySolver.topVariables:"
			+ " The underlining CPO does not have a top element.");
	}

	LinkedList result = new LinkedList();
	for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
	    InequalityTerm variable = (InequalityTerm)e.nextElement();
	    if (variable.value().equals(top)) {
		result.insertLast(variable);
	    }
	}
	return result.elements();
    }

    /** Returns an enumeration of Inequality that are not satisfied under
     *  the current value of variables. This method can be called regardless
     *  of whether <code>solve()</code> is called or not. If all the
     *  inequalities are satisfied, an empty Enumeration is returned.
     *  @return an Enumeration of Inequality
     */
    public Enumeration unsatisfiedIneq() {
	LinkedList result = new LinkedList();

        for (int i = 0; i < _Ilist.size(); i++) {
	    Info info = (Info)_Ilist.elementAt(i);
            if ( !info._ineq.satisfied(_cpo)) {
                result.insertLast(info._ineq);
	    }
        }
	return result.elements();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                          inner class                           ////

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
    
    ////////////////////////////////////////////////////////////////////////
    ////                       private variables                        ////

    private CPO _cpo = null;
 
    // Vector representation of Ilist. Each entry is an instance of the
    // inner class Info. This vector effectively gives each inequality an
    // index, _Clist and _NS use that index.
    private Vector _Ilist = new Vector();
    
    // Each entry in _Clist is a vector of Integers containing the
    // index of inequalities in _Ilist.
    private Hashtable _Clist = new Hashtable();
}

