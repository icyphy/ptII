/* A special actor that sit on the feedback loop in a DFM graph.

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

import ptolemy.kernel.util.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// DFMFeedbackActor
/** 
 This is an actor that is on the feedback loop inthe DFM graph.  It take a input
 and produce an output.  This output is for the next iteration.  Thus a 
 token is produced for the next iteration will cause the system to refire its 
 source actors when the new token has a "New" tag.  There is a init value saved 
 in this actor, that initial value is fired during initialization of the system.
 <p>
 The tag on the init token is important.  The designer recommends use "PreviousResult
 Valid" tag, since that preserves the previous iteration before the stop condition
 is met, kinda like a roll_back.  Thus the new parameter change will affect that
 iteration only.  Also do use SelectInput with the FeedBack actor for . 
 <p>
@author William Wu  
@version $id$
*/

public class DFMFeedbackActor extends DFMActor {
    /** Constructor
     * 
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public DFMFeedbackActor(CompositeActor container, String name, DFMToken initValue)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _feedbackActor = true;
        _initValue = initValue;
        // constructor one input port, one output port.
        _input = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    public void fire(){
        sendInitToken();
        super.fire();
    }
 
    /** Send out the initial 
     */	
    public void sendInitToken() {
System.out.println("DFMFeedback ready to send init token");
        try{
             _output.broadcast(_initValue);
System.out.println("DFMFeedback sending init token "+_initValue.getTag());
Object tmp = _portDrawer.get("output");
if (tmp != null){
    DFMPortDrawer portdrawer = (DFMPortDrawer) tmp;
    Object value = _initValue.getData();
    String val = new String("");
    if (value != null){ 
        val = value.toString();
    } 
System.out.println("redrawing init token");
    portdrawer.draw(_initValue.getTag(), val);
}

        } catch (IllegalActionException e) {}
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Simplly get the input data into the output token bin.  Also notify
     *  director about the firing of these token for next iteration. 
     */	
    protected void _performProcess() {
        DFMToken token = (DFMToken) _inputTokens.get("input");
        _outputTokens.put("output", token); 
System.out.println("produce feedback token, tag: "+token.getTag());
        DFMDirector  dir = (DFMDirector) getDirector();
        dir.nextIterTokenProduced(token.getTag());
 
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The init value produced by this feedback actor. */
    protected DFMToken _initValue;

    private IOPort _input;
    private IOPort _output;
}
