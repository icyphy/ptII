/* This determines the info class to be sent to the DAC, by looking at the nodeID

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
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;
import java.lang.Math;

//////////////////////////////////////////////////////////////////////////
//// PNRDIdentifier
/** 
@author Mudit Goel
@version @(#)PNRDIdentifier.java	1.13 09/13/98
*/
public class PNRDIdentifier extends AtomicActor {
    
    /** Constructor  Adds port   
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     */
    public PNRDIdentifier(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);
        //System.out.println("Identifier created : "+getName());
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initializes the Star. Should be called before execution
     * @param prime is the prime for this sieve
     */	
    public void setInitState(int depth, int branch) 
            throws NameDuplicationException {
        _depth = depth;
        _branch = branch;
    }
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    public void fire() throws IllegalActionException {
        ObjectToken data = null;
	//for (int i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	data = (ObjectToken)_input.get(0);
	PNRDInfo info = (PNRDInfo)data.getValue();
	
	int maxbranch = (int)Math.pow(2,_depth);
	int branch = _branch;
	while (true) {
	    if (maxbranch == 1) break;
	    if (branch < maxbranch/2) {
		info = info.left;
	    } else {
		info = info.right;
		branch -= maxbranch/2;
	    }
	    maxbranch = maxbranch/2;
	}
	int[][] hack = new int[1][];
	hack[0] = info.codeBookEntry;
	IntMatrixToken code = new IntMatrixToken(hack);
	_output.broadcast(code);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _input;
    private IOPort _output;
    private int _branch;
    private int _depth;
}


