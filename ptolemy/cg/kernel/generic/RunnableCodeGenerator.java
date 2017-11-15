/* Code generator that generates runnable output that does not require compilation.

Copyright (c) 2009-2017 The Regents of the University of California.
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
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

 */

package ptolemy.cg.kernel.generic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RunnableCodeGenerator

/** Code generate a runnable code that does not require compilation.
 *
 *  @author Christopher Brooks.  Based on ProgramCodeGenerator by Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (rodiers)
 *  @Pt.AcceptedRating red (rodiers)
 */
public class RunnableCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the ProgramCodeGenerator.
     *  @param container The container.
     *  @param name The name of the ProgramCodeGenerator.
     *  @param outputFileExtension The extension of the output file.
     *   (for example c in case of C and java in case of Java)
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public RunnableCodeGenerator(NamedObj container, String name,
            String outputFileExtension)
                    throws IllegalActionException, NameDuplicationException {
        super(container, name, outputFileExtension);

        run = new Parameter(this, "run");
        run.setTypeEquals(BaseType.BOOLEAN);
        run.setExpression("true");

        runCommand = new StringParameter(this, "runCommand");
        // Set it to a default so that derived classes may override it.
        runCommand.setExpression(_runCommandDefault);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then run the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter run;

    /** The command to use to run the generated code if the
     *  <i>useMake</i> parameter is false.  The initial default value
     *  is "make -f @modelName@.mk run".  Various '@' delimited
     *  key/value pairs will be automatically substituted.  In the
     *  default case @modelName@ will be replaced with a sanitized
     *  (Java-safe) version of the model name.
     *
     *  <p>If the string "@help:all@" appears, then all the key/value
     *  pairs are echoed at run time, though this may not result in a
     *  syntactically correct command.</p>
     *
     *  <p>If <i>useMake</i> is true, then the value of this parameter
     *  is ignored.</p>
     */
    public StringParameter runCommand;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the attribute into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new attribute.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RunnableCodeGenerator newObject = (RunnableCodeGenerator) super
                .clone(workspace);

        try {
            newObject._substituteMap = CodeGeneratorUtilities.newMap(this);
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException(ex.getMessage());
        }
        return newObject;
    }

    /** Reset the code generator.
     *  @exception IllegalActionException Not thrown in this base class,
     *  thrown by the parent if the container of the model
     *  cannot be set to null.
     */
    @Override
    protected void _reset() throws IllegalActionException {
        super._reset();
        if (_substituteMap != null) {
            _substituteMap.clear();
        }
    }

    /** Return an updated array of command line options.
     *  @return An array of updated command line options.
     */
    @Override
    public String[][] updateCommandOptions() {
        // This is a hack.

        // The command-line options that take arguments.
        String[][] options = {
                { "-run", "               true|false (default: true)" },
                { "-runCommand",
                "        <a string, default: make -f @modelName@.mk run>" },
        };

        String[][] parentOptions = super.updateCommandOptions();
        String[][] allOptions = new String[parentOptions.length
                                           + options.length][2];
        int i = 0;
        for (; i < parentOptions.length; i++) {
            allOptions[i][0] = parentOptions[i][0];
            allOptions[i][1] = parentOptions[i][1];
        }
        for (int j = 0; j < options.length; j++) {
            allOptions[i + j][0] = options[j][0];
            allOptions[i + j][1] = options[j][1];
        }
        return allOptions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     *  @exception IllegalActionException If there are problems reading
     *  parameters or executing the commands.
     */
    @Override
    protected int _executeCommands() throws IllegalActionException {
        _updateSubstituteMap();

        List<String> commands = new LinkedList<String>();

        String command = "";
        // The run command.
        if (_isTopLevel()) {
            if (((BooleanToken) run.getToken()).booleanValue()) {
                List<String> setupCommands = _setupCommands();
                commands.addAll(setupCommands);
                command = _runCommand();
                commands.add(command);
            }
        }

        if (commands.size() == 0) {
            return -1;
        }

        System.out.println("RunnableCodeGenerator: run command: (cd "
                           + codeDirectory.asFile() + "; "
                           + command + ")");

        _executeCommands.setCommands(commands);
        _executeCommands.setWorkingDirectory(codeDirectory.asFile());

        try {
            // FIXME: need to put this output in to the UI, if any.
            _executeCommands.start();
        } catch (Throwable throwable) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator<?> allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + _eol);
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:" + _eol + errorMessage + _eol + throwable);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }

    /** Return the command to run the generated code.
     *  Derived classes typically extend {@link #_updateSubstituteMap}.   
     *  @return The command to run the generated code.
     *  @exception IllegalActionException If the there is a problem
     *  substituting the @...@ tags.
     */
    protected String _runCommand() throws IllegalActionException {
        _updateSubstituteMap();
        String command = CodeGeneratorUtilities
            .substitute(((StringToken) runCommand.getToken())
                        .stringValue(), _substituteMap);
        return command;
    }

    /** Return a list of setup commands to be invoked before
     *  the run command.
     *  @return The list of commands.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected List<String> _setupCommands() throws IllegalActionException {
        _updateSubstituteMap();
        return new LinkedList<String>();
    }

    /** Update the substitute map for the setup and run commands
     *  In this base class, @codeDirectory@, @modelName@
     *  and @PTII@ are added to the map.
     *  @exception IllegalActionException Not thrown in this base class,
     *  Derived classes should throw it if there is a problem parsing
     *  a value.
     */
    protected void _updateSubstituteMap()
        throws IllegalActionException {
        if (_substituteMap == null) {
            _substituteMap = CodeGeneratorUtilities.newMap(this);
        }

        _substituteMap.put("@codeDirectory@", codeDirectory.asFile().toString());
        _substituteMap.put("@modelName@", _sanitizedModelName);
        _substituteMap.put("@PTII@", StringUtilities.getProperty("ptolemy.ptII.dir"));

        if (_model instanceof CompositeActor) {
            _substituteMap.put("@stopTime@", ((CompositeActor) _model).getDirector().stopTime.getExpression());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The initial default value of the <i>runCommand</i> parameter.
     *  The constructor of a derived class may compare the value of <i>runCommand</i>
     *  and this variable and decide to override the value of the <i>runCommand</i>
     *  parameter with a new value.
     */
    protected final static String _runCommandDefault = "make -f @modelName@.mk run";

    /** Map of '@' delimited keys to values.
     */
    protected Map<String, String> _substituteMap;
}
