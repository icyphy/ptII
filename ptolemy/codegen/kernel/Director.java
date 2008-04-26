/* Code generator helper class associated with the Director class.

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
package ptolemy.codegen.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////
////Director

/**
 Code generator helper associated with the ptolemy.actor.Director class.
 This class is also associated with a code generator.

 FIXME: need documentation on how subclasses should extend this class.

 @see CodeGenerator
 @author Ye Zhou, Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (zhouye)
 @Pt.AcceptedRating Yellow (zhouye)

 */
public class Director implements ActorCodeGenerator {
    /** Construct the code generator helper associated with the given director.
     *  Note before calling the generate*() methos, you must also call
     *  setCodeGenerator(CodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        _director = director;
    }

    /////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////

    /** Generate code for declaring read and write offset variables if needed.
     *  It delegates to the helpers of contained actors.
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while creating
     *  offset variables.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helperObject.createOffsetVariablesIfNeeded());
        }
        return code.toString();
    }

    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the helpers for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator.comment("The firing of the director."));

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helper.generateFireCode());
            code.append(helper.generateTypeConvertFireCode());
        }
        return code.toString();
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(actorHelper.generateFireFunctionCode());
        }
        return code.toString();
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException If something goes wrong.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        return new HashSet();
    }

    /** Generate a main loop for an execution under the control of
     *  this director.  In this base class, this simply delegates
     *  to generateFireCode() and generatePostfireCOde().
     *  @return Whatever generateFireCode() returns.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainLoop() throws IllegalActionException {
        return generateFireCode() + generatePostfireCode();
    }

    /** Generate the initialize code for this director.
     *  The initialize code for the director is generated by appending the
     *  initialize code for each actor.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator.comment(1,
                "The initialization of the director."));

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            // Initialize code for the actor.
            code.append(helperObject.generateInitializeCode());

            // Update write offset due to initial tokens produced.
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                int rate = DFUtilities.getTokenInitProduction(port);
                _updateConnectedPortsOffset(port, code, rate);
            }
        }
        return code.toString();
    }

    /**
     * Generate the expression that represents the offset in the generated
     * code.
     * @param offsetString The specified offset from the user.
     * @param port The referenced port.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated
     * code.
     * @exception IllegalActionException If there is problems getting
     * the port buffer size or the offset in the channel and offset map.
     */
    public String generateOffset(String offsetString, IOPort port, 
        int channel, boolean isWrite, CodeGeneratorHelper helper)
            throws IllegalActionException {
        assert false;
        return "";
        //return helper._generateOffset(offsetString, port, channel, isWrite);
    }

    /** Generate the postfire code of the associated composite actor.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating postfire code for the actor
     *   or while creating buffer size and offset map.
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_codeGenerator.comment(0, "The postfire of the director."));

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helperObject.generatePostfireCode());
        }

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        boolean addedDirectorComment = false;
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            // If a helper generates preinitialization code, then
            // print a comment
            String helperObjectPreinitializationCode = helperObject
                    .generatePreinitializeCode();

            if (!addedDirectorComment
                    && CodeGenerator
                            .containsCode(helperObjectPreinitializationCode)) {
                addedDirectorComment = true;
                code.append(_codeGenerator.comment(0,
                        "The preinitialization of the director."));
            }
            code.append(helperObjectPreinitializationCode);
        }

        return code.toString();
    }

    /** Generate mode transition code. It delegates to the helpers of
     *  actors under the control of this director. The mode transition
     *  code generated in this method is executed after each global
     *  iteration, e.g., in HDF model.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If an actor helper throws it
     *   while generating mode transition code.
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            helperObject.generateModeTransitionCode(code);
        }
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(_codeGenerator
                .comment("Transfer tokens to the inside")));

        // FIXME: The codegen.kernel class should not depend on codegen.c.
        // What if we wanted to generate code in another language.
        // We should abstract the actor functionality into codegen.actor.
        ptolemy.codegen.c.actor.TypedCompositeActor _compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(_director
                .getContainer());

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {
                String name = inputPort.getName();

                if (inputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                code.append(CodeStream.indent(_compositeActorHelper
                        .getReference("@" + name)));
                code.append(" = ");
                code.append(_compositeActorHelper.getReference(name));
                code.append(";" + _eol);
            }
        }

        // Generate the type conversion code before fire code.
        code.append(_compositeActorHelper.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, 1);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(_codeGenerator
                .comment("Transfer tokens to the outside"));

        // FIXME: The codegen.kernel class should not depend on codegen.c.
        // What if we wanted to generate code in another language.
        // We should abstract the actor functionality into codegen.actor.
        ptolemy.codegen.c.actor.TypedCompositeActor _compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(_director
                .getContainer());

        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {
                String name = outputPort.getName();

                if (outputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                code.append(_compositeActorHelper.getReference(name) + " = ");
                code.append(_compositeActorHelper.getReference("@" + name));
                code.append(";" + _eol);
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, 1);
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helperObject.generateVariableDeclaration());
        }

        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helperObject.generateVariableInitialization());
        }

        return code.toString();
    }

    /** Generate the wrapup code of the director associated with this helper
     *  class. For this base class, this method just generate the wrapup code
     *  for each actor.
     *  @return The generated wrapup code.
     *  @exception IllegalActionException If the helper class for each actor
     *   cannot be found, or if an error occurs while the helper generate the
     *   wrapup code.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_codeGenerator.comment(1, "The wrapup of the director."));

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj) actor);
            code.append(helperObject.generateWrapupCode());
        }

        return code.toString();
    }

    /**
     * Return the reference to the specified parameter or port of the
     * associated actor. For a parameter, the returned string is in
     * the form "fullName_parameterName". For a port, the returned string
     * is in the form "fullName_portName[channelNumber][offset]", if
     * any channel number or offset is given.
     *
     * FIXME: need documentation on the input string format.
     *
     * @param name The name of the parameter or port
     * @param isWrite True if the parameter is writable
     * @param helper The helper of the associated actor
     * @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    public String getReference(String name, boolean isWrite, 
            CodeGeneratorHelper helper) throws IllegalActionException {
        
        return helper._getReference(name, isWrite);
    }

    /** Return the buffer size of a given channel (i.e, a given port
     *  and a given channel number). In this base class, this method
     *  always returns 1.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The buffer size of the given channel. This base class
     *   always returns 1.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        return 1;
    }

    /** Return the director associated with this class.
     *  @return The director associated with this class.
     */
    public NamedObj getComponent() {
        return _director;
    }

