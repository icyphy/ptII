/* Find the Signal to Noise ratio between two signals
@Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (mikele@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import java.lang.Math;

//////////////////////////////////////////////////////////////////////////
//// PSNR
/**
This actor consumes an IntMatrixToken from each input port, and calculates the
Power Signal to Noise Ratio (PSNR) between them.  The PSNR is output on the
output port as a DoubleToken.

@author Michael Leung, Steve Neuendorffer
@version $Id$
*/
public final class PSNR extends SDFAtomicActor {
    /** Create a new PSNR actor with the given container and name. */
    public PSNR(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(DoubleToken.class);

        signal = (SDFIOPort) newPort("signal");
        signal.setInput(true);
        signal.setTypeEquals(IntMatrixToken.class);

        distortedSignal = (SDFIOPort) newPort("distortedSignal");
        distortedSignal.setInput(true);
        distortedSignal.setTypeEquals(IntMatrixToken.class);

    }

    /** The input signal. */
    public SDFIOPort signal;
    /** A distorted version of the input signal. */
    public SDFIOPort distortedSignal;
    /** The calculated PSNR between the two inputs. */
    public SDFIOPort output;

    /** Fire the actor.
     *  Consume one image on each of the input ports.
     *
     *  Summary:
     *  Loop thru both of the signal image and the distortedSignal image
     *  and find the Signal Power and Noise Power.
     *         signalPower--- sum of the square of the all signal
     *                        image pixel values.
     *         noisePower --- sum of the square of all of the difference
     *                        between the signal image pixels value
     *                        and the distortedSignal image pixels value.
     *
     *  Assume that pixel values are bounded from 0 to 255 inclusively.
     *
     *  Algorithm:
     *  Signal to Nosie Ratio (PSNR) can be found by the equation:
     *
     *  PSNR = 10 * log10(signalPower/noisePower)
     *
     *  @exception IllegalActionException if a pixel value is not between 
     *  zero and 255, or the dimensions of the input tokens do not match.
     */

    public void fire() throws IllegalActionException {

        int i, j;
        int signalPower = 0;
        int noisePower = 0;
        int pixel1;
        int pixel2;

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

                pixel1 = signalToken.getElementAt(j, i);
                pixel2 = distortedSignalToken.getElementAt(j, i);

                if ((pixel1 < 0) ||
                        (pixel1 > 255 ))
                    throw new IllegalActionException("PSNR:"+
                            "The signal contains a pixel at " + i +
                            ", " + j + " with value " + pixel1 +
                            " that is not between 0 and 255.");

                if ((pixel2 < 0) ||
                        (pixel2 > 255 ))
                    throw new IllegalActionException("PSNR:"+
                            "The distortedSignal contains a pixel at " + i +
                            ", " + j + " with value " + pixel2 +
                            "that is not between 0 and 255.");

                signalPower = signalPower +
                    pixel1 * pixel1;
                noisePower = noisePower +
                    (pixel1 - pixel2)*
                    (pixel1 - pixel2);
            }
        }

        PSNRValue = 10 * Math.log ((double) signalPower / noisePower) /
            Math.log (10.0) ;

        DoubleToken message = new DoubleToken(PSNRValue);
        output.send(0, message);
    }
}
