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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;

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
        // event thread and sets the value of the local variable
        // _modelToExport.
        _modelToExport = null;
        Runnable openModelAction = new Runnable() {
            public void run() {
                try {
                    System.out.println("Opening " + modelFileName);
                    _modelToExport = ConfigurationApplication
                            .openModel(modelFileName);
                    _masterEffigy = Configuration.findEffigy(_modelToExport);
                    if (_masterEffigy == null) {
                        throw new Exception("Cannot find effigy.");
                    }

                    NamedObj toplevel = _masterEffigy.toplevel();
                    if (!(toplevel instanceof Configuration)) {
                        throw new Exception("Cannot find configuration.");
                    }
                    _configuration = (Configuration) toplevel;

                } catch (Throwable throwable) {
                    System.out.println("Failed to open " + modelFileName
                            + ".\n" + throwable.getMessage());
                    return;
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        if (_modelToExport == null) {
            throw new Exception("No model to export.");
        }

        _basicGraphFrame = BasicGraphFrame.getBasicGraphFrame(_modelToExport);

        // Get permission to write to the destination directory.
        final File directory = new File(destinationDirectory);
        if (directory.exists()) {
            if (directory.isDirectory()) {
                if (!MessageHandler.yesNoQuestion("Directory exists: "
                        + directory + ". Overwrite contents?")) {
                    MessageHandler.message("HTML export canceled.");
                    return;
                }
            } else {
                if (!MessageHandler
                        .yesNoQuestion("File exists with the same name. Overwrite file?")) {
                    MessageHandler.message("HTML export canceled.");
                    return;
                }
                if (!directory.delete()) {
                    MessageHandler.message("Unable to delete file.");
                    return;
                }
                if (!directory.mkdir()) {
                    MessageHandler.message("Unable to create directory.");
                    return;
                }
            }
        } else {
            if (!directory.mkdir()) {
                MessageHandler.message("Unable to create directory.");
                return;
            }
        }

        if (run) {
            // Optionally run the model.
            Runnable runAction = new Runnable() {
                public void run() {
                    try {
                        System.out.println("Running "
                                + _modelToExport.getFullName());
                        Manager manager = _modelToExport.getManager();
                        if (manager == null) {
                            manager = new Manager(_modelToExport.workspace(),
                                    "MyManager");
                            _modelToExport.setManager(manager);
                        }
                        _modelToExport
                                .setModelErrorHandler(new BasicModelErrorHandler());
                        manager.execute();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(runAction);
        }

        // Open submodels and export to web
        Runnable ExportToWebAction = new Runnable() {
            public void run() {
                try {
                    // Open submodels.
                    System.out.println("Opening submodels.");

                    Set<Effigy> effigiesToClose = new HashSet<Effigy>();
                    effigiesToClose.add(_masterEffigy);

                    List<Entity> entities = _modelToExport.entityList();
                    for (Entity entity : entities) {
                        _openEntity(entity, effigiesToClose);
                    }

                    System.out.println("Writing web files to " + directory);
                    ExportHTMLAction action = new ExportHTMLAction(
                            _basicGraphFrame);
                    action.writeHTML(directory);

                    System.out.println("Closing the model.");
                    // Close all tableau for top-level effigies.
                    // For simple cases, the following line will work. But if the
                    // model has classes defined in a separate file, then it won't.
                    // ConfigurationApplication.closeModelWithoutSavingOrExiting(_modelToExport);
                    Iterator iterator = effigiesToClose.iterator();
                    while (iterator.hasNext()) {
                        Effigy effigy = (Effigy) iterator.next();
                        effigy.closeTableaux();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(ExportToWebAction);
    }

    /** Export a model as an image.
     *
     *  <p>Note that the a graphical display must be present, this
     *  code displays the model and executes.  To use in a headless
     *  environment under Linux, install Xvfb.</p>
     *
     *  <p>Usage:</p>
     *  <p> To save a gif:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.ExportToWeb model.xml
     *  </pre>
     *  <p>or, to save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.ExportToWeb png model.xml
     *  </pre>
     *  <p>or, to run the model and then save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.ExportToWeb -run png model.xml
     *  </pre>
     *
     *  @param args The arguments for the export image operation.
     *  The arguments should be in the format:
     *  [-run] [-save] [GIF|gif|PNG|png] model.xml.
     *
     *  @exception args If there is 1 argument, then it names a
     *  Ptolemy MoML file and the model is exported as a .gif file.
     *  If there are two arguments, then the first argument names
     *  a format, current formats are GIF, gif, PNG and png and
     *  the second argument names a Ptolemy MoML file.
     */
    public static void main(String args[]) {
        String usage = "Usage: java -classpath $PTII "
                + "ptolemy.vergil.basic.ExportToWeb "
                + "[-run] model.xml directory";
        boolean run = false;
        String modelFileName = null;
        String directoryName = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-run")) {
                run = true;
            } else {
                if (modelFileName == null) {
                    modelFileName = args[i];
                } else {
                    directoryName = args[i];
                }
            }
        }
        if (modelFileName == null || args.length > 3) {
            System.err.println("Wrong number of arguments");
            System.err.println(usage);
            System.exit(3);
        }

        // Default directory name matches the model file name without the extension.
        if (directoryName == null) {
            int dot = modelFileName.lastIndexOf(".");
            directoryName = modelFileName.substring(0, dot);
        }

        try {
            new ExportToWeb().exportToWeb(modelFileName, directoryName, run);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(5);
        }
        System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open a composite entity and recursively open any composite
     *  entities or state refinements that it contains.
     *  @param entity The entity to open.
     *  @param effigiesToClose A list that the effigy will be added to
     *   if its effigy is not contained by the _masterEffigy (i.e.,
     *   what was opened was a class defined in another file).
     *  @exception IllegalActionException If opening fails.
     *  @exception NameDuplicationException Not thrown.
     */
    private void _openComposite(CompositeEntity entity,
            Set<Effigy> effigiesToClose) throws IllegalActionException,
            NameDuplicationException {
        Tableau tableau = _configuration.openModel(entity);
        NamedObj effigy = tableau.getContainer();
        // If the model for the effigy is not the same as the entity,
        // the entity is an AO class instance, and the class definition
        // was opened instead. If that class definition does not have
        // the same top level as entity, then it will have to be
        // explicitly closed.
        if (effigy instanceof Effigy && !_masterEffigy.deepContains(effigy)) {
            effigiesToClose.add((Effigy) effigy);
        }
        List<Entity> entities = (entity).entityList();
        for (Entity inside : entities) {
            _openEntity(inside, effigiesToClose);
        }
    }

    /** Open the specified entity using the specified configuration.
     *  This method will recursively descend through the model, opening
     *  every composite actor and every state refinement.
     *  @param entity The entity to open.
     *  @param effigiesToClose A list that the effigy will be added to
     *   if its effigy is not contained by the _masterEffigy (i.e.,
     *   what was opened was a class defined in another file).
     */
    private void _openEntity(Entity entity, Set<Effigy> effigiesToClose)
            throws IllegalActionException, NameDuplicationException {
        if (entity instanceof CompositeEntity) {
            _openComposite((CompositeEntity) entity, effigiesToClose);
        } else if (entity instanceof State) {
            TypedActor[] refinements = ((State) entity).getRefinement();
            for (TypedActor refinement : refinements) {
                _openComposite((CompositeEntity) refinement, effigiesToClose);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The BasicGraphFrame of the model. */
    private BasicGraphFrame _basicGraphFrame;

    /** Used in exportToWeb() to record the configuration for _modelToExport. */
    private Configuration _configuration;

    /** Used in exportToWeb() to record the effigy for _modelToExport. */
    private Effigy _masterEffigy;

    /** Used in exportToWeb() to communicate between the main thread and the
     *  Swing event thread.
     */
    private TypedCompositeActor _modelToExport;
}
