/* Multiply the input with a gain.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
//// CTGain
/**
The output of the actor is the input multiplies a gain. This actor
should have the same function in all domains. Single input single output.
(IO type: double). This actor has one double parameter, the gain.
@author Jie Liu
@version $Id$
*/
public class CTGain extends CTActor {
    /** Construct the gain, default gain is 1.0. Single input, single
     *  output.
     * @see ptolemy.domains.ct.kernel.CTActor
     * @param container The CTSubSystem this star belongs to
     * @param name The name
     * @exception NameDuplicationException another star already had this name
     * @exception IllegalActionException illustrates internal problems
     */
    public CTGain(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(false);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setMultiport(false);
        output.setDeclaredType(DoubleToken.class);
        _gain = (double)1.0;
        _paramGain = new CTParameter(this, "Gain", new DoubleToken(_gain));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Multiply the input token with the gain, and output the product.
     *
     *  @exception IllegalActionException If there's no input token
     *        when needed.
     */
    public void fire() throws IllegalActionException{
        double in = ((DoubleToken)input.get(0)).doubleValue();
        output.broadcast(new DoubleToken(_gain*in));
    }


    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Never thrown.*
     */
    public void updateParameters() throws IllegalActionException {
        _gain = ((DoubleToken)_paramGain.getToken()).doubleValue();
    }

    /** The single input port.
     */
    public TypedIOPort input;

    /** The single output port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private CTParameter _paramGain;
    private double _gain;
}
