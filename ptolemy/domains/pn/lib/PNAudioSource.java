/* Reads an audio file and divides the audio data into blocks.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNAudioSource
/** 
Reads an audio file and divides the audio data into blocks.

@author Yuhong Xiong, Mudit Goel
@(#)PNAudioSource.java	1.22 09/13/98
*/

public class PNAudioSource extends AtomicActor {
    
    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNAudioSource(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        //_input.ISINPUT = false;
        _output = new IOPort(this, "output", false, true);
        //_output.ISINPUT = false;
	_filename = new Parameter(this, "Audio_Source", new StringToken());
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void initialize() {
	String filename =((StringToken)_filename.getToken()).stringValue();
	try {
	    FileInputStream fis = new FileInputStream(filename);
	    _file = new DataInputStream(fis);
	} catch (FileNotFoundException e) {
	    System.out.println("Error: In PNAudioSource: file \"" +
                    filename + "\" not found!");
	}
    }

    //public void initialize() {
    //setInitState("/users/mudit/_Ptolemy/tycho/java/ptolemy/domains/pn/demo/test.bin");
    //}

    
    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    //FIXME: CUrrently this is a BIIIIIG hack and should be changed ASA 
    // the new kernel strategy is implemented
    public void fire() throws IllegalActionException {
        try {
	    //Read the segment request
	    int segmentsize = -1;
	    IntToken segment = (IntToken)_input.get(0);
	    segmentsize = segment.intValue();
	    
	    short[] myshort = new short[segmentsize];
	    for (int k = 0; k < segmentsize; k++) {
		myshort[k] = _file.readShort();
	    }
	    
	    double[][] data = new double[1][segmentsize];
	    for (int k = 0; k < segmentsize; k++) {
		data[0][k] = (double)myshort[k];
	    }
	    
	    DoubleMatrixToken dataToken = new DoubleMatrixToken(data);
	    _output.broadcast(dataToken);
	    try {
		_file.close();
	    } catch (IOException e) {
		System.out.println(e.getMessage());
	    }
	} catch (IOException e) {
	    System.out.println("PNAudioSource Can't readShort.");
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    //    private FileInputStream _file;
    private Parameter _filename;
    private DataInputStream _file;
    //private int _blockSize;
    //private byte[] _buffer;
    private IOPort _output;
    private IOPort _input;
}




