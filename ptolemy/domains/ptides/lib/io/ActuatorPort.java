/* Actuator port.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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


package ptolemy.domains.ptides.lib.io;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
 *  This port provides a specialized TypedIOPort for actuators
 *  used in Ptides. This port just specializes parameters.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class ActuatorPort extends PtidesPort {

    /** Create a new ActuatorPort with a given container and a name.
     * @param container The container of the port.
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public ActuatorPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);

        this.setOutput(true);

        actuateAtEventTimestamp = new Parameter(this, "actuateAtEventTimestamp");
        actuateAtEventTimestamp.setTypeEquals(BaseType.BOOLEAN);
        actuateAtEventTimestamp.setExpression("true");
        _actuateAtEventTimestamp = true;

    }

    /** Actuate at event timestamp parameter that defaults to the boolean value TRUE.
     *  If this parameter is set to FALSE, an actuator can produce outputs as soon
     *  as they are available.
     */
    public Parameter actuateAtEventTimestamp;

    /** Return true if actuation should happen exactly at event timestamp.
     * @return True if actuation should happen exactly at event timestamp.
     */
    public boolean actuateAtEventTimestamp() {
        return _actuateAtEventTimestamp;
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == actuateAtEventTimestamp) {
            _actuateAtEventTimestamp = ((BooleanToken) actuateAtEventTimestamp.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }


    private boolean _actuateAtEventTimestamp;

}
