/* This generates a stream of integer beginning with the seed

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
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// PNRamp
/** 

@author Mudit Goel
@version $Id$
*/
public class PNRamp extends PNStar{
    /** Constructor
     */	
    public PNRamp() {
        super();
    }

    /** Constructor 
     */
    public PNRamp(Workspace workspace) {
        super(workspace);
    }

    /** Constructor
     */
    public PNRamp(CompositeEntity container, String name)
             throws NameDuplicationException {
        super(container, name);
    }
 
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Initializes and adds ports to the star
     * @param initValue is the initial token that the star puts in the stream
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made
     * @exception IllegalActionException a port with name null is being added
     *  to the star
     */
    public void initialize(int seed)
            throws NameDuplicationException, IllegalActionException {
        _seed = seed;
        _output = newOutPort(this, "output");
        super.initialize(this);
    }
    
    /** Writes successive integers to the output
     */
    public void run() {
        int i;
        IntToken data;
        try {
            for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                data = new IntToken(_seed);
                writeTo(_output, data);
                _seed++;
            }
            System.out.println("Terminating et al "+this.getName());
        } catch (NoSuchElementException e) {
	    System.out.println("Terminating "+this.getName());
            return;
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* This is the initial value that the star puts in the stream */
    private int _seed;
    /* Output port */
    private PNOutPort _output;
}
