/*

 Copyright (c) 1999-2003 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_3
                                        COPYRIGHTENDKEY
@ProposedRating Red (rowland@eecs.berkeley.edu)
@AcceptedRating Red (rowland@eecs.berkeley.edu)
*/
package ptolemy.data.unit;

import java.text.DecimalFormat;
import java.util.Vector;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// Solver
/**
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class Solver {

    /**
     * Construct a Solver
     *
     * @param model
     * @param vLabels
     * @param constraints
     * @exception IllegalActionException If there is an exception
     */
    public Solver(
        TypedCompositeActor model,
        String[] vLabels,
        Vector constraints)
        throws IllegalActionException {
        _numConstraints = constraints.size();
        _variables = vLabels;
        _model = model;
        _numVariables = _variables.length;
        _vectorA = new Unit[_numConstraints];
        _source = new NamedObj[_numConstraints];
        _done = new boolean[_numConstraints];
        _arrayP = new double[_numConstraints][];
        for (int i = 0; i < _numConstraints; i++) {
            _arrayP[i] = new double[_numVariables];
            _done[i] = false;
        }
        for (int constraintNum = 0;
            constraintNum < _numConstraints;
            constraintNum++) {
            UnitEquation constraint =
                (UnitEquation) (constraints.elementAt(constraintNum));
            constraint.canonicalize();
            Vector rightUTerms = constraint.getRhs().getUTerms();
            if (rightUTerms.size() != 1) {
                throw new IllegalActionException(
                    "Constraint " + constraint + " has nonsingular RHS");
            }
            UnitTerm rhsUterm = (UnitTerm) (rightUTerms.elementAt(0));
            if (!rhsUterm.isUnit()) {
                throw new IllegalActionException(
                    "Constraint " + constraint + " has nonUnit RHS");
            }
            _vectorA[constraintNum] = rhsUterm.getUnit();
            _source[constraintNum] = constraint.getSource();
            Vector leftUTerms = constraint.getLhs().getUTerms();
            for (int i = 0; i < leftUTerms.size(); i++) {
                UnitTerm leftUTerm = (UnitTerm) (leftUTerms.elementAt(i));
                if (!leftUTerm.isVariable()) {
                    throw new IllegalActionException(
                        "Constraint " + constraint + " has nonVar LHS");
                }
                String variableLabel = leftUTerm.getVariable();
                double exponent = leftUTerm.getExponent();
                int varIndex = getVarIndex(variableLabel);
                _arrayP[constraintNum][varIndex] = exponent;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
    /**
     * Annotates the model so that when it is displayed it will be color coded
     * and have tooltips that will convey various aspects of the solution.
     */
    public void annotateGraph() {
        StringBuffer moml = new StringBuffer();
        for (int constraintNum = 0;
            constraintNum < _numConstraints;
            constraintNum++) {
            NamedObj source = _source[constraintNum];
            if (source instanceof IOPort) {
                IOPort port = (IOPort) source;
                ComponentEntity actor = (ComponentEntity) (port.getContainer());
                String expression = _vectorA[constraintNum].commonDesc();
                moml.append(
                    "<entity name=\""
                        + actor.getName()
                        + "\">"
                        + "<port name=\""
                        + port.getName()
                        + "\">"
                        + "<property name=\"_color\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \"green\"/><property name=\"_description\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \""
                        + expression
                        + "\"/></port>"
                        + "</entity>");
            } else if (source instanceof IORelation) {
                if (!ConstraintConsistent(constraintNum)) {
                    IORelation relation = (IORelation) source;
                    moml.append(
                        "<relation name=\""
                            + relation.getName()
                            + "\" class=\"ptolemy.actor.TypedIORelation\">"
                            + "<property name=\"_color\" "
                            + "class = \"ptolemy.kernel.util.StringAttribute\" "
                            + "value = \"magenta\"/>"
                            + "<property name=\"_description\" "
                            + "class = \"ptolemy.kernel.util.StringAttribute\" "
                            + "value = \""
                            + relation.toString()
                            + "\"/></relation>");
                }
            }
        }
        if (moml.length() > 0) {
            UnitConstraints uc = null;
            uc = new UnitConstraints();
            String momlUpdate = "<group>" + moml.toString() + "</group>";
            MoMLChangeRequest request =
                new MoMLChangeRequest(uc, _model, momlUpdate);
            request.setUndoable(false);
            _model.requestChange(request);
        }
    }

    /**
     * The current state of the solver. A StringBuffer is produced that shows
     * the variables, done vector, P array, and A vector in a human readable
     * arrangement.
     *
     * @return A StringBuffer with the contents of the solver.
     */
    public StringBuffer dumpContents() {
        StringBuffer retv = new StringBuffer();
        retv.append("Contents\n");
        for (int j = 0; j < _numVariables; j++) {
            retv.append(
                "   " + _vNumFormat.format(j) + " " + _variables[j] + "\n");
        }
        retv.append("    ");
        for (int j = 0; j < _numVariables; j++) {
            retv.append(" " + _vNumFormat.format(j));
        }
        retv.append("\n");
        for (int i = 0; i < _numConstraints; i++) {
            if (_done[i]) {
                retv.append("T ");
            } else {
                retv.append("F ");
            }
            retv.append("" + _vNumFormat.format(i) + " ");
            for (int j = 0; j < _numVariables; j++) {
                retv.append("" + _pFormat.format(_arrayP[i][j]) + " ");
            }
            retv.append(
                "" + _vectorA[i] + " " + _vectorA[i].commonDesc() + "\n");
        }
        retv.append("\\Contents\n");
        retv.append("State: ");
        switch (_solveState) {
            case NOTRUN :
                {
                    retv.append("NotRun");
                    break;
                }
            case NONUNIQUESOLUTION :
                {
                    retv.append("No Unique Solution");
                    break;
                }
            case NOSOLUTION :
                {
                    retv.append("No Solution");
                    break;
                }
            case SOLVED :
                {
                    retv.append("Solved");
                    break;
                }
        }
        return retv;
    }

    /**
     * Get the bindings that have been produced as a result of running the
     * solver.
     *
     * @return The bindings.
     */
    public Bindings getBindings() {
        return _bindings;
    }

    /**
     * Return true if solution has been found. Solution can be unique or
     * non-unique.
     *
     * @return True if a solution has been found, false otherwise.
     */
    public boolean isSolved() {
        return (_solveState == SOLVED || _solveState == NONUNIQUESOLUTION);
    }

    /**
     * Return true if a unique solution has been found.
     *
     * @return True if a unique solution has been found, false otherwise.
     */
    public boolean isUnique() {
        return (_solveState == SOLVED);
    }

    /**
     * Specify whether or not to have debugging information produced.
     *
     * @param debug
     *            True to see debugging information in standard output, false
     *            otherwise
     */
    public void setDebug(boolean debug) {
        this._debug = debug;
    }

    /**
     * Search for a solution. The algorithm used is described in XXX. When the
     * done the state of the solver will be in one of three states - SOLVED,
     * NONUNIQUESOLUTION, and NOSOLUTION.
     */
    public void solve() {
        _bindings = new Bindings();
        Index g;
        while ((g = findG()) != null) {
            System.out.println("g " + g);
            int k = g.getK();
            int l = g.getL();
            System.out.println("PPPPPPP " + _arrayP[k][l]);
            Unit U = _vectorA[k].pow(1.0 / _arrayP[k][l]);
            _vectorA[k] = U;
            _arrayP[k][l] = 1;
            _bindings.put(_variables[l], U);
            for (int i = 0; i < _numConstraints; i++) {
                if (i != k && !_done[i] && _arrayP[i][l] != 0) {
                    _vectorA[i] = _vectorA[i].divideBy(U.pow(_arrayP[i][l]));
                    _arrayP[i][l] = 0;
                }
            }
            _done[k] = true;
            System.out.println(dumpContents());
        }
        _solveState = SOLVED;
        for (int i = 0; i < _numConstraints; i++) {
            if (!_done[i]) {
                _solveState = NONUNIQUESOLUTION;
                int numNonZeroP = 0;
                for (int j = 0; j < _numVariables; j++) {
                    if (_arrayP[i][j] != 0)
                        numNonZeroP++;
                }
                if (numNonZeroP > 1) {
                    _solveState = NOSOLUTION;
                    break;
                } else if (
                    numNonZeroP == 0
                        && !_vectorA[i].equals(UnitLibrary.Identity)) {
                    _solveState = NOSOLUTION;
                    break;
                }
            }
        }
    }

    /**
    * Skeleton of a method that will be used to obtain a solution of a
     * particular variable.
     *
     * @param variable
     *            The variable to solve for.
     */
    public void solveForVariable(String variable) {
        int vIndex = getVarIndex(variable);
        System.out.println("XX " + variable);
    }

    /**
     * An older, experimental version of solve.
     *
     */
    public void solveII() {
        _bindings = new Bindings();
        while (true) {
            if (PisZero()) {
                _solveState = SOLVED;
                return;
            }
            Vector G = createG();
            System.out.println("G " + G);
            if (G.size() == 0) {
                _solveState = NONUNIQUESOLUTION;
                return;
            }
            if (!GUnique(G)) {
                _solveState = NOSOLUTION;
                return;
            }
            Vector variablesBeingBound = new Vector();
            for (int h = 0; h < G.size(); h++) {
                Index x = (Index) (G.elementAt(h));
                int k = x.getK();
                int l = x.getL();
                System.out.println("PPPPPPP " + _arrayP[k][l]);
                Unit U = _vectorA[k].pow(1.0 / _arrayP[k][l]);
                _bindings.put(_variables[l], U);
                variablesBeingBound.add(_variables[l]);
            }
            // Now apply the bindings
            for (int h = 0; h < variablesBeingBound.size(); h++) {
                String vLabel = (String) (variablesBeingBound.elementAt(h));
                int l = getVarIndex(vLabel);
                Unit unit = _bindings.get(vLabel);
                for (int i = 0; i < _numConstraints; i++) {
                    _vectorA[i] = _vectorA[i].divideBy(unit.pow(_arrayP[i][l]));
                    _arrayP[i][l] = 0;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    /////                        private methods                 //////

    /**
     * Determine if the constraint reperesented by a particular row in P is
     * consistent.
     *
     * @param constraintNum
     *            The row number in P.
     * @return
     */
    private boolean ConstraintConsistent(int constraintNum) {
        Unit unit = UnitLibrary.Identity.copy();
        for (int varNum = 0; varNum < _numVariables; varNum++) {
            if (_arrayP[constraintNum][varNum] != 0) {
                Unit opnd = _bindings.get(_variables[varNum]);
                if (opnd != null) {
                    unit =
                        unit.multiplyBy(
                            opnd.pow(_arrayP[constraintNum][varNum]));
                } else {
                    return false;
                }
            }
        }
        return unit.equals(_vectorA[constraintNum]);
    }

    private boolean GUnique(Vector G) {
        for (int h = 0; h < G.size(); h++) {
            Index x = (Index) (G.elementAt(h));
            int k = x.getK();
            int l = x.getL();
            for (int h1 = h; h1 < G.size(); h1++) {
                Index x1 = (Index) (G.elementAt(h1));
                int k1 = x1.getK();
                int l1 = x1.getL();
                if ((l == l1) && (!_vectorA[k].equals(_vectorA[k1]))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine if entire P matrix is zero.
     *
     * @return True if entire P matrix is zero.
     */
    private boolean PisZero() {
        for (int j = 0; j < _numVariables; j++) {
            for (int i = 0; i < _numConstraints; i++) {
                if (_arrayP[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private Vector createG() {
        Vector G = new Vector();
        for (int i = 0; i < _numConstraints; i++) {
            int l = -1;
            boolean possible = false;
            for (int j = 0; j < _numVariables; j++) {
                if (_arrayP[i][j] != 0) {
                    if (l == -1) {
                        possible = true;
                        l = j;
                    } else {
                        possible = false;
                        break;
                    }
                }
            }
            if (possible) {
                G.add(new Index(i, l));
            }
        }
        return G;
    }

    /**
     * Finds an Index(i, l) such that P[i][l] != 0 and 1) all other P in row i
     * is equal to 0 and 2) all other P in column l is equal to 0
     *
     * @return Index(i, l)
     */
    private Index findG() {
        for (int i = 0; i < _numConstraints; i++) {
            if (!_done[i]) {
                int l = -1;
                boolean possible = false;
                for (int j = 0; j < _numVariables; j++) {
                    if (_arrayP[i][j] != 0) {
                        if (l == -1) {
                            possible = true;
                            l = j;
                        } else {
                            possible = false;
                            break;
                        }
                    }
                }
                if (possible) {
                    return (new Index(i, l));
                }
            }
        }
        return null;
    }

    /**
     * @param variableLabel
     * @return
     */
    private int getVarIndex(String variableLabel) {
        for (int i = 0; i < _variables.length; i++) {
            if (_variables[i].equals(variableLabel)) {
                return i;
            }
        }
        return -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    private static final int NOTRUN = -1;
    private static final int SOLVED = 0;
    private static final int NOSOLUTION = 1;
    private static final int NONUNIQUESOLUTION = 2;
    double _arrayP[][];
    Bindings _bindings = null;
    boolean _debug = true;
    boolean _done[];
    TypedCompositeActor _model;
    int _numConstraints = 0;
    int _numVariables = 0;
    private static final DecimalFormat _pFormat = new DecimalFormat(" 0;-0");
    private int _solveState = NOTRUN;
    NamedObj _source[];
    String _variables[];
    Unit _vectorA[];
    private static final DecimalFormat _vNumFormat = new DecimalFormat("00");
    /**
     * Class that represents an index in the P array.
     *
     */
    private class Index {

        int k, l;

        private Index(int k1, int l1) {
            k = k1;
            l = l1;
        }

        public int getK() {
            return k;
        }

        public int getL() {
            return l;
        }

        public String toString() {
            return "(" + k + "," + l + ")";
        }
    }
}
