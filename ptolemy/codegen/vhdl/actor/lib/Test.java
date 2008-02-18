/* A helper class for actor.lib.FixConst

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
package ptolemy.codegen.vhdl.actor.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.data.ArrayToken;
import ptolemy.data.FixToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Precision;

/**
 * A helper class for ptolemy.actor.lib.Uniform.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Test extends VHDLCodeGeneratorHelper {
    /**
     * Construct a FixConst helper.
     * @param actor the associated actor
     */
    public Test(ptolemy.actor.lib.Test actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in the <code>fireBlock</code> from FixConst.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters
     *  an error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();

        // FIXME: we should implement this later.
        if (actor.input.getWidth() > 1) {
            throw new IllegalActionException(this,
                    "Code generation does not support multiport connections.");
        }

        ArrayList args = new ArrayList();

        Precision precision = _getSourcePortPrecision(actor.input);

        int high = precision.getIntegerBitLength() - 1;
        int low = -precision.getFractionBitLength();

        args.add(Integer.valueOf(high));
        args.add(Integer.valueOf(low));

        ArrayToken valueArray = (ArrayToken) actor.correctValues.getToken();

        int i;
        StringBuffer values = new StringBuffer();
        for (i = 0; i < valueArray.length() - 1; i++) {
            values.append(((FixToken) valueArray.getElement(i))
                    .convertToDouble()
                    + ", ");
        }
        values.append(((FixToken) valueArray.getElement(i)).convertToDouble());

        args.add(values.toString());

        String signed = (precision.isSigned()) ? "SIGNED_TYPE"
                : "UNSIGNED_TYPE";

        args.add(signed);

        _codeStream.appendCodeBlock("fireBlock", args);

        return processCode(_codeStream.toString());
    }

    /** Get the files needed by the code generated for the FixConst actor.
     *  @return A set of strings that are names of the library and package.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();

        files.add("ieee.std_logic_1164.all");
        files.add("ieee.numeric_std.all");
        files.add("ieee_proposed.math_utility_pkg.all");
        files.add("ieee_proposed.fixed_pkg.all");
        files.add("work.pt_utility.all");
        return files;
    }
}
