/* An adapter class for ptolemy.actor.lib.Const.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.DimensionSystem.actor.lib;

import java.util.List;

import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Const

/**
 An adapter class for ptolemy.actor.lib.Const.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Const extends Source {

    /**
     * Construct a Const adapter for the dimensionSystem lattice.
     * @param solver The given solver.
     * @param actor The given Const actor
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public Const(LatticeOntologySolver solver, ptolemy.actor.lib.Const actor)
            throws IllegalActionException {
        super(solver, actor);
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the constraints of this component. The constraints is a list of
     * inequalities. 
     * This method sets the constraint of the output to at least that of the
     * value Parameter of the actor.

     * @return The constraints of this component.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_actor.output, _actor.value);
        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return the list of property-able Attributes.
     * A property-able Attribute is a StringAttribute with the name
     * "guardTransition", a StringAttribute in an Expression actor,
     * a StringAttribute with the name "expression" or a Variable
     * with full visibility.  However, Variables with certain names
     * are excluded.  This method adds the value Parameter of the
     * Const actor to the list that is returned.
     * @see ptolemy.data.properties.Propertyable
     * @return The list of property-able Attributes.
     */
    
    /* 12/17/09 Charles Shelton
     * I don't think this method is necessary.
     * value is a Parameter in the Const actor
     * and all actor parameters are already added to
     * the propertyable attribute list.
    
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        result.add(_actor.value);
        // FIXME: Findbugs: No relationship between generic parameter and method argument
        // _actor.trigger is a TypedIOPort in actor.lib.Source, not an Attribute.
        // CurrentTime._getPropertyableAttributes() has something similar.
        //result.remove(_actor.trigger);
        return result;
    }
    
    */

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Const actor associated with this solver. */
    private ptolemy.actor.lib.Const _actor;
}
