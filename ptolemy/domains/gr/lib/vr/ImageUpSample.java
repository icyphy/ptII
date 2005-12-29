/* This actor upsamples an image stream by an integer factor by inserting blank images.

Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.gr.lib.vr;

import ptolemy.data.AWTImageToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.UpSample;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.image.BufferedImage;


// Imports go here, in alphabetical order, with no wildcards.
//////////////////////////////////////////////////////////////////////////
//// ImageUpSample

/**
   This actor upsamples an image stream by an integer factor by inserting blank images
   with the specified resolution.

   @author Tiffany Crawford
   @version $Id$
   @see Reslice
   @since Ptolemy II x.x
   @Pt.ProposedRating Red (tsc)
   @Pt.AcceptedRating Red
*/
public class ImageUpSample extends UpSample {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageUpSample(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);

        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Resolution of image */
    public Parameter xResolution;
    public Parameter yResolution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the input AWTImageToken and produce the same token on the output.
     *  Then create a number of blank image tokens of the same type as the
     *  input token on the output port, so that output.tokenProductionRate
     *  tokens are created in total.  If there is not token on the input,
     *  then this method throws a NoTokenException (which is a runtime
     *  exception).
     *  @exception IllegalActionException If a runtime type conflict occurs.
     */
    public void fire() throws IllegalActionException {
        //FIXME Is this casted properly?
        AWTImageToken token = (AWTImageToken) input.get(0);
        int factorValue = ((IntToken) factor.getToken()).intValue();
        int phaseValue = ((IntToken) phase.getToken()).intValue();
        int xResolutionValue = ((IntToken) xResolution.getToken()).intValue();
        int yResolutionValue = ((IntToken) yResolution.getToken()).intValue();

        if (phaseValue >= factorValue) {
            throw new IllegalActionException(this,
                "Phase is out of range: " + phaseValue);
        }

        AWTImageToken[] result = new AWTImageToken[factorValue];
        BufferedImage bufferedImage = new BufferedImage(xResolutionValue,
                yResolutionValue, BufferedImage.TYPE_3BYTE_BGR);
        AWTImageToken blankImageToken = new AWTImageToken(bufferedImage);

        for (int i = 0; i < factorValue; i++) {
            if (i == phaseValue) {
                result[i] = token;
            } else {
                result[i] = blankImageToken;
            }
        }

        output.send(0, result, factorValue);
    }
}
