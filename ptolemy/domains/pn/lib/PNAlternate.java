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

package pt.kernel;

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

    /** Description
     */	
    public void initialize(PNExecutive myExecutive)
            throws NameDuplicationException, GraphException {
        _input = addInPort(this, "input");
        _output1 = addOutPort(this, "output1");
        _output2 = addOutPort(this, "output2");
        _myExecutive = myExecutive;
    }
    
    public void run() {
        int data;
        try {
            while (true) {
                data =  readFrom(_input);
                writeTo(_output1, data);
                data =  readFrom(_input);
                writeTo(_output2, data);
            }
        } catch (TerminationException e) {
            return;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private PNInPort _input;
    private PNOutPort _output1;
    private PNOutPort _output2;
}
