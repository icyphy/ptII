/* A code generation helper class for domains.ptides.lib.SensorReceiver
 @Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.luminary.domains.ptides.lib;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.ptides.lib.SensorReceiver

 @author Jeff C. Jensen, Jia Zou
 @version $Id$
 @since Ptolemy II 7.1
 */
public class SensorInputDevice extends CCodeGeneratorHelper {

    /**
     * Construct a SensorReceiver helper.
     * @param actor The associated actor.
     */
    public SensorInputDevice(ptolemy.domains.ptides.lib.SensorInputDevice actor) {
        super(actor);
    }

    /**
     * Generate preinitialize code.
     * Reads the <code>preinitBlock</code> from SensorReceiver.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.domains.ptides.lib.SensorInputDevice actor = (ptolemy.domains.ptides.lib.SensorInputDevice) getComponent();

        return code.toString();
    }
}
