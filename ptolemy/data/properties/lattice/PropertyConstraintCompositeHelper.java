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

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;


////TypedCompositeActor

/**
 Code generator helper for composite actor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintCompositeHelper 
extends PropertyConstraintHelper {

    /** Construct the property constraint helper associated
     *  with the given TypedCompositeActor.
     * @param solver TODO
     * @param component The associated component.
     *  @throws IllegalActionException 
     * @throws IllegalActionException 
     */
    public PropertyConstraintCompositeHelper(
            PropertyConstraintSolver solver, CompositeEntity component) 
    throws IllegalActionException {

        super(solver, component, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * 
     */
    protected void _addDefaultConstraints(
            ConstraintType actorConstraintType) throws IllegalActionException {

        for (PropertyHelper helper : _getSubHelpers()) {

            ((PropertyConstraintHelper)helper)
            ._addDefaultConstraints(actorConstraintType);
        }
    }    

    /**
     * 
     * @return
     * @throws IllegalActionException
     */
    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        List<PropertyHelper> helpers = super._getSubHelpers();

        CompositeEntity component = 
            (CompositeEntity) getComponent();

        for (Object actor : component.entityList()) {
            helpers.add(_solver.getHelper(actor));
        }
        return helpers;
    }

    /** Return all constraints of this component.  The constraints is
     *  a list of inequalities. 
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        CompositeEntity actor = (CompositeEntity) getComponent();
        
        // Set up inter-actor constraints.
        for (Entity entity : (List<Entity>) actor.entityList()) {
            PropertyConstraintHelper helper = 
                (PropertyConstraintHelper) _solver.getHelper(entity);

            boolean constraintSource = helper.isConstraintSource();

            for (TypedIOPort port : (List<TypedIOPort>) 
                    helper._getConstraintedPorts(constraintSource)) {
                
                _constraintObject(helper.interconnectConstraintType, port, 
                        _getConstraintingPorts(constraintSource, port));
            }
        }

        // Set up inner composite connection constraints.
        for (TypedIOPort port : (List<TypedIOPort>) 
                _getConstraintedInsidePorts(isConstraintSource())) {
            
            _constraintObject(interconnectConstraintType, port, 
                    port.insidePortList());
        }

        return super.constraintList();
    }
    
    public void setAtLeastByDefault(Object term1, Object term2) {
        setAtLeast(term1, term2);
        
        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 1);
            _solver.incrementStats("# of composite default constraints", 1);
        }
    }
    
    public void setSameAsByDefault(Object term1, Object term2) {
        setAtLeast(term1, term2);
        
        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 2);
            _solver.incrementStats("# of composite default constraints", 2);
        }
    }

    /**
     * @param constraintSource
     * @return
     */
    protected List _getConstraintedInsidePorts(boolean constraintSource) {
        Actor actor = (Actor) getComponent();
        return constraintSource ? actor.inputPortList() :
            actor.outputPortList();
    }
}
