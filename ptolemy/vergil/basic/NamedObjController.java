/* The node controller for Ptolemy objects.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.basic;

import java.awt.event.ActionEvent;
import java.net.URL;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.toolbox.FigureAction;
import diva.graph.GraphController;

//////////////////////////////////////////////////////////////////////////
//// NamedObjController
/**
This class extends LocatableNodeController with an association
with a configuration. The configuration is central to a Ptolemy GUI,
and is used by derived classes to perform various functions such as
opening models or their documentation. The class also contains an
inner class the specifically supports accessing the documentation for
a Ptolemy II object.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class NamedObjController extends LocatableNodeController {

    /** Create a node controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public NamedObjController(GraphController controller) {
        super(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is used in derived classes to
     *  to open files (such as documentation).  The configuration is
     *  is important because it keeps track of which files are already
     *  open and ensures that there is only one editor operating on the
     *  file at any one time.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The configuration. */
    protected Configuration _configuration;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Action to edit a custom icon.
     */
    protected class EditIconAction extends FigureAction {

        public EditIconAction() {
            super("Edit Custom Icon");
            // For some inexplicable reason, the I key doesn't work here.
            // putValue(GUIUtilities.ACCELERATOR_KEY,
            //       KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Process the edit icon command.
         *  @param e The event.
         */
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot edit icon without a configuration.");
                return;
            }

            // Determine which entity was selected for the action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            
            try {
                // FIXME: This should be done as a change request!
                EditorIcon icon = (EditorIcon)object
                        .getAttribute("_icon", EditorIcon.class);
                if (icon == null) {
                    icon = new EditorIcon(object, "_icon");
                } else if (icon instanceof XMLIcon) {
                    // There is an icon currently that is not custom.
                    // Without trashing the _iconDescription, we can remove
                    // this icon and replace it.
                    icon.setContainer(null);
                    icon = new EditorIcon(object, "_icon");
                }
                _configuration.openModel(icon);
            } catch (Exception ex) {
                MessageHandler.error("Custom Icon failed.", ex);
            }
        }
    }
    
    /** This is an action that accesses the documentation for a Ptolemy
     *  object associated with a figure.  Note that this base class does
     *  not put this action in a menu, since some derived classes will
     *  not want it.  But by having it here, it is available to all
     *  derived classes.
     */
    protected class GetDocumentationAction extends FigureAction {
        public GetDocumentationAction() {
            super("Get Documentation");
        }
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            NamedObj target = getTarget();
            String className = target.getClass().getName();
            String docName = "doc.codeDoc." + className;
            try {
                URL toRead = getClass().getClassLoader().getResource(
                        docName.replace('.', '/') + ".html");
                if (toRead != null) {
                    if (_configuration != null) {
                        _configuration.openModel(null,
                                toRead, toRead.toExternalForm());
                    } else {
                        MessageHandler.error(
                                "Cannot open documentation for "
                                + className
                                + " without a configuration.");
                    }
                } else {
                    MessageHandler.error("Cannot find documentation for "
                            + className
                            + "\nTry Running \"make\" in ptII/doc,"
                            + "\nor installing the documentation component.");
                }
            } catch (Exception ex) {
                MessageHandler.error("Cannot find documentation for "
                        + className
                        + "\nTry Running \"make\" in ptII/doc."
                        + "\nor installing the documentation component.", ex);
            }
        }
    }
    
    /** Action to remove a custom icon.
     */
    protected class RemoveIconAction extends FigureAction {

        public RemoveIconAction() {
            super("Remove Custom Icon");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Process the remove icon command.
         *  @param e The event.
         */
        public void actionPerformed(ActionEvent e) {

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            
            try {
                EditorIcon icon = (EditorIcon)object
                        .getAttribute("_icon", EditorIcon.class);
                // An XMLIcon is not a custom icon, so don't remove it.
                if (!(icon instanceof XMLIcon)) {
                    NamedObj context = MoMLChangeRequest.getDeferredToParent(object);
                    if (context == null) {
                        context = object;
                    }
                    String moml;
                    if (context != object) {
                        moml = "<entity name=\""
                                + object.getName(context)
                                + "\"><deleteProperty name=\""
                                + icon.getName()
                                + "\"/></entity>";
                    } else {
                        moml = "<deleteProperty name=\""
                                + icon.getName()
                                + "\"/>";
                    }
                    MoMLChangeRequest request
                            = new MoMLChangeRequest(this, context, moml);
                    context.requestChange(request);
                }
            } catch (Exception ex) {
                MessageHandler.error("Remove custom Icon failed.", ex);
            }
        }
    }
}
