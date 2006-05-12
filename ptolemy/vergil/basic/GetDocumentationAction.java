/* An action for getting documentation.

 Copyright (c) 2006 The Regents of the University of California.
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
import java.util.List;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.DocBuilder;
import ptolemy.vergil.actor.DocBuilderEffigy;
import ptolemy.vergil.actor.DocBuilderGUI;
import ptolemy.vergil.actor.DocBuilderTableau;
import ptolemy.vergil.actor.DocEffigy;
import ptolemy.vergil.actor.DocTableau;
import ptolemy.vergil.toolbox.FigureAction;

//////////////////////////////////////////////////////////////////////////
//// GetDocumentationAction

/** This is an action that accesses the documentation for a Ptolemy
 object associated with a figure.  Note that this base class does
 not put this action in a menu, since some derived classes will
 not want it.  But by having it here, it is available to all
 derived classes.

 This class provides an action for removing instance-specific documentation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class GetDocumentationAction extends FigureAction {

    /** Construct an instance of this action. */
    public GetDocumentationAction() {
        super("Get Documentation");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the action by opening documentation for the target.
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if (_configuration == null) {
            MessageHandler
                    .error("Cannot get documentation without a configuration.");
        }

        NamedObj target = getTarget();
        if (target == null) {
            // Ignore and return.
            return;
        }

        // If the object contains
        // an attribute named of class DocAttribute or if there
        // is a doc file for the object in the standard place,
        // then use the DocViewer class to display the documentation.
        // For backward compatibility, if neither of these is found,
        // then we open the Javadoc file, if it is found.
        List docAttributes = target.attributeList(DocAttribute.class);
        // Get the last doc attribute.
        if (docAttributes.size() == 0) {
            // No doc attribute. Try for a doc file.
            String className = target.getClass().getName();
            try {
                String docName = "doc/codeDoc/" + className.replace('.', '/') + ".xml";
                String docClassName = "doc.codeDoc." + className;
                URL toRead = getClass().getClassLoader().getResource(docName);
                if (toRead == null) {
                    // No doc file either.
                    // For backward compatibility, open the
                    // Javadoc file, as before.
                    toRead = getClass().getClassLoader().getResource(
                            docName.replace('.', '/') + ".html");
                }
                if (toRead != null) {
                    _configuration.openModel(null, toRead, toRead
                            .toExternalForm());
                } else {
                    throw new Exception("Could not get " + docClassName
                            + " or " + docName + " as a resource");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                // Need to create an effigy and tableau.
                Effigy context = Configuration.findEffigy(target);
                if (context == null) {
                    context = Configuration.findEffigy(target.getContainer());
                    if (context == null) {
                        MessageHandler.error("Cannot find an effigy for "
                                + target.getFullName());
                    }
                }
                ComponentEntity effigy = context.getEntity("DocBuilderEffigy");
                if (effigy == null) {
                    try {
                        effigy = new DocBuilderEffigy(context, "DocBuilderEffigy");
                    } catch (KernelException exception) {
                        throw new InternalErrorException(exception);
                    }
                }
                if (!(effigy instanceof DocBuilderEffigy)) {
                    MessageHandler.error("Found an effigy named DocBuilderEffigy that "
                        + "is not an instance of DocBuilderEffigy!");
                }
                //((DocEffigy) effigy).setDocAttribute(docAttribute);
                ComponentEntity tableau = ((Effigy) effigy).getEntity("DocBuilderTableau");
                if (tableau == null) {
                    try {
                        tableau = new DocBuilderTableau((DocBuilderEffigy) effigy, "DocBuilderTableau");
                    ((DocBuilderTableau) tableau).setTitle("Documentation for "
                            + target.getFullName());
                    } catch (KernelException exception) {
                        throw new InternalErrorException(exception);
                    }
                }
                if (!(tableau instanceof DocBuilderTableau)) {
                MessageHandler.error("Found a tableau named DocBuilderTableau that "
                        + "is not an instance of DocBuilderTableau!");
                }
                // FIXME: Tell the user what to do here.
                ((DocBuilderTableau) tableau).show();
                //DocBuilderGUI docBuilderGUI = new DocBuilderGUI(
                //        new DocBuilder(target, "myDocBuilder"), (Tableau)null);

                //docBuilderGUI.show();
                } catch (Throwable throwable) {
                    MessageHandler.error("Cannot find documentation for "
                        + className + "\nTry Running \"make\" in ptII/doc."
                        + "\nor installing the documentation component.", throwable);
                }
            }
        } else {
            // Have a doc attribute. Use that.
            DocAttribute docAttribute = (DocAttribute) docAttributes
                    .get(docAttributes.size() - 1);
            // Need to create an effigy and tableau.
            Effigy context = Configuration.findEffigy(target);
            if (context == null) {
                context = Configuration.findEffigy(target.getContainer());
                if (context == null) {
                    MessageHandler.error("Cannot find an effigy for "
                            + target.getFullName());
                }
            }
            ComponentEntity effigy = context.getEntity("DocEffigy");
            if (effigy == null) {
                try {
                    effigy = new DocEffigy(context, "DocEffigy");
                } catch (KernelException exception) {
                    throw new InternalErrorException(exception);
                }
            }
            if (!(effigy instanceof DocEffigy)) {
                MessageHandler.error("Found an effigy named DocEffigy that "
                        + "is not an instance of DocEffigy!");
            }
            ((DocEffigy) effigy).setDocAttribute(docAttribute);
            ComponentEntity tableau = ((Effigy) effigy).getEntity("DocTableau");
            if (tableau == null) {
                try {
                    tableau = new DocTableau((DocEffigy) effigy, "DocTableau");
                    ((DocTableau) tableau).setTitle("Documentation for "
                            + target.getFullName());
                } catch (KernelException exception) {
                    throw new InternalErrorException(exception);
                }
            }
            if (!(tableau instanceof DocTableau)) {
                MessageHandler.error("Found a tableau named DocTableau that "
                        + "is not an instance of DocTableau!");
            }
            ((DocTableau) tableau).show();
        }
    }

    /** Set the configuration.  This is used
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
}
