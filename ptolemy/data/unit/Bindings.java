/* Bindings is used to represent (variable, value) pairs.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

                                                 PT_COPYRIGHT_VERSION_3
                                                 COPYRIGHTENDKEY
@Pt.ProposedRating Red (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/

package ptolemy.data.unit;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;

//////////////////////////////////////////////////////////////////////////
//// Bindings
/**
Represents a set of bindings. Each binding is a (variable, Unit) pair where
variable is a String. If the value of Unit is null, then the binding for the
variable exists but its value is null. Since null is an allowable value the
Hashtable class is not adequate.

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class Bindings {

    /**
     * Construct Bindings with no members.
     *
     */
    public Bindings() {
        super();
    }

    /**
     * Create bindings for a set of nodes in a CompositeEntity. The set of
     * nodes can be a subset of the nodes in the CompositeEntity. Each port on
     * each node yields a binding.
     *
     * @param nodes
     *            The set of nodes(ComponentEntities).
     */
    public Bindings(Vector nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            ComponentEntity actor = (ComponentEntity) (nodes.elementAt(i));
            Iterator iter = actor.portList().iterator();
            while (iter.hasNext()) {
                IOPort actorPort = (IOPort) iter.next();
                String varLabel = actorPort.getFullName();
                put(varLabel, null);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public methods                     ////

    /**
     * Return true if the binding exists.
     *
     * @param vLabel A String that represents the variable.
     * @return True if there exists a binding for the variable.
     */
    public boolean bindingExists(String vLabel) {
        Iterator iter = _keys.iterator();
        while (iter.hasNext()) {
            String key = (String) (iter.next());
            if (key.equals(vLabel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the value for a variable.
     *
     * @param vLabel A String that represents the variable.
     * @return The value for the variable.
     */
    public Unit get(String vLabel) {
        Unit u = (Unit) (_VarLabel2Unit.get(vLabel));
        return u;
    }

    /**
     * A human readable form (more or less) of the bindings.
     */
    public String humanReadableForm() {
        StringBuffer retv = new StringBuffer("Bindings\n");
        Iterator keys = _keys.iterator();
        while (keys.hasNext()) {
            String varLabel = (String) (keys.next());
            Unit unit = (Unit) (_VarLabel2Unit.get(varLabel));
            String unitExpr = "null";
            if (unit != null) {
                unitExpr = unit.commonDesc();
            }
            retv.append("   " + varLabel + " = " + unitExpr + "\n");
        }
        retv.append("\\Bindings\n");
        return retv.toString();
    }

    /**
     * Create a binding for a variable and Unit. If a binding already exists
     * for the variable, then update the Unit
     *
     * @param varLabel A String that represents the variable.
     * @param U The Unit.
     */
    public void put(String varLabel, Unit U) {
        _keys.add(varLabel);
        if (U != null) {
            _VarLabel2Unit.put(varLabel, U);
        }
    }

    /**
     * Create an array of Strings that contains all of the variables.
     *
     * @return An array of Strings containing the variables.
     */
    public String[] variableLabels() {
        String retv[] = new String[_keys.size()];
        Iterator iter = _keys.iterator();
        int i = 0;
        while (iter.hasNext()) {
            retv[i++] = (String) (iter.next());
        }
        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private                     variables ////
    Hashtable _VarLabel2Unit = new Hashtable();
    LinkedHashSet _keys = new LinkedHashSet();
}
