/* An actor that performs pitch detection on an audio signal.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.domains.sdf.lib.javasound;

import ptolemy.domains.sdf.test.pitchshift.*;
import ptolemy.math.*;
import ptolemy.math.SignalProcessing;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import collections.LinkedList;

//import ptolemy.media.*;
import javax.media.sound.sampled.*;

import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFPitchDetector
/**


@author Brian K. Vogel
@version
*/

public class SDFPitchDetector extends SDFAtomicActor {

    /** 
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFPitchDetector(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
	// FIXME: Allow this to be set as parameter.
	productionRate = 512;
	consumptionRate = productionRate;
	output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(DoubleToken.class);
	output.setTokenProductionRate(productionRate);

	input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(DoubleToken.class);
	input.setTokenConsumptionRate(consumptionRate);
		
	
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

     /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the director when type changes in the parameters occur.
     *  This will cause type resolution to be redone at the next opportunity.
     *  It is assumed that type changes in the parameters are implemented
     *  by the director's change request mechanism, so they are implemented
     *  when it is safe to redo type resolution.
     *  If there is no director, then do nothing.
     */
    public void attributeTypeChanged(Attribute attribute) {
        Director dir = getDirector();
        if (dir != null) {
            dir.invalidateResolvedTypes();
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>isPeriodic</code> and <code>pathName</code>
     *  public members to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
	 try {
	     SDFPitchDetector newobj = (SDFPitchDetector)super.clone(ws);
	     newobj.output = (SDFIOPort)newobj.getPort("output");
	     newobj.input = (SDFIOPort)newobj.getPort("input");
	     // set the type constraints.
	     return newobj;
	 } catch (CloneNotSupportedException ex) {
	     // Errors should not occur here...
	     throw new InternalErrorException(
			 "Clone failed: " + ex.getMessage());
	 }
    }

    /** Output the sample value of the sound file corresponding to the
     *  current index.
     */
    public void fire() throws IllegalActionException {
        
	DoubleToken[] audioTokenArray = new DoubleToken[consumptionRate];
        input.getArray(0, audioTokenArray);
	// Convert to double[].
        double[] audioInDoubleArray = new double[consumptionRate];
        int i;
        for (i = 0; i < consumptionRate; i++) {
            audioInDoubleArray[i] = audioTokenArray[i].doubleValue();
        }
	
	double[] currPitchArray = pd.performPitchDetect(audioInDoubleArray);

	
	// Convert to DoubleToken[].
	// FIXME: I don't think this is very efficient. Currently
	// creating a new token for each sample!
	for (i = 0; i < productionRate; i++) {
	    audioTokenArray[i] = new DoubleToken(currPitchArray[i]);
	}
	output.sendArray(0, audioTokenArray);
    }

    /** Read in the sound file specified by the <i>pathName</i> parameter
     *  and initialize the current sample index to 0.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

	sampleRate = 22050;
	
	pd = new PitchDetector(productionRate,
			       (int)sampleRate);
	    
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   //// 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private double sampleRate;

    private PitchDetector pd;

    private int productionRate;
    private int consumptionRate;

}
