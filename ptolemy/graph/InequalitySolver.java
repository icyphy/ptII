/** An algorithm to solve a set of inequality constraints.

    Copyright (c) 1997-2003 The Regents of the University of California.
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

    @ProposedRating Green (cxh@eecs.berkeley.edu)
    added description() method
    made many methods throw IllegalActionException
    @AcceptedRating Red (cxh@eecs.berkeley.edu)

 */

package ptolemy.graph;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

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
   @version $Id$
   @since Ptolemy II 0.2
 */

// Note: To make it easier to reference the above paper, some of the
// private methods and variables in this class have the same names that
// are used in the paper, which may violate the naming convention
// in some cases.

public class InequalitySolver {

    /** Construct an inequality solver.
     *  @param cpo The CPO over which the inequalities are defined.
     */
    public InequalitySolver(CPO cpo) {
        _cpo = cpo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a group of inequalities to the set of constraints.
     *  @param inequalities An <code>Iterator</code> for instances of
     *  <code>Inequality</code>.
     */
    public void addInequalities(Iterator inequalities) {
        while (inequalities.hasNext()) {
            addInequality((Inequality)inequalities.next());
        }
    }

    /** Add an <code>Inequality</code> to the set of constraints.
     *  @param ineq An <code>Inequality</code>.
     */
    public void addInequality(Inequality ineq) {
        // put ineq. to _Ilist
        Integer indexWrap = new Integer(_Ilist.size());
        Info info = new Info(ineq);
        _Ilist.add(info);

        // add var->ineq to Hashtable
        _addToClist(ineq.getLesserTerm().getVariables(), indexWrap);
        _addToClist(ineq.getGreaterTerm().getVariables(), indexWrap);
    }

    /** Return an <code>Iterator</code> of the variables whose current
     *  values are the bottom of the underlying CPO. If none of the
     *  variables have its current value set to the bottom, an empty
     *  <code>Iterator</code> is returned.
     *  @return An Iterator of InequalityTerms
     *  @exception InvalidStateException If the underlying CPO does not
     *   have a bottom element.
     *  @exception IllegalActionException If testing any one of the
     *  variables throws an exception.
     */
    public Iterator bottomVariables() throws IllegalActionException {
        Object bottom = _cpo.bottom();
        if (bottom == null) {
            throw new InvalidStateException(
                    "The underlying CPO does not have a bottom element.");
        }

        return _filterVariables(bottom);
    }

    /** Return a description of this solver as a String */
    public String description() {
        // This method is useful for debugging.
        StringBuffer results =
            new StringBuffer("{_Ilist:\n ");
        for (int i = 0; i < _Ilist.size(); i++) {
            Info info = (Info)_Ilist.get(i);
            results.append("{_ineq: " + info._ineq
                    + " _inCvar: " + info._inCvar
                    + " _inserted: " + info._inserted
                    + "}\n  ");
        }
        results.append("}\n{Clist:\n ");
        for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
            InequalityTerm variable = (InequalityTerm)e.nextElement();
            results.append("{"
                    + ((variable == null) ?
                            "variable == null" : variable.toString())
                    + "}\n ");
        }
        results.append("}\n");
        return results.toString();
    }

    /** Solve the set of inequalities for the greatest solution.
     *  If the set of inequalities is definite (when solving for the greatest
     *  solution, <i>definite</i> means that the lesser terms of all the
     *  inequalities are either constants or single variables),
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
     *  @return True if a solution for the inequalities is found,
     *  false otherwise.
     *  @exception IllegalActionException If testing any one of the
     *  inequalities throws an exception.
     */
    public boolean solveGreatest() throws IllegalActionException {
        return _solve(false);
    }

    /** Solve the set of inequalities for the least solution.
     *  If the set of inequalities is definite (when solving for the least
     *  solution, <i>definite</i> means that the greater terms of all the
     *  inequalities are either constants or single variables),
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
     *  See the paper referred to in the class document for details.
     *  @return True if a solution for the inequalities is found,
     *   <code>false</code> otherwise.
     *  @exception IllegalActionException If testing any one of the
     *  inequalities throws an exception.
     */
    public boolean solveLeast() throws IllegalActionException {
        return _solve(true);
    }

    /** Return an <code>Iterator</code> of the variables whose current
     *  values are the top of the underlying CPO. If none of the
     *  variables have the current value set to the top, an empty
     *  <code>Iterator</code> is returned.
     *  @return An Iterator of InequalityTerms
     *  @exception InvalidStateException If the underlying CPO does not
     *   have a top element.
     *  @exception IllegalActionException If testing any one of the
     *  variables throws an exception.
     */
    public Iterator topVariables() throws IllegalActionException {
        Object top = _cpo.top();
        if (top == null) {
            throw new InvalidStateException(
                    "The underlying CPO does not have a top element.");
        }

        return _filterVariables(top);
    }

    /** Return an <code>Iterator</code> of <code>Inequalities</code>
     *  that are not satisfied with the current value of variables.
     *  If all the inequalities are satisfied, an empty
     *  <code>Iterator</code> is returned.
     *  @return An Iterator of Inequalities
     *  @exception IllegalActionException If testing any one of the
     *  inequalities throws an exception.
     */
    public Iterator unsatisfiedInequalities() throws IllegalActionException {
        LinkedList result = new LinkedList();

        for (int i = 0; i < _Ilist.size(); i++) {
            Info info = (Info)_Ilist.get(i);
            if ( !info._ineq.isSatisfied(_cpo)) {
                result.addLast(info._ineq);
            }
        }
        return result.iterator();
    }

