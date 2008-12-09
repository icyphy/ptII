/*

 Copyright (c) 1997-2008 The Regents of the University of California.
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

package ptolemy.vergil.erg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.domains.erg.kernel.Event;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.fsm.FSMGraphFrame;
import ptolemy.vergil.fsm.FSMGraphModel;
import diva.graph.GraphPane;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ERGGraphFrame extends FSMGraphFrame {

    /**
     * @param entity
     * @param tableau
     */
    public ERGGraphFrame(CompositeEntity entity, Tableau tableau) {
        super(entity, tableau);
    }

    /**
     * @param entity
     * @param tableau
     * @param defaultLibrary
     */
    public ERGGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    public synchronized void saveAsDesignPattern() {
        Tableau tableau = getTableau();
        PtolemyEffigy effigy = (PtolemyEffigy) tableau.getContainer();
        NamedObj model = effigy.getModel();
        _initialSaveAsFileName = model.getName() + ".xml";
        if (_initialSaveAsFileName.length() == 4) {
            _initialSaveAsFileName = "model.xml";
        }

        ERGController controller = (ERGController) getModel();
        List<Attribute> attributes = controller.attributeList();
        for (Attribute attribute : attributes) {
            attribute.updateContent();
        }

        try {
            _performingSaveAsDesignPattern = true;
            controller.exportAsGroup.setToken(BooleanToken.TRUE);

            JFileChooser fileDialog = _saveAsFileDialog();
            fileDialog.setSelectedFile(new File(fileDialog
                    .getCurrentDirectory(), _initialSaveAsFileName));

            int returnVal = fileDialog.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileDialog.getSelectedFile();
                if (file.getName().indexOf(".") == -1) {
                    file = new File(file.getAbsolutePath() + ".xml");
                }

                try {
                    if (_confirmFile(null, file)) {
                        _directory = fileDialog.getCurrentDirectory();
                        _writeModelToFile(effigy.getElementName(), file);
                    }
                } catch (Exception ex) {
                    report("Error in save as event group.", ex);
                }
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(controller, e,
                    "Unable to export model.");
        } finally {
            _performingSaveAsDesignPattern = false;
            try {
                controller.exportAsGroup.setToken(BooleanToken.FALSE);
            } catch (IllegalActionException e) {
                // Ignore.
            }
        }
    }
    
    protected void _writeModelToFile(String elementName, File file)
            throws IOException {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            String name = getModel().getName();
            String filename = file.getName();
            int period = filename.indexOf(".");
            if (period > 0) {
                name = filename.substring(0, period);
            } else {
                name = filename;
            }
            NamedObj model = getModel();
            if (model.getContainer() != null) {
                fileWriter.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + elementName + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");
            }
            model.exportMoML(fileWriter, 0, name);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new ERGGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final FSMGraphModel graphModel = new FSMGraphModel(
                (CompositeEntity) entity);
        return new FSMGraphPane(_controller, graphModel, entity);
    }

    protected String _getDefaultEventMoML() {
        NamedObj child = GTTools.getChild(_topLibrary, "Event", false, false,
                true, false);
        if (child instanceof Event) {
            return child.exportMoML();
        } else {
            return "<entity name=\"Event\" class=\"ptolemy.domains.erg" +
                    ".kernel.Event\">";
        }
    }

    protected synchronized JFileChooser _saveAsFileDialog() {
        JFileChooser dialog = super._saveAsFileDialog();
        if (_performingSaveAsDesignPattern) {
            Query query = (Query) dialog.getAccessory();
            query.setBoolean("submodel", true);
        }
        return dialog;
    }

    private boolean _performingSaveAsDesignPattern = false;
}
