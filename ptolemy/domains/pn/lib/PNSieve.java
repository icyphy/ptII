/* This constructs a sequence of prime numbers based on Sieve of Erathsenes

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
import pt.actors.*;
import pt.data.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// PNSieve
/** 
@author Mudit Goel
@version $Id$
*/
public class PNSieve extends PNActor {
    
    /** Constructor  Adds port   
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     */
    public PNSieve(CompositeActor container, String name)
            throws NameDuplicationException {
        super(container, name);
        _input = newInPort(this, "input");
        _output = null;
    }
    

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Initializes the Star. Should be called before execution
     * @param prime is the prime for this sieve
     */	
    public void setInitState(int prime) {
        _prime = prime;
        System.out.println("Next Prime is "+ _prime);
    }
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    public void run() {
        IntToken data;
        try {
            int i;
            for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                Enumeration outports = _input.deepConnectedOutputPorts();
                while (outports.hasMoreElements()) {
                    PNOutPort outport = (PNOutPort)outports.nextElement();
                    data = (IntToken)readFrom(_input, outport);
                    if (data.intValue()%_prime != 0) {
                        // is it the next prime? 
                        if (_output == null) {
                            // yes - make the sieve for it 
                            PNSieve newSieve = new PNSieve((CompositeActor)
                                    getContainer(), data.intValue() + "_sieve");
                            newSieve.setInitState(data.intValue());
                            _output = new PNOutPort(this, "output");
                            IORelation relation = new IORelation((CompositeEntity)
                                    getContainer(), data.intValue()+"_queue");
                            _output.link(relation);
                            PNPort inport = (PNPort)newSieve.getPort("input");
                            inport.link(relation);
                            inport.getQueue(_output).setCapacity(1);
			    //THIS IS CURRENTLY VERY IMPORTANT
                            ((PNDirector)getDirector()).setMutate(true);
                        } 
                        else {
                            writeTo(_output, data);
                        }
                    }
                }
            }
            ((PNDirector)getDirector()).processStopped();
        } catch (NoSuchElementException e) {
            System.out.println("Terminating "+ this.getName());
            return;
        } catch (NameDuplicationException e) {
            System.err.println("Exception: " + e.toString());
            //This should never be thrown
        } catch (IllegalActionException e) {
            //This should never be thrown
            System.err.println("Exception: " + e.toString());
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


