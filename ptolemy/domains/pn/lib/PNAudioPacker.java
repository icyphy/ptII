/* Assemble the decoded audio signal blocks and write to file.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNAudioPacker
/** 
Assemble the decoded audio signal blocks and write to file.

@author Yuhong Xiong, Mudit Goel
@(#)PNAudioPacker.java	1.13 09/13/98
*/

public class PNAudioPacker extends AtomicActor {
    
    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNAudioPacker(CompositeActor container, String name, int numBlocks)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        System.out.println("packer created");
        _done = new IOPort(this, "done", false, true);
        _input = new IOPort[numBlocks];
	for (int i = 0; i< numBlocks; i++) {
            _input[i] = new IOPort(this, "input" + i, true, false);
	}
	_source = new Parameter(this, "AudioFileName", new StringToken());
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //public void setInitState(String filename) {
    public void initialize() {
	String filename = ((StringToken)_source.getToken()).stringValue();
	try {
	    _file = new FileOutputStream(filename, true);
	} catch (IOException e) {
	    System.out.println("Error: In PNAudioPacker: Can't open \"" +
                    filename + "\"");
	}
        System.out.println("packer initialized");
    }
    
    /** Loop through all the input ports and convert input from double to
     *  short and write to file.
     *  Assuming output is in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {
	System.out.println("I am running :-");
	for (int j = 0; j < _input.length; j++) {
	    System.out.println("Bo fo blocks = "+j);
	    for (int l=0; l<_input[j].getWidth(); l++) {
		DoubleMatrixToken inToken= (DoubleMatrixToken)_input[j].get(l);
		double[] signal = inToken.doubleMatrix()[0];
		System.out.println("Signal length is "+signal.length);
		byte[] pcm = new byte[signal.length * 2];
		for (int k = 0; k < signal.length; k++) {
		    if (signal[k] > Short.MAX_VALUE) {
			System.out.println(getClass().getName() +
				": Overflow. signal = " + signal[k]);
		    } else if (signal[k] < Short.MIN_VALUE) {
			System.out.println(getClass().getName() +
				": Underflow. signal = " + signal[k]);
		    }
		    short tem = (short)signal[k];
		    pcm[2*k] = (byte)(tem >>> 8);
		    pcm[2*k+1] = (byte)(tem & 0xff);
		}
		try {
		    _file.write(pcm);
		} catch (IOException e) {
		    System.out.println(e.getMessage());
		}
	    }
	}
	IntToken done = new IntToken(1);
	System.out.println(" Packer writing to .. ");
	_done.broadcast(done);
	System.out.println(" Packer written to .. ");
	try {
	    _file.close();
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	}
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private Parameter _source;
    private FileOutputStream _file;
    private IOPort[] _input;
    private IOPort _done;
}

