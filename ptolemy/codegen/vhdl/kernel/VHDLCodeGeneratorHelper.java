/* Base class for VHDL code generator helper.

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
package ptolemy.codegen.vhdl.kernel;

import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.vhdl.FixTransformer;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.math.Precision;

//////////////////////////////////////////////////////////////////////////
//// VHDLCodeGeneratorHelper

/**
 Base class for VHDL code generator helper. 

 <p>Actor helpers extend this class and optionally define
 generateFireCode(), generateInitializeCode(), generatePreinitializeCode(),
 and generateWrapupCode() methods.  In derived classes, these methods,
 if present, make actor specific changes to the corresponding code.
 If these methods are not present, then the parent class will automatically
 read the corresponding .vhdl file and subsitute in the corresponding code
 block.  For example, generateInitializeCode() reads the
 <code>initBlock</code>, processes the macros and adds the resulting
 code block to the output.

 <p>For a complete list of methods to define, see 
 {@link ptolemy.codegen.kernel.CodeGeneratorHelper}.

 <p>For further details, see <code>$PTII/ptolemy/codegen/README.html</code>

 @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class VHDLCodeGeneratorHelper extends CodeGeneratorHelper {
    /**
     * Create a new instance of the VHDL code generator helper.
     * @param component The actor object for this helper.
     */
    public VHDLCodeGeneratorHelper(NamedObj component) {
        super(component, component.getName() + " VHDL");
        _parseTreeCodeGenerator = new VHDLParseTreeCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Indicate whether this helper should generate code in the 
     * current generate file. 
     * @return True if this helper is synthesizable and the current
     *  generate file is the synthesizable code file or if this helper
     *  is non-synthesizable and the current generate file is the 
     *  testbench file; Otherwise, return false. 
     * @exception IllegalActionException
     */
    public boolean doGenerate() throws IllegalActionException {
        if (isSynthesizable()) {
            return getCodeGenerator().getGenerateFile() == VHDLCodeGenerator.SYNTHESIZABLE;
        } else {
            return getCodeGenerator().getGenerateFile() == VHDLCodeGenerator.TESTBENCH;
        }
    }

    /** Get the VHDL code generator associated with this helper class.
     *  @return The VHDL code generator associated with this helper class.
     */
    public VHDLCodeGenerator getCodeGenerator() {
        return (VHDLCodeGenerator) _codeGenerator;
    }

    /** Return the reference to the specified port of the container
     *  actor. The returned string is in the form 
     *  "fullName_portName[channelNumber]", if a channel number is given.
     *  @param name The name of the given port.
     *  @return the reference to specified port.
     *  @exception IllegalActionException If the port does not exist.
     */
    public String getReference(String name) throws IllegalActionException {
        name = processCode(name);

        StringBuffer result = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if ((tokenizer.countTokens() != 1) && (tokenizer.countTokens() != 3)
                && (tokenizer.countTokens() != 5)) {
            throw new IllegalActionException(getComponent(),
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        boolean forComposite = false;

        // Usually given the name of an input port, getReference(String name) 
        // returns variable name representing the input port. Given the name 
        // of an output port, getReference(String name) returns variable names
        // representing the input ports connected to the output port. 
        // However, if the name of an input port starts with '@', 
        // getReference(String name) returns variable names representing the 
        // input ports connected to the given input port on the inside. 
        // If the name of an output port starts with '@', 
        // getReference(String name) returns variable name representing the 
        // the given output port which has inside receivers.
        // The special use of '@' is for composite actor when
        // tokens are transferred into or out of the composite actor.
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        TypedIOPort port = getPort(refName);

        String[] channelAndOffset = _getChannelAndOffset(name);

        if (port != null) {

            int channelNumber = 0;

            if (!channelAndOffset[0].equals("")) {
                channelNumber = (new Integer(channelAndOffset[0])).intValue();
            }

            // To support modal model, we need to check the following condition
            // first because an output port of a modal controller should be
            // mainly treated as an output port. However, during choice action,
            // an output port of a modal controller will receive the tokens sent
            // from the same port.  During commit action, an output port of a modal
            // controller will NOT receive the tokens sent from the same port.
            if ((port.isOutput() && !forComposite)
                    || (port.isInput() && forComposite)) {

                result.append(generateVariableName(port));
                result.append("_" + channelNumber);
                return result.toString();
            }

            // Note that if the width is 0, then we have no connection to
            // the port but the port might be a PortParameter, in which
            // case we want the Parameter.
            // codegen/c/actor/lib/string/test/auto/StringCompare3.xml
            // tests this.

            if ((port.isInput() && !forComposite && port.getWidth() > 0)
                    || (port.isOutput() && forComposite)) {

                Channel sourceChannel = getSourceChannel(port, channelNumber);

                result.append(generateVariableName(sourceChannel.port));
                result.append("_" + sourceChannel.channelNumber);
                return result.toString();
            }
        }

        throw new IllegalActionException(getComponent(),
                "Reference not found: " + name);
    }

    /**
     * Return whether or not this helper is synthesizable. It checks the
     * synthesizable parameter of the actor. Return true if the parameter
     * exists and it evaluates to true; otherwise, return false.  
     * @return true if this helper is synthesizable, otherwise false.
     * @exception IllegalActionException If the parameter cannot be evaluated.
     */
    public boolean isSynthesizable() throws IllegalActionException {
        Actor actor = (Actor) getComponent();
        if (actor instanceof FixTransformer) {
            Parameter parameter = (Parameter) ((FixTransformer) actor)
                    .getAttribute("synthesizable");

            return ((BooleanToken) parameter.getToken()).booleanValue();
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /**
     * Generate a string that represents the VHDL type for the given port. 
     * For a fix type port, this generates a "std_logic_vector" type in
     * VHDL. If the port is boolean type or fix type with a width of 1,
     * the VHDL type "std_logic" is returned.  
     * @param port The given port.
     * @return a string that represents the VHDL type.
     */
    protected String _generateVHDLType(TypedIOPort port) {
        StringBuffer code = new StringBuffer();
        if (port.getType() == BaseType.FIX) {
            int bits = new Precision(_getPortPrecision(port)).getNumberOfBits() - 1;

            if (bits == 0) {
                code.append("std_logic");
            } else {
                code.append("std_logic_vector (" + bits + " DOWNTO 0)");
            }

        } else if (port.getType() == BaseType.BOOLEAN) {

            code.append("std_logic");

        } else {
            // FIXME: we are only dealing with fix point type and boolean type.
            code.append("UNKNOWN TYPE");

        }
        return code.toString();
    }

    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected VHDLCodeGeneratorHelper _getHelper(NamedObj component)
            throws IllegalActionException {
        return (VHDLCodeGeneratorHelper) super._getHelper(component);
    }

    /**
     * Return the precision expression string associated with the given
     * port. This assumes that there is a precision parameter named 
     * "portNamePrecision" in the associated actor. 
     * @param port The given port.
     * @return The precision expression string.
     */
    protected String _getPortPrecision(Port port) {
        Parameter precision = (Parameter) ((Entity) port.getContainer())
                .getAttribute(port.getName() + "Precision");

        return precision.getExpression();
    }

    /**
     * Return the actor that contains the source port connected to the
     * given port. 
     * @param port The given port.
     * @return The actor that contains the source port connected to the
     * given port.
     * @exception IllegalActionException If getSourceChannel(IOPort, int)
     *  throws it.
     */
    protected Actor _getSourcePortActor(IOPort port)
            throws IllegalActionException {
        IOPort sourcePort = getSourceChannel(port, 0).port;

        return (Actor) sourcePort.getContainer();
    }

    /**
     * Return the precision of the source port that is connected to the
     * given port.
     * @param port The given port.
     * @return The precision of the source port that is connected to the
     * given port.
     * @exception IllegalActionException If getSourceChannel(IOPort, int) or
     *  getPrecision(IOPort) throw it.
     */
    protected Precision _getSourcePortPrecision(IOPort port)
            throws IllegalActionException {
        IOPort sourcePort = getSourceChannel(port, 0).port;

        FixTransformer sourceActor = ((FixTransformer) _getSourcePortActor(port));

        return new Precision(sourceActor.getPortPrecision(sourcePort));
    }

}
