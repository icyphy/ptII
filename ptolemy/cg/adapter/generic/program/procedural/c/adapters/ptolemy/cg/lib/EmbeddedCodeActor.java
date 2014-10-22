/* Code generator adapter for EmbeddedCodeActor.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.cg.lib;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// EmbeddedCodeActor

/**
 Code generator adapter for EmbeddedCodeActor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @see ptolemy.cg.lib.EmbeddedCodeActor
 @Pt.ProposedRating red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class EmbeddedCodeActor extends CompiledCompositeActor {
    /** Construct the code generator adapter associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public EmbeddedCodeActor(ptolemy.cg.lib.EmbeddedCodeActor component) {
        super(component);
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * In this implementation nothing needs to happen.
     * @param forComposite True if we are generating code for a composite.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateTypeConvertFireCode(boolean forComposite)
            throws IllegalActionException {
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A placeholder or dummy actor used in Embedded C code generation.
     */
    public static class EmbeddedActor extends NamedProgramCodeGeneratorAdapter {

        /** Create a EmbeddedActor.
         *  @param actor The associated actor.
         */
        public EmbeddedActor(
                ptolemy.cg.lib.EmbeddedCodeActor.EmbeddedActor actor) {
            super(actor);
        }

        /** Perform any setup or initialization of the adapter.
         *  Note that this is not the Ptolemy initialize() method,
         *  this method merely sets up any codegen-time variables
         *  in the adapters.  In this class, reset the gode stream and
         *  set the code blocks to the value of the embeddedCode parameter.
         *
         *  @exception IllegalActionException If an error occurs while
         *   initializing an adapter.
         */
        @Override
        public void setupAdapter() throws IllegalActionException {
            // To test this, run:
            // $PTII/bin/vergil $PTII/ptolemy/cg/lib/test/auto/Scale_c.xml
            // FIXME: One can do optimization here so that reset
            // happens only when the embedded C code is modified.
            _templateParser.getCodeStream().reset();
            _templateParser.getCodeStream().setCodeBlocks(
                    ((ptolemy.cg.lib.EmbeddedCodeActor) getComponent()
                            .getContainer()).embeddedCode.getExpression());
        }
    }
}
