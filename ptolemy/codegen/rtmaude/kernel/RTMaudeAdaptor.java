/* RTMaude Code generator helper class.

 Copyright (c) 2009 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.codegen.rtmaude.data.expr.PropertyParameter;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// RTMaudeAdaptor

/**
* Generate RTMaude code in DE domain. Every adaptor (helper) class for
* RTMaude should extend this class.
*
* @see ptolemy.codegen.kernel.CodeGeneratorHelper
* @author Kyungmin Bae
@version $Id$
@since Ptolemy II 7.1
* @version $Id$
* @Pt.ProposedRating Red (kquine)
*
*/
public class RTMaudeAdaptor extends CodeGeneratorHelper {

    protected String defaultTermBlock = "termBlock";

    public RTMaudeAdaptor(NamedObj component) {
        super(component);
        _parseTreeCodeGenerator = getParseTreeCodeGenerator();
    }

    public List<String> getBlockCodeList(String blockName, String... args)
            throws IllegalActionException {
        List<String> rl = new LinkedList();
        rl.add(_generateBlockCode(blockName, args));
        return rl;
    }

    /**
     * @param blockName
     * @param args
     * @return The block code
     * @exception IllegalActionException
     */
    protected String _generateBlockCode(String blockName, String... args)
            throws IllegalActionException {
        return super._generateBlockCode(blockName, Arrays.asList(args));
    }

    @Override
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

    protected String _generateInfoCode(String name, List<String> parameters)
            throws IllegalActionException {
        if (name.equals("name")) {
            return getComponent().getName();
        }

        throw new IllegalActionException("Unknown RTMaudeObj Information");
    }

    @Override
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
                return _generateInfoCode(aName, largs);
            } else {
                return _generateBlockCode(aName, largs);
            }
        }
        if (macro.equals("indent")) {
            return _eol + CodeStream.indent(1, processCode(parameter));
        }
        return super._replaceMacro(macro, parameter);
    }

    @Override
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

    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        return _generateBlockCode("fireFuncBlock", generateTermCode(),
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
        return _generateBlockCode(defaultTermBlock); // term block
    }

    public String generateEntryCode() throws IllegalActionException {
        HashSet<String> inc_set;
        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            inc_set = new HashSet(getBlockCodeList("moduleName"));
        } else {
            inc_set = new HashSet(getBlockCodeList("funcModuleName"));
        }

        return _generateBlockCode("mainEntry", CodeStream.indent(2,
                new ListTerm<String>("ACTOR-BASE", " +" + _eol, inc_set)
                        .generateCode()));
    }

    public String generateExitCode() throws IllegalActionException {
        HashSet<String> check_inc_set = new HashSet<String>();
        StringBuffer commands = new StringBuffer();
        ptolemy.data.Token bound = ((RTMaudeCodeGenerator) _codeGenerator).simulation_bound
                .getToken();

        check_inc_set.add("INIT");
        check_inc_set.addAll(getBlockCodeList("checkModuleName"));

        if (bound != null) {
            if (bound.toString().equals("Infinity")) {
                commands.append("(rew {init} .)");
            } else {
                commands.append("(trew {init} in time <= "
                        + IntToken.convert(bound).toString() + " .)");
            }
        }

        for (PropertyParameter p : (List<PropertyParameter>) _codeGenerator
                .attributeList(PropertyParameter.class)) {
            commands.append("(" + p.stringValue() + " .)" + _eol);
        }

        return _generateBlockCode("mainExit", CodeStream.indent(2,
                new ListTerm<String>("MODELCHECK-BASE", " +" + _eol,
                        check_inc_set).generateCode()), commands.toString());
    }

    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        _parseTreeCodeGenerator = new RTMaudeParseTreeCodeGenerator(); //FIXME: _codeGenerator
        return _parseTreeCodeGenerator;
    }
}
