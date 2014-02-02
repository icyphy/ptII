/* Code generator adapter for MultiCompositeActor actor.

 Copyright (c) 2005-2011 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.hoc;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// MultiCompositeActor

/**
 Code generator adapter for MultiCompositeActor actor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (zgang)
 @Pt.AcceptedRating Green (cxh)
 */
public class MultiCompositeActor extends TypedCompositeActor {
    /** Construct the code generator adapter associated
     *  with the given MultiCompositeActor actor.
     *  @param component The associated component.
     */
    public MultiCompositeActor(
            ptolemy.actor.lib.hoc.MultiCompositeActor component) {
        super(component);
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                super.generateVariableDeclaration());
        //ptolemy.actor.lib.hoc.Case container = (ptolemy.actor.lib.hoc.Case) getComponent()
        //        .getContainer();
        Iterator refinements = /*container.*/((CompositeActor) getComponent())
                .deepEntityList().iterator();
        while (refinements.hasNext()) {
            CompositeActor refinement = (CompositeActor) refinements.next();
            code.append(getCodeGenerator().comment(
                    "Case Variable Declarations for "
                            + refinement.getFullName()));
            //             Director director = refinement.getDirector();
            //             NamedProgramCodeGeneratorAdapter refinementAdapter = (NamedProgramCodeGeneratorAdapter)getCodeGenerator().getAdapter(director);
            //             code.append(refinementAdapter.generateVariableDeclaration());
            //NamedProgramCodeGeneratorAdapter refinementAdapter = (NamedProgramCodeGeneratorAdapter)getCodeGenerator().getAdapter(refinement);
            //code.append(refinementAdapter.generateVariableDeclaration());
        }
        //         code.append(((NamedProgramCodeGeneratorAdapter)getCodeGenerator().getAdapter(
        //                                 ((CompositeActor)getComponent()).getDirector()))
        //                 .generateVariableDeclaration());

        Iterator<?> ports = ((ptolemy.kernel.Entity) getComponent()).portList()
                .iterator();
        while (ports.hasNext()) {
            ptolemy.actor.TypedIOPort port = (ptolemy.actor.TypedIOPort) ports
                    .next();
            code.append(getCodeGenerator()
                    .comment(
                            "Case Variable Declarations for port "
                                    + port.getFullName()));
            //NamedProgramCodeGeneratorAdapter portAdapter = (NamedProgramCodeGeneratorAdapter)getCodeGenerator().getAdapter(port);
            _portVariableDeclaration(code, port);
        }
        return code.toString();
    }

    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param port The port for which the name is generated.
     * @return The sanitized name.
     * @exception IllegalActionException If there is a problem getting
     * information about the port.
     */
    public String generatePortName(TypedIOPort port)
            throws IllegalActionException {
        // FIXME: note that if we have a port that has a character that
        // is santized away, then we will run into problems if we try to
        // refer to the port by the sanitized name.
        String portName = StringUtilities.sanitizeName(port.getFullName());

        // FIXME: Assume that all objects share the same top level. In this case,
        // having the top level in the generated name does not help to
        // expand the name space but merely lengthen the name string.
        //        NamedObj parent = namedObj.toplevel();
        //        if (namedObj.toplevel() == namedObj) {
        //            return "_toplevel_";
        //        }
        //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
        if (portName.startsWith("_")) {
            portName = portName.substring(1, portName.length());
        }
        portName = TemplateParser.escapePortName(portName);

        if (!((BooleanToken) getCodeGenerator().variablesAsArrays.getToken())
                .booleanValue()) {
            return portName;
        }

        // Get the name of the port that refers to the array of all ports.
        return getCodeGenerator()
                .generatePortName(port, portName, 1 /*_ports.getBufferSize(port)*/);
    }

    private void _portVariableDeclaration(StringBuffer codeResult,
            TypedIOPort port) throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        boolean variablesAsArrays = ((BooleanToken) getCodeGenerator().variablesAsArrays
                .getToken()).booleanValue();
        if (!variablesAsArrays) {
            code.append("public static " + targetType(port.getType()) + " ");
        }
        code.append(generatePortName(port));

        int bufferSize = 1; //_ports.getBufferSize(port);

        if (port.isMultiport()) {
            if (!variablesAsArrays) {
                code.append("[]");
                // Coverity points out that buffersize is 1;
//                 if (bufferSize > 1) {
//                     code.append("[]");
//                 }
            }
            code.append(" = new " + targetType(port.getType()));
            // Coverity points out that bufferSize is 1, so the if will never be new.
//         } else {
//             if (bufferSize > 1) {
//                 if (!variablesAsArrays) {
//                     code.append("[]");
//                 }
//                 code.append(" = new " + targetType(port.getType()));
//             } else {
//                 //code.append(" = ");
//             }
        }

        if (port.isMultiport()) {
            code.append("["
                    + java.lang.Math.max(port.getWidth(), port.getWidthInside())
                    + "]");
        }

        // Coverity points out the bufferSize is 1.
//         if (bufferSize > 1) {
//             code.append("[" + bufferSize + "]");
//         } else {
//             //code.append("0");
//         }
        code.append(";" + _eol);
        if (variablesAsArrays) {
            if (code.toString().indexOf("=") != -1) {
                codeResult.append(code);
            }
        } else {
            codeResult.append(code);
        }
    }

}
