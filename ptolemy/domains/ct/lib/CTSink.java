/* A black hole actor for performance testing.

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
//// CTSink
/** 
Consume the input token and do nothing.
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTSink extends CTActor{
    /** Construct the sink. 
     * @see ptolemy.domains.ct.kernel.CTActor
     * @param container The CTSubSystem this star belongs to
     * @param name The name
     * @exception NameDuplicationException another star already had this name
     * @exception IllegalActionException illustrates internal problems
     */
    public CTSink(TypedCompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.makeInput(true);
        input.makeOutput(false);
        input.makeMultiport(true);
        input.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**Print out the input token and the current time each in a line.
     * The format of printout is :
     * CTTime:......
     * CTData:......
     *  @exception IllegalActionException If there's no director or 
     *        no input token when needed.
     */	
    
    public void fire() throws  IllegalActionException{
       CTDirector dir = (CTDirector) getDirector();
       if (dir == null) {
           throw new IllegalActionException(this, 
                   "No director avaliable");
       }
       for(int i = 0; i < input.getWidth(); i++) {
           ((DoubleToken)input.get(i)).doubleValue();
       }
    }

    /** The single input port.
     */
    public TypedIOPort input;

}
