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

package pt.kernel;

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

    public PNRedirect(String name) {
        super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    public void initialize(int initValue)
            throws NameDuplicationException, GraphException {
        _initValue = initValue;
        _input = addInPort(this, "input");
        _output = addOutPort(this, "output");
    }

    /** Description
     */	
    public void initialize(PNExecutive myExecutive, int initValue)
            throws NameDuplicationException, GraphException {
        _myExecutive = myExecutive;
        initialize(initValue);
    }

    public void run() {
        int i;
        int data;
        try {
            writeTo(_output, _initValue);
            for(i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                data = readFrom(_input);
                writeTo(_output, data);
            }
        } catch (TerminationException e) {
            return;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private int _initValue;
    private PNInPort _input;
    private PNOutPort _output;
    

}
