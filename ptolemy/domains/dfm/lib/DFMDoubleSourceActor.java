/* A double source actor fires a double token every time.

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
//// DFMDoubleSourceActor
/** 
 A double source actor that fires a double token.
@author William Wu 
@version %W%	%G%
@see classname
@see full-classname
*/
public class DFMDoubleSourceActor extends DFMActor {
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public DFMDoubleSourceActor(CompositeActor container, String name)
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _output = new IOPort(this, "output", false, true);  
        _setSource();
       // _setParamChanged();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Change the value that this actor firee. 
     * Check the DFMDirector's flag to see if it is ok to set
     * the parameter.
     * @param name name of the paramter, there is only here "Value" 
     * @param arg the value to be fired with. 
     */	
    public boolean changeParameter(String name, Object arg) {
        
        DFMDirector dir = (DFMDirector) getDirector();
        if (!dir.isWaitForNextIteration()) return false; 

        if (name.equals("Value")){
            _value = (new Double((String) arg)).doubleValue();
            _setParamChanged(true);
            System.out.println("changed output value");
            dir.dfmResume();
            return true;
        } else {
            // throw new IllegalArgumentException("");
        }
 
        return false;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Set the output token to the preset value. 
     */	
    protected void _performProcess() {
        DFMDoubleToken outtoken = new DFMDoubleToken("New", _value);
        _outputTokens.put("output", outtoken); 
System.out.println("firing value: "+_value);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _value = 1.0;
    private IOPort _output;
}
