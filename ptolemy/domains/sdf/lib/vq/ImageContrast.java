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
//// ImageContrast
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class ImageContrast extends SDFAtomicActor {
    public ImageContrast(TypedCompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
    
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "outmax", new IntToken("255"));
        new Parameter(this, "outmin", new IntToken("0"));
 
        SDFIOPort outputport = (SDFIOPort) newPort("contrast");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);
        outputport.setDeclaredType(IntMatrixToken.class);

        SDFIOPort inputport = (SDFIOPort) newPort("figure");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);
        inputport.setDeclaredType(IntMatrixToken.class);
    }

    public void initialize() throws IllegalActionException {
	Parameter p;
	p = (Parameter) getAttribute("XFramesize");
        xframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YFramesize");
        yframesize = ((IntToken)p.getToken()).intValue();

    }

    public void fire() throws IllegalActionException {
        int i, j;
	
        int a;
        
        int inMin, inMax;
        int frameElement;
        SDFIOPort outputport = (SDFIOPort) getPort("contrast");
        SDFIOPort inputport = (SDFIOPort) getPort("figure");
       
        //for just now, we used inMax = 255 and inMin =0
        inMax = 255;
        inMin = 0;
        message = (IntMatrixToken) inputport.get(0);
        frame = message.intArray();

        for(j = 0; j < yframesize; j ++) 
            for(i = 0; i < xframesize; i ++) {    
                frameElement = frame[xframesize*j+i];
                if (frameElement >= inMax)
                    inMax = frameElement;
                if (frameElement <= inMin)
                    inMin = frameElement;
            }
        
        //if the (inMax != inMin), inMax and inMin is not the default
        //value 255 and 0, then do the image contrast. If not, don't do
        // anything             

        if ((inMax != inMin) && !((inMax == 255) && (inMin == 0))) 
            for (j = 0; j < yframesize; j ++)
                for(i = 0; i < xframesize; i ++) {
                    frameElement = frame[xframesize*j+i];
                    frameElement = (frameElement - inMin)* 255 / (inMax-inMin);                 }
        message = new IntMatrixToken(frame, yframesize, xframesize);
        outputport.send(0, message);
	
    } 

    IntMatrixToken message;
 
    private int part[];
    private int frame[];
    private int xframesize;
    private int yframesize;
         
}








