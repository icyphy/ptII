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
 * Represents a composite actor that can generate a JHDL circuit.
 * Provides methods for creating JHDLIORelations and JHDLIOPorts.
 * Also provides a bit-width resolution algorithm for composite
 * actors.
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class JHDLCompositeActor extends CompositeActor implements Resolve,
    ConstructJHDL {
    public JHDLCompositeActor() {
        super();
    }

    public ComponentRelation newRelation(String name)
        throws IllegalActionException, NameDuplicationException {
        return new JHDLIORelation(this, name);
    }

    public ComponentRelation newRelation() throws IllegalActionException {
        try {
            return new JHDLIORelation(this);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(this, ex, null);
        }
    }

    public Port newPort(String name) throws NameDuplicationException {
        try {
            JHDLIOPort port = new JHDLIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex, null);
        }
    }

    // Resolve bitwidths
    public boolean resolve() {
        boolean ok;

        // 1. Resolve top-level ports
        for (Iterator i = portList().iterator(); i.hasNext();) {
            JHDLIOPort port = (JHDLIOPort) i.next();
            ok = port.resolveInside();
            ok = port.resolveOutside();
        }

        // 2. Iterate through all nodes until none need resolving
        Collection unresolvedNodes = entityList();
        Vector resolvedNodes = new Vector();
        ;

        do {
            resolvedNodes = new Vector(unresolvedNodes.size());

            for (Iterator i = unresolvedNodes.iterator(); i.hasNext();) {
                Resolve r = (Resolve) i.next();
                boolean resolved = r.resolve();
                System.out.println("Resolving " + ((NamedObj) r).getName()
                    + " " + resolved);

                if (resolved == true) {
                    resolvedNodes.add(r);
                    System.out.println(resolved + " resolving " + r);
                }
            }

            //} while (false);
        } while (resolvedNodes.size() < unresolvedNodes.size());

        return true;
    }

    public void build(Logic parent) {
        // 0. Add top-level Cell
        Logic cell = new Logic(parent, getName());
        System.out.println("Creating hardware for " + this + " cell=" + cell);

        // 1. Add top-level ports
        for (Iterator i = portList().iterator(); i.hasNext();) {
            JHDLIOPort port = (JHDLIOPort) i.next();
            port.buildJHDLPort(cell);
        }

        // 2. Create Wires for all internal relations
        for (Iterator i = relationList().iterator(); i.hasNext();) {
            JHDLIORelation r = (JHDLIORelation) i.next();

            if (r.getJHDLWire() == null) {
                r.buildJHDLWire(cell);
            }
        }

        // 3. Create each cell instance
        for (Iterator i = entityList().iterator(); i.hasNext();) {
            ConstructJHDL j = (ConstructJHDL) i.next();
            j.build(cell);
        }

        System.out.println("Done building ");
    }

    // generate a structural view (in dot format) of the circuit
    public String toDot() {
        StringBuffer sb = new StringBuffer();

        sb.append("//Dotfile created for JHDLCompositeActor\r\n");
        sb.append("digraph " + getName() + " {\r\n");
        sb.append("\tcompound=true;\r\n");
        sb.append("\t// Vertices\r\n");

        for (Iterator i = entityList().iterator(); i.hasNext();) {
            Entity e = (Entity) i.next();
            String name;

            sb.append("\t\"" + e.getName() + "\"");

            /*
              if (source.hasWeight()) {
              sb.append(" [label=\""
              +convertSpecialsToEscapes(source.getWeight().toString())
              +"\"]");
              }
            */
            sb.append(";\r\n");
        }

        sb.append("\t// Edges\r\n");

        for (Iterator i = relationList().iterator(); i.hasNext();) {
            JHDLIORelation r = (JHDLIORelation) i.next();
            JHDLIOPort output = null;

            for (Iterator j = r.linkedPortList().iterator(); j.hasNext();) {
                JHDLIOPort port = (JHDLIOPort) j.next();

                if (port.isOutput()) {
                    output = port;
                }
            }

            Entity outputNode = (Entity) output.getContainer();

            for (Iterator j = r.linkedPortList().iterator(); j.hasNext();) {
                JHDLIOPort port = (JHDLIOPort) j.next();

                if (port.isOutput()) {
                    continue;
                }

                Entity destNode = (Entity) port.getContainer();
                sb.append("\t" + outputNode.getName());
                sb.append(" -> ");
                sb.append(destNode.getName());
                sb.append(";\r\n");
            }
        }

        sb.append("}\r\n");
        return sb.toString();
    }
}
