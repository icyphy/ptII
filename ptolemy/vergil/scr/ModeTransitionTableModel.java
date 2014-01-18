/* The mode transition table for configuring an SCR Model.

 Copyright (c) 2000-2013 The Regents of the University of California.
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
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
The mode transition table for configuring an SCR Model.
@author Patricia Derler
@version $Id: ConstraintMonitor.java 67792 2013-10-26 19:36:54Z cxh $
@since Ptolemy II 10.0
@Pt.ProposedRating Red (pd)
@Pt.AcceptedRating Red (pd)
*/
public class ModeTransitionTableModel extends AbstractTableModel {

	public ModeTransitionTableModel(FSMActor model) {
		_model = model;
		_tableContentIsInvalid = true;
		_initializeTableContent();
	}

	public void addRow(Vector rowData) {
		if (_tableContent == null) {
			_tableContent = new ArrayList<String>();
		}
		_tableContent.addAll(rowData);
		_tableContentIsInvalid = false;
		this.fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		_initializeTableContent();
		return _tableContent.size() / getColumnCount();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		_initializeTableContent();
		int index = arg0 * getColumnCount() + arg1;
		if (index < 0) {
			return null;
		} else {
			return _tableContent.get(index);
		}
	}

	public void setValueAt(Object value, int row, int col) {
		_tableContent.set(row * 3 + col, value);
		fireTableCellUpdated(row, col);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public void saveModel() throws IllegalActionException,
			NameDuplicationException {
		//_removeAllButSelfTransitions();
		//_model.removeAllEntities();
		
		for (int i = 0; i < _tableContent.size(); i = i + 3) {
			String sourceState = (String) _tableContent.get(i);
			String transitionGuard = (String) _tableContent.get(i + 1);
			String destinationState = (String) _tableContent.get(i + 2);

			State source = (State) _model.getEntity(sourceState);
			State destination = (State) _model.getEntity(destinationState);
			if (source == null) {
				source = new State(_model, sourceState);
			}
			if (destination == null) {
				destination = new State(_model, destinationState);
			}
			
			Transition transition = null;
			for (Object object : source.outgoingPort.linkedRelationList()) {
				if (destination.incomingPort.linkedRelationList().contains(object)) {
					transition = (Transition) object;
				}
			}
			if (transition == null) {
				transition = new Transition(_model,
						((CompositeEntity) _model).uniqueName("transition"));
				source.outgoingPort.link(transition);
				destination.incomingPort.link(transition);
			}
			
			transition.guardExpression.setExpression(transitionGuard);
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Source Mode";
		case 1:
			return "Events";
		case 2:
			return "Destination Mode";
		default:
			return "";
		}

	}

	private void _initializeTableContent() {
		if (_tableContent == null || _tableContentIsInvalid) {
			_tableContent = new ArrayList();
			for (Object entity : _model.relationList()) {
				Transition transition = ((Transition) entity);
				if (transition.sourceState() != transition
						.destinationState()) {
					_tableContent.add(transition.sourceState()
							.getName());
					_tableContent.add(transition.guardExpression
							.getExpression());
					_tableContent.add(transition.destinationState()
							.getName());
				}
			}
			_tableContentIsInvalid = false;
		}
	}

	private void _removeAllButSelfTransitions() {
		List transitions = new ArrayList();
		
		for (State state: _model.entityList(State.class)) {
			transitions.add(SCRTableHelper.getSelfTransition(state));
		}
		_model.removeAllRelations();
		
	}

	private ArrayList _tableContent;
	private boolean _tableContentIsInvalid;
	private FSMActor _model;

}