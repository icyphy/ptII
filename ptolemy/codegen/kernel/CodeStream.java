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
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

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
 *     LinkedList args = new LinkedList();
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
        this._codeGenerator = _helper._codeGenerator;
    }

    /**
     * Construct a new code stream associated with the given java actor
     * helper. Each actor should have its own codestream during code
     * generation.
     * @param templateArguments Template arguments to be substituted
     * in the code.  Template arguments begin with "<" and end with ">".
     * @param helper The actor helper associated with this code stream,
     * which is currently ignored.
     */
    public CodeStream(List templateArguments, CodeGeneratorHelper helper) {
        this(helper);
        _templateArguments = templateArguments;
    }

    /**
     * Construct a new code stream, given a specified file path of the
     * helper .[target] file as a URL suitable for
     * {@link ptolemy.util.FileUtilities#openForReading(String, URI, ClassLoader)},
     *  for example "file:./test/testCodeBlock.c".
     * @param path The given file path.
     * @param generator The generator associated with this CodeStream.
     */
    public CodeStream(String path, CodeGenerator generator) {
        _filePath = path;
        _codeGenerator = generator;
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
     * invokes appendCodeBlock(String, LinkedList) with no arguments by
     * passing an empty array list of argments. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, List, boolean)
     * @param blockName The given code block name.
     * @exception IllegalActionException If
     *  appendCodeBlock(String, List, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName) throws IllegalActionException {
        appendCodeBlock(blockName, new LinkedList(), false);
    }

    /**
     * Append the code block specified the given block name. This method
     * invokes appendCodeBlock(String, LinkedList) with no arguments by
     * passing an empty array list of argments. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, List, boolean)
     * @param blockName The given code block name.
     * @param mayNotExist Indicate if it is okay not to find the code block.
     *  if the code block has parameters.
     * @exception IllegalActionException If
     *  appendCodeBlock(String, List, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName, boolean mayNotExist)
            throws IllegalActionException {
        appendCodeBlock(blockName, new LinkedList(), mayNotExist);
    }

    /**
     * Append the code block specified the given block name. This method
     * invokes appendCodeBlock(String, LinkedList) with no arguments by
     * passing an empty array list of argments. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, List, boolean)
     * @param blockName The given code block name.
     * @param mayNotExist Indicate if it is okay not to find the code block.
     *  if the code block has parameters.
     * @param indentLevel The level of indention.
     * @exception IllegalActionException If
     *  appendCodeBlock(String, List, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName, boolean mayNotExist,
            int indentLevel) throws IllegalActionException {
        appendCodeBlock(blockName, new LinkedList(), mayNotExist, indentLevel);
    }

    /**
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, List, boolean)
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     *  if the code block has parameters.
     * @exception IllegalActionException If
     *  appendCodeBlock(String, List, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName, List arguments)
            throws IllegalActionException {
        appendCodeBlock(blockName, arguments, false);
    }

    /**
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list. The requested
     * code block is required to exist.
     * @see #appendCodeBlock(String, List, boolean)
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     *  if the code block has parameters.
     * @param indentLevel The level of indention.
     * @exception IllegalActionException If
     *  appendCodeBlock(String, List, boolean) throws the exception.
     */
    public void appendCodeBlock(String blockName, List arguments,
            int indentLevel) throws IllegalActionException {
        appendCodeBlock(blockName, arguments, false, indentLevel);
    }

    /**
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list.
     * The initial default level of indention is 0.  To change the
     * the level of indentation, call {@link #setIndentLevel(int)}.
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     * @param mayNotExist Indicate if it is okay not to find the code block.
     *  if the code block has parameters.
     * @exception IllegalActionException If _constructCodeTable() throws
     *  the exception, or if the requested code block is required but cannot
     *  be found, or if the numbers of arguments and parameters do not match.
     */
    public void appendCodeBlock(String blockName, List arguments,
            boolean mayNotExist) throws IllegalActionException {
        appendCodeBlock(blockName, arguments, mayNotExist, _indentLevel);
    }

    /**
     * Append the specific code block with an array of arguments and
     * substitute each argument with the parameters of the code block in
     * the order listed in the given arguments array list.
     * @param blockName The name of the code block.
     * @param arguments The user-specified arguments for the code block,
     * @param mayNotExist Indicate if it is okay not to find the code block.
     *  if the code block has parameters.
     * @param indentLevel The level of indention.
     * @exception IllegalActionException If _constructCodeTable() throws
     *  the exception, or if the requested code block is required but cannot
     *  be found, or if the numbers of arguments and parameters do not match.
     */
    public void appendCodeBlock(String blockName, List arguments,
            boolean mayNotExist, int indentLevel) throws IllegalActionException {
        if (!mayNotExist && arguments.size() == 0) {
            // That means this is a request by the user. This check prevents
            // user from appending duplicate code blocks that are already
            // appended by the code generator by default.
            String[] blocks = CodeGeneratorHelper.getDefaultBlocks();

            for (int i = 0; i < blocks.length; i++) {
                
                // The default blocks are automatically appended 
                // by CodeGeneratorHelper.
                if (_helper != null && blockName.matches(blocks[i])) {
                    throw new IllegalActionException(
                            blockName
                            + " -- is a code block that is appended by default.");
                }
            }
        }

        String codeBlock = getCodeBlock(blockName, arguments, mayNotExist);
        
        _stream.append(codeBlock);
    }

    /** Return a codeBlock with a given name and substitute in the
     * given arguments.  The codeBlock must exist or an exception is thrown.
     * @param blockName The given name that identifies the code block.
     * @param arguments The list of arguments to substitute in the code block.
     * @return The content contained by the code block with the given name.
     * @throws IllegalActionException Thrown if 
     *  getCodeBlock(String, List, boolean) throws it.
     */
    public String getCodeBlock(String blockName, List arguments) throws IllegalActionException {
        return getCodeBlock(blockName, arguments, false);
    }
    
    /** Return a codeBlock with a given name and substitute in the
     * given arguments.
     * @param blockName The given name that identifies the code block.
     * @param arguments The list of arguments to substitute in the code block.
     * @param mayNotExist False to require the codeblock to exist.
     * @return The content contained by the code block with the given name.
     * @throws IllegalActionException Thrown if a problem occurs in
     * constructing the code block table, or the given code block name
     * is required to exist but does not.
     */
    public String getCodeBlock(String blockName, List arguments, 
            boolean mayNotExist) throws IllegalActionException {

        // First, it checks if the code file is parsed already.
        // If so, it gets the code block from the well-constructed code
        // block table.  If not, it has to construct the table.
        if (_doParsing) {
            _constructCodeTable(mayNotExist);
        }

        Signature signature = new Signature(blockName, arguments.size());

        StringBuffer codeBlock = _declarations.getCode(signature, arguments);

        // Cannot find a code block with the matching signature.
        if (codeBlock == null) {
            if (mayNotExist) {
                return "";
            } else {
                throw new IllegalActionException(_helper,
                        "Cannot find code block: \"" + signature + "\" in "
                        + _filePath + ".");
            }
        }

        return codeBlock.toString();
    }

    /**
     * Append multiple code blocks whose names match the given regular
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
        if (_doParsing) {
            _constructCodeTable(true);
        }

        Iterator allSignatures = _declarations.keys();
        while (allSignatures.hasNext()) {
            Signature signature = (Signature) allSignatures.next();
            if (signature.numParameters == 0
                    && signature.functionName.matches(nameExpression)) {
                //_stream.append(_declarations.getCode(signature, new LinkedList()));
                appendCodeBlock(signature.functionName, new LinkedList(), false,
                        _indentLevel);
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
     * Return a String that contains all the code block names and
     * bodies from the associated helper .[target] file.
     * @return The content from parsing the helper .[target] file.
     * @exception IllegalActionException If an error occurs during parsing.
     */
    public String description() throws IllegalActionException {
        StringBuffer buffer = new StringBuffer();

        if (_doParsing) {
            _constructCodeTable(true);
        }

        for (Iterator keys = _declarations.keys(); keys.hasNext();) {
            Signature signature = (Signature) keys.next();
            buffer.append(signature.functionName);

            List parameters = _declarations.getParameters(signature);

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

            buffer.append(":" + _eol);
            buffer.append(_declarations.getCode(signature, new LinkedList()));
            buffer.append(_eol + "-------------------------------" + _eol
                    + _eol);
        }

        return buffer.toString();
    }

    /**
     * Return a list of code block names contained by this CodeStream.
     * @return The list of code block names contained by this CodeStream.
     */
    public List<String> getAllCodeBlockNames() {
        List<String> result = new LinkedList<String>();
        Iterator signatures = _declarations.keys();
        while (signatures.hasNext()) {
            Signature signature = (Signature) signatures.next();
            result.add(signature.functionName);
        }
        return result;
    }
    
    /**
     * Return a set of code block signatures contained by this CodeStream.
     * @return The set of code block signatures contained by this CodeStream.
     * @exception IllegalActionException If there is a problem when
     * parsing the code helper .[target] file.
     */
    public Set<Signature> getAllCodeBlockSignatures() 
            throws IllegalActionException {
        if (_doParsing) {
            _constructCodeTable(true);
        }
        return _declarations.keySet();
    }

    /** Given a code block name, return the corresponding code block.
     *  @param name The name of the code block.
     *  @return The code block with the name.
     *  @exception IllegalActionException If a code block by that
     *  name cannot be found.
     */
    public String getCodeBlock(String name) throws IllegalActionException {
        StringBuffer result = _declarations.getCode(new Signature(name, 0),
                new LinkedList());

        if (result == null) {
            throw new IllegalActionException("Cannot find code block: \""
                    + name + "\".");
        }
        return result.toString();
    }

    /** Given a code block signature, return the corresponding code block
     *  template.
     *  @param signature  The signature of the code block.
     *  @return The code block template that matches the signature, or
     *  the empty string if a code block with that signature cannot
     *  be found.
     *  @exception IllegalActionException If thrown whilegetting a code block
     *  template with the name of the signature.
     */
    public String getCodeBlockTemplate(Object signature)
            throws IllegalActionException {
        if (signature instanceof Signature) {
            return _declarations.getTemplateCode((Signature) signature)
                    .toString();
        }
        return "";
    }

    
    /** Indent the string to the default indent level.
     * @param inputString The string to be indented.
     * @return The indented string.
     */
    public static String indent(String inputString) {
        return indent(_indentLevel, inputString);
    }

    /** Indent the string to the specified indent level.
     * @param indentLevel The level of indention.
     * @param inputString The string to be indented
     * @return The indented string.
     */
    public static String indent(int indentLevel, String inputString) {
        if (indentLevel <= 0) {
            return inputString;
        }

        String indent = StringUtilities.getIndentPrefix(indentLevel);
        // For every line.separator, substitute line.separator + indent.
        String tmpString = StringUtilities.substitute(inputString, _eol, _eol
                + indent);
        if (tmpString.endsWith(_eol + indent)) {
            // Chop off the last indent
            tmpString = tmpString.substring(0, tmpString.length()
                    - indent.length());
        }
        // Insert the initial indent.
        return indent + tmpString;
    }

    /**
     * Insert the contents of the given String to this code stream
     * at the given position.
     * @param offset The given position.
     * @param code The given string.
     */
    public void insert(int offset, String code) {
        _stream.insert(offset, code);
    }

    /**
     * return a boolean indicating if this stream is empty.
     * @return true if this stream is empty.
     */
    public boolean isEmpty() {
        return _stream.length() == 0;
    }

    /**
     * Simple stand alone test method. Parse a helper .[target] file,
     * and print all the code blocks.
     * @param args Command-line arguments, the first of which names a
     * .[target] file as a URL , for example file:./test/testCodeBlock.c.
     * @exception IOException If an error occurs when reading user inputs.
     * @exception IllegalActionException If an error occurs during parsing
     *  the helper .[target] file.
     */
    public static void main(String[] args) throws IOException,
            IllegalActionException {
        try {
            CodeStream code = new CodeStream(args[0], null);

            System.out.println(_eol + "----------Result-----------------------"
                    + _eol);
            //System.out.println(code.description());
            System.out.println(_eol + "----------Result-----------------------"
                    + _eol);

            LinkedList codeBlockArgs = new LinkedList();
            codeBlockArgs.add(Integer.toString(3));
            code.appendCodeBlock("initBlock", codeBlockArgs, false);
            System.out.println(code);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse additional code blocks from the file specified by the given
     * file path. The new code blocks will be put alongside and have higher
     * precedence than the code blocks already contained by this CodeStream.
     * Also, the specified file path is required to point to an existing
     * file; otherwise, an exception is thrown.
     * @param filePath The given file path.
     * @exception IllegalActionException Thrown if an error occurs when
     *  parsing the code blocks in the file.
     */
    public void parse(String filePath) throws IllegalActionException {
        // Since CodeStream does lazy evaluation, we have to force it
        // to parse the previous specified code blocks.
        if (_doParsing) {
            _constructCodeTable(true);
        }

        // Set the new filePath.
        _filePath = filePath;

        // We do not follow the lazy-eval semantics here because the
        // user explicitly specified parsing here.
        _constructCodeTable(false);
    }

    /** Reset this CodeStream object so that its code table will be
     *  re-constructed when needed.
     */
    public void reset() {
        _doParsing = true;
        _declarations = null;
    }

    /** Set the code blocks which will be parsed instead of .c file.
     *  @param codeBlocks The code blocks to be parsed.
     */
    public void setCodeBlocks(String codeBlocks) {
        _codeBlocks = codeBlocks;
    }

    /** Set the indent level.
     *  @param indentLevel The indent level, where 0 means no indentation,
     *  1 means indent one level (probably 4 spaces).
     */
    public static void setIndentLevel(int indentLevel) {
        _indentLevel = indentLevel;
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

    private static String _checkArgumentName(String name)
            throws IllegalActionException {
        if (name.startsWith("$")) {
            return '\\' + name;
        } else {
            return name;
        }
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
                    + "\" is not well-formed." + _eol
                    + "Parameter name for code block needs to starts with '$'");
        }
        //name.matches("[a-zA-Z_0-9]");
        return '\\' + name;
    }

    /**
     * Read the code blocks associated with this helper and construct the code
     * block and parameter table. If there is a pre-specified file path to
     * read from, it only reads code block from the specified file only.
     * Otherwise, it recursively searches code blocks from super classes'
     * helpers.
     * @param mayNotExist Indicate if the file is required to exist.
     * @param filePath The given .[target] file to read from.
     * @exception IllegalActionException If an error occurs when parsing the
     *  helper .[target] file.
     */
    private void _constructCodeTable(boolean mayNotExist)
            throws IllegalActionException {

        if (_declarations == null) {
            _declarations = new CodeBlockTable();
        }

        if (_codeBlocks != null) {
            _constructCodeTableHelper(mayNotExist);

        } else if (_filePath != null) {
            // Use the pre-specified file path.
            _constructCodeTableHelper(mayNotExist);
        } else {
            for (Class helperClass = _helper.getClass(); helperClass != null; helperClass = helperClass
                    .getSuperclass()) {

                _filePath = _getPath(helperClass);

                _constructCodeTableHelper(mayNotExist);

                mayNotExist = true; // Superclass
            }
        }

        _doParsing = false;
    }

    /**
     *
     * @param mayNotExist Indicate if the file is required to exist.
     * @exception IllegalActionException
     */
    private void _constructCodeTableHelper(boolean mayNotExist)
            throws IllegalActionException {
        BufferedReader reader = null;

        try {
            StringBuffer codeToBeParsed = new StringBuffer();

            if (_codeBlocks != null) {
                codeToBeParsed.append(_codeBlocks);
            } else {
                // Open the .c file for reading.
                reader = FileUtilities.openForReading(_filePath, null, null);

                if (reader == null) {
                    return;
                }

                int lineNumber = 1;

                String filename = FileUtilities
                        .nameToURL(_filePath, null, null).getPath();

                if (_codeGenerator == null && _helper != null) {
                    _codeGenerator = _helper._codeGenerator;
                }

                // Read the entire content of the code block file.
                for (String line = reader.readLine(); line != null; line = reader
                        .readLine(), lineNumber++) {

                    if (_needLineInfo()) {
                        codeToBeParsed.append(_codeGenerator.generateLineInfo(
                                lineNumber, filename));
                    }
                    codeToBeParsed.append(line + _eol);
                }
            }

            if (_templateArguments != null) {
                // Template parameter substitution.
                _templateParameters = _parseParameterList(codeToBeParsed, 0,
                        codeToBeParsed.indexOf(">"), "<", ">");

                codeToBeParsed = _substituteParameters(codeToBeParsed,
                        _templateParameters, _templateArguments);
            }

            _declarations.addScope();

            // repeatedly parse the file
            while (_parseCodeBlock(codeToBeParsed) != null) {
                ;
            }

        } catch (IllegalActionException ex) {
            reset();
            throw ex;

        } catch (IOException ex) {
            if (reader == null) {
                if (mayNotExist) {
                    /* System.out.println("Warning: Helper .[target] file " +
                     _filePath + " not found"); */
                } else {
                    reset();
                    throw new IllegalActionException(null, ex,
                            "Cannot open file: " + _filePath);
                }
            } else {
                reset();
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

    /** Return true if the generated source code is bound to the line
     *  number and file of the helper templates.
     * @return True if the generated source code is bound to the line
     *  number and file of the helper templates.    Return false
     *  if the source is bound only to the output file, or if there is
     *  no CodeGenerator associated with this stream.
     * @exception IllegalActionException If there is a problem reading
     *  the {@link ptolemy.codegen.kernel.CodeGenerator#sourceLineBinding}
     *  parameter.
     */
    private boolean _needLineInfo() throws IllegalActionException {
        Token sourceLineBinding = null;

        if (_codeGenerator != null) {
            sourceLineBinding = _codeGenerator.sourceLineBinding.getToken();
        } else {
            return false;
        }
        return ((BooleanToken) sourceLineBinding).booleanValue();
    }

    /**
     * Get the file path for the helper .[target] file associated with
     * the given helper class.  If the helper has no code generator,
     * then the empty string is returned.
     * @param helperClass The given helper class
     * @return Path for the helper .[target] file.
     */
    private String _getPath(Class helperClass) {
        CodeGenerator codeGenerator = _helper.getCodeGenerator();
        if (codeGenerator == null) {
            return "";
        }
        String extension = _helper._codeGenerator.generatorPackage
                .getExpression();
        extension = extension.substring(extension.lastIndexOf(".") + 1);
        return "$CLASSPATH/" + helperClass.getName().replace('.', '/') + "."
                + extension;
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

        StringBuffer body = new StringBuffer(codeInFile.substring(_parseIndex,
                endIndex));

        // strip beginning new lines and white spaces
        while (body.length() > 0
                && (body.charAt(0) == '\n' || body.charAt(0) == '\r' || body
                        .charAt(0) == ' ')) {
            body.deleteCharAt(0);
        }
        // strip ending new lines and white spaces
        int endChar = body.length() - 1;
        while (endChar >= 0
                && (body.charAt(endChar) == '\n'
                        || body.charAt(endChar) == '\r' || body.charAt(endChar) == ' ')) {
            body.deleteCharAt(endChar);
            endChar = body.length() - 1;
        }
        // add back one ending new line
        body.append(_eol);

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
            String name = _checkCodeBlockName(codeInFile.substring(_parseIndex,
                    endIndex));

            signature = new Signature(name, 0);

            // Add an empty parameter list.
            _declarations.putParameters(signature, new LinkedList());

        } else {
            String name = _checkCodeBlockName(codeInFile.substring(_parseIndex,
                    parameterIndex));

            int parameterEndIndex = codeInFile.indexOf(")", _parseIndex);

            List parameterList = _parseParameterList(codeInFile, _parseIndex,
                    parameterEndIndex);

            signature = new Signature(name, parameterList.size());

            _declarations.putParameters(signature, parameterList);
        }

        _parseIndex = _HEADEREND.length() + endIndex;
        return signature;
    }

    private boolean _doParsing = true;

    private static List _parseParameterList(StringBuffer codeInFile, int start,
            int end) {

        return _parseParameterList(codeInFile, start, end, "(", ")");
    }

    /**
     * Return a list of parameter expression contains in the given string
     * buffer. It parses comma-separated expressions and also understand
     * nested parenthesis by matching open and close parenthesis pairs.
     * @param codeInFile The given string buffer.
     * @param start The given start index to start parsing.
     * @param end The given end index to stop parsing.
     * @return The list of parameter expressions.
     */
    private static LinkedList _parseParameterList(StringBuffer codeInFile,
            int start, int end, String startSymbol, String endSymbol) {

        int parameterIndex = codeInFile.indexOf(startSymbol, start);

        LinkedList parameterList = new LinkedList();

        // Keep parsing for extra parameters.
        for (int commaIndex = codeInFile.indexOf(",", start); commaIndex != -1
                && (commaIndex < end); commaIndex = codeInFile.indexOf(",",
                commaIndex + 1)) {

            String newParameter = codeInFile.substring(parameterIndex + 1,
                    commaIndex);

            int openIndex = 0;
            int closeIndex = 0;
            do {
                openIndex = newParameter.indexOf(startSymbol, openIndex + 1);
                closeIndex = newParameter.indexOf(endSymbol, closeIndex + 1);
            } while (openIndex >= 0 && closeIndex >= 0);

            // It matches the number of open and close parenthesis pairs
            // in order to parse nested parenthesis.
            if (openIndex < 0 && closeIndex < 0) {
                parameterList.add(newParameter.trim());
                parameterIndex = commaIndex;
            }
        }

        String newParameter = codeInFile.substring(parameterIndex + 1, end);

        if (newParameter.trim().length() > 0) {
            parameterList.add(newParameter.trim());
        }

        return parameterList;
    }

    /**
     * Substitute parameters.
     * This method searches all occurences of each parameter expressions
     * in the given code block content and replace each with the given
     * arguments using straight-forward text substitution.
     * @param codeBlock The given code block content.
     * @param parameters The given list of parameters.
     * @param arguments The given list of arguments.
     * @return The code block content after parameter substitutions.
     * @exception IllegalActionException Thrown if
     *  _checkParameterName(String) throws it.
     */
    private static StringBuffer _substituteParameters(StringBuffer codeBlock,
            List parameters, List arguments) throws IllegalActionException {
        // Text-substitute for each parameters.
        for (int i = 0; i < arguments.size(); i++) {

            //String replaceString = arguments.get(i).toString();
            String replaceString = _checkArgumentName(arguments.get(i)
                    .toString());
            String parameterName = _checkParameterName(parameters.get(i)
                    .toString());
            try {
                codeBlock = new StringBuffer(codeBlock.toString().replaceAll(
                        parameterName, replaceString));
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Failed to replace \"" + parameterName + "\" with \""
                                + replaceString + "\"");
            }
        }
        return codeBlock;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /**
     * The code block table class. A code block table contains
     * code objects, which consists of the file path of the code
     * block, the code block content, and the parameter list. The
     * table is represented using a LinkedList of scopes, where the
     * first element is scope for the left child class and each latter
     * element is parent scope of the prior.
     */
    private class CodeBlockTable {

        /**
         * Constructor for a code block table.
         */
        public CodeBlockTable() {
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * Add a new scope to the code block table. This assumes parent
         * scope is being added always add to the end of the list.
         */
        public void addScope() {
            _codeTableList.addLast(new Hashtable());
        }

        /**
         * Return the code block content of the given signature and
         * substitute parameters with the given list of arguments.
         * This searches from the all scopes from the code block table.
         * @param signature The given code block signature.
         * @param arguments The given list of arguments.
         * @return The code block content.
         * @exception IllegalActionException Thrown if
         *  getCode(Signature, List, List) throws it.
         */
        public StringBuffer getCode(Signature signature, List arguments)
                throws IllegalActionException {
            return _getCode(signature, arguments, _codeTableList);
        }

        /**
         * Return the code block content of the given signature without
         * substituting parameters. This searches from the all scopes 
         * from the code block table.
         * @param signature The given code block signature.
         * @return The code block content.
         * @exception IllegalActionException Thrown if
         *  getCode(Signature, List, List) throws it.
         */
        public StringBuffer getTemplateCode(Signature signature) 
                throws IllegalActionException {
            
            StringBuffer result = new StringBuffer();
            
            result.append("/*** " + 
                    _getHeader(signature, _codeTableList) + " ***/\n");
            result.append(_getCode(signature, null, _codeTableList));
            result.append("/**/\n\n");
            return result;
        }
        
        /**
         * Get the list of parameters for the code block with the given
         * signature. This searches the code block from the entire list
         * of scopes.
         * @param signature The given code block signature.
         * @return The list of parameters strings.
         */
        public List getParameters(Signature signature) {
            return _getParameters(signature, _codeTableList);
        }

        /**
         * Put the given file path and code content into the current scope
         * using the given signature as the key. It assumes the given
         * signature already exists in the current scope.
         * @param signature The given signature.
         * @param filePath The given file path.
         * @param code The given code content.
         */
        public void putCode(Signature signature, String filePath,
                StringBuffer code) {

            Hashtable currentScope = (Hashtable) _codeTableList.getLast();

            Object[] codeBlock = (Object[]) currentScope.get(signature);
            codeBlock[0] = filePath;
            codeBlock[1] = code;
        }

        /**
         * Put the given list parameters into the current scope using the
         * given signature as the key.
         * @param signature The given signature.
         * @param parameters The given list of parameters.
         * @exception IllegalActionException Thrown if the given signature
         *  already exists in the current scope.
         */
        public void putParameters(Signature signature, List parameters)
                throws IllegalActionException {
            Hashtable currentScope = (Hashtable) _codeTableList.getLast();
            currentScope.get(signature);
            Object[] codeBlock = new Object[3];
            codeBlock[2] = parameters;

            if (currentScope.containsKey(signature)) {
                throw new IllegalActionException(
                        "Multiple code blocks have the same signature: "
                                + signature + " in " + _filePath);
            }

            currentScope.put(signature, codeBlock);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /**
         * Return the code block content of the given signature and
         * substitute parameters with the given list of arguments.
         * This searches from the specified list of scopes.
         * @param signature The given signature.
         * @param arguments The given list of arguments.
         * @param scopeList The given list of scopes.
         * @return The code block content of the given signature.
         * @exception IllegalActionException Thrown if the code block for a
         *  super call cannot be found, or if substituteParameters(
         *  StringBuffer, List, List) throws it.
         */
        private StringBuffer _getCode(Signature signature, List arguments,
                List scopeList) throws IllegalActionException {

            int size = scopeList.size();

            if (size == 0) {
                return null;
            }

            Hashtable table = (Hashtable) scopeList.get(0);

            if (!table.containsKey(signature)) {
                return _getCode(signature, arguments, scopeList
                        .subList(1, size));
            } else {
                Object[] codeObject = (Object[]) table.get(signature);
                StringBuffer codeBlock = (StringBuffer) codeObject[1];
                List parameters = (List) codeObject[2];

                if (arguments != null) {
                    codeBlock = _substituteParameters(codeBlock, parameters,
                            arguments);
    
                    codeBlock = _substituteSuper(signature, 
                            scopeList, codeObject, codeBlock);
                }                

                return codeBlock;

            }
        }
        
        private String _getHeader(Signature signature,
                List scopeList) throws IllegalActionException {
            int size = scopeList.size();

            if (size == 0) {
                return null;
            }

            Hashtable table = (Hashtable) scopeList.get(0);

            if (!table.containsKey(signature)) {
                return _getHeader(signature, scopeList
                        .subList(1, size));
            } else {
                Object[] codeObject = (Object[]) table.get(signature);
                Iterator parameters = ((List) codeObject[2]).iterator();

                StringBuffer header = new StringBuffer(signature.functionName + "(");
                while (parameters.hasNext()) {
                    header.append("$" + parameters.next());
                    if (parameters.hasNext()) {
                        header.append(", ");
                    }
                }
                return header.toString() + ")";
            }
        }

        /**
         * @param signature
         * @param scopeList
         * @param codeObject
         * @param codeBlock
         * @return
         * @throws IllegalActionException
         */
        private StringBuffer _substituteSuper(Signature signature, 
            List scopeList, Object[] codeObject, StringBuffer codeBlock) 
                throws IllegalActionException {
            
            String callExpression = "(\\$super\\s*\\.\\s*\\w+\\s*\\(.*\\)\\s*;)"
                    + "|(\\$this\\s*\\.\\s*\\w+\\s*\\(.*\\)\\s*;)"
                    + "|(\\$super\\s*\\(.*\\)\\s*;)";

            String[] subBlocks = codeBlock.toString().split(callExpression);

            StringBuffer returnCode = new StringBuffer(subBlocks[0]);

            Pattern pattern = Pattern.compile(callExpression);
            Matcher matcher = pattern.matcher(codeBlock);

            for (int i = 1; i < subBlocks.length; i++) {

                String call = "";

                if (matcher.find()) {
                    call = matcher.group();
                }

                int dotIndex = call.indexOf(".");
                int openIndex = call.indexOf("(");

                boolean isSuper = call.contains("super");
                boolean isImplicit = dotIndex < 0 || dotIndex > openIndex;

                String blockName = (isImplicit) ? signature.functionName
                        : call.substring(dotIndex + 1, openIndex).trim();

                List callArguments = CodeStream._parseParameterList(
                        new StringBuffer(call), 0, call.length() - 2);

                Signature callSignature = new Signature(blockName,
                        callArguments.size());

                if (!isSuper && callSignature.equals(signature)) {
                    throw new IllegalActionException(_helper, callSignature
                            .toString()
                            + " recursively appends itself in "
                            + codeObject[0]);
                }

                StringBuffer callCodeBlock = (!isSuper) ? getCode(
                        callSignature, callArguments) : _getCode(
                        callSignature, callArguments, scopeList.subList(1,
                                scopeList.size()));

                if (callCodeBlock == null) {
                    throw new IllegalActionException(_helper,
                            "Cannot find " + (isSuper ? "super" : "this")
                                    + " block for " + callSignature
                                    + " in " + codeObject[0]);
                }

                //superBlock.insert(0, "///////// Super Block ///////////////\n");
                //superBlock.append("///////// End of Super Block ////////\n");

                returnCode.append(callCodeBlock);
                returnCode.append(subBlocks[i]);
            }
            return returnCode;
        }

        /**
         * Get the parameters for the code block with the given signature.
         * It search the code block from the given list of scopes.
         * @param signature The given signature.
         * @param scopeList The given list of scopes.
         * @return The list of parameter strings.
         */
        private List _getParameters(Signature signature, List scopeList) {

            if (scopeList.isEmpty()) {
                return new LinkedList();
            }
            Hashtable currentScope = (Hashtable) scopeList.get(0);

            if (currentScope.containsKey(signature)) {
                return (List) ((Object[]) currentScope.get(signature))[2];

            } else {
                return _getParameters(signature, scopeList.subList(1, scopeList
                        .size()));
            }
        }

        private Set keySet() {
            HashSet signatures = new HashSet();

            for (Hashtable table : _codeTableList) {
                signatures.addAll(table.keySet());
            }
            return signatures;
        }
        
        /**
         * Return all contained signature keys. This method is used for
         * testing purposes.
         * @return A Iterator of all contained signature keys.
         */
        private Iterator keys() {
            return keySet().iterator();
        }

        /**
         * LinkedList of Hashtable of code blocks. Each index of the
         * LinkedList represents a separate helper .c code block file.
         */
        private LinkedList<Hashtable> _codeTableList = new LinkedList<Hashtable>();
    }

    /**
     * A private class for representing a code block signature.
     */
    private static class Signature implements Comparable {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Constructor for a code block signature. It consists of the
         * code block name and the number of parameters.
         * @param functionName The given code block name.
         * @param numParameters The number of parameters.
         * @throw IllegalActionException Thrown if the given name is null,
         *  or the number of parameters is less than zero.
         */
        public Signature(String functionName, int numParameters)
                throws IllegalActionException {

            if (functionName == null || numParameters < 0) {
                throw new IllegalActionException("Bad code block signature: ("
                        + functionName + ") with " + numParameters
                        + " parameters.");
            }

            this.functionName = functionName;

            this.numParameters = numParameters;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * Return true if the given object is equal to this signature.
         * @param object The given object.
         */
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
            StringBuffer result = new StringBuffer(functionName + "(");
            for (int i = 0; i < numParameters; i++) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append("$");
            }
            return result.toString() + ")";
        }

        /**
         * The code block name.
         */
        public String functionName;

        /**
         * The number of parameters.
         */
        public int numParameters;

        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
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

    /** The indent level. */
    private static int _indentLevel = 0;

    /** The code generator associated with this code stream.
     */
    protected CodeGenerator _codeGenerator;

    private String _codeBlocks;

    /**
     * The code block table that stores the code blocks information,
     * like the code body (StringBuffer), signatures, .c helper class
     * associated with the code blocks. It uses code block Signature
     * as keys.
     */
    private CodeBlockTable _declarations = null;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line charactor so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    private static String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

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

    private List _templateArguments;

    private List _templateParameters;

}
