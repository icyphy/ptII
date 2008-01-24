/* Code generator helper for typed composite actor.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator helper for composite actor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintCompositeHelper extends PropertyConstraintHelper {

    /** Construct the property constraint helper associated
     *  with the given TypedCompositeActor.
     * @param solver TODO
     * @param component The associated component.
     *  @exception IllegalActionException 
     * @exception IllegalActionException 
     */
    public PropertyConstraintCompositeHelper(PropertySolver solver,
            ptolemy.actor.CompositeActor component)
            throws IllegalActionException {

        super(solver, component, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * 
     */
    public void updateProperty(boolean isTraining)
            throws IllegalActionException, NameDuplicationException {

        super.updateProperty(isTraining);
        ptolemy.actor.CompositeActor component = (ptolemy.actor.CompositeActor) _component;

        Iterator iterator = component.entityList().iterator();

        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            PropertyHelper helper = getSolver().getHelper(actor);

            helper.updateProperty(isTraining);
        }
    }

    /**
     * 
     */
    protected void _changeDefaultConstraints(ConstraintType actorConstraintType)
            throws IllegalActionException {
        ptolemy.actor.CompositeActor component = (ptolemy.actor.CompositeActor) _component;

        Iterator iterator = component.entityList().iterator();

        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            PropertyConstraintHelper helper = getSolver().getHelper(actor);

            helper._changeDefaultConstraints(actorConstraintType);
        }
    }

    /**
     * 
     */
    public void reinitialize() throws IllegalActionException {
        ptolemy.actor.CompositeActor component = (ptolemy.actor.CompositeActor) _component;
        super.reinitialize();

        Iterator iterator = component.entityList().iterator();

        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            PropertyHelper helper = getSolver().getHelper(actor);

            helper.reinitialize();
        }
    }

    /**
     * 
     * @param constraintType
     * @exception IllegalActionException
     */
    protected void _setConnectionConstraintType(ConstraintType constraintType,
            ConstraintType compositeConstraintType,
            ConstraintType expressionASTNodeConstraintType)
            throws IllegalActionException {

        interconnectConstraintType = compositeConstraintType;

        ptolemy.actor.CompositeActor component = (ptolemy.actor.CompositeActor) _component;

        Iterator iterator = component.entityList().iterator();

        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            PropertyConstraintHelper helper = getSolver().getHelper(actor);

            helper._setConnectionConstraintType(constraintType,
                    compositeConstraintType, expressionASTNodeConstraintType);
        }
    }

    /** Return all constraints of this component.  The constraints is
     *  a list of inequalities. 
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List constraintList() throws IllegalActionException {
        _constraints.clear();

        ArrayList constraints = new ArrayList();

        ptolemy.actor.CompositeActor component = (ptolemy.actor.CompositeActor) _component;

        Iterator iterator = component.entityList().iterator();

        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            PropertyConstraintHelper helper = getSolver().getHelper(
                    (NamedObj) actor);

            // Add constraints from helpers of contained actors.
            constraints.addAll(helper.constraintList());

        }

        boolean constraintSource = (interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET)
                || (interconnectConstraintType == ConstraintType.SRC_LESS);

        CompositeActor actor = (CompositeActor) _component;

        List portList1 = actor.portList();

        Iterator ports = portList1.iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            List portList2;

            // Eliminate duplicates.
            if (constraintSource ^ port.isOutput()) {
                portList2 = port.insidePortList();
            } else {
                if (constraintSource) {
                    portList2 = _getSinkPortList(port);
                } else {
                    portList2 = _getSourcePortList(port);
                }
            }
            constraints.addAll(_constraintObject(interconnectConstraintType,
                    port, portList2));
        }

        //constraints.addAll(_constraints);

        return constraints;
    }

}
