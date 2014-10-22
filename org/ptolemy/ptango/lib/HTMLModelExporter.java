/* A subclass of ModelReference that produces on its output an HTML page for the contained model.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib;

import java.awt.Color;
import java.io.StringWriter;
import java.util.List;

import javax.swing.SwingUtilities;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.actor.lib.VisualModelReference;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.ExportParameters;
import ptolemy.vergil.basic.export.html.ExportHTMLAction;

///////////////////////////////////////////////////////////////////
//// HTMLModelExporter
/**
 * A subclass of ModelReference that produces on its output an HTML page for the contained model.
 *
 * FIXME: More
 *
 * Running in a new thread is not supported by HTMLModelExporter.
 *
 * @author Edward A. Lee and Beth Latronico
 * @version $Id$
 * @since Ptolemy II 10.0
 * @see ptolemy.vergil.basic.export.web.HTMLText
 * @see ptolemy.vergil.basic.export.web.LinkToOpenTableaux

 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 */
public class HTMLModelExporter extends VisualModelReference {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public HTMLModelExporter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _parameters = new ExportParameters();

        directoryToExportTo = new FileParameter(this, "directoryToExportTo");
        Parameter allowFiles = new Parameter(directoryToExportTo, "allowFiles");
        allowFiles.setExpression("false");
        allowFiles.setVisibility(Settable.NONE);
        Parameter allowDirectories = new Parameter(directoryToExportTo,
                "allowDirectories");
        allowDirectories.setExpression("true");
        allowDirectories.setVisibility(Settable.NONE);

        backgroundColor = new ColorAttribute(this, "backgroundColor");

        openCompositesBeforeExport = new Parameter(this,
                "openCompositesBeforeExport");
        openCompositesBeforeExport.setTypeEquals(BaseType.BOOLEAN);
        openCompositesBeforeExport.setExpression("false");

        runBeforeExport = new Parameter(this, "runBeforeExport");
        runBeforeExport.setTypeEquals(BaseType.BOOLEAN);
        runBeforeExport.setExpression("false");

        deleteFilesOnExit = new Parameter(this, "deleteFilesOnExit");
        deleteFilesOnExit.setTypeEquals(BaseType.BOOLEAN);
        deleteFilesOnExit.setExpression("true");

        copyJavaScriptFiles = new Parameter(this, "copyJavaScriptFiles");
        copyJavaScriptFiles.setTypeEquals(BaseType.BOOLEAN);
        copyJavaScriptFiles.setExpression("false");

        usePtWebsite = new Parameter(this, "usePtWebsite");
        usePtWebsite.setTypeEquals(BaseType.BOOLEAN);
        usePtWebsite.setExpression("false");
        usePtWebsite.setVisibility(Settable.EXPERT);

        webPage = new TypedIOPort(this, "webPage", false, true);
        webPage.setTypeEquals(BaseType.STRING);

        // Use runBeforeExport rather than the superclass parameter
        // executionOnFiring, which we hide.
        executionOnFiring.setExpression("do nothing");
        executionOnFiring.setVisibility(Settable.EXPERT);

        // Control the opening and closing of the model.
        // FIXME: The options allow for the submodel to not have been
        // opened at all, in which case we want to export only the results
        // of execution.  For now, we just hide the parameters
        // that controls this, openOnFiring and closeOnPostfire.
        openOnFiring.setExpression("open in Vergil");
        closeOnPostfire.setExpression("do nothing");
        openOnFiring.setVisibility(Settable.EXPERT);
        closeOnPostfire.setVisibility(Settable.EXPERT);

        postfireAction.setVisibility(Settable.EXPERT);

        // Do not offer the option to show the result in a browser.
        // It is an output of this actor.
        _parameters.showInBrowser = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                 ports and parameters                      ////

    // Unfortunately, most of these parameters are direct copies
    // from WebExportParameters. I don't know of any way to avoid
    // this code duplication.

    /** Background color. By default this is blank, which indicates
     *  to use the background color of the model.
     */
    public ColorAttribute backgroundColor;

    /** If true, then make an exported web page stand alone.
     *  Instead of referencing JavaScript and image files on the
     *  ptolemy.org website, if this parameter is true, then the
     *  required files will be copied into the target directory.
     *  This is a boolean that defaults to false.
     */
    public Parameter copyJavaScriptFiles;

    /** If true, deleted generated files when the JVM exits.
     */
    public Parameter deleteFilesOnExit;

    /** The directory to export files that the output HTML
     *  references, such as image files. If a relative name is given,
     *  then it is relative to the location of the model file.
     *  By default, this is blank,
     *  which will result in writing to a directory with name
     *  equal to the sanitized name of the model,
     *  and the directory will be contained in the same location
     *  where the model that contains this attribute is stored.
     */
    public FileParameter directoryToExportTo;

    /** If true, hierarchically open all composite actors
     *  in the model before exporting (so that these also
     *  get exported, and hyperlinks to them are created).
     *  This is a boolean that defaults to false.
     */
    public Parameter openCompositesBeforeExport;

    /** If true, run the model before exporting (to open plotter
     *  or other display windows that get exported). Note that
     *  it is important the model have a finite run. This is a
     *  boolean that defaults to false.
     */
    public Parameter runBeforeExport;

    /** If true, use the server-side includes of the Ptolemy website.
     *  This is a boolean that defaults to false. This parameter
     *  is marked as an expert parameter, so by default, it is not
     *  visible.
     */
    public Parameter usePtWebsite;

