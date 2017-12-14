/* An adapter class for actor.lib.Bernoulli

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
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Bernoulli

/**
 * An adapter class for ptolemy.actor.lib.Bernoulli.
 *
 * @author Christopher Brooks, based on Uniform.java by Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Bernoulli extends RandomSource {
    /**
     * Construct a Bernoulli helper.
     * @param actor the associated actor
     */
    public Bernoulli(ptolemy.actor.lib.Bernoulli actor) {
        super(actor);
    }

    /**
     * Generate shared code.
     * Read from Bernoulli.c, replace macros with their values and
     * return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public Set getSharedCode() throws IllegalActionException {
        // LinkedHashSet gives order to the insertion. The order of code block
        // is important here because gaussianBlock uses code from the other
        // shared code blocks.
        Set sharedCode = new LinkedHashSet();
        sharedCode.addAll(super.getSharedCode());

        // gaussianBlock is from the RandomSource parent class.
        ArrayList<String> args = new ArrayList<String>();
        sharedCode.add(
                getTemplateParser().generateBlockCode("gaussianBlock", args));
        return sharedCode;
    }

    /**
     * Get the files needed by the code generated for the
     * Bernoulli actor.
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the Bernoulli actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for producing a new random number.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected String _generateRandomNumber() throws IllegalActionException {
        ArrayList<String> args = new ArrayList<String>();
        return getTemplateParser().generateBlockCode("randomBlock", args);
    }

}
