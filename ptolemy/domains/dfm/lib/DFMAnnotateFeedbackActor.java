/* A special actor that sit on the annotation feedback loop in a DFM graph.

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
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// DFMAnnotateFeedbackActor
/** 
 This is an actor that is on the annotation feedback loop inthe DFM graph.  It take a input
 and produce an output.  This output is for the annotation within the same
 iteration.  Thus it will also cause the system to refire its source actors, but if
 no other "New" token is produced then the system will just have a annotation run.  
 The output token is has the tag "Annotate" on it.  There is a init value saved 
 in this actor, that initial value is fired during initialization of the system.
 <p>
@author William Wu  
@version $id$
*/

public class DFMAnnotateFeedbackActor extends DFMFeedbackActor {
    /** Constructor
     * 
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public DFMAnnotateFeedbackActor(CompositeActor container, String name, DFMToken initValue)
            throws NameDuplicationException, IllegalActionException {
        super(container, name, initValue);
        _savedInitValue = _initValue;
        // constructor one input port, one output port.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    public void init(){
        _initValue = _savedInitValue;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Simplly get the input data into the output token bin.  Also notify
     *  director about the firing of these token for next iteration. Change 
     */	
    protected void _performProcess() {
        DFMToken token = (DFMToken) _inputTokens.get("input");
        token.setTag("Annotate");
        _outputTokens.put("output", token); 
        _initValue = new DFMToken("PreviousResultValid");
        DFMDirector  dir = (DFMDirector) getDirector();
        dir.nextIterTokenProduced(token.getTag());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The init value produced by this feedback actor. */
    protected DFMToken _savedInitValue;
}
