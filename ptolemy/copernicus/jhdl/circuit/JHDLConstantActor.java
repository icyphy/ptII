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

import byucc.jhdl.apps.Viewers.Schematic.SmartSchematicFrame;

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
 * This class represents a JHDL constant circuit. This class will generate
 * the corresponding JHDL Wire with the given constant.
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLConstantActor extends JHDLAtomicActor {
    JHDLConstantActor(CompositeEntity container, String name, int constant,
        int width) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _constant = constant;
    }

    JHDLConstantActor(CompositeEntity container, int constant, int width)
        throws IllegalActionException, NameDuplicationException {
        this(container, container.uniqueName("C"), constant, width);
        output = new JHDLIOPort(this, "output", width);
    }

    public boolean resolve() {
        boolean resolve = output.resolveOutside();
        return resolve;
    }

    public void build(Logic parent) {
        Wire c = parent.constant(output.getSignalWidth(), _constant);
        JHDLIORelation r = output.getOutsideRelation();
        parent.buf_o(c, r.getJHDLWire());
    }

    protected int _constant;
    public JHDLIOPort output;
}
