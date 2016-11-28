/* An adapter class for org.terraswarm.accessor.JSAccessor

 Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.org.terraswarm.accessor;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// JSAccessor

/**
 An adapter class for org.terraswarm.accessor.JSAccessor.

 @author Jia Zou, based on JSAccessor.java by Gang Zhou, Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (jiazou)
 @Pt.AcceptedRating Red (jiazou)
 */
public class JSAccessor extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct the JSAccessor adapter.
     *  @param actor the associated actor
     */
    public JSAccessor(org.terraswarm.accessor.JSAccessor actor) {
        super(actor);
        System.out.println("###### ctor: ptolemy/cg/adapter/generic/program/procedural/adapters/org/terraswarm/accessor/JSAccessor.java");
    }

    /** Generate the initialize code. Declare the variable state.
     *  @return The initialize code.
     *  @exception IllegalActionException If thrown while generating
     *  the initialization code, while appending the code block or
     *  while converting the codeStream to a string.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        //org.terraswarm.accessor.JSAccessor actor = (org.terraswarm.accessor.JSAccessor) getComponent();

        System.out.println("###### generateInitializeCode: ptolemy/cg/adapter/generic/program/procedural/adapters/org/terraswarm/accessor/JSAccessor.java");
        CodeStream codeStream = _templateParser.getCodeStream();
        return processCode(codeStream.toString());
    }
}
