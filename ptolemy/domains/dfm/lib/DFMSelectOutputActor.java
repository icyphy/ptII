/* A control actor that select which output to use sending the
   output token to.

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
//// DFMSelectOutputActor
/** 
 A type of control actor in DFM that perform a select on its output channel
 for sending the input data.  The select is currently base on numerical
 threashold.  In the future this should take advantage of the expression
 package. 
@author  William Wu 
@version $id$ 
*/

public class DFMSelectOutputActor extends DFMActor {
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public DFMSelectOutputActor(CompositeActor container, String name)
                throws NameDuplicationException, IllegalActionException {
 
         super(container, name);
         _input = new IOPort(this, "input", true, false);
         _output1 = new IOPort(this, "output1", false, true);
         _output2 = new IOPort(this, "output2", false, true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Change the value that this actor used to comare input with. 
     * @param name name of the paramter, there is only here "ThreasholdValue" 
     * @param arg the value to be fired with. 
     */ 
    public boolean changeParameter(String name, Object arg) {
        DFMDirector dir = (DFMDirector) getDirector();
        if (!dir.isWaitForNextIteration()) return false; 

        if (name.equals("ThreasholdValue")){
            _threashold = (new Double((String) arg)).doubleValue();
            _setParamChanged(true);
            dir.dfmResume();
            return true;
        }
        return false;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Select the output channel depends on input data and threashold level.
     * Expression package should be used. 
     */	
    protected void _performProcess() {
        double indata = ((Double) _getData("input")).doubleValue();
        if (indata < _threashold){
            DFMDoubleToken outtoken1 = new DFMDoubleToken("New", indata);
            DFMToken outtoken2 = new DFMToken("No-Op");
            _outputTokens.put("output1", outtoken1);
            _outputTokens.put("output2", outtoken2);
            _savedoutputTokens.put("output1", outtoken1);
            _savedoutputTokens.put("output2", outtoken2);
        } else{
            DFMDoubleToken outtoken1 = new DFMDoubleToken("New", indata);
            DFMToken outtoken2 = new DFMToken("No-Op");
            _outputTokens.put("output1", outtoken2);
            _outputTokens.put("output2", outtoken1);
            _savedoutputTokens.put("output1", outtoken2);
            _savedoutputTokens.put("output2", outtoken1);
        }
    }
    
    protected void _notPerformProcess(){
      
        if (_noop){ 
            super._notPerformProcess();
            return;
        }

        DFMToken out1 = (DFMToken) _savedoutputTokens.get("output1"); 
        DFMToken out2 = (DFMToken) _savedoutputTokens.get("output2"); 
        if (out1.getTag().equals("New")){
            DFMToken outtoken = new DFMToken("PreviousResultValid");
            _outputTokens.put("output1", outtoken);
            _savedoutputTokens.put("output1", outtoken);
        } else {
            DFMToken outtoken = new DFMToken(out1.getTag());
            _outputTokens.put("output1", outtoken);
            _savedoutputTokens.put("output1", outtoken);
        }     
        if (out2.getTag().equals("New")){
            DFMToken outtoken = new DFMToken("PreviousResultValid");
            _outputTokens.put("output2", outtoken);
            _savedoutputTokens.put("output2", outtoken);
        } else {
            DFMToken outtoken = new DFMToken(out2.getTag());
            _outputTokens.put("output1", outtoken);
            _savedoutputTokens.put("output1", outtoken);
        }     
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _threashold = 0.0;
    private IOPort _input;
    private IOPort _output1;
    private IOPort _output2;
}
