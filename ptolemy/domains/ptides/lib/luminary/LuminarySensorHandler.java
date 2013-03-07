/*
@Copyright (c) 2009-2013 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib.luminary;

import java.util.List;

import ptolemy.domains.ptides.lib.SensorHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * All input devices for Luminary should extend this class. This class
 * saves the total number of configurations of interrupts.
 * For example, if we only support GPInputHandler, i.e., the only subclass
 * of LuminaryInputDevice is GPInputHandler, and GPInputHandler supports 8
 * configurations, then numberOfSupportedInputDeviceConfigurations is set to
 * 8. If more devices are implemented, numberOfSupportedInputDeviceConfigurations
 * should be updated.
 *
 * @author Jia Zou
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating
 *
 */
public abstract class LuminarySensorHandler extends SensorHandler {

    /**
     * Constructs a LuminarySensorHandler object.
     *
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException if the super constructor throws it.
     * @exception NameDuplicationException if the super constructor throws it.
     */
    public LuminarySensorHandler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The total number of supported configurations for all Luminary
     *  sensor input devices. A sensor device could have multiple access
     *  pin and pads (configurations), this number keeps track of the
     *  total number of configurations available.
     */
    public static int numberOfSupportedInputDeviceConfigurations = 8;

    /** A sensor device could have multiple access pin and pads
     *  (configurations), This method returns the current configuration.
     *  @return The current configuration captured in a string.
     *  @exception IllegalActionException
     */
    abstract public String configuration() throws IllegalActionException;

    /** The set of supported configurations.
     *  @return The set of supported configurations.
     */
    abstract public List<String> supportedConfigurations();

    /** There is a default configuration, which is returned.
     *  @return The default configuration.
     */
    abstract public String startingConfiguration();
}
