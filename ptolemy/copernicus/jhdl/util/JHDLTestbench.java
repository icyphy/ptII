/*

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.util;

import byucc.jhdl.base.TestBench;
import byucc.jhdl.base.HWSystem;
import byucc.jhdl.base.Wire;
import byucc.jhdl.base.Cell;
import byucc.jhdl.Logic.Logic;

import java.util.Vector;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// JHDLTestbench.java
/**
 *
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class JHDLTestbench extends Logic implements TestBench{

    public JHDLTestbench(HWSystem parent) {
	this(parent,JHDL_TESTBENCH_NAME,DEFAULT_CELL_NAME);
    }

    public JHDLTestbench(HWSystem parent, String cellname) {
 	this(parent,JHDL_TESTBENCH_NAME,cellname);
    }

    public JHDLTestbench(HWSystem parent, String testbenchname, 
            String cellname) {
	super(parent,testbenchname);
	_inputWires = new Vector();
	_outputWires = new Vector();
	_topcell = new Logic(this,cellname);
    }

    public Cell getTopCell() { return _topcell; }

    public Wire addPrimaryInputWire(String name, int bits) {
        Wire w = wire(bits,name);
        _inputWires.add(w);
        return w;
    }

    public Wire addPrimaryOutputWire(String name, int bits) {
        Wire w = wire(bits,name);
        _outputWires.add(w);
        return w;
    }

    public static final String JHDL_TESTBENCH_NAME = "JHDLTestBench";
    public static final String DEFAULT_CELL_NAME = "top";

    protected Vector _inputWires;
    protected Vector _outputWires;
    protected Cell _topcell;

}
