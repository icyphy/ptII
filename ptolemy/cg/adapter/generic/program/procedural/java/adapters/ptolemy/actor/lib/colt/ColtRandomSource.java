/* A helper class for ptolemy.actor.lib.colt.ColtRandomSource

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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.colt;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ColtRandomSource

/**
 A helper class for ptolemy.actor.lib.colt.ColtRandomSource.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class ColtRandomSource extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct the ColtRandomSource adapter.
     *  @param actor the associated actor.
     */
    public ColtRandomSource(ptolemy.actor.lib.colt.ColtRandomSource actor) {
        super(actor);
    }

    /** Generate fire code.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());
        code.append(_generateRandomNumber());
        return processCode(code.toString());
    }

    /** Generate the code for initializing the random number generator
     *  with the seed, if it has been given.  A seed of zero is interpreted
     *  to mean that no seed is specified.  In such cases, a seed based on
     *  the current time and this instance of a ColtRandomSource is used to be
     *  fairly sure that two identical sequences will not be returned.
     *  @return The initialize code of this actor.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.colt.ColtRandomSource actor = (ptolemy.actor.lib.colt.ColtRandomSource) getComponent();

        long seedValue = ((LongToken) actor.seed.getToken()).longValue();

        ArrayList<String> args = new ArrayList<String>();
        CodeStream codeStream = _templateParser.getCodeStream();
        if (seedValue == 0) {
            args.add(Long.toString(actor.hashCode()));
            codeStream.appendCodeBlock("setSeedBlock0", args);
        } else { // Use fixed seed + actorDisplayName.hashCode().
            // BTW - the reason to use the full name here is so that
            // each random number generator generates a sequence
            // of different random numbers.  If we use just the
            // display name, then two actors that have the same
            // name will generate the same sequence of numbers which
            // is bad for Monte Carlo simulations.
            args.add(Long.toString(actor.getFullName().hashCode()));
            codeStream.appendCodeBlock("setSeedBlock1", args);
        }

        String generatorClassValue = ((StringToken) actor.generatorClass
                .getToken()).stringValue();
        if (generatorClassValue == null || generatorClassValue.equals("DRand")) {
            codeStream.appendCodeBlock("setRandomNumberGeneratorDRand");
        } else if (generatorClassValue.equals("MersenneTwister (MT19937)")) {
            codeStream.appendCodeBlock("setRandomNumberMersenneTwister");
        } else if (generatorClassValue.equals("Ranecu")) {
            codeStream.appendCodeBlock("setRandomNumberRanecu");
        } else if (generatorClassValue.equals("Ranlux")) {
            codeStream.appendCodeBlock("setRandomNumberRanlux");
        } else if (generatorClassValue.equals("Ranmar")) {
            codeStream.appendCodeBlock("setRandomNumberRanmar");
        }

        return processCode(codeStream.toString());
    }

    /** Get the files needed by the code generated for the ColtRandomSource actor.
     *  @return A set of strings that are names of the files
     *  needed by the code generated for the ColtRandomSource actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("cern.jet.random.engine.DRand;");
        files.add("cern.jet.random.engine.MersenneTwister;");
        files.add("edu.cornell.lassp.houle.RngPack.RandomElement;");
        files.add("edu.cornell.lassp.houle.RngPack.Ranecu;");
        files.add("edu.cornell.lassp.houle.RngPack.Ranlux;");
        files.add("edu.cornell.lassp.houle.RngPack.Ranmar;");

        ((ProceduralCodeGenerator) getCodeGenerator())
        .addLibraryIfNecessary("$(PTII)/lib/ptcolt.jar");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for producing a new random number.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return The code that produces a new random number.
     */
    protected abstract String _generateRandomNumber()
            throws IllegalActionException;
}
