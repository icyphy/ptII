/* Base class for C code generator helper.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.kernel;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// CCodeGeneratorHelper

/**
 Base class for C code generator helper. 
 <p>Actor helpers extend this class and optionally define
 generateFireCode(), generateInitializeCode(), generatePreinitializeCode(),
 and generateWrapupCode() methods.  In derived classes, these methods,
 if present, make actor specific changes to the corresponding code.
 If these methods are not present, then the parent class will automatically
 read the corresponding .c file and subsitute in the corresponding code
 block.  For example, generateInitializeCode() reads the
 <code>initBlock</code>, processes the macros and adds the resulting
 code block to the output.
 <p>For a complete list of methods to define, see 
 {@link ptolemy.codegen.kernel.CodeGeneratorHelper}.
 
 @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou
 @version $Id$
 @since Ptolemy II 6.0
o @Pt.ProposedRating Red (cxh) Remove mostly empty methods, add info about writing actors.
 @Pt.AcceptedRating Red (cxh)
 */
public class CCodeGeneratorHelper extends CodeGeneratorHelper {
    /**
     * Create a new instance of the C code generator helper.
     * @param component The actor object for this helper.
     */
    public CCodeGeneratorHelper(NamedObj component) {
        super(component);
        _parseTreeCodeGenerator = new CParseTreeCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the main entry point.
     *  @return In this base class, return a comment.  Subclasses
     *  should return the definition of the main entry point for a program.
     *  In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {
        // FIXME: should this be moved to class called CCodeGenerator?
        return "\n\nmain(int argc, char *argv[]) {\n";
    }

    /** Generate the main entry point.
     *  @return Return a string that declares the start of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {
        // FIXME: should this be moved to class called CCodeGenerator?
        return _INDENT1 + "exit(0);\n}\n";
    }

    /** Return a new parse tree to use with expressions.
     *  @return the new parse tree to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        _parseTreeCodeGenerator = new CParseTreeCodeGenerator();
        return _parseTreeCodeGenerator;
    }
}
