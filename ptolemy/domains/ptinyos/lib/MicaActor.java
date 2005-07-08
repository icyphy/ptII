/* FIXME comment

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptinyos.kernel.PtinyOSActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// MicaActor

/**
 * This composite actor is designed for use in the PtinyOS domain.
 *
 * FIXME comment
 *
 * @author Elaine Cheong
 * @version $Id$
 * @Pt.ProposedRating Red (celaine)
 * @Pt.AcceptedRating Red (celaine)
 */
public class MicaActor extends PtinyOSActor {
    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public MicaActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public MicaActor(Workspace workspace) {
        super(workspace);
    }

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MicaActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Photo input port.
        // FIXME why is the name hidden in Vergil?
        photo = new PortParameter(this, "photo");
        photo.setExpression("0");

        // FIXME this is really an unsigned short (uint16_t), not an int.
        photo.setTypeEquals(BaseType.DOUBLE);

        temperature = new PortParameter(this, "temperature");
        temperature.setExpression("0");
        temperature.setTypeEquals(BaseType.DOUBLE);

        accelx = new PortParameter(this, "accelx");
        accelx.setExpression("0");
        accelx.setTypeEquals(BaseType.DOUBLE);

        accely = new PortParameter(this, "accely");
        accely.setExpression("0");
        accely.setTypeEquals(BaseType.DOUBLE);

        magx = new PortParameter(this, "magx");
        magx.setExpression("0");
        magx.setTypeEquals(BaseType.DOUBLE);

        magy = new PortParameter(this, "magy");
        magy.setExpression("0");
        magy.setTypeEquals(BaseType.DOUBLE);

        // LED output ports.
        ledRed = new TypedIOPort(this, "ledRed", false, true);
        ledRed.setTypeEquals(BaseType.BOOLEAN);

        ledGreen = new TypedIOPort(this, "ledGreen", false, true);
        ledGreen.setTypeEquals(BaseType.BOOLEAN);

        ledYellow = new TypedIOPort(this, "ledYellow", false, true);
        ledYellow.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Persistent photosensor input data.
     */
    public PortParameter photo;

    /** FIXME comment
     */
    public PortParameter temperature;

    public PortParameter microphone;

    public PortParameter accelx;

    public PortParameter accely;

    public PortParameter magx;

    public PortParameter magy;

    /* From tinyos-1.x/tos/platform/pc/sensorboard.h

     enum {
     TOS_ADC_PHOTO_PORT = 1,
     TOS_ADC_TEMP_PORT = 2,
     TOS_ADC_MIC_PORT = 3,
     TOS_ADC_ACCEL_X_PORT = 4,
     TOS_ADC_ACCEL_Y_PORT = 5,
     TOS_ADC_MAG_X_PORT = 6,
     // TOS_ADC_VOLTAGE_PORT = 7,  defined this in hardware.h
     TOS_ADC_MAG_Y_PORT = 8,
     };
     */

    /** LED output ports
     */
    public TypedIOPort ledRed;

    public TypedIOPort ledGreen;

    public TypedIOPort ledYellow;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
}
