/* An adapter class for ptolemy.actor.lib.RandomSource

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib;

import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// RandomSource

/**
 An adapter class for ptolemy.actor.lib.RandomSource.

 @author Christopher Brooks, based on codegen RandomSource byGang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class RandomSource
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.RandomSource {
    /**
     *  Construct the RandomSource helper.
     *  @param actor the associated actor.
     */
    public RandomSource(ptolemy.actor.lib.RandomSource actor) {
        super(actor);
    }

    /** Get the files needed by the code generated for the RandomSource actor.
     *  @return A set of strings that are names of the files
     *  needed by the code generated for the RandomSource actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("java.util.Random;");
        return files;
    }
}
