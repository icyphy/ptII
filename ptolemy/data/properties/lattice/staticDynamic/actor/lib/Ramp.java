/* A helper class for ptolemy.actor.lib.Const.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.staticDynamic.actor.lib;

import ptolemy.data.ScalarToken;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.staticDynamic.Lattice;
import ptolemy.data.properties.token.PortValueSolver;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Const

/**
 A helper class for ptolemy.actor.lib.Const.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Ramp extends PropertyConstraintHelper {

    /**
     * Construct a Const helper for the staticDynamic lattice.
     * This set a permanent constraint for the output port to
     * be STATIC, but does not use the default actor constraints.
     * @param actor The given Source actor
     * @param lattice The staticDynamic lattice.
     * @exception IllegalActionException 
     */
    public Ramp(PropertySolver solver, ptolemy.actor.lib.Ramp actor)
            throws IllegalActionException {

        super(solver, actor, false);

        setAtLeast(actor.output, new FunctionTerm());
    }

    /**
     * 
     * @author mankit
     *
     */
    private class FunctionTerm extends MonotonicFunction {

        /**
         * 
         */
        public Object getValue() throws IllegalActionException {
            ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) _component;
            Lattice lattice = (Lattice) getSolver().getLattice();

            PropertyToken property = (PropertyToken) PortValueSolver
                    .findSolver("staticValueToken").getHelper(_component)
                    .getProperty(actor.step);

            if (property != null
                    && ((ScalarToken) property.getToken()).doubleValue() == 0) {
                return lattice.STATIC;
            }

            return lattice.DYNAMIC;
        }

        /**
         * 
         */
        public InequalityTerm[] getVariables() {
            ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) _component;

            try {
                InequalityTerm term = getPropertyTerm(actor.step);

                if (term.isSettable()) {
                    return new InequalityTerm[] { term };
                }
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return new InequalityTerm[0];
        }

    }
}
