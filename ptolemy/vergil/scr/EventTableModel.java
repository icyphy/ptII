/* The event table for configuring an SCR Model.

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

import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
   The event table for configuring an SCR Model.
   @author Patricia Derler
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (pd)
   @Pt.AcceptedRating Red (pd)
 */
@SuppressWarnings("serial")
public class EventTableModel extends AbstractTableModel {

    /** Construct a new event table model for a given parameter and
     *  the FSMActor. Initialize the column count to 3.
     * @param parameter The parameter
     * @param model The model
     */
    public EventTableModel(Parameter parameter, FSMActor model) {
        _model = model;
        _parameter = parameter;
        _columnCount = 3;// FIXME should be configurable

    }

    /** Check that all modes are unique. -- by definition
     * Check that all values are unique.
     * Check that pairwise OR of events in a row is always false.
     * --- check coverage: AND of all events in a row is true
     */
   public void checkDisjointness() throws IllegalActionException {
        SCRTableHelper.checkDisjointness(_tableContent, getRowCount(),
                getColumnCount(), _model);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        _initializeTableContent();
        if (columnIndex == 0 && rowIndex < getRowCount() - 1) {
            return ((State) _model.entityList().get(rowIndex)).getName();
        } else if (columnIndex == 0) {
            return "----";
        }
        int contentIndex = _getContentIndex(rowIndex, columnIndex);
        if (contentIndex < 0) {
            return null;
        } else {
            return _tableContent.get(contentIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        int contentIndex = _getContentIndex(rowIndex, columnIndex);
        _tableContent.add(contentIndex, aValue);
        _tableContent.remove(contentIndex + 1);
    }

    /** Return true, indicating that the cell is editable.
     *  @param rowIndex The rowIndex, which is ignored in this base
     *  class.
     *  @param columnIndex The columnIndex.
     *  @return return true if columnIndex is greater than 0.
     */

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex > 0);
    }

    /** Return the row count.
     *  @return the row count   
     */
    @Override
    public int getRowCount() {
        if (_rowCount == -1) {
            _rowCount = _model.entityList(State.class).size() + 1;
        }
        return _rowCount;
    }

    /** Add a column. */
    public void addColumn() {
        for (int i = 0; i < getRowCount() - 1; i++) {
            _tableContent.add(i * (getColumnCount()) + (getColumnCount() - 1),
                    "false");
        }
        _columnCount = _columnCount + 1;
        this.fireTableStructureChanged();
        this.fireTableDataChanged();
    }

    /** Delete a column. 
     *  @param index The column to be deleted.
     */
    public void deleteColumn(int index) {
        if (index > 0 && index < getColumnCount()) {
            for (int i = getRowCount() - 1; i >= 0; i--) {
                _tableContent.remove(i * (getColumnCount() - 1) + index - 1);
            }
            _columnCount = _columnCount - 1;
            this.fireTableStructureChanged();
            this.fireTableDataChanged();
        }
    }

    /** Get the column count.
     *  @return The columnt count.
     */
    @Override
    public int getColumnCount() {
        return _columnCount;
    }

