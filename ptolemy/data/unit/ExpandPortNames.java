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
import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// ExpandPortNames
/**
Visit a UnitEquation and substitute each portname with its full portname. An
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

    /** The method that actually does the substitution.
     * @param equation
     * @param actor
     */
    public void expand(UnitEquation equation, ComponentEntity actor) {
        _actorPorts = actor.portList();
        try {
            equation.visit(this);
        } catch (IllegalActionException e) {
            KernelException.stackTraceToString(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The method that actually does the substitution of a variable with
     *  the full name of the port.
     * @see ptolemy.data.unit.EquationVisitor#_visitUnitTerm(ptolemy.data.unit.UnitTerm)
     */
    protected Object _visitUnitTerm(UnitTerm uTerm)
        throws IllegalActionException {
        if (uTerm.isVariable()) {
            String _portName = uTerm.getVariable();
            Iterator iter = _actorPorts.iterator();
            while (iter.hasNext()) {
                IOPort actorPort = (IOPort) iter.next();
                if (actorPort.getName().equals(_portName)) {
                    uTerm.setVariable(actorPort.getFullName());
                    return null;
                }
            }
            throw new IllegalActionException(
                "Can't find Model port " + _portName);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    List _actorPorts;
}
