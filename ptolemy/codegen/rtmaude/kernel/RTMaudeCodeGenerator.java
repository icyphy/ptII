package ptolemy.codegen.rtmaude.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class RTMaudeCodeGenerator extends CodeGenerator {

    String maudeCommand = "/usr/local/share/maude/maude.intelDarwin";
    
    Parameter simulation_bound;
    
    public RTMaudeCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        simulation_bound = new Parameter(this, "Simulation bound");
        
        allowDynamicMultiportReference.setVisibility(Settable.NONE);
        compile.setVisibility(Settable.NONE);
        compileTarget.setVisibility(Settable.NONE);
        generateCpp.setVisibility(Settable.NONE);
        generateEmbeddedCode.setVisibility(Settable.NONE);
        padBuffers.setVisibility(Settable.NONE);
        target.setVisibility(Settable.NONE);
         
        compile.setExpression("false");
        generatorPackage.setExpression("ptolemy.codegen.rtmaude");
    }
    
    @Override
    protected String _generateBodyCode() throws IllegalActionException {
        CompositeEntity model = (CompositeEntity) getContainer();
        
        CodeGeneratorHelper compositeHelper = (CodeGeneratorHelper) _getHelper(model);
        return CodeStream.indent(1, compositeHelper.generateFireCode() + " ");
    }
    
    @Override
    public String generateMainEntryCode() throws IllegalActionException {
        return super.generateMainEntryCode() +
            ((RTMaudeAdaptor) _getHelper(getContainer())).generateEntryCode();
    }

    @Override
    public String generateMainExitCode() throws IllegalActionException {
        return super.generateMainExitCode() + 
            ((RTMaudeAdaptor) _getHelper(getContainer())).generateExitCode();
    }

    @Override
    protected String _generateIncludeFiles() throws IllegalActionException {
        return "load ptolemy-base.maude";
    }

    @Override
    protected StringBuffer _finalPassOverCode(StringBuffer code)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return code;
    }

    @Override
    public String formatComment(String comment) {
        return "***( " + comment + " )***" + _eol;
    }

    @Override
    protected int _executeCommands() throws IllegalActionException {
        List commands = new LinkedList();

        if (((BooleanToken) run.getToken()).booleanValue()) {
            commands.add(maudeCommand + " " + _sanitizedModelName + ".rtmaude");            
        }

        if (commands.size() == 0) {
            return -1;
        }

        _executeCommands.setCommands(commands);
        _executeCommands.setWorkingDirectory(codeDirectory.asFile());

        try {
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
        return _executeCommands.getLastSubprocessReturnCode();    }
}
