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

import byucc.jhdl.Logic.*;

import byucc.jhdl.base.Cell;
import byucc.jhdl.base.HWSystem;
import byucc.jhdl.base.TestBench;

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
 * A testbench for a generated JHDL circuit. Creates the top-level
 * wires for the circuit under test.
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLActorTestbench extends JHDLCompositeActor {
    public JHDLActorTestbench(ComponentEntity e)
        throws IllegalActionException, NameDuplicationException {
        super();
        setName("testbench");
        _e = e;
        _e.setContainer(this);

        // add relation for each port
        for (Iterator i = e.portList().iterator(); i.hasNext();) {
            JHDLIOPort port = (JHDLIOPort) i.next();
            JHDLIORelation r = (JHDLIORelation) newRelation();
            r.setSignalWidth(port.getSignalWidth());
            port.link(r);
        }
    }

    public Cell getTestbench() {
        return _testbench;
    }

    public void build(HWSystem hw) {
        _testbench = new SimpleTestbench(hw, "testbench");
        System.out.println("Buliding testbench " + _testbench);

        for (Iterator i = relationList().iterator(); i.hasNext();) {
            JHDLIORelation r = (JHDLIORelation) i.next();

            if (r.getJHDLWire() == null) {
                r.buildJHDLWire(_testbench);
            }
        }

        ((ConstructJHDL) _e).build(_testbench);
    }

    // Component under test
    protected ComponentEntity _e;

    // Actual JHDL testbench
    protected Logic _testbench;
}
