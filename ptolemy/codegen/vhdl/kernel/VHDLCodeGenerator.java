package ptolemy.codegen.vhdl.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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
        //inline.setExpression("false");
        generateComment.setExpression("false");

        _macros.add("refList");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return a formatted comment containing the specified string 
     *  with a specified indent level. VHDL comments begin with "--"
     *  and terminiated by end of line. VHDL does not support block 
     *  comments.
     *  @param comment The string to put in the comment.
     *  @param indentLevel The indentation level.
     *  @return A formatted comment.
     */
    public String formatComment(String comment) {
        return "-- " + comment + "\n";            
    }

    /** Generate into the specified code buffer the code associated
     *  with the execution of the container composite actor. This method
     *  calls the generateFireCode() method of the code generator helper
     *  associated with the director of the container.
     *  @return The generated code.
     *  @exception IllegalActionException If a static scheduling director is
     *   missing or the generateFireCode(StringBuffer) method of the
     *   director helper throws the exception.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeEntity model = (CompositeEntity) getContainer();
        ActorCodeGenerator modelHelper = _getHelper(model);
        code.append(modelHelper.generateFireCode());
        return code.toString();
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a VHDL file.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {
        return "";
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {
        return "";
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
                code.append("library " + libraryName + "\n");
            }
        }

        files = includingFiles.iterator();
        while (files.hasNext()) {
            code.append("use " + files.next() + ";" + _eol);
        }
        code.append("use work.pt_utility.all;\n");
        
        return code.toString();
    }    
}
