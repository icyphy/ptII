/* UnitEquation visitor that substitutes any portname with their full portname.

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
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ExpandPortNames
/**
Visit a UnitEquation and for each contained variable that represents a port
substitute it with a variable that represents the port from
the perspective of the model that contains the actor that contains the port.
For example, the variable representing the value of the plus port of an actor
named AddSubtract22 would originally have the variable label plus which would
be substituted with AddSubtract22.plus.
The reason for doing this is that a
ComponentEntity will have constraints on units specified as a set of
UnitEquations. Within each UnitEquation a variable of the form $PortName is
used to represent the Unit value at that port. Since a CompositeEntity will
have several ComponentEntities, each with a set of ports, it is possible that
port names will be duplicated.
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/

public class ExpandPortNames extends EquationVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The method is the entry point to the class.
     * @param equation The UnitEquation to be visited.
     * @param actor The ComponentEntity that contains ports that may be
     * referenced in the equation.
     */
    public void expand(UnitEquation equation, ComponentEntity actor)
        throws IllegalActionException {
        _actorPorts = actor.portList();
        equation.visit(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The method that actually does the substitution of a variable with
     *  the model name of the port.
     * @see ptolemy.data.unit.EquationVisitor#_visitUnitTerm(UnitTerm)
     */
    protected Object _visitUnitTerm(UnitTerm uTerm)
        throws IllegalActionException {
        if (uTerm.isVariable()) {
            String portName = uTerm.getVariable();
            if (portName == null) {
                throw new IllegalActionException(uTerm + " is not a variable");
            }
            Iterator iter = _actorPorts.iterator();
            while (iter.hasNext()) {
                IOPort actorPort = (IOPort) iter.next();
                if (actorPort.getName().equals(portName)) {
                    uTerm.setVariable(
                        actorPort.getName(
                            actorPort.getContainer().getContainer()));
                    return null;
                }
            }
            throw new IllegalActionException(
                "Can't find Model port " + portName);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    List _actorPorts;
}
