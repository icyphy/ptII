/* Base class for code generator adapter.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic;

import java.util.LinkedList;
import java.util.List;

import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.kernel.util.DecoratedAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////////
////ProgramCodeGeneratorAdapterStrategy

/**
* FIXME: Overhaul comments.
* Base class for code generator adapter.
*
* <p>Subclasses should override generateFireCode(),
* generateInitializeCode() generatePostfireCode(),
* generatePreinitializeCode(), and generateWrapupCode() methods by
* appending a corresponding code block.
*
* <p>Subclasses should be sure to properly indent the code by
* either using the code block functionality in methods like
* _generateBlockCode(String) or by calling
* {@link ptolemy.codegen.kernel.CodeStream#indent(String)},
* for example:
* <pre>
*     StringBuffer code = new StringBuffer();
*     code.append(super.generateWrapupCode());
*     code.append("// Local wrapup code");
*     return processCode(CodeStream.indent(code.toString()));
* </pre>
*
* @author Bert Rodiers
* @version $Id$
* @since Ptolemy II 7.1
* @Pt.ProposedRating Red (rodiers)
* @Pt.AcceptedRating Red (rodiers)
*/
//FIXME: Why extend NamedObj? Extend Attribute and store in the actor being adapted?
abstract public class CodeGeneratorAdapter extends NamedObj {

    /** Create and return the decorated attributes for the corresponding Ptolemy Component.
     *  @param genericCodeGenerator The code generator that is the decorator for the
     *  corresponding Ptolemy Component.
     *  @return The decorated attributes.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public List<DecoratedAttribute> createDecoratedAttributes(
            GenericCodeGenerator genericCodeGenerator)
            throws IllegalActionException, NameDuplicationException {
        return new LinkedList<DecoratedAttribute>();
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    abstract public ProgramCodeGenerator getCodeGenerator();

    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    abstract public void setCodeGenerator(GenericCodeGenerator codeGenerator);

    /** Set the strategy for generating code for this adapter.
     * @param strategy The strategy.
     */
    public void setStrategy(Object strategy) {
    }
}