    /** Return an empty HashSet.
     *  @return An empty HashSet.
     *  @exception IllegalActionException Not thrown in this method.
     */
    public Set getIncludeDirectories() throws IllegalActionException {
        return new HashSet();
    }

    /** Return an empty HashSet.
     *  @return An empty HashSet.
     *  @exception IllegalActionException Not thrown in this method.
     */
    public Set getLibraries() throws IllegalActionException {
        return new HashSet();
    }

    /** Return an empty HashSet.
     *  @return An empty HashSet.
     *  @exception IllegalActionException Not thrown in this method.
     */
    public Set getLibraryDirectories() throws IllegalActionException {
        return new HashSet();
    }

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The director gets those variables if it implements
     *  ExplicitChangeContext interface.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If the helper associated with an actor
     *   or director throws it while getting modified variables.
     */
    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();

        if (_director instanceof ExplicitChangeContext) {
            set.addAll(((ExplicitChangeContext) _director)
                    .getModifiedVariables());
        }

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            set.addAll(helperObject.getModifiedVariables());
        }

        return set;
    }

    /**
     * Generate the shared code. In this base class, return an empty set.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set getSharedCode() throws IllegalActionException {
        return new HashSet();
    }

    /** Set the code generator associated with this helper class.
     *  @param codeGenerator The code generator associated with this
     *   helper class.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;

        String path = getClass().getName().replace('.', '/');  

        String packageName = getClass().getPackage().getName();
        if (packageName.startsWith("ptolemy.codegen")) {
            // Strip off the ptolemy.codegen
            packageName = packageName.substring(16);
        }

        String extension = "";
        if (packageName.indexOf('.') != -1) { 
            // Usually, Directors are in packages like
            // ptolemy.codedgen.c.  Thus "c" is the file extension
            // for the helper.  This is a bit of a hack.
            extension = packageName.substring(0,
                    packageName.indexOf('.'));
        }
        _codeStream = new CodeStream(
                "$CLASSPATH/" + path + "." + extension, _codeGenerator);
    }

    /////////////////////////////////////////////////////////////////////
    ////                   protected methods                         ////

    /** Return the minimum number of power of two that is greater than or
     *  equal to the given integer.
     *  @param value The given integer.
     *  @return the minimum number of power of two that is greater than or
     *   equal to the given integer.
     *  @exception IllegalActionException If the given integer is not positive.
     */
    protected int _ceilToPowerOfTwo(int value) throws IllegalActionException {
        if (value < 1) {
            throw new IllegalActionException(getComponent(),
                    "The given integer must be a positive integer.");
        }

        int powerOfTwo = 1;

        while (value > powerOfTwo) {
            powerOfTwo <<= 1;
        }

        return powerOfTwo;
    }

    /** Get the helper class associated with the given component.
     *  @param component The given component.
     *  @return the helper class associated with the given component.
     *  @exception IllegalActionException If the code generator throws
     *   it when getting the helper associated with the given component.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return _codeGenerator._getHelper(component);
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     *  @see #_INDENT1
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }

    /** Update the read offsets of the buffer associated with the given port.
     *
     *  @param port The port whose read offset is to be updated.
     *  @param code The string buffer that the generated code is appended to.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    protected void _updatePortOffset(IOPort port, StringBuffer code, int rate)
            throws IllegalActionException {
        if (rate == 0) {
            return;
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }
        
        PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);
        code.append(portHelper.updateOffset(rate, this));
    }

    /** Update the offsets of the buffers associated with the ports connected
     *  with the given port in its downstream.
     *
     *  @param port The port whose directly connected downstream actors update
     *   their write offsets.
     *  @param code The string buffer that the generated code is appended to.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    protected void _updateConnectedPortsOffset(IOPort port, StringBuffer code,
            int rate) throws IllegalActionException {
        if (rate == 0) {
            return;
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }
        
        PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);
        code.append(portHelper.updateConnectedPortsOffset(rate, this));
        
    }

    ////////////////////////////////////////////////////////////////////
    ////                     protected variables                    ////

    /** The code generator containing this director helper.
     */
    protected CodeGenerator _codeGenerator;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line charactor so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected static final String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** The codeStream associated with this director. */
    protected CodeStream _codeStream;
    
    /** The associated director.
     */
    protected ptolemy.actor.Director _director;

    /** Indent string for indent level 1.
     *  @see #_getIndentPrefix(int)
     */
    protected static final String _INDENT1 = _getIndentPrefix(1);

    /** Indent string for indent level 2.
     *  @see #_getIndentPrefix(int)
     */
    protected static final String _INDENT2 = _getIndentPrefix(2);

    /** Indent string for indent level 3.
     *  @see #_getIndentPrefix(int)
     */
    protected static final String _INDENT3 = _getIndentPrefix(3);

    /** Indent string for indent level 4.
     *  @see #_getIndentPrefix(int)
     */
    protected static final String _INDENT4 = _getIndentPrefix(4);

}
