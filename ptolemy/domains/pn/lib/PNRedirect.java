/* Reads a token from a stream and writes a token to a stream

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
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// PNRedirect
/** 

@author Mudit Goel
@version $Id$
*/
public class PNRedirect extends PNStar{
    /** Constructor
     */	
    public PNRedirect() {
        super();
    }

    /** Constructor 
     */
    public PNRedirect(Workspace workspace) {
        super(workspace);
    }

    /** Constructor Adds ports to the star
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made, or a star with an 
     *  identical name already exists.
     * @exception IllegalActionException a port with name null is being added
     *  to the star
     */
    public PNRedirect(CompositeEntity container, String name)
             throws NameDuplicationException {
        super(container, name);
        _input = newInPort(this, "input");
        _output = newOutPort(this, "output");
    }
    
 
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Reads a token from it's input stream and writes it to the output
     */
    public void run() {
        int i;
        IntToken data;
        try {
            writeTo(_output, _initValue);
            //System.out.println(this.getName()+" writes "+_initValue.intValue()+" to "+_output.getName());
            for(i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                data = (IntToken)readFrom(_input);
                writeTo(_output, data);
                //System.out.println(this.getName()+" writes "+data.intValue()+" to "+_output.getName());
            }
        } catch (NoSuchElementException e) {
	    System.out.println("Terminating "+this.getName());
            return;
        }
    }

    /** Initializes a token to be written to the output 
     * @param initValue is the initial token that the star puts in the stream
     */
     public void setInitState(int initvalue) {
        _initValue = new IntToken(initvalue);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* This is the initial value that the star puts in the stream */
    private IntToken _initValue;
    /* Input port */
    private PNInPort _input;
    /* Output port */
    private PNOutPort _output;
}
