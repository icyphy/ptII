/* The conditions table for configuring an SCR Model.

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

import javax.swing.table.AbstractTableModel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.RefinementActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
   The conditions table for configuring an SCR Model.
   @author Patricia Derler
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (pd)
   @Pt.AcceptedRating Red (pd)
 */
@SuppressWarnings("serial")
public class ConditionsTableModel extends AbstractTableModel {

    /** Construct a new conditions table model for a given output port and
     *  the FSMActor. Initialize the column count to 3.
     * @param port
     * @param model
     */
    public ConditionsTableModel(IOPort port, FSMActor model) {
        _model = model;
        _port = port;
        _columnCount = 3;// FIXME should be configurable

    }

    /** Add a column. */
    public void addColumn() {
        for (int i = 0; i < getRowCount() - 1; i++) {
            _tableContent.add(i * (getColumnCount()) + (getColumnCount() - 1),
                    "");
        }
        _columnCount = _columnCount + 1;
        this.fireTableStructureChanged();
        this.fireTableDataChanged();
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

    @Override
    public int getColumnCount() {
        return _columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        _initializeTableContent();
        if (rowIndex < getRowCount() - 1) {
            if (columnIndex == 0) {
                return ((State) _model.entityList().get(rowIndex)).getName();
            }
        } else {
            if (columnIndex == 0) {
                return "----";
            }
        }
        int contentIndex = SCRTableHelper.getContentIndex(rowIndex,
                columnIndex, getColumnCount());
        if (contentIndex < 0) {
            return null;
        } else {
            return _tableContent.get(contentIndex);
        }
    }

    @Override
    public int getRowCount() {
        if (_rowCount == -1) {
            _rowCount = _model.entityList(State.class).size() + 1;
        }
        return _rowCount;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex > 0);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        int contentIndex = SCRTableHelper.getContentIndex(rowIndex,
                columnIndex, getColumnCount());
        _tableContent.add(contentIndex, aValue);
        _tableContent.remove(contentIndex + 1);
    }

    /** Save the model.
     *  @exception IllegalActionException  If thrown while saving the model.
     *  @exception NameDuplicationException  If thrown while saving the model.
     */
    public void saveModel() throws NameDuplicationException {
        for (int i = 0; i < getRowCount() - 1; i++) {
            StringBuffer expression = new StringBuffer();
            State state = (State) _model.getEntity((String) getValueAt(i, 0));
            String condition = "";
            String value = "";
            for (int j = 0; j < getColumnCount() - 1; j++) {
                SCRTableHelper.getContentIndex(i, j, getColumnCount());
                condition = (String) getValueAt(i, j + 1);
                value = (String) getValueAt(getRowCount() - 1, j + 1);
                expression = expression.append("(" + condition + " ? " + value
                        + " : ");
            }
            // insert the last value as the dummy
            expression = expression.append(" " + value);
            for (int j = 0; j < getColumnCount() - 1; j++) {
                expression = expression.append(")");
            }
            // System.out.println("Save model - expression: " + expression);

            try {
                if (state.getRefinement() == null) {
                    ((RefinementActor) state.getContainer()).addRefinement(
                            state,
                            ((CompositeEntity) _model).uniqueName(state
                                    .getName() + "_refinement"), null,
                            Refinement.class.getName(), null);
                }
                CompositeEntity entity = (CompositeEntity) state
                        .getRefinement()[0];
                if (entity.attributeList(Director.class).size() == 0) {
                    /* ContinuousDirector director =*/new ContinuousDirector(
                            entity, "Continuous Director");
                }
                Object expressionActorObject = entity.getEntity(_port.getName()
                        + "_out");
                TypedIORelation relation = null;
                if (expressionActorObject == null) {
                    expressionActorObject = new Expression(entity,
                            _port.getName() + "_out");
                    relation = new TypedIORelation(entity,
                            entity.uniqueName("relation"));

                }
                Expression expressionActor = (Expression) expressionActorObject;
                expressionActor.expression.setExpression(expression.toString());

                if (relation != null) {
                    expressionActor.output.link(relation);
                    IOPort port = (IOPort) ((CompositeActor) entity)
                            .getPort(_port.getName());
                    port.link(relation);
                }
            } catch (IllegalActionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    }

    private void _initializeTableContent() {
        if (_tableContent == null) {
            _tableContent = new ArrayList();
            for (int i = 0; i < (getColumnCount() - 1) * (getRowCount() + 1); i++) {
                _tableContent.add("");
            }
            for (int rowIndex = 0; rowIndex < getRowCount() - 1; rowIndex++) {

                State state = (State) _model.getEntity((String) getValueAt(
                        rowIndex, 0));
                try {
                    if (state.getRefinement() != null) {
                        CompositeEntity composite = (CompositeEntity) state
                                .getRefinement()[0];
                        //                                                if parsing existing ModalModels use following code as a start. For now, do not attempt that as there
                        //                                                might be too many ways of interpreting existing ModalModels.
                        //                                                for (Object insidePortObject : ((IOPort)((CompositeActor) state.getRefinement()[0]).getPort(_port.getName())).insidePortList()) {
                        //                                                        IOPort insidePort = (IOPort) insidePortObject;
                        //                                                        Actor actor = (Actor) insidePort.getContainer();
                        //                                                        if (actor instanceof Const) {
                        //                                                                String value = ((Const)actor).value.getToken().toString();
                        //                                                        }
                        //                                                }

                        Expression expressionActor = (Expression) composite
                                .getEntity(_port.getName() + "_out");
                        String expression = null;
                        if (expressionActor != null) {
                            expression = expressionActor.expression
                                    .getExpression();
                        } else {
                            expression = "";
                        }
                        _parseExpression(expression, rowIndex);
                    }
                } catch (IllegalActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * (condition1 ? value1 : (condition2 ? value2 : (... : nil)))
     *
     *
     * @param expression
     * @param rowIndex
     */
    private void _parseExpression(String expression, int rowIndex) {
        if (!expression.equals("")) {
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

                expression = expression.substring(expression.indexOf("(") + 1)
                        .trim();
                // ...

                int valueIndex = SCRTableHelper.getContentIndex(
                        getRowCount() - 1, i, getColumnCount());
                int contentIndex = SCRTableHelper.getContentIndex(rowIndex, i,
                        getColumnCount());

                _tableContent.add(valueIndex, value);
                _tableContent.remove(valueIndex + 1);
                _tableContent.add(contentIndex, condition);
                _tableContent.remove(contentIndex + 1);
            }
        }
    }

    private int _columnCount;
    private int _rowCount = -1;
    private IOPort _port;
    private FSMActor _model;
    private ArrayList _tableContent;
}
