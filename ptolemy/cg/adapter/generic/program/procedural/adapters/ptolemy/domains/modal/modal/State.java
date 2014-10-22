/* Code generator helper for modal controller.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////State

/**
Code generator helper for modal controller.

@author  Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */
public class State extends NamedProgramCodeGeneratorAdapter {

    /** Construct the code generator adapter associated with the given
     *  state.
     *  @param component The associated
     *  ptolemy.domains.modal.kernel.State
     */
    public State(ptolemy.domains.modal.kernel.State component) {
        super(component);

    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(getCodeGenerator().comment(
                "State: Transfer tokens to the outside"));

        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {
                String name = outputPort.getName();

                if (outputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                code.append(name + " = ");
                code.append("@" + name);
                code.append(";" + _eol);
                //code.append(_compositeActorHelper.get.getReference(name) + " = ");
                //code.append(_compositeActorHelper.getReference("@" + name));
                //code.append(";" + _eol);
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, 1);
    }

    /** Update the read offsets of the buffer associated with the given port.
     *
     *  @param port The port whose read offset is to be updated.
     *  @param code The string buffer that the generated code is appended to.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    protected void _updatePortOffset(IOPort port, StringBuffer code, int rate)
            throws IllegalActionException {
        if (rate == 0) {
            return;
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }

        NamedProgramCodeGeneratorAdapter portHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(port);
        String str;
        Receiver rec[][] = port.getRemoteReceivers();

        for (Receiver[] element : rec) {
            for (int j = 0; j < element.length; j++) {
                str = element[j].toString();
                str = str.substring(str.indexOf("{") + 2, str.lastIndexOf("."));
                str = str.replace('.', '_');

                code.append(str + " = ");
            }
        }

        code.append(portHelper.getDisplayName() + ";" + _eol);
    }

}
