/* This star discards whatever it receives at the input

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

package pt.domains.pn.actors;
import pt.domains.pn.kernel.*;
import pt.kernel.*;
import pt.data.*;
import pt.actors.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNSink
/** 

@author Mudit Goel
@version $Id$
*/
public class PNSink extends PNActor{
    
    /** Constructor Adds ports to the star
     * @param initValue is the initial token that the star puts in the stream
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made
     */
    public PNSink(CompositeActor container, String name)
            throws NameDuplicationException {
        super(container, name);
        _input = newInPort(this, "input");
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Writes successive integers to the output
     */
    public void run() {
        int i;
        Token[] data;
        try {
            for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                //Enumeration outports = _input.deepConnectedOutputPorts();
                //while (outports.hasMoreElements()) {
                //PNOutPort outport = (PNOutPort)outports.nextElement();
                Enumeration relations = _input.linkedRelations();
                while (relations.hasMoreElements()) {
                    IORelation relation = (IORelation)relations.nextElement();
                    data = readFrom(_input, relation);
                }
            }
            // System.out.println("Terminating at al "+this.getName());
            ((PNDirector)getDirector()).processStopped();
        } catch (NoSuchItemException e) {
	    // System.out.println("Terminating "+this.getName());
            return;
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    /* Input port */
    private PNInPort _input;
}

