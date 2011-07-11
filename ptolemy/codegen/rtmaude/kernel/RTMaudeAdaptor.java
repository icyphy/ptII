/* RTMaude Code generator helper class.

 Copyright (c) 2009-2011 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN AS IS BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.rtmaude.kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.actor.CompositeActor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.codegen.rtmaude.data.expr.PropertyParameter;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// RTMaudeAdaptor

/**
 * Generate RTMaude code in DE domain. Every adaptor (helper) class for
 * RTMaude should extend this class.
 *
 * @see ptolemy.codegen.kernel.CodeGeneratorHelper
 * @author Kyungmin Bae
 * @since Ptolemy II 8.0
 * @version $Id$
 * @Pt.AcceptedRating Red (kquine)
 * @Pt.ProposedRating Red (kquine)
 */
public class RTMaudeAdaptor extends CodeGeneratorHelper {

    /**
     * Constructs a RTMaude code generator adaptor.
     * @param component The associated component
     */
    public RTMaudeAdaptor(NamedObj component) {
        super(component);
        _parseTreeCodeGenerator = getParseTreeCodeGenerator();
    }

    /**
     * Given a block name, generate a list of codes for that block. (For
     * Composite Actor and Director)
     * @param blockName The name of the block.
     * @param args      The arguments for the block.
     * @return The list of the block codes
     * @exception IllegalActionException
     */
    public List<String> getBlockCodeList(String blockName, String... args)
            throws IllegalActionException {
        List<String> rl = new LinkedList();
        String bcode = _generateBlockCode(blockName, args);
        rl.add(bcode);

        return rl;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#generateFireCode()
     */
    public String generateFireCode() throws IllegalActionException {
        String comment = _codeGenerator
                .comment("Fire "
                        + ((getComponent() instanceof CompositeActor) ? "Composite Actor: "
                                : "") + generateName(getComponent()));

        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            return processCode(comment + _generateFireCode()
                    + generateTypeConvertFireCode());
        } else {
            return processCode(comment + generateTermCode());
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#generateFireFunctionCode()
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        return _generateBlockCode(
                "fireFuncBlock",
                CodeStream.indent(1, _generateFireCode()
                        + generateTypeConvertFireCode()));
    }

    /**
     * Generate a Real-time Maude term representation of given component.
     *
     * @return the term representation of a component
     * @exception IllegalActionException
     */
    public String generateTermCode() throws IllegalActionException {
        try {
            return _generateBlockCode(defaultTermBlock); // term block
        } catch (Exception e) // FIXME: temporally ignore exceptions, for avoiding bugs caused by others..
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#getSharedCode()
     */
    public Set getSharedCode() throws IllegalActionException {
        // Use LinkedHashSet to give order to the shared code.
        Set sharedCode = new LinkedHashSet();
        semanticsIncludes = getModuleCode("semantics");
        formalIncludes = getModuleCode("formal");

        for (String m : semanticsIncludes) {
            sharedCode.add(getRTMmodule().get(m));
        }
        for (String m : formalIncludes) {
            sharedCode.add(getRTMmodule().get(m));
        }

        return sharedCode;
    }

    /**
     * Generates code for RTMaude module with the given template
     * indicated by the header of the block. The name of the template block
     * will be "header_Class".
     * @param header    The header of the template block
     * @return The RTMaude module code
     * @exception IllegalActionException
     */
    public List<String> getModuleCode(String header)
            throws IllegalActionException {
        Set<String> blocks = new HashSet(_codeStream.getAllCodeBlockNames());
        LinkedList<String> modNames = new LinkedList();

        if (RTMmodule == null) {
            RTMmodule = new HashMap();
        }

        for (Class c = getComponent().getClass(); !c.getName().equals(
                "ptolemy.kernel.util.NamedObj"); c = c.getSuperclass()) {

            String blockName = header + "_" + c.getSimpleName();
            if (blocks.contains(blockName)) {
                String module = _generateBlockCode(blockName);
                Matcher nameMat = Pattern.compile("tomod\\s+(\\S+)").matcher(
                        module);
                if (!nameMat.find()) {
                    throw new IllegalActionException("Invalid " + header
                            + " module block: No name");
                } else {
                    String name = nameMat.group(1);
                    RTMmodule.put(name, module);
                    modNames.addFirst(name);
                }
            }
        }
        return modNames;
    }

    /**
     * Generates the entry code for RTMaude model, whose template is defined
     * in "mainEntry" block.
     * @return The entry code
     * @exception IllegalActionException
     */
    public String generateEntryCode() throws IllegalActionException {
        LinkedHashSet<String> incs;

        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            incs = new LinkedHashSet(semanticsIncludes);
        } else {
            incs = new LinkedHashSet(getBlockCodeList("funcModuleName"));
        }

        return _generateBlockCode(
                "mainEntry",
                CodeStream.indent(2, new ListTerm<String>("ACTOR-BASE", " +"
                        + _eol, incs).generateCode()));
    }

    /**
     * Generates the exit code for the RTMaude model, whose template is defined
     * in "mainExit" block.
     * @return The exit code
     * @exception IllegalActionException
     */
    public String generateExitCode() throws IllegalActionException {
        LinkedHashSet<String> check_inc_set = new LinkedHashSet<String>();
        StringBuffer commands = new StringBuffer();
        ptolemy.data.Token bound = ((RTMaudeCodeGenerator) _codeGenerator).simulation_bound
                .getToken();

        check_inc_set.add("INIT");
        if (formalIncludes != null) {
            check_inc_set.addAll(formalIncludes);
        } else {
            throw new IllegalActionException(
                    "getSharedCode() has not been invoked");
        }

        if (bound != null) {
            if (bound.toString().equals("Infinity")) {
                commands.append("(rew in INIT : {init} .)");
            } else {
                commands.append("(trew in INIT : {init} in time <= "
                        + IntToken.convert(bound).toString() + " .)");
            }
        }

        for (PropertyParameter p : _codeGenerator
                .attributeList(PropertyParameter.class)) {
            commands.append("(" + p.stringValue() + " .)" + _eol);
        }

        return _generateBlockCode("mainExit", CodeStream.indent(2,
                new ListTerm<String>("MODELCHECK-BASE", " +" + _eol,
                        check_inc_set).generateCode()), commands.toString());
    }

    /**
     * Return a new parse tree code generator to use with expressions.
     * @return the parse tree code generator to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        _parseTreeCodeGenerator = new RTMaudeParseTreeCodeGenerator(); //FIXME: _codeGenerator
        return _parseTreeCodeGenerator;
    }

    /**
     * Returns the RTMmodule value.
     * @return a Map object representing the RTMmodule value
     */
    public Map<String, String> getRTMmodule() {
        return RTMmodule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Returns some information of the component. Subclasses may
     * override this method to define more specific information.
     * @param name       The name of the desired information
     * @param parameters The parameters for the information
     * @return The information indicated by the given name.
     * @exception IllegalActionException
     */
    protected String getInfo(String name, List<String> parameters)
            throws IllegalActionException {
        // FIXME: Rename to _getInfo()
        if (name.equals("name")) {
            return getComponent().getName();
        }
        throw new IllegalActionException("Unknown RTMaudeObj Information");
    }

    /**
     * Given a block name, generate code for that block.
     * @param blockName The name of the block.
     * @param args      The arguments for the block.
     * @return The block code
     * @exception IllegalActionException
     */
    protected String _generateBlockCode(String blockName, String... args)
            throws IllegalActionException {
        return super._generateBlockCode(blockName, Arrays.asList(args));
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#_generateTypeConvertMethod(java.lang.String, java.lang.String, java.lang.String)
     */
    protected String _generateTypeConvertMethod(String ref, String castType,
            String refType) throws IllegalActionException {

        //FIXME: too specific.
        if (refType != null && refType.equals("String")) {
            return "# " + ref;
        }
        if (castType != null && castType.equals("time")) {
            return "toTime(" + ref + ")";
        } else {
            return super._generateTypeConvertMethod(ref, castType, refType);
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#_replaceMacro(java.lang.String, java.lang.String)
     */
    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {
        if (macro.equals("info") || macro.equals("block")) {
            String[] args = parameter.split("(?<!\\\\),");
            for (int i = 1; i < args.length; i++) {
                if (args[i].contains("$")) {
                    args[i] = processCode(args[i]);
                }
            }

            ArrayList<String> largs = new ArrayList(Arrays.asList(args));
            String aName = largs.remove(0);

            if (macro.equals("info")) {
                return getInfo(aName, largs);
            } else {
                return _generateBlockCode(aName, largs);
            }
        }
        if (macro.equals("indent")) {
            return CodeStream.indent(1, processCode(parameter));
        }
        return super._replaceMacro(macro, parameter);
    }

    /** Get the translated expression.
     *  @param expression The expression to be parsed and translated.
     *  @return the translated expression.
     *  @exception IllegalActionException If thrown by the parser.
     */
    protected String getTranslatedExpression(String expression)
            throws IllegalActionException {
        // FIXME: Rename to _getTranslatedExpression
        ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = null;
        try {
            // parse the value of this variable
            parseTree = parser.generateParseTree(expression);
        } catch (Throwable throwable) {
            throw new IllegalActionException(getComponent(), throwable,
                    "Failed to generate parse tree for \"" + getName()
                            + "\". in \"" + this.getContainer() + "\"");
        }
        try {
            parseTreeCodeGenerator.evaluateParseTree(parseTree, null);
        } catch (Exception ex) {
            throw new IllegalActionException(getComponent(), ex,
                    "Failed to evaluate parse tree for\"" + getName()
                            + "\". in \"" + this.getContainer() + "\"");
        }
        return parseTreeCodeGenerator.generateFireCode();
    }

    /** The default name of the block for the term template. */
    protected String defaultTermBlock = "termBlock";
    // FIXME: rename to _defaultTermBlock.

    Map<String, String> RTMmodule;
    List<String> semanticsIncludes;
    List<String> formalIncludes;
}
