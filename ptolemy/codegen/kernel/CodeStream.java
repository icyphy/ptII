/*
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
package ptolemy.codegen.kernel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

/**
 * Read and process code blocks from the helper .c file. Helper .c files
 * contain c code blocks for the associated java helper actor. A proper
 * code block should have the following grammar:
 * <pre>
 *     _BLOCKSTART CodeBlockName [(Parameter1, Parameter2), ...] _HEADEREND
 *         CodeBlockBody
 *     _BLOCKEND
 * </pre>
 * Parameterized code blocks can contain parameters which the user can
 * specify. Parameter substitution syntax is straight-forward string pattern
 * substitution, so the user is responsible for declaring unique parameter
 * names. For example, a code block is declared to be the following:
 * <pre>
 *     &#47;*** initBlock ($arg) ***&#47;
 *         if ($ref(input) != $arg) {
 *             $ref(output) = $arg;
 *         }
 *     &#47;**&#47;
 * </pre>
 * If the user invoke the appendCodeBlock() method with a single argument,
 * which is the integer 3,
 * <pre>
 *     ArrayList args = new ArrayList();
 *     args.add(Integer.toString(3));
 *     appendCodeBlock("initBlock", args);
 * </pre>
 * then after parameter substitution, the code block would become:
 * <pre>
 *     if ($ref(input) != 3) {
 *         $ref(output) = 3;
 *     }
 * </pre>
 * Parameter substitution takes place before macro substitution processed
 * by the codegen kernel. CodeStream supports overriding superclass code
 * blocks. It also supports overloading code blocks with different number
 * of parameters.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class CodeStream {
    /**
     * Construct a new code stream associated with the given java actor
     * helper. Each actor should have its own codestream during code
     * generation.
     * @param helper The actor helper associated with this code stream,
     * which is currently ignored.
     */
    public CodeStream(CodeGeneratorHelper helper) {
        _helper = helper;
    }

    /**
     * Construct a new code stream, given a specified file path of the
     * helper .c file as a URL suitable for
     * {@link ptolemy.util.FileUtilities#openForReading(String, URI, ClassLoader)},
     *  for example "file:./test/testCodeBlock.c".
     * @param path The given file path.
     * .c file 
     */
    public CodeStream(String path) {
        _filePath = path;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Append the contents of the given CodeStream to this code stream.
     * @param codeStream The given code stream.
     */
    //public void append(CodeStream codeStream) {
    //    _stream.append(codeStream.toString());
    //}
    /**
     * Append the contents of the given String to this code stream.
     * @param codeBlock The given string.
     */
    public void append(String codeBlock) {
        _stream.append(codeBlock);
    }

    /**
     * Append the contents of the given StringBuffer to this code stream.
     * @param codeBlock The given string buffer.
     */
    //public void append(StringBuffer codeBlock) {
    //    _stream.append(codeBlock);
    //}
    /**
     * Append the code block specified the given block name. This method
     * invokes appendCodeBlock(String, ArrayList) with no arguments by
     * passing an empty array list of argments. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, ArrayList, boolean)
     * @param blockName The given code block name.
     * @exception IllegalActionException If 
     *  appendCodeBlock(String, ArrayList, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName) throws IllegalActionException {
        appendCodeBlock(blockName, new ArrayList(), false);
    }

    /**
     * Append the code block specified the given block name. This method
     * invokes appendCodeBlock(String, ArrayList) with no arguments by
     * passing an empty array list of argments. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, ArrayList, boolean)
     * @param blockName The given code block name.
     * @param mayNotExist Indicate if it is okay not to find the code block.
     *  if the code block has parameters.
     * @exception IllegalActionException If 
     *  appendCodeBlock(String, ArrayList, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName, boolean mayNotExist)
            throws IllegalActionException {
        appendCodeBlock(blockName, new ArrayList(), mayNotExist);
    }

    /**
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, ArrayList, boolean)
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     *  if the code block has parameters.
     * @exception IllegalActionException If 
     *  appendCodeBlock(String, ArrayList, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName, ArrayList arguments)
            throws IllegalActionException {
        appendCodeBlock(blockName, arguments, false);
    }

    /**
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list.
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     * @param mayNotExist Indicate if it is okay not to find the code block.
     *  if the code block has parameters.
     * @exception IllegalActionException If _constructCodeTable() throws
     *  the exception, or if the requested code block is required but cannot 
     *  be found, or if the numbers of arguments and parameters do not match.
     */
    public void appendCodeBlock(String blockName, ArrayList arguments,
            boolean mayNotExist) throws IllegalActionException {
        if (!mayNotExist && arguments.size() == 0) {
            // That means this is a request by the user. This check prevents
            // user from appending duplicate code blocks that are already
            // appended by the code generator by default.
            String[] blocks = CodeGeneratorHelper.getDefaultBlocks();

            for (int i = 0; i < blocks.length; i++) {
                if (blockName.matches(blocks[i])) {
                    throw new IllegalActionException(blockName + 
                            " -- is a code block that is appended by default.");
                }
            }
        }

        // First, it checks if the code file is parsed already.
        // If so, it gets the code block from the well-constructed code
        // block table.  If not, it has to construct the table.
        if (_declarations == null) {
            _constructCodeTable(mayNotExist);
        }

        Signature signature = new Signature(blockName, arguments.size());
        
        StringBuffer codeBlock = _declarations.getCode(signature);

        ArrayList parameters = _declarations.getParameters(signature);

        // Cannot find a code block with the matching signature.
        if (codeBlock == null) {
            if (mayNotExist) {
                return;
            } else {
                throw new IllegalActionException(
                        "Cannot find code block: " + signature + ".\n" );
            }
        }

        // Text-substitute for each parameters.
        for (int i = 0; i < arguments.size(); i++) {
            String replaceString = arguments.get(i).toString();
            try {
                codeBlock = new StringBuffer(codeBlock.toString().replaceAll(
                        _checkParameterName(parameters.get(i).toString()),
                        replaceString));
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex, signature + " in "
                        + _declarations.getFilePath(signature)
                        + " problems replacing \"" + parameters.get(i).toString()
                        + "\" with \"" + replaceString + "\"");
            }
        }

        _stream.append(codeBlock);
    }

    /**
     * Append multiple code blocks whose names match the given egular
     * expression.
     * @param nameExpression The given regular expression for the block names.
     * @exception IllegalActionException If _constructCodeTable() throws
     *  the exception, or if the requested code block is required but cannot 
     *  be found, or if the numbers of arguments and parameters do not match.
     */
    public void appendCodeBlocks(String nameExpression)
            throws IllegalActionException {
        // First, it checks if the code file is parsed already.
        // If so, it gets the code block from the well-constructed code
        // block table.  If not, it has to construct the table.
        if (_declarations == null) {
            _constructCodeTable(true);
        }        
        
        Iterator allSignatures = _declarations.keys();
        while (allSignatures.hasNext()) {
             Signature signature = (Signature) allSignatures.next();
            if (signature.numParameters == 0 && 
                    signature.functionName.matches(nameExpression)) {
                _stream.append(_declarations.getCode(signature));
            }
        }
    }

    /**
     * Clear the contents of this code stream.
     */
    public void clear() {
        _stream = new StringBuffer();
    }

    /**
     * Return a StringBuffer that contains all the code block names and
     * bodies from the associated helper .c file.
     * @return The content from parsing the helper .c file.
     * @exception IllegalActionException If an error occurs during parsing.
     */
    public String description() throws IllegalActionException {
        StringBuffer buffer = new StringBuffer();

        if (_declarations == null) {
            _constructCodeTable(true);
        }

        for (Iterator keys = _declarations.keys(); 
                keys.hasNext();) {
            Signature signature = (Signature) keys.next();
            buffer.append(signature.functionName);

            ArrayList parameters = 
                (ArrayList) _declarations.getParameters(signature);

            if ((parameters != null) && (parameters.size() > 0)) {
                for (int i = 0; i < parameters.size(); i++) {
                    if (i == 0) {
                        buffer.append("(" + parameters.get(i));
                    } else {
                        buffer.append(", " + parameters.get(i));
                    }
                }

                buffer.append(")");
            }

            buffer.append(":\n");
            buffer.append((StringBuffer) _declarations.getCode(signature));
            buffer.append("\n-------------------------------\n\n");
        }

        return buffer.toString();
    }

    /**
     * Simple stand alone test method. Parse a helper .c file, and print
     * all the code blocks.
     * @param args Command-line arguments, the first of which names a 
     * .c file as a URL , for example file:./test/testCodeBlock.c.
     * @exception IOException If an error occurs when reading user inputs.
     * @exception IllegalActionException If an error occurs during parsing
     *  the helper .c file.
     */
     public static void main(String[] args) throws IOException,
             IllegalActionException {
         try {
             CodeStream code = new CodeStream(args[0]);
         
             System.out.println("\n----------Result-----------------------\n");
             System.out.println(code.description());
             System.out.println("\n----------Result-----------------------\n");
         
             ArrayList codeBlockArgs = new ArrayList();
             codeBlockArgs.add(Integer.toString(3));
             code.appendCodeBlock("initBlock", codeBlockArgs, false);
             System.out.println(code);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
         
    /**
     * Return the string representation of the code stream.
     * @return The string representation of this code stream.
     */
    public String toString() {
        return _stream.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Type check the given name and return a well-formed code block name.
     * This method assumes the code block name is short (that is why String
     * is used instead of StringBuffer). This method returns a well-formed
     * code block name, otherwise, it throws an exception.
     * @param name The given code block name.
     * @return The well-formed code block name after lexical checking.
     * @exception Thrown if the given name is not well-formed. 
     */
    private static String _checkCodeBlockName(String name)
            throws IllegalActionException {
        // FIXME: extra lexical checking
        // e.g. Do we allow nested code block within code block name??
        // e.g. ...spaces??
        // e.g. ...special symbols??
        return name.trim();
    }

    /**
     * Type check the parameter name.
     * Check if the given string starts with '$'. Insert '\' before '$'. 
     * '$' is a special charater in regular expression, and we are using this
     * name string as regular expression to do text substitution. Requires the
     * rest of the characters to be within the set [a-zA-Z_0-9].
     * Returns a well-formed parameter name, otherwise, throws an
     * IllegalActionException.
     * @param name The given parameter name.
     * @return The well-formed parameter name after lexical checking.
     * @exception Thrown if the given name is not well-formed. 
     */
    private static String _checkParameterName(String name)
            throws IllegalActionException {
        if (!name.startsWith("$")) {
            throw new IllegalActionException("Parameter \"" + name
                    + "\" is not well-formed.\n"
                    + "Parameter name for code block needs to starts with '$'");
        }
        name.matches("[a-zA-Z_0-9]");
        return '\\' + name;
    }

    /**
     * Read the code blocks associated with this helper and construct the code
     * block and parameter table. If there is a pre-specified file path to
     * read from, it only reads code block from the specified file only.
     * Otherwise, it recursively searches code blocks from super classes'
     * helpers.
     * @param mayNotExist Indicate if the file is required to exist.
     * @param filePath The given .c file to read from.
     * @exception IllegalActionException If an error occurs when parsing the
     *  helper .c file.
     */
    private void _constructCodeTable(boolean mayNotExist)
            throws IllegalActionException {

        _declarations = new CodeBlockTable();

        if (_filePath != null) {
            // Use the pre-specified file path.
            _constructCodeTableHelper(mayNotExist);            
        } else {        
            for (Class helperClass = _helper.getClass(); helperClass != null;
                helperClass = helperClass.getSuperclass()) {

                _filePath = _getPath(helperClass);

                _constructCodeTableHelper(mayNotExist);

                mayNotExist = true;     // Superclass
            }
        }
    }

    /**
     * 
     * @param mayNotExist Indicate if the file is required to exist.
     * @throws IllegalActionException
     */
    private void _constructCodeTableHelper(boolean mayNotExist)
        throws IllegalActionException {
        BufferedReader reader = null;    
        
        try {
            // Open the .c file for reading.
            reader = FileUtilities.openForReading(
                    _filePath, null, null);

            StringBuffer codeInFile = new StringBuffer();

            // FIXME: is there a better way to read the entire file?
            // create a string of all code in the file
            for (String line = reader.readLine(); 
                line != null; line = reader.readLine()) {
                codeInFile.append(line + "\n");
            }

            _declarations.addScope();

            // repeatedly parse the file
            while (_parseCodeBlock(codeInFile) != null) {
                ;
            }

        } catch (IllegalActionException ex) {
            _declarations = null;
            throw ex;
        } catch (IOException ex) {
            if (reader == null) {
                if (mayNotExist) {
                } else {
                    _declarations = null;
                    throw new IllegalActionException(null, ex,
                            "Cannot open file: " + _filePath);
                }
            } else {
                _declarations = null;
                throw new IllegalActionException(null, ex,
                        "Error reading file: " + _filePath);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                throw new IllegalActionException(null, ex,
                        "Error closing file: " + _filePath);
            }
        }                
    }
    
    /**
     * Get the file path for the helper .c file associated with the given
     * helper class.
     * @param helperClass The given helper class
     * @return Path for the helper .c file.
     */
    private String _getPath (Class helperClass) {
        return "$CLASSPATH/" + helperClass.getName().replace('.', '/') + ".c";
    }
    
    /**
     * Parse from the _parseIndex and return the next code block
     * body from the given StringBuffer. This method recursively
     * parses within the code body for nested code blocks.
     * @param codeInFile Code from the helper .c file.
     * @return The code body within the current code block.
     * @exception IllegalActionException If code block's close block
     *  pattern, _BLOCKEND, is missing.
     * @see _parseIndex
     * @see _BLOCKEND
     */
    private StringBuffer _parseBody(StringBuffer codeInFile)
            throws IllegalActionException {
        int openBlock = 1;
        int scanIndex = _parseIndex;

        int startIndex;
        int endIndex = -1;

        while (openBlock > 0) {
            endIndex = codeInFile.indexOf(_BLOCKEND, scanIndex);
            startIndex = codeInFile.indexOf(_BLOCKSTART, scanIndex);

            if ((startIndex < endIndex) && (startIndex != -1)) {
                openBlock++;
                scanIndex = startIndex + 1;
            } else {
                openBlock--;
                scanIndex = endIndex + 1;
            }
        }

        if (endIndex == -1) {
            throw new IllegalActionException("Missing close block in "
                    + _filePath);
        }

        StringBuffer body = new StringBuffer(
                codeInFile.substring(_parseIndex, endIndex));

        // Recursively parsing for nested code blocks
        //for (String subBlockKey = _parseCodeBlock(body); subBlockKey != null;) {
            // FIXME: do we include the nested code block into 
            // the current block??
            //body.append((StringBuffer) _codeBlockTable.get(subBlockKey));
            // FIXME: take away the nested code block from
            // the current code block
            // reset the parse index to parse the body from the beginning
            //_parseIndex = 0;
            //subBlockKey = _parseCodeBlock(body);
        //}

        _parseIndex = _BLOCKEND.length() + endIndex;
        return body;
    }

    /**
     * Parse from the _parseIndex for the next single code block and return
     * the code block name. This method puts the code block body (value)
     * and the code block name (key) into the code block table. It calls
     * the parseHeader(StringBuffer) and parseBody(StringBuffer) functions.
     * @param codeInFile Code from the helper .c file.
     * @return The name of the code block, or null if there is no more code
     *  blocks to be parsed.
     * @exception IllegalActionException If an error occurs during parsing.
     * @see parseHeader(StringBuffer)
     * @see parseBody(StringBuffer)
     */
    private Signature _parseCodeBlock(StringBuffer codeInFile) 
            throws IllegalActionException {
        
        Signature signature = _parseHeader(codeInFile);

        if (signature != null) {
            StringBuffer body = _parseBody(codeInFile);
            _declarations.putCode(signature, _filePath, body);
        }

        return signature;
    }

    /**
     * Parse the header of the code block. Usually code block header starts
     * with the code block name followed by the list of parameters. Users can
     * also overload the code block by giving different number of arguments,
     * and/or specifying different types for the ports. appendCodeBlock would
     * append the corresponding block by checking the number of given
     * arguments.
     * Parse from the _parseIndex for the next code block header and
     * return the next code block name. This method parses for any parameter
     * declarations and put the list of parameter(s) into the _parameterTable.
     * @param codeInFile Code from the helper .c file.
     * @return The name of the code block, or null if there is no more
     *  code blocks to be parsed.
     * @exception IllegalActionException If the code block's close header
     *  pattern, _HEADEREND, is missing.
     * @see _HEADEREND
     * @see _parameterTable
     */
    private Signature _parseHeader(StringBuffer codeInFile)
            throws IllegalActionException {
        Signature signature;
        
        _parseIndex = codeInFile.indexOf(_BLOCKSTART, _parseIndex);

        // Check to see if there are no more code block start headers.
        if (_parseIndex == -1) {
            return null;
        }

        _parseIndex += _BLOCKSTART.length();

        int endIndex = codeInFile.indexOf(_HEADEREND, _parseIndex);

        if (endIndex == -1) {
            throw new IllegalActionException("Missing code block close header"
                    + " in " + _filePath);
        }

        int parameterIndex = codeInFile.indexOf("(", _parseIndex);

        if ((parameterIndex == -1) || (parameterIndex >= endIndex)) {
            String name = _checkCodeBlockName(
                    codeInFile.substring(_parseIndex, endIndex));

            signature = new Signature(name, 0);
        } else {
            String name = _checkCodeBlockName(codeInFile.substring(_parseIndex,
                    parameterIndex));

            int parameterEndIndex = codeInFile.indexOf(")", _parseIndex);

            ArrayList parameterList = new ArrayList();

            // Keep parsing for extra parameters.
            for (int commaIndex = codeInFile.indexOf(",", _parseIndex); 
            commaIndex != -1 && (commaIndex < parameterEndIndex);
            commaIndex = codeInFile.indexOf(",", commaIndex + 1)) {
                
                String newParameter = 
                    codeInFile.substring(parameterIndex + 1, commaIndex);

                parameterList.add(newParameter.trim());
                parameterIndex = commaIndex;
            }

            String newParameter = 
                codeInFile.substring(parameterIndex + 1, parameterEndIndex);
            parameterList.add(newParameter.trim());

            signature = new Signature(name, parameterList.size());

            _declarations.putParameters(signature, parameterList);            
        }
        
        _parseIndex = _HEADEREND.length() + endIndex;
        return signature;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private class CodeBlockTable {
        
        public CodeBlockTable() {}

        public Iterator keys() {
            HashSet signatures = new HashSet();
            Iterator files = _codeTableList.iterator();

            while (files.hasNext()) {
                Hashtable table = (Hashtable) files.next();
                signatures.addAll(table.keySet());
            }
            return signatures.iterator();
        }
        
        public String getFilePath (Signature signature)
                throws IllegalActionException {
            Iterator files = _codeTableList.iterator();

            while (files.hasNext()) {
                Hashtable table = (Hashtable) files.next();
                if (table.containsKey(signature)) {
                    return (String) 
                    ((Object[]) table.get(signature))[0];
                }
            }
            throw new IllegalActionException(
                    "Cannot find code block " + signature + ".\n");
        }

        public StringBuffer getCode (Signature signature)
                throws IllegalActionException {
            Iterator files = _codeTableList.iterator();

            while (files.hasNext()) {
            
                Hashtable table = (Hashtable) files.next();
                
                if (table.containsKey(signature)) {
                    return (StringBuffer) 
                    ((Object[]) table.get(signature))[1];
                }
            }
            return null;
        }
        
        public void putCode (Signature signature, String filePath,
                StringBuffer code) throws IllegalActionException {
            Object[] codeBlock = new Object[2];
            
            codeBlock[0] = filePath;
            codeBlock[1] = code;
            
            Hashtable currentScope = (Hashtable) _codeTableList.getLast(); 
            
            if (currentScope.containsKey(signature)) {
                throw new IllegalActionException(
                        "Multiple code blocks have the same signature: "
                        + signature + " in " + _filePath);                
            } 
            
            currentScope.put(signature, codeBlock);                
        }
        
        public ArrayList getParameters (Signature signature) {
            return (ArrayList) _parameterTable.get(signature);
        }
        
        public void addScope () {
            _codeTableList.addLast(new Hashtable());
        }

        public void putParameters (Signature signature, ArrayList parameters) {
            _parameterTable.put(signature, parameters);
        }
        
        /**
         * LinkedList of Hashtable of code blocks. Each index of the
         * LinkedList represents a separate helper .c code block file. 
         */
        private LinkedList _codeTableList = new LinkedList();

        /**
         * The code block table that stores the code block parameters
         * (ArrayList) with the code block names (Signature) as key.
         */
        private Hashtable _parameterTable = new Hashtable();        
        
    }
    
    private class Signature {

        /**
         * 
         * @param functionName
         * @param numParameters
         * @throws IllegalActionException
         */
        public Signature(String functionName, int numParameters) 
            throws IllegalActionException {
            
            if (functionName == null || numParameters < 0) {
                throw new IllegalActionException (
                        "Bad code block signature: (" + functionName +
                        ") with " + numParameters + " parameters.");
            }
            
            this.functionName = functionName;

            this.numParameters = numParameters;
        }

        public boolean equals(Object object) {
            return object instanceof Signature
                && functionName.equals(((Signature) object).functionName)
                && numParameters == ((Signature) object).numParameters;
        }

        /**
         * Return the hash code for this channel. Implementing this method
         * is required for comparing the equality of channels.
         * @return Hash code for this channel.
         */
        public int hashCode() {
            return functionName.hashCode() + numParameters;
        }

        /**
         * Return the string format of this code block signature.
         */
        public String toString() {
            String result = functionName + "(";
            for (int i = 0; i < numParameters; i++) {
                if (i != 0) {
                    result += ", ";
                }
                result += "$";
            }
            return result += ")";
        }
        
        public String functionName;

        public int numParameters;
    }
    /**
     * String pattern which represents the end of a code block.
     * Both _BLOCKSTART and _BLOCKEND cannot be the prefix of the other.
     */
    private static final String _BLOCKEND = "/**/";

    /**
     * String pattern which represents the start of a code block.
     */
    private static final String _BLOCKSTART = "/***";

    /**
     * String pattern which represents the end of a code block header.
     */
    private static final String _HEADEREND = "***/";

    /**
     * The code block table that stores the code blocks information,
     * like the code body (StringBuffer), signatures, .c helper class
     * associated with the code blocks. It uses code block Signature
     * as keys.
     */
    private CodeBlockTable _declarations = null;

    /**
     * The path of the current .c file being parsed.
     */
    private String _filePath = null;
    
    /**
     * The helper associated with this code stream.
     */
    private CodeGeneratorHelper _helper = null;

    /**
     * Index pointer that indicates the current location
     * within the .c file to be parsed.
     */
    private int _parseIndex = 0;

    /**
     * The content of this CodeStream.
     */
    private StringBuffer _stream = new StringBuffer();
}
