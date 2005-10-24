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
 * This class represents a "Wire" in JHDL. It has a signal width and
 * provides signal width resolution functionality.
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLIORelation extends IORelation implements Signal {
    public JHDLIORelation(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        this(container, name, Signal.UNRESOLVED);
    }

    public JHDLIORelation(CompositeEntity container, int width)
        throws IllegalActionException, NameDuplicationException {
        this(container, container.uniqueName("R"), width);
    }

    public JHDLIORelation(CompositeEntity container)
        throws IllegalActionException, NameDuplicationException {
        this(container, container.uniqueName("R"), Signal.UNRESOLVED);
    }

    public JHDLIORelation(CompositeEntity container, String name, int width)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _portWidth = width;
    }

    public int getSignalWidth() {
        return _portWidth;
    }

    public void setSignalWidth(int width) {
        _portWidth = width;
    }

    public boolean isResolved() {
        if (_portWidth != Signal.UNRESOLVED) {
            return true;
        }

        return false;
    }

    // Resolve strategy:
    // - Output ports are resolved by the container
    // - Input ports are resolved by the container
    // - Relations are resolved bythe output ports

    /*
      public boolean resolve() {

      if (isResolved()) {
      return isResolved();
      }

      // Output ports set the signal width
      for (Iterator i = linkedPortList().iterator();i.hasNext();) {
      JHDLIOPort port = (JHDLIOPort) i.next();
      if (port.isOutput()) {
      setSignalWidth(port.getSignalWidth());
      }
      }
      return isResolved();
      }
    */
    public Wire getJHDLWire() {
        return _wire;
    }

    public Wire buildJHDLWire(Logic parent) {
        _wire = parent.wire(getSignalWidth(), getName());
        System.out.println("Creating JHDL Wire for relation " + this + " wire="
            + _wire);
        return _wire;
    }

    public void setJHDLWire(Wire wire) {
        _wire = wire;
    }

    protected String _description(int detail, int indent, int bracket) {
        return super._description(detail, indent, bracket)
        + " { relationWidth=" + _portWidth + "}";
    }

    protected int _portWidth;
    protected Wire _wire;
}
