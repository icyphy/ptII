/* An adapter class for actor.lib.RandomSource

 @Copyright (c) 2010-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.data.LongToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RandomSource

/**
 * An adapter class for ptolemy.actor.lib.RandomSource.
 *
 * @author Christopher Brooks, based on Uniform.java by Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public abstract class RandomSource
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.RandomSource {
    /**
     * Construct a RandomSource helper.
     * @param actor the associated actor
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
        files.add("<stdlib.h>");
        files.add("<time.h>");
        return files;
    }

    /** Generate the initialize code. Declare the variable state.
     *  @return The initialize code.
     *  @exception IllegalActionException If thrown while generating
     *  the initialization code, while appending the code block or
     *  while converting the codeStream to a string.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        ptolemy.actor.lib.RandomSource actor = (ptolemy.actor.lib.RandomSource) getComponent();

        long seedValue = ((LongToken) (actor.seed.getToken())).longValue();

        ArrayList args = new ArrayList();

        CodeStream codeStream = _templateParser.getCodeStream();
        // FIXME: Handle privateSeed.
        if (seedValue == 0) {
            args.add(Integer.toString(actor.hashCode()));
            codeStream.appendCodeBlock("setSeedBlock0", args);
        } else { // Use fixed seed + actorName.hashCode().
            args.add(Integer.toString(actor.getFullName().hashCode()));
            codeStream.appendCodeBlock("setSeedBlock1", args);
        }

        code.append(processCode(codeStream.toString()));
        return code.toString();
    }

    /**
     * Get shared code.  This method reads the
     * <code>sharedBlock</code> from Expression.c, replaces macros
     * with their values and returns the processed code string.
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public Set getSharedCode() throws IllegalActionException {
        Set codeBlocks = super.getSharedCode();
        return codeBlocks;
    }
}
