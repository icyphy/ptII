/* A helper class for ptolemy.actor.lib.Test
 Copyright (c) 2005 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Test

/**
 A helper class for ptolemy.actor.lib.Test.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Test extends CCodeGeneratorHelper {
    /** Construct a C code generator Test actor that contains
     *  a standard actor.lib.Test actor.
     *  @param actor The master Test actor.
     */
    public Test(ptolemy.actor.lib.Test actor) {
        super(actor);
        _testActor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Test.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        // FIXME: handle widths greater than 1.
        if (_testActor.input.getWidth() > 1) {
            throw new IllegalActionException(_testActor,
                    "The C version of the Test actor currently only handles "
                            + "inputs of width 1.  The width of input was: "
                            + _testActor.input.getWidth());
        }

        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("fireBlock");

        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from Test.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("preinitBlock");
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * Test actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the Test actor.
     */
    public Set getHeaderFiles() {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        files.add("\"math.h\"");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Original type polymorphic Test actor. */
    private ptolemy.actor.lib.Test _testActor;
}