    /** Return an <code>Iterator</code> of all the variables in the
     *  inequality constraints.
     *  @return An Iterator of InequalityTerms
     *  @exception IllegalActionException If testing any one of the
     *  variables throws an exception.
     */
    public Iterator variables() {
        LinkedList result = new LinkedList();
        for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
            InequalityTerm variable = (InequalityTerm)e.nextElement();
            result.addLast(variable);
        }
        return result.iterator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // Each instance of this class is an entry in _Ilist.
    private class Info {
        private Info(Inequality ineq) {
            _ineq = ineq;
        }

        private Inequality _ineq;

        // True if this ineq. is in the "Cvar" set of the Rehof paper,
        // i.e., if looking for the least solution and the greaterTerm
        // is settable, or looking for the greatest solution and the
        // lesserTerm is settable.
        private boolean _inCvar = false;

        // If this ineq. is in _NS
        private boolean _inserted = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Add the InequalityTerms in the specified array as keys and the
    // index as value to _Clist.  The InequalityTerms are variables
    // and the index is the index of the Inequality in _Ilist that
    // contains the variables.
    private void _addToClist(InequalityTerm[] variables,
            Integer indexWrap) {
        for (int i = 0; i < variables.length; i++) {
            if ( !variables[i].isSettable()) {
                throw new InvalidStateException(
                        "Variable in an InequalityTerm is not settable.");
            }

            ArrayList entry = (ArrayList)(_Clist.get(variables[i]));
            if (entry == null) {
                // variable not in Hashtable
                entry = new ArrayList();
                _Clist.put(variables[i], entry);
            }
            entry.add(indexWrap);
        }
    }

    // filter out the variables with a certain value. If the given value
    // is null, return all variables. This method is used by,
    // bottomVariables(), and topVariables(), and variables(). For variables(),
    // this method effectively converts an Enumeration to an Iterator.
    // This is necessary for interface consistency since other methods
    // in this package return Iterators.
    private Iterator _filterVariables(Object value)
            throws IllegalActionException {

        LinkedList result = new LinkedList();
        for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
            InequalityTerm variable = (InequalityTerm)e.nextElement();
            if (value == null || variable.getValue().equals(value)) {
                result.addLast(variable);
            }
        }
        return result.iterator();
    }

    // The solver used by solveLeast() and solveGreatest().
    // If the argument is true, solve for the least solution;
    // otherwise, solve for the greatest solution.
    private boolean _solve(boolean least) throws IllegalActionException {

        // initialize all variables
        Object init = least ? _cpo.bottom() : _cpo.top();
        if (init == null) {
            throw new InvalidStateException(
                    "The underlying CPO is not a lattice.");
        }

        for (Enumeration e = _Clist.keys(); e.hasMoreElements() ;) {
            InequalityTerm variable = (InequalityTerm)e.nextElement();
            try {
                variable.initialize(init);
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(null, null, ex,
                        "Cannot initialize variable.");
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
            Info info = (Info)_Ilist.get(i);
            info._inCvar = least ? info._ineq.getGreaterTerm().isSettable()
                : info._ineq.getLesserTerm().isSettable();

            if (info._inCvar) {
                if (info._ineq.isSatisfied(_cpo)) {
                    info._inserted = false;
                } else {         // insert to _NS
                    _NS.addLast(new Integer(i));
                    info._inserted = true;
                }
            }
        }

        // The outer loop is for handling the situation that some
        // InequalityTerms do not report all the variables they depend on
        // from the getVariables() call. This can happen, for example, in
        // type resolution application involving structured types, where
        // the type term for an element of a structured type does not have
        // a reference to the term of its enclosing type.
        boolean allSatisfied = false;
        while ( !allSatisfied) {

            // solve the inequalities
            while (_NS.size() > 0) {

                int index = ((Integer)(_NS.removeFirst())).intValue();

                Info info = (Info)(_Ilist.get(index));
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
                    throw new InvalidStateException(null, null, ex,
                            "Can't update variable.\n");
                }

                // insert or drop the inequalities affected
                ArrayList affected = (ArrayList)_Clist.get(updateTerm);
                for (int i = 0; i < affected.size(); i++) {
                    Integer index1Wrap = (Integer)(affected.get(i));
                    int index1 = index1Wrap.intValue();
                    Info affectedInfo = (Info)_Ilist.get(index1);
                    if (index1 != index && affectedInfo._inCvar) {
                        if (affectedInfo._ineq.isSatisfied(_cpo)) {    // drop
                            if (affectedInfo._inserted) {
                                _NS.remove(index1Wrap);
                            }
                        } else {                        // insert
                            if ( !affectedInfo._inserted) {
                                _NS.addFirst(index1Wrap);
                            }
                        }
                    }
                }
            }

            allSatisfied = true;
            for (int i = 0; i < _Ilist.size(); i++) {
                Info info = (Info)_Ilist.get(i);
                if (info._inCvar) {
                    if (info._ineq.isSatisfied(_cpo)) {
                        info._inserted = false;
                    } else {         // insert to _NS
                        _NS.addLast(new Integer(i));
                        info._inserted = true;
                        allSatisfied = false;
                    }
                }
            }
        }

        // Check the inequalities not involved in the above iteration.
        // These inequalities are the ones in the "Ccnst" set in the
        // Rehof paper.
        for (int i = 0; i < _Ilist.size(); i++) {
            Info info = (Info)_Ilist.get(i);
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

    // ArrayList representation of Ilist. Each entry is an instance of the
    // inner class Info. This vector effectively gives each inequality an
    // index, _Clist and _NS use that index.
    private ArrayList _Ilist = new ArrayList();

    // Mapping from variable to the Inequalities containing them.
    // Each entry in _Clist is a vector of Integers containing the
    // index of inequalities in _Ilist.
    private Hashtable _Clist = new Hashtable();
}
