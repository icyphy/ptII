/* A less than operator that compares two doubles.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.logic;

import ptolemy.kernel.util.*;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;

//////////////////////////////////////////////////////////////////////////
//// LessThan
/**

A less than operator that compares two doubles.  This operator has two
input ports and one output port, none of which are multiports.  The
types on the ports are undeclared to be double since only the types that
can be losslessly converted to double can be compared.

A datum that arrives on the input port named
<i>leftHandSide</i> will be compared for less than with a datum in the
input port named <i>rightHandSide</i>. The output is of type boolean 
indicating whether the <i>leftHandSide</i> is less than the 
<i>rightHandSide</i>.
<p>
Currently, this actor does reasonable comparison only when there is 
at least one token in each of the input port. If one of the port is
empty, the actor will produce "false".
For each firing, the actor will consume exact one token from each
input port, if there is any.

@author Jie Liu
@version $Id$
*/

public class LessThan extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public LessThan(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	leftHandSide = new TypedIOPort(this, "leftHandSide", true, false);
        leftHandSide.setTypeEquals(BaseType.DOUBLE);
        rightHandSide = new TypedIOPort(this, "rightHandSide", true, false);
        rightHandSide.setTypeEquals(BaseType.DOUBLE);
	output = new TypedIOPort(this, "output", false, true);
        output.setTypeLessThan(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for the EQUALS operation.  This port represents the left
     *  side of an equals sign.  Its type is inferred from the connections.
     */
    public TypedIOPort leftHandSide;

    /** Input for the EQUALS operation.  This port represents the right
     *  side of an equals sign.  Its type is inferred from the connections.
     */
    public TypedIOPort rightHandSide;

    /** Output port.  The type is inferred from the connections.
     */
    public TypedIOPort output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        LessThan newobj = (LessThan)super.clone(ws);
        newobj.rightHandSide = (TypedIOPort)newobj.getPort("rightHandSide");
        newobj.rightHandSide.setTypeEquals(BaseType.DOUBLE);
        newobj.leftHandSide = (TypedIOPort)newobj.getPort("leftHandSide");
        newobj.leftHandSide.setTypeEquals(BaseType.DOUBLE);
        newobj.output = (TypedIOPort)newobj.getPort("output");
        newobj.output.setTypeEquals(BaseType.BOOLEAN);
        return newobj;
    }

    /** If there is a token in both the <i>leftHandSide</i> and 
     *  <i>rightHandSide</i>
     *  ports, compare their value and return the resulting
     *  BooleanToken in the <i>output</i> port.  If one or more of the input
     *  ports has no token, output false.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (leftHandSide.hasToken(0)) {
            double left = ((DoubleToken)leftHandSide.get(0)).doubleValue();
            if(rightHandSide.hasToken(0)) {
                double right = 
                    ((DoubleToken)rightHandSide.get(0)).doubleValue();
                if (left < right) 
            if (leftHandSide.get(0).isEqualTo(rightHandSide.get(0)).booleanValue())
                output.broadcast(BooleanToken.TRUE);
            else
                output.broadcast(BooleanToken.FALSE);
        }
    }
}
