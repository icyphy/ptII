/* The node controller for Ptolemy objects.

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
package ptolemy.vergil.basic;

import java.awt.event.ActionEvent;
import java.net.URL;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.DocManager;
import ptolemy.vergil.actor.DocViewer;
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
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
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
            
            // If the object contains
            // an attribute named "_docAttribute" or if there
            // is a doc file for the object in the standard place,
            // then use the DocViewer class to display the documentation.
            // For backward compatibility, if neither of these is found,
            // then we open the Javadoc file, if it is found.
            if (target.getAttribute(DocManager.DOC_ATTRIBUTE_NAME) == null) {
                // No doc attribute. Try for a doc file.
                try {
                    String className = target.getClass().getName();
                    URL toRead = getClass().getClassLoader().getResource(
                            className.replace('.', '/') + "Doc.xml");
                    if (toRead == null) {
                        // No doc file either.
                        // For backward compatibility, open the
                        // Javadoc file, as before.
                        String docName = "doc.codeDoc." + className;
                        
                        try {
                            toRead = getClass().getClassLoader().getResource(
                                    docName.replace('.', '/') + ".html");
                            
                            if (toRead != null) {
                                if (_configuration != null) {
                                    _configuration.openModel(null, toRead, toRead
                                            .toExternalForm());
                                } else {
                                    MessageHandler.error("Cannot open documentation for "
                                            + className + " without a configuration.");
                                }
                                return;
                            } else {
                                MessageHandler.error("Cannot find documentation for "
                                        + className + "\nTry Running \"make\" in ptII/doc,"
                                        + "\nor installing the documentation component.");
                                return;
                            }
                        } catch (Exception ex) {
                            MessageHandler.error("Cannot find documentation for "
                                    + className + "\nTry Running \"make\" in ptII/doc."
                                    + "\nor installing the documentation component.", ex);
                        }
                    }
                } catch (Exception ex) {
                    // Ignore and open using DocViewer.
                }
            }
            
            // FIXME: Need to create a tableau.
            DocViewer viewer = new DocViewer(target, _configuration);
            viewer.pack();
            viewer.show();
        }
    }
}
