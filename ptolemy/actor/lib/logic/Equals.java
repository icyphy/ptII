/* A polymorphic logical equals operator.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (johnli@eecs.berkeley.edu) 
@AcceptedRating Red (johnli@eecs.berkeley.edu) 
*/

package ptolemy.actor.lib.logic;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// Equals
/** 

A polymorphic logical equals operator.  This operator has two
input ports and one output port, none of which are multiports.  The
types on the ports are undeclared and will be resolved by the type
resolution mechanism. A datum that arrives on the input port named
<i>upperPort</i> will be compared for equality with a datum in the
input port named <i>lowerPort</i>.  This distinction between operands
is necessary to determine which method to call; data in the
<i>upperPort</i> is comparable to being on the left side of the
equality operator.
<p>
Currently, the type system is quite liberal about the resolved
types it will permit at the inputs.
It consumes at most one input token from each port.
If no input tokens are available at all, then no output is produced.

@author John Li
@version $Id$
*/

public class Equals extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Equals(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	upperPort = new TypedIOPort(this, "upperPort", true, false);
        lowerPort = new TypedIOPort(this, "lowerPort", true, false);
	output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for the EQUALS operation.  This port represents the left 
     *  side of an equals sign.  Its type is inferred from the connections. 
     */
    public TypedIOPort upperPort = null;

    /** Input for the EQUALS operation.  This port represents the right 
     *  side of an equals sign.  Its type is inferred from the connections. 
     */
    public TypedIOPort lowerPort = null;

    /** Output port.  The type is inferred from the connections.
     */
    public TypedIOPort output = null;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Equals newobj = (Equals)super.clone(ws);
            newobj.lowerPort = (TypedIOPort)newobj.getPort("lowerPort");
            newobj.upperPort = (TypedIOPort)newobj.getPort("upperPort");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** If there is a token in both the <i>upperPort</i> and <i>lowerPort</i>
     *  ports, compare their equality and return the resulting
     *  BooleanToken in the <i>output</i> port.  If one or more of the input 
     *  ports has no token, do nothing.  
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (upperPort.hasToken(0) && lowerPort.hasToken(0)) {
            if (upperPort.get(0).isEqualTo(lowerPort.get(0)).booleanValue())
                output.broadcast(BooleanToken.TRUE);
            else
                output.broadcast(BooleanToken.FALSE);
        }   
    }
}
