/* One line description of file.

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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (acataldo@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.apps.softwalls;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// AngleProcessor
/**

Given an input angle, it computes the equivalent angle in the range [0, 2*Pi).

@author Adam Cataldo
@version $Id$
@since Ptolemy II 2.0.1
*/
public class AngleProcessor extends TypedAtomicActor {
    /** Constructs an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AngleProcessor(CompositeEntity container, String name)
	throws IllegalActionException, NameDuplicationException {

	super(container, name);

	// Create and configure ports
	inputAngle = new TypedIOPort(this, "inputAngle", true, false);
        outputAngle = new TypedIOPort(this, "outputAngle", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input angle */
    public TypedIOPort inputAngle;

    /** Output angle */
    public TypedIOPort outputAngle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Overrides the base class to output the correct angle.
     *  @exception IllegalActionException Not thrown in this base class.
     */

    public void fire() throws IllegalActionException {
        double angle;
        /** Get current function and gradient information.
         */

        angle = ((DoubleToken)inputAngle.get(0)).doubleValue();

        while (angle < 0) {
            angle = angle + 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle = angle - 2 * Math.PI;
        }

        outputAngle.send(0, new DoubleToken(angle));
    }
}


