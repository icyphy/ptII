/* Alternates the input into its different outputs

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

package pt.domains.pn.stars;
import pt.domains.pn.kernel.*;
import pt.kernel.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;

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
    public PNAlternate(Workspace workspace) {
        super(workspace);
    }
    
    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNAlternate(CompositeEntity container, String name)
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
    public void run() {
        IntToken data;
        try {
	    int i;
	    for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                data = (IntToken)readFrom(_input);
                writeTo(_output0, data);
                //try {
                //System.out.println(this.getName()+" writes "+
                //((IntToken)data).intValue()+" to "+
                //_output1.getFullName());
                //} catch (InvalidStateException e) {
                //System.err.println("Exception: " + e.toString());
                //}
                data = (IntToken)readFrom(_input);
                writeTo(_output1, data);
                //try {
                //System.out.println(this.getName()+" writes "+
                //((IntToken)data).intValue()+" to "+
                //_output2.getFullName());
                //} catch (InvalidStateException e) {
                //System.err.println("Exception: " + e.toString());
                //}
            }                
        } catch (NoSuchElementException e) {
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
