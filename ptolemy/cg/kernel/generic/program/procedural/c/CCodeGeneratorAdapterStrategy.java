/* The CCodeGeneratorAdapterStrategy.

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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.cg.kernel.generic.program.procedural.c;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGeneratorAdapterStrategy;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////CCodeGeneratorAdapterStrategy
/**

The strategy that determines how code should be generated for a certain CCodeGeneratorAdapter.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/
public class CCodeGeneratorAdapterStrategy extends ProceduralCodeGeneratorAdapterStrategy {
    /**
     * Create a new instance of the C code generator adapter.
     */
    public CCodeGeneratorAdapterStrategy() {        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     */
    public CCodeGenerator getCodeGenerator() {
        return (CCodeGenerator) _codeGenerator;
    }

    /** Get the header files needed to compile with the jvm library.
      *  @return A set of strings that are names of the header files
      *   needed by the code generated for jvm library
      *  @exception IllegalActionException Not Thrown in this subclass.
      */
    public Set<String> getJVMHeaderFiles() throws IllegalActionException {
        String javaHome = StringUtilities.getProperty("java.home");

        ExecuteCommands executeCommands = getCodeGenerator()
                .getExecuteCommands();
        if (executeCommands == null) {
            executeCommands = new StreamExec();
        }

        if (!_printedJVMWarning) {
            // We only print this once.
            _printedJVMWarning = true;

            executeCommands.stdout(_eol + _eol
                    + "WARNING: This model uses an actor that "
                    + "links with the jvm library." + _eol
                    + "  To properly run the executable, you must have jvm.dll"
                    + " in your path." + _eol
                    + "  If you do not, then when you run the executable, "
                    + "it will immediately exit" + _eol + "  with no message!"
                    + _eol + "  For example, place " + javaHome
                    + "\\bin\\client" + _eol
                    + "  in your path.  If you are running Vergil from the "
                    + "command line as " + _eol + "  $PTII/bin/ptinvoke, "
                    + "then this has been handled for you." + _eol
                    + "  If you are running via Eclipse, then you must update "
                    + "your path by hand." + _eol + _eol + _eol);
        }

        String jreBinClientPath = javaHome + File.separator + "bin"
                + File.separator + "client";
        executeCommands
                .stdout(_eol + _eol
                        + "CCodeGeneratorAdapter: appended to path "
                        + jreBinClientPath);

        executeCommands.appendToPath(jreBinClientPath);

        javaHome = javaHome.replace('\\', '/');
        if (javaHome.endsWith("/jre")) {
            javaHome = javaHome.substring(0, javaHome.length() - 4);
        }

        if (!(new File(javaHome + "/include").isDirectory())) {
            // It could be that we are running under WebStart
            // or otherwise in a JRE, so we should look for the JDK.
            File potentialJavaHomeParentFile = new File(javaHome)
                    .getParentFile();
            // Loop through twice, once with the parent, once with
            // C:/Program Files/Java.  This is lame, but easy
            for (int loop = 2; loop > 0; loop--) {
                // Get all the directories that have include/jni.h under them.
                File[] jdkFiles = potentialJavaHomeParentFile
                        .listFiles(new FileFilter() {
                            public boolean accept(File pathname) {
                                return new File(pathname, "/include/jni.h")
                                        .canRead();
                            }
                        });
                if (jdkFiles != null && jdkFiles.length >= 1) {
                    // Sort and get the last directory, which should
                    // be the most recent JDK.
                    java.util.Arrays.sort(jdkFiles);
                    javaHome = jdkFiles[jdkFiles.length - 1].toString();
                    break;
                } else {
                    // Not found, please try again.
                    potentialJavaHomeParentFile = new File(
                            "C:\\Program Files\\Java");
                }
            }
        }

        getCodeGenerator().addInclude("-I\"" + javaHome + "/include\"");

        String osName = StringUtilities.getProperty("os.name");
        String platform = "win32";
        if (osName.startsWith("Linux")) {
            platform = "linux";
        } else if (osName.startsWith("SunOS")) {
            platform = "solaris";
        } else if (osName.startsWith("Mac OS X")) {
            platform = "Mac OS X";
        }
        String jvmLoaderDirective = "-ljvm";
        String libjvmAbsoluteDirectory = "";
        if (platform.equals("win32")) {
            getCodeGenerator().addInclude(
                    "-I\"" + javaHome + "/include/" + platform + "\"");

            // The directive we use to find jvm.dll, which is usually in
            // something like c:/Program Files/Java/jre1.6.0_04/bin/client/jvm.dll
            jvmLoaderDirective = "-ljvm";

            String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir")
                    .replace('\\', '/');
            String libjvmRelativeDirectory = "ptolemy/codegen/c/lib/win";
            libjvmAbsoluteDirectory = ptIIDir + "/" + libjvmRelativeDirectory;
            String libjvmFileName = "libjvm.dll.a";
            String libjvmPath = libjvmAbsoluteDirectory + "/" + libjvmFileName;

            if (!(new File(libjvmPath).canRead())) {
                // If we are under WebStart or running from jar files, we
                // will need to copy libjvm.dll.a from the jar file
                // that gcc can find it.
                URL libjvmURL = Thread.currentThread().getContextClassLoader()
                        .getResource(
                                libjvmRelativeDirectory + "/" + libjvmFileName);
                if (libjvmURL != null) {
                    String libjvmAbsolutePath = null;
                    try {
                        // Look for libjvm.dll.a in the codegen directory
                        File libjvmFileCopy = new File(
                                getCodeGenerator().codeDirectory.asFile(),
                                "libjvm.dll.a");

                        if (!libjvmFileCopy.canRead()) {
                            // Create libjvm.dll.a in the codegen directory
                            FileUtilities.binaryCopyURLToFile(libjvmURL,
                                    libjvmFileCopy);
                        }

                        libjvmAbsolutePath = libjvmFileCopy.getAbsolutePath();
                        if (libjvmFileCopy.canRead()) {
                            libjvmAbsolutePath = libjvmAbsolutePath.replace(
                                    '\\', '/');
                            libjvmAbsoluteDirectory = libjvmAbsolutePath
                                    .substring(0, libjvmAbsolutePath
                                            .lastIndexOf("/"));

                            // Get rid of everything before the last /lib
                            // and the .dll.a
                            jvmLoaderDirective = "-l"
                                    + libjvmAbsolutePath.substring(
                                            libjvmAbsolutePath
                                                    .lastIndexOf("/lib") + 4,
                                            libjvmAbsolutePath.length() - 6);

                        }
                    } catch (Exception ex) {
                        throw new IllegalActionException(getComponent(), ex,
                                "Could not copy \"" + libjvmURL
                                        + "\" to the file system, path was: \""
                                        + libjvmAbsolutePath + "\"");
                    }
                }
            }
        } else if (platform.equals("Mac OS X")) {
            if (javaHome != null) {
                libjvmAbsoluteDirectory = javaHome + "/../Libraries";
            }
        } else {
            // Solaris, Linux etc.
            getCodeGenerator().addInclude(
                    "-I\"" + javaHome + "/include/" + platform + "\"");
        }
        getCodeGenerator().addLibrary("-L\"" + libjvmAbsoluteDirectory + "\"");
        getCodeGenerator().addLibrary(jvmLoaderDirective);

        Set<String> files = new HashSet<String>();
        files.add("<jni.h>");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the template parser.
     */
    protected void _createParser() { 
        _templateParser = new CTemplateParser(getComponent(), _adapter);
    }
    
    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatement(Channel source,
            Channel sink, int offset) throws IllegalActionException {

        Type sourceType = ((TypedIOPort) source.port).getType();
        Type sinkType = ((TypedIOPort) sink.port).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }

        // The references are associated with their own adapter, so we need
        // to find the associated adapter.
        String sourcePortChannel = source.port.getName() + "#"
                + source.channelNumber + ", " + offset;
        String sourceRef = (_getAdapter(source.port.getContainer()))
                .getReference(sourcePortChannel);

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
                + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = (_getAdapter(sink.port.getContainer())).getReference(
                sinkPortChannel, true);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = ProgramCodeGeneratorAdapter.generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        String sourceCodeGenType = _codeGenerator.codeGenType(sourceType);
        String sinkCodeGenType = _codeGenerator.codeGenType(sinkType);

        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
            result = "$convert_" + sourceCodeGenType + "_" + sinkCodeGenType
                    + "(" + result + ")";
        }
        return sinkRef + " = " + result + ";" + _eol;
    }

    /** Return the prototype for fire functions.
     * @return The string"(void)" so as to avoid the avr-gcc 3.4.6
     * warning: "function declaration isn't a prototype"
     */
    protected String _getFireFunctionArguments() {
        return "(void)";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** True if we have printed the JVM warning. */
    private boolean _printedJVMWarning;
}
