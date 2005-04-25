/*

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.jhdl.circuit;

import byucc.jhdl.Logic.Logic;
import byucc.jhdl.Logic.Modules.arrayMult;

import byucc.jhdl.base.HWSystem;
import byucc.jhdl.base.Wire;

import soot.*;

import soot.jimple.*;

import ptolemy.actor.*;
import ptolemy.copernicus.jhdl.soot.*;
import ptolemy.copernicus.jhdl.util.*;
import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
////

/**
 * This class represents a binary operator JHDL circuit. This class
 * can generate the following JHDL circuits: ADD, SUB, AND, OR, XOR,
 * MULT, and conditions.
 *
 * TODO: implement circuit conditionals!
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLBinOpActor extends JHDLAtomicActor {
    JHDLBinOpActor(CompositeEntity container, int operation)
            throws IllegalActionException, NameDuplicationException {
        super(container);
        input1 = new JHDLIOPort(this, "input1");
        input2 = new JHDLIOPort(this, "input2");
        output = new JHDLIOPort(this, "output");
        _operation = operation;
    }

    public JHDLIOPort input1;
    public JHDLIOPort input2;
    public JHDLIOPort output;

    public boolean resolve() {
        int width;
        System.out.println("Resolving " + getName());

        if (input1.isResolved() && input2.isResolved()) {
            if (input1.getSignalWidth() != input2.getSignalWidth()) {
                System.out.println("Binop input1/input2 signal mismatch: "
                        + input1.getSignalWidth() + " vs. "
                        + input2.getSignalWidth());
                return false;
            }

            if (output.isResolved()) {
                output.resolveOutside();
                return true;
            }
        } else {
            width = Signal.UNRESOLVED;

            if (input1.isResolved()) {
                input1.resolveOutside();
                input2.setSignalWidth(input1.getSignalWidth());
                input2.resolveOutside();
            } else if (input2.isResolved()) {
                input2.resolveOutside();
                input1.setSignalWidth(input2.getSignalWidth());
                input1.resolveOutside();
            } else {
                System.out.println("Both inputs unresolved");
                return false;
            }
        }

        output.setSignalWidth(input1.getSignalWidth());
        output.resolveOutside();
        return true;
    }

    public void build(Logic cell) {
        Wire input1Wire = input1.getOutsideRelation().getJHDLWire();
        Wire input2Wire = input2.getOutsideRelation().getJHDLWire();
        Wire outputWire = output.getOutsideRelation().getJHDLWire();
        Wire binOpWire = null;

        switch (_operation) {
        case ADD:
            binOpWire = cell.add(input1Wire, input2Wire);
            break;

        case SUB:
            binOpWire = cell.sub(input1Wire, input2Wire);
            break;

        case AND:
            binOpWire = cell.and(input1Wire, input2Wire);
            break;

        case OR:
            binOpWire = cell.or(input1Wire, input2Wire);
            break;

        case XOR:
            binOpWire = cell.xor(input1Wire, input2Wire);
            break;

        case MULT:

            Wire allbits = cell.wire(64);
            new arrayMult(cell, // parent
                    input1Wire, // x
                    input2Wire, // y
                    null, // clk_en
                    allbits, // pout
                    true, // signed
                    0); // pipedepth
            binOpWire = allbits.range(31, 0);
            break;
        }

        cell.buf_o(binOpWire, outputWire);
        System.out.println("Building cell with wire " + binOpWire);
    }

    protected String _description(int detail, int indent, int bracket) {
        return super._description(detail, indent, bracket) + " { OP="
            + _operation + "}";
    }

    public static final int ADD = 1;
    public static final int SUB = 2;
    public static final int AND = 3;
    public static final int OR = 4;
    public static final int XOR = 5;
    public static final int MULT = 6;
    public static final int CONDITION = 7;
    protected int _operation;
}
