/* A helper class for ptolemy.actor.lib.colt.ColtBinomialSelector.

 @Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.colt;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.kernel.util.IllegalActionException;

/**
 * An adapter class for ptolemy.actor.lib.colt.ColtBinomialSelector.
 *
 * @see ptolemy.actor.lib.colt.ColtBinomialSelector
 * @author Christopher Brooks, based on codegen ColtBinomialSelector by Teale Fristoe
 * @version $Id$
 * @since Ptolemy II 10.0
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
     * Generate the preinitialize code. Declare temporary variables.
     * @return The preinitialize code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        // Automatically append the "preinitBlock" by default.
        super.generatePreinitializeCode();

        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.appendCodeBlock("preinitBinomialSelectorBlock");

        ptolemy.actor.lib.colt.ColtBinomialSelector actor = (ptolemy.actor.lib.colt.ColtBinomialSelector) getComponent();

        ArrayList<String> args = new ArrayList<String>();
        args.add(Integer.toString(0));
        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            codeStream.appendCodeBlock("preinitBinomialSelectorArraysBlock",
                    args);
        }

        return processCode(codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * ColtBinomialSelector actor.
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the ColtBinomialSelector actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("cern.jet.random.Binomial;");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for producing new random numbers.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return The code that produces a new random number.
     */
    @Override
    protected String _generateRandomNumber() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        ptolemy.actor.lib.colt.ColtBinomialSelector actor = (ptolemy.actor.lib.colt.ColtBinomialSelector) getComponent();

        ArrayList<String> args = new ArrayList<String>();
        args.add(Integer.toString(0));
        // StringBuffer code = new StringBuffer();

        // code.append(_generateBlockCode("initBinomialSelectorBlock"));
        codeStream.appendCodeBlock("initBinomialSelectorBlock");

        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            // code.append(_generateBlockCode("initArraysBinomialSelectorBlock", args));
            codeStream.appendCodeBlock("initArraysBinomialSelectorBlock", args);
        }

        args.set(0, Integer.toString(actor.populations.getWidth()));
        codeStream.appendCodeBlock("updateStateVariables", args);

        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            // code.append(_generateBlockCode("binomialSelectorBlock", args));
            codeStream.appendCodeBlock("binomialSelectorBlock", args);

            if (i < actor.output.getWidth()) {
                codeStream.appendCodeBlock("fireBlock", args);
            }
        }
        codeStream.append("}");

        for (int i = 0; i < actor.populations.getWidth(); i++) {
            args.set(0, Integer.toString(i));

            if (i < actor.output.getWidth()) {
                codeStream.appendCodeBlock("fireBlock", args);
            }
        }
        // return processCode(code.toString());
        return processCode(codeStream.toString());
    }

}
