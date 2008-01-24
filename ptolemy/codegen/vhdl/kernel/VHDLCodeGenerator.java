/* VHSIC Hardware Description Language (VHDL) code generator.

 Copyright (c) 2006-2008 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

/**
 VHSIC Hardware Description Language (VHDL) code generator.
 VHDL is a language that is used to program hardware such as
 Field Programmable Gate Arrays (FPGAs).

 @author Man-kit Leung, Vinayak Nagpal
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating red (zgang)
 @Pt.AcceptedRating red (zgang)
 */
public class VHDLCodeGenerator extends CodeGenerator {

    /** Create a new instance of the VHDL code generator.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public VHDLCodeGenerator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        generatorPackage.setExpression("ptolemy.codegen.vhdl");

        generateComment.setExpression("false");

        inline.setContainer(null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a formatted comment containing the specified string 
     *  with a specified indent level. VHDL comments begin with "--"
     *  and terminiated by end of line. VHDL does not support block 
     *  comments.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String formatComment(String comment) {
        return "-- " + comment + "\n";
    }

    /** Generate code and append it to the given string buffer.
     *  Write the code to the directory specified by the codeDirectory
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.codegen.c</code>, then the file that is
     *  written will be <code>$HOME/Foo.c</code>
     *  This method is the main entry point.
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public int generateCode(StringBuffer code) throws KernelException {
        _sanitizedModelName = StringUtilities.sanitizeName(_model.getName());

        for (_generateFile = 0; _generateFile < 2; _generateFile++) {
            _signals.clear();
            // FIXME: FindBugs noticed that code was being overwritten.
            // This seems wrong?
            //code = new StringBuffer();

            String includeFiles = _generateIncludeFiles();

            String preinitializeCode = _generatePreinitializeCode();

            String mainEntryCode = generateMainEntryCode();

            CodeStream.setIndentLevel(1);
            String sharedCode = _generateSharedCode();
            String signalDeclarationCode = generateVariableDeclaration();
            String bodyCode = _generateBodyCode();

            CodeStream.setIndentLevel(0);
            String mainExitCode = generateMainExitCode();

            // The appending phase.
            code.append(includeFiles);

            code.append(preinitializeCode);

            code.append(mainEntryCode);

            code.append(sharedCode);
            code.append(signalDeclarationCode);

            code.append(_eol + "begin" + _eol);

            code.append(bodyCode);

            code.append(mainExitCode);

            if (_generateFile == SYNTHESIZABLE) {
                _codeFileName = _writeCode(code);
            } else {
                _writeCode(code);
            }

        }

        _writeTopLevel();

        _writeMakefile();

        //return _executeCommands();
        return 0;
    }

    /** Generate the main entry point.
     *  @return Return a string that declares the start of the
     *   archecture block.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {

        return "architecture composite of " + _sanitizedModelName + " is"
                + _eol + _eol;
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the
     *   architecture block.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {

        return _eol + "end architecture composite;" + _eol;
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer result = new StringBuffer();

        Iterator ports = _signals.iterator();

        if (_generateFile == TESTBENCH) {
            result.append("    SIGNAL global_clk : std_logic := '0';" + _eol);
            result.append("    SIGNAL global_reset : std_logic := '0';" + _eol);
        }

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            VHDLCodeGeneratorHelper helper = _getHelper(port.getContainer());

            result.append("    SIGNAL ");

            result
                    .append(helper.getReference(port.getName() + "#" + 0)
                            + " : ");

            result.append(helper._generateVHDLType(port) + ";\n");
        }

        return result.toString();
    }

    /** Generate variable name for the given attribute. The reason to append 
     *  underscore is to avoid conflict with the names of other objects. For
     *  example, the paired PortParameter and ParameterPort have the same name. 
     *  @param attribute The attribute to generate variable name for.
     *  @return The generated variable name.
     */
    public String generateVariableName(NamedObj attribute) {
        String name = StringUtilities.sanitizeName(attribute.getFullName());
        while (name.startsWith("_")) {
            name = name.substring(1);
        }
        while (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * Return the current index of the generate file.
     * @return The current index of the generate file.
     */
    public int getGenerateFile() {
        return _generateFile;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     *  @exception IllegalActionException If there are problems reading
     *  parameters or executing the commands.
     */
    protected int _executeCommands() throws IllegalActionException {
        List commands = new LinkedList();

        // Run compile script.
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            commands
                    .add("vsim -s compile.tcl " + _sanitizedModelName + ".vhdl");
        }

        // Run simulation script.
        if (isTopLevel()) {
            if (((BooleanToken) compile.getToken()).booleanValue()) {
                String command = codeDirectory.stringValue()
                        + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                                .stringValue().endsWith("\\")) ? "/" : "")
                        + _sanitizedModelName;

                commands.add("\"" + command.replace('\\', '/') + "\"");
            }
        }

        if (commands.size() == 0) {
            return -1;
        }

        _executeCommands.setCommands(commands);
        _executeCommands.setWorkingDirectory(codeDirectory.asFile());

        try {
            // FIXME: need to put this output in to the UI, if any. 
            _executeCommands.start();
        } catch (Exception ex) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + _eol);
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:" + _eol + errorMessage);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }

