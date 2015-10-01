package ptolemy.vergil.basic.imprt.fmu;

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;

import diva.graph.GraphController;
import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.actor.lib.fmi.FMUImportHybrid;
import ptolemy.data.expr.FileParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.gui.Top;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;

@SuppressWarnings("serial")
public class ImportFMUHybridAction extends AbstractAction {

    public ImportFMUHybridAction(Top frame) {
        super("Import FMU Hybrid as a Ptolemy Actor");
        _frame = frame;
        putValue("tooltip", "Import a Functional Mock-up Unit (FMU) file as a Ptolemy actor.");
        //putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_X));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ppublic methods                   ////

    /** Import a FMU. */
    @Override
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
                basicGraphFrameClass = Class.forName("ptolemy.vergil.basic.BasicGraphFrame");
            } catch (Throwable throwable) {
                throw new InternalErrorException(null, throwable,
                        "Could not find ptolemy.vergil.basic.BasicGraphFrame?");
            }
            if (basicGraphFrameClass == null) {
                throw new InternalErrorException(null, null, "Could not find ptolemy.vergil.basic.BasicGraphFrame!");
            } else if (!basicGraphFrameClass.isInstance(_frame)) {
                throw new InternalErrorException("Frame " + _frame + " is not a BasicGraphFrame?");
            } else {
                BasicGraphFrame basicGraphFrame = (BasicGraphFrame) _frame;

                Query query = new Query();
                query.setTextWidth(60);

                // Use this file chooser so that we can read URLs or files.
                query.addFileChooser("location", "Location (URL)", _lastLocation, /* URI base */null,
                        /* File startingDirectory */basicGraphFrame.getLastDirectory(), /* allowFiles */true,
                        /* allowDirectories */false,
                        /* Color background */
                        PtolemyQuery.preferredBackgroundColor(_frame), PtolemyQuery.preferredForegroundColor(_frame));
                query.addCheckBox("modelExchange", "Import for Model Exchange", _lastModelExchange);

                ComponentDialog dialog = new ComponentDialog(_frame, "Instantiate Functional Mock-up Unit (FMU)",
                        query);
                if (dialog.buttonPressed().equals("OK")) {
                    _lastLocation = query.getStringValue("location");
                    _lastModelExchange = query.getBooleanValue("modelExchange");

                    // Use the center of the screen as a location.
                    Rectangle2D bounds = basicGraphFrame.getVisibleCanvasRectangle();
                    double x = bounds.getWidth() / 2.0;
                    double y = bounds.getHeight() / 2.0;

                    // Unzip the fmuFile.  We probably need to do this
                    // because we will need to load the shared library later.
                    String fmuFileName = null;

                    // FIXME: Use URLs, not files so that we can work from JarZip files.

                    fmuFileName = _lastLocation;

                    // Get the associated Ptolemy model.
                    GraphController controller = basicGraphFrame.getJGraph().getGraphPane().getGraphController();
                    AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller.getGraphModel();
                    NamedObj context = model.getPtolemyModel();

                    // Create a temporary FileParameter so that we can use
                    // $PTII or $CLASSPATH.  The issue here is that the dialog
                    // that is brought up is a ptolemy.gui.Query, which
                    // does not know about FileParameter
                    FileParameter fmuFileParameter = (FileParameter) context.getAttribute("_fmuFile",
                            FileParameter.class);
                    try {
                        if (fmuFileParameter == null) {
                            fmuFileParameter = new FileParameter(context, "_fmuFile");
                        }
                        fmuFileParameter.setExpression(fmuFileName);
                        fmuFileParameter.setPersistent(false);
                        fmuFileParameter.setVisibility(Settable.EXPERT);

                        FMUImportHybrid.importFMU(this, fmuFileParameter, context, x, y, _lastModelExchange);
                    } finally {
                        // Avoid leaving a parameter in the model.
                        if (fmuFileParameter != null) {
                            fmuFileParameter.setContainer(null);
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            MessageHandler.error("Import FMU failed.", throwable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables

    /** The top-level window of the contents to be exported. */
    Top _frame;

    /** The most recent location for instantiating a class. */
    private String _lastLocation = "";

    /** The most recent selection of Model Exchange (vs. Co-Simulation). */
    private boolean _lastModelExchange = false;
}
