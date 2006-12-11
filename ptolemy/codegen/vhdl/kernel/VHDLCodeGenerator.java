package ptolemy.codegen.vhdl.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

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
        boolean inline = ((BooleanToken) this.inline.getToken()).booleanValue();

        String includeFiles = generateIncludeFiles();
        String preinitializeCode = generatePreinitializeCode();
        CodeStream.setIndentLevel(1);
        CodeStream.setIndentLevel(2);

        String fireFunctionCode = null;
        if (!inline) {
            CodeStream.setIndentLevel(1);
            fireFunctionCode = generateFireFunctionCode();
            CodeStream.setIndentLevel(0);
        }
        CodeStream.setIndentLevel(0);

        // The appending phase.
        code.append(includeFiles);
        code.append(preinitializeCode);

        if (!inline) {
            code.append(fireFunctionCode);
        }

        _codeFileName = _writeCode(code);
        _writeMakefile();
        //return _executeCommands();
        return 0;
    }
        
    /** Generate library and package files.
     *  @return Return a string that contains the library and use statements.
     *  @throws IllegalActionException If the helper class for some actor 
     *   cannot be found.
     */
    public String generateIncludeFiles() throws IllegalActionException {
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
            if (librarySet.add(libraryName)) {  // true if not already exists.
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
    public String generatePreinitializeCode() throws IllegalActionException {

        ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());

        return compositeActorHelper.generatePreinitializeCode();        
    }
    
    
    /** Generate variable name for the given attribute. The reason to append 
     *  underscore is to avoid conflict with the names of other objects. For
     *  example, the paired PortParameter and ParameterPort have the same name. 
     *  @param attribute The attribute to generate variable name for.
     *  @return The generated variable name.
     */
    public String generateVariableName(NamedObj namedObj) {
        String name = 
            StringUtilities.sanitizeName(namedObj.getFullName());
        while (name.startsWith("_")) {
            name = name.substring(1);
        }
        while (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory. In this base class, 0 is
     *  returned by default.
     *  @return The result of the execution.
     */
    protected int _executeCommands() throws IllegalActionException {
        List commands = new LinkedList();
        
        // Run compile script.
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            commands.add("vsim -s compile.tcl " + _sanitizedModelName + ".vhdl");
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
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The index of the list of code files to generate code for.
     */
    protected int generateFile;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The index for the synthesizeable vhdl code file.  
     */
    private final static int SYNTHEZIEABLE = 0; 

    /**
     * The index for the testbench (non-synthesizeable) code file.  
     */
    private final static int TESTBENCH = 1; 
}
