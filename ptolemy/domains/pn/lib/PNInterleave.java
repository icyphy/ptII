/* This interleaves elements from two streams into one stream

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.domains.pn.kernel;
import pt.kernel.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNInterleave
/** 
Merges two input streams into one output stream by alternating between the two outputs.

@author Mudit Goel
@version $Id$
*/
public class PNInterleave extends PNStar{
    /** Constructor
     */	
    public PNInterleave() {
        super();
    }

    /** Constructor
     */
    public PNInterleave(String name) {
        super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    public void initialize(PNExecutive myExecutive) 
            throws NameDuplicationException, GraphException {
        _input = addInPort(this, "input");
        _output = addOutPort(this, "output");
        _myExecutive = myExecutive;
        _myExecutive.registerStar(this);
    }

    /** Description
     */	
    public void run() {
        int data;
        int i;
        try {
            for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	 	Enumeration relations = _input.enumRelations();
		while (relations.hasMoreElements()) {
		    PNFifoRelation nextQueue = (PNFifoRelation)relations.nextElement();
                    data = readFrom(_input, nextQueue);
                    writeTo(_output, data);
                    System.out.println(this.getName()+" writes "+data+" to "+_output.getName());
		}
            }
            _myExecutive.processStopped();
        } catch(TerminationException e) {
	    System.out.println("Terminating "+ this.getName());
            return;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private PNInPort _input;
    private PNOutPort _output;

}
