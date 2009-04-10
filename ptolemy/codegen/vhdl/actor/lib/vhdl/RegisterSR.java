/* A helper class for actor.lib.RegisterSR

 @Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.vhdl.actor.lib.vhdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Precision;

/**
 * A helper class for ptolemy.actor.lib.Uniform.
 *
 * @author Vinayak Nagpal
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class RegisterSR extends VHDLCodeGeneratorHelper {
    /**
     * Construct a RegisterSR helper.
     * @param actor the associated actor
     */
    public RegisterSR(ptolemy.actor.lib.vhdl.RegisterSR actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in the <code>fireBlock</code> RegisterSR.vhdl,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters
     *  an error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ArrayList args = new ArrayList();
        ptolemy.actor.lib.vhdl.RegisterSR actor = (ptolemy.actor.lib.vhdl.RegisterSR) getComponent();
        Precision precision = new Precision(((Parameter) actor
                .getAttribute("outputPrecision")).getExpression());
        int width = precision.getNumberOfBits() - 1;

        args.add("" + width);

        _codeStream.appendCodeBlock("fireBlock", args);

        return processCode(_codeStream.toString());
    }

    /** Get the files needed by the code generated for the Concat actor.
     *  @return A set of strings that are names of the library and package.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();

        files.add("ieee.std_logic_1164.all");
        files.add("ieee.numeric_std.all");
        files.add("ieee_proposed.math_utility_pkg.all");
        files.add("ieee_proposed.fixed_pkg.all");
        return files;
    }
}
