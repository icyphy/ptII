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
and Torben Mogensen.

The algorithm in Rehof works for definite inequalities, which are
inequalities that the greater term is a constant or a variable.  This
solver doesn't enforce this requirement.  However, if the greater term
of some inequalities is a function, this solver may not be able to find
the solution even when the set of inequalities is satisfiable.  See
the paper for details.

This solver assumes that the CPO passed to the constructor is a lattice,
but it does not verify it.  If the least upper bound (LUB) of some
elements doesn't exist during the execution of the algorithm, an exception
is thrown.

@author Yuhong Xiong
$Id$
*/

// To make it easier to reference the above paper, this class uses the
// same names for methods and variables as in the paper, which may
// voilate the naming convention in some cases.

public class InequalitySolver {

    /** Constructs and initializes this inequality solver.
     *  @param cpo a CPO over which the inequalities are defined.
     */
    public InequalitySolver(CPO cpo) {
        _cpo = cpo;
        _bottom = _cpo.bottom();
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Adds an inequality to the set of constraints.
     *  @param ineq an <code>Inequality</code>.
     *  @param varList an array of <code>InequalityTerm</code>s corresponding
     *   to variables in the specified inequality.
     */
    public void addInequality(Inequality ineq, InequalityTerm[] varList) {
        // init all variables to bottom
        for (int i = 0; i < varList.length; i++) {
	    try {
                varList[i].set(_bottom);
	    } catch (IllegalActionException e) {
		System.out.println("IllegalActionException: can't " +
			"initialize variable.");
	    }
        }
        
        // put ineq to _Cvar or _Ccnst
        if (ineq.greaterTerm().settable()) {
            Integer indexObj = new Integer(_Cvar.size());
            CvarRecord rec = new CvarRecord(ineq, !ineq.satisfied());
            _Cvar.addElement(rec);

            // update _NS
            if (rec._inserted) {

		// FIXME: restore this line for jdk1.2
//                _NS.addLast(indexObj);

		// FIXME: delete this line for jdk1.2
                _NS.insertLast(indexObj);

            }
            
            // add var->ineq to Hashtable
            for (int i = 0; i < varList.length; i++) {
                Object clisti = _Clist.get(varList[i]);
                if (clisti == null) {
                    Vector ineqVec = new Vector();
                    ineqVec.addElement(indexObj);
                    _Clist.put(varList[i], ineqVec);
                } else {
                    ((Vector)clisti).addElement(indexObj);
                }
            }
        } else {                // ineq is not in Cvar
            _Ccnst.addElement(ineq);
        }
    }
    
    /** Solves the set of inequalities and updates the variables.
     *  If the set of inequalities is satisfiable, this method returns
     *  <code>true</code>; if the set of inequalities is definite,
     *  ie., the greater term of each inequality is a constant or a
     *  variable, and the set of inequalities is not satisfiable, this
     *  method returns <code>false</code>; if the set of inequalities
     *  is not definite, a <code>false</code> return value doesn't
     *  guarantee the set of inequalities is not satisfiable.
     *  @return <code>true</code> if the set of inequalities is
     *   satisfiable; <code>false</code> if not satisfiable, or
     *   the set of inequalities is not definite.
     *  @exception InvalidStateException the CPO over which the
     *   inequalities are defined is not a lattice.
     */
    public boolean solve() {
        while (_NS.size() > 0) {

	    // FIXME: restore this line for jdk1.2
//            int index = ((Integer)(_NS.removeFirst())).intValue();

	    // FIXME: delete the following 2 lines for jdk1.2
            int index = ((Integer)(_NS.first())).intValue();
            _NS.removeFirst();
	    // end last FIXME


            CvarRecord rec = (CvarRecord)(_Cvar.elementAt(index));
            rec._inserted = false;
            Object value = _cpo.lub(rec._ineq.lesserTerm().value(),
                                    rec._ineq.greaterTerm().value());
            if (value == null) {
                throw new InvalidStateException("The CPO over which " +
                          "the inequalities are defined is not a lattice.");
            }
	    try {
                rec._ineq.greaterTerm().set(value);
	    } catch (IllegalActionException e) {
		System.out.println("IllegalActionException: Can't " +
			"set value to variable.");
	    }
            
            // insert or drop the inequalities in Clist[rec._ineq.greaterTerm()]
            Vector affected = (Vector)(_Clist.get(rec._ineq.greaterTerm()));
            for (int i = 0; i < affected.size(); i++) {
                Integer index1Obj = (Integer)(affected.elementAt(i));
                int index1 = index1Obj.intValue();
                if (index1 != index) {
                    rec = (CvarRecord)(_Cvar.elementAt(index1));
                    if (rec._ineq.satisfied()) {    // drop
                        if (rec._inserted) {

			    // FIXME: restore this line for jdk1.2
//                            _NS.remove(index1Obj);

			    // FIXME: delete this line for jdk1.2
                            _NS.removeOneOf(index1Obj);

                        }
                    } else {                        // insert
                        if ( !rec._inserted) {

			    // FIXME: restore this line for jdk1.2
//                            _NS.addFirst(index1Obj);

			    // FIXME: delete this line for jdk1.2
                            _NS.insertFirst(index1Obj);

                        }
                    }
                }
            }
        }
        
        // check if the inequalities in Ccnst are satisfied
        for (int i = 0; i < _Ccnst.size(); i++) {
            Inequality ineq = (Inequality)(_Ccnst.elementAt(i));
            if ( !ineq.satisfied()) {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                          inner class                           ////

    // This class contains a reference to an inequality and the inserted
    // flag.  Each instance is an entry in _Cvar.
    private class CvarRecord {
        private CvarRecord(Inequality ineq, boolean inserted) {
            _ineq = ineq;
            _inserted = inserted;
        }
        
        private Inequality _ineq;
        private boolean _inserted;
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                       private variables                        ////

    private CPO _cpo = null;
    private Object _bottom = null;    // local cache
    
    // Vector representation of Cvar.  Each entry is an instance of the
    // inner class CvarRecord which contains an inequality and the inserted
    // flag.
    // These vectors effectively gives each inequality in Cvar an index,
    // and _ClistCvar and NS (below) use that index.
    private Vector _Cvar = new Vector();
    
    // inequalities in Ccnst.  This vector and _CvarIneq holds all the
    // inequalities in the constraint set, which makes up Ilist in Rehof.
    private Vector _Ccnst = new Vector();
    
    // In Rehof, each entry of Clist holds pointers to inequalities in
    // Ilist, but only pointers to inequalities in Cvar is used during
    // the iteration, so the Clist here only contains references to
    // inequalities in Cvar.
    // Each entry in _Clist is a vector of Integers containing the
    // index of inequalities in _Cvar.
    private Hashtable _Clist = new Hashtable();
    
    // Not Satisfied list.  Each entry is an Integer.
    // Note remove in jdk1.2 LinkedList is not an O(1) operation, but
    // an O(n) operation, where n is the number of elements in list.
    // If the size of _NS is large, writing our own linked list class
    // with a Cell class can be considered.
    private LinkedList _NS = new LinkedList();
}

