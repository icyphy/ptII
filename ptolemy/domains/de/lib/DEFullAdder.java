/* A composite actor that contains a full adder.

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
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEFullAdder
/**
This composite actor contains instances of DEXORGate, DEANDGate, and DEORGate
wired together to form a full adder.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEFullAdder extends TypedCompositeActor {

    /** Construct a DESampler actor.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEFullAdder(TypedCompositeActor container,
            String name,
            double gateDelay)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        S = new TypedIOPort(this, "S", false, true);
        Cout = new TypedIOPort(this, "Cout", false, true);

        // create input ports
        A = new TypedIOPort(this, "A", true, false);
        B = new TypedIOPort(this, "B", true, false);
        Cin = new TypedIOPort(this, "Cin", true, false);

        // S = A xor B xor Cin
        DEXORGate xor1 = new DEXORGate(this, "xor1", gateDelay);
        DEXORGate xor2 = new DEXORGate(this, "xor2", gateDelay);
        Relation RelA = this.connect(A, xor1.input1);
        Relation RelB = this.connect(B, xor1.input2);
        Relation RelCin = this.connect(Cin, xor2.input2);
        this.connect(xor1.output, xor2.input1);
        this.connect(xor2.output, S);

        // Cout = AB + BCin + CinA
        DEANDGate and1 = new DEANDGate(this, "and1", gateDelay);
        DEANDGate and2 = new DEANDGate(this, "and2", gateDelay);
        DEANDGate and3 = new DEANDGate(this, "and3", gateDelay);
        DEORGate or1 = new DEORGate(this, "or1", gateDelay);
        DEORGate or2 = new DEORGate(this, "or2", gateDelay);
        and1.input1.link(RelA);
        and1.input2.link(RelB);
        and2.input1.link(RelB);
        and2.input2.link(RelCin);
        and3.input1.link(RelCin);
        and3.input2.link(RelA);
        this.connect(and1.output, or1.input1);
        this.connect(and2.output, or1.input2);
        this.connect(or1.output, or2.input2);
        this.connect(and3.output, or2.input1);
        this.connect(or2.output, Cout);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial token
    private DoubleToken _zeroToken = new DoubleToken(0.0);

    // the last token seen in the input port.
    private DoubleToken _lastToken = _zeroToken;

    // the ports.
    public TypedIOPort A;
    public TypedIOPort B;
    public TypedIOPort Cin;
    public TypedIOPort S;
    public TypedIOPort Cout;

}






