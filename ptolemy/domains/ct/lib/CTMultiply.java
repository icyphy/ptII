/* Multi-input sigle output multiplexer.

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
//// CTMultiply
/** 
Output the multiplication of all the inputs. Multi-input single output.
(IO type: double). This actor has no parameter.
@author Jie Liu
@version  $Id$
*/
public class CTMultiply extends CTActor{
    /** Construct the multiplexer. Multi-input, single output, Nondynamic.
     * @see pt.domains.ct.kernel.CTActor
     * @param container The CTSubSystem this star belongs to
     * @param name The name
     * @exception NameDuplicationException another star already had this name
     * @exception IllegalActionException illustrates internal problems
     */	
    public CTMultiply(TypedCompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.makeInput(true);
        input.makeOutput(false);
        input.makeMultiport(true);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.makeInput(false);
        output.makeOutput(true);
        output.makeMultiport(false);
        output.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   
    /** Multiply the input tokens, and output the product
     *  @exception IllegalActionException If there's no enough input
     *       tokens.
     */
    public void fire() throws IllegalActionException{
        double product = 1.0;
        for(int i = 0; i < input.getWidth(); i++) {
            product *= ((DoubleToken)input.get(i)).doubleValue();
        }
        output.broadcast(new DoubleToken(product));
    }
    
    /** The multi-input port.
     */
    public TypedIOPort input;

    /** The singal output port.
     */
    public TypedIOPort output;
}

