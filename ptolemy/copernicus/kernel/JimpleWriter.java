/* A transformer that writes Jimple text.

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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import soot.HasPhaseOptions;
import soot.PhaseOptions;
import soot.Printer;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.util.EscapedWriter;

/**
 A transformer that writes Jimple text.
 @author Stephen Neuendorffer, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JimpleWriter extends SceneTransformer implements HasPhaseOptions {
    private static JimpleWriter instance = new JimpleWriter();

    private JimpleWriter() {
    }

    public static JimpleWriter v() {
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

    /** Write out the Jimple file.
     *  Sample option arguments:
     *        <code>-p wjtp.writeJimple1 outputDirectory:jimple1</code>
     *
     *  @see ClassWriter
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.writeJimple2</code>.
     *  @param options The options Map.  This method uses the
     *  <code>outdir</code> option to specify where the .jimple
     *  file should be written
     */
    @Override
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("JimpleWriter.internalTransform(" + phaseName + ", "
                + options + ")");

        String outputDirectory = PhaseOptions.getString(options,
                "outputDirectory");

        for (Iterator classes = Scene.v().getApplicationClasses().iterator(); classes
                .hasNext();) {
            SootClass theClass = (SootClass) classes.next();

            String fileName;

            if (!outputDirectory.equals("")) {
                File outputDirectoryFile = new File(outputDirectory);

                if (!outputDirectoryFile.isDirectory()) {
                    if (!outputDirectoryFile.mkdirs()) {
                        throw new RuntimeException(
                                "Failed to create directory \""
                                        + outputDirectoryFile + "\"");
                    }
                }

                fileName = outputDirectory
                        + System.getProperty("file.separator");
            } else {
                fileName = "";
            }

            fileName += theClass.getName() + ".jimple";

            FileOutputStream streamOut = null;
            PrintWriter writerOut = null;

            try {
                streamOut = new FileOutputStream(fileName);
                writerOut = new PrintWriter(new EscapedWriter(
                        new OutputStreamWriter(streamOut, java.nio.charset.Charset.defaultCharset())));

                Printer printer = Printer.v();
                printer.setOption(Integer.MAX_VALUE);
                printer.printTo(theClass, new java.io.PrintWriter(writerOut));

                //                theClass.printJimpleStyleTo(writerOut, 0);
            } catch (Exception e) {
                System.out.println("JimpleWriter.internalTransform(): "
                        + "Failed to output jimple for file '" + fileName
                        + "':" + e);
            } finally {
                if (writerOut != null) {
                    writerOut.close();
                }

                try {
                    if (streamOut != null) {
                        streamOut.close();
                    }
                } catch (IOException io) {
                }
            }
        }
    }
}
