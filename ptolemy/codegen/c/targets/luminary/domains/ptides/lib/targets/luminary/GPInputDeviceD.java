/* A code generation helper class for ptolemy.domains.sdf.lib.SampleDelay

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
package ptolemy.codegen.c.targets.luminary.domains.ptides.lib.targets.luminary;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.ptides.lib.GPInputDeviceD.

 @author elgeeko
 */

public class GPInputDeviceD extends ptolemy.codegen.c.targets.luminary.domains.ptides.lib.GPInputDevice {
    /** Construct a helper with the given
     *  ptolemy.domains.ptides.lib.GPInputDeviceD actor.
     *  @param actor The given ptolemy.domains.ptides.lib.GPInputDeviceD actor.
     */
    public GPInputDeviceD(ptolemy.domains.ptides.lib.targets.luminary.GPInputDeviceD actor) {
        super(actor);
    }
    
    ////////////////////////////////////////////////////////////////////
    ////                     public methods                         ////

    /** Generate the initialize code for the GPIOA_Transmitter actor by
     *  enabling and configuring the peripheral and interrupts
     *  @return The generated initialize code for the GPIOA_Transmitter actor.
     *  @exception IllegalActionException If the base class throws it,
     *   or if the initial
     *   outputs of the GPIOA_Transmitter actor is not defined.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.domains.ptides.lib.targets.luminary.GPInputDeviceD actor = (ptolemy.domains.ptides.lib.targets.luminary.GPInputDeviceD) getComponent();

	return "";
    }
}
