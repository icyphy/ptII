/*
 @Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.codegen.c.actor.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
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
 *     &#47;*** initBlock (arg) ***&#47;
 *         if ($ref(input) != arg) {
 *             $ref(output) = arg;
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
 * by the codegen kernel. CodeStream do not support method overriding, so
 * each code block name within the same .c helper file have to be unique.
 * 
 * @author Man-Kit
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class CodeStream {
    
    /**
     * Construct a new code stream associated with the given java actor
     * helper. Each actor should have its own codestream during code
     * generation.
     * @param helper The actor helper associated with this code stream.
     */
    public CodeStream(CCodeGeneratorHelper helper) {
        _actorHelper = helper;
        String classNamePath = helper.getClass().getName()
                .replace('.', '/');
        _filePath = "$CLASSPATH/" + classNamePath + ".c";
    }

    /**
     * Construct a new code stream, given a specified file path of the
     * helper .c file.
     * @param path The given file path.
     */
    public CodeStream(String path) {
        _filePath = path;
    }
    
    /** 
     * Append the contents of the given CodeStream to this code stream.
     * @param codeBlock The given code stream.
     */
    public void append(CodeStream codeBlock) {
        _stream.append(codeBlock.toString());
    }

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
    public void append(StringBuffer codeBlock) {
        _stream.append(codeBlock);
    }

    /** 
     * Append the code block specified the given block name. This method
     * invokes appendCodeBlock(String, ArrayList) with no arguments by
     * passing an empty array list of argments.
     * @see #appendCodeBlock(String, ArrayList)
     * @param blockName The given code block name.
     * @exception IllegalActionException If appendCodeBlock(String, ArrayList)
     *  throws the exception.
     */
    public void appendCodeBlock(String blockName) 
        throws IllegalActionException {
        appendCodeBlock(blockName, new ArrayList());
    }

    /** 
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list.
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     *  if the code block has parameters.
     * @exception IllegalActionException If _constructCodeTable() throws
     *  the exception, or if the code block cannot be found, or if the
     *  numbers of arguments and parameters do not match.
     */
    public void appendCodeBlock(String blockName, ArrayList arguments)
            throws IllegalActionException {
        // First, it checks if the code file is parsed already.
        // If so, it gets the code block from the well-constructed code
        // block table.  If not, it has to construct the table.
        if (_codeBlockTable == null) {
            _constructCodeTable();
        }
        StringBuffer codeBlock = (StringBuffer) _codeBlockTable.get(blockName);

        if (codeBlock == null) {
            throw new IllegalActionException("Cannot find code block: "
                    + blockName + " in " + _filePath + ".");
        }

        ArrayList parameters = (ArrayList) _parameterTable.get(blockName);
        if (parameters == null) {
            if (arguments.size() != 0) {
            	throw new IllegalActionException(blockName + " in " +
                    _filePath + "does not take any arguments.");
            }
        }
        else {
            // Check if there are more parameters than arguments.
            if (parameters.size() - arguments.size() < 0) {
                throw new IllegalActionException(blockName + " in " + 
                    _filePath + " only takes " + parameters.size() + 
                    " arguments.");
            }
            // Check if there are more arguments than parameters.
            else if (parameters.size() - arguments.size() > 0) {
            	for (int i = arguments.size(); i < parameters.size(); i++) {
                    throw new IllegalActionException(blockName + " in "
                            + _filePath + "expects parameter (" + 
                            parameters.get(i) + ").");
                }
            }
        }
        
        // substitute for each parameters
        for (int i = 0; i < arguments.size(); i++) {
            String replaceString = arguments.get(i).toString();
            codeBlock = new StringBuffer(codeBlock.toString().replaceAll(
                    parameters.get(i).toString(), replaceString));
        }
        _stream.append(codeBlock);
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
     * @exception IllegalActionException If an error occurs during parsing.
     */
    public StringBuffer description() throws IllegalActionException {
        StringBuffer buffer = new StringBuffer();

        if (_codeBlockTable == null) {
            _constructCodeTable();
        }

        for (Iterator keys = _codeBlockTable.keySet().iterator(); keys
                .hasNext();) {
            String key = (String) keys.next();
            buffer.append(key);
            ArrayList parameters = (ArrayList)_parameterTable.get(key);
            if (parameters != null && parameters.size() > 0) {
                for (int i = 0; i < parameters.size(); i++) {
                    if (i == 0) {
                        buffer.append("(" + parameters.get(i));
                    }
                    else {
                    	buffer.append(", " + parameters.get(i));
                    }
                }
                buffer.append(")");
            }
            buffer.append(":\n");
            buffer.append((StringBuffer) _codeBlockTable.get(key));
            buffer.append("\n-------------------------------\n\n");
        }
        return buffer;
    }

    /**
     * Simple stand alone test method. This method prompts the user for
     * the path of the helper .c file, parse and print all code blocks in
     * the helper .c file.
     * @param args Command-line arguments.
     * @exception IOException If an error occurs when reading user inputs.
     * @exception IllegalActionException If an error occurs during parsing
     *  the helper .c file.
     */
    public static void main(String[] args) throws IOException,
            IllegalActionException {
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(System.in));
        System.out.println("----------Testing-------------------------------");

        System.out.print("please input file path: ");
        String filePath = in.readLine();

        System.out.println("\n----------Result------------------------------");
        System.out.println(new CodeStream(filePath).description());
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
     */
    private static String _checkCodeBlockName(String name) {
        // FIXME: extra lexical checking
        // e.g. Do we allow nested code block within code block name??
        // e.g. ...spaces??
        // e.g. ...special symbols??
        return name.trim();
    }

    /**
     * Read the helper .c file identified by the _filePath and construct the
     * code block table and parameter table.
     * @exception IllegalActionException If an error occurs when parsing the
     *  helper .c file.
     */
    private void _constructCodeTable() throws IllegalActionException {

        _codeBlockTable = new Hashtable();
        _parameterTable = new Hashtable();

        BufferedReader reader = null;

        try {
            // open the .c file for reading
            reader = FileUtilities.openForReading(_filePath, null, null);

            StringBuffer codeInFile = new StringBuffer();

            // FIXME: is there a better way to read the entire file?
            // create a string of all code in the file
            for (String line = reader.readLine(); line != null; 
                line = reader.readLine()) {
                codeInFile.append(line + "\n");
            }

            // repeatedly parse the file
            while (_parseCodeBlock(codeInFile) != null) {
                ;
            }            
        } catch (IOException ex) {
            if (reader == null) {
                throw new IllegalActionException(null, ex, 
                        "Cannot open file: " + _filePath);
            } else {
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

        StringBuffer body = 
            new StringBuffer(codeInFile.substring(_parseIndex, endIndex));

        // Recursively parsing for nested code blocks
        for (String subBlockKey = _parseCodeBlock(body); 
            subBlockKey != null;) {
            // FIXME: do we include the nested code block into 
            // the current block??
            //body.append((StringBuffer) _codeBlockTable.get(subBlockKey));
            // FIXME: take away the nested code block from
            // the current code block

        	// reset the parse index to parse the body from the beginning
            _parseIndex = 0;        
            subBlockKey = _parseCodeBlock(body);
        }

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
    private String _parseCodeBlock(StringBuffer codeInFile)
            throws IllegalActionException {
        String name = _parseHeader(codeInFile);

        if (name != null) {
            if (_codeBlockTable.containsKey(name)) {
                throw new IllegalActionException(
                        "Multiple code blocks have the same name: " + name
                                + " in " + _filePath);
            }

            StringBuffer body = _parseBody(codeInFile);
            _codeBlockTable.put(name, body);
        }

        return name;
    }

    /**
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
    private String _parseHeader(StringBuffer codeInFile)
            throws IllegalActionException {
        String name;
        _parseIndex = codeInFile.indexOf(_BLOCKSTART, _parseIndex);

        // Check to see if there are no more code block start headers.
        if (_parseIndex == -1) {
            return null;
        }

        _parseIndex += _BLOCKSTART.length();

        int endIndex = codeInFile.indexOf(_HEADEREND, _parseIndex);
        if (endIndex == -1) {
            throw new IllegalActionException(
                    "Missing code block close header" + " in " + _filePath);
        }

        int parameterIndex = codeInFile.indexOf("(", _parseIndex);
        if (parameterIndex != -1 && parameterIndex < endIndex) {
            name = _checkCodeBlockName(codeInFile.substring(_parseIndex,
                    parameterIndex));
            int parameterEndIndex = codeInFile.indexOf(")", _parseIndex);
            
            if (_parameterTable.get(name) == null) {
                _parameterTable.put(name, new ArrayList());
            }
            ArrayList parameterList = (ArrayList) _parameterTable.get(name);
            
            // keep parsing for extra parameters
            for (int commaIndex = codeInFile.indexOf(",", _parseIndex);
                commaIndex != -1 && commaIndex < parameterEndIndex; 
                commaIndex = codeInFile.indexOf(",", commaIndex + 1)) {
            	
                String newParameter = 
                    codeInFile.substring(parameterIndex + 1, commaIndex);
                parameterList.add(newParameter.trim());
                parameterIndex = commaIndex;
            }
            String newParameter = 
                codeInFile.substring(parameterIndex + 1, parameterEndIndex);
            parameterList.add(newParameter.trim());
        } else {
            name = _checkCodeBlockName(
                    codeInFile.substring(_parseIndex, endIndex));
        }
        _parseIndex = _HEADEREND.length() + endIndex;
        return name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /**
     * String pattern which represents the end of a code block.
     * Both _BLOCKSTART and _BLOCKEND cannot be the prefix of the other.
     */
    private static String _BLOCKEND = "/**/";

    /**
     * String pattern which represents the start of a code block.
     */
    private static String _BLOCKSTART = "/***";

    /**
     * String pattern which represents the end of a code block header.
     */
    private static String _HEADEREND = "***/";

    /**
     * The associated actor helper object.
     */
    private CCodeGeneratorHelper _actorHelper;

    /**
     * The code block table that stores the code block body (StringBuffer)
     * with the code block name (String) as key.
     */
    private Hashtable _codeBlockTable = null;

    /**
     * File path to the .c files associated with this CodeStream's helper.
     */
    private String _filePath;

    /**
     * The code block table that stores the code block parameters
     * (ArrayList) with the code block names (String) as key.
     */
    private Hashtable _parameterTable = null;

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
