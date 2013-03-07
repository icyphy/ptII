/* This composite actor represents the hardware interface to the Mica
 mote and is designed for use in the PtinyOS domain.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
import ptolemy.domains.ptinyos.kernel.PtinyOSCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MicaCompositeActor

/**
 This composite actor represents the hardware interface to the Mica
 mote and is designed for use in the PtinyOS domain.  It contains
 input ports for the photosensor, temperature sensor, microphone,
 accelerometer (x- and y-axis), magnetometer (x- and y-axis); and
 output ports for the LEDs (red, green, and yellow).

 <p>This actor is always a type opaque composite actor.  The outside
 types of the input ports (the sensor ports) are of type DoubleToken,
 and the inside types are of type unsigned short (uint16_t) in C.  The
 C code (
 tinyos-1.x/contrib/ptII/ptinyos/tos/platform/ptII/adc_model.c) masks
 the unsigned short for 10-bit usage (10 least significant bits), to
 reflect the bit-width of the ADC registers on the actual Mica
 hardware.  The outside types of the output ports (the LED ports) are
 of type BooleanToken, and the inside types are of type short in C.

 <p>Detailed port information can be found in
 tinyos-1.x/tos/platform/pc/sensorboard.h.

 <p> Also see tinyos-1.x/contrib/ptII/ptinyos/tos/platform/ptII/ptII.c
 and tinyos-1.x/contrib/ptII/ptinyos/tos/platform/ptII/adc_model.c

 @author Elaine Cheong
 @version $Id$
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (celaine)
 @since Ptolemy II 5.1
 */
public class MicaCompositeActor extends PtinyOSCompositeActor {
    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public MicaCompositeActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public MicaCompositeActor(Workspace workspace) {
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
    public MicaCompositeActor(CompositeEntity container, String name)
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
        accelerometerX = new PortParameter(this, "accelerometerX");
        accelerometerX.setExpression("0");
        accelerometerX.setTypeEquals(BaseType.DOUBLE);

        // Persistent accelerometer (y-axis) input port.
        accelerometerY = new PortParameter(this, "accelerometerY");
        accelerometerY.setExpression("0");
        accelerometerY.setTypeEquals(BaseType.DOUBLE);

        // Persistent magnetometer (x-axis) input port.
        magnetometerX = new PortParameter(this, "magnetometerX");
        magnetometerX.setExpression("0");
        magnetometerX.setTypeEquals(BaseType.DOUBLE);

        // Persistent magnetometer (y-axis) input port.
        magnetometerY = new PortParameter(this, "magnetometerY");
        magnetometerY.setExpression("0");
        magnetometerY.setTypeEquals(BaseType.DOUBLE);

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

    /** Persistent photosensor input data.  The default value is a
     *  DoubleToken with value 0.  The valid range is between 0 and
     *  0x3FF.
     */
    public PortParameter photo;

    /** Persistent temperature input data.  The default value is a
     *  DoubleToken with value 0.  The valid range is between 0 and
     *  0x3FF.
     */
    public PortParameter temperature;

    /** Persistent microphone input data.  The default value is a
     *  DoubleToken with value 0.  The valid range is between 0 and
     *  0x3FF.
     */
    public PortParameter microphone;

    /** Persistent accelerometer (x-axis) input data.  The default
     *  value is a DoubleToken with value 0.  The valid range is
     *  between 0 and 0x3FF.
     */
    public PortParameter accelerometerX;

    /** Persistent accelerometer (y-axis) input data.  The default
     *  value is a DoubleToken with value 0.  The valid range is
     *  between 0 and 0x3FF.
     */
    public PortParameter accelerometerY;

    /** Persistent magnetometer (x-axis) input data.  The default value is a
     *  DoubleToken with value 0.  The valid range is between 0 and
     *  0x3FF.
     */
    public PortParameter magnetometerX;

    /** Persistent magnetometer (y-axis) input data.  The default
     *  value is a DoubleToken with value 0.  The valid range is
     *  between 0 and 0x3FF.
     */
    public PortParameter magnetometerY;

    /** Red LED output port.  The default type is BooleanToken.
     */
    public TypedIOPort ledRed;

    /** Green LED output port.  The default type is BooleanToken.
     */
    public TypedIOPort ledGreen;

    /** Yellow LED output port.  The default type is BooleanToken.
     */
    public TypedIOPort ledYellow;
}
