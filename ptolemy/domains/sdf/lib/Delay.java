/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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
package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

/**
 * This class serves as a delay for Dataflow Domains which allows schedulers
 * like SDF to break a cycle within the topology.  It uses SDFAtomicActor's
 * setTokenInitProduction method to specify that it will create a token
 * on its output port during initialization.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
//FIXME: This should maybe get rewritten somehow so that it never
// Actually gets fired, but overrides getRemoteReceivers on it's input
// port to return the RemoteReceivers of its output port.
public class Delay extends SDFAtomicActor {
    public Delay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try{
            TypedIOPort input = (TypedIOPort)newPort("input");
            input.setInput(true);
            setTokenConsumptionRate(input, 1);
            input.setTypeEquals(Token.class);

            TypedIOPort output = (TypedIOPort)newPort("output");
            output.setOutput(true);
            setTokenProductionRate(output, 1);
            output.setTypeEquals(Token.class);
            setTokenInitProduction(output, 1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFDelay: constructor error");
            e1.printStackTrace();
        }
    }
 
    public TypedIOPort input;
    public TypedIOPort output;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Delay newobj = (Delay)(super.clone(ws));
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** 
     * Initialize this actor.
     * Create the Delay Token.
     * @exception IllegalActionException If one of the contained methods 
     * throws it.
     */
    public void initialize() throws IllegalActionException {
        // Create the Delay token.
        IntToken token = new IntToken();
        TypedIOPort output = (TypedIOPort)getPort("output");
        output.send(0, token);
    }

    /** 
     * Fire this actor.
     * Copy the input to the output.
     * @exception IllegalActionException If one of the contained methods 
     * throws it.
     */
    public void fire() throws IllegalActionException {
        Token message = input.get(0);
        output.send(0, message);
    }
}






