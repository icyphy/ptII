/* An actor that outputs the a complex image from a magnitude and
phase input.

@Copyright (c) 2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIPolarToComplex
/**
Output a complex image with alternating real and imaginary bands.  This
actor takes two inputs, an image representing the magnitude of the
bands, and an image representing the phase of the bands.  The two
inputs must have the same number of bands.

<p> The output of this actor may not be suitable for displaying or saving
because of the increase in the number of bands, as well as the high
resolution of the data (doubles).

@see JAIDataConvert
@author James Yeh
@version $Id$
@since Ptolemy II 3.1
*/

public class JAIPolarToComplex extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIPolarToComplex(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        magnitude = new TypedIOPort(this, "magnitude", true, false);
        phase = new TypedIOPort(this, "phase", true, false);
        output = new TypedIOPort(this, "output", false, true);

        magnitude.setTypeEquals(BaseType.OBJECT);
        phase.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The magnitude image input.  This image must have the same
     *  amount of bands as the phase image input.
     */
    public TypedIOPort magnitude;

    /** The output of the actor.  This image contains twice as many
     *  as the input.  The first two bands correspond to the cartesian
     *  values of the first magnitude and phase bands.  The second two
     *  bands correspond to the cartesian values of the second magnitude
     *  and phase bands, etc.
     */
    public TypedIOPort output;

    /** The phase image input.  This image must have the same amount of
     *  bands as the phase magnitude image input.
     */
    public TypedIOPort phase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the the complex image from its magnitude and phase
     *  components.
     *  @exception IllegalActionException If a contained method throws
     *  it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken magnitudeToken = (JAIImageToken)magnitude.get(0);
        JAIImageToken phaseToken = (JAIImageToken)phase.get(0);
        RenderedOp magnitude = magnitudeToken.getValue();
        RenderedOp phase = phaseToken.getValue();
        RenderedOp newImage =
            JAI.create("polartocomplex", magnitude, phase);
        output.send(0, new JAIImageToken(newImage));
    }
}
