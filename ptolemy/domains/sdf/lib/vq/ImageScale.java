/*
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
@AcceptedRating Red
@ProposedRating Red
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

//////////////////////////////////////////////////////////////////////////
//// ImageScale
/**
This actor re-scales an image. i.e.
it change the degree of black/white according to the user specified
outmax & outmin. Outmax is the upperlimit that user wants to scale the
image to and outmin is the lowerlimit that user wants to scale the image
to. Default values are 255 for outmax and 0 for outmin.

@author Michael Leung
@version $Id$
*/

public final class ImageScale extends SDFAtomicActor {
    public ImageScale(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "outmax", new IntToken("255"));
        new Parameter(this, "outmin", new IntToken("0"));

        SDFIOPort outputport = (SDFIOPort) newPort("contrast");
        outputport.setOutput(true);
        outputport.setTokenProductionRate(1);
        outputport.setTypeEquals(BaseType.INT_MATRIX);

        SDFIOPort inputport = (SDFIOPort) newPort("figure");
        inputport.setInput(true);
        inputport.setTokenConsumptionRate(1);
        inputport.setTypeEquals(BaseType.INT_MATRIX);
    }

    /** Initialize the actor.
     *  Get the values of all parameters.
     *  @exception IllegalActionException If outmax is less than outmin,
     *  or xframesize is less than one, or yframesize is less than one.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

	Parameter p;
	p = (Parameter) getAttribute("XFramesize");
        xframesize = ((IntToken)p.getToken()).intValue();
        if(xframesize < 0)
            throw new IllegalActionException(
                    "The value of the xframesize parameter(" + xframesize +
                    ") must be greater than zero.");
        p = (Parameter) getAttribute("YFramesize");
        yframesize = ((IntToken)p.getToken()).intValue();
        if(yframesize < 0)
            throw new IllegalActionException(
                    "The value of the yframesize parameter(" + yframesize +
                    ") must be greater than zero.");
        p = (Parameter) getAttribute("outmax");
        outmax = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("outmin");
        outmin = ((IntToken)p.getToken()).intValue();
        if(outmax < outmin)
            throw new IllegalActionException(
                    "The value of the outmax parameter(" + outmax +
                    ") must be greater than " +
                    "the value of the outmin parameter(" + outmin +").");


    }

    /** Fire the actor.
     *  Consume one image on the input port.  Rescale it, so that its values
     *  range between the values specified in parameters outmax and outmin.
     *  Send the new image out the output port.
     *
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {

        int i, j;
        int a;
        int inMin, inMax;
        int frameElement;
        SDFIOPort outputport = (SDFIOPort) getPort("contrast");
        SDFIOPort inputport = (SDFIOPort) getPort("figure");

        IntMatrixToken message = (IntMatrixToken) inputport.get(0);
        int frame[] = message.intArray();

        //look into the image to find and set the most dark spot (inMin) and
        // the most light spot (inMax)
        inMax = 0;
        inMin = 255;
        for(j = 0; j < yframesize; j ++)
            for(i = 0; i < xframesize; i ++) {
                frameElement = frame[xframesize*j+i];
                if (frameElement >= inMax)
                    inMax = frameElement;
                if (frameElement <= inMin)
                    inMin = frameElement;
            }

        //There are two cases which we should not do the for loop
        //to change the contrast.
        // case I : if inMax == inMin, in which case there will be a
        //          division by zero.
        // case II: if inMax and inMin are already the same as the user
        //          specified outmax and outmin

        if ((inMax != inMin) && !((inMax == outmax) && (inMin == outmin)))
            for (j = 0; j < yframesize; j ++)
                for(i = 0; i < xframesize; i ++) {
                    frame[xframesize*j+i] =
                        (frame[xframesize*j+i] - inMin)* (outmax - outmin) /
                        (inMax-inMin) + outmin;
                }
        message = new IntMatrixToken(frame, yframesize, xframesize);
        outputport.send(0, message);

    }

    private int xframesize;
    private int yframesize;
    private int outmax;
    private int outmin;
}
