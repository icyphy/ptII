/* Code generator helper for composite actor with co-simulation option.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib.jni;

import java.util.List;

import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.StaticSchedulingCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CompiledCompositeActor

/**
 Code generator helper for composite actor with co-simulation option.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class CompiledCompositeActor extends TypedCompositeActor {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public CompiledCompositeActor(
            ptolemy.actor.lib.jni.CompiledCompositeActor component) {
        super(component);
    }
    
    /** Generate code for a given actor.
     *  @param compositeActor The actor for which code is generated.
     *  @exception IllegalActionException If there are problems
     *  accessing the actor.
     */   
    public static void generateCode(
            ptolemy.actor.TypedCompositeActor compositeActor) 
            throws IllegalActionException {
        
        ptolemy.actor.lib.jni.CompiledCompositeActor actor 
                = (ptolemy.actor.lib.jni.CompiledCompositeActor) compositeActor; 

        List codeGenerators = actor.attributeList(CodeGenerator.class);

        CodeGenerator codeGenerator = null;
        try {
            if (codeGenerators.size() == 0) {
                // Add a codeGenerator
                codeGenerator = new StaticSchedulingCodeGenerator(actor,
                        "CodeGenerator_AutoAdded");
            } else {
                // Get the last CodeGenerator in the list, maybe
                // it was added last?
                codeGenerator = (CodeGenerator) codeGenerators
                        .get(codeGenerators.size() - 1);
            }

            codeGenerator.codeDirectory.setExpression(actor.codeDirectory
                    .getExpression());

            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            codeGenerator.codeDirectory
                    .setBaseDirectory(codeGenerator.codeDirectory.asFile()
                            .toURI());

            codeGenerator.generatorPackage.setExpression(actor.generatorPackage
                    .getExpression());

            codeGenerator.inline.setExpression(actor.inline.getExpression());

            codeGenerator.overwriteFiles.setExpression(actor.overwriteFiles
                    .getExpression());

        } catch (NameDuplicationException e) {
            throw new IllegalActionException(actor, e, "Name duplication.");
        }

        try {
            codeGenerator.generateCode();
        } catch (KernelException e) {
            throw new IllegalActionException(actor, e,
                    "Failed to generate code.");
        }
    }

    /** Do nothing. Since the outside domain is the simulation domain. 
     *  @exception IllegalActionException Not thrown here.
     */
//     protected void _createInputBufferSizeAndOffsetMap()
//             throws IllegalActionException {
//     }

    /** Return nothing. Since the outside domain is the simulation domain. 
     *  @exception IllegalActionException Not thrown here.
     */
//     protected String _generateInputVariableDeclaration()
//             throws IllegalActionException {
//         return "";
//     }
}
