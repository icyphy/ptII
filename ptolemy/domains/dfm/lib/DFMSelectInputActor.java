/* This DFM control actor select one of its input and send it to output.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.dfm.lib;

import ptolemy.domains.dfm.data.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// DFMSelectInputActor
/** 
 This control actor selects one of its input and send to it output.  
 The input will be selected for output if it's token has "New" tag,
 and no other input has the "New" tag; when more than one input has
 "New" tag, then the more primary one (input1) will be selected.  
  
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$
*/

public class DFMSelectInputActor extends DFMActor {
    /** Constructor
     * construct a two input-one output selector.
     */	
    public DFMSelectInputActor(CompositeActor container, String name)
                throws NameDuplicationException, IllegalActionException {
        super(container, name);
        // constructor one input port, one output port.
        _input1 = new IOPort(this, "input1", true, false);
        _input2 = new IOPort(this, "input2", true, false);
        _output = new IOPort(this, "output", false, true);

    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Select the one input and send to output. 
     */	
    protected void _performProcess() {
        DFMToken in1 = (DFMToken) _inputTokens.get("input1"); 
        DFMToken in2 = (DFMToken) _inputTokens.get("input2");
        DFMToken outtoken;
        if (in1.getTag().equals("New")) {
System.out.println("selecting first input");
            outtoken = in1;
        } else if (in2.getTag().equals("New")){ 
System.out.println("selecting second input");
            outtoken = in2;
        } else {
            // throw an exception here ?
            outtoken = new DFMToken("No-Op");
        }
        _outputTokens.put("output", outtoken); 
        _savedoutputTokens.put("output", outtoken); 
    }

    private IOPort _input1;
    private IOPort _input2;
    private IOPort _output;
}
