/* A top-level dialog window for editing Unit constraints.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@Pt.ProposedRating Yellow (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import diva.canvas.Figure;
import diva.canvas.interactor.SelectionEvent;
import diva.canvas.interactor.SelectionListener;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.graph.modular.Graph;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.unit.Solver;
import ptolemy.data.unit.UnitConstraints;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;

//////////////////////////////////////////////////////////////////////////
//// UnitSolverDialog
/**
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitSolverDialog
    extends PtolemyDialog
    implements ActionListener, ListSelectionListener, SelectionListener {

    /**
     * @param dialogTableau The DialogTableau.
     * @param owner The object that, per the user, appears to be generating the
     * dialog.
     * @param target The object whose units are being solved.
     * @param configuration The configuration to use to open the help screen.
     */
    public UnitSolverDialog(
        DialogTableau dialogTableau,
        Frame owner,
        Entity target,
        Configuration configuration) {
        super(
            "Solve units for " + target.getName(),
            dialogTableau,
            owner,
            target,
            configuration);
        _tableau = ((TableauFrame) owner).getTableau();

        _model =
            ((TypedCompositeActor) (((PtolemyEffigy) (_tableau.getContainer()))
                .getModel()));
        BasicGraphFrame parent = (BasicGraphFrame) (_tableau.getFrame());
        JGraph jGraph = parent.getJGraph();
        GraphPane graphPane = jGraph.getGraphPane();
        _controller = (GraphController) graphPane.getGraphController();
        _selectionModel = _controller.getSelectionModel();
        _graphModel = (AbstractBasicGraphModel) _controller.getGraphModel();

        _selectionModel.addSelectionListener(this);

        if (_model == getTarget()) {
            _entities = _getSelectedNodes();
            _relations = _getSelectedRelations();
            if (_entities.isEmpty() && _relations.isEmpty()) {
                _entities =
                    new Vector(_model.entityList(ComponentEntity.class));
                _relations = new Vector(_model.relationList());
            }
        } else {
            _entities = new Vector();
            _entities.add(getTarget());
            _relations = new Vector();
        }

        JPanel fullSolverPanel = new JPanel();
        fullSolverPanel.setLayout(
            new BoxLayout(fullSolverPanel, BoxLayout.Y_AXIS));
        fullSolverPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Full Solver"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        _runFullSolverButton.addActionListener(this);
        fullSolverPanel.add(_runFullSolverButton);
        _fullSolutionResult.setOpaque(true);
        _fullSolutionResult.setBackground(Color.white);
        fullSolverPanel.add(_fullSolutionResult);

        JPanel membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        membersPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Members"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        _setToSelected.setEnabled(false);
        membersPanel.add(_setToSelected);
        _setToSelected.addActionListener(this);
        membersPanel.add(_showMembers);
        _showMembers.addActionListener(this);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(fullSolverPanel);
        topPanel.add(membersPanel);

        JPanel minimalSpanPanel = new JPanel(new BorderLayout());
        minimalSpanPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Minimal Spanning Solver"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        minimalSpanPanel.add(_runMinimalSpanSolverButton, BorderLayout.NORTH);
        _runMinimalSpanSolverButton.addActionListener(this);
        _solutionsListModel = new SolutionListModel();
        _solutionsList = new JList(_solutionsListModel);
        _solutionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _solutionsList.addListSelectionListener(this);
        JScrollPane scrollPane = new JScrollPane(_solutionsList);
        minimalSpanPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.add(topPanel);
        mainPane.add(minimalSpanPanel);
        setContents(mainPane);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // This method gets invoked as a result of a GUI action. Presently, it
    // handles button presses. It has to be a public, not protected, or private,
    // since it is inherited from ActionListener where it is public. Since it
    // isn't meant to be invoked by the developer there is no javadoc.
    public void actionPerformed(ActionEvent aEvent) {
        String command = aEvent.getActionCommand();
        if (aEvent.getSource() == _runMinimalSpanSolverButton) {
            _uConstraints = new UnitConstraints(_model, _entities, _relations);
            _solutions = _uConstraints.minimalSpanSolution();
            _solutionsListModel.setSolutions(_solutions);
            _solutionsList.setModel(_solutionsListModel);
        } else if (aEvent.getSource() == _runFullSolverButton) {
            _solutionsList.clearSelection();
            _uConstraints = new UnitConstraints(_model, _entities, _relations);
            Solver solution = _uConstraints.completeSolve();
            _fullSolutionResult.setText(solution.getStateDesc());
        } else if (aEvent.getSource() == _setToSelected) {
            _setSelectedMembers();
        } else if (aEvent.getSource() == _showMembers) {
            _showMembers();
        } else {
            super.actionPerformed(aEvent);
        }
    }

    /* (non-Javadoc)
     * @see diva.canvas.interactor
     *                 .SelectionListener#selectionChanged(SelectionEvent)
     */
    public void selectionChanged(SelectionEvent e) {
        _setToSelected.setEnabled(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.event
     *               .ListSelectionListener#valueChanged(ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        int index = _solutionsList.getSelectedIndex();
        if (index >= 0) {
            _showMembers();
            Solver solution = (Solver) (_solutions.elementAt(index));
            solution.annotateGraph();
        }
    }

    public class SolutionListModel extends AbstractListModel {
        Vector _solutions = new Vector();
        public Object getElementAt(int index) {
            return ((Solver) (_solutions.elementAt(index)))
                .getShortDescription();
        }
        public int getSize() {
            return _solutions.size();
        }
        public void setSolutions(Vector s) {
            _solutions = s;
            fireContentsChanged(this, 0, _solutions.size());
        }
        public void clear() {
            _solutions = new Vector();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void _cancel() {
        _selectionModel.removeSelectionListener(this);
        super._cancel();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.gui.PtolemyDialog#_createExtendedButtons(JPanel)
     */
    protected void _createExtendedButtons(JPanel _buttons) {
    }

    protected URL _getHelpURL() {
        URL helpURL =
            getClass().getClassLoader().getResource(
                "ptolemy/actor/gui/doc/unitConstraintsSolver.htm");
        return helpURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a Vector of selected odes in a Tableau. This method really
     *  belongs elsewhere and will be moved there at some point.
     * @param tableau
     * @return Vector of selected Nodes.
     */
    private Vector _getSelectedNodes() {
        Vector nodes = new Vector();
        if (_tableau.getFrame() instanceof BasicGraphFrame) {
            Object selection[] = _selectionModel.getSelectionAsArray();
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    Object userObject = ((Figure) selection[i]).getUserObject();
                    NamedObj actual =
                        (NamedObj) _graphModel.getSemanticObject(userObject);
                    if (actual instanceof ComponentEntity) {
                        nodes.add(actual);
                    }
                }
            }
        }
        return nodes;
    }

    /** Create a Vector of selected Relations in a Tableau. This method really
     *  belongs elsewhere and will be moved there at some point.
     * @param tableau
     * @return Vector of selected Relations.
     */
    private Vector _getSelectedRelations() {
        Vector relations = new Vector();
        if (_tableau.getFrame() instanceof BasicGraphFrame) {

            Object selection[] = _selectionModel.getSelectionAsArray();
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    Object userObject = ((Figure) selection[i]).getUserObject();
                    NamedObj actual =
                        (NamedObj) _graphModel.getSemanticObject(userObject);
                    if ((actual instanceof Relation)
                        && (!relations.contains(actual))) {
                        relations.add(actual);
                    }
                }
            }
        }
        return relations;
    }

    /**
     *
     */
    private void _setSelectedMembers() {
        Vector entities = _getSelectedNodes();
        Vector relations = _getSelectedRelations();
        _entities = new Vector();
        _relations = new Vector();
        for (int i = 0; i < entities.size(); i++) {
            _entities.add(entities.elementAt(i));
        }
        for (int i = 0; i < relations.size(); i++) {
            _relations.add(relations.elementAt(i));
        }
        _setToSelected.setEnabled(false);
    }

    /**
    *
    */
    private void _showMembers() {
        _selectionModel.clearSelection();
        Iterator nodes = _graphModel.nodes(_model);
        while (nodes.hasNext()) {
            Location node = (Location) nodes.next();
            NamedObj entity = (NamedObj) _graphModel.getSemanticObject(node);
            if (_entities.contains(entity)) {
                Figure figure = _controller.getFigure(node);
                _selectionModel.addSelection(figure);
                Iterator edges =
                    GraphUtilities.partiallyContainedEdges(node, _graphModel);
                while (edges.hasNext()) {
                    Object edge = edges.next();
                    Object relation = _graphModel.getSemanticObject(edge);
                    if (_relations.contains(relation)) {
                        Figure relationFigure = _controller.getFigure(edge);
                        _selectionModel.addSelection(relationFigure);
                    }
                }
            }
        }
    }

    private void _xshowMembers() {
        String color = null;
        Vector entities = new Vector(_model.entityList(ComponentEntity.class));
        Vector relations = new Vector(_model.relationList());
        StringBuffer moml = new StringBuffer();
        for (int i = 0; i < entities.size(); i++) {
            ComponentEntity entity = (ComponentEntity) (entities.elementAt(i));
            Iterator ports = entity.portList().iterator();
            if (_entities.contains(entity)) {
                moml.append(
                    "<entity name=\""
                        + entity.getName()
                        + "\">"
                        + "<property name=\"_color\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \"blue\"/>");
                while (ports.hasNext()) {
                    Port port = (Port) (ports.next());
                    moml.append(
                        "<port name=\""
                            + port.getName()
                            + "\">"
                            + "<property name=\"_color\" "
                            + "class = \"ptolemy.kernel.util.StringAttribute\" "
                            + "value = \"blue\"/></port>");
                }
                moml.append("</entity>");
            } else {
                StringAttribute colorAttribute;
                moml.append("<entity name=\"" + entity.getName() + "\">");
                colorAttribute =
                    (StringAttribute) entity.getAttribute("_color");
                if (colorAttribute != null) {
                    moml.append("<deleteProperty name=\"_color\"/>");
                }
                while (ports.hasNext()) {
                    Port port = (Port) (ports.next());
                    colorAttribute =
                        (StringAttribute) port.getAttribute("_color");
                    if (colorAttribute != null) {
                        moml.append(
                            "<port name=\""
                                + port.getName()
                                + "\">"
                                + "<deleteProperty name=\"_color\"/></port>");
                    }
                }
                moml.append("</entity>");
            }
        }
        for (int i = 0; i < relations.size(); i++) {
            Relation relation = (Relation) (relations.elementAt(i));
            if (_relations.contains(relation)) {
                color = "blue";
            } else {
                color = "black";
            }
            moml.append(
                "<relation name=\""
                    + relation.getName()
                    + "\" class=\"ptolemy.actor.TypedIORelation\">"
                    + "<property name=\"_color\" "
                    + "class = \"ptolemy.kernel.util.StringAttribute\" "
                    + "value = \""
                    + color
                    + "\"/>"
                    + "</relation>");
        }
        if (moml.length() > 0) {
            String momlUpdate = "<group>" + moml.toString() + "</group>";
            MoMLChangeRequest request =
                new MoMLChangeRequest(this, _model, momlUpdate);
            request.setUndoable(false);
            if (_debug) {
                System.out.println("Show Members " + momlUpdate);
            }
            _model.requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    GraphController _controller = null;
    Vector _entities = null;
    JLabel _fullSolutionResult = new JLabel("Not Run");
    JButton _setToSelected = new JButton("Set To Selected");
    JButton _showMembers = new JButton("Show Members");
    TypedCompositeActor _model = null;
    SelectionModel _selectionModel = null;
    AbstractBasicGraphModel _graphModel = null;
    Vector _relations = null;
    Vector _solutions = new Vector();
    JList _solutionsList = null;
    SolutionListModel _solutionsListModel = null;
    JButton _runMinimalSpanSolverButton = new JButton("Run");
    JButton _runFullSolverButton = new JButton("Run");
    Tableau _tableau = null;
    UnitConstraints _uConstraints = null;

}
