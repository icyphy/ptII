/* CT Dummy Step Size Control Actor.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.domains.ct.kernel.test;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;


//////////////////////////////////////////////////////////////////////////
//// CTDummySSControlActor
/**
Dummy actor for step size control. SISO.
@author  Jie Liu
@version $Id$
*/
public class CTDummySSControlActor extends CTActor
    implements CTStepSizeControlActor{
    /** Constructor
     */
    public CTDummySSControlActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setTypeEquals(BaseType.DOUBLE);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return true always.
     */
    public boolean isThisStepSuccessful() {
        return true;
    }

    /** Return java.lang.Double.MAX_VALUE.
     *  @return java.lang.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return 0.0.
     *  @return 0.0.
     */
    public double refinedStepSize() {
        return 0.0;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** @serial The single-input port. */
    public TypedIOPort input;

    /** @serial The signal output port. */
    public TypedIOPort output;
}
