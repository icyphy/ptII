/* Code generator helper for EmbeddedCActor.

 Copyright (c) 2007-2010 The Regents of the University of California.
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

import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// EmbeddedCActor

/**
 Code generator helper for EmbeddedCActor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.1
 @see ptolemy.actor.lib.jni.EmbeddedCActor
 @Pt.ProposedRating red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class EmbeddedCActor extends CompiledCompositeActor {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public EmbeddedCActor(ptolemy.actor.lib.jni.EmbeddedCActor component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A placeholder or dummy actor used in Embedded C code generation.
     */
    public static class EmbeddedActor extends CCodeGeneratorHelper {

        /** Create a EmbeddedActor.
         *  @param actor The associated actor.
         */
        public EmbeddedActor(
                ptolemy.actor.lib.jni.EmbeddedCActor.EmbeddedActor actor) {
            super(actor);
        }

        /** Generate the shared code. Since this is the first generate
         *  method invoked out of all, the CodeStream object is reset
         *  so that its code table will be re-constructed.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        public Set getSharedCode() throws IllegalActionException {
            // FIXME: One can do optimization here so that reset
            // happens only when the embedded C code is modified.
            _codeStream.reset();
            _codeStream
                    .setCodeBlocks(((ptolemy.actor.lib.jni.EmbeddedCActor) getComponent()
                            .getContainer()).embeddedCCode.getExpression());
            return super.getSharedCode();
        }
    }
}
