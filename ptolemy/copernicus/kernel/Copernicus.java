/* Standalone application that generates code

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.kernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import ptolemy.actor.*;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.microstar.xml.XmlException;

//////////////////////////////////////////////////////////////////////////
//// Copernicus
/**
A Standalone application that generates code using the Ptolemy II code
generation system.  This class acts a wrapper for the copernicus.*.Main
classes by providing defaults arguments for the various backends.

The <i>generatorAttribute</i> Parameter names a MoML file that
contains definitions for other Parameters and Variables that control
the compilation and execution of the model

The default compilation arguments are read in from a file named
compileCommandTemplate.in, variables are substituted and the compile
command executed and then default arguments are read in from a file
named runCommandTemplate.in.

<p>For example:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.Copernicus foo.xml
</pre>
will read in the $PTII/ptolemy/copernicus/java/compileCommandTemplate.in,
substitute in the appropriate variables and then generate code for foo.xml

<p>The default code generator is the deep java code generator in
$PTII/ptolemy/copernicus/java.

<p>The argument that names the xml file containing the model to generate
code for should be a relative pathname.  The xml file argument is
converted into a URL internally.
If no xml file argument is specified,
then code is generated for
<code>$PTII/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml</code>

<p>Generating code is fairly complex, so there are many other parameters
that can be set as the other arguments.

<p>The general format is
<code>-<i>VariableName</i> <i>VariableValue</i></code>, for example:
<code>-codeGenerator "shallow"</code>
<p>For example:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode -codeGenerator "shallow" foo.xml
</pre>

<p>The initial parameters, their values and any documentation can be
printed with

<pre>
java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode -help
</pre>

If you have rebuilt Ptolemy II from sources, and have a shell such as
bash available, then you can use <code>$PTII/bin/copernicus</code>
as a shortcut.  For example
<pre>
$PTII/bin/copernicus -codeGenerator "shallow" foo.xml
</pre>

<p>The details of how this class works can be found in the
{@link GeneratorAttribute} documentation.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class Copernicus {

    /** Parse the specified command-line arguments and then execute
     *  any specified commands.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public Copernicus(String args[]) throws Exception {
        // Parse the command-line arguments
        _parseArgs(args);
        
        if(_modelPath == null) {
            System.out.println(_usage());
            // NOTE: This means the test suites cannot test this.
            System.exit(0);
        }

        // Parse the model.
        CompositeActor toplevel = readInModel(_modelPath);
        
        _generatorAttribute = 
            (GeneratorAttribute)toplevel.getAttribute(GENERATOR_NAME);
        if(_generatorAttribute == null) {
            _generatorAttribute =
                new GeneratorAttribute(toplevel, GENERATOR_NAME);
            _generatorAttribute.initialize();
        }
  
        // Savecommand-line parameters in the generator attribute.
        _saveParsedArgs();

        // Parse the file named by the modelPath Parameter and update
        // parameters.  FIXME: Is this necessary?
        _generatorAttribute.sanityCheckAndUpdateParameters(null);

        if (_verbose) {
            System.out.println(_generatorAttribute.toString());
        }

        // Pass the model off to the real working function.
        compileAndRun(toplevel, _generatorAttribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the command to run the generated code.
     *  The <i>generatorAttribute</i> argument contains a
     *  the <i>runCommandTemplateFile</i> parameter that refers
     *  to a template file that contains the command to run the generated
     *  code after the parameters from <i>generatorAttribute</i>
     *  are substituted in.
     *
     *  @param generatorAttribute The GeneratorAttribute that contains
     *  the Parameters that determine the command to run.
     *  @return The command to run the generated code.
     */
    public static String commandToRun(GeneratorAttribute generatorAttribute)
            throws Exception {
        String runCommandTemplateFile = generatorAttribute
            .getParameter("runCommandTemplateFile");
        return substitute(runCommandTemplateFile, generatorAttribute);
    }

    /** Possibly create the generated code and run it.
     *        What actually happens depends on the values of the <i>compile</i>
     *  and <i>run</i> parameters in <i>generatorAttribute<i>
     *  @param generatorAttribute The GeneratorAttribute that contains
     *  the parameters that determine the commands to create and run
     *  the generated code.
     */
    public static void compileAndRun(CompositeActor model,
            GeneratorAttribute generatorAttribute)
            throws Exception {

        // Save the _generatorAttribute in a temporary file and then
        // add an attribute to _generatorAttribute that lists the
        // location of the temporary file.
        // This is a bit of a hack, but it is necessary if we want
        // to be able to use the the key/value pairs that are in
        // _generatorAttribute later in MakefileWriter.
        // At first glance, it would appear that we could just read
        // the GeneratorAttribute from the model, but one problem is
        // that we filter out the GeneratorAttribute in KernelMain.
        // Another problem is that this class reads in the model and
        // then modifies the GeneratorAttribute according to the
        // values of the command line arguments and other values, but
        // we never update the model with this data.
        String generatorAttributeFileName =
            exportMoMLToTemporaryFile(generatorAttribute);
        // We add the filename as an attribute so that we can use its
        // value to substitute.
        // We substitute forward slashes for backward slashes because
        // having backward slashes in attributes causes TokenMgrErrors
        // while reading in a model.
        new Parameter(generatorAttribute, "generatorAttributeFileName",
                new StringToken(StringUtilities
                        .substitute(generatorAttributeFileName,
                                "\\", "/")));

        int exitValue = 1;
        String compile = generatorAttribute.getParameter("compile");

        if (compile.equals("true")) {
            // NOTE Is this still needed?
            // Temporary hack because cloning doesn't properly clone
            // type constraints.  In some ways, it would make sense to
            // do this in readInModel(), where we already have a
            // MoMLParser object, but we want to be sure the type
            // constraints are cloned if we are passed in a model
            // directly without running readInModel().
           //  CompositeActor modelClass = null;
//             try {
//                 modelClass = (CompositeActor)
//                     _parser.searchForClass(_momlClassName,
//                             model.getMoMLInfo().source);
//             } catch (XmlException xml) {
//                 throw new
//                     IllegalActionException("Failed to find class '"
//                             + _momlClassName + "' in '"
//                             + model.getMoMLInfo().source
//                             + "': " + xml);
//             }
            
           //  if (modelClass != null) {
//                 model = modelClass;
//             }

            // Instantiate the right code generator.
            String codeGeneratorClassName = generatorAttribute
                .getParameter("codeGeneratorClassName");
            Class codeGeneratorClass = Class.forName(codeGeneratorClassName);
            KernelMain codeGenerator = (KernelMain)
                codeGeneratorClass.newInstance();
     
            // Compile the model.
            codeGenerator.compile(
                    model.getName(),
                    model,
                    generatorAttribute);
        }

        String run = generatorAttribute.getParameter("run");

        if (run.equals("true")) {
            String command = commandToRun(generatorAttribute);
            exitValue = executeCommand(command);
            if (exitValue != 0) {
                throw new Exception("Problem executing command. "
                        + "Return value was: " + exitValue
                        + ". Command was:\n" + command);
            }
        }
    }

    /** Execute a command in a subshell, and print out the results
     *  in standard error and standard out.  Lines that begin with
     *  an octothorpe '#' are ignored.  Substrings that start and end with
     *  a double quote are considered to be a single argument.
     *
     *  @param command The command to execute.
     *  @return the exit status of the process, which is usually
     *  0 if the process executed normally.
     */
    public static int executeCommand(String command) throws Exception {

        if (command == null || command.length() == 0 ) {
            System.out.println("Warning, null or 0 length command string "
                    + "passed to Copernicus.executeCommand()");
            return 0;
        }

        String [] commands = StringUtilities.tokenizeForExec(command);

        if (commands.length == 0) {
            System.out.println("Warning, command was parsed to 0 tokens, "
                    + "perhaps the command string was empty or "
                    + "consisted only of comments?\n"
                    + "command string was '" + command + "'");
            return 0;
        }
        System.out.println("About to execute:\n ");
        for (int i = 0; i < (commands.length - 1); i++) {
            System.out.println("        \"" + commands[i] + "\" \\");
        }

        if (commands.length > 0) {
            System.out.println("        \"" + commands[commands.length - 1]
                    + "\"");
        }
        System.out.flush();

        // 0 indicates normal execution
        int processReturnCode = 1;
        try {
            // This code is similar to tcl.lang.ExecCmd, so if you
            // make changes here, please take a look at ExecCmd and
            // see if it needs updating.

            Process process = Runtime.getRuntime().exec(commands);

            // Set up a Thread to read in any error messages
            _StreamReaderThread errorGobbler = new
                _StreamReaderThread(process.getErrorStream(), System.err);

            // Set up a Thread to read in any output messages
            _StreamReaderThread outputGobbler = new
                _StreamReaderThread(process.getInputStream(), System.out);

            // Start up the Threads
            errorGobbler.start();
            outputGobbler.start();
            try {
                processReturnCode = process.waitFor();
                errorGobbler.join();
                outputGobbler.join();
                synchronized (_lock) {
                    process = null;
                }
            } catch (InterruptedException interrupted) {
                System.out.println("InterruptedException: "
                        + interrupted);
                throw interrupted;
            }
            System.out.println("All Done.");
        } catch (final IOException io) {
            System.out.flush();
            System.err.println("IOException: " + io);
        }
        return processReturnCode;
    }

    /** Export the MoML of the namedObj argument to a temporary file.
     *  The file is deleted when the Java virtual machine terminates.
     *  @param namedObj The NamedObj to export
     *  @return The name of the temporary file that was created
     *  @exception Exception If the temporary file cannot be created.
     *  @see java.io.File#createTempFile(java.lang.String, java.lang.String, java.io.File)
     */
    public static String exportMoMLToTemporaryFile(NamedObj namedObj)
            throws Exception {
        File temporaryFile = File.createTempFile("ptCopernicus", ".xml");
        temporaryFile.deleteOnExit();
        FileWriter writer = new FileWriter(temporaryFile);
        String header = "<class name=\"Temp\" extends=\"ptolemy.actor.TypedCompositeActor\">\n";
        writer.write(header, 0, header.length());
        namedObj.exportMoML(writer, 1, GENERATOR_NAME);
        String footer = "</class>\n";
        writer.write(footer, 0, footer.length());
        writer.close();

        // Substitute backslashes here because setting a parameter to include
        // backslashes causes problems.
        return
            StringUtilities.substitute(temporaryFile.toString(), "\\", "/");
    }

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            new Copernicus(args);
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
        }
        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
        // We need to call exit here if we are running codegen on
        // a model that uses Swing.  Useful models that use the
        // plotter fall in this category.
        System.exit(0);
    }

    /** Given a NamedObj, generate a HashMap containing String key/value
     *  pairs where each key is a Parameter contained in the namedObj
     *  argument, and each value is the value of the Parameter.
     */
    public static HashMap newMap(NamedObj namedObj)
            throws IllegalActionException {
        HashMap substituteMap = new HashMap();
        Iterator attributes = namedObj.attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            if (attribute instanceof Variable) {
                Variable variable = (Variable)attribute;
                // If getToken() fails, make sure that you are calling
                // setExpression with a string that has double quotes.
                String value = variable.getToken().toString();
                // Strip out any leading and trailing double quotes
                if (value.startsWith("\"") && value.length() >  2) {
                    value = value.substring(1, value.length()-1);
                }
                substituteMap.put("@" + variable.getName() + "@",
                        value);
            }
        }

        //         System.out.println("The map for " + namedObj +":");
        //         Iterator keys = substituteMap.keySet().iterator();
        //         while (keys.hasNext()) {
        //             String key = (String)keys.next();
        //             System.out.println(key + "\t" + (String)substituteMap.get(key));
        //         }

        return substituteMap;
    }

    /** Given a string that names a file or URL, try to
     *  open as a file, and then as a URL.
     *  @param inputFileName The name of the file or URL to open
     *  @return A BufferedReader that refers to the inputFileName
     *  @exception FileNotFoundException If the file cannot be found.
     *  @exception IOException If there were problems creating
     *  the BufferedReader.
     */
    public static BufferedReader openAsFileOrURL(String inputFileName)
            throws FileNotFoundException, IOException {
        BufferedReader inputFile;
        try {
            inputFile =
                new BufferedReader(new FileReader(inputFileName));
        } catch (IOException ex) {
            // Try it as a resource
            URL inputFileURL =
                Thread.currentThread().getContextClassLoader()
                .getResource(inputFileName);
            if (inputFileURL == null) {
                throw ex;
            }
            inputFile =
                new BufferedReader(new InputStreamReader(inputFileURL
                        .openStream()));
        }
        return inputFile;
    }

    /** Read in a MoML class, either as a top level model or
     *  a file, initialize the model, then create instance classes for actors.
     *  <p> The MoML class name is processed as follows:
     *  <ol>
     *  <li> The momlClassName argument is assumed to be a dot
     *  separated top level model name such as
     *  <code>ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom</code>
     *  and inserted into a MoML fragment:
     *  <p><code>
     *  &lt;entity name="ToplevelModel" class=" + momlClassName + "/&gt;
     *  </code>
     *  and then passed to MoMLParser.parse().
     *  <li>If the parse fails, then the name is tried as a
     *  relative MoML file name and passed to MoMLParser.parseFile().
     *  </ol>
     *  @exception IllegalActionException If the model cannot be parsed.
     */
    public CompositeActor readInModel(String modelPathOrURL)
            throws IllegalActionException, NameDuplicationException {
        URL modelURL = null;
        try {
            modelURL = MoMLApplication.specToURL(modelPathOrURL);
        } catch (IOException ex) {
            try {
                // We might have a JAR URL because we are inside webstart
                modelURL = JNLPUtilities.jarURLEntryResource(modelPathOrURL);
            } catch (IOException ex2) {
            }
            if (modelURL == null) {
                throw new IllegalActionException(null, ex,
                        "Failed to parse '"
                        + modelPathOrURL + "'");
            }
        }

        // Create a parser.
        _parser = new MoMLParser();

        // Handle Backward Compatibility.
        _parser.addMoMLFilters(BackwardCompatibility.allFilters());

        // Filter out any graphical classes and the GeneratorAttribute
        // itself.  If we don't filter out GeneratorAttribute, then
        // we get quite a few attributes in the generated code, which
        // causes problems at runtime because sometimes the parameters
        // are out of order, so there are dependency issues, or,
        // in the case of the applet generator, the parameters depend
        // on the value of Java system properties like ptolemy.ptII.directory
        // which is not accessible because of security concerns.
        RemoveGraphicalClasses removeGraphicalClasses =
            new RemoveGraphicalClasses();

        // FIXME: Not sure why this is necessary, but it helps
        // when generating an applet for moml/demo/spectrum.xml
        removeGraphicalClasses.put("ptolemy.kernel.util.Location", null);

        removeGraphicalClasses
            .put("ptolemy.copernicus.kernel.GeneratorAttribute", null);
        // shallow/test/IIRGUI.xml has a GeneratorTableauAttribute in it.
        removeGraphicalClasses
            .put("ptolemy.copernicus.gui.GeneratorTableauAttribute", null);
        _parser.addMoMLFilter(removeGraphicalClasses);

        // Parse the model.
        CompositeActor toplevel = null;
        try {
            toplevel = (CompositeActor)_parser.parse(modelURL, modelURL);
        } catch (Exception exception) {
            StringBuffer errorMessage = new StringBuffer();
            errorMessage.append("Failed to parse '" + modelPathOrURL
                    + "' as a top level model in \n"
                    + modelURL + "!");
            throw new IllegalActionException(errorMessage.toString());
        }
        
        // If the name of the toplevel is the empty string, change it to
        // the basename of the file.
        // FIXME: is this correct for filenames?
        if (toplevel.getName().length() == 0) {
            String baseName = (new File(modelPathOrURL)).getName();
            if (baseName.lastIndexOf('.') != -1) {
                baseName = baseName.substring(0,
                        baseName.lastIndexOf('.'));
            }
            toplevel.setName(baseName);
        }
        
        // Make the name follow Java initializer naming conventions.
        toplevel.setName(StringUtilities.sanitizeName(toplevel.getName()));

        return toplevel;
    }

    /** Given a string and a Map containing String key/value pairs,
     *  substitute any keys found in the input with the corresponding
     *  values.
     *
     *  @param input The input string that contains substrings
     *  like "@codeBase@".
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @return  A string with the keys properly substituted with
     *  their corresponding values.
     */
    public static String substitute(String input,
            Map substituteMap) {

        // At first glance it would appear that we could use StringTokenizer
        // however, the token is really the String @codeBase@, not
        // the @ character.  StringTokenizer has problems with
        // "@codebase", which reports as having one token, but
        // should not be substituted since it is not "@codebase@"

        Iterator keys = substituteMap.keySet().iterator();

        while (keys.hasNext()) {
            String key = (String)keys.next();
            input = StringUtilities.substitute(input, key,
                    (String)substituteMap.get(key));
        }
        return input;
    }

    /** Read in the contents of inputFileName, and for each Parameter
     *  in namedObj, search for strings like
     *  <code>@<i>ParameterName</i>@</code> in inputFileName, and
     *  substitute in the value of the Parameter and return the results.
     *
     *  @param inputFileName  The name of the file to read from.
     *  @param namedObj The NamedObj that contains Parameters to
     *  be searched for in inputFileName.
     *  @return The contents of inputFileName after doing the substitutions
     */
    public static String substitute(String inputFileName,
            NamedObj namedObj)
            throws FileNotFoundException, IOException {

        Map substituteMap;
        try {
            substituteMap = newMap(namedObj);
        } catch (IllegalActionException ex) {
            // IOException does not have a constructor that takes a
            // cause argument.
            IOException exception = new IOException("Problem generating a "
                    + "substitution map for "
                    + namedObj.getName());
            exception.initCause(ex);
            throw exception;
        }
        URL inputFileURL = Thread.currentThread().getContextClassLoader()
            .getResource(inputFileName);
        if (inputFileURL == null) {
            throw new FileNotFoundException("Failed to find '"
                    + inputFileName
                    + "' as a resource");
        }
        BufferedReader inputReader =
            new BufferedReader(new InputStreamReader(inputFileURL
                    .openStream()));
        String inputLine;
        StringBuffer output = new StringBuffer();
        String lineSeparator = System.getProperty("line.separator");
        while ( (inputLine = inputReader.readLine()) != null) {
            output.append(substitute(inputLine + lineSeparator,
                    substituteMap));
        }
        inputReader.close();
        return output.toString();
    }

    /** Read in the contents of inputFile, and replace each matching
     *  String key found in substituteMap with the corresponding
     *  String value and write the results to outputFileName.
     *  @param inputFile A BufferedReader that refers to the file to be
     *  read in.
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @param outputFileName The name of the file to write to.
     *  @see #substitute(String, Map, String)
     */
    public static void substitute(BufferedReader inputFile,
            Map substituteMap,
            String outputFileName)
            throws FileNotFoundException, IOException {
        PrintWriter outputFile =
            new PrintWriter(new BufferedWriter(
                                    new FileWriter(outputFileName)));
        String inputLine;
        while ( (inputLine = inputFile.readLine()) != null) {
            outputFile.println(substitute(inputLine, substituteMap));
        }
        inputFile.close();
        outputFile.close();
    }

    /** Read in the contents of inputFileName, and replace each
     *  matching String key found in substituteMap with the
     *  corresponding String value and write the results to
     *  outputFileName.
     *  @param inputFileName  The name of the file to read from.
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @param outputFileName The name of the file to write to.
     *  @see #substitute(BufferedReader, Map, String)
     */
    public static void substitute(String inputFileName,
            Map substituteMap,
            String outputFileName)
            throws FileNotFoundException, IOException {
        BufferedReader inputFile = openAsFileOrURL(inputFileName);
        substitute(inputFile, substituteMap, outputFileName);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the GeneratorAttribute */
    public static String GENERATOR_NAME = "_generator";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            System.out.println(_usage());
            System.out.println(_help());
            // NOTE: This means the test suites cannot test -help
            System.exit(0);
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-verbose")) {
            _verbose = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version "
                    + VersionAttribute.CURRENT_VERSION.getExpression()
                    + ", Build $Id$");
            // NOTE: This means the test suites cannot test -version
            System.exit(0);
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else if (!arg.startsWith("-")) {
            // Assume the argument is a file name or URL.
            _modelPath = arg;
        } else {
            // Argument not recognized.
            return false;
        }
        return true;
    }

    /** Parse the command-line arguments.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(String args[]) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (_parseArg(arg) == false) {
                if (arg.trim().startsWith("-")) {
                    if (i >= args.length - 1) {
                        throw new IllegalActionException("Cannot set " +
                                "parameter " + arg + " when no value is " +
                                "given.");
                    }
                    // Save in case this is a parameter name and value.
                    _parameterNames.add(arg.substring(1));
                    _parameterValues.add(args[i + 1]);
                    i++;
                } else {
                    // Unrecognized option.
                    throw new IllegalActionException("Unrecognized option: "
                            + arg);
                }
            }
        }
    }

    /** Save arguments that were parsed in the generatorAttribute of
     * the model.
     */
    protected void _saveParsedArgs() throws Exception { 
        _generatorAttribute.updateModelAttributes(_modelPath);

        // Check saved options to see whether any is setting an attribute.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            String name = (String)names.next();
            String value = (String)values.next();

            boolean match = false;

            Attribute attribute = _generatorAttribute.getAttribute(name);
            if (attribute instanceof Settable) {
                match = true;
                ((Settable)attribute).setExpression(value);
                if (attribute instanceof Variable) {
                    // Force evaluation so that listeners
                    // are notified.
                    ((Variable)attribute).getToken();
                }
            }

            if (!match) {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: " +
                        "No parameter exists with name " + name);
            }
        }
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a string containing all the Parameters */
    private static String _help() {
        NamedObj namedObj = new NamedObj();
        try {
            GeneratorAttribute generatorAttribute =
                new GeneratorAttribute(namedObj, "_helpGeneratorAttribute");
            return generatorAttribute.toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    /** Return a string containing the usage */
    private String _usage() {
        StringBuffer usage =
            new StringBuffer(StringUtilities.usageString(_commandTemplate,
                    _commandOptions, _commandFlags));

        try {
            NamedObj namedObj = new NamedObj();
            usage.append(
                    "\n\nThe following attributes of the code generator can\n"
                    + "be set.  For example '-codeGenerator java' means\n"
                    + "to use the java code generator\n\n");

            _generatorAttribute =
                new GeneratorAttribute(namedObj, GENERATOR_NAME);
            _generatorAttribute.initialize();

            // Parse the file named by the modelPath Parameter and update
            // parameters
            _generatorAttribute.sanityCheckAndUpdateParameters(null);

            usage.append(_generatorAttribute.toString());
        } catch(Exception ex) {
            usage.append("Problem evaluating default arguments: " + ex);
        }

        return usage.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The command-line options that are either present or not. */
    protected String _commandFlags[] = {
        "-help: Print this help information\n",
        "-test: Smoke test the code generator by killing after 2 seconds.\n",
        "-verbose: Show verbose execution information.\n",
        "-version: Show version information.\n",
    };

    /** The command-line options that take arguments. */
    protected String _commandOptions[][] = {
        {"-<parameter name>", "<parameter value>"},
    };

    /** The form of the command line. */
    protected String _commandTemplate =
    "\ncopernicus [options . . .] [relative xml filename]\n"
    + "This command used to generate code from a model.\n"
    + "This command is very complex, see $PTII/doc/codegen.htm for details\n\n"
    + "This command does command line argument substitution by reading\n"
    + "template files and then executes a subprocess that that does\n"
    + "the code generation.\n"
    + "This command parses the given model file (specified as a file name,\n"
    + " a URL or a resource) and generates code for the model\n"
    + "based on a large number of configuration parameters.\n"
    + "If the model contains an instance of the GeneratorAttribute class\n"
    + "then the configuration parameters are taken from that instance.\n"
    + "If the model does not contain an instance of the GeneratorAttribute\n"
    + "class, then default parameters are taken from the moml file in\n"
    + "$PTII/ptolemy/copernicus/kernel/Generator.xml.\n"
    + "In any case, those parameters may be overridden by command line\n"
    + "options of the form: -<option> <value>.\n\n"
    + "The most significant configuration option is the\n"
    + "-codeGenerator option which is used to select which code\n"
    + "generator is used.  The default value is 'java', which means\n"
    + "that the code generator class at ptolemy.copernicus.java.Main.java\n"
    + "is used to generate code.\n"
    + "-codeGenerator can have the following values:\n"
    + "   applet         Generate a html files containing an applet version.\n"
    + "   c              Generate C code version.\n"
    + "   interpreted    Generate interpreted version of the model\n"
    + "                    Similar to 'Save As, used primary for testing.\n"
    + "   java           Generate a deep Java version that uses very\n"
    + "                    few classes from Ptolemy.\n"
    + "   jhdl           Generate a JDHL version (requires JHDL).\n"
    + "   shallow        Generate a shallow Java version that uses many\n"
    + "                   classes from Ptolemy.\n"
    + "\nExample usage:\n"
    + "    copernicus ~/ptII/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml\n"
    + "(The model is loaded relative to your home directory and the java deep code generator is used.)\n"
    + "    copernicus -codeGenerator shallow ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml\n"
    + "(The model is loaded relative to the classpath and the shallow code generator is used.)\n"
    + "    copernicus -codeGenerator c http://c:/users/neuendor/ptII/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml\n"
    + "(The model is loaded from the given URL (which happens to refer to the local file system, and the c code generator is used.)\n";

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;

    /** If true, then print debugging information. */
    protected boolean _verbose = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Private class that reads a stream in a thread and prints
    // to another stream
    private static class _StreamReaderThread extends Thread {

        _StreamReaderThread(InputStream inputStream, PrintStream stream) {
            _inputStream = inputStream;
            _stream = stream;
        }

        // Read lines from the _inputStream and output them to the
        // _stream
        public void run() {
            try {
                InputStreamReader inputStreamReader =
                    new InputStreamReader(_inputStream);
                BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);


                // If we are writing to stderr, prefix the string.
                // The reason is that writing to stderr causes Tcl
                // exec to return an error unless it is called with
                // -stderrok.  However, we would like to know if the
                // execution of a command prints to stderr, so rather
                // than use -stderrork, we mark the lines.

                //String prefix = ((_stream == System.err) ? "Copernicus._StreamReaderThread() Writing to STDERR: " : "");
                String line = null;
                while ( (line = bufferedReader.readLine()) != null) {
                    
                    _stream.println(/*prefix + */ line);
                }
            } catch (IOException ioe) {
                System.out.flush();
                System.err.println("IOException: " + ioe);
            }
        }

        // Stream to read from.
        private InputStream _inputStream;
        // Stream to write to. Usually System.out or System.err
        private PrintStream _stream;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // GeneratorAttribute that contains Parameters that
    // control code generation.
    private GeneratorAttribute _generatorAttribute;

    // For locking.
    private static Object _lock = new Object();

    // List of parameter names seen on the command line.
    private List _parameterNames = new LinkedList();

    // List of parameter values seen on the command line.
    private List _parameterValues = new LinkedList();

    // The value of the ptolemy.ptII.dir property.
    private String _ptIIDirectory;

    private String _modelPath = null;
    private static String _momlClassName;

    private static MoMLParser _parser;
}
