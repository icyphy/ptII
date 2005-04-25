/* Top-level window for Ptolemy models with a menubar and status bar.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.expr.FileParameter;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;


//////////////////////////////////////////////////////////////////////////
//// PtolemyFrame

/**
   This is a top-level window for Ptolemy models with a menubar and status bar.
   Derived classes should add components to the content pane using a
   line like:
   <pre>
   getContentPane().add(component, BorderLayout.CENTER);
   </pre>
   This extends the base class by associating with it a Ptolemy II model
   or object and specifying a model error handler for that model
   that handles model errors by throwing an exception.
   <p>
   If the model contains an instance of FileParameter named "_help", then
   the file or URL specified by that attribute will be opened when "Help"
   in the Help menu is invoked.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Green (eal)
   @Pt.AcceptedRating Yellow (johnr)
*/
public abstract class PtolemyFrame extends TableauFrame {
    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     */
    public PtolemyFrame(NamedObj model) {
        this(model, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model
     *  or object. After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model or object to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame, or null if none.
     */
    public PtolemyFrame(NamedObj model, Tableau tableau) {
        super(tableau);

        // Create a file filter that accepts .xml and .moml files.
        LinkedList extensions = new LinkedList();
        extensions.add("xml");
        extensions.add("moml");
        extensions.add("XML");
        extensions.add("MOML");
        extensions.add("MoML");
        _fileFilter = new ExtensionFileFilter(extensions);

        setModel(model);

        // Set the window properties if there is an attribute in the
        // model specifying them.  Errors are ignored.
        try {
            WindowPropertiesAttribute properties = (WindowPropertiesAttribute) model
                            .getAttribute("_windowProperties",
                                WindowPropertiesAttribute.class);

            if (properties != null) {
                properties.setProperties(this);
            }
        } catch (IllegalActionException ex) {
            // Ignore.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the associated model or Ptolemy II object.
     *  This can be a CompositeEntity or an EditorIcon, and possibly
     *  other Ptolemy II objects.
     *  @return The associated model or object.
     */
    public NamedObj getModel() {
        return _model;
    }

    /** Set the associated model.  This also sets an error handler for
     *  the model that results in model errors throwing an exception
     *  and associates an undo stack with the model.
     *  @param model The associated model.
     */
    public void setModel(NamedObj model) {
        _model = model;
        _model.setModelErrorHandler(new BasicModelErrorHandler());

        List attrList = _model.attributeList(UndoStackAttribute.class);

        if (attrList.size() == 0) {
            // Create and attach a new instance
            try {
                new UndoStackAttribute(_model, "_undoInfo");
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        if (super._clear()) {
            setModel(new CompositeEntity());
            return true;
        } else {
            return false;
        }
    }

    /** Close the window.  Look for any Dialogs that are open and close those
     *  first. If a DialogTableau returns false then it means that the user
     *  has cancelled the close operation.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        PtolemyEffigy ptolemyEffigy = (PtolemyEffigy) getEffigy();

        // The effigy should not be null, but if the window has
        // already been closed somehow, then it will be.
        if (ptolemyEffigy != null) {
            List tableaux = ptolemyEffigy.entityList(Tableau.class);
            Iterator tableauxIterator = tableaux.iterator();

            while (tableauxIterator.hasNext()) {
                Tableau tableau = (Tableau) tableauxIterator.next();

                if (tableau instanceof DialogTableau) {
                    DialogTableau dialogTableau = (DialogTableau) tableau;

                    if (!(dialogTableau.close())) {
                        return false;
                    }
                }
            }
        }

        return super._close();
    }

    /** Display more detailed information than given by _about().
     *  If the model contains an instance of FileParameter named "_help",
     *  that the file or URL given by that attribute is opened.  Otherwise,
     *  a built-in generic help file is opened.
     */
    protected void _help() {
        try {
            FileParameter helpAttribute = (FileParameter) getModel()
                                                                          .getAttribute("_help",
                                FileParameter.class);
            URL doc = helpAttribute.asURL();
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            super._help();
        }
    }

    /** Print the contents.  If this frame implements either the
     *  Printable or Pageable then those interfaces are used to print
     *  it.  This overrides the base class to queue a change request to do
     *  the printing, because otherwise, printing will cause a deadlock.
     */
    protected void _print() {
        if (_model != null) {
            ChangeRequest request = new ChangeRequest(this, "Print") {
                    protected void _execute() throws Exception {
                        PtolemyFrame.super._print();
                    }
                };

            _model.requestChange(request);
        } else {
            super._print();
        }
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  If setModel() has been called, then the initial filename
     *  is set to the name of the model.  If setModel() has not yet
     *  been called, then the initial filename to
     *  <code>model.xml</code>.
     *  If the model is not idle or paused, we first pause it before
     *  calling the parent _saveAs() method and then resume when
     *  we return from the parent _saveAs() method.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        if (_model != null) {
            // Use the name of the top level by default.
            _initialSaveAsFileName = _model.toplevel().getName() + ".xml";

            if (_initialSaveAsFileName.length() == 4) {
                // Useless model name (empty string).
                _initialSaveAsFileName = "model.xml";
            }
        } else {
            _initialSaveAsFileName = "model.xml";
        }

        // If the model is not idle or paused, then pause it while saving
        // This solves bug where if we have Const -> MonitorValue with
        // SDFDirector with default parameters and run it and then do
        // SaveAs, we got strange behaviour.
        if (_model instanceof CompositeActor) {
            Manager manager = ((CompositeActor) _model).getManager();

            if (manager != null) {
                Manager.State state = manager.getState();

                if ((state == Manager.IDLE) && (state == Manager.PAUSED)) {
                    return super._saveAs();
                } else {
                    manager.pause();

                    boolean returnValue = super._saveAs();
                    manager.resume();
                    return returnValue;
                }
            }
        }

        return super._saveAs();
    }

    /** Create and return a file dialog for the "Save As" command.
     *  This overrides the base class to add options to the dialog.
     *  @return A file dialog for save as.
     */
    protected JFileChooser _saveAsFileDialog() {
        JFileChooser fileDialog = super._saveAsFileDialog();

        if ((_model != null) && (_model.getContainer() != null)) {
            _query = new Query();
            _query.addCheckBox("submodel", "Save submodel only", false);
            fileDialog.setAccessory(_query);
        }

        return fileDialog;
    }

    /** Write the model to the specified file.  This method delegates
     *  to the top effigy containing the associated Tableau, if there
     *  is one, and otherwise throws an exception. This ensures that the
     *  data written is the description of the entire model, not just
     *  the portion within some composite actor.   It also adjusts the
     *  URIAttribute in the model to match the specified file, if
     *  necessary, and creates one otherwise.  It also
     *  overrides the base class to update the attributes if they need
     *  to update their content.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        Tableau tableau = getTableau();

        if (tableau != null) {
            Effigy effigy = (Effigy) tableau.getContainer();

            if (effigy != null) {
                // Update all the attributes that need updated.
                if (_model != null) {
                    Iterator attributes = _model.attributeList(Attribute.class)
                                                            .iterator();

                    while (attributes.hasNext()) {
                        Attribute attribute = (Attribute) attributes.next();
                        attribute.updateContent();
                    }
                }

                // Ensure that if we do ever try to call this method,
                // that it is the top effigy that is written.
                // If there is no model, delegate to the top effigy.
                // Otherwise, delegate to the effigy corresponding
                // to the top-level of the model (which may not be
                // the same as the top effigy, e.g. when using
                // ModelReference). An exception is that if we
                // in a saveAs command (_query != null) and the
                // user has requested saving the submodel, then
                // we do no delegating.
                if (_model == null) {
                    effigy = effigy.topEffigy();
                } else if ((_query == null)
                                || ((_model.getContainer() != null)
                                && !_query.getBooleanValue("submodel"))) {
                    effigy = effigy.masterEffigy();
                }

                effigy.writeFile(file);
                return;
            }
        }

        throw new IOException("Cannot find an effigy to delegate writing.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The model that this window controls, if any.
    private NamedObj _model;

    // The query used to specify save as options.
    private Query _query;
}
