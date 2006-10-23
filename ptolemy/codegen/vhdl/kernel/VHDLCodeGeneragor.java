package ptolemy.codegen.vhdl.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.StaticSchedulingCodeGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class VHDLCodeGeneragor extends StaticSchedulingCodeGenerator {

    public VHDLCodeGeneragor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a VHDL file.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {
        return "architecture behavior of " + _sanitizedModelName + "is";
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {
        return "end architecture" + _sanitizedModelName;
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
                code.append("library " + libraryName);
            }
        }

        files = includingFiles.iterator();
        while (files.hasNext()) {
            code.append("use " + files.next() + ";\n");
        }
        
        return code.toString();
    }    
    
    /** Generate ports declarations.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }
    
    /** Generate preinitialize code.
     *  Declaration of signals.
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found, or if an error occurs when the director
     *   helper generates preinitialize code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        return "";
    }    

    /**
     * Connection of the signals to ports.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If the helper class for the model
     *  director cannot be found or if an error occurs when the director
     *  helper generates initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return "";
    }
    
    /** Generate the behavorial code.
     *  @return The empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateBodyCode() throws IllegalActionException {        
        return "";
    }

}
