/* Reads an audio file and divides the audio data into blocks.

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
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNImageSink
/** 
Stores an image file (int the ASCII PBM format) and creates a matrix token

@author Mudit Goel
*/

public class RLDecoder extends AtomicActor {
    
    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public RLDecoder(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);
        _dimenin = new IOPort(this, "dimensionsIn", true, false);
        _dimenout = new IOPort(this, "dimensionsOut", false, true);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
            
    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {
        //Get and send dimensions
        _dimenout.broadcast(_dimenin.get(0));
        _dimenout.broadcast(_dimenin.get(0));

        //Read and decode the input
        while(true) {
            IntToken token = (IntToken)_input.get(0);
            int value = token.intValue();
            //send the first occurence of the value
            _output.broadcast(token);
            //Obtain the number of occurences
            token = (IntToken)_input.get(0);
            int count = token.intValue();
            for (int i=1; i<count; i++) {
                _output.broadcast(new IntToken(value));
            }
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _dimenin;
    private IOPort _dimenout;
    private IOPort _output;
    private IOPort _input;
}




