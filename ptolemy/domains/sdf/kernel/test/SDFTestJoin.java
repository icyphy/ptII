/*
@Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY

@ProposedRating Red
@AcceptedRating Red
*/
package ptolemy.domains.sdf.kernel.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFTestJoin
/**
 * A deterministic merge of two token streams.
 * @author Stephen Neuendorffer
 * @version $Id$
*/

public class SDFTestJoin extends SDFAtomicActor {

    public SDFTestJoin(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try{
            input1 = (SDFIOPort)newPort("input1");
            input1.setInput(true);
            input1.setTokenConsumptionRate(1);
            input1.setTypeEquals(BaseType.GENERAL);

            input2 = (SDFIOPort)newPort("input2");
            input2.setInput(true);
            input2.setTokenConsumptionRate(1);
            input2.setTypeEquals(BaseType.GENERAL);

            output = (SDFIOPort)newPort("output");
            output.setOutput(true);
            output.setTokenProductionRate(2);
            output.setTypeEquals(BaseType.GENERAL);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFTestJoin: constructor error");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public SDFIOPort input1;
    public SDFIOPort input2;
    public SDFIOPort output;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        SDFTestJoin newobj = (SDFTestJoin)(super.clone(ws));
        newobj.input1 = (SDFIOPort)newobj.getPort("input1");
        newobj.input2 = (SDFIOPort)newobj.getPort("input2");
        newobj.output = (SDFIOPort)newobj.getPort("output");
        return newobj;
    }

    /** Fire the actor.
     * Copy one token from input1 to the output and then copy one token
     * from input2 to the output.
     * @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        IntToken message;

        message = (IntToken)input1.get(0);
        output.send(0, message);
        message = (IntToken)input2.get(0);
        output.send(0, message);

    }
}
