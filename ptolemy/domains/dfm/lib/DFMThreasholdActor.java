/* A Threashold level actor in DFM.

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
//// DFMThreasholdActor
/** 
 A threashold actor that allow token to pass through when the token value
 is greater than the threashold.  If the token value is less than threashold
 then no-op token is produced.   Expression package should be used here also.
  
@author  William Wu  wbwu@eecs.berkeley.edu
@version $id$ 
*/

public class DFMThreasholdActor extends DFMActor {
    /** Constructor
     */	
    public DFMThreasholdActor(CompositeActor container, String name)
                throws NameDuplicationException, IllegalActionException {
                 super(container, name);
         _input = new IOPort(this, "input", true, false);
         _output = new IOPort(this, "output", false, true);
 

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

    /** Send output data depend on input data and threashold level.
     * Expression package should be used.
     */
    protected void _performProcess() {
        double indata = ((Double) _getData("input")).doubleValue();
        if (indata < _threashold){

            DFMDoubleToken outtoken = new DFMDoubleToken("New", indata);
            _outputTokens.put("output", outtoken);
            _savedoutputTokens.put("output", outtoken);
System.out.println("under threashold, pass the value: "+indata);
        } else{
            DFMToken outtoken = new DFMToken("No-Op");
            _outputTokens.put("output", outtoken);
            _savedoutputTokens.put("output", outtoken);
System.out.println("over threashold, not pass, no-op passed ");
        }
    }


    protected void _notPerformProcess(){
 
        if (_noop){
            super._notPerformProcess();
            return;
        }
 
        DFMToken out = (DFMToken) _savedoutputTokens.get("output");
        if (out.getTag().equals("New")){
            DFMToken outtoken = new DFMToken("PreviousResultValid");
            _outputTokens.put("output", outtoken);
            _savedoutputTokens.put("output", outtoken);
        } else {
            DFMToken outtoken = new DFMToken(out.getTag());
            _outputTokens.put("output", outtoken);
            _savedoutputTokens.put("output", outtoken);
        }   
    }
 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _threashold = 0.0;
    private IOPort _input;
    private IOPort _output;
}
