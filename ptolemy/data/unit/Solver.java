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
@Pt.ProposedRating Red (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/
package ptolemy.data.unit;

import java.text.DecimalFormat;
import java.util.Iterator;
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
     *
     */
    public Solver() {
    }

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
                int varIndex = _getVarIndex(variableLabel);
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
        _createAnnotations();
        String color = null;
        StringBuffer moml = new StringBuffer();
        for (int j = 0; j < _numVariables; j++) {
            int secondDot = _variables[j].substring(1).indexOf(".") + 1;
            String variable = _variables[j].substring(secondDot + 1);
            String explanation = _varBindings[j];
            if (_varValid[j]) {
                color = "green";
            } else {
                color = "magenta";
            }

            moml.append(
                "<port name=\""
                    + _variables[j].substring(secondDot + 1)
                    + "\">"
                    + " <property name=\"_color\" "
                    + "class = \"ptolemy.kernel.util.StringAttribute\" "
                    + "value = \""
                    + color
                    + "\"/>"
                    + "<property name=\"_explanation\" "
                    + "class = \"ptolemy.kernel.util.StringAttribute\" "
                    + "value = \""
                    + explanation
                    + "\"/>"
                    + "</port>");
        }
        for (int constraintNum = 0;
            constraintNum < _numConstraints;
            constraintNum++) {
            NamedObj source = _source[constraintNum];
			String expression = _vectorA[constraintNum].commonDesc();

            if (_constraintConsistent[constraintNum]) {
                color = "green";
            } else {
                color = "magenta";
            }
            if (source instanceof IOPort) {
                IOPort port = (IOPort) source;
                ComponentEntity actor = (ComponentEntity) (port.getContainer());
                moml.append(
                    "<entity name=\""
                        + actor.getName()
                        + "\">"
                        + "<port name=\""
                        + port.getName()
                        + "\">"
                        + "<property name=\"_color\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \""
                        + color
                        + "\"/>"
                        + "<property name=\"_explanation\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \""
                        + expression
                        + "\"/></port>"
                        + "</entity>");
            } else if (source instanceof IORelation) {
                IORelation relation = (IORelation) source;
                moml.append(
                    "<relation name=\""
                        + relation.getName()
                        + "\" class=\"ptolemy.actor.TypedIORelation\">"
                        + "<property name=\"_color\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \""
                        + color
                        + "\"/>"
                        + "<property name=\"_explanation\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \""
                        + expression
                        + "\"/></relation>");
            }
        }
        if (moml.length() > 0) {
            String momlUpdate = "<group>" + moml.toString() + "</group>";
            MoMLChangeRequest request =
                new MoMLChangeRequest(this, _model, momlUpdate);
            request.setUndoable(false);
            if (_debug) {
                System.out.println("Solver.annotateGraph moml " + momlUpdate);
            }
            _model.requestChange(request);
        }
    }

    /**
     * Search for a solution. The algorithm used is described in XXX. When the
     * done the state of the solver will be in one of three states - SOLVED,
     * NONUNIQUESOLUTION, and NOSOLUTION.
     */
    public Vector completeSolve() {
        if (_debug) {
            System.out.println(
                "Solver.solve " + header() + " initial\n" + state());
        }
        Index g;
        while ((g = _findG()) != null) {
            int k = g.getK();
            int l = g.getL();
            Unit U = _vectorA[k].pow(1.0 / _arrayP[k][l]);
            _vectorA[k] = U;
            _arrayP[k][l] = 1;
            for (int i = 0; i < _numConstraints; i++) {
                if (i != k && !_done[i] && _arrayP[i][l] != 0) {
                    _vectorA[i] = _vectorA[i].divideBy(U.pow(_arrayP[i][l]));
                    _arrayP[i][l] = 0;
                }
            }
            _done[k] = true;
        }
        _analyzeState();
        Vector retv = new Vector();
        retv.add(this);
        return retv;
    }

    public Solver copy() {
        Solver retv = new Solver();
        retv._numConstraints = _numConstraints;
        retv._variables = _variables;
        retv._model = _model;
        retv._numVariables = _numVariables;
        retv._source = _source;
        retv._debug = _debug;
        retv._vectorA = new Unit[_numConstraints];
        retv._done = new boolean[_numConstraints];
        retv._arrayP = new double[_numConstraints][];
        for (int i = 0; i < _numConstraints; i++) {
            retv._arrayP[i] = new double[_numVariables];
            for (int j = 0; j < _numVariables; j++) {
                retv._arrayP[i][j] = _arrayP[i][j];
            }
            retv._done[i] = _done[i];
            retv._vectorA[i] = _vectorA[i].copy();
        }
        retv._upper = this;
        return retv;
    }

    /**
     * @return The short description.
     */
    public String getShortDescription() {
        return _stateDescription;
    }

    /**
     * The current state of the solver. A StringBuffer is produced that shows
     * the variables, done vector, P array, and A vector in a human readable
     * arrangement.
     *
     * @return A StringBuffer with the state of the solver.
     */
    public StringBuffer header() {
        StringBuffer retv = new StringBuffer();
        retv.append("Header\nVariables\n");
        for (int j = 0; j < _numVariables; j++) {
            retv.append(
                "   " + _vNumFormat.format(j) + " " + _variables[j] + "\n");
        }
        retv.append("\n");
        retv.append("ConstrNum  Source\n");
        for (int i = 0; i < _numConstraints; i++) {
            NamedObj source = _source[i];
            retv.append(
                ""
                    + _vNumFormat.format(i)
                    + "         "
                    + source.toString()
                    + "\n");
        }
        retv.append("\\Header\n");
        return retv;
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
     * @return The vector of partial solutions.
     */
    public Vector partialSolve() {
        if (_debug) {
            System.out.println(
                "Solver.solve " + header() + " initial\n" + state());
        }
        // Eliminate the singletons (due to Ports)
        Iterator allG = _findAllG().iterator();
        while (allG.hasNext()) {
            Index g = (Index) (allG.next());
            _eliminate(g);
        }
        if (_debug) {
            System.out.println("Solver.solve initialized\n" + state());
        }
        Vector branchPoints = _findAllG();
        Vector solutions = new Vector();
        if (branchPoints.size() > 0) {
            for (int i = 0; i < branchPoints.size(); i++) {
                Solver s = copy();
                Vector results =
                    s._partialSolveRecursively(
                        1,
                        (Index) (branchPoints.elementAt(i)));
                solutions.addAll(results);
            }
        } else {
            _analyzeState();
            solutions.add(this);
        }
        if (_debug) {
            for (int i = 0; i < solutions.size(); i++) {
                Solver solution = (Solver) (solutions.elementAt(i));
                System.out.println("A Solution\n" + solution.state());
            }
        }
        return solutions;

    }

    /**
     * Specify whether or not to have debugging information produced.
     *
     * @param debug True to see debugging information in standard output, false
     *            otherwise
     */
    public void setDebug(boolean debug) {
        this._debug = debug;
    }

    /**
     * The current state of the solver. A StringBuffer is produced that shows
     * the variables, done vector, P array, and A vector in a human readable
     * arrangement.
     *
     * @return A StringBuffer with the state of the solver.
     */
    public StringBuffer state() {
        StringBuffer retv = new StringBuffer();
        retv.append("State\n    ");
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
        if (_branchPoint == null) {
            retv.append("BranchPoint = null\n");
        } else {
            retv.append("BranchPoint = " + _branchPoint.toString() + "\n");
        }
        retv.append("Solution: ");
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
        retv.append("\n\\State\n");
        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    /////                        private methods                 //////

    /**
     *
     */
    private void _analyzeState() {
        _solveState = SOLVED;
        _stateDescription = "Unique";
        for (int i = 0; i < _numConstraints; i++) {
            if (!_done[i]) {
                int numNonZeroP = 0;
                for (int j = 0; j < _numVariables; j++) {
                    if (_arrayP[i][j] != 0)
                        numNonZeroP++;
                }
                if (numNonZeroP == 0
                    && !_vectorA[i].equals(UnitLibrary.Identity)) {
                    _solveState = NOSOLUTION;
                    Unit factor = _vectorA[i].invert();
                    String uString = factor.commonDesc();
                    NamedObj source = _source[i];
                    String sourceName = "NoSource";
                    if (source instanceof IORelation) {
                        sourceName = ((IORelation) source).getName();
                    } else if (source instanceof ComponentEntity) {
                        sourceName = ((ComponentEntity) source).getName();
                    }
                    _stateDescription =
                        "NoSolution " + sourceName + " " + uString;
                    return;
                }
                if (numNonZeroP > 1
                    && _vectorA[i].equals(UnitLibrary.Identity)) {
                    _solveState = NONUNIQUESOLUTION;
                    _stateDescription = "NonUnique";
                    return;
                }
            }
        }
        for (int j = 0; j < _numVariables; j++) {
            int numNonZeroP = 0;
            for (int i = 0; i < _numConstraints; i++) {
                if (_arrayP[i][j] != 0)
                    numNonZeroP++;
            }
            if (numNonZeroP == 0) {
                _solveState = NOSOLUTION;
                _stateDescription = _variables[j] + " is unbound";
                return;
            }
            if (numNonZeroP > 1) {
                _solveState = NOSOLUTION;
                _stateDescription = _variables[j] + " is ambiguous";
                return;
            }
        }
    }

    private int[] _branchesFrom(Index g) {
        int k = g.getK();
        int l = g.getL();
        int num = 0;
        for (int i = 0; i < _numConstraints; i++) {
            if (i != k && _arrayP[i][l] != 0)
                num++;
        }
        int retv[] = new int[num];
        int index = 0;
        for (int i = 0; i < _numConstraints; i++) {
            if (i != k && _arrayP[i][l] != 0)
                retv[index++] = i;
        }
        return retv;
    }

    private void _createAnnotations() {
        _varBindings = new String[_numVariables];
        _constraintExplanations = new String[_numConstraints];
        _varValid = new boolean[_numVariables];
        _constraintConsistent = new boolean[_numConstraints];
        for (int i = 0; i < _numConstraints; i++) {
            _constraintConsistent[i] = true;
            _constraintExplanations[i] = "";
            int numNonZeroP = 0;
            for (int j = 0; j < _numVariables; j++) {
                if (_arrayP[i][j] != 0)
                    numNonZeroP++;
            }
            if (numNonZeroP == 0
                && !_vectorA[i].equals(UnitLibrary.Identity)) {
                Unit factor = _vectorA[i].invert();
                String uString = factor.commonDesc();
                _constraintConsistent[i] = false;
                _constraintExplanations[i] += uString;
            } else if (
                numNonZeroP > 1 && _vectorA[i].equals(UnitLibrary.Identity)) {
                _constraintConsistent[i] = false;
            }
        }
        for (int j = 0; j < _numVariables; j++) {
            _varBindings[j] = "";
            int numNonZeroP = 0;
            for (int i = 0; i < _numConstraints; i++) {
                if (_arrayP[i][j] != 0) {
                    Unit U = _vectorA[i].pow(1.0 / _arrayP[i][j]);
                    if (numNonZeroP > 0) {
                        _varBindings[j] += ";";
                    }
                    _varBindings[j] += U.commonDesc();
                    numNonZeroP++;
                }
            }
            if (numNonZeroP > 1) {
                _varBindings[j] = "*AMBIGUOUS* " + _varBindings[j];
                _varValid[j] = false;
            }
            if (numNonZeroP == 1)
                _varValid[j] = true;
            else
                _varValid[j] = false;
        }
    }

    private Vector _createG() {
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
    * @param k
    * @param l
    */
    private void _eliminate(Index g) {
        int k = g.getK();
        int l = g.getL();
        if (_debug) {
            System.out.println("Eliminating (" + k + ", " + l + ")");
        }
        Unit U = _vectorA[k].pow(1.0 / _arrayP[k][l]);
        _vectorA[k] = U;
        _arrayP[k][l] = 1;
        for (int i = 0; i < _numConstraints; i++) {
            if (i != k && !_done[i] && _arrayP[i][l] != 0) {
                _vectorA[i] = _vectorA[i].divideBy(U.pow(_arrayP[i][l]));
                _arrayP[i][l] = 0;
            }
        }
        _setBranchPoint(g);
        _done[k] = true;
    }

    /**
     * Finds all Index(i, l) such that P[i][l] != 0 and 1) all other P in row i
     * is equal to 0
     *
     * @return Index(i, l)
     */
    private Vector _findAllG() {
        Vector retv = new Vector();
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
                    retv.add(new Index(i, l));
                    continue;
                }
            }
        }
        return retv;
    }

    /**
     * @param rows
     * @return
     */
    private Vector _findAllGInRows(int[] rows) {
        Vector retv = new Vector();
        for (int a = 0; a < rows.length; a++) {
            int k = rows[a];
            Index g = _findGInRow(k);
            if (g != null) {
                retv.add(g);
            }
        }
        return retv;
    }

    /**
     * Finds an Index(i, l) such that P[i][l] != 0 and 1) all other P in row i
     * is equal to 0
     *
     * @return Index(i, l)
     */
    private Index _findG() {
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
     * @param k
     * @return
     */
    private Index _findGInRow(int k) {
        Index retv = null;
        int l = -1;
        for (int j = 0; j < _numVariables; j++) {
            if (_arrayP[k][j] != 0) {
                if (l == -1) {
                    l = j;
                } else {
                    return null;
                }
            }
        }
        if (l == -1)
            return null;
        return (new Index(k, l));
    }

    private boolean _gUnique(Vector G) {
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
     * @param variableLabel
     * @return The index of the variable.
     */
    private int _getVarIndex(String variableLabel) {
        for (int i = 0; i < _variables.length; i++) {
            if (_variables[i].equals(variableLabel)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return The bindings.
     */
    private Bindings _makeBindings() {
        Bindings retv = new Bindings();
        for (int variableNum = 0; variableNum < _numVariables; variableNum++) {
            int numNonZero = 0;
            int row = -1;
            for (int i = 0; i < _numConstraints; i++) {
                if (_arrayP[i][variableNum] != 0.0) {
                    numNonZero++;
                    row = i;
                }
            }
            if (numNonZero == 1) {
                Unit U = _vectorA[row].pow(1.0 / _arrayP[row][variableNum]);
                retv.put(_variables[variableNum], U);
            }
        }
        return retv;
    }

    private Vector _partialSolveRecursively(int level, Index g) {
        Vector retv = new Vector();
        if (_debug) {
            System.out.print(
                "\nSolver._eliminateRecursively level "
                    + level
                    + " BrancPoint "
                    + g
                    + "\n"
                    + state());
        }

        int rows[] = _branchesFrom(g);
        _eliminate(g);
        Vector branchPoints = _findAllGInRows(rows);
        if (_debug) {
            System.out.print("Branch Rows at level " + level + " for " + g);
            for (int a = 0; a < rows.length; a++) {
                System.out.print(" " + rows[a]);
            }
            System.out.print("\nRemaining BranchPoints");
            for (int a = 0; a < branchPoints.size(); a++) {
                System.out.print(" " + (Index) (branchPoints.elementAt(a)));
            }
            System.out.print("\n");
        }
        if (branchPoints.size() > 0) {
            for (int gi = 0; gi < branchPoints.size(); gi++) {
                Solver s = copy();
                Vector results =
                    s._partialSolveRecursively(
                        level + 1,
                        (Index) (branchPoints.elementAt(gi)));
                if (results != null) {
                    retv.addAll(results);
                }
            }
        } else {
            _analyzeState();
            if (_debug) {
                System.out.println(
                    "Solver.solve final level " + level + state());
                Solver s = this;
                int ll = level;
                while (s != null) {
                    System.out.print(
                        "Solver.backtrace level " + ll-- +"\n" + s.state());
                    s = s._upper;
                }
            }
            retv.add(this);
        }
        return retv;
    }

    /**
     * @param g
     */
    private void _setBranchPoint(Index g) {
        _branchPoint = g;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    private static final int NOTRUN = -1;
    private static final int SOLVED = 0;
    private static final int NOSOLUTION = 1;
    private static final int NONUNIQUESOLUTION = 2;
    double _arrayP[][];
    String _varBindings[] = null;
    String _constraintExplanations[] = null;
    boolean _varValid[] = null;
    boolean _constraintConsistent[] = null;
    Index _branchPoint = null;
    boolean _debug = false;
    boolean _done[];
    TypedCompositeActor _model;
    int _numConstraints = 0;
    int _numVariables = 0;
    private static final DecimalFormat _pFormat = new DecimalFormat(" 0;-0");
    String _stateDescription = "No description";
    private int _solveState = NOTRUN;
    NamedObj _source[];
    Solver _upper = null;
    String _variables[];
    Unit _vectorA[];
    private static final DecimalFormat _vNumFormat = new DecimalFormat("00");

    ///////////////////////////////////////////////////////////////////
    ////                     inner class                           ////

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
