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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.canvas.Figure;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////////////
//// UnitConstraints
/**
UnitConstraints represents a collection of UnitEquations. The are two general
ways to create an instance of this class. The first requires you to create an
instance without any UnitEquations and then add them with the method
addConstraint. The second is to specify a TypedCompositeActor as well as
specific nodes and relations in the TypedCompositeActor, and have the
UnitConstraints constructor determine which UnitEquations belong to the set
contained in the new instance of UnitConstraints.
<p>

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitConstraints implements UnitPresentation {

    /** Construct an empty set of Unit constraints.
     *
     */
    public UnitConstraints() {
        _constraints = new Vector();
    }

    /** Construct a set of Unit constraints from the specified componentEntities
     * and realtions of a model.
     * <p>
     * For each componentEntity each constraints in
     * the form of Unit equation is retrieved and then used a basis to create a
     * Unit constraint to add to the set. Each port on the componentEntity is
     * inspected to see if it has a Unit specified for it. If so, then that Unit
     * specification is used to create a corresponding Unit constraint that gets
     * added to the set.
     * <p>
     * A component entity itself may have Unit constraint(s) that then get added
     * to the set. For example, the AddSubtract actor would likely have a
     * constraint that requires that the plus, and minus ports be equal.
     * <p>
     * The relations are then considered. Any relation that connects two ports
     * seen on a component entity in the first step is used to create a Unit
     * equation that gets added to the set.
     * @param model The model containing the component entities.
     * @param componentEntities The component entities.
     * @param relations The relations.
     */
    public UnitConstraints(
        TypedCompositeActor model,
        Vector componentEntities,
        Vector relations) {
        this();
        _model = model;
        _bindings = new Bindings(componentEntities);
        for (int i = 0; i < componentEntities.size(); i++) {
            ComponentEntity componentEntity =
                (ComponentEntity) (componentEntities.elementAt(i));
            Vector actorConstraints = getConstraints(componentEntity);
            if (actorConstraints != null) {
                for (int j = 0; j < actorConstraints.size(); j++) {
                    UnitEquation uEquation =
                        ((UnitEquation) (actorConstraints.elementAt(j))).copy();
                    _equationVisitor.expand(uEquation, componentEntity);
                    uEquation.setSource(componentEntity);
                    addConstraint(uEquation);
                }
            }
            Iterator iter = componentEntity.portList().iterator();
            while (iter.hasNext()) {
                IOPort actorPort = (IOPort) iter.next();
                UnitExpr rhsExpr = getUnitExpr(actorPort);
                if (rhsExpr != null) {
                    UnitExpr lhsExpr = new UnitExpr(actorPort);
                    UnitEquation uC = new UnitEquation(lhsExpr, rhsExpr);
                    uC.setSource(actorPort);
                    addConstraint(uC);
                }
            }
        }
        for (int i = 0; i < relations.size(); i++) {
            IORelation relation = (IORelation) (relations.elementAt(i));
            List ports = relation.linkedPortList();
            IOPort inputPort = null;
            Iterator portIter = ports.iterator();
            while (portIter.hasNext()) {
                IOPort port = (IOPort) (portIter.next());
                if (port.isOutput()) {
                    inputPort = port;
                }
            }
            if (inputPort != null
                && _bindings.bindingExists(inputPort.getFullName())) {
                Iterator portsIterator = ports.iterator();
                while (portsIterator.hasNext()) {
                    IOPort outPort = (IOPort) (portsIterator.next());
                    if ((outPort != inputPort)
                        && (_bindings.bindingExists(outPort.getFullName()))) {
                        UnitExpr lhsUExpr = new UnitExpr(outPort);
                        UnitExpr rhsUExpr = new UnitExpr(inputPort);
                        UnitEquation uC = new UnitEquation(lhsUExpr, rhsUExpr);
                        uC.setSource(relation);
                        this.addConstraint(uC);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /** Add a Unit constraint in the form of a Unit equation to the set.
     * @param unitEquation The Unit equation to be added to the set.
     */
    public void addConstraint(UnitEquation unitEquation) {
        _constraints.add(unitEquation);
    }

    /* (non-Javadoc)
     * @see ptolemy.data.unit.UnitPresentation#commonDesc()
     */
    public String commonExpression() {
        if (_constraints == null) {
            return null;
        }
        String retv = null;
        if (!_constraints.isEmpty()) {
            retv =
                ((UnitEquation) (_constraints.elementAt(0))).commonExpression();
        }
        for (int i = 1; i < _constraints.size(); i++) {
            retv += ";"
                + ((UnitEquation) (_constraints.get(i))).commonExpression();
        }
        return retv;
    }

    public Solver completeSolve() {
        Solver solution = null;
        try {
            if (_debug) {
                System.out.println(humanReadableForm(_bindings));
            }
            Solver G =
                new Solver(
                    _model,
                    _bindings.variableLabels(),
                    getConstraints());
            G.setDebug(_debug);
            solution = G.completeSolve();
            //G.annotateGraph();
        } catch (IllegalActionException e) {
            KernelException.stackTraceToString(e);
        }
        return solution;
    }

    /**
     *
     */
    public Bindings getBindings() {
        return _bindings;
    }

    /**
     * @return The constraints.
     */
    public Vector getConstraints() {
        return _constraints;
    }

    /**
     * @param ce
     * @return The constraints on the argument
     */
    // TODO: Move this method to ComponentEntity.
    public Vector getConstraints(ComponentEntity ce) {
        List unitsAttrs =
            ce.attributeList(ptolemy.data.unit.UnitAttribute.class);
        for (int i = 0; i < unitsAttrs.size(); i++) {
            UnitAttribute attr = (UnitAttribute) (unitsAttrs.get(i));
            if (attr.getName().equals("_unitConstraints")) {
                return attr.getUnitEquations();
            }
        }
        return null;
    }

    /** Get the number of Unit constraints currently in the set.
     * @return The number of constraints.
     */
    public int getNumConstraints() {
        return _constraints.size();
    }

    /**
     * @param port
     * @return The Unit expression on the port.
     */
    public UnitExpr getUnitExpr(IOPort port) {
        UnitAttribute ua = (UnitAttribute) (port.getAttribute("_units"));
        if (ua != null) {
            return ua.getUnitExpr();
        }
        return null;
    }

    /**
     * @param modelBindings
     * @return A human readable form of the constraints.
     */
    public String humanReadableForm(Bindings modelBindings) {
        StringBuffer retv = new StringBuffer("Constraints\n");
        for (int i = 0; i < _constraints.size(); i++) {
            UnitEquation uc = (UnitEquation) (_constraints.elementAt(i));
            retv.append(
                "         " + uc.humanReadableForm(modelBindings) + "\n");
        }
        retv.append("\\Constraints");
        return retv.toString();
    }

    /**
     *
     */
    public Vector minimalSpanSolution() {
        Vector solutions = null;
        try {
            if (_debug) {
                System.out.println(humanReadableForm(_bindings));
            }
            Solver G =
                new Solver(
                    _model,
                    _bindings.variableLabels(),
                    getConstraints());
            G.setDebug(_debug);
            solutions = G.partialSolve();
            //G.annotateGraph();
        } catch (IllegalActionException e) {
            KernelException.stackTraceToString(e);
        }
        return solutions;
    }

    public static Vector solve(Tableau tableau, NamedObj target) {
        TypedCompositeActor model =
            ((TypedCompositeActor) (((PtolemyEffigy) (tableau.getContainer()))
                .getModel()));
        UnitConstraints uConstraints = null;
        if (model == target) {
            Vector nodes = _getSelectedNodes(tableau);
            Vector relations = _getSelectedRelations(tableau);
            if (!nodes.isEmpty() || !relations.isEmpty()) {
                uConstraints = new UnitConstraints(model, nodes, relations);
            } else {
                uConstraints =
                    new UnitConstraints(
                        model,
                        new Vector(model.entityList(ComponentEntity.class)),
                        new Vector(model.relationList()));
            }
        } else {
            Vector nodes = new Vector();
            nodes.add(target);
            uConstraints = new UnitConstraints(model, nodes, new Vector());
        }
        return uConstraints.minimalSpanSolution();
    }

    ///////////////////////////////////////////////////////////////////
    ////                 private methods                           ////

    private void _debug(String msg) {
        if (_debug)
            System.out.println(msg);
    }

    /** Create a Vector of selected nodes in a Tableau. This method really
     *  belongs elsewhere and will be moved there at some point.
     * @param tableau
     * @return Vector of selected Nodes.
     */
    private static Vector _getSelectedNodes(Tableau tableau) {
        Vector nodes = new Vector();
        if (tableau.getFrame() instanceof BasicGraphFrame) {
            TypedCompositeActor model =
                (
                    (TypedCompositeActor) (((PtolemyEffigy) (tableau
                        .getContainer()))
                    .getModel()));
            BasicGraphFrame parent = (BasicGraphFrame) (tableau.getFrame());
            JGraph jGraph = parent.getJGraph();
            GraphPane graphPane = jGraph.getGraphPane();
            GraphController controller =
                (GraphController) graphPane.getGraphController();
            SelectionModel selectionModel = controller.getSelectionModel();
            AbstractBasicGraphModel graphModel =
                (AbstractBasicGraphModel) controller.getGraphModel();
            Object selection[] = selectionModel.getSelectionAsArray();
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    Object userObject = ((Figure) selection[i]).getUserObject();
                    NamedObj actual =
                        (NamedObj) graphModel.getSemanticObject(userObject);
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
    private static Vector _getSelectedRelations(Tableau tableau) {
        Vector relations = new Vector();
        if (tableau.getFrame() instanceof BasicGraphFrame) {
            TypedCompositeActor model =
                (
                    (TypedCompositeActor) (((PtolemyEffigy) (tableau
                        .getContainer()))
                    .getModel()));
            BasicGraphFrame parent = (BasicGraphFrame) (tableau.getFrame());
            JGraph jGraph = parent.getJGraph();
            GraphPane graphPane = jGraph.getGraphPane();
            GraphController controller =
                (GraphController) graphPane.getGraphController();
            SelectionModel selectionModel = controller.getSelectionModel();
            AbstractBasicGraphModel graphModel =
                (AbstractBasicGraphModel) controller.getGraphModel();
            Object selection[] = selectionModel.getSelectionAsArray();
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    Object userObject = ((Figure) selection[i]).getUserObject();
                    NamedObj actual =
                        (NamedObj) graphModel.getSemanticObject(userObject);
                    if ((actual instanceof Relation)
                        && (!relations.contains(actual))) {
                        relations.add(actual);
                    }
                }
            }
        }
        return relations;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    private Bindings _bindings = null;
    private Vector _constraints = null;
    private boolean _debug = false;
    private static ExpandPortNames _equationVisitor = new ExpandPortNames();
    private TypedCompositeActor _model = null;

}
