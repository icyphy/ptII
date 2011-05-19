/* A helper class for ptolemy.actor.lib.Test

 Copyright (c) 2005-2010 The Regents of the University of California.
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
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Test

/**
 A helper class for ptolemy.actor.lib.Test.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class Test extends CCodeGeneratorHelper {
    /**
     *  Construct a Test helper.
     *  @param actor The master Test actor.
     */
    public Test(ptolemy.actor.lib.Test actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Test.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();

        ArrayList args = new ArrayList();
        args.add(Integer.valueOf(0));
        String multiChannel = "";
        Type inputType = actor.input.getType();
        String inputTypeString = "";
        String blockName = "Block";

        if (actor.input.getWidth() > 1) {
            // If we have multiple inputs, use different blocks
            multiChannel = "MultiChannel";
        }

        inputTypeString = codeGenType(inputType);
        if (isPrimitive(inputType)) {
            blockName = inputTypeString + blockName;
        } else {
            if (multiChannel.length() == 0
                    && (actor.input.getType() instanceof ArrayType)) {
                blockName = "Array" + blockName;
            } else {
                blockName = "Token" + blockName;
            }
            args.add(inputTypeString);
        }

        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));

            _codeStream.appendCodeBlock(blockName + multiChannel, args);
        }
        return processCode(_codeStream.toString());
    }

    /** Generate the initialize code. Declare the variable state.
     *  @return The initialize code.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();
        for (int i = 0; i < actor.input.getWidth(); i++) {
            if (!isPrimitive(actor.input.getType())) {
                // One of the channels is not primitive, so we will
                // later call TokenBlock($channel), so we define
                // toleranceToken for our use.
                _codeStream.appendCodeBlock("toleranceTokenInitBlock");
                break;
            }
        }
        return processCode(_codeStream.toString());
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

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();

        if (actor.input.getWidth() > 1) {
            ArrayList args = new ArrayList();
            args.add(Integer.valueOf(0));
            for (int i = 0; i < actor.input.getWidth(); i++) {
                args.set(0, Integer.valueOf(i));
                _codeStream.appendCodeBlock("TokenPreinitBlock", args);
            }
        }

        for (int i = 0; i < actor.input.getWidth(); i++) {
            if (!isPrimitive(actor.input.getType())) {
                // One of the channels is not primitive, so we will
                // later call TokenBlock($channel), so we define
                // toleranceToken for our use.
                _codeStream.appendCodeBlock("toleranceTokenPreinitBlock");
                break;
            }
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * Test actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the Test actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<stdio.h>");
        files.add("<math.h>");
        return files;
    }
}