    /**
     * Generate the body code that sits between the main entry and
     * exit code.
     */
    protected String _generateBodyCode() throws IllegalActionException {

        StringBuffer result = new StringBuffer();

        VHDLCodeGeneratorHelper compositeActorHelper = (VHDLCodeGeneratorHelper) _getHelper(getContainer());

        Iterator gateways = _gatewayPorts.iterator();

        while (gateways.hasNext()) {
            TypedIOPort port = (TypedIOPort) gateways.next();
            VHDLCodeGeneratorHelper helper = _getHelper(port.getContainer());

            String signal = helper.getReference(port.getName() + "#" + 0);
            String portName = helper.getReference(port.getName() + "#" + 0)
                    + "_port";

            boolean isOutput = helper.doGenerate();

            if (isOutput) {
                result.append(_eol + "        " + portName + " <= " + signal
                        + ";" + _eol);
            } else {
                result.append(_eol + "        " + signal + " <= " + portName
                        + ";" + _eol);
            }

        }

        result.append(compositeActorHelper.generateFireCode());

        return result.toString();
    }

    /** Generate library and use statements.
     *  @return Return a string that contains the library and use statements.
     *  @exception IllegalActionException If the helper class for some actor 
     *   cannot be found.
     */
    protected String _generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        Iterator files = includingFiles.iterator();

        HashSet librarySet = new HashSet();
        librarySet.add("STD");

        while (files.hasNext()) {
            String file = (String) files.next();
            StringTokenizer tokens = new StringTokenizer(file, ".");
            String libraryName = tokens.nextToken();
            if (librarySet.add(libraryName)) { // true if not already exists.
                code.append("library " + libraryName + ";\n");
            }
        }

        files = includingFiles.iterator();
        while (files.hasNext()) {
            code.append("use " + files.next() + ";" + _eol);
        }
        code.append("use work.pt_utility.all;\n");

