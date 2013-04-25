/* Implement the Import FMU menu choice.

   Copyright (c) 2012 The Regents of the University of California.
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
   COPYRIGHTENDKEY 2
*/

package ptolemy.vergil.basic.imprt.fmu;

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.AbstractAction;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.actor.lib.fmi.FMUImport;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.gui.Top;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// ImportFMUAction

/**
   An Action to Import a Functional Mock-up Unit (FMU).

   <p>This package is optional.  To add the "Import FMU" menu choice
   to the GraphEditor, add the following to the configuration:</p>
   <pre>
   &lt;property name="_importFMUClassName"
   class="ptolemy.data.expr.StringParameter"
   value="ptolemy.vergil.basic.imprt.fmu.ImportFMUAction"/&gt;
   </pre>
   <p>{@link ptolemy.vergil.basic.BasicGraphFrame} checks for this
   parameter and adds the "Import FMU" menu choice if the class named
   by that parameter exists.</p>

   <p>The <code>$PTII/ptolemy/configs/defaultFullConfiguration.xml</code> file
   already has this parameter.  The ptiny configuration does <b>not</b> have
   this parameter so that we have a smaller download.</p>

   <p>An FMU file is a zipped
   file that contains a file named <code>modelDescription.xml</code>
   that describes the ports and parameters that are created.
   At run time, method calls are made to C functions that are
   included in shared libraries included in the <code>.fmu</code>
   file.</p>

   <p>We use an import facility here is that the user an configure the
   ports of the actor, possibly deleting ports.  If the actor read its
   .fmu file each time it was instantiated, then it would be more difficult
   to manage because the ports could change.</p>

   @author  Christopher Brooks.  Based on ExportPDFAction by Edward A. Lee
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class ImportFMUAction extends AbstractAction {
    // This package is called "imprt" because "import" is a Java keyword.

    /** Create a new action to import a Functional Mock-up Unit (FMU)
     *  .fmu file.
     *  @param frame The Frame which to which this action is added.
     */
    public ImportFMUAction(Top frame) {
        super("Import FMU");
        _frame = frame;
        putValue("tooltip", "Import a Functional Mock-up Unit (FMU) file.");
        //putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_X));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ppublic methods                   ////

    /** Import a FMU. */
    public void actionPerformed(ActionEvent e) {
        _importFMU();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Import a Functional Mock-up Unit (FMU) file.
     */
    private void _importFMU() {
        try {
            Class basicGraphFrameClass = null;
            try {
                basicGraphFrameClass = Class
                        .forName("ptolemy.vergil.basic.BasicGraphFrame");
            } catch (Throwable throwable) {
                throw new InternalErrorException(null, throwable,
                        "Could not find ptolemy.vergil.basic.BasicGraphFrame?");
            }
            if (basicGraphFrameClass == null) {
                throw new InternalErrorException(null, null,
                        "Could not find ptolemy.vergil.basic.BasicGraphFrame!");
            } else if (!basicGraphFrameClass.isInstance(_frame)) {
                throw new InternalErrorException("Frame " + _frame
                        + " is not a BasicGraphFrame?");
            } else {
                BasicGraphFrame basicGraphFrame = (BasicGraphFrame) _frame;

                Query query = new Query();
                query.setTextWidth(60);
                // Use this file chooser so that we can read URLs or files.
                query.addFileChooser("location", "Location (URL)",
                        _lastLocation,
                        /* URI base */null,
                        /* File startingDirectory */basicGraphFrame
                                .getLastDirectory(),
                        /* allowFiles */true,
                        /* allowDirectories */false,
                        /* Color background */
                        PtolemyQuery.preferredBackgroundColor(_frame),
                        PtolemyQuery.preferredForegroundColor(_frame));

                ComponentDialog dialog = new ComponentDialog(_frame,
                        "Instantiate Functional Mock-up Unit (.fmi)", query);
                if (dialog.buttonPressed().equals("OK")) {
                    _lastLocation = query.getStringValue("location");

                    // Get the associated Ptolemy model.
                    GraphController controller = basicGraphFrame.getJGraph()
                            .getGraphPane().getGraphController();
                    AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller
                            .getGraphModel();
                    NamedObj context = model.getPtolemyModel();

                    // Use the center of the screen as a location.
                    Rectangle2D bounds = basicGraphFrame
                            .getVisibleCanvasRectangle();
                    double x = bounds.getWidth() / 2.0;
                    double y = bounds.getHeight() / 2.0;

                    // Unzip the fmuFile.  We probably need to do this
                    // because we will need to load the shared library later.
                    String fmuFileName = null;

                    // FIXME: Use URLs, not files so that we can work from JarZip files.

                    // Only read the file if the name has changed from the last time we
                    // read the file or if the modification time has changed.
                    //fmuFileName = fmuFile.asFile().getCanonicalPath();
                    fmuFileName = _lastLocation;
                    if (fmuFileName.equals(_fmuFileName)) {
                        return;
                    }
                    _fmuFileName = fmuFileName;

                    long modificationTime = new File(fmuFileName)
                            .lastModified();
                    if (_fmuFileModificationTime == modificationTime) {
                        return;
                    }

                    _fmuFileModificationTime = modificationTime;

                    FMUImport.importFMU(this, fmuFileName, context, x, y);
                }
            }
        } catch (Exception ex) {
            MessageHandler.error("Import FMU failed", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables

    /** The name of the fmuFile.
     *  The _fmuFileName field is set the first time we read
     *  the file named by the <i>fmuFile</i> parameter.  The
     *  file named by the <i>fmuFile</i> parameter is only read
     *  if the name has changed or if the modification time of
     *  the file is later than the time the file was last read.
     */
    private String _fmuFileName = null;

    /** The modification time of the file named by the
     *  <i>fmuFile</i> parameter the last time the file was read.
     */
    private long _fmuFileModificationTime = -1;

    /** The top-level window of the contents to be exported. */
    Top _frame;

    /** The most recent location for instantiating a class. */
    private String _lastLocation = "";
}
