/* An adapter class for ptolemy.actor.lib.Ramp.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalAND.actor.lib;

import java.util.List;

import ptolemy.data.IntToken;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Ramp

/**
 An adapter class for ptolemy.actor.lib.Ramp.

 @author Thomas Mandl
@version $Id$
@since Ptolemy II 7.1
*/
public class Ramp extends Source {

    /**
     */
    public Ramp(PropertyConstraintSolver solver, ptolemy.actor.lib.Ramp actor)
            throws IllegalActionException {

        super(solver, actor);
        _actor = actor;
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        if (_actor.step.getPort().connectedPortList().isEmpty()) {
            setAtLeast(_actor.output, _actor.step);
            if (_actor.step.getToken().isEqualTo(new IntToken(0))
                    .booleanValue()) {
                setAtLeast(_actor.output, _lattice.getElement("TRUE"));
            } else {
                setAtLeast(_actor.output, _lattice.getElement("FALSE"));
            }
        } else {
            // since we do not have partial evaluation we have to set
            // the property to a conservative solution
            setAtLeast(_actor.output, _lattice.getElement("FALSE"));
        }

        return super.constraintList();
    }

    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        if (_actor.step.getPort().connectedPortList().isEmpty()) {
            result.add(_actor.step);
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.Ramp _actor;

}
