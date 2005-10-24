/* This composite actor represents the hardware interface to the Mica
 mote and is designed for use in the PtinyOS domain.

 Copyright (c) 2005 The Regents of the University of California.
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
 This composite actor represents the hardware interface to the Mica
 mote and is designed for use in the PtinyOS domain.  It contains
 input ports for the photosensor, temperature sensor, microphone,
 accelerometer (x- and y-axis), magnetometer (x- and y-axis); and
 output ports for the LEDs (red, green, and yellow).

 <p>Note: the ports are actually of type unsigned short (uint16_t),
 although we implement them with double tokens.

 <p>Port information from tinyos-1.x/tos/platform/pc/sensorboard.h:
<pre>
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
</pre>

 @author Elaine Cheong
 @version $Id$
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 @since Ptolemy II 5.1
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

        // Persistent photo input port.
        photo = new PortParameter(this, "photo");
        photo.setExpression("0");
        photo.setTypeEquals(BaseType.DOUBLE);

        // Persistent temperature input port.
        temperature = new PortParameter(this, "temperature");
        temperature.setExpression("0");
        temperature.setTypeEquals(BaseType.DOUBLE);

        // Persistent microphone input port.
        microphone = new PortParameter(this, "microphone");
        microphone.setExpression("0");
        microphone.setTypeEquals(BaseType.DOUBLE);

        // Persistent accelerometer (x-axis) input port.
        accelx = new PortParameter(this, "accelx");
        accelx.setExpression("0");
        accelx.setTypeEquals(BaseType.DOUBLE);

        // Persistent accelerometer (y-axis) input port.
        accely = new PortParameter(this, "accely");
        accely.setExpression("0");
        accely.setTypeEquals(BaseType.DOUBLE);

        // Persistent magnetometer (x-axis) input port.
        magx = new PortParameter(this, "magx");
        magx.setExpression("0");
        magx.setTypeEquals(BaseType.DOUBLE);

        // Persistent magnetometer (y-axis) input port.
        magy = new PortParameter(this, "magy");
        magy.setExpression("0");
        magy.setTypeEquals(BaseType.DOUBLE);

        // Red LED output port.
        ledRed = new TypedIOPort(this, "ledRed", false, true);
        ledRed.setTypeEquals(BaseType.BOOLEAN);

        // Green LED output port.
        ledGreen = new TypedIOPort(this, "ledGreen", false, true);
        ledGreen.setTypeEquals(BaseType.BOOLEAN);

        // Yellow LED output port.
        ledYellow = new TypedIOPort(this, "ledYellow", false, true);
        ledYellow.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Persistent photosensor input data.
     */
    public PortParameter photo;

    /** Persistent temperature input data.
     */
    public PortParameter temperature;

    /** Persistent microphone input data.
     */
    public PortParameter microphone;

    /** Persistent accelerometer (x-axis) input data.
     */
    public PortParameter accelx;

    /** Persistent accelerometer (y-axis) input data.
     */
    public PortParameter accely;

    /** Persistent magnetometer (x-axis) input data.
     */
    public PortParameter magx;

    /** Persistent magnetometer (y-axis) input data.
     */
    public PortParameter magy;

    /** Red LED output port.
     */
    public TypedIOPort ledRed;

    /** Green LED output port.
     */
    public TypedIOPort ledGreen;

    /** Yellow LED output port.
     */
    public TypedIOPort ledYellow;
}
