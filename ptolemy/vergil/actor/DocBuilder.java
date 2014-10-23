/* Build documentation for Java and Actors

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gui.Configuration;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// DocBuilder

/** Build Documentation for Java and Actors.
 *
 *  <p>This class sets the commands that build the Java classes.
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 5.2
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public class DocBuilder extends Attribute {

    // In principle, this class should be usable from both within a UI
    // and without a UI.

    /** Create a new instance of the DocBuilder.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public DocBuilder(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        cleanFirst = new Parameter(this, "cleanFirst");
        cleanFirst.setTypeEquals(BaseType.BOOLEAN);

        cleanFirst.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then clean before building documentation.  The default
     *  value is true because if a user is adding an actor, then the
     *  codeDoc/tree.html, codeDoc/ptolemy/actor/lib/Ramp.xml,
     *  and codeDoc/ptolemy/actor/lib/RampIdx.xml files might already
     *  exist.  It is safer to force a clean each time because the
     *  makefile does not accurately capture the dependencies between
     *  .java sources and .html, .xml and Idx.xml files.
     */
    public Parameter cleanFirst;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Build the Java class and Actor documentation.
     *  The default is to run make in <code>$PTII/doc</code>.
     *
     *  However, if the configuration set by {@link
     *  #setConfiguration(Configuration)} then the configuration is
     *  searched for a _docApplicationSpecializer parameter.  If that
     *  parameter exists it is assumed to name a class that implements
     *  the {@link DocApplicationSpecializer} interface and the
     *  {@link DocApplicationSpecializer#buildCommands(ExecuteCommands)}
     *  method which returns the commands to invoke.
     *
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception IllegalActionException If there is a problem building
     *  the documentation.
     */
    public int buildDocs() throws IllegalActionException {
        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }
        return _executeCommands();
    }

    /** Get the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @return executeCommands The subprocess command executor.
     *  @see #setExecuteCommands(ExecuteCommands)
     */
    public ExecuteCommands getExecuteCommands() {
        return _executeCommands;
    }

    /** Set the configuration.
     *  @param configuration The configuration in which we look up the
     *  _applicationName and _docApplicationSpecializer parameters.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    /** Set the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @param executeCommands The subprocess command executor.
     *  @see #getExecuteCommands()
     */
    public void setExecuteCommands(ExecuteCommands executeCommands) {
        _executeCommands = executeCommands;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the command to compile ptII/doc/doclets/PtDoclet.
     */
    private static String _compilePtDoclet(File ptII) {
        String results = "";
        try {
            String javaHome = StringUtilities.getProperty("java.home");
            if (javaHome != null && javaHome.length() > 1) {
                javaHome = javaHome.replace('\\', '/');
                String toolsJarFileBase = "/../lib/tools.jar";
                File toolsJarFile = new File(javaHome + toolsJarFileBase);
                if (toolsJarFile.exists()) {
                    results = "javac -classpath \"" + ptII + File.pathSeparator
                            + javaHome + toolsJarFileBase
                            + "\" doc/doclets/PtDoclet.java";
                } else {
                    if (StringUtilities.getProperty("os.name").equals(
                            "Mac OS X")) {
                        results = "javac -classpath \"" + ptII
                                + "\" doc/doclets/PtDoclet.java";
                    } else {
                        results = "echo \"Warning: Failed to generate commands to compile "
                                + "ptII/doc/doclets/PtDoclet.java. The jar file tools.jar at "
                                + toolsJarFile.getCanonicalPath()
                                + " does not exist?\"";
                    }
                }
            }
        } catch (Throwable throwable) {
            results = "echo \"Warning, failed to generate command "
                    + "to compile ptII/doc/doclets/PtDoclet.java: "
                    + KernelException.stackTraceToString(throwable) + "\"";

        }
        return results;
    }

    /** Build the documentation.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     */
    private int _executeCommands() throws IllegalActionException {

        File ptII = new File(StringUtilities.getProperty("ptolemy.ptII.dir")
                + "/doc");
        _executeCommands.setWorkingDirectory(ptII);

        List commands = null;
        // Search for a _docApplicationSpecializer in the configuration.
        Parameter docApplicationSpecializerParameter = null;
        if (_configuration != null) {
            docApplicationSpecializerParameter = (Parameter) _configuration
                    .getAttribute("_docApplicationSpecializer", Parameter.class);
        }
        if (docApplicationSpecializerParameter != null) {
            String docApplicationSpecializerClassName = docApplicationSpecializerParameter
                    .getExpression();

            try {
                Class docApplicationSpecializerClass = Class
                        .forName(docApplicationSpecializerClassName);
                DocApplicationSpecializer docApplicationSpecializer = (DocApplicationSpecializer) docApplicationSpecializerClass
                        .newInstance();
                commands = docApplicationSpecializer
                        .buildCommands(_executeCommands);
            } catch (Throwable throwable) {
                throw new IllegalActionException(
                        "Failed to call doc application initializer "
                                + "class \""
                                + docApplicationSpecializerClassName
                                + "\" buildCommands() method");
            }
        } else {
            commands = new LinkedList();
            String applicationName = null;

            try {
                StringAttribute applicationNameAttribute = (StringAttribute) _configuration
                        .getAttribute("_applicationName", StringAttribute.class);

                if (applicationNameAttribute != null) {
                    applicationName = applicationNameAttribute.getExpression();
                }
            } catch (Throwable throwable) {
                // Ignore and use the default applicationName: "",
                // which means we look in doc.codeDoc.
            }

            // Windows users might not have the rm command.
            if (((BooleanToken) cleanFirst.getToken()).booleanValue()) {
                String codeDocDirectory = ptII + "/codeDoc";
                _executeCommands.updateStatusBar("Deleting the contents of \""
                        + codeDocDirectory + "\".");
                if (!FileUtilities.deleteDirectory(codeDocDirectory)) {
                    _executeCommands
                            .stderr("Warning: Could not delete some of the files in \""
                                    + codeDocDirectory + "\".");
                }
            }

            if (applicationName == null) {
                File ptIImk = new File(
                        StringUtilities.getProperty("ptolemy.ptII.dir")
                        + "/mk/ptII.mk");
                // If the user has run configure, then we run make,
                // otherwise we run the javadoc command.
                if (ptIImk.exists()) {
                    commands.add("make codeDoc/tree.html");
                    commands.add("make codeDoc/ptolemy/actor/lib/Ramp.xml");
                    commands.add("make codeDoc/ptolemy/actor/lib/RampIdx.xml");
                } else {
                    ptII = new File(
                            StringUtilities.getProperty("ptolemy.ptII.dir"));
                    _executeCommands.setWorkingDirectory(ptII);
                    _executeCommands
                    .updateStatusBar("When creating docs, warnings are ok.");

                    commands.add(_compilePtDoclet(ptII));

                    String styleSheetFile = "";
                    //Unfortunately, -stylesheetfile is not supported with -doclet?
                    //                     String styleSheetFileName = ptII + "/doc/doclets/stylesheet.css";
                    //                     if (new File(styleSheetFileName).exists()) {
                    //                         styleSheetFile = "-stylesheetfile " + styleSheetFileName + " ";
                    //                     } else {
                    //                         System.out.println("DocBuilder: Warning: could not find "
                    //                                 + styleSheetFileName + ". The JavaDoc output might "
                    //                                 + "be hard to read from within Vergil.");
                    //                     }

                    //                    commands.add("which javadoc");
                    commands.add("javadoc -classpath \""
                            + StringUtilities.getProperty("java.class.path")
                            + "\" -J-Xmx512m -d doc/codeDoc "
                            + styleSheetFile
                            + "-doclet doc.doclets.PtDoclet "
                            + "-subpackages com:diva:jni:org:ptolemy:thales "
                            + "-exclude ptolemy.apps:ptolemy.copernicus:diva.util.java2d.svg");
                    commands.add("java -Xmx256m -classpath \""
                            + StringUtilities.getProperty("java.class.path")
                            + "\" ptolemy.moml.filter.ActorIndex doc/codeDoc/allNamedObjs.txt "
                            + "\"" + ptII
                            + "/ptolemy/configs/doc/models.txt\" doc/codeDoc");
                }
            } else {
                commands.add("make codeDoc" + applicationName
                        + "/doc/codeDoc/tree.html");
                commands.add("make APPLICATION=" + applicationName
                        + " \"PTDOCFLAGS=-d doc/codeDoc" + applicationName
                        + "/doc/codeDoc" + " codeDoc" + applicationName
                        + "/ptolemy/actor/lib/Ramp.xml");
                commands.add("make APPLICATION=" + applicationName + " codeDoc"
                        + applicationName + "/ptolemy/actor/lib/RampIdx.xml");
            }
            if (commands.size() == 0) {
                return -1;
            }
        }

        _executeCommands.setCommands(commands);

        try {
            // FIXME: need to put this output in to the UI, if any.
            _executeCommands.start();
        } catch (Exception ex) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + "\n");
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:\n" + errorMessage);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The configuration in which we look up the
     *  _applicationName and _docApplicationSpecializer parameters.
     */
    private Configuration _configuration;

    /** The object that actually executes the commands.
     */
    private ExecuteCommands _executeCommands;
}
