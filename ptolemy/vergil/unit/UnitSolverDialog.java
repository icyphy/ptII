/* A top-level dialog window for editing Unit constraints.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.vergil.unit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashSet;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.PtolemyDialog;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.unit.Solution;
import ptolemy.moml.unit.UnitConstraints;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.canvas.Figure;
import diva.canvas.interactor.BasicSelectionRenderer;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionEvent;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionListener;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.interactor.SelectionRenderer;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////
//// UnitSolverDialog

/**
 Dialog for the Unit Solver.

 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
@SuppressWarnings("serial")
public class UnitSolverDialog extends PtolemyDialog implements
ListSelectionListener, SelectionListener {
    /**
     * Construct a Unit Solver Dialog.
     * @param dialogTableau The DialogTableau.
     * @param owner The object that, per the user, appears to be generating the
     * dialog.
     * @param target The object whose units are being solved.
     * @param configuration The configuration to use to open the help screen.
     */
    public UnitSolverDialog(DialogTableau dialogTableau, Frame owner,
            Entity target, Configuration configuration) {
        super("Solve units for " + target.getName(), dialogTableau, owner,
                target, configuration);

        SelectionRenderer tempSelectionRenderer = null;
        _tableau = ((TableauFrame) owner).getTableau();

        _model = (TypedCompositeActor) target;

        // ((TypedCompositeActor) (((PtolemyEffigy) (_tableau.getContainer()))
        //      .getModel()));
        BasicGraphFrame parent = (BasicGraphFrame) _tableau.getFrame();
        JGraph jGraph = parent.getJGraph();
        GraphPane graphPane = jGraph.getGraphPane();
        _controller = graphPane.getGraphController();
        _selectionModel = _controller.getSelectionModel();

        Interactor interactor = _controller.getEdgeController(new Object())
                .getEdgeInteractor();
        _graphModel = (AbstractBasicGraphModel) _controller.getGraphModel();
        _selectionInteractor = (SelectionInteractor) interactor;
        _defaultSelectionRenderer = _selectionInteractor.getSelectionRenderer();
        tempSelectionRenderer = new BasicSelectionRenderer(
                new BasicEdgeHighlighter());

        if (_model == getTarget()) {
            _entities = _getSelectedNodes();
            _relations = _getSelectedRelations();

            if (_entities.isEmpty() && _relations.isEmpty()) {
                _entities = new HashSet<ComponentEntity>(
                        _model.entityList(ComponentEntity.class));
                _relations = new HashSet<Relation>(_model.relationList());
            }
        } else {
            _entities = new HashSet<ComponentEntity>();
            Entity targetEntity = getTarget();
            if (targetEntity instanceof ComponentEntity) {
                _entities.add((ComponentEntity) targetEntity);
            }
            _relations = new HashSet<Relation>();
        }

        _selectionModel.clearSelection();
        _selectionInteractor.setSelectionRenderer(tempSelectionRenderer);
        _showComponents();
        _selectionModel.addSelectionListener(this);

        JPanel fullSolverPanel = new JPanel();
        fullSolverPanel.setLayout(new BoxLayout(fullSolverPanel,
                BoxLayout.Y_AXIS));
        fullSolverPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Full Solution"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        _runFullSolverButton.addActionListener(this);
        fullSolverPanel.add(_runFullSolverButton);
        _fullSolutionResult.setOpaque(true);
        _fullSolutionResult.setBackground(Color.white);
        fullSolverPanel.add(_fullSolutionResult);

        JPanel componentsPanel = new JPanel();
        componentsPanel.setLayout(new BoxLayout(componentsPanel,
                BoxLayout.Y_AXIS));
        componentsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Components"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        _setToSelectedButton.setEnabled(false);
        componentsPanel.add(_setToSelectedButton);
        _setToSelectedButton.addActionListener(this);
        componentsPanel.add(_showComponentsButton);
        _showComponentsButton.addActionListener(this);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(fullSolverPanel);
        topPanel.add(componentsPanel);

        JPanel minimalSpanPanel = new JPanel(new BorderLayout());
        minimalSpanPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Minimal Spanning Solutions"),
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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent aEvent) {
        //String command = aEvent.getActionCommand();
        if (aEvent.getSource() == _runMinimalSpanSolverButton) {
            try {
                // FIXME: UnitContstraint ctor should take args other than Vectors.
                _uConstraints = new UnitConstraints(_model, new Vector(
                        _entities), new Vector(_relations));
                _solutions = _uConstraints.minimalSpanSolutions();
            } catch (IllegalActionException e) {
                MessageHandler.error("Minimal Span Solver failed: ", e);
                return;
            }

            _solutionsListModel.setSolutions(_solutions);
            _solutionsList.setModel(_solutionsListModel);
        } else if (aEvent.getSource() == _runFullSolverButton) {
            _solutionsList.clearSelection();

            try {
                _uConstraints = new UnitConstraints(_model, new Vector(
                        _entities), new Vector(_relations));

                Solution solution = _uConstraints.completeSolution();
                _fullSolutionResult.setText(solution.getShortStateDesc());

                //solution.trace();
            } catch (IllegalActionException e) {
                MessageHandler.error("Full Solver failed: ", e);
                return;
            }
        } else if (aEvent.getSource() == _setToSelectedButton) {
            _setSelectedComponents();
        } else if (aEvent.getSource() == _showComponentsButton) {
            _showComponents();
        } else {
            super.actionPerformed(aEvent);
        }
    }

    /** Remove all the annotations from the graph.
     * Actors, their ports, and relations are inspected to see if they either a
     * _color and/or an _explanation attribute. If so, then the attribute is
     * removed via a MoMl changeRequest.
     */
    public void deAnnotateGraph() {
        StringBuffer moml = new StringBuffer();
        Iterator entities = _model.entityList(ComponentEntity.class).iterator();

        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity) entities.next();
            String entityDeletes = _deletesIfNecessary(entity);
            moml.append("<entity name=\"" + entity.getName() + "\">");

            if (entityDeletes != null) {
                moml.append(entityDeletes);
            }

            Iterator ports = entity.portList().iterator();

            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                String portDeletes = _deletesIfNecessary(port);

                if (portDeletes != null) {
                    moml.append("<port name=\"" + port.getName() + "\">"
                            + portDeletes + "</port>");
                }
            }

            moml.append("</entity>");
        }

        Iterator relations = _model.relationList().iterator();

        while (relations.hasNext()) {
            Relation relation = (Relation) relations.next();
            String relationDeletes = _deletesIfNecessary(relation);

            if (relationDeletes != null) {
                moml.append("<relation name=\"" + relation.getName() + "\">"
                        + relationDeletes + "\"/></relation>");
            }
        }

        if (moml.length() > 0) {
            String momlUpdate = "<group>" + moml.toString() + "</group>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, _model,
                    momlUpdate);
            request.setUndoable(true);
            request.setPersistent(false);

            _model.requestChange(request);
        }
    }

    /* (non-Javadoc)
     * @see diva.canvas.interactor
     *                 .SelectionListener#selectionChanged(SelectionEvent)
     */
    @Override
    public void selectionChanged(SelectionEvent e) {
        _setToSelectedButton.setEnabled(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.event
     *               .ListSelectionListener#valueChanged(ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        int index = _solutionsList.getSelectedIndex();

        if (index >= 0) {
            _showComponents();

            Solution solution = (Solution) _solutions.elementAt(index);
            solution.annotateGraph();

            //solution.trace();
        }
    }

    /** List of solutions.
     */
    public static class SolutionListModel extends AbstractListModel {
        Vector _solutions = new Vector();

        /** Return an element.
         *  @param index The index of the element to be returned.
         *  @return The element at the specified index.
         */
        @Override
        public Object getElementAt(int index) {
            return ((Solution) _solutions.elementAt(index)).getStateDesc();
        }

        /** Return the number of solutions.
         *  @return The number of solutions.
         */
        @Override
        public int getSize() {
            return _solutions.size();
        }

        /** Set the solutions to the specified argument.
         *  @param solutions A vector of solutions.
         */
        public void setSolutions(Vector solutions) {
            _solutions = solutions;
            fireContentsChanged(this, 0, _solutions.size());
        }

        /** Clear the current set of solutions.
         */
        public void clear() {
            _solutions = new Vector();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    @Override
    protected void _cancel() {
        _selectionModel.removeSelectionListener(this);
        _selectionModel.clearSelection();
        _selectionInteractor.setSelectionRenderer(_defaultSelectionRenderer);
        _showComponents();
        super._cancel();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.gui.PtolemyDialog#_createExtendedButtons(JPanel)
     */
    @Override
    protected void _createExtendedButtons(JPanel _buttons) {
    }

    @Override
    protected URL _getHelpURL() {
        URL helpURL = getClass().getClassLoader().getResource(
                "ptolemy/actor/gui/doc/unitConstraintsSolver.htm");
        return helpURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private String _deletesIfNecessary(NamedObj obj) {
        String retv = null;
        Attribute color = obj.getAttribute("_color");
        Attribute explanation = obj.getAttribute("_explanation");

        if (color != null && explanation != null) {
            retv = "<deleteProperty name=\"_color\"/>"
                    + "<deleteProperty name=\"_explanation\"/>";
        }

        return retv;
    }

    /** Create a Vector of selected nodes in a Tableau. This method really
     *  belongs elsewhere and will be moved there at some point.
     *  @return Vector of selected Nodes.
     */
    private Set<ComponentEntity> _getSelectedNodes() {
        Set<ComponentEntity> nodes = new HashSet<ComponentEntity>();

        if (_tableau.getFrame() instanceof BasicGraphFrame) {
            Object[] selection = _selectionModel.getSelectionAsArray();

            for (Object element : selection) {
                if (element instanceof Figure) {
                    Object userObject = ((Figure) element).getUserObject();
                    NamedObj actual = (NamedObj) _graphModel
                            .getSemanticObject(userObject);

                    if (actual instanceof ComponentEntity) {
                        nodes.add((ComponentEntity) actual);
                    }
                }
            }
        }

        return nodes;
    }

    /** Create a Vector of selected Relations in a Tableau. This method really
     *  belongs elsewhere and will be moved there at some point.
     *  @return Vector of selected Relations.
     */
    private Set<Relation> _getSelectedRelations() {
        Set<Relation> relations = new HashSet<Relation>();

        if (_tableau.getFrame() instanceof BasicGraphFrame) {
            Object[] selection = _selectionModel.getSelectionAsArray();

            for (Object element : selection) {
                if (element instanceof Figure) {
                    Object userObject = ((Figure) element).getUserObject();
                    NamedObj actual = (NamedObj) _graphModel
                            .getSemanticObject(userObject);

                    if (actual instanceof Relation
                            && !relations.contains(actual)) {
                        relations.add((Relation) actual);
                    }
                }
            }
        }

        return relations;
    }

    /**
     *
     */
    private void _setSelectedComponents() {
        // FIXME: Why don't we just set _entities and _relations here?
        Set<ComponentEntity> entities = _getSelectedNodes();
        Set<Relation> relations = _getSelectedRelations();
        _entities = new HashSet<ComponentEntity>(entities);
        _relations = new HashSet<Relation>(relations);

        //         for (int i = 0; i < entities.size(); i++) {
        //             _entities.add(entities.elementAt(i));
        //         }

        //         for (int i = 0; i < relations.size(); i++) {
        //             _relations.add(relations.elementAt(i));
        //         }

        _setToSelectedButton.setEnabled(false);
    }

    /**
     *
     */
    private void _showComponents() {
        _selectionModel.clearSelection();
        deAnnotateGraph();

        Iterator nodes = _graphModel.nodes(_model);

        while (nodes.hasNext()) {
            Location node = (Location) nodes.next();
            NamedObj entity = (NamedObj) _graphModel.getSemanticObject(node);

            if (_entities.contains(entity)) {
                Figure figure = _controller.getFigure(node);
                _selectionModel.addSelection(figure);

                Iterator edges = GraphUtilities.partiallyContainedEdges(node,
                        _graphModel);

                while (edges.hasNext()) {
                    Object edge = edges.next();
                    Object relation = _graphModel.getSemanticObject(edge);

                    if (relation instanceof Relation
                            && _relations.contains(relation)) {
                        Figure relationFigure = _controller.getFigure(edge);
                        _selectionModel.addSelection(relationFigure);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    GraphController _controller = null;

    SelectionRenderer _defaultSelectionRenderer = null;

    Set<ComponentEntity> _entities = null;

    JLabel _fullSolutionResult = new JLabel("Not Run");

    JButton _setToSelectedButton = new JButton("Set To Selected");

    JButton _showComponentsButton = new JButton("Show Components");

    TypedCompositeActor _model = null;

    SelectionModel _selectionModel = null;

    AbstractBasicGraphModel _graphModel = null;

    Set<Relation> _relations = null;

    SelectionInteractor _selectionInteractor = null;

    Vector _solutions = new Vector();

    JList _solutionsList = null;

    SolutionListModel _solutionsListModel = null;

    JButton _runMinimalSpanSolverButton = new JButton("Run");

    JButton _runFullSolverButton = new JButton("Run");

    Tableau _tableau = null;

    UnitConstraints _uConstraints = null;
}
