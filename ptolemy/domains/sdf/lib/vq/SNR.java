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
//// SNR
/** 
This actor get two image token (type: IntMatrixToken); 
one image token comes from the original input image,
another one comes from the modified ouput image; and calculate the 
Power Signal to Noise Ratio (PSNR).

@author Michael Leung
@version $Id$
*/

public final class SNR extends SDFAtomicActor {
    public SNR(TypedCompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        
        super(container, name);
        
	new Parameter(this, "originalXFramesize", new IntToken("176"));
        new Parameter(this, "originalYFramesize", new IntToken("144"));
        new Parameter(this, "modifiedXFramesize", new IntToken("176"));
        new Parameter(this, "modifiedYFramesize", new IntToken("144"));
        
        //Aviod it from being eaten by ActorConsumer, 
        //for right now, don't set the 
        //outputport.
        
        //SDFIOPort outputport = (SDFIOPort) newPort("SNRvalue");
        //outputport.setOutput(true);
        //setTokenProductionRate(outputport, 1);
        //outputport.setDeclaredType(IntToken.class);
        
        SDFIOPort inputport1 = (SDFIOPort) newPort("inoriginal");
        inputport1.setInput(true);
        setTokenConsumptionRate(inputport1, 1);
        inputport1.setDeclaredType(IntMatrixToken.class);
        
        SDFIOPort inputport2 = (SDFIOPort) newPort("inmodified");
        inputport2.setInput(true);
        setTokenConsumptionRate(inputport2, 1);
        inputport2.setDeclaredType(IntMatrixToken.class);
        
    }
    
    /** Initialize the actor.
     *  Get the values of all parameters.
     *  
     *  Assume that the x and y framesize for both of the image has to be
     *  greater than zero. Assume that xframesize and yframesize of both image 
     *  has to be the same, otherwise the SNR value becomes insiginficant.
     *  
     *  @exception IllegalActionException if :
     *   
     *  case 1) xframesize of the original image is less than one.
     *  case 2) xframesize of the modified image is less than one.
     *  case 3) yframesize of the original image is less than one.
     *  case 4) yframesize of the modified image is less than one.
     *  case 5) xframesize of the original and modified image is not the same. 
     *  case 6) yframesize of the original and modified image is not the same.
     */
    
    public void initialize() throws IllegalActionException {
        
	Parameter p;
	
        p = (Parameter) getAttribute("originalXFramesize");
        originalXFramesize = ((IntToken)p.getToken()).intValue();
        if(originalXFramesize < 0) 
            throw new IllegalActionException(
                    "The value of the xframesize (original image) parameter("
                    + originalXFramesize +
                    ") must be greater than zero.");
        
        p = (Parameter) getAttribute("modifiedXFramesize");
        modifiedXFramesize = ((IntToken)p.getToken()).intValue();
        if(modifiedXFramesize < 0) 
            throw new IllegalActionException(
                    "The value of the xframesize (modified image) parameter("
                    + modifiedXFramesize +
                    ") must be greater than zero.");
        
        p = (Parameter) getAttribute("originalYFramesize");
        originalYFramesize = ((IntToken)p.getToken()).intValue();
        if(originalYFramesize < 0) 
            throw new IllegalActionException(
                    "The value of the yframesize (original image) parameter(" 
                    + originalYFramesize + 
                    ") must be greater than zero.");
        
        p = (Parameter) getAttribute("modifiedYFramesize");
        modifiedYFramesize = ((IntToken)p.getToken()).intValue();
        if(modifiedYFramesize < 0) 
            throw new IllegalActionException(
                    "The value of the yframesize (modified image) parameter(" 
                    + modifiedYFramesize + 
                    ") must be greater than zero.");
        
        if ((originalXFramesize != modifiedXFramesize))
            throw new IllegalActionException(
                    "The value of the original XFramesize parameter(" 
                    + originalXFramesize + 
                    ") is not the same as the modified XFramesize parameter."
                    + modifiedXFramesize );
        
        if ((originalYFramesize != modifiedYFramesize))
            throw new IllegalActionException(
                    "The value of the original YFramesize parameter(" 
                    + originalYFramesize + 
                    ") is not the same asthe  modified YFramesize parameter."
                    + modifiedYFramesize );
    }
    
