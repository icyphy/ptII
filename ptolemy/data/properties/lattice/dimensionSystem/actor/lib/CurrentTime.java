/* An adapter class for ptolemy.actor.lib.CurrentTime.

 Copyright (c) 2009-2010 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.dimensionSystem.actor.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// CurrentTime

/**
 An adapter class for ptolemy.actor.lib.CurrentTime.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class CurrentTime extends AtomicActor {

    /**
     * Construct a CurrentTime adapter for the dimensionSystem lattice.
     * @param solver The given solver.
     * @param actor The given Expression actor
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public CurrentTime(PropertyConstraintSolver solver,
            ptolemy.actor.lib.CurrentTime actor) throws IllegalActionException {
        super(solver, actor, false);
        _actor = actor;
    }

    /**
     * Return the constraints of this component. The constraints is a list of
     * inequalities.
     * This method sets the constraint of the output to at least the value of
     * the "TIME" element in the {@link ptolemy.data.properties.lattice#_lattice}
     * @return The constraints of this component.
     * @exception IllegalActionException If thrown while reading the lattice or
     * if thrown by the superclass. 
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_actor.output, _lattice.getElement("TIME"));
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
     * are excluded.
     * @see ptolemy.data.properties.Propertyable
     * @return The list of property-able Attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        // FIXME: Findbugs: No relationship between generic parameter and method argument
        // _actor.trigger is a TypedIOPort in actor.lib.Source, not an Attribute.
        // Const._getPropertyableAttributes() has something similar.
        //result.remove(_actor.trigger);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The CurrentTime actor associated with this solver. */
    private ptolemy.actor.lib.CurrentTime _actor;
}
