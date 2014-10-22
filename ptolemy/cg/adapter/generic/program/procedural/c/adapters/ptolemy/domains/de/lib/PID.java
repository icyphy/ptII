/* A adapter class for ptolemy.domains.de.lib.PID

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib;

import java.util.LinkedList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// PID

/**
 * A adapter class for ptolemy.domains.de.lib.PID.
 *
 * @author William Lucas
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */
public class PID extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a PID adapter.
     * @param actor the associated actor
     */
    public PID(ptolemy.domains.de.lib.PID actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * A function which returns the generated code from the C template
     * initialization method.
     * @return A string representing the Initialize C code for this actor
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        LinkedList args = new LinkedList();
        Parameter Kp = ((ptolemy.domains.de.lib.PID) getComponent()).Kp;
        double KpValue = ((DoubleToken) Kp.getToken()).doubleValue();
        args.add(Double.toString(KpValue));
        Parameter Ki = ((ptolemy.domains.de.lib.PID) getComponent()).Ki;
        double KiValue = ((DoubleToken) Ki.getToken()).doubleValue();
        args.add(Double.toString(KiValue));
        Parameter Kd = ((ptolemy.domains.de.lib.PID) getComponent()).Kd;
        double KdValue = ((DoubleToken) Kd.getToken()).doubleValue();
        args.add(Double.toString(KdValue));

        codeStream.appendCodeBlock("initBlock", args);
        return processCode(codeStream.toString());
    }

    /**
     * A function which returns the generated code from the C template
     * postFire method.
     * @return A string representing the postFire C code for this actor
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        LinkedList args = new LinkedList();
        if (((ptolemy.domains.de.lib.PID) getComponent()).reset
                .isOutsideConnected()) {
            codeStream.appendCodeBlock("resetConnectedBlock", args);
        }

        codeStream.appendCodeBlock("postFireBlock", args);
        return processCode(codeStream.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Generate fire code.
     * The method generates code that is executed when the <i>input</i> has a Token
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        LinkedList args = new LinkedList();
        CodeStream codeStream = _templateParser.getCodeStream();

        codeStream.appendCodeBlock("customFireBlock", args);

        return codeStream.toString();
    }
}
