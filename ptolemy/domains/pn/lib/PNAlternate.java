/* Alternates the input into its different outputs

 Copyright (c) 1997- The Regents of the University of California.
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

package pt.domains.pn.stars;
import pt.domains.pn.kernel.*;
import pt.kernel.*;
import pt.data.*;
import pt.actors.*;
import java.util.Enumeration;
//import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// PNAlternate
/** 
@author Mudit Goel
@version $Id$
*/
public class PNAlternate extends PNActor {
    
    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNAlternate(CompositeActor container, String name)
            throws NameDuplicationException {
        super(container, name);
        _input = newInPort(this, "input");
        _output0 = newOutPort(this, "output0");
        _output1 = newOutPort(this, "output1");
    }
    

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    //FIXME: CUrrently this is a BIIIIIG hack and should be changed ASA 
    // the new kernel strategy is implemented
    public void run() {
        Token[] data;
        try {
	    int i;
	    for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                Enumeration relations = _input.linkedRelations();
                while (relations.hasMoreElements()) {
                    IORelation relation = (IORelation)relations.nextElement();
                    data = readFrom(_input, relation);
		    for (int j =0; j<data.length; j++) {
		      writeTo(_output0, data[j]);
		    }
                }
                relations = _input.linkedRelations();
                while (relations.hasMoreElements()) {
                    IORelation relation = (IORelation)relations.nextElement();
                    data = readFrom(_input, relation);
                    writeTo(_output1, data[0]);
                }
            }                
            ((PNDirector)getDirector()).processStopped();
        } catch (NoSuchItemException e) {
	    System.out.println("Terminating "+ this.getName());
            return;
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    // The input port 
    private PNInPort _input;
    // The output port 
    private PNOutPort _output0;
    private PNOutPort _output1;
}
