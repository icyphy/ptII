/* A code generation adapter class for ptolemy.domains.ptides.lib.targets.luminary.GPInputHandler

 Copyright (c) 2009-2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.lib.luminary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.InputDevice;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A code generation adapter class for ptolemy.domains.ptides.lib.targets.luminary.GPInputHandler.
 * @author Jia Zou, Isaac Liu, Jeff C. Jensen
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */

public class GPInputHandler extends InputDevice {
    /** Construct an adapter with the given
     *  ptolemy.domains.ptides.lib.GPInputHandler actor.
     *  @param actor The given ptolemy.domains.ptides.lib.targets.luminary.GPInputHandler actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GPInputHandler(
            ptolemy.domains.ptides.lib.luminary.GPInputHandler actor)
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
                    "does not know to which pin this output device is associated.");
        }
        if (padParameter != null) {
            _padID = padParameter.stringValue();
        } else {
            throw new IllegalActionException(
                    "does not know to which pin this output device is associated.");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the code for the sensing function.
     * @return the code for the sensing function, which is read from the
     * "sensingBlock"
     * @exception IllegalActionException If thrown while appending the code
     * block or processing the code stream.
     */
    @Override
    public String generateSensorSensingFuncCode() throws IllegalActionException {
        List args = new LinkedList();
        CodeStream _codeStream = _templateParser.getCodeStream();

        args.add(CodeGeneratorAdapter.generateName(getComponent()));
        args.add(_padID);
        args.add(_pinID);

        _codeStream.clear();
        _codeStream.append("Event* temp;" + _eol);
        _codeStream.append("saveState();" + _eol);
        _codeStream.append("stackedModelTagIndex++;" + _eol);
        _codeStream.append("if (stackedModelTagIndex > MAX_EVENTS) {" + _eol);
        _codeStream
                .append("die(\"MAX_EVENTS too small for stackedModelTagIndex\");"
                        + _eol);
        _codeStream.append("}" + _eol);
        _codeStream
                .append("executingModelTag[stackedModelTagIndex].microstep = currentMicrostep;"
                        + _eol);
        _codeStream
                .append("executingModelTag[stackedModelTagIndex].timestamp = currentModelTime;"
                        + _eol);
        _codeStream.append("getRealTime(&currentModelTime);" + _eol);
        _codeStream.append("currentMicrostep = 0;" + _eol);

        _codeStream.appendCodeBlock("sensingBlock", args);

        _codeStream.append("GPIOPinIntClear(GPIO_PORT" + _padID
                + "_BASE, GPIO_PIN_" + _pinID + ");" + _eol);

        _codeStream.append("temp = FREE_EVENT_LIST;" + _eol);
        _codeStream.append(args.get(0) + "();" + _eol);
        _codeStream.append("if (temp != FREE_EVENT_LIST) {" + _eol);
        _codeStream.append("addStack();" + _eol);
        _codeStream.append("} else {" + _eol);
        _codeStream
                .append("currentMicrostep = executingModelTag[stackedModelTagIndex].microstep;"
                        + _eol);
        _codeStream
                .append("currentModelTime = executingModelTag[stackedModelTagIndex].timestamp;"
                        + _eol);
        _codeStream.append("stackedModelTagIndex--;" + _eol);
        _codeStream.append("loadState();" + _eol);
        _codeStream.append("}" + _eol);

        return processCode(_codeStream.toString());
    }

    /**
     * Return the code for the hardware initialization function.
     * @return the code for the hardware initialization function.
     * @exception IllegalActionException If thrown while appending the code
     * block or processing the code stream.
     */
    @Override
    public String generateHardwareInitializationCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        List args = new ArrayList();
        args.add(_padID);
        args.add(_pinID);
        code.append(processCode(_templateParser.getCodeStream().getCodeBlock(
                "initializeGPInput", args)));
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _pinID;
    private String _padID;
}
