/* Class for representing a solution.

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
//// Solution
/** An instance of this class contains a "solution" of Unit constraints.
In essence, the solution represents the constraints between a set of Unit
variables, and a set of Units.
The table below illustrates this.
<TABLE BORDER = "1">
 <TR>
  <TH></TH>
  <TH>V1</TH>
  <TH>V2</TH>
  <TH>...</TH>
  <TH>Vl</TH>
  <TH></TH>
</TR>

 <TR>
  <TD>C1</TD>
  <TD>P11</TD>
  <TD>P12</TD>
  <TD>...</TD>
  <TD>P1l</TD>
  <TD>U1</TD>
</TR>

 <TR>
   <TD>C2</TD>
  <TD>P21</TD>
  <TD>P22</TD>
  <TD>...</TD>
  <TD>P2l</TD>
  <TD>U2</TD>
</TR>

 <TR>
   <TD>:</TD>
  <TD>:</TD>
  <TD>:</TD>
  <TD>...</TD>
  <TD>:</TD>
  <TD>:</TD>
</TR>

 <TR>
   <TD>Ck</TD>
  <TD>Pk1</TD>
  <TD>Pk2</TD>
  <TD>...</TD>
  <TD>Pkl</TD>
  <TD>Uk</TD>
</TR>
</TABLE>
Here, the columns V1, V2, ..., Vl represent l variables.
The columns C1, C2, ..., Ck represent k constraints.
The U1, U2, ..., Uk on the right represent Units.
The meaning of the ith row is that V1^Pi1 + V2^pi2 + .. +Vk^pik = Uk.
<p>
Generally, this class is used by creating an instance that is derived from the
Units specifications of a model.
Then, a method is invoked that results in other instances being created that are
a transformations of the original instance. These transformed instances are
equivalent, in a sense, to the original instance. The difference is that they
provide a different perspective than that of the original instance. In
particular, some of the transformed instances can be used to highlight
inconsistencies not apparent in the original instance.
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class Solution {

    /** Construct an empty solution.
     * This constructor is used only by the copy() method, and, can therefore,
     * be made private.
     *
     */
    private Solution() {
    }

    /**
     * Construct a Solution from a set of variables, and a set of constraints.
     *
     * @param model The model that is the source of the variables and
     * constraints.
     * @param vLabels The variables.
     * @param constraints The constraints.
     * @exception IllegalActionException If there are problems with transforming
     * a constraint to canonical form.
     */
    public Solution(
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
            UnitEquation canonicalEquation = constraint.canonicalize();
            Vector rightUTerms = canonicalEquation.getRhs().getUTerms();
            if (rightUTerms.size() != 1) {
                throw new IllegalActionException(
                    "canonicalEquation "
                        + canonicalEquation
                        + " has nonsingular RHS");
            }
            UnitTerm rhsUterm = (UnitTerm) (rightUTerms.elementAt(0));
            if (!rhsUterm.isUnit()) {
                throw new IllegalActionException(
                    "canonicalEquation "
                        + canonicalEquation
                        + " has nonUnit RHS");
            }
            _vectorA[constraintNum] = rhsUterm.getUnit();
            _source[constraintNum] = constraint.getSource();
            Vector leftUTerms = canonicalEquation.getLhs().getUTerms();
            for (int i = 0; i < leftUTerms.size(); i++) {
                UnitTerm leftUTerm = (UnitTerm) (leftUTerms.elementAt(i));
                if (leftUTerm == null) {
                    throw new IllegalActionException(
                        "canonicalEquation "
                            + canonicalEquation
                            + " has nonVar LHS");
                }
                String variableLabel = leftUTerm.getVariable();
                double exponent = leftUTerm.getExponent();
                int varIndex = -1;
                for (int j = 0; j < _variables.length; j++) {
                    if (_variables[j].equals(variableLabel)) {
                        varIndex = j;
                        break;
                    }
                }
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
            String explanation = _varBindings[j];
            if (_varValid[j]) {
                color = "green";
            } else {
                color = "magenta";
            }

            moml.append(
                "<port name=\""
                    + _variables[j]
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
            String expression = _vectorA[constraintNum].descriptiveForm();

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
            _debug("Solver.annotateGraph moml " + momlUpdate);
            _model.requestChange(request);
        }
    }

    /** Search for a complete solution.
     * @return The solution.
     */
    public Solution completeSolution() {
        _debug("Solver.solve " + _header() + " initial\n" + _state());
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
        return this;
    }

    /** Make a copy of this solution.
     * @return The copy.
     */
    public Solution copy() {
        Solution retv = new Solution();
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

    /** Create a short description of the state of the solution. In the case
     * that the solution is inconsistent a short description of the
     * inconsistency is included.
     * @return The short description.
     */
    public String getShortDescription() {
        return _stateDescription;
    }

    /** Get the state of the solution.
     * @return The state of the solution.
     */
    public String getStateDesc() {
        switch (_solveState) {
            case _NOTRUN :
                {
                    return "NotRun";
                }
            case _NONUNIQUE :
                {
                    return "No Unique Solution";
                }
            case _INCONSISTENT :
                {
                    return "Inconsistent";
                }
            case _CONSISTENT :
                {
                    return "Consistent";
                }
        }
        return null;
    }

    /** Produce all of the minimal span solutions that can be generated from
     * this instance. A minimal span solution is one in which a minimal number
     * of constraints that are connected yield an inconsistency.
     * @return The vector of minimal span solutions.
     */
    public Vector minimalSpanSolutions() {
        _debug("Solver.solve " + _header() + " initial\n" + _state());
        // Eliminate the singletons (due to Ports)
        Iterator allG = _findAllG().iterator();
        while (allG.hasNext()) {
            Index g = (Index) (allG.next());
            _eliminate(g);
        }
        _debug("Solver.solve initialized\n" + _state());
        Vector branchPoints = _findAllG();
        Vector solutions = new Vector();
        if (branchPoints.size() > 0) {
            for (int i = 0; i < branchPoints.size(); i++) {
                Solution s = copy();
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
                Solution solution = (Solution) (solutions.elementAt(i));
                System.out.println("A Solution\n" + solution._state());
            }
        }
        return solutions;

    }

    /** Specify whether or not to have debugging information produced.
     * @param debug True to see debugging information in standard output, false
     *            otherwise
     */
    public void setDebug(boolean debug) {
        this._debug = debug;
    }

    ///////////////////////////////////////////////////////////////////
    /////                        private methods                 //////

    private void _analyzeState() {
        _solveState = _CONSISTENT;
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
                    _solveState = _INCONSISTENT;
                    Unit factor = _vectorA[i].invert();
                    String uString = factor.descriptiveForm();
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
                    _solveState = _NONUNIQUE;
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
                _solveState = _INCONSISTENT;
                _stateDescription = _variables[j] + " is unbound";
                return;
            }
            if (numNonZeroP > 1) {
                _solveState = _INCONSISTENT;
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
                String uString = factor.descriptiveForm();
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
                    _varBindings[j] += U.descriptiveForm();
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

    private void _debug(String msg) {
        if (_debug)
            System.out.println(msg);
    }

    /**
    * @param k
    * @param l
    */
    private void _eliminate(Index g) {
        int k = g.getK();
        int l = g.getL();
        _debug("Eliminating (" + k + ", " + l + ")");
        Unit U = _vectorA[k].pow(1.0 / _arrayP[k][l]);
        _vectorA[k] = U;
        _arrayP[k][l] = 1;
        for (int i = 0; i < _numConstraints; i++) {
            if (i != k && !_done[i] && _arrayP[i][l] != 0) {
                _vectorA[i] = _vectorA[i].divideBy(U.pow(_arrayP[i][l]));
                _arrayP[i][l] = 0;
            }
        }
        _branchPoint = g;
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

    /**
     * Create a human readable presentation of the parts of the solution that
     * won't change as a result of
     * the operations necessary to carry out the Gaussian elimination.
     * I.e. the variable names, and the constraints.
     * @return A StringBuffer with a human readable presentation of the
     * invariant parts of the solution.
     */
    private StringBuffer _header() {
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

    private Vector _partialSolveRecursively(int level, Index g) {
        Vector retv = new Vector();
        _debug(
            "\nSolver._eliminateRecursively level "
                + level
                + " BrancPoint "
                + g
                + "\n"
                + _state());

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
                Solution s = copy();
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
                    "Solver.solve final level " + level + _state());
                Solution s = this;
                int ll = level;
                while (s != null) {
                    System.out.print(
                        "Solver.backtrace level " + ll-- +"\n" + s._state());
                    s = s._upper;
                }
            }
            retv.add(this);
        }
        return retv;
    }

    /**
     * The current state of the solver. A StringBuffer is produced that shows
     * the variables, done vector, P array, and A vector in a human readable
     * arrangement.
     *
     * @return A StringBuffer with the state of the solver.
     */
    private StringBuffer _state() {
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
                "" + _vectorA[i] + " " + _vectorA[i].descriptiveForm() + "\n");
        }
        if (_branchPoint == null) {
            retv.append("BranchPoint = null\n");
        } else {
            retv.append("BranchPoint = " + _branchPoint.toString() + "\n");
        }
        retv.append("Solution: ");
        retv.append(getStateDesc());
        retv.append("\n\\State\n");
        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    private static final int _NOTRUN = -1;
    private static final int _CONSISTENT = 0;
    private static final int _INCONSISTENT = 1;
    private static final int _NONUNIQUE = 2;
    double _arrayP[][];
    String _varBindings[] = null;
    String _constraintExplanations[] = null;
    boolean _varValid[] = null;
    boolean _constraintConsistent[] = null;
    Index _branchPoint = null;
    boolean _debug = true;
    boolean _done[];
    TypedCompositeActor _model;
    int _numConstraints = 0;
    int _numVariables = 0;
    private static final DecimalFormat _pFormat = new DecimalFormat(" 0;-0");
    String _stateDescription = "No description";
    private int _solveState = _NOTRUN;
    NamedObj _source[];
    Solution _upper = null;
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
