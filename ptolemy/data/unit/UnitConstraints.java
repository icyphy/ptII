/* A collection that contains Unit constraints.

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
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////////////
//// UnitConstraints
/**
UnitConstraints represents a group, with duplicates allowed, of
UnitContstraints. The are two general ways to create an instance of this class.
The first requires you to create an instance without any UnitConstraints and
then add them with the method addConstraint. The second is to specify a
TypedCompositeActor as well as specific nodes and relations in the
TypedCompositeActor, and have the UnitConstraintCollection constructor
determine which UnitConstraintss belong to the collection.
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitConstraints implements UnitPresentation {

    /** Construct an empty collection of Unit constraints.
     *
     */
    public UnitConstraints() {
        _constraints = new Vector();
    }

    /** Construct a collection of Unit constraints from the specified
     * componentEntities and realtions of a model.
     * <p>
     * For each componentEntity each constraints in
     * the form of Unit equation is retrieved and then used a basis to create a
     * Unit constraint to add to the collection. Each port on the
     * componentEntity is inspected to see if it has a Unit specified for it.
     * If so, then that Unit specification is used to create a corresponding
     * Unit constraint that gets added to the collection.
     * <p>
     * A component entity itself may have Unit constraint(s) that then get added
     * to the collection. For example, the AddSubtract actor would likely have a
     * constraint that requires that the plus, and minus ports be equal.
     * <p>
     * The relations are then considered. Any relation that connects two ports
     * seen on a component entity in the first step is used to create a Unit
     * equation that gets added to the collection.
     * @param model The model containing the component entities.
     * @param entities The component entities.
     * @param relations The relations.
     */
    public UnitConstraints(
        TypedCompositeActor model,
        Vector entities,
        Vector relations) throws IllegalActionException {
        this();
        _model = model;
        _bindings = new Bindings(entities);
        for (int i = 0; i < entities.size(); i++) {
            ComponentEntity componentEntity =
                (ComponentEntity) (entities.elementAt(i));
            Vector actorConstraints = new Vector();
            List unitsAttrs =
                componentEntity.attributeList(
                    ptolemy.data.unit.UnitAttribute.class);
            for (int j = 0; j < unitsAttrs.size(); j++) {
                UnitAttribute attr = (UnitAttribute) (unitsAttrs.get(j));
                if (attr.getName().equals("_unitConstraints")) {
                    actorConstraints.addAll(
                        attr.getUnitConstraints().getConstraints());
                }
            }
            for (int j = 0; j < actorConstraints.size(); j++) {
                UnitEquation uEquation =
                    ((UnitEquation) (actorConstraints.elementAt(j))).copy();
                _equationVisitor.expand(uEquation, componentEntity);
                uEquation.setSource(componentEntity);
                addConstraint(uEquation);
            }
            Iterator iter = componentEntity.portList().iterator();
            while (iter.hasNext()) {
                IOPort actorPort = (IOPort) iter.next();
                UnitExpr rhsExpr = null;
                UnitAttribute ua =
                    (UnitAttribute) (actorPort.getAttribute("_units"));
                if (ua != null) {
                    rhsExpr = ua.getUnitExpr();
                }
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
                && _bindings.bindingExists(
                    inputPort.getName(
                        inputPort.getContainer().getContainer()))) {
                Iterator portsIterator = ports.iterator();
                while (portsIterator.hasNext()) {
                    IOPort outPort = (IOPort) (portsIterator.next());
                    if ((outPort != inputPort)
                        && (_bindings
                            .bindingExists(
                                outPort.getName(
                                    outPort.getContainer().getContainer())))) {
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

    /** Add a UnitConstraint to the collection.
     * @param constraint The UnitConstraint to be added to the collection.
     */
    public void addConstraint(UnitConstraint constraint) {
        _constraints.add(constraint);
    }

    /** Generate a complete solution.
     * @return The solution.
     */
    public Solution completeSolution() throws IllegalActionException {
        Solution solution = null;
        if (_debug) {
            System.out.println(
                "Constraints\n" + descriptiveForm() + "\\Constraints");
        }
        Solution G =
            new Solution(_model, _bindings.variableLabels(), getConstraints());
        G.setDebug(_debug);
        solution = G.completeSolution();
        return solution;
    }

    /**
     * @see ptolemy.data.unit.UnitPresentation#descriptiveForm()
     */
    public String descriptiveForm() {
        if (_constraints == null) {
            return null;
        }
        String retv = null;
        if (!_constraints.isEmpty()) {
            retv =
                ((UnitEquation) (_constraints.elementAt(0))).descriptiveForm();
        }
        for (int i = 1; i < _constraints.size(); i++) {
            retv += ";"
                + ((UnitEquation) (_constraints.get(i))).descriptiveForm();
        }
        return retv;
    }

    /** Get the constraints in the collection.
    * @return The constraints.
    */
    public Vector getConstraints() {
        return _constraints;
    }

    /** Generate the minimal span solutions of the collection.
     * @return The minimal span solutions.
     */
    public Vector minimalSpanSolutions() throws IllegalActionException {
        Vector solutions = null;
        if (_debug) {
            System.out.println(
                "Constraints\n" + descriptiveForm() + "\\Constraints");
        }
        Solution G =
            new Solution(_model, _bindings.variableLabels(), getConstraints());
        G.setDebug(_debug);
        solutions = G.minimalSpanSolutions();
        //G.annotateGraph();
        return solutions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                 private methods                           ////

    private void _debug(String msg) {
        if (_debug)
            System.out.println(msg);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    private Bindings _bindings = null;
    private Vector _constraints = null;
    private boolean _debug = false;
    private static ExpandPortNames _equationVisitor = new ExpandPortNames();
    private TypedCompositeActor _model = null;

}
