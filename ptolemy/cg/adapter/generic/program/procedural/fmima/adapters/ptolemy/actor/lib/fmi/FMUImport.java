/* An adapter class for ptolemy.actor.lib.fmi.FMUImport

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor.lib.fmi;

import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FMUImport

/**
 An adapter class for ptolemy.actor.lib.fmi.FMUImport.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends FMIMACodeGeneratorAdapter {
    /**
     *  Construct the FMUImport adapter.
     *  @param actor the associated actor
     */
    public FMUImport(ptolemy.actor.lib.fmi.FMUImport actor) {
        super(actor);
    }

    /** Generate FMIMA code.
     *  @return The generated FMIMA.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating FMIMA for the director.
     */
    @Override
    public String generateFMIMA() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) getComponent();
        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/lib/fmi/FMUImport.java "
                                + _eol
                                + "   "
                                + actor.getName()
                                + " is a FMUImport: "));
        for (TypedIOPort input : actor.inputPortList()) {
            code.append(getCodeGenerator().comment(" input " + input.getName()));
            if (input.getName().equals("inc20RC1pt")) {
                code.append("connections[1].sourceFMU = &fmus[1];\n"
                        + "connections[1].sourcePort = getValueReference(getScalarVariable(fmus[1].modelDescription, 1));\n"
                        + "connections[1].sourceType = fmi_Real;\n"
                        + "connections[1].sinkFMU = &fmus[2];\n"
                        + "connections[1].sinkPort = getValueReference(getScalarVariable(fmus[2].modelDescription, 0));\n"
                        + "connections[1].sinkType = fmi_Real;\n");
            }
        }

        for (TypedIOPort output : actor.outputPortList()) {
            code.append(getCodeGenerator().comment(
                    " output " + output.getName()));
            if (output.getName().equals("inc20RC1pt")) {
                code.append("connections[0].sourceFMU = &fmus[0];\n"
                        + "connections[0].sourcePort = getValueReference(getScalarVariable(fmus[0].modelDescription, 0));\n"
                        + "connections[0].sourceType = fmi_Integer;\n"
                        + "connections[0].sinkFMU = &fmus[1];\n"
                        + "connections[0].sinkPort = getValueReference(getScalarVariable(fmus[1].modelDescription, 0));\n"
                        + "connections[0].sinkType = fmi_Real;\n");
            }

        }

        return /*processCode(code.toString())*/code.toString();
    }

}
