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


//////////////////////////////////////////////////////////////////////////
//// ZeroBias
/**
This takes state input from the aircraft, and outputs a zero whenever it's
fired.  The only purpose of this is to have a replacement actor for testing.

@author Adam Cataldo
@version $Id$
@since Ptolemy II 2.0.1
*/
public class ZeroBias extends TypedAtomicActor {
    /** Constructs an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ZeroBias(CompositeEntity container, String name) 
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
	// Create and configure ports
	x = new TypedIOPort(this, "x", true, false);
	y = new TypedIOPort(this, "y", true, false);
	heading = new TypedIOPort(this, "heading", true, false);
        bankAngle = new TypedIOPort(this, "bankAngle", true, false);
	bias = new TypedIOPort(this, "bias", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Current x position */
    public TypedIOPort x;

    /** Current y position */
    public TypedIOPort y;

    /** Current heading angle */
    public TypedIOPort heading;

    /** Current bank angle */
    public TypedIOPort bankAngle;

    /** Output bias */
    public TypedIOPort bias;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Overrides the base class to output a value.
     * @exception IllegalActionException Not thrown in this base class.
     */

    public void fire() throws IllegalActionException {
	bias.send(0, new DoubleToken(0.0));
    }

}