    /** The output port on which to produce HTML text for this
     *  web page. This has type string.
     */
    public TypedIOPort webPage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == backgroundColor) {
            if (backgroundColor.getToken() == null) {
                // No color specified. Use the model's background color,
                // if there is one, and the default background if not.
                // First, see whether there is a local preferences object
                // in the model.
                NamedObj container = getContainer();
                if (container != null) {
                    List list = container
                            .attributeList(PtolemyPreferences.class);
                    if (list.size() > 0) {
                        // Use the last of the preferences if there is more than one.
                        PtolemyPreferences preferences = (PtolemyPreferences) list
                                .get(list.size() - 1);
                        _parameters.backgroundColor = preferences.backgroundColor
                                .asColor();
                        return;
                    }
                }
                // There is no local preferences. If we have previously
                // looked up a default color in the configuration, use that
                // color.
                if (_defaultColor == null) {
                    // Look for default preferences in the configuration.
                    Effigy effigy = Configuration.findEffigy(container
                            .toplevel());
                    if (effigy == null) {
                        // No effigy. Can't find a configuration.
                        _defaultColor = Color.white;
                    } else {
                        Configuration configuration = (Configuration) effigy
                                .toplevel();
                        try {
                            PtolemyPreferences preferences = PtolemyPreferences
                                    .getPtolemyPreferencesWithinConfiguration(configuration);
                            if (preferences != null) {
                                _defaultColor = preferences.backgroundColor
                                        .asColor();
                            }
                        } catch (IllegalActionException ex) {
                            _defaultColor = Color.white;
                        }
                    }
                }
                _parameters.backgroundColor = _defaultColor;
            } else {
                _parameters.backgroundColor = backgroundColor.asColor();
            }
        } else if (attribute == copyJavaScriptFiles) {
            _parameters.copyJavaScriptFiles = ((BooleanToken) copyJavaScriptFiles
                    .getToken()).booleanValue();
        } else if (attribute == deleteFilesOnExit) {
            _parameters.deleteFilesOnExit = ((BooleanToken) deleteFilesOnExit
                    .getToken()).booleanValue();
        } else if (attribute == directoryToExportTo) {
            _parameters.directoryToExportTo = directoryToExportTo.asFile();
        } else if (attribute == openCompositesBeforeExport) {
            _parameters.openCompositesBeforeExport = ((BooleanToken) openCompositesBeforeExport
                    .getToken()).booleanValue();
        } else if (attribute == runBeforeExport) {
            _parameters.runBeforeExport = ((BooleanToken) runBeforeExport
                    .getToken()).booleanValue();
            if (_parameters.runBeforeExport) {
                executionOnFiring.setExpression("run in calling thread");
            } else {
                executionOnFiring.setExpression("do nothing");
            }
        } else if (attribute == usePtWebsite) {
            _parameters.usePtWebsite = ((BooleanToken) usePtWebsite.getToken())
                    .booleanValue();
        } else if (attribute == executionOnFiring) {
            String executionOnFiringValue = executionOnFiring.stringValue();

            if (executionOnFiringValue.equals("run in a new thread")) {
                throw new IllegalActionException(this,
                        "Running in a new thread is not supported by HTMLModelExporter.");
            }
            super.attributeChanged(attribute);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the attribute.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HTMLModelExporter newObject = (HTMLModelExporter) super
                .clone(workspace);
        newObject._defaultColor = null;
        newObject._parameters = new ExportParameters();
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: The opening must occur in the event thread.
        // Regrettably, we cannot continue with the firing until
        // everything is complete, so we use the very dangerous
        // invokeAndWait() method.
        _exception = null;
        Runnable doExport = new Runnable() {
            @Override
            public void run() {
                // Since we do not support running in a new thread,
                // the model has completed execution if it is to execute at all.
                if (_tableau != null) {
                    // FIXME: If _parameters.directoryToExportTo == null or
                    // is not a directory, then
                    // get the temporary directory from the WebServer.
                    // See ExportHTMLAction.exportToWeb
                    // FIXME: Following should not be necessary. The above super.fire() call should shown the tableau.
                    _tableau.show();
                    // FIXME: Do we have to wait for the frame to exist?
                    BasicGraphFrame graphFrame = (BasicGraphFrame) _tableau
                            .getFrame();
                    if (!ExportHTMLAction.copyJavaScriptFilesIfNeeded(
                            graphFrame, _parameters)) {
                        // Canceled the export.
                        _exception = new IllegalActionException(
                                HTMLModelExporter.this, "Export canceled.");
                        return;
                    }

                    // Create a writer to write to.
                    StringWriter writer = new StringWriter();

                    // Now do the export.
                    try {
                        // The null third argument ensure that there is no attempt to open
                        // the exported file in a browser.
                        ExportHTMLAction.openRunAndWriteHTML(graphFrame,
                                _parameters, null, writer, true);
                        writer.flush();
                        webPage.send(0, new StringToken(writer.toString()));
                    } catch (IllegalActionException e) {
                        _exception = e;
                    }
                } else {
                    _exception = new IllegalActionException(
                            HTMLModelExporter.this, "No model to export.");
                }
            }
        };
        try {
            // FIXME: No, this can't work. If the model runs and wants
            // to perform GUI actions, such as generate plots, in other
            // threads, such as PN threads, then those GUI actions will block.
            // We need to run the model outside the GUI thread.
            SwingUtilities.invokeAndWait(doExport);
        } catch (Exception ex) {
            throw new IllegalActionException(this, null, ex, "Open failed.");
        }
        if (_exception != null) {
            throw _exception;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The default background color. */
    private Color _defaultColor;

    /** If an exception occurs in event thread activities, it will be stored here. */
    private IllegalActionException _exception = null;

    /** The current parameter values. */
    private ExportParameters _parameters;
}
