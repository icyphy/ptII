/* This interleaves elements from it's different streams into one stream

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

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNInterleave
/** 
Merges it's input streams into one output stream by alternating/circulating 
between it's inputs and directing them to the output.

@author Mudit Goel
@version $Id$
*/
public class PNInterleave extends AtomicActor{

    /** Constructor Adds ports to the star
     * @param myExecutive is the executive responsible for the simulation
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made or a star with an 
     *  identical name already exists.
     */ 
    public PNInterleave(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _input.makeMultiport(true);
        _output = new IOPort(this, "output", false, true);
	_output.makeMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This reads tokens from each of it's inputs in a circular fashion and
     *  redirects them each to the output 
     */	
    //FIXME: THis code shoudl change when the new kernel is in place
    public void fire() throws IllegalActionException {
        Token data;
        //int i;
        //setCycles(((PNCompositeActor)getContainer()).getCycles());
        //try {
	//for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	while (true) {
	    int width = _input.getWidth();
	    for (int i=0; i<width; i++) {
		//Enumeration relations = _input.linkedRelations();
		//while (relations.hasMoreElements()) {
		//IORelation relation = (IORelation)relations.nextElement();
		//data = readFrom(_input, relation);
		System.out.println("Trying to get token from");
		data = _input.get(i);
		System.out.println("Read "+data.stringValue()+" from "+_input.getName());
		_output.broadcast(data);
		System.out.println("Interleaving broadcasting "+data.stringValue());
		// for (int j=0; j<data.length; j++) {
		//                         writeTo(_output, data[j]);
		//                         System.out.println(this.getName()+" writes "+((IntToken)data[j]).intValue()+" to "+_output.getName());
		//                     }
	    }
	}
	//((PNDirector)getDirector()).processStopped();
	//} catch(NoSuchItemException e) {
	//System.out.println("Terminating "+ this.getName());
	//return;
	//}
	//System.out.println("Terminating "+ this.getName());
	//return;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Input ports 
    private IOPort _input;
    // Output port 
    private IOPort _output;

}
