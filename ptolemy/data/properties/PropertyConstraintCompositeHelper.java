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
package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.PropertyConstraintSolver.ConstraintType;
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
     *  @param component The associated component.
     * @throws IllegalActionException 
     */
    public PropertyConstraintCompositeHelper(ptolemy.actor.CompositeActor component,
            PropertyLattice lattice) throws IllegalActionException {
        super(component, lattice);
    }

    /**
     * 
     */
    public void updatePortProperty() throws IllegalActionException, NameDuplicationException {

        ptolemy.actor.CompositeActor component = 
            (ptolemy.actor.CompositeActor) _component;
        
        Iterator iterator = component.entityList().iterator();
        
        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();
            
            PropertyConstraintHelper helper = 
                _lattice.getHelper(actor);
            
            helper.updatePortProperty();
        }
    }
    
    /**
     * 
     */
    protected void _changeDefaultConstraints(
            ConstraintType actorConstraintType) throws IllegalActionException {
        ptolemy.actor.CompositeActor component = 
            (ptolemy.actor.CompositeActor) _component;
        
        Iterator iterator = component.deepEntityList().iterator();
        
        while (iterator.hasNext()) {
            ptolemy.actor.AtomicActor actor = 
                (ptolemy.actor.AtomicActor) iterator.next();
            
            PropertyConstraintHelper helper = 
                _lattice.getHelper(actor);
            
            if (helper._useDefaultConstraints) {
                helper._changeDefaultConstraints(actorConstraintType);
            }
        }        
    }    
    
    /**
     * 
     * @param constraintType
     * @throws IllegalActionException
     */
    protected void _setConnectionConstraintType(
            ConstraintType constraintType, 
            ConstraintType compositeConstraintType) throws IllegalActionException {

        connectionConstraintType = constraintType;
        compositeConnectionConstraintType = compositeConstraintType;
        
        ptolemy.actor.CompositeActor component = 
            (ptolemy.actor.CompositeActor) _component;
        
        Iterator iterator = component.entityList().iterator();
        
        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            if (actor instanceof CompositeActor) {
                
                PropertyConstraintCompositeHelper helper = 
                    (PropertyConstraintCompositeHelper) _lattice.getHelper(actor);
                
                helper._setConnectionConstraintType(constraintType, compositeConstraintType);
            }
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
       
        ptolemy.actor.CompositeActor component = 
            (ptolemy.actor.CompositeActor) _component;
        
        Iterator iterator = component.deepEntityList().iterator();
        
        while (iterator.hasNext()) {
            ptolemy.actor.AtomicActor actor = 
                (ptolemy.actor.AtomicActor) iterator.next();
            
            PropertyConstraintHelper helper = 
                _lattice.getHelper(actor);

            // Add constraints from helpers of contained actors.
            constraints.addAll(helper.constraintList());

            boolean constraintSource = 
                (connectionConstraintType == ConstraintType.SRC_EQUALS_MEET) ||  
                (connectionConstraintType == ConstraintType.SRC_LESS);

            List portList1 = (constraintSource) ?
                    actor.outputPortList() : actor.inputPortList();

            Iterator ports = portList1.iterator();
            
            while (ports.hasNext()) {                    
                TypedIOPort port = (TypedIOPort) ports.next();

                List portList2 = (constraintSource) ? 
                        port.sinkPortList() : port.sourcePortList();
                        
                _constraintPort(connectionConstraintType, port, portList2);
            }
        }
        constraints.addAll(_constraints);
        
        return constraints;
    }


    /**
     * 
     */
    public ConstraintType connectionConstraintType;

    /**
     * 
     */
    public ConstraintType compositeConnectionConstraintType;
    
}