    /** Save the model. 
     *  @exception IllegalActionException  If thrown while saving the model.
     *  @exception NameDuplicationException  If thrown while saving the model.
     */
    public void saveModel() throws IllegalActionException,
    NameDuplicationException {
        for (int i = 0; i < getRowCount() - 1; i++) {
            StringBuffer insideModeExpression = new StringBuffer();
            StringBuffer enterModeExpression = new StringBuffer();
            String guard = "";
            String value = "";
            State state = (State) _model.getEntity((String) getValueAt(i, 0));
            for (int j = 0; j < getColumnCount() - 1; j++) {
                _getContentIndex(i, j);
                String condition = (String) getValueAt(i, j + 1);
                value = (String) getValueAt(getRowCount() - 1, j + 1);
                String[] conditions = _handleInmodeExpression(condition, value,
                        state);
                String enterModeCondition = "";
                if (condition.contains("@T(Inmode)")) {
                    enterModeCondition = "true";
                } else {
                    enterModeCondition = conditions[1];
                }
                condition = conditions[0];
                if (condition.equals("")) {
                    condition = "false";
                }
                if (!guard.equals("")) {
                    guard = guard + " || ";
                }
                guard = guard + condition;

                if (enterModeCondition.equals("")) {
                    enterModeCondition = "false";
                }
                insideModeExpression = insideModeExpression.append("("
                        + condition + " ? " + value + " : ");
                enterModeExpression = enterModeExpression.append("("
                        + enterModeCondition + " ? " + value + " : ");
            }
            // if no condition was true use
            // - nil ?
            // - last value again ?
            //                        insideModeExpression = insideModeExpression.append(" " + _parameter.getName());
            //                        enterModeExpression = enterModeExpression.append(" " + _parameter.getName());

            insideModeExpression = insideModeExpression.append(" nil");
            enterModeExpression = enterModeExpression.append(" nil");

            // close brackets
            for (int j = 0; j < getColumnCount() - 1; j++) {
                insideModeExpression = insideModeExpression.append(")");
                enterModeExpression = enterModeExpression.append(")");
            }

            // ?? self transitions can become problematic when several self transitions are enabled.

            // if contains _isPresent then there really is an event.
            if (guard.contains("_isPresent")) {
                Transition transition = SCRTableHelper.getSelfTransition(state,
                        _parameter);
                if (transition == null) {
                    try {
                        transition = new Transition(_model,
                                ((CompositeEntity) _model).uniqueName(state
                                        .getName() + "_transition"));
                        state.outgoingPort.link(transition);
                        state.incomingPort.link(transition);
                    } catch (IllegalActionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NameDuplicationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                transition.setActions.setExpression(_parameter.getName()
                        + " = " + insideModeExpression);
                transition.guardExpression.setExpression(guard);
            }

            //                        if (state.getRefinement() == null) {
            //                                ((RefinementActor) state.getContainer()).addRefinement(
            //                                                state,
            //                                                ((CompositeEntity) _model).uniqueName(state
            //                                                                .getName() + "_refinement"), null,
            //                                                Refinement.class.getName(), null);
            //                        }
            //                        CompositeEntity entity = (CompositeEntity) state
            //                                        .getRefinement()[0];
            //                        if (entity.attributeList(Director.class).size() == 0) {
            //                                ContinuousDirector director = new ContinuousDirector(
            //                                                entity, "Continuous Director");
            //                        }
            //
            //                        Object setVariableActor = entity.getEntity("set_" + _parameter.getName());
            //                        if (setVariableActor == null) {
            //                                setVariableActor = new SetVariable(entity,
            //                                                "set_" + _parameter.getName());
            //                        }
            //                        SetVariable setVariable = (SetVariable) setVariableActor;
            //                        setVariable.delayed.setExpression("true");
            //                        setVariable.variableName.setExpression(_parameter.getName());
            //
            //                        Object expressionActorObject = entity.getEntity("set_" + _parameter.getName() + "_expression");
            //                        TypedIORelation relation = null;
            //                        TypedIORelation relationExpressionRemoveNil = null;
            //                        RemoveNilTokens removeNilTokens = null;
            //                        if (expressionActorObject == null) {
            //                                expressionActorObject = new Expression(entity,
            //                                                "set_" + _parameter.getName() + "_expression");
            //                                relation = new TypedIORelation(entity,
            //                                                entity.uniqueName("relation"));
            //                                removeNilTokens = new RemoveNilTokens(entity, entity.uniqueName("RemoveNilTokens"));
            //                                relationExpressionRemoveNil = new TypedIORelation(entity,
            //                                                entity.uniqueName("relation"));
            //                        }
            //
            //                        Expression expressionActor = (Expression) expressionActorObject;
            //                        expressionActor.expression.setExpression(insideModeExpression.toString());
            //
            //                        for (Object portObject : ((CompositeActor) entity).inputPortList()) {
            //                                IOPort modePort = (IOPort) portObject;
            //                                TypedIOPort expressionPort = (TypedIOPort) expressionActor.getPort(modePort.getName());
            //                                if (expressionPort == null) {
            //                                        expressionPort = new TypedIOPort((ComponentEntity) expressionActorObject, modePort.getName(), true, false);
            //                                }
            //                                TypedIORelation inputRelation = null;
            //                                if (modePort.insideRelationList().size() == 0) {
            //                                        inputRelation = new TypedIORelation(entity,
            //                                                        entity.uniqueName("relation"));
            //                                } else {
            //                                        if (modePort.insideSinkPortList().contains(expressionPort)) {
            //                                                continue;
            //                                        }
            //                                        inputRelation = (TypedIORelation) modePort.insideRelationList().get(0);
            //                                }
            //                                modePort.link(inputRelation);
            //                                expressionPort.link(inputRelation);
            //                        }
            //
            //                        if (relation != null) {
            //                                expressionActor.output.link(relationExpressionRemoveNil);
            //                                removeNilTokens.input.link(relationExpressionRemoveNil);
            //                                removeNilTokens.output.link(relation);
            //                                setVariable.input.link(relation);
            //                        }

            Transition transition = null;
            boolean noSetActionSet = true;
            // set inmodeExpression/enter mode expression
            for (Object intoModeTransition : state.incomingPort
                    .linkedRelationList()) {
                if (!state.outgoingPort.linkedRelationList().contains(
                        intoModeTransition)) {
                    transition = (Transition) intoModeTransition;
                    String newExpression = _parameter.getName() + " = "
                            + enterModeExpression;
                    String expression = transition.setActions.getExpression();
                    try {
                        if (transition.setActions.getDestinations().contains(
                                _parameter)) {
                            int index = expression.indexOf(_parameter.getName()
                                    + " =");
                            String before = expression.substring(0, index);
                            int afterIdx = expression.indexOf(";", index + 1);
                            String after = "";
                            if (afterIdx > 0) {
                                after = expression.substring(afterIdx).trim();
                                if (after.startsWith(";")) {
                                    after = after.substring(1).trim();
                                }
                            }
                            expression = before + after;
                            expression = expression.trim();
                        }
                        if (!expression.equals("") && !expression.endsWith(";")) {
                            expression = expression + "; ";
                        }
                        transition.setActions.setExpression(expression
                                + newExpression);
                        transition.setActions.setPersistent(true);
                        noSetActionSet = false;
                    } catch (IllegalActionException e) {
                        throw e;
                    }
                }
            }
            if (noSetActionSet) {
                // throw new IllegalActionException(_model, "Cannot add in mode expression because no transitions into mode " + state.getName());
            }

        }
    }

    /** Map from input port name to input value.
     *  The fire() method populates this map.
     *  This is protected so that if a subclass overrides fire(), it
     *  can determine the values of the inputs.
     */
    protected Map<String, Token> _tokenMap;

    private int _getContentIndex(int rowIndex, int columnIndex) {
        return (columnIndex - 1) + (getColumnCount() - 1) * rowIndex;
    }

    /** Parse an expression.
     *
     * (condition1 ? value1 : (condition2 ? value2 : (... : originalValue)))
     *
     * @param expression The expression
     * @param rowIndex The column index
     * @param inmode True if "@T(Inmode)" should be the condition.
     */
    private void _parseExpression(String expression, int rowIndex,
            boolean inmode) {

        if (expression != "") {
            String condition = "";
            String value = "";
            for (int i = 1; i < getColumnCount(); i++) {
                // (condition ? value : (...
                expression = expression.substring(expression.indexOf("(") + 1)
                        .trim();
                // condition ? value : (...

                int endOfCondition = expression.indexOf("?");
                if (expression.contains("(")
                        && (expression.indexOf("(") < expression.indexOf("?"))) {
                    endOfCondition = SCRTableHelper
                            .indexOfMatchingCloseBracket(expression,
                                    expression.indexOf("("));
                }
                condition = expression.substring(0, endOfCondition).trim();

                expression = expression.substring(endOfCondition).trim();
                // ? value : (...

                expression = expression.substring(expression.indexOf("?") + 1)
                        .trim();
                // value : (...

                int endOfValue = expression.indexOf(":");
                if (expression.contains("(")
                        && (expression.indexOf("(") < expression.indexOf(":"))) {
                    endOfValue = SCRTableHelper.indexOfMatchingCloseBracket(
                            expression, expression.indexOf("("));
                }
                value = expression.substring(0, endOfValue).trim();

                expression = expression.substring(expression.indexOf(":") + 1)
                        .trim();
                // (...

                // expression = expression.substring(expression.indexOf("(") + 1).trim();
                // ...

                int valueIndex = _getContentIndex(getRowCount() - 1, i);
                int contentIndex = _getContentIndex(rowIndex, i);

                if (inmode && condition.equals("true")) {
                    condition = "@T(Inmode)";
                }

                // do not put "| false"
                String content = (String) _tableContent.get(contentIndex);
                if (!content.equals("")) {
                    if (content.equals("false")) {
                        content = condition;
                    } else if (condition.equals("false")) {
                        // do nothing;
                    } else {
                        content = content + " || " + condition;
                    }
                } else {
                    content = condition;
                }

                _tableContent.add(valueIndex, value);
                _tableContent.remove(valueIndex + 1);

                _tableContent.add(contentIndex, content);
                _tableContent.remove(contentIndex + 1);
            }
        }
    }

    private void _initializeTableContent() {
        if (_tableContent == null) {
            _tableContent = new ArrayList();
            for (int i = 0; i < (getColumnCount() - 1) * (getRowCount() + 1); i++) {
                _tableContent.add("false");
            }
            for (int rowIndex = 0; rowIndex < getRowCount() - 1; rowIndex++) {

                State state = (State) _model.getEntity((String) getValueAt(
                        rowIndex, 0));
                boolean parsedInmode = false;
                if (state.incomingPort.linkedRelationList().size() > 0) {
                    for (Object object : state.incomingPort
                            .linkedRelationList()) {
                        Transition transition = (Transition) object;
                        String expression = null;
                        try {
                            if (transition.setActions.getDestinations()
                                    .contains(_parameter)) {
                                expression = transition.setActions
                                        .getExpression(_parameter.getName());
                            } else {
                                expression = "";
                            }
                        } catch (IllegalActionException e) {
                            expression = "";
                        }
                        if (!expression.equals("")) {
                            if (state.outgoingPort.linkedRelationList()
                                    .contains(transition)) {
                                // self transition
                                _parseExpression(expression, rowIndex, false);
                            } else if (!parsedInmode) {
                                // incoming transition -- inmode
                                _parseExpression(expression, rowIndex, true);
                                parsedInmode = true;
                            }
                        }
                    }
                }

                //                                try {
                //                                        if (state.getRefinement() != null) {
                //                                                CompositeEntity composite = (CompositeEntity) state
                //                                                                .getRefinement()[0];
                //                                                Expression expressionActor = (Expression) composite
                //                                                                .getEntity("set_" + _parameter.getName() + "_expression");
                //                                                String expression = null;
                //                                                if (expressionActor != null) {
                //                                                        expression = expressionActor.expression
                //                                                                        .getExpression();
                //                                                } else {
                //                                                        expression = "";
                //                                                }
                //                                                _parseExpression(expression, rowIndex, false);
                //
                //                                                //if (_port.c)
                //                                        }
                //                                } catch (IllegalActionException e) {
                //                                        // TODO Auto-generated catch block
                //                                        e.printStackTrace();
                //                                }
            }
        }
    }

    private String[] _handleInmodeExpression(String expression, String value,
            State state) {
        StringBuffer inmodeExpression = new StringBuffer();
        if (expression.contains("@T(Inmode)")) {
            // everything that is connected to inmode with an & goes onto
            // the transition
            String before = expression.substring(0,
                    expression.indexOf("@T(Inmode)"));
            String after = expression.substring(expression
                    .indexOf("@T(Inmode)") + 10);
            before = before.trim();
            after = after.trim();
            while (before.endsWith("&")) {
                before = before.substring(0, before.length() - 1).trim();
                if (before.endsWith(")")) {
                    int index = SCRTableHelper.indexOfMatchingOpenBracket(
                            before, before.length());
                    if (!inmodeExpression.toString().equals("")) {
                        inmodeExpression = inmodeExpression.append(" & ");
                    }
                    inmodeExpression = inmodeExpression.append(" "
                            + before.substring(index));
                    before = before.substring(0, index - 1).trim();
                }
            }

            while (after.startsWith("||")) {
                after = after.substring(2).trim();
                if (after.startsWith("(")) {
                    int index = SCRTableHelper.indexOfMatchingCloseBracket(
                            after, 0);
                    if (!inmodeExpression.toString().equals("")) {
                        inmodeExpression = inmodeExpression.append(" & ");
                    }
                    inmodeExpression = inmodeExpression.append(" "
                            + after.substring(0, index));
                    if (index < after.length()) {
                        after = after.substring(index + 1).trim();
                    }
                }
            }
            if (!inmodeExpression.toString().equals("")) {
                // if inmodeExpression then value
                inmodeExpression = inmodeExpression.append(" ? " + value);
            }

            expression = before + after;

        }
        return new String[] { expression, inmodeExpression.toString() };
    }

    private int _columnCount;
    private Parameter _parameter;
    private FSMActor _model;
    private ArrayList _tableContent;
    private int _rowCount = -1;

}
