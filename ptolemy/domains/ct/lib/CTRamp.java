/* A CT actor that outputs a ramp.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
@ProposedCodeRate red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CTRamp
/** 
A ramp source
@author Jie Liu
@version $Id$
@see CTActor
@see ptolemy.domains.ct.kernel.CTActor
*/
public class CTRamp extends CTActor {
    /** Construct the CTRamp actor, default slop 1, defalt initial value 0
     * @param container CTSubSystem this star belongs to
     * @param name The name
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */	
    public CTRamp(TypedCompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);
        _initValue = (double)0.0;
        _paramInitValue = new CTParameter(this, "InitialValue",
                new DoubleToken(_initValue));
        _slope = 1.0;
        _paramSlope = new CTParameter(this, "Slope", 
                new DoubleToken(_slope));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a token according to the shape of the ramp and the
     *  the current time.
     *
     *  @exception IllegalActionException If there's no director or no
     *        input token when needed.
     */
    public void fire() throws IllegalActionException{
        CTDirector dir = (CTDirector) getDirector();
        if(dir == null) {
            throw new IllegalActionException( this, " Has no director.");
        }
        double now = dir.getCurrentTime();
        double t0 = dir.getStartTime();
        double out = (double)_slope * ((double)now-t0) + _initValue ;
        output.broadcast(new DoubleToken(out));
    }

    /** Update parameters
     *  @exception IllegalActionException Never thrown.
     */
    public void updateParameters() throws IllegalActionException {
        _initValue = ((DoubleToken)_paramInitValue.getToken()).doubleValue();
        _slope = ((DoubleToken)_paramSlope.getToken()).doubleValue();
    }

    /** The single output port
     */
    public TypedIOPort output;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private double _initValue;
    private CTParameter _paramInitValue;
    private double _slope;
    private CTParameter _paramSlope;
}
