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

import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.lattice.PropertyConstraintHelper;
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
public class Expression extends PropertyConstraintHelper {

    /**
     * Construct a Const helper for the staticDynamic lattice.
     * This set a permanent constraint for the output port to
     * be STATIC, but does not use the default actor constraints.
     * @param actor The given Source actor
     * @param lattice The staticDynamic lattice.
     * @exception IllegalActionException 
     */
    public Expression(PropertySolver solver, ptolemy.actor.lib.Expression actor)
            throws IllegalActionException {

        super(solver, actor, false);

        setAtLeast(actor.output, actor.expression);
    }

}
