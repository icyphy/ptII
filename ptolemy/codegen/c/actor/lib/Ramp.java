/* A helper class for ptolemy.actor.lib.Ramp
 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Ramp

/**
 A helper class for ptolemy.actor.lib.Ramp.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (cxh) Complex, Fix, Matrix and Array inputs are not supported
 @Pt.AcceptedRating Red (mankit)
 */
public class Ramp extends CCodeGeneratorHelper {
    /** Constructor method for the Ramp helper.
     *  @param actor the associated actor
     */
    public Ramp(ptolemy.actor.lib.Ramp actor) {
        super(actor);
    }

    /** Generate the preinitialize code. Declare the variable state.
     *  @return The preinitialize code.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) getComponent();

        ArrayList args = new ArrayList();
        args.add(codeGenType(actor.output.getType()));
        
        if (actor.output.getType() == BaseType.STRING) {
            _codeStream.appendCodeBlock("StringInitBlock");            
        } else {
            _codeStream.appendCodeBlock("CommonInitBlock", args);                        
        }
        
        return processCode(_codeStream.toString());
    }

    /** Generate the preinitialize code. Declare the variable state.
     *  @return The preinitialize code.
     *  @exception IllegalActionException
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) getComponent();

        ArrayList args = new ArrayList();
        args.add(cType(actor.output.getType()));

        _codeStream.appendCodeBlock("preinitBlock", args);
        return processCode(_codeStream.toString());
    }

    /**
     * Generate fire code for the Ramp actor.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) getComponent();

        String type = codeGenType(actor.output.getType());
        if (!isPrimitive(type)) {
            type = "Token";
        }

        _codeStream.appendCodeBlock(type + "FireBlock");
        return processCode(_codeStream.toString());
    }
}
