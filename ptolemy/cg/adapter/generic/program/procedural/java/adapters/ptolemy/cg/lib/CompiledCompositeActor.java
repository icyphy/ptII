/* Code generator  for composite actor with co-simulation option.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.cg.lib;

import java.io.IOException;
import java.util.List;

import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringBufferExec;

///////////////////////////////////////////////////////////////////
//// CompiledCompositeActor

/**
 Code generator  for a composite actor that contains an actor
 with a body written in Java.

 @author Gang Zhou, Christopher Brooks, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class CompiledCompositeActor extends TypedCompositeActor {
    /** Construct the code generator  associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public CompiledCompositeActor(
            ptolemy.cg.lib.CompiledCompositeActor component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If necessary, copy files from the fileDependencies code block.
     *  @param compositeActor  The compositeActor, which is usually
     *  an EmbeddedJavaActor.
     *  @return True if a file was copied.
     *  @exception IOException If there is a problem reading the
     *  <i>codeDirectory</i> parameter.
     *  @exception IllegalActionException If there is a problem reading the
     *  <i>codeDirectory</i> parameter.
     *  @see #copyFilesToCodeDirectory(NamedObj, ProgramCodeGenerator)
     */
    public static long copyFilesToCodeDirectory(
            ptolemy.actor.TypedCompositeActor compositeActor)
                    throws IOException, IllegalActionException {
        // This is static so that ptolemy.cg.lib.CompiledCompositeActor
        // will not depend on ptolemy.codegen.
        ProgramCodeGenerator codeGenerator = _getCodeGenerator(compositeActor);
        return NamedProgramCodeGeneratorAdapter.copyFilesToCodeDirectory(
                compositeActor, codeGenerator);
    }

    /** Generate code for a given actor.
     *  @param compositeActor The actor for which code is generated.
     *  @exception IllegalActionException If there are problems
     *  accessing the actor.
     */
    public static void generateCode(
            ptolemy.actor.TypedCompositeActor compositeActor)
                    throws IllegalActionException {

        // This is static so that ptolemy.cg.lib.CompiledCompositeActor
        // will not depend on ptolemy.codegen.

        // FindBugs wants this.
        if (!(compositeActor instanceof ptolemy.cg.lib.CompiledCompositeActor)) {
            throw new InternalErrorException(compositeActor, null,
                    " is not an instance of "
                            + "ptolemy.cg.lib.CompiledCompositeActor.");
        }

        ptolemy.cg.lib.CompiledCompositeActor actor = (ptolemy.cg.lib.CompiledCompositeActor) compositeActor;
        GenericCodeGenerator codeGenerator = _getCodeGenerator(compositeActor);

        // Append the output to stderr, stdout and the StringBuffer;
        final StringBufferExec executeCommands = new StringBufferExec(true);
        int returnCode = 0;
        try {
            codeGenerator.setExecuteCommands(executeCommands);
            returnCode = codeGenerator.generateCode();
        } catch (Exception e) {
            throw new IllegalActionException(actor, e,
                    "Failed to generate code.");
        }

        if (returnCode != 0) {
            // Throw outside the above try so that we don't get as many nested
            // exceptions;
            String message = "Execution of subcommands to generate code for "
                    + "CompiledCompositeActor failed, last process returned '"
                    + returnCode + "', which is not 0:\n"
                    + executeCommands.buffer.toString();
            System.err.println(message);
            throw new IllegalActionException(message);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the codeGenerator for a given actor.
     *  If there is no GenericCodeGenerator, then one is added.
     *  @param compositeActor The actor for which code is generated.
     *  @exception IllegalActionException If there are problems
     *  accessing the actor.
     */
    private static ProgramCodeGenerator _getCodeGenerator(
            ptolemy.actor.TypedCompositeActor compositeActor)
                    throws IllegalActionException {
        // This is static so that ptolemy.cg.lib.CompiledCompositeActor
        // will not depend on ptolemy.codegen
        ProgramCodeGenerator codeGenerator = null;
        try {
            // FindBugs wants this.
            if (!(compositeActor instanceof ptolemy.cg.lib.CompiledCompositeActor)) {
                throw new InternalErrorException(compositeActor, null,
                        " is not an instance of "
                                + "ptolemy.cg.lib.CompiledCompositeActor.");
            }

            ptolemy.cg.lib.CompiledCompositeActor actor = (ptolemy.cg.lib.CompiledCompositeActor) compositeActor;
            List<?> codeGenerators = actor
                    .attributeList(JavaCodeGenerator.class);

            if (codeGenerators.size() == 0) {
                // Add a codeGenerator
                codeGenerator = new JavaCodeGenerator(actor,
                        "CodeGenerator_AutoAdded");
            } else {
                // Get the last GenericCodeGenerator in the list, maybe
                // it was added last?
                codeGenerator = (ProgramCodeGenerator) codeGenerators
                        .get(codeGenerators.size() - 1);
            }

            codeGenerator.codeDirectory.setExpression(actor.codeDirectory
                    .getExpression());

            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            codeGenerator.codeDirectory
            .setBaseDirectory(codeGenerator.codeDirectory.asFile()
                    .toURI());

            codeGenerator.generatorPackageList
            .setExpression(actor.generatorPackage.getExpression());

            codeGenerator.inline.setExpression(actor.inline.getExpression());

            codeGenerator.overwriteFiles.setExpression(actor.overwriteFiles
                    .getExpression());

        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(compositeActor, ex,
                    "Name duplication.");
        }
        return codeGenerator;
    }
}