    /** Fire the actor.
     *  Consume one image on each of the input ports.  
     *  
     *  Summary:
     *  Loop thru both of the original image and the modified image
     *  and find the Signal Power and Noise Power
     *         signalPower--- sum of the square of the all original
     *                        image pixel values
     *         noisePower --- sum of the square of all of the difference
     *                        between the original image pixels value
     *                        and the modified image pixels value
     *
     *  Assume that color is bounded from 0 to 255 inclusively.
     *  @exception IllegalActionException if image color is out-of-bound.
     *  
     *  Algorithm:
     *  Signal to Nosie Ratio (SNR) can be found by the equation:
     *  
     *  SNR = 10 * log (signalPower/noisePower)
     *  (assume log base 10)
     *
     *  For this version, just print out the value of SNR in the UNIX window.
     *  When the suitable display actor is ready, send the value 
     *  of SNR out to the output port.
     */
    
    public void fire() throws IllegalActionException {
        
        int i, j;
        int originalFrame[] = new int[originalXFramesize * originalYFramesize];
        int modifiedFrame[] = new int[modifiedXFramesize * modifiedYFramesize];
        int signalPower = 0;
        int noisePower = 0;
        int originalFrameElement;
        int modifiedFrameElement;
        
        // variable Power of Signal to Noise Ration in dB scale
        double SNR; 
        
        //SDFIOPort outputport = (SDFIOPort) getPort("SNRvalue");
        SDFIOPort inputport1 = (SDFIOPort) getPort("inoriginal");
        SDFIOPort inputport2 = (SDFIOPort) getPort("inmodified");
        
        IntMatrixToken originalMessage;
        IntMatrixToken modifiedMessage;
        
        originalMessage = (IntMatrixToken) inputport1.get(0);
        originalFrame = originalMessage.intArray();
        
        modifiedMessage = (IntMatrixToken) inputport2.get(0);
        modifiedFrame = modifiedMessage.intArray();
        
        for(j = 0; j < originalYFramesize; j ++) 
            for(i = 0; i < originalXFramesize; i ++) {    
                
                originalFrameElement = originalFrame[originalXFramesize*j+i];
                modifiedFrameElement = modifiedFrame[originalXFramesize*j+i];
                
                if ((originalFrameElement < 0) || 
                        (originalFrameElement > 255 ))
                    throw new IllegalActionException("SNR:"+
                            "The original image contains a pixel at " + i + 
                            ", " + j + " with value " 
                            + originalFrame[originalXFramesize*j+i] +
                            " that is not between 0 and 255.");
                
                if ((modifiedFrameElement < 0) || 
                        (modifiedFrameElement > 255 ))
                    throw new IllegalActionException("SNR:"+
                            "The modified image contains a pixel at " + i + 
                            ", " + j + " with value " 
                            + modifiedFrame[originalXFramesize*j+i] +
                            "that is not between 0 and 255.");
                
                signalPower = signalPower + 
                    originalFrameElement*originalFrameElement;
                noisePower = noisePower + 
                    (originalFrameElement - modifiedFrameElement)*
                    (originalFrameElement - modifiedFrameElement);
                //  System.out.println ("signal = "+signalPower);
                //  System.out.println ("noisePower = "+noisePower);
            }
        
        SNR = 10 * Math.log ((double) signalPower / noisePower) / 
            Math.log (10.0) ;
        
        message = new DoubleToken(SNR);
        //outputport.send(0, message);
        System.out.println ("SNR = "+SNR);
    }
    
    DoubleToken message;
    
    private int originalFrame[];
    private int modifiedFrame[];
    private int originalXFramesize;
    private int modifiedXFramesize;
    private int originalYFramesize;
    private int modifiedYFramesize;
    private int originalFrameElement;
    private int modifiedFrameElement;
    private int signalPower;
    private int noisePower;
    private int SNR;
}











