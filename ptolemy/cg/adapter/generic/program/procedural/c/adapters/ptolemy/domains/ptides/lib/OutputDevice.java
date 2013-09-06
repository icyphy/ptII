/* A code generation adapter class for domains.ptides.lib.OutputDevice

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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation adapter class for ptolemy.domains.ptides.lib.OutputDevice.

 @author Jeff C. Jensen, Jia Zou
 @version $Id$
 @since Ptolemy II 8.0
 */
public abstract class OutputDevice extends NamedProgramCodeGeneratorAdapter {

    /**
     * Construct an OutputDevice adapter.
     * @param actor The associated actor.
     */
    public OutputDevice(ptolemy.domains.ptides.lib.OutputDevice actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the code for the actuator actuation function.
     * @return the code for the sensing function, which in this case is
     * the empty string.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateActuatorActuationFuncCode()
            throws IllegalActionException {
        return "";
    }

    /**
     * Return the hardware initialization code.
     * @return the hardware initialization code, which in this case
     * is the empty string.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateHardwareInitializationCode()
            throws IllegalActionException {
        return "";
    }
}
