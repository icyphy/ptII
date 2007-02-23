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
package ptolemy.codegen.vhdl.actor.lib.vhdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.data.IntToken;
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
public class AddSubtract extends VHDLCodeGeneratorHelper {
    /**
     * Construct a FixConst helper.
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.vhdl.AddSubtract actor) {
        super(actor);
    }

    /**
     * 
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        _codeStream.clear();

        ptolemy.actor.lib.vhdl.AddSubtract actor = (ptolemy.actor.lib.vhdl.AddSubtract) getComponent();

        int latencyValue = ((IntToken) actor.latency.getToken()).intValue();

        boolean isAdd = actor.operation.getExpression().equals("ADD");

        Precision precisionA = _getSourcePortPrecision(actor.A);
        Precision precisionB = _getSourcePortPrecision(actor.B);

        if (precisionA.isSigned() != precisionB.isSigned()) {
            throw new IllegalActionException(this,
                    "VHDL Adder does not support "
                            + "operation of different sign.");
        }

        boolean signed = precisionA.isSigned();

        ArrayList args = new ArrayList();
        String operation = "pt_";

        operation += (signed) ? "sfixed_" : "ufixed_";
        operation += (isAdd) ? "add2" : "sub2";
        operation += (latencyValue == 0) ? "_lat0" : "";

        args.add(operation);

        if (latencyValue == 0) {
            _codeStream.appendCodeBlock("sharedBlock_lat0", args);
        } else {
            _codeStream.appendCodeBlock("sharedBlock", args);
        }

        sharedCode.add(processCode(_codeStream.toString()));
        return sharedCode;
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

        ptolemy.actor.lib.vhdl.AddSubtract actor = (ptolemy.actor.lib.vhdl.AddSubtract) getComponent();

        int latencyValue = ((IntToken) actor.latency.getToken()).intValue();

        boolean isAdd = actor.operation.getExpression().equals("ADD");

        Precision precisionA = _getSourcePortPrecision(actor.A);
        Precision precisionB = _getSourcePortPrecision(actor.B);

        boolean signed = precisionA.isSigned();

        ArrayList args = new ArrayList();
        String operation = "pt_";

        operation += (signed) ? "sfixed_" : "ufixed_";
        operation += (isAdd) ? "add2" : "sub2";
        operation += (latencyValue == 0) ? "_lat0" : "";

        args.add(operation);

        int highA = precisionA.getIntegerBitLength() - 1;
        int lowA = -precisionA.getFractionBitLength();

        int highB = precisionB.getIntegerBitLength() - 1;
        int lowB = -precisionB.getFractionBitLength();

        Precision outputPrecision = new Precision(
                _getPortPrecision(actor.output));

        int highO = outputPrecision.getIntegerBitLength() - 1;
        int lowO = -outputPrecision.getFractionBitLength();

        args.add("" + highA);
        args.add("" + lowA);
        args.add("" + highB);
        args.add("" + lowB);
        args.add("" + highO);
        args.add("" + lowO);
        if (((IntToken) actor.latency.getToken()).intValue() > 0) {
            args.add("," + _eol + "LATENCY =>" + actor.latency.getExpression()
                    + "," + _eol + "RESET_ACTIVE_VALUE => '0'");
            args.add("," + _eol + "clk => clk," + _eol + "reset => reset");
        } else {
            args.add("");
            args.add("");
        }

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
