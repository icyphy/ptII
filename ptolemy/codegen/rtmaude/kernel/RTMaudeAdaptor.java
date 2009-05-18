package ptolemy.codegen.rtmaude.kernel;

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

public class RTMaudeAdaptor extends CodeGeneratorHelper {
    
    protected String defaultTermBlock = "termBlock";
        
    public RTMaudeAdaptor(NamedObj component) {
        super(component);
        _parseTreeCodeGenerator = getParseTreeCodeGenerator();
    }
    
    public List<String> getBlockCodeList(String blockName, String ... args) 
            throws IllegalActionException {
        List<String> rl = new LinkedList();
        rl.add(_generateBlockCode(blockName,args));
        return rl;
    }
    
    /**
     * @param blockName
     * @param args
     * @return The block code
     * @throws IllegalActionException
     */
    protected String _generateBlockCode(String blockName, String ... args)
            throws IllegalActionException {
        return super._generateBlockCode(blockName, Arrays.asList(args));
    }
    
    @Override
    protected String _generateTypeConvertMethod(String ref, String castType,
            String refType) throws IllegalActionException {
        
        //FIXME: too specific.
        if (refType != null && refType.equals("String"))
            return "# " + ref;
        if (castType != null && castType.equals("time")) {
            return "toTime(" + ref + ")";
        }
        else
            return super._generateTypeConvertMethod(ref, castType, refType);
    }
    
    @Override
    public String generateFireCode() throws IllegalActionException {
        String comment = _codeGenerator.comment("Fire " +
                ((getComponent() instanceof CompositeActor) ? "Composite Actor: " : "") +
                generateName(getComponent()));

        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE)
            return processCode(comment + 
                    _generateFireCode() + generateTypeConvertFireCode());
        else
            return processCode(comment + generateTermCode());        
    }
    
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        return _generateBlockCode("fireFuncBlock",
                _generateBlockCode("funcModuleName"),
                _generateBlockCode("moduleName"),
                generateTermCode(),
                CodeStream.indent(1,_generateFireCode() + generateTypeConvertFireCode())
                );
    }

    /**
     * Generate a Real-time Maude term representation of given component.
     * 
     * @return the term representation of a component
     * @throws IllegalActionException 
     */
    public String generateTermCode() throws IllegalActionException {
        return _generateBlockCode(defaultTermBlock);    // term block
    }
    
    public String generateEntryCode() throws IllegalActionException {
        HashSet<String> inc_set;
        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE)
            inc_set = new HashSet(getBlockCodeList("moduleName"));
        else
            inc_set = new HashSet(getBlockCodeList("funcModuleName"));
            
        return _generateBlockCode("mainEntry",
                CodeStream.indent(2, 
                        new ListTerm<String>("ACTOR-BASE", " +" + _eol,
                                inc_set).generateCode())
            );
    }
    
    public String generateExitCode() throws IllegalActionException {
        HashSet<String> check_inc_set = new HashSet<String>();
        StringBuffer commands = new StringBuffer();
        ptolemy.data.Token bound = ((RTMaudeCodeGenerator)_codeGenerator).
                            simulation_bound.getToken();
        
        check_inc_set.add("INIT");
        check_inc_set.addAll(getBlockCodeList("checkModuleName"));
        
        
        if ( bound != null ) {
            if (bound.toString().equals("Infinity"))
                commands.append("(rew {init} .)");
            else
                commands.append("(trew {init} in time <= " + 
                        IntToken.convert(bound).toString() + " .)");
        }

        for (PropertyParameter p : (List<PropertyParameter>)
                _codeGenerator.attributeList(PropertyParameter.class)) {
            commands.append("(" + p.stringValue() + " .)" + _eol);
        }
        
        return _generateBlockCode("mainExit", 
                CodeStream.indent(2, 
                        new ListTerm<String>("MODELCHECK-BASE", " +" + _eol, 
                                check_inc_set).generateCode()),
                commands.toString()
        );
    }
    
    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        _parseTreeCodeGenerator = new RTMaudeParseTreeCodeGenerator(); //FIXME: _codeGenerator
        return _parseTreeCodeGenerator;
    }
}
