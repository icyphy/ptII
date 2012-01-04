/* Export a model to a web page.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.swing.SwingUtilities;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.ExportParameters;

///////////////////////////////////////////////////////////////////
//// ExportToWeb
/**
 * Export a model to a web page.
 * Usage:
 * <pre>
 *   java -classpath $PTII ptolemy.vergil.basic.export.html.ExportToWeb \
 *      modelFile \
 *      directoryName
 * </pre>
 * This will open the model and all submodels (including all composite
 * actors, modal models, and state refinements in modal models), optionally
 * run the model (to open all plots), and then export an suite of files
 * that are placed in the specified directory. The index.html file
 * in that directory is the main entry point. Note that the model
 * has to have parameters set for a finite run, or invoking this
 * will not return. To run the model, specify -run before the modelFile.
 * To generate a Ptolemy-style web page using server-side includes,
 * then also give the option -Dptolemy.ptII.exportHTML.usePtWebsite=true.
 * This must be given before the class name,
 * ptolemy.vergil.basic.export.html.ExportToWeb. I.e.,
 *
 * <pre>
 *   java -Dptolemy.ptII.exportHTML.usePtWebsite=true \
 *      -classpath $PTII \
 *      ptolemy.vergil.basic.export.html.ExportToWeb \
 *      modelFile \
 *      directoryName
 * </pre>
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportToWeb {
    
    /////////////////////////////////////////////////////////////////////
    ////                      public methods                         ////

    /** Export a model to a web page.
     *  @param modelFileName A Ptolemy model in MoML format.
     *  The string may start with $CLASSPATH, $HOME or other formats
     *  suitable for {@link ptolemy.util.FileUtilities#nameToFile(String, URI)}.
     *  @param destinationDirectory The destination directory name.
     *  @param run True if the model should be run first.
     *  @exception Exception Thrown if there is a problem reading the model
     *   or exporting the web page.
     */
    public void exportToWeb(final String modelFileName,
            final String destinationDirectory, final boolean run)
            throws Exception {
        // Open the model. The following code executes in the swing
        // event thread, but it spawns another thread to run the model.
        // We will need to wait for that thread to terminate before
        // exiting.
        Runnable openModelAction = new Runnable() {
            public void run() {
                try {
                    System.out.println("Opening " + modelFileName);
                    CompositeActor modelToExport = ConfigurationApplication
                            .openModel(modelFileName);
                    Effigy masterEffigy = Configuration.findEffigy(modelToExport);
                    if (masterEffigy == null) {
                        throw new Exception("Cannot find effigy.");
                    }
                    if (modelToExport == null) {
                        throw new Exception("No model to export.");
                    }
                    BasicGraphFrame graphFrame = BasicGraphFrame.getBasicGraphFrame(modelToExport);
                    
                    ExportParameters parameters;
                    List<WebExportParameters> exportParameters = modelToExport.attributeList(WebExportParameters.class);
                    if (exportParameters != null && exportParameters.size() != 0) {
                        parameters = exportParameters.get(exportParameters.size() - 1).getExportParameters();
                    } else {
                        parameters = new ExportParameters(new File(destinationDirectory));
                        parameters.backgroundColor = Color.white;
                        parameters.openCompositesBeforeExport = true;
                        parameters.showInBrowser = true;
                        // FIXME: Should the following be a command-line option?
                        parameters.copyJavaScriptFiles = false;
                    }
                    if (run) {
                        // This may override the value specified in the model.
                        parameters.runBeforeExport = true;
                    }
                    
                    // Do the export.
                    ExportHTMLAction.exportToWeb(graphFrame, parameters);
                    
                } catch (Throwable throwable) {
                    System.out.println("Failed to open " + modelFileName
                            + ".\n" + throwable.getMessage());
                    throwable.printStackTrace();
                    return;
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        // Need to wait for the thread spawned by the above to exit.
        ExportHTMLAction.waitForExportToComplete();
    }

    /** Export a model as an image.
     *
     *  <p>Note that the a graphical display must be present, this
     *  code displays the model and executes.  To use in a headless
     *  environment under Linux, install Xvfb.</p>
     *
     *  <p>Usage:</p>
     *  <p> To export a model in a directory named "model":</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.html.ExportToWeb model.xml
     *  </pre>
     *  <p>or, to run the model and then export a model in a directory named MyDirectory:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.html.ExportToWeb -run model.xml MyDirectory
     *  </pre>
     *  <p>or, to print a usage message:
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.html.ExportToWeb -help
     *  </pre>
     *
     *  @param args The arguments for the export image operation.
     *  The arguments should be in the format:
     *  <code>[-help] | [-run] model.xml [directory]</code>
     */
    public static void main(String args[]) {
        String usage = "Usage: java -classpath $PTII "
                + "ptolemy.vergil.basic.export.html.ExportToWeb "
                + "[-help] | [-run] model.xml [directory]";
        boolean run = false;
        String modelFileName = null;
        String directoryName = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-run")) {
                run = true;
            } else {
                if (args[i].equalsIgnoreCase("-help")) {
                    System.out.println(usage);
                    StringUtilities.exit(0);
                } else {
                    if (modelFileName == null) {
                        modelFileName = args[i];
                    } else {
                        directoryName = args[i];
                    }
                }
            }
        }
        if (modelFileName == null || args.length > 3) {
            System.err.println("Wrong number of arguments");
            System.err.println(usage);
            // Use StringUtilities.exit() so that we can test unit test this code
            // and avoid FindBugs warnings about System.exit().
            StringUtilities.exit(3);
        }

        // Default directory name matches the model file name without the extension.
        if (directoryName == null) {
            int dot = modelFileName.lastIndexOf(".");
            if (dot == -1) {
                System.err.println("The model file name argument \""
                        + "\" does not have a dot (.) and a directory name was not specified.");
                StringUtilities.exit(4);
            }
            directoryName = modelFileName.substring(0, dot);
        }

        try {
            (new ExportToWeb()).exportToWeb(modelFileName, directoryName, run);
        } catch (Exception ex) {
            ex.printStackTrace();
            StringUtilities.exit(5);
        }
        StringUtilities.exit(0);
    }
}
