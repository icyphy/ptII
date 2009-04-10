/* Code generator adapter class associated with the SDFDirector class.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy;
import ptolemy.cg.kernel.generic.CodeStream;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.c.CCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.c.CCodegenUtilities;
import ptolemy.cg.lib.CompiledCompositeActor;
import ptolemy.cg.lib.PointerToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////
////SDFDirector

/**
 Code generator adapter associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Ye Zhou, Gang Zhou
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (eal)
 */
public class SDFDirector extends ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////


    /** Generate input variable declarations.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    @Override
    public String generateInputVariableDeclaration()
            throws IllegalActionException {
        boolean dynamicReferencesAllowed = ((BooleanToken) getCodeGenerator().allowDynamicMultiportReference
                .getToken()).booleanValue();

        StringBuffer code = new StringBuffer();

        Iterator<?> inputPorts = ((Actor) getComponent()).inputPortList()
                .iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            if (!inputPort.isOutsideConnected()) {
                continue;
            }

            code.append("static " + targetType(inputPort.getType()) + " "
                    + CodeGeneratorAdapterStrategy.generateName(inputPort));

            int bufferSize = _ports.getBufferSize(inputPort);
            if (inputPort.isMultiport()) {
                code.append("[" + inputPort.getWidth() + "]");
                if (bufferSize > 1 || dynamicReferencesAllowed) {
                    code.append("[" + bufferSize + "]");
                }
            } else {
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
            }

            code.append(";" + _eol);
        }

        return code.toString();
    }

    /** Generate output variable declarations.
     *  @return a String that declares output variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    @Override
    public String generateOutputVariableDeclaration()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if (!outputPort.isOutsideConnected() || outputPort.isInsideConnected()) {
                code.append("static " + targetType(outputPort.getType()) + " "
                        + CodeGeneratorAdapterStrategy.generateName(outputPort));

                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }

                int bufferSize = _ports.getBufferSize(outputPort);

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        _updatePortBufferSize();
        _portNumber = 0;
        _intFlag = false;
        _doubleFlag = false;
        _booleanFlag = false;

        return code.toString();
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment("SDFDirector: "
                + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);
        boolean targetCpp = ((BooleanToken) ((CCodeGenerator) getCodeGenerator()).generateCpp
                .getToken()).booleanValue();

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator().getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode.getToken())
                        .booleanValue()) {

            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }
            Type type = ((TypedIOPort) inputPort).getType();
            String portName = inputPort.getName();

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {

                    String tokensFromOneChannel = "tokensFromOneChannelOf"
                            + portName + i;
                    String pointerToTokensFromOneChannel = "pointerTo"
                            + tokensFromOneChannel;
                    code.append("jobject "
                            + tokensFromOneChannel
                            + " = "
                            + CCodegenUtilities.jniGetObjectArrayElement(
                                    portName, String.valueOf(i), targetCpp)
                            + ";" + _eol);

                    if (type == BaseType.INT) {
                        code.append("jint * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements("Int",
                                        tokensFromOneChannel, targetCpp) + ";"
                                + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append("jdouble * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements(
                                        "Double", tokensFromOneChannel,
                                        targetCpp) + ";" + _eol);
                    } else if (type == PointerToken.POINTER) {
                        code.append("jint * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements("Int",
                                        tokensFromOneChannel, targetCpp) + ";"
                                + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append("jboolean * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements(
                                        "Boolean", tokensFromOneChannel,
                                        targetCpp) + ";" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                    }
                    String portNameWithChannelNumber = portName;
                    if (inputPort.isMultiport()) {
                        portNameWithChannelNumber = portName + '#' + i;
                    }
                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference("@"
                                + portNameWithChannelNumber + "," + k));
                        if (type == PointerToken.POINTER) {
                            code.append(" = (void *) "
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "];" + _eol);
                        } else {
                            code.append(" = " + pointerToTokensFromOneChannel
                                    + "[" + k + "];" + _eol);
                        }
                    }

                    if (type == BaseType.INT) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Int", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Double", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == PointerToken.POINTER) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Int", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Boolean", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                    }
                }
            }

        } else {
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
                    String name = inputPort.getName();

                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference(
                                "@" + name + "," + k));
                        code.append(" = " + _eol);
                        code.append(compositeActorAdapter.getReference(
                                name + "," + k));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment("SDFDirector: "
                + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);
        boolean targetCpp = ((BooleanToken) ((CCodeGenerator) getCodeGenerator()).generateCpp
                .getToken()).booleanValue();

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator().getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode.getToken())
                        .booleanValue()) {

            if (_portNumber == 0) {
                int numberOfOutputPorts = container.outputPortList().size();

                code.append("jobjectArray tokensToAllOutputPorts;"
                        + _eol);
                code.append("jclass "
                        + _objClass
                        + " = "
                        + CCodegenUtilities.jniFindClass("Ljava/lang/Object;",
                                targetCpp) + ";" + _eol);
                code.append("tokensToAllOutputPorts = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfOutputPorts), "objClass",
                                targetCpp) + ";" + _eol);
            }

            String portName = outputPort.getName();
            String tokensToThisPort = "tokensTo" + portName;

            // FindBugs wants this instanceof check.
            if (!(outputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(outputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            Type type = ((TypedIOPort) outputPort).getType();

            int numberOfChannels = outputPort.getWidthInside();
            code.append("jobjectArray " + tokensToThisPort + ";"
                    + _eol);

            // Find jni classes and methods and initialize the jni array
            // for the given type.
            if (type == BaseType.INT) {
                if (!_intFlag) {
                    code.append("jclass " + _objClassI + " = "
                            + CCodegenUtilities.jniFindClass("[I", targetCpp)
                            + ";" + _eol);
                    _intFlag = true;
                }
                code.append(tokensToThisPort + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), _objClassI,
                                targetCpp) + ";" + _eol);
            } else if (type == BaseType.DOUBLE) {
                if (!_doubleFlag) {
                    code.append("jclass " + _objClassD + " = "
                            + CCodegenUtilities.jniFindClass("[D", targetCpp)
                            + ";" + _eol);
                    _doubleFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), _objClassD,
                                targetCpp) + ";" + _eol);
            } else if (type == PointerToken.POINTER) {
                if (!_intFlag) {
                    code.append("jclass " + _objClassI + " = "
                            + CCodegenUtilities.jniFindClass("[I", targetCpp)
                            + ";" + _eol);
                    _intFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), _objClassI,
                                targetCpp) + ";" + _eol);
            } else if (type == BaseType.BOOLEAN) {
                if (!_booleanFlag) {
                    code.append("jclass objClassZ = "
                            + CCodegenUtilities.jniFindClass("[Z", targetCpp)
                            + ";" + _eol);
                    _booleanFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), "objClassZ",
                                targetCpp) + ";" + _eol);
            } else {
                // FIXME: need to deal with other types
            }

            // Create an array to contain jni objects
            for (int i = 0; i < outputPort.getWidthInside(); i++) {

                String tokensToOneChannel = "tokensToOneChannelOf" + portName;
                if (i == 0) {
                    if (type == BaseType.INT) {
                        code.append("jint " + tokensToOneChannel
                                + "[" + rate + "];" + _eol);

                    } else if (type == BaseType.DOUBLE) {
                        code.append("jdouble " + tokensToOneChannel
                                + "[" + rate + "];" + _eol);

                    } else if (type == PointerToken.POINTER) {
                        code.append("jint " + tokensToOneChannel
                                + "[" + rate + "];" + _eol);

                    } else if (type == BaseType.BOOLEAN) {
                        code.append("jboolean " + tokensToOneChannel
                                + "[" + rate + "];" + _eol);

                    } else {
                        // FIXME: need to deal with other types
                    }
                }

                String portNameWithChannelNumber = portName;
                if (outputPort.isMultiport()) {
                    portNameWithChannelNumber = portName + '#' + i;
                }

                // Assign each token to the array of jni objects
                for (int k = 0; k < rate; k++) {
                    String portReference = compositeActorAdapter
                            .getReference("@" + portNameWithChannelNumber + ","
                                    + k);
                    if (type == PointerToken.POINTER) {
                        code.append(tokensToOneChannel + "[" + k
                                + "] = " + "(int) " + portReference + ";"
                                + _eol);
                    } else {
                        code.append(tokensToOneChannel + "[" + k
                                + "] = " + portReference + ";" + _eol);
                    }
                }

                String tokensToOneChannelArray = "arr" + portName + i;
                // Create and fill an array of Java objects.
                if (type == BaseType.INT) {
                    code.append("jintArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Int", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Int",
                                    tokensToOneChannelArray, "0", String
                                            .valueOf(rate), tokensToOneChannel,
                                    targetCpp) + ";" + _eol);

                } else if (type == BaseType.DOUBLE) {
                    code.append("jdoubleArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Double", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Double",
                                    tokensToOneChannelArray, "0", String
                                            .valueOf(rate), tokensToOneChannel,
                                    targetCpp) + ";" + _eol);

                } else if (type == PointerToken.POINTER) {
                    code.append("jintArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Int", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Int",
                                    tokensToOneChannelArray, "0", String
                                            .valueOf(rate), tokensToOneChannel,
                                    targetCpp) + ";" + _eol);

                } else if (type == BaseType.BOOLEAN) {
                    code.append("jbooleanArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Boolean", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Boolean",
                                    tokensToOneChannelArray, "0", String
                                            .valueOf(rate), tokensToOneChannel,
                                    targetCpp) + ";" + _eol);
                } else {
                    // FIXME: need to deal with other types
                }

                code.append(CCodegenUtilities.jniSetObjectArrayElement(
                                tokensToThisPort, String.valueOf(i),
                                tokensToOneChannelArray, targetCpp) + ";"
                        + _eol);
                code.append(CCodegenUtilities.jniDeleteLocalRef(
                                tokensToOneChannelArray, targetCpp) + ";"
                        + _eol);
            }

            code.append(CCodegenUtilities.jniSetObjectArrayElement(
                            "tokensToAllOutputPorts", String
                                    .valueOf(_portNumber), tokensToThisPort,
                            targetCpp) + ";" + _eol);
            code.append(CCodegenUtilities.jniDeleteLocalRef(tokensToThisPort,
                            targetCpp) + ";" + _eol);
            _portNumber++;

        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = outputPort.getName();

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(CodeStream.indent(compositeActorAdapter
                                .getReference(name + "," + k)));
                        code.append(" =" + _eol);
                        code.append(CodeStream.indent(compositeActorAdapter.getReference("@" + name
                                        + "," + k)));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, rate);
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    public CCodeGenerator getCodeGenerator() {
        return (CCodeGenerator) super.getCodeGenerator();
    }
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    private int _portNumber = 0;

    private boolean _doubleFlag;

    private boolean _booleanFlag;

    private boolean _intFlag;

    /** Name of variable containing jni class for Objects. */
    private String _objClass = "objClass";

    /** Name of variable containing jni double class. */
    private String _objClassD = "objClassD";

    /** Name of variable containing jni int class. */
    private String _objClassI = "objClassI";

}
