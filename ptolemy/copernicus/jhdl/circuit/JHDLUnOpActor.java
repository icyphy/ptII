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
 * This class represents a unary operator JHDL circuit. This class
 * can generate the following JHDL circuits: NOT
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLUnOpActor extends JHDLAtomicActor {
    JHDLUnOpActor(CompositeEntity container, int operation)
        throws IllegalActionException, NameDuplicationException {
        super(container);
        input = new JHDLIOPort(this, "input");
        output = new JHDLIOPort(this, "output");
        _operation = operation;
    }

    public boolean resolve() {
        if (!input.isResolved()) {
            return false;
        }

        if (output.isResolved()) {
            return true;
        }

        output.setSignalWidth(input.getSignalWidth());
        output.resolveOutside();
        return true;
    }

    public void build(Logic parent) {
        // Assume NOT operator for now
        Wire inputWire = input.getOutsideRelation().getJHDLWire();
        Wire outputWire = output.getOutsideRelation().getJHDLWire();
        Wire notWire = parent.not(inputWire);
        parent.buf_o(notWire, outputWire);
    }

    public JHDLIOPort input;
    public JHDLIOPort output;
    public static final int NOT = 1;
    protected int _operation;
}
