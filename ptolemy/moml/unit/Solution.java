/* Class for representing a solution.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
 */
package ptolemy.moml.unit;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// Solution

/** An instance of this class contains a "solution" of Unit constraints.
 In essence, the solution represents the constraints between a set of Unit
 variables, and a set of Units.
 The table below illustrates this.
 <table border = "1">
 <caption>Unit Constraints</caption>
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
 Generally, this class is used by creating an instance that is derived from
 the Units specifications of a model.
 Then, a method is invoked that results in other instances being created that
 are transformations of the original instance. These transformed instances
 are equivalent, in a sense, to the original instance. The difference is that
 they provide a different perspective than that of the original instance. In
 particular, some of the transformed instances can be used to highlight
 inconsistencies not apparent in the original instance.
 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
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
    public Solution(TypedCompositeActor model, String[] vLabels,
            Vector constraints) throws IllegalActionException {
        _constraints = constraints;
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

        for (int constraintNum = 0; constraintNum < _numConstraints; constraintNum++) {
            UnitEquation constraint = (UnitEquation) constraints
                    .elementAt(constraintNum);
            UnitEquation canonicalEquation = constraint.canonicalize();
            Vector rightUTerms = canonicalEquation.getRhs().getUTerms();

            if (rightUTerms.size() != 1) {
                throw new IllegalActionException("canonicalEquation "
                        + canonicalEquation + " has nonsingular RHS");
            }

            UnitTerm rhsUterm = (UnitTerm) rightUTerms.elementAt(0);

            if (!rhsUterm.isUnit()) {
                throw new IllegalActionException("canonicalEquation "
                        + canonicalEquation + " has nonUnit RHS");
            }

            _vectorA[constraintNum] = rhsUterm.getUnit();
            _source[constraintNum] = constraint.getSource();

            Vector leftUTerms = canonicalEquation.getLhs().getUTerms();

            for (int i = 0; i < leftUTerms.size(); i++) {
                UnitTerm leftUTerm = (UnitTerm) leftUTerms.elementAt(i);

                if (leftUTerm == null) {
                    throw new IllegalActionException("canonicalEquation "
                            + canonicalEquation + " has nonVar LHS");
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
    ////                         public methods                    ////

    /**
     * Annotates the model so that when it is displayed it will be color coded
     * and have tooltips that will convey various aspects of the solution.
     */
    public void annotateGraph() {
        if (_debug) {
            trace();
        }

        String colorString;
        StringBuffer moml = new StringBuffer();

        for (int j = 0; j < _numVariables; j++) {
            String explanation = _varBindings[j];
            Color colorValue = null;

            if (_varState[j] == _CONSISTENT) {
                colorValue = Color.GREEN;
            } else if (_varState[j] == _INCONSISTENT) {
                colorValue = Color.RED;
            }

            colorString = _getColorString(colorValue);
            moml.append("<port name=\"" + _variables[j] + "\">"
                    + " <property name=\"_color\" "
                    + "class = \"ptolemy.actor.gui.ColorAttribute\" "
                    + "value = \"" + colorString + "\"/>"
                    + "<property name=\"_explanation\" "
                    + "class = \"ptolemy.kernel.util.StringAttribute\" "
                    + "value = \"" + explanation + "\"/>" + "</port>");
        }

        for (int constraintNum = 0; constraintNum < _numConstraints; constraintNum++) {
            NamedObj source = _source[constraintNum];
            String expression = _constraintExplanations[constraintNum];
            Color colorValue = null;

            if (_constraintState[constraintNum] == _CONSISTENT) {
                colorValue = Color.GREEN;
            } else if (_constraintState[constraintNum] == _INCONSISTENT) {
                colorValue = Color.RED;
            }

            colorString = _getColorString(colorValue);
            if (source instanceof IOPort) {
                IOPort port = (IOPort) source;
                ComponentEntity actor = (ComponentEntity) port.getContainer();
                moml.append("<entity name=\"" + actor.getName() + "\">"
                        + _momlAnnotate(port, colorString, expression)
                        + "</entity>");
            } else if (source instanceof IORelation) {
                IORelation relation = (IORelation) source;
                moml.append(_momlAnnotate(relation, colorString, expression));
            } else if (source instanceof ComponentEntity) {
                ComponentEntity componentEntity = (ComponentEntity) source;
                moml.append(_momlAnnotate(componentEntity, colorString,
                        expression));
            }
        }

        if (moml.length() > 0) {
            String momlUpdate = "<group>" + moml.toString() + "</group>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, _model,
                    momlUpdate);
            request.setUndoable(true);
            request.setPersistent(false);
            _debug("Solver.annotateGraph moml " + momlUpdate);
            _model.requestChange(request);
        }
    }

    /** Search for a complete solution.
     * @return The solution.
     */
    public Solution completeSolution() {
        _debug("Solver.completeSolution.initial " + headerInfo() + stateInfo());

        Index g;

        while ((g = _findG()) != null) {
            _eliminate(g);
            _debug("Solver.completeSolution " + stateInfo());
        }

        for (int i = 0; i < _numConstraints; i++) {
            _done[i] = true;
        }

        _analyzeState();

        if (_debug) {
            trace();
        }

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
        retv._constraints = _constraints;
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

    /** Get the state of the solution.
     * @return The state of the solution.
     */
    public String getShortStateDesc() {
        switch (_solveState) {
        case _UNKNOWN:
            return "UnKnown";

        case _NONUNIQUE:
            return "No Unique Solution";

        case _INCONSISTENT:
            return "Inconsistent";

        case _CONSISTENT:
            return "Consistent";
        }

        return "Unknown";
    }

    /** Get the state of the solution.
     * @return The state of the solution.
     */
    public String getStateDesc() {
        return _stateDescription;
    }

    /**
     * Create a human readable presentation of the parts of the solution that
     * won't change as a result of
     * the operations necessary to carry out the Gaussian elimination.
     * I.e. the variable names, and the constraints.
     * @return A StringBuffer with a human readable presentation of the
     * invariant parts of the solution.
     */
    public StringBuffer headerInfo() {
        StringBuffer retv = new StringBuffer();
        retv.append("Header\nVariables\n");

        for (int j = 0; j < _numVariables; j++) {
            retv.append("   " + _vNumFormat.format(j) + " " + _variables[j]
                    + "\n");
        }

        retv.append("\n");
        retv.append("ConstrNum  Source\n");

        for (int i = 0; i < _numConstraints; i++) {
            NamedObj source = _source[i];
            retv.append(""
                    + _vNumFormat.format(i)
                    + "         "
                    + source.toString()
                    + " "
                    + ((UnitEquation) _constraints.elementAt(i))
                    .descriptiveForm() + "\n");
        }

        retv.append("\\Header\n");
        return retv;
    }

    /** Produce all of the minimal span solutions that can be generated from
     * this instance. A minimal span solution is one in which a minimal number
     * of constraints that are connected yield an inconsistency.
     * @return The vector of minimal span solutions.
     */
    public Vector minimalSpanSolutions() {
        Vector solutions = new Vector();
        _debug("Solver.minimalSpanSolutions " + headerInfo() + " initial\n"
                + stateInfo());

        Solution root = copy();

        // First eliminate all the singletons. These are due to statically bound
        // ports.
        Iterator allG = root._findAllG().iterator();

        while (allG.hasNext()) {
            Index g = (Index) allG.next();
            root._eliminate(g);
        }

        // The solution may already be inconsistent. This would be the case if
        // two statically bound ports are connected but have different units.
        root._checkForInConsistency();

        if (root._solveState == _INCONSISTENT) {
            root._analyzeState();
            solutions.add(root);
        } else {
            // The root solution is consistent. Now use the root as the starting
            // point to generate all possible minimally spanning solutions.
            root._branchPoint = null;
            _debug("Solver.solve root\n" + root.stateInfo());
            root._branchPoints = root._findAllG();

            if (root._branchPoints.size() > 0) {
                for (int i = 0; i < root._branchPoints.size(); i++) {
                    Solution s = root.copy();
                    Vector results = s._partialSolveRecursively(1,
                            (Index) root._branchPoints.elementAt(i));
                    solutions.addAll(results);
                }
            } else {
                root._analyzeState();
                solutions.add(root);
            }
        }

        if (_debug) {
            for (int i = 0; i < solutions.size(); i++) {
                Solution solution = (Solution) solutions.elementAt(i);
                System.out.println("A Solution\n" + solution.stateInfo());
            }
        }

        return solutions;
    }

    /**
     * The current state of the solver. A StringBuffer is produced that shows
     * the variables, done vector, P array, and A vector in a human readable
     * arrangement.
     *
     * @return A StringBuffer with the state of the solver.
     */
    public StringBuffer stateInfo() {
        StringBuffer retv = new StringBuffer();
        retv.append("State\n");
        retv.append("BranchPoints " + _branchPoints + "\n    ");

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

            retv.append("" + _vectorA[i] + " " + _vectorA[i].descriptiveForm());
            retv.append(" "
                    + ((UnitEquation) _constraints.elementAt(i))
                    .descriptiveForm());
            retv.append("\n");
        }

        if (_branchPoint == null) {
            retv.append("BranchPoint = null\n");
        } else {
            retv.append("BranchPoint = " + _branchPoint.toString() + "\n");
        }

        retv.append("Solution: " + getStateDesc());
        retv.append("\n\\State\n");
        return retv;
    }

    public void trace() {
        System.out.print("Solver.trace\n");

        Solution s = this;

        while (s != null) {
            System.out.print(s.stateInfo());
            s = s._upper;
        }

        System.out.print(headerInfo());
    }

    ///////////////////////////////////////////////////////////////////
    /////                        private methods                 //////
    private void _analyzeState() {
        _varBindings = new String[_numVariables];
        _constraintExplanations = new String[_numConstraints];
        _varState = new int[_numVariables];
        _constraintState = new int[_numConstraints];

        StringBuffer inconsistencyDesc = new StringBuffer();

        for (int i = 0; i < _numConstraints; i++) {
            _constraintState[i] = _UNKNOWN;
            _constraintExplanations[i] = "";

            if (_done[i]) {
                int numNonZeroP = 0;

                for (int j = 0; j < _numVariables; j++) {
                    if (_arrayP[i][j] != 0) {
                        numNonZeroP++;
                    }
                }

                if (numNonZeroP == 0
                        && !_vectorA[i].equals(UnitLibrary.Identity)) {
                    Unit factor = _vectorA[i].invert();
                    String uString = factor.descriptiveForm();
                    _constraintState[i] = _INCONSISTENT;
                    _constraintExplanations[i] = uString;
                } else if (numNonZeroP > 1
                        && _vectorA[i].equals(UnitLibrary.Identity)) {
                    _constraintState[i] = _NONUNIQUE;
                } else {
                    String uString = _vectorA[i].descriptiveForm();
                    _constraintState[i] = _CONSISTENT;
                    _constraintExplanations[i] = uString;
                }
            }
        }

        for (int j = 0; j < _numVariables; j++) {
            _varBindings[j] = "";

            int numNonZeroP = 0;

            for (int i = 0; i < _numConstraints; i++) {
                if (_done[i] && _arrayP[i][j] != 0) {
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
                _varState[j] = _INCONSISTENT;
            }

            if (numNonZeroP == 1) {
                _varState[j] = _CONSISTENT;
            } else {
                _varBindings[j] = "<Unbound>";
                _varState[j] = _NONUNIQUE;
            }
        }

        boolean stateInconsistent = false;
        boolean stateNonUnique = false;
        _solveState = _CONSISTENT;

        for (int i = 0; i < _numConstraints; i++) {
            switch (_constraintState[i]) {
            case _INCONSISTENT: {
                stateInconsistent = true;

                NamedObj source = _source[i];
                String sourceName = "NoSource";

                if (source instanceof IORelation) {
                    sourceName = ((IORelation) source).getName();
                } else if (source instanceof ComponentEntity) {
                    sourceName = ((ComponentEntity) source).getName();
                } else if (source instanceof TypedIOPort) {
                    sourceName = source.getName(source.getContainer()
                            .getContainer());
                }

                inconsistencyDesc.append(" " + sourceName + " "
                        + _constraintExplanations[i]);
                break;
            }

            case _NONUNIQUE: {
                stateNonUnique = true;
                break;
            }
            }
        }

        if (stateInconsistent && stateNonUnique) {
            _debug("State is both Inconsistent and NonUnique");
        }

        for (int j = 0; j < _numVariables; j++) {
            if (_varState[j] == _INCONSISTENT) {
                stateInconsistent = true;
                inconsistencyDesc.append(" " + _variables[j] + "="
                        + _varBindings[j]);
            }
        }

        if (stateInconsistent) {
            _solveState = _INCONSISTENT;
        } else if (stateNonUnique) {
            _solveState = _NONUNIQUE;
        } else {
            _solveState = _CONSISTENT;
        }

        switch (_solveState) {
        case _UNKNOWN: {
            _stateDescription = "UnKnown";
            break;
        }

        case _NONUNIQUE: {
            _stateDescription = "No Unique Solution";
            break;
        }

        case _INCONSISTENT: {
            _stateDescription = "Inconsistent" + inconsistencyDesc;
            break;
        }

        case _CONSISTENT: {
            _stateDescription = "Consistent";
            break;
        }
        }
    }

    private int[] _branchesFrom(Index g) {
        int k = g.getK();
        int l = g.getL();
        int num = 0;

        for (int i = 0; i < _numConstraints; i++) {
            if (i != k && _arrayP[i][l] != 0) {
                num++;
            }
        }

        int[] retv = new int[num];
        int index = 0;

        for (int i = 0; i < _numConstraints; i++) {
            if (i != k && _arrayP[i][l] != 0) {
                retv[index++] = i;
            }
        }

        return retv;
    }

    private void _checkForInConsistency() {
        for (int i = 0; i < _numConstraints; i++) {
            if (!_vectorA[i].equals(UnitLibrary.Identity)) {
                boolean inconsistent = true;

                for (int j = 0; j < _numVariables; j++) {
                    if (_arrayP[i][j] != 0) {
                        inconsistent = false;
                    }
                }

                if (inconsistent) {
                    _done[i] = true;
                    _solveState = _INCONSISTENT;
                }
            }
        }
    }

    private void _debug(String msg) {
        if (_debug) {
            System.out.println(msg);
        }
    }

    /**
     * @param g
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
     * @return A vector of Indexes.
     */
    private Vector _findAllGInRows(int[] rows) {
        Vector retv = new Vector();

        for (int k : rows) {
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
                    return new Index(i, l);
                }
            }
        }

        return null;
    }

    private Index _findGInRow(int k) {
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

        if (l == -1) {
            return null;
        }

        return new Index(k, l);
    }

    /** Return the string representation of the color value.
     *  @param colorValue The input color value.
     *  @return The string representation of the color as a Ptolemy expression
     *   string array of double values.
     */
    private String _getColorString(Color colorValue) {
        if (colorValue == null) {
            return "";
        } else {
            float[] colorArray = colorValue.getRGBComponents(null);
            return "{ " + colorArray[0] + ", " + colorArray[1] + ", "
            + colorArray[2] + ", " + colorArray[3] + " }";
        }
    }

    private String _momlAnnotate(NamedObj entity, String color,
            String expression) {
        String colorProperty = null;
        // We don't use ptolemy.actor.gui.ColorAttribute here
        // because we don't want to add a dependency between
        // moml.unit and actor.gui
        Attribute currentColor = entity.getAttribute("_color");

        if (currentColor != null && color == null) {
            colorProperty = "<deleteProperty _name=_color/>";
        } else if (color != null) {
            colorProperty = "<property name=\"_color\" "
                    + "class = \"ptolemy.actor.gui.ColorAttribute\" "
                    + "value = \"" + color + "\"/>";
        }

        return "<" + entity.getElementName() + " name=\"" + entity.getName()
                + "\" class=\"" + entity.getClassName() + "\">" + colorProperty
                + "<property name=\"_explanation\" "
                + "class = \"ptolemy.kernel.util.StringAttribute\" "
                + "value = \"" + expression + "\"/></"
                + entity.getElementName() + ">";
    }

    private Vector _partialSolveRecursively(int level, Index g) {
        Vector retv = new Vector();
        _debug("\nSolver._eliminateRecursively level " + level + " BrancPoint "
                + g + "\n" + stateInfo());

        int[] rows = _branchesFrom(g);
        _eliminate(g);
        _checkForInConsistency();
        _branchPoints = _findAllGInRows(rows);

        if (_solveState != _INCONSISTENT && _branchPoints.size() > 0) {
            if (_debug) {
                System.out.print("Branch Rows at level " + level + " for " + g);

                for (int row : rows) {
                    System.out.print(" " + row);
                }

                System.out.print("\nRemaining BranchPoints");

                for (int a = 0; a < _branchPoints.size(); a++) {
                    System.out.print(" " + _branchPoints.elementAt(a));
                }

                System.out.print("\n");
            }

            for (int gi = 0; gi < _branchPoints.size(); gi++) {
                Solution s = copy();
                Vector results = s._partialSolveRecursively(level + 1,
                        (Index) _branchPoints.elementAt(gi));

                if (results != null) {
                    retv.addAll(results);
                }
            }
        } else {
            _analyzeState();

            if (_debug) {
                trace();
            }

            retv.add(this);
        }

        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static final int _UNKNOWN = -1;

    private static final int _CONSISTENT = 0;

    private static final int _INCONSISTENT = 1;

    private static final int _NONUNIQUE = 2;

    double[][] _arrayP;

    Index _branchPoint = null;

    Vector _branchPoints = null;

    Vector _constraints = null;

    String[] _constraintExplanations = null;

    int[] _constraintState = null;

    boolean _debug = false;

    boolean[] _done;

    TypedCompositeActor _model;

    int _numConstraints = 0;

    int _numVariables = 0;

    private static final DecimalFormat _pFormat = new DecimalFormat(" 0;-0");

    private int _solveState = _UNKNOWN;

    NamedObj[] _source;

    String _stateDescription = "No description";

    Solution _upper = null;

    String[] _varBindings = null;

    String[] _variables;

    int[] _varState = null;

    Unit[] _vectorA;

    private static final DecimalFormat _vNumFormat = new DecimalFormat("00");

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /**
     * Class that represents an index in the P array.
     *
     */
    private static class Index {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        int k;

        int l;

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

        @Override
        public String toString() {
            return "(" + k + "," + l + ")";
        }
    }
}
