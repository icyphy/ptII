/*
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

//////////////////////////////////////////////////////////////////////////
//// ImageScale
/** ImageScale re-scales the contrast of the appearance of the image. i.e.
/   it change the degree of black/white according to the user specified
/   outmax & outmin. Outmax is the upperlimit that user wants to scale the 
/   image to and outmin is the lowerlimit that user wants to scale the image
/   to. For the sake of convenience, right now I've set the VideoDemo.tcl
/   outmax = 255 and outmin = 0.
/ 
@author Michael Leung
@version $Id$
*/

public final class ImageScale extends SDFAtomicActor {
    public ImageScale(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
    
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "outmax", new IntToken("255"));
        new Parameter(this, "outmin", new IntToken("0"));
 
        SDFIOPort outputport = (SDFIOPort) newPort("contrast");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);

        SDFIOPort inputport = (SDFIOPort) newPort("figure");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);
    }

    public void initialize() throws IllegalActionException {

	Parameter p;
	p = (Parameter) getAttribute("XFramesize");
        xframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YFramesize");
        yframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("outmax");
        outmax = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("outmin");
        outmin = ((IntToken)p.getToken()).intValue();
    }

    public void fire() throws IllegalActionException {

        int i, j;
        int a;
        int inMin, inMax;
        int frameElement;
        SDFIOPort outputport = (SDFIOPort) getPort("contrast");
        SDFIOPort inputport = (SDFIOPort) getPort("figure");
       
        //setting inMax and inMin to the user specified outmax and outmin
        //contrast degree, the largest contrast will be outmax = 255 and 
        //outmin = 0

        inMax = 0;
        inMin = 255;
        message = (ImageToken) inputport.get(0);
        frame = message.intArray();
        //System.out.println("outmax="+outmax);
        //System.out.println("outmin="+outmin);

        //look into the image to find and set the most dark spot (inMax) and
        // the most light spot (inMin)

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
        // case I : if inMax and inMin variable all has the same values
        //          which you don't have to do the job
        // case II: if inMax and inMin is already the same as the user
        //          specified contrast degree, which is outmax and outmin

        if ((inMax != inMin) && !((inMax == outmax) && (inMin == outmin))) 
            for (j = 0; j < yframesize; j ++)
                for(i = 0; i < xframesize; i ++) {
                    frame[xframesize*j+i] = 
                        (frame[xframesize*j+i] - inMin)* (outmax - outmin) / 
                        (inMax-inMin) + outmin;
                }
        message = new ImageToken(frame, yframesize, xframesize);
        outputport.send(0, message);
	
    } 

    ImageToken message;
 
    private int part[];
    private int frame[];
    private int xframesize;
    private int yframesize;
    private int outmax;
    private int outmin;
}








