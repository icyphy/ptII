/* Alternates the input into two different outputs

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
//// PNAlternate
/** 
@author Mudit Goel
@version $Id$
*/
public class PNAlternate extends PNStar {
    /** Constructor
     */	
    public PNAlternate() {
	super();
    }

    /** Constructor
     */
    public PNAlternate(String name) {
        super(name);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Initializes the Star. Should be called before execution
     * @param myExecutive is the executive responsible for execution
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     * @exception GraphException is thrown if a port with a null name is passed
     */	
    public void initialize(PNExecutive myExecutive)
            throws NameDuplicationException, GraphException {
        _input = addInPort(this, "input");
        _output = addOutPort(this, "output");
        _myExecutive = myExecutive;
        _myExecutive.registerStar(this);
    }
    
    /** Reads one input from it's input port and writes this token to each 
     *  of it's output ports. Needs to read one token each for every output
     *  port. Goes through the list of ports in a circular order. 
     */
    public void run() {
        int data;
        try {
	    int i;
	    for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
            //while (true) {
		Enumeration relations = _output.enumRelations();
                while (relations.hasMoreElements()) {
                    PNFifoRelation nextQueue = (PNFifoRelation)relations.nextElement();
                    data = readFrom(_input);
                    writeTo(_output, data, nextQueue);
                    System.out.println(this.getName()+" writes "+data+
                            " to "+ nextQueue.getName());
                }
            }
        } catch (TerminationException e) {
	    System.out.println("Terminating "+ this.getName());
            return;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* The input port */
    private PNInPort _input;
    /* The output port */
    private PNOutPort _output;
}
