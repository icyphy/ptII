/* Reads a token from a stream and writes a token to a stream

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
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// PNRedirect
/** 

@author Mudit Goel
@version $Id$
*/
public class PNRedirect extends AtomicActor{

    /** Constructor Adds ports to the star
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made, or a star with an 
     *  identical name already exists.
     * @exception IllegalActionException a port with name null is being added
     *  to the star
     */
    public PNRedirect(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
	//_input.makeMultiport(false);
        _output = new IOPort(this, "output", false, true);
	//_output.makeMultiport(false);
    }
    
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads a token from it's input stream and writes it to the output
     */
    public void fire() throws IllegalActionException {
        int i;
        Token data;
        //try {
	//writeTo(_output, _initValue);
	System.out.println(this.getName()+" writes *before* "+_initValue.stringValue()+" to "+_output.getName());
	_output.broadcast(_initValue);
	System.out.println(this.getName()+" writes "+_initValue.stringValue()+" to "+_output.getName());
	while (true) {
	    //for(i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	    //Enumeration relations = _input.linkedRelations();
	    //while (relations.hasMoreElements()) {
	    //IORelation relation = (IORelation)relations.nextElement();
	    //data = readFrom(_input, relation);
	    data = _input.get(0);
	    //writeTo(_output, data[0]);
	    _output.broadcast(data);
	    System.out.println(this.getName()+" writes "+data.stringValue()+" to "+_output.getName());
	    //}
	}
	//((PNDirector)getDirector()).processStopped();
	//} catch (NoSuchItemException e) {
	//System.out.println("Terminating "+this.getName());
	//return;
        //}
    }

    public void setParam(String name, String valueString) 
	    throws IllegalActionException {
	if (name.equals("Initial Value")) {
	    _initValue = new IntToken(valueString);
	} else {
	    throw new IllegalActionException(this,valueString+" param does not exist");
	}
    }

    /** Initializes a token to be written to the output 
     * @param initValue is the initial token that the star puts in the stream
     */
//     public void setInitState(int initvalue) {
//         _initValue = new IntToken(initvalue);
//     }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* This is the initial value that the star puts in the stream */
    private IntToken _initValue;
    /* Input port */
    private IOPort _input;
    /* Output port */
    private IOPort _output;
}

