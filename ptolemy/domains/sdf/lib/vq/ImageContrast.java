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
This actor change the contrast of an image. i.e.
if the input image has a lot of pixels with the same or similar color,
we can evenly distribute the color of the image with this actor using the 
Gray Scale Equalization. 
 
@author Michael Leung
@version $Id$
*/

public final class ImageContrast extends SDFAtomicActor {
    public ImageContrast(TypedCompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
    
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        
        SDFIOPort outputport = (SDFIOPort) newPort("outcontrast");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);
        outputport.setDeclaredType(IntMatrixToken.class);
        
        SDFIOPort inputport = (SDFIOPort) newPort("incontrast");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);
        inputport.setDeclaredType(IntMatrixToken.class);
    }
    
    /** Initialize the actor.
     *  Get the values of all parameters.
     *  @exception IllegalActionException if xframesize is less than one,
     *  or yframesize is less than one.
     */
    
    public void initialize() throws IllegalActionException {
        
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
    
    }
    
    /** Fire the actor.
     *  Consume one image on the input port.  
     *  
     *  Summary:
     *  Contrast the image so that  
     *  an more evenly color distributed image can be obtained. 
     *  Assume that color is bounded from 0 to 255 inclusively.
     *  @exception IllegalActionException if image color is out-of-bound.
     *  
     *  Algorithm:
     *  Construct a color histogram for the input image. 
     *  Construct a cdf for the color histogram.
     *  Using Gray Scale Equalization, re-map each image pixel color. 
     *
     *  Send the new image out the output port.
     */

    public void fire() throws IllegalActionException {
        
        int i, j;
        int colorHistogram[] = new int[256] ;
        
        SDFIOPort outputport = (SDFIOPort) getPort("outcontrast");
        SDFIOPort inputport = (SDFIOPort) getPort("incontrast");
        
        message = (IntMatrixToken) inputport.get(0);
        frame = message.intArray();
               
        //Constract a color distribution histogram for the input image:
        //Assuming the color bound for the input 0 and 255. If color detected
        //that has color either bigger than 255 OR small than 0, then throw an
        //illegal action exception.
        
        for(i = 0; i < 256; i ++) 
            colorHistogram[i] = 0;
        
        for(j = 0; j < yframesize; j ++) 
            for(i = 0; i < xframesize; i ++) {    
                frameElement = frame[xframesize*j+i];
                if ((frameElement < 0) || (frameElement > 255 ))
                    throw new IllegalActionException("ImageContrast:"+
                            "input image pixel contains at" + i + "," + j +
                            "with value" + frame[xframesize*j+i] +
                            "that is out of bounds." +
                            "Not between 0 and 255.");
                colorHistogram[frame[xframesize*j+i]]++;
            }
        
        //Construct the cdf of the color distribution histogram
        //colorHistogram[0]= colorHistogram[0]
        
        for(i = 1; i < 256; i ++)   
            colorHistogram[i] = colorHistogram[i-1] + colorHistogram[i];
        
        // Search each pixel in the image and re-map it to a new 
        // color number to make a new relatively even color distrubution
        // image.

        int distributionConstant = xframesize*yframesize/255;

        for(j = 0; j < yframesize; j ++) 
            for(i = 0; i < xframesize; i ++) {
                frameElement = frame[xframesize*j+i];
                frame[xframesize*j+i] = colorHistogram[frameElement] /
                    distributionConstant;
            }
                            
        message = new IntMatrixToken(frame, yframesize, xframesize);
        outputport.send(0, message);
	
    } 
    
    IntMatrixToken message;
    
    private int part[];
    private int frame[];
    private int xframesize;
    private int yframesize;
    private int frameElement;
    private int colorHistogram[];
    private int distributionConstant;
}








