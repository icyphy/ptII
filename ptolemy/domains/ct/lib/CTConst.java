/* An actor that outputs a continuous constant.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CTConst
/**
A constant source in the CT domain. The actor output a token at the
fire() phase. The value of the token equals the parameter "value"
set in the setParam method. If the "value" is not set, the default
value is 0.
@author Jie Liu
@version $Id$
@see pt.domains.ct.kernel.CTActor
*/
public class CTConst extends CTActor {
    /** Construct the CTConst star. This actor is not a dynamic actor.
     *  The default output value is 0.
     * @see CTActor#CTActor()
     * @param container The CTSubSystem this star belongs to
     * @param name The name.
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public CTConst(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);
        _value = (double)0.0;
        _paramValue = new CTParameter(this, "Value", new DoubleToken(_value));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a doubleToken of the constant value.
     *
     *  @exception IllegalActionException Never thrown.
     */
    public void fire() throws IllegalActionException{
        output.broadcast(new DoubleToken(_value));
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Never thrown.
     */
    public void updateParameters() throws IllegalActionException{
        _value = ((DoubleToken)_paramValue.getToken()).doubleValue();
    }

    /** The single output port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private CTParameter _paramValue;
    private double _value;
}
