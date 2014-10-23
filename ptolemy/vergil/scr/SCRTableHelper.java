/* Helper functions for SCR Tables.

 Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.vergil.scr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;

/**
Helper functions for SCR Tables.
@author Patricia Derler
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (pd)
@Pt.AcceptedRating Red (pd)
 */
public class SCRTableHelper {

    /** Search towards the left of the string.
     * @param expression The expression to be searched.
     * @param openingBracket the index of the open bracket.
     * @return The index of the matching open bracket.
     */
    public static int indexOfMatchingOpenBracket(String expression,
            int openingBracket) {
        int openBrackets = 1;
        int i = openingBracket - 1;
        while (openBrackets != 0) {
            char c = expression.charAt(i);
            if (c == '(') {
                openBrackets = openBrackets + 1;
            } else if (c == ')') {
                openBrackets = openBrackets - 1;
            }
            i = i - 1;
        }
        return i;
    }

    /** Search towards the right of the string.
     * @param expression The expression to be searched.
     * @param openingBracket the index of the open bracket.
     * @return The index of the matching close bracket.
     */
    public static int indexOfMatchingCloseBracket(String expression,
            int openingBracket) {
        int openBrackets = 1;
        int i = openingBracket + 1;
        while (openBrackets != 0) {
            char c = expression.charAt(i);
            if (c == '(') {
                openBrackets = openBrackets + 1;
            } else if (c == ')') {
                openBrackets = openBrackets - 1;
            }
            i = i + 1;
        }
        return i;
    }

    /** Returns a self transition on a given state, if there is one, null otherwise.
     * @param state The given state.
     * @return The self transition or null.
     */
    public static Transition getSelfTransition(State state) {
        List relationList = state.outgoingPort.linkedRelationList();
        for (Object object : relationList) {
            Transition t = (Transition) object;
            // found a self transition
            if (t.destinationState() == state) {
                return t;
            }
        }
        return null;
    }

    /** Return the content index.
     *  @param rowIndex the index of the row.
     *  @param columnIndex the index of the column.
     *  @param columnCount The number of columns.
     *  @return the content index.
     */
    public static int getContentIndex(int rowIndex, int columnIndex,
            int columnCount) {
        return (columnIndex - 1) + (columnCount - 1) * rowIndex;
    }

    /** Check that all modes are unique. -- by definition
     * Check that all values are unique.
     * Check that pairwise OR of events in a row is always false.
     * --- check coverage: AND of all events in a row is true
     * @param _tableContent The table content.
     * @param rowCount the number of rows.
     * @param columnCount the number of columns.
     * @param model the model to be checked.
     */
    public static void checkDisjointness(List<String> _tableContent,
            int rowCount, int columnCount, FSMActor model)
            throws IllegalActionException {
        //Check that all values are unique.
        List<String> subList = _tableContent.subList(
                getContentIndex(rowCount - 1, 1, columnCount),
                getContentIndex(rowCount - 1, columnCount, columnCount));

        Set<String> set = new HashSet<String>(subList);

        if (set.size() < subList.size()) {
            throw new IllegalActionException("There are value duplicates!");
        }

        ASTPtRootNode _parseTree = null;
        ParseTreeEvaluator _parseTreeEvaluator = null;
        VariableScope _scope = null;

        //Check that pairwise OR of events in a row is always false.
        for (int i = 0; i < rowCount - 1; i++) { // for every mode
            for (int j = 1; j < columnCount; j++) {
                String event = _tableContent.get(getContentIndex(i, j,
                        columnCount));
                for (int k = j + 1; k < columnCount; k++) {
                    String event2 = _tableContent.get(getContentIndex(i, k,
                            columnCount));

                    String condition = event + " & " + event2;
                    condition = condition.replace("@T(Inmode)", "true");

                    if (_parseTree == null) {
                        PtParser parser = new PtParser();
                        _parseTree = parser.generateParseTree(condition);
                    }

                    if (_parseTreeEvaluator == null) {
                        _parseTreeEvaluator = new ParseTreeEvaluator();
                    }

                    if (_scope == null) {
                        _scope = new VariableScope(model);
                    }

                    Token result = _parseTreeEvaluator.evaluateParseTree(
                            _parseTree, _scope);
                    BooleanToken booleanResult = (BooleanToken) result;
                    if (booleanResult.booleanValue()) {
                        throw new IllegalActionException(
                                "Disjointness Criteria missed");
                    }
                }
            }
        }
    }

    /** Return the self transition.
     *  @param state The state.
     *  @param parameter the parameter.
     *  @return the self transition.
     */
    public static Transition getSelfTransition(State state, Parameter parameter) {
        for (Object relation : state.incomingPort.linkedRelationList()) {
            if (state.outgoingPort.linkedRelationList().contains(relation)) {
                Transition transition = (Transition) relation;
                if (parameter != null) {
                    try {
                        if (transition.setActions.getDestinations().contains(
                                parameter)) {
                            return transition;
                        }
                    } catch (IllegalActionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    return transition;
                }
            }
        }
        return null;
    }

}
