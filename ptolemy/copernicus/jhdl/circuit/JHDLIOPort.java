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

import byucc.jhdl.base.Cell;
import byucc.jhdl.base.CellInterface;
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
 * A JHDLIOPort is a Ptolemy representation of a JHDL port. It has a
 * signal width (bit width) and methods for resolving bit widths.
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLIOPort extends IOPort implements Signal {
    public JHDLIOPort(ComponentEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        this(container, name, Signal.UNRESOLVED);
    }

    public JHDLIOPort(ComponentEntity container, String name, int width)
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

    // Set the width of all inside linked relations. Call resolve propagate
    // on all linked relations. Can only perform resolution when
    // port is resolved.
    public boolean resolveInside() {
        System.out.println("Resolving Inside " + this);
        return _resolveRelations(insideRelationList()); // inside relations
    }

    // Set the width of all outside linked relations. Call resolve propagate
    // on all linked relations. Can only perform resolution when
    // port is resolved.
    public boolean resolveOutside() {
        System.out.println("Resolving Outside " + this);
        return _resolveRelations(linkedRelationList()); // outside relations
    }

    // Resolve a list of relations linked to this port
    protected boolean _resolveRelations(List relationList) {
        if (!isResolved()) {
            return false;
        }

        int width = getSignalWidth();

        // propagate signal width of this port to each linked relation
        // and Port
        for (Iterator i = relationList.iterator(); i.hasNext();) {
            JHDLIORelation r = (JHDLIORelation) i.next();
            System.out.println("Resolving " + r);

            if (!r.isResolved()) {
                r.setSignalWidth(width);
            } else {
                if (r.getSignalWidth() != width) {
                    return false;
                }
            }

            for (Iterator j = r.linkedPortList().iterator(); j.hasNext();) {
                JHDLIOPort port = (JHDLIOPort) j.next();

                if (port == this) {
                    continue;
                }

                if (!port.isResolved()) {
                    port.setSignalWidth(width);
                } else {
                    if (port.getSignalWidth() != width) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Each JHDLIOPort may have at most one Relation linked to the
     * port from the outside. This method will return the
     * JHDLIORelation that is linked to the port from the outside
     * (linkedRelationList). If no Relation is linked to this port,
     * this method will return a null.
     **/
    public JHDLIORelation getOutsideRelation() {
        Iterator i = linkedRelationList().iterator();

        if (!i.hasNext()) {
            return null;
        }

        JHDLIORelation or = (JHDLIORelation) i.next();
        return or;
    }

    /**
     * Each JHDLIOPort may have at most one Relation linked to the
     * inside of the actor. This will only be necessary for composite
     * actors that have relations between internal entities.
     *
     * This method will return the JHDLIORelation that is linked to
     * the port on the inside using the insideRelationList().
     * If no Relation is linked to this port,
     * this method will return a null.
     **/
    public JHDLIORelation getInsideRelation() {
        Iterator i = insideRelationList().iterator();

        if (!i.hasNext()) {
            return null;
        }

        JHDLIORelation ir = (JHDLIORelation) i.next();

        // there should not be any
        return ir;
    }

    // Creates a JHDL port
    public void buildJHDLPort(Cell parent) {
        String portName = getName();

        // 1. Add JHDL port to JHDL parent Cell
        CellInterface ci = null;

        if (isInput()) {
            ci = Cell.in(portName, getSignalWidth());
        } else {
            ci = Cell.out(portName, getSignalWidth());
        }

        parent.addPort(ci);

        // 2. Get top-level wire (assume top-level wire has been added)
        JHDLIORelation or = getOutsideRelation();
        Wire outsideWire = or.getJHDLWire();

        // 3. JHDL port connect (get inside Wire)
        Wire insideWire = parent.connect(portName, outsideWire);
        System.out.println("Creating inner wire " + insideWire);

        // 4. Get inner relation linked to this port & associate
        //    relation with new Wire
        JHDLIORelation ir = getInsideRelation();
        ir.setJHDLWire(insideWire);
    }

    protected String _description(int detail, int indent, int bracket) {
        return super._description(detail, indent, bracket) + " { portWidth="
        + _portWidth + (isInput() ? " input" : " output") + " }";
    }

    protected int _portWidth;
}