        return code.toString();
    }

    /** Generate preinitialize code (if there is any).
     *  This method calls the generatePreinitializeCode() method
     *  of the code generator helper associated with the model director
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found, or if an error occurs when the director
     *   helper generates preinitialize code.
     */
    protected String _generatePreinitializeCode() throws IllegalActionException {

        StringBuffer result = new StringBuffer();

        result.append(_eol + "entity " + _sanitizedModelName + " is" + _eol);
        result.append("    port (" + _eol);

        result.append("        clk : IN std_logic ;" + _eol);
        result.append("        reset : IN std_logic ");

        CompositeActor composite = (ptolemy.actor.CompositeActor) getContainer();

        Iterator actors = composite.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            VHDLCodeGeneratorHelper helper = _getHelper((NamedObj) actor);

            Iterator outputPorts = actor.outputPortList().iterator();

            while (outputPorts.hasNext()) {

                TypedIOPort port = (TypedIOPort) outputPorts.next();

                Iterator sinks = helper.getSinkChannels(port, 0).iterator();

                boolean notGenerated = true;

                while (sinks.hasNext()) {
                    Port sink = ((Channel) sinks.next()).port;

                    VHDLCodeGeneratorHelper sinkHelper = _getHelper(sink
                            .getContainer());

                    // Gateway exists.
                    if (notGenerated
                            && sinkHelper.isSynthesizable() != helper
                                    .isSynthesizable()) {

                        boolean isOutput = helper.doGenerate();

                        result.append(";" + _eol + "        "
                                + helper.getReference(port.getName() + "#" + 0)
                                + "_port : ");

                        result.append((isOutput) ? "OUT " : "IN ");

                        result.append(helper._generateVHDLType(port));

                        _gatewayPorts.add(port);

                        notGenerated = false;

                        _signals.add(port);

                    } else {

                        _signals.add(port);
                    }
                }
            }
        }

        result.append(_eol + "    ) ;" + _eol);
        result.append("end entity;" + _eol + _eol);

        return result.toString();
    }

    /** Get the vhdl code generator helper associated with the
     *  given component.
     *  @param component The given component.
     *  @return The vhdl code generator helper.
     *  @exception IllegalActionException If the helper class
     *   cannot be found.
     */
    protected VHDLCodeGeneratorHelper _getHelper(NamedObj component)
            throws IllegalActionException {
        return (VHDLCodeGeneratorHelper) super._getHelper(component);
    }

    /** Write the code with the sanitized model name postfixed with "_tb"
     *  if the current generate file is the testbench module; Otherwise,
     *  write code with the sanitized model name (as usual). 
     *  @param code The StringBuffer containing the code.
     *  @return The name of the file that was written.
     *  @exception IllegalActionException  If the super class throws it.
     */
    protected String _writeCode(StringBuffer code)
            throws IllegalActionException {

        if (_generateFile == TESTBENCH) {
            _sanitizedModelName += "_tb";
        }
        return super._writeCode(code);
    }

    /**
     * Write the top level file.
     * @exception IllegalActionException
     */
    protected void _writeTopLevel() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _sanitizedModelName = StringUtilities.sanitizeName(_model.getName());

        code.append("library ieee;" + _eol);
        code.append("use ieee.std_logic_1164.all;" + _eol);

        code.append("ENTITY " + _sanitizedModelName + "_top IS" + _eol);
        code.append("END ENTITY;" + _eol + _eol);

        code.append("ARCHITECTURE structure of " + _sanitizedModelName
                + "_top is" + _eol + _eol);

        // generate model component.
        code.append("    COMPONENT " + _sanitizedModelName + " IS" + _eol);

        code.append("        PORT (" + _eol);
        Iterator gateways = _gatewayPorts.iterator();

        while (gateways.hasNext()) {
            TypedIOPort gateway = (TypedIOPort) gateways.next();
            VHDLCodeGeneratorHelper helper = _getHelper(gateway.getContainer());

            String signal = helper.getReference(gateway.getName() + "#" + 0);

            if (helper.isSynthesizable()) {

                code.append("            " + signal + " : OUT ");
                code.append(helper._generateVHDLType(gateway));
            } else {

                code.append("            " + signal + " : IN ");
                code.append(helper._generateVHDLType(gateway));
            }
            code.append(" ;" + _eol);
        }
        code.append("            clk : IN std_logic ;" + _eol);
        code.append("            reset : IN std_logic ) ;" + _eol);

        code.append("    END COMPONENT;" + _eol + _eol);

        // generate testbench component.
        code.append("    COMPONENT " + _sanitizedModelName + "_tb IS" + _eol);

        code.append("        PORT (" + _eol);

        gateways = _gatewayPorts.iterator();

        while (gateways.hasNext()) {
            TypedIOPort gateway = (TypedIOPort) gateways.next();
            VHDLCodeGeneratorHelper helper = _getHelper(gateway.getContainer());

            String signal = helper.getReference(gateway.getName() + "#" + 0);

            if (helper.isSynthesizable()) {

                code.append("            " + signal + " : IN ");
                code.append(helper._generateVHDLType(gateway));
            } else {

                code.append("            " + signal + " : OUT ");
                code.append(helper._generateVHDLType(gateway));
            }
            code.append(" ;" + _eol);
        }
        code.append("            clk : IN std_logic ;" + _eol);
        code.append("            reset : IN std_logic ) ;" + _eol);

        code.append("    END COMPONENT;" + _eol);

        gateways = _gatewayPorts.iterator();

        while (gateways.hasNext()) {
            TypedIOPort gateway = (TypedIOPort) gateways.next();
            VHDLCodeGeneratorHelper helper = _getHelper(gateway.getContainer());

            String signal = helper.getReference(gateway.getName() + "#" + 0)
                    + "_signal";

            code.append(_eol + "    SIGNAL " + signal + " : ");
            code.append(helper._generateVHDLType(gateway));
            code.append(" ;" + _eol);
        }

        code.append("    SIGNAL global_clk : std_logic ;" + _eol);
        code.append("    SIGNAL global_reset : std_logic ; " + _eol);

        code.append(_eol + "BEGIN" + _eol + _eol);

        code.append("    " + "model : " + _sanitizedModelName + _eol);
        code.append("    PORT MAP (" + _eol);

        gateways = _gatewayPorts.iterator();

        while (gateways.hasNext()) {
            TypedIOPort gateway = (TypedIOPort) gateways.next();

            VHDLCodeGeneratorHelper helper = _getHelper(gateway.getContainer());

            String signal = helper.getReference(gateway.getName() + "#" + 0);

            code.append("        " + signal + " => " + signal + "_signal,"
                    + _eol);
        }

        code.append("        clk => global_clk," + _eol);
        code.append("        reset => global_reset ) ;" + _eol + _eol);

        code.append("    " + "testbench : " + _sanitizedModelName + "_tb"
                + _eol);
        code.append("    PORT MAP (" + _eol);

        gateways = _gatewayPorts.iterator();

        while (gateways.hasNext()) {
            TypedIOPort gateway = (TypedIOPort) gateways.next();

            VHDLCodeGeneratorHelper helper = _getHelper(gateway.getContainer());

            String signal = helper.getReference(gateway.getName() + "#" + 0);

            code.append("        " + signal + " => " + signal + "_signal,"
                    + _eol);
        }

        code.append("        clk => global_clk," + _eol);
        code.append("        reset => global_reset ) ;" + _eol + _eol);

        code.append("    global_clk <= not global_clk after 50 ns ;" + _eol);
        code.append("    global_reset <= '1' after 110 ns ;" + _eol + _eol);
        //code.append("clk <= global_clk;" + _eol); 
        //code.append("reset <= global_reset;" + _eol); 
        code.append("END structure;" + _eol);

        _sanitizedModelName += "_top";

        _codeFileName = _writeCode(code);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The index of the list of code files to generate code for. 0 refers
     * to the synthesizeable vhdl code file, and 1 refers to the testbench
     * code file.
     */
    protected int _generateFile;

    /**
     * The Set of gateway ports that need to be declared.
     */
    protected Set<Port> _gatewayPorts = new HashSet();

    /**
     * The Set of (non-gateway) signals that need to be declared.
     */
    protected Set<Port> _signals = new HashSet();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The index for the synthesizeable vhdl code file.  
     */
    public final static int SYNTHESIZABLE = 0;

    /**
     * The index for the testbench (non-synthesizeable) code file.  
     */
    public final static int TESTBENCH = 1;

}
