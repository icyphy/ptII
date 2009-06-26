/* RTMaude Code generator class

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

//////////////////////////////////////////////////////////////////////////
//// RTMaudeCodeGenerator

/**
* RTMaude code generator.
*
* @see ptolemy.codegen.kernel.CodeGenerator
* @author Kyungmin Bae
@version $Id$
@since Ptolemy II 7.1
* @version $Id$
* @Pt.ProposedRating Red (kquine)
*
*/
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
