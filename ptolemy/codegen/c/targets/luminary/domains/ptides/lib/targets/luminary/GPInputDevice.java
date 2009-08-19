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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.codegen.c.domains.ptides.lib.InputDevice;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A code generation helper class for ptolemy.domains.ptides.lib.targets.luminary.GPInputDevice.
 * @author Jia Zou, Isaac Liu, Jeff Jensen
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */

public class GPInputDevice extends InputDevice {
    /** Construct a helper with the given
     *  ptolemy.domains.ptides.lib.GPIOA_Transmitter actor.
     *  @param actor The given ptolemy.domains.ptides.lib.targets.luminary.GPInputDevice actor.
     *  @throws IllegalActionException 
     * @throws NameDuplicationException 
     */
    public GPInputDevice(
            ptolemy.domains.ptides.lib.luminary.GPInputDevice actor)
            throws IllegalActionException, NameDuplicationException {
        super(actor);

        Parameter pinParameter = actor.pin;
        StringParameter padParameter = actor.pad;
        _pinID = null;
        _padID = null;

        if (pinParameter != null) {
            _pinID = ((IntToken) pinParameter.getToken()).toString();
        } else {
            throw new IllegalActionException(
                    "does not know what pin this output device is associated to.");
        }
        if (padParameter != null) {
            _padID = padParameter.stringValue();
        } else {
            throw new IllegalActionException(
                    "does not know what pin this output device is associated to.");
        }

    }

    ////////////////////////////////////////////////////////////////////
    ////                     public methods                         ////

    public String generateSensorSensingFuncCode() throws IllegalActionException {
        List args = new LinkedList();

        args.add(CodeGeneratorHelper.generateName(getComponent()));
        args.add(_padID);
        args.add(_pinID);

        _codeStream.clear();
        _codeStream.appendCodeBlock("sensingBlock", args);

        return processCode(_codeStream.toString());
    }

    public String generateHardwareInitializationCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        List args = new ArrayList();
        args.add(_padID);
        args.add(_pinID);
        code.append(processCode(_codeStream.getCodeBlock("initializeGPInput",
                args)));
        return code.toString();
    }

    private String _pinID;
    private String _padID;
}
