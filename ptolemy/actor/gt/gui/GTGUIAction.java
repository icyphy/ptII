/* An action that can be configured with a Ptera-based model transformation to
   be applied to the current model.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.gui;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.IgnoringAttribute;
import ptolemy.actor.gui.properties.GUIAction;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory.TransformationListener;

//////////////////////////////////////////////////////////////////////////
//// GTGUIAction

/**
 An action that can be configured with a Ptera-based model transformation to be
 applied to the current model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTGUIAction extends GUIAction {

    /** Construct an item with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a
     *   period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GTGUIAction(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** React to this item being selected. In this base class, if a source
     *  file is specified in the configuration of this item, e.g.:
     *  <pre>
     *    &lt;configure source="some_file.xml"&gt;
     *    &lt;/configure&gt;
     *  </pre>
     *  then the source is read and its contents are used as the moml text.
     *  The moml text can also be given directly:
     *  <pre>
     *    &lt;configure&gt;
     *      &lt;entity name="C" class="ptolemy.actor.lib.Const"&gt;
     *      &lt;/entity&gt;
     *    &lt;/configure&gt;
     *  </pre>
     *
     *  Depending on whether the parse parameter is true or false,
     *  the moml text may be parsed first or not. If it is parsed, the
     *  returned NamedObj is used to generate a new moml string to be
     *  applied to the model in the current tableau (the nearest tableau
     *  that contains this GUI property). If it is not parsed, then the moml
     *  text is directly applied to the model.
     *
     *  @param parse Whether the configure text should be parsed before applying
     *   to the current model.
     */
    @Override
    public void perform(boolean parse) {
        if (_momlText != null) {
            NamedObj originalModel = null;
            CompositeEntity model = null;
            try {
                originalModel = getModel();
                _processUnselectedObjects(originalModel, true);
                model = (CompositeEntity) GTTools.cleanupModel(originalModel);

                if (_parsedObject == null) {
                    if (_momlSource != null) {
                        URL url = _parser.fileNameToURL(_momlSource, null);
                        _parsedObject = _parser.parse(url, url);
                        _momlSource = null;
                    } else {
                        _parsedObject = _parser.parse(_momlText);
                    }
                    _parser.reset();
                }

                PteraModalModel ptera = (PteraModalModel) _parsedObject;
                BasicGraphFrame frame = (BasicGraphFrame) getFrame();
                TransformationListener listener = new TransformationListener(
                        ptera, model, frame);
                Manager manager = ptera.getManager();
                if (manager == null) {
                    Workspace workspace = ptera.workspace();
                    manager = new Manager(workspace, "manager");
                    ptera.setManager(manager);
                }
                manager.addExecutionListener(listener);
                try {
                    manager.execute();
                } catch (Throwable t) {
                    MessageHandler.error("Error while executing the "
                            + "transformation model.", t);
                } finally {
                    manager.removeExecutionListener(listener);
                }
            } catch (Exception e) {
                throw new InternalErrorException(e);
            } finally {
                if (originalModel != null) {
                    try {
                        _processUnselectedObjects(originalModel, false);
                    } catch (KernelException e) {
                        // Ignore.
                    }
                }
                if (model != null) {
                    try {
                        _processUnselectedObjects(model, false);
                    } catch (KernelException e) {
                        // Ignore.
                    }
                }
            }
        }
    }

    /** Mark the unselected objects in the given model to be ignored or not
     *  ignored.
     *
     *  @param model The model that contains the selected or unselected objects.
     *  @param ignore Whether the objects should be ignored.
     *  @exception NameDuplicationException If instances of IgnoringAttribute
     *   cannot be created for the objects.
     *  @exception IllegalActionException If IgnoringAttribute exists and have the
     *   same names.
     */
    protected void _processUnselectedObjects(NamedObj model, boolean ignore)
            throws NameDuplicationException, IllegalActionException {
        BasicGraphFrame frame = (BasicGraphFrame) getFrame();
        HashSet<NamedObj> selections;
        if (ignore) {
            selections = frame.getSelectionSet();
            if (selections.isEmpty()) {
                return;
            }
            frame.clearSelection();
        } else {
            selections = null;
        }
        Collection<NamedObj> children = GTTools.getChildren(model, true, true,
                true, true);
        for (NamedObj child : children) {
            if (selections == null || !selections.contains(child)) {
                if (ignore) {
                    new IgnoringAttribute(child, "_ignore");
                } else {
                    IgnoringAttribute attribute = (IgnoringAttribute) child
                            .getAttribute("_ignore");
                    if (attribute != null) {
                        attribute.setContainer(null);
                    }
                }
            }
        }
    }
}
