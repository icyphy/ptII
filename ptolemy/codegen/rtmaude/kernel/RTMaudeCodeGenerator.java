/* RTMaude Code generator class

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
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// RTMaudeCodeGenerator

/**
* RTMaude code generator.
*
* @see ptolemy.codegen.kernel.CodeGenerator
* @author Kyungmin Bae
@version $Id$
@since Ptolemy II 8.0
* @version $Id$
* @Pt.ProposedRating Red (kquine)
*
*/
public class RTMaudeCodeGenerator extends CodeGenerator {
    /**
     * Create a new instance of the RTMaude code generator.
     * @param container The container.
     * @param name      The name of the code generator.
     * @exception IllegalActionException   If the super class throws the
     *   exception or error occurs when setting the file path.
     * @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
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
        measureTime.setVisibility(Settable.NONE);
        sourceLineBinding.setVisibility(Settable.NONE);
        target.setVisibility(Settable.NONE);

        compile.setExpression("false");
        generatorPackage.setExpression("ptolemy.codegen.rtmaude");
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RTMaudeCodeGenerator newObject = (RTMaudeCodeGenerator) super.clone(workspace);
        newObject.simulation_bound = (Parameter)newObject.getAttribute("Simulation bound");
        return newObject;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#_generateBodyCode()
     */
    protected String _generateBodyCode() throws IllegalActionException {
        CompositeEntity model = (CompositeEntity) getContainer();

        CodeGeneratorHelper compositeHelper = (CodeGeneratorHelper) _getHelper(model);
        return CodeStream.indent(1, compositeHelper.generateFireCode() + " ");
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#generateMainEntryCode()
     */
    public String generateMainEntryCode() throws IllegalActionException {
        return super.generateMainEntryCode()
                + ((RTMaudeAdaptor) _getHelper(getContainer()))
                        .generateEntryCode();
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#generateMainExitCode()
     */
    public String generateMainExitCode() throws IllegalActionException {
        return super.generateMainExitCode()
                + ((RTMaudeAdaptor) _getHelper(getContainer()))
                        .generateExitCode();
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#_generateIncludeFiles()
     */
    protected String _generateIncludeFiles() throws IllegalActionException {
        return "load ptolemy-base.maude";
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#_finalPassOverCode(java.lang.StringBuffer)
     */
    protected StringBuffer _finalPassOverCode(StringBuffer code)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return code;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#formatComment(java.lang.String)
     */
    public String formatComment(String comment) {
        return "***( " + comment + " )***" + _eol;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.CodeGenerator#_executeCommands()
     */
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
        return _executeCommands.getLastSubprocessReturnCode();
    }

    /** The default path of the Maude program.
     * FIXME: Users may need to change this.
     */
    String maudeCommand = "/usr/local/share/maude/maude.intelDarwin";

    /** The bound (natural number) of steps to simulate a given model. */
    Parameter simulation_bound;
}
