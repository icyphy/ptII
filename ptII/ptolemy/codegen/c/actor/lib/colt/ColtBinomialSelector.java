/*
 @Copyright (c) 2005-2010 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib.colt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.colt.ColtBinomialSelector.
 *
 * @see ptolemy.actor.lib.colt.ColtBinomialSelector
 * @author Teale Fristoe
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red
 * @Pt.AcceptedRating
 *
 */
public class ColtBinomialSelector extends ColtRandomSource {
    /**
     * Constructor method for the ColtBinomialSelector helper.
     * @param actor the associated actor
     */
    public ColtBinomialSelector(
            ptolemy.actor.lib.colt.ColtBinomialSelector actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate shared code.
     * Read from ColtBinomialSelector.c, replace macros with their values and
     * return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set getSharedCode() throws IllegalActionException {
        // LinkedHashSet gives order to the insertion. The order of code block
        // is important here because binomialBlock uses code from the other
        // shared code blocks.
        Set sharedCode = new LinkedHashSet();
        sharedCode.addAll(super.getSharedCode());

        // binomialBlock is from the RandomSource parent class.
        sharedCode.add(_generateBlockCode("binomialBlock"));
        return sharedCode;
    }

    /**
     * Generate the preinitialize code. Declare temporary variables.
     * @return The preinitialize code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        // Automatically append the "preinitBlock" by default.
        super.generatePreinitializeCode();

        _codeStream.appendCodeBlock("preinitBinomialSelectorBlock");

        ptolemy.actor.lib.colt.ColtBinomialSelector actor = (ptolemy.actor.lib.colt.ColtBinomialSelector) getComponent();

        ArrayList args = new ArrayList();
        args.add(Integer.valueOf(0));
        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            _codeStream.appendCodeBlock("preinitBinomialSelectorArraysBlock",
                    args);
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * ColtBinomialSelector actor.
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the ColtBinomialSelector actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for producing new random numbers.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return The code that produces a new random number.
     */
    protected String _generateRandomNumber() throws IllegalActionException {
        _codeStream.clear();

        ptolemy.actor.lib.colt.ColtBinomialSelector actor = (ptolemy.actor.lib.colt.ColtBinomialSelector) getComponent();

        ArrayList args = new ArrayList();
        args.add(Integer.valueOf(0));
        // StringBuffer code = new StringBuffer();

        // code.append(_generateBlockCode("initBinomialSelectorBlock"));
        _codeStream.appendCodeBlock("initBinomialSelectorBlock");

        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            // code.append(_generateBlockCode("initArraysBinomialSelectorBlock", args));
            _codeStream
                    .appendCodeBlock("initArraysBinomialSelectorBlock", args);
        }

        args.set(0, Integer.valueOf(actor.populations.getWidth()));
        _codeStream.appendCodeBlock("updateStateVariables", args);

        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            // code.append(_generateBlockCode("binomialSelectorBlock", args));
            _codeStream.appendCodeBlock("binomialSelectorBlock", args);
            if (i < actor.output.getWidth()) {
                _codeStream.appendCodeBlock("fireBlock", args);
            }
        }

        _codeStream.append("}");

        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));

            if (i < actor.output.getWidth()) {
                _codeStream.appendCodeBlock("fireBlock", args);
            }
        }

        // return processCode(code.toString());
        return processCode(_codeStream.toString());
    }

}
