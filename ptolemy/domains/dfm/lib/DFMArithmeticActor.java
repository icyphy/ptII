/* The DFM actor that does arithmetic operation on double tokens.

 Copyright (c) 1998 The Regents of the University of California.
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
//// DFMArithmeticActor
/** 
 The arithmetic actor in DFM domain.  It is a two input ports and one 
 output port.  The operation is defined by the string value passed
 in at constructor function.
   
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.domains.dfm.kernel.DFMActor
 
*/

public class DFMArithmeticActor extends DFMActor {

    /** Constructor
     * @param container This is the CompositeActor containing this actor.
     * @param name This is the name of this actor.
     * @param operation The operation of this arithmethic.
     * @exception  
     */	
    public DFMArithmeticActor(CompositeActor container, String name, String operation) 
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _operation = new String(operation);
        // constructor one input port, one output port.
        _input1 = new IOPort(this, "input1", true, false);
        _input2 = new IOPort(this, "input2", true, false);
        _output = new IOPort(this, "output", false, true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Perform the arithmetic that according to the given operation name.
     */	
    protected void  _performProcess() {
        // assume the input tokens are double token
      
        double data1 = ((Double) _getData("input1")).doubleValue(); 
        double data2 = ((Double) _getData("input2")).doubleValue(); 
        double result = 0.0;
        if (_operation.equals("ADD")){
            result = data1+data2;
System.out.println("doing add");
        } else if (_operation.equals("SUBTRACT")){
            result = data1-data2;
        } else if (_operation.equals("MULTIPLY")){
System.out.println("doing mul");
            result = data1*data2;
        } else if (_operation.equals("DIVIDE")){
            result = data1/data2;
        } else if (_operation.equals("POWER")){
            result = Math.pow(data1,data2);
        } else {
            // throw an exception here?
        }
        DFMDoubleToken outtoken = new DFMDoubleToken("New", result);
        _outputTokens.put("output", outtoken); 
System.out.println("result "+result);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _input1;
    private IOPort _input2;
    private IOPort _output;
    private String _operation;
}
