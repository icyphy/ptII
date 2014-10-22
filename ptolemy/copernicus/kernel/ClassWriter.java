/* A transformer that writes class files.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.copernicus.kernel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import soot.HasPhaseOptions;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.util.JasminOutputStream;

//////////////////////////////////////////////////////////////////////////
//// ClassWriter

/**
 Write all of the application classes out to class files.  Jasmin files
 for the classes will be created in a temporary directory and then
 compiled into bytecode using the Jasmin assembler.  The output
 directory is specified using the outputDirectory parameter.  The class files
 will be placed in the appropriate subdirectory of that directory
 according to their package name.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ClassWriter extends SceneTransformer implements HasPhaseOptions {
    private static ClassWriter instance = new ClassWriter();

    private ClassWriter() {
    }

    public static ClassWriter v() {
        return instance;
    }

    @Override
    public String getPhaseName() {
        return "";
    }

    @Override
    public String getDefaultOptions() {
        return "";
    }

    @Override
    public String getDeclaredOptions() {
        return "debug outputDirectory";
    }

    /** Write out the class file.
     *  This transform can be used to take snapshots, and is
     *  usually called in conjunction with JimpleWriter inside addTransforms():
     *  <pre>
     *   Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot1",
     *           ClassWriter.v()));
     *   Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot1",
     *           JimpleWriter.v()));
     *  </pre>
     *  Sample option arguments:
     *        <code>-p wjtp.snapshot1 outputDirectory:jimple1</code>
     *
     *  @see JimpleWriter
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.snapshot1</code>.
     *  @param options The options Map.  This method uses the
     *  <code>outputDirectory</code> option to specify where the .class
     *  file should be written
     */
    @Override
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ClassWriter.internalTransform(" + phaseName + ", "
                + options + ")");

        String outputDirectory = PhaseOptions.getString(options,
                "outputDirectory");

        if (!outputDirectory.equals("")) {
            File outputDirectoryFile = new File(outputDirectory);

            if (!outputDirectoryFile.isDirectory()) {
                if (!outputDirectoryFile.mkdirs()) {
                    throw new RuntimeException("Failed to create directory \""
                            + outputDirectoryFile + "\"");
                }
            }
        }

        for (Iterator classes = Scene.v().getApplicationClasses().iterator(); classes
                .hasNext();) {
            SootClass theClass = (SootClass) classes.next();

            try {
                _write(theClass, outputDirectory);
            } catch (Exception ex) {
                // If we get an IOException, we might not have any idea
                // of which directory was problematic
                throw new RuntimeException("Creating class file for '"
                        + theClass + "' in directory '" + outputDirectory
                        + "' failed", ex);
            }
        }
    }

    private void _write(SootClass cl, String outputDir) {
        String outputDirWithSep = "";

        if (!outputDir.equals("")) {
            outputDirWithSep = outputDir + System.getProperty("file.separator");
        }

        try {
            File tempFile = new File(outputDirWithSep
                    + cl.getName()
                            .replace(
                                    '.',
                                    System.getProperty("file.separator")
                                            .toCharArray()[0]) + ".class");

            _create(tempFile.getAbsoluteFile());

            OutputStream streamOut = new JasminOutputStream(
                    new FileOutputStream(tempFile));

            PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(
                    streamOut));

            if (cl.containsBafBody()) {
                new soot.baf.JasminClass(cl).print(writerOut);
            } else {
                new soot.jimple.JasminClass(cl).print(writerOut);
            }

            writerOut.flush();
            streamOut.close();

        } catch (IOException ex) {
            throw new RuntimeException("Could not produce new classfile!", ex);
        }
    }

    private void _create(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory \""
                        + file.getParentFile() + "\"");
            }
        }

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create " + file + "\"");
        }
    }
}
