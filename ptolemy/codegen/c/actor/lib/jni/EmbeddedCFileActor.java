/* Code generator helper for EmbeddedCFileActor.

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

import ptolemy.kernel.util.IllegalActionException;

/**
Code generator helper for EmbeddedCFileActor.

@author Christine Avanessians
@version $Id:
@since Ptolemy II 8.0
@see ptolemy.actor.lib.jni.EmbeddedCFileActor
@Pt.ProposedRating red (cavaness)
@Pt.AcceptedRating Red (cavaness)
*/
public class EmbeddedCFileActor extends CompiledCompositeActor {
    /** Construct the code generator helper associated with the given
     *  TypedCompositeActor.
     *  @param actor The associated actor.
     */
    public EmbeddedCFileActor(ptolemy.actor.lib.jni.EmbeddedCFileActor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A placeholder or dummy actor used in Embedded C code generation.
     *  It extends the EmbeddedActor class in the EmbeddedCActor.
     */
    public static class EmbeddedFileActor extends
            ptolemy.codegen.c.actor.lib.jni.EmbeddedCActor.EmbeddedActor {

        /** Create a EmbeddedFileActor.
         *  @param actor The associated actor.
         */
        public EmbeddedFileActor(
                ptolemy.actor.lib.jni.EmbeddedCFileActor.EmbeddedFileActor actor) {
            super(actor);
        }

        /** Before generating the shared code, call changeEmbeddedCCode (in ptolemy/actor/
         *  lib/jni/EmbeddedCFileActor)to make sure the contents of the file have been saved
         *  into the embeddedCCode parameter (in ptolmey/actor/lib/jni/EmbeddedCActor).  The
         *  embeddedCCode parameter is set right before code generation in order for the most
         *  recent revision of the file to be utilized.
         */
        public Set getSharedCode() throws IllegalActionException {

            ((ptolemy.actor.lib.jni.EmbeddedCFileActor) getComponent()
                    .getContainer()).changeEmbeddedCCode();

            return super.getSharedCode();
        }
    }

}
