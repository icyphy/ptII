/* This constructs a sequence of prime numbers based on Sieve of Erathsenes

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
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// PNSieve
/** 
@author Mudit Goel
@version $Id$
*/
public class PNSieve extends PNStar {
    /** Constructor
     */	
    public PNSieve() {
	super();
    }
    
    /** Constructor
     */
    public PNSieve(Workspace workspace) {
        super(workspace);
    }
    
    /** Constructor
     */
    public PNSieve(CompositeEntity container, String name)
            throws NameDuplicationException {
        super(container, name);
    }
    

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Initializes the Star. Should be called before execution
     * @param myExecutive is the executive responsible for execution
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     * @exception IllegalActionException is thrown if a port with a null
     *  name is passed
     */	
    public void initialize(int prime)
            throws NameDuplicationException, IllegalActionException {
        _input = newInPort(this, "input");
        _output = null;
        _prime = prime;
        super.initialize(this);
        System.out.println("Next Prime is "+ _prime);
    }
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    public void run() {
        IntToken data;
        try {
            while(true) {
                data = (IntToken)readFrom(_input);
                if (data.intValue()%_prime != 0) {
                    /* is it the next prime? */
                    if (_output == null) {
                        /* yes - make the sieve for it */
                        PNSieve newSieve = new PNSieve(getContainer(), data.intValue() + "_sieve");
                        _output = new PNOutPort(this, "output");
                        IORelation relation = new IORelation(getContainer(),
                                data.intValue()+"_queue");
                        _output.link(relation);
                        PNPort inport = newSieve.getPort("_input");
                        inport.link(relation);
                        newSieve.initialize(data.intValue());
                        Thread temp = new Thread(_processGroup, star);
                        temp.start();
                    } 
                    else {
                        writeTo(_output, data);
                    }
                }
            }
        } catch (NoSuchElementException e) {
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
    private int _prime;
}


