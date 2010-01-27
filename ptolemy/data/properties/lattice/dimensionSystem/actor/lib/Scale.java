/* An adapter class for ptolemy.actor.lib.Scale.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
import ptolemy.data.properties.lattice.dimensionSystem.MultiplyMonotonicFunction;
import ptolemy.data.properties.lattice.dimensionSystem.DivideMonotonicFunction;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Scale

/**
 An adapter class for ptolemy.actor.lib.Scale.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Scale extends AtomicActor {

    /**
     * Construct a Scale adapter for the dimensionSystem lattice.
     * @param solver The given solver.
     * @param actor The given Scale actor.
     * @exception IllegalActionException
     */
    public Scale(PropertyConstraintSolver solver, ptolemy.actor.lib.Scale actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Scale actor = (ptolemy.actor.lib.Scale) getComponent();

        // The output of the Scale actor is the product of the input and the factor parameter
        // So use the MultiplyMonotonicFunction for the output property.
        setAtLeast(actor.output, new MultiplyMonotonicFunction(actor.input,
                actor.factor, _lattice, this));
        
        // The input of the Scale actor is the a factor of multiplication
        // So use the FactorMonotonicFunction for the input property.
        setAtLeast(actor.input, new DivideMonotonicFunction(actor.output,
                actor.factor, _lattice, this));

        return super.constraintList();
    }

    // Added by Charles Shelton 05/11/09:
    // The factor parameter for the Scale actor must be added to the list of
    // propertyable attributes in order for its property to be resolved.

    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        result.add(((ptolemy.actor.lib.Scale) getComponent()).factor);
        return result;
    }
}
