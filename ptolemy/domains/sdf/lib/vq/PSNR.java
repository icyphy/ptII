/* Find the Signal to Noise ratio between two signals
@Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.math.ExtendedMath;

//////////////////////////////////////////////////////////////////////////
//// PSNR
/**
This actor consumes an IntMatrixToken from each input port, and calculates the
Power Signal to Noise Ratio (PSNR) between them.  The PSNR is output on the
output port as a DoubleToken.

@author Michael Leung, Steve Neuendorffer
@version $Id$
*/
public class PSNR extends SDFAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PSNR(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.DOUBLE);

        signal = (SDFIOPort) newPort("signal");
        signal.setInput(true);
        signal.setTypeEquals(BaseType.INT_MATRIX);

        distortedSignal = (SDFIOPort) newPort("distortedSignal");
        distortedSignal.setInput(true);
        distortedSignal.setTypeEquals(BaseType.INT_MATRIX);

    }

    /** The input signal. */
    public SDFIOPort signal;

    /** A distorted version of the input signal. */
    public SDFIOPort distortedSignal;

    /** The calculated PSNR between the two inputs. */
    public SDFIOPort output;

    /** Fire the actor.
     *  Consume one image on each of the input ports.   Calculate the
     *  PSNR as follows:
     *  noise = signal-distortedSignal;
     *  signalPower = Power(signal);
     *  noisePower = Power(noise);
     *  PSNR = 10 * log10(signalPower/noisePower);
     *  @exception IllegalActionException if
     *  the dimensions of the input tokens do not match.
     */
    public void fire() throws IllegalActionException {

        int i, j;
        int signalPower = 0;
        int noisePower = 0;
        int element1;
        int element2;

        double PSNRValue;

        IntMatrixToken signalToken =
            (IntMatrixToken) signal.get(0);
        IntMatrixToken distortedSignalToken =
            (IntMatrixToken) distortedSignal.get(0);
        int columns = signalToken.getColumnCount();
        int rows = signalToken.getRowCount();
        if((distortedSignalToken.getColumnCount() != columns)||
                (distortedSignalToken.getRowCount() != rows)) {
            throw new IllegalActionException("Input token dimensions " +
                    "must match!");
        }

        for(j = 0; j < rows; j ++) {
            for(i = 0; i < columns; i ++) {

                element1 = signalToken.getElementAt(j, i);
                element2 = distortedSignalToken.getElementAt(j, i);

                signalPower = signalPower +
                    element1 * element1;
                noisePower = noisePower +
                    (element1 - element2)*
                    (element1 - element2);
            }
        }

        PSNRValue = 10 * ExtendedMath.log10((double)signalPower / noisePower);

        DoubleToken message = new DoubleToken(PSNRValue);
        output.send(0, message);
    }
}
