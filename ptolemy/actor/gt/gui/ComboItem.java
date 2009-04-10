/*

 Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.actor.gui.properties.ComboBox;
import ptolemy.actor.gui.properties.ComboBox.Item;
import ptolemy.data.BooleanToken;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory.TransformationListener;

//////////////////////////////////////////////////////////////////////////
//// ComboItem

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ComboItem extends Item {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public ComboItem(ComboBox container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        parse.setToken(BooleanToken.TRUE);
        parse.setVisibility(Settable.NONE);
    }

    public void select() {
        if (_momlText != null) {
            NamedObj originalModel = null;
            CompositeEntity model = null;
            try {
                originalModel = _getModel();
                _processUnselectedObjects(originalModel, true);
                model = (CompositeEntity) GTTools.cleanupModel(originalModel);

                if (_parsedObject == null) {
                    if (_momlSource != null) {
                        URL url = _parser.fileNameToURL(_momlSource,
                                null);
                        _parsedObject = _parser.parse(url, url);
                        _momlSource = null;
                    } else {
                        _parsedObject = _parser.parse(_momlText);
                    }
                    _parser.reset();
                }

                PteraModalModel ptera = (PteraModalModel) _parsedObject;
                BasicGraphFrame frame = (BasicGraphFrame) _getFrame();
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
                    MessageHandler.error("Error while executing the " +
                            "transformation model.", t);
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

    protected void _processUnselectedObjects(NamedObj model, boolean ignore)
            throws NameDuplicationException, IllegalActionException {
        BasicGraphFrame frame = (BasicGraphFrame) _getFrame();
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
                    IgnoringAttribute attribute =
                        (IgnoringAttribute) child.getAttribute("_ignore");
                    if (attribute != null) {
                        attribute.setContainer(null);
                    }
                }
            }
        }
    }
}
