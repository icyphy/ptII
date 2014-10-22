/* An action for getting documentation.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.DocApplicationSpecializer;
import ptolemy.vergil.actor.DocBuilderEffigy;
import ptolemy.vergil.actor.DocBuilderTableau;
import ptolemy.vergil.actor.DocEffigy;
import ptolemy.vergil.actor.DocManager;
import ptolemy.vergil.actor.DocTableau;
import ptolemy.vergil.toolbox.FigureAction;

///////////////////////////////////////////////////////////////////
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
@SuppressWarnings("serial")
public class GetDocumentationAction extends FigureAction {

    /** Construct an instance and give a preference for whether the
     * KeplerDocumentationAttribute or the docAttribute should be displayed
     * if both exist.
     * @param docPreference 0 for docAttribute, 1 for
     * KeplerDocumentationAttribute
     */
    public GetDocumentationAction(int docPreference) {
        super("Get Documentation");
        _docPreference = docPreference;
    }

    /** Construct an instance of this action. */
    public GetDocumentationAction() {
        super("Get Documentation");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the action by opening documentation for the target.
     *  In the default situation, the documentation is in doc.codeDoc.
     *  However, if we have a custom application like HyVisual,
     *  VisualSense or Viptos, then we create the docs in
     *  doc.codeDoc<i>ApplicationName</i>.doc.codeDoc.  However, this
     *  directory gets jar up and shipped with these apps when we ship
     *  windows installers and the docs are found at doc.codeDoc
     *  again.  So, if _applicationName is set, we look in
     *  doc.codeDoc<i>_applicationName</i>.doc.codeDoc.  If that is
     *  not found, we look in doc.codeDoc.  If that is not found,
     *  we bring up {@link ptolemy.vergil.actor.DocBuilderGUI}.
     */
    @Override
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

        showDocumentation(target);
    }

    /**
     * Show the documentation for a NamedObj.  This does the same
     * thing as the actionPerformed but without the action handler
     * @param target The NamedObj that will have its documentation shown.
     */
    public void showDocumentation(NamedObj target) {
        if (_configuration == null) {
            MessageHandler
                    .error("Cannot get documentation without a configuration.");
        }

        // If the object contains
        // an attribute named of class DocAttribute or if there
        // is a doc file for the object in the standard place,
        // then use the DocViewer class to display the documentation.
        // For backward compatibility, if neither of these is found,
        // then we open the Javadoc file, if it is found.
        List docAttributes = target.attributeList(DocAttribute.class);
        //check for the KeplerDocumentation attribute
        KeplerDocumentationAttribute keplerDocumentationAttribute = (KeplerDocumentationAttribute) target
                .getAttribute("KeplerDocumentation");
        int docAttributeSize = docAttributes.size();

        if (docAttributes.size() != 0 && keplerDocumentationAttribute != null) {
            //if there is both a docAttribute and a KeplerDocumentationAttribute
            //use the preference passed in to the constructor
            if (_docPreference == 0) {
                keplerDocumentationAttribute = null;
            } else if (_docPreference == 1) {
                docAttributeSize = 0;
            }
        }

        if (keplerDocumentationAttribute != null) {
            //use the KeplerDocumentationAttribute
            DocAttribute docAttribute = keplerDocumentationAttribute
                    .getDocAttribute(target);
            if (docAttribute != null) {
                _showDocAttributeTableau(docAttribute, target);
            } else {
                throw new InternalErrorException(
                        "Error building Kepler documentation");
            }
        } else if (docAttributeSize != 0) {
            // Have a doc attribute. Use that.
            DocAttribute docAttribute = (DocAttribute) docAttributes
                    .get(docAttributes.size() - 1);
            _showDocAttributeTableau(docAttribute, target);
        } else {
            // No doc attribute. Try for a doc file.
            String className = target.getClass().getName();
            Effigy context = Configuration.findEffigy(target);
            NamedObj container = target.getContainer();
            while (context == null && container != null) {
                context = Configuration.findEffigy(container);
                container = container.getContainer();
            }
            /* This test is pointless, since it shows the doc anyway.
            if (context == null) {
                MessageHandler.error("Cannot find an effigy for "
                        + target.getFullName());
            }
             */
            getDocumentation(_configuration, className, context);
        }
    }

    /** Get the documentation for a particular class.
     *  <p>If the configuration has a parameter _docApplicationSpecializer
     *  and that parameter names a class that that implements the
     *  DocApplicationSpecializer interface, then we call
     *  docClassNameToURL().
     *
     *  <p>If the documentation is not found, pop up a dialog and ask the
     *  user if they would like to build the documentation, use the
     *  website documentation or cancel.  The location of the website
     *  documentation is set by the _remoteDocumentationURLBase attribute
     *  in the configuration.  That attribute, if present, should be a
     *  parameter that whose value is a string that represents the URL
     *  where the documentation may be found.  If the
     *  _remoteDocumentationURLBase attribute is not set, then the
     *  location of the website documentation defaults to
     *  <code>http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII/<i>Major.Version</i></code>,
     *  where <code><i>Major.Version</i></code> is the value returned by
     *  {@link ptolemy.kernel.attributes.VersionAttribute#majorCurrentVersion()}.
     *
     *  @param configuration The configuration.
     *  @param className The dot separated fully qualified name of the class.
     *  @param context The context.
     */
    public static void getDocumentation(Configuration configuration,
            String className, Effigy context) {
        try {

            // Look for the PtDoc .xml file or the javadoc.
            // Don't look for the source or the index.
            URL toRead = DocManager.docClassNameToURL(configuration, className,
                    true, true, false, false);
            if (toRead != null) {
                _lastClassName = null;
                if (toRead.toExternalForm().endsWith(".html")) {
                    // Sadly, Javadoc from Java 1.7 cannot be
                    // displayed using a JEditorPane, so we open
                    // javadoc in an external browser.  To test this
                    // out, see
                    // http://docs.oracle.com/javase/tutorial/uiswing/components/editorpane.html#editorpane
                    // and modify the example so that it tries to view
                    // the Javadoc for Object.
                    toRead = new URL(toRead.toExternalForm() + "#in_browser");
                }
                // Opening a remote URL can be slow, so we report to the status bar.
                BasicGraphFrame basicGraphFrame = BasicGraphFrame
                        .getBasicGraphFrame(context);

                if (basicGraphFrame != null) {
                    basicGraphFrame.report("Opening " + toRead);
                }
                configuration.openModel(null, toRead, toRead.toExternalForm());
                if (basicGraphFrame != null) {
                    basicGraphFrame.report("Opened documentation for "
                            + className);
                }
            } else {
                Parameter docApplicationSpecializerParameter = (Parameter) configuration
                        .getAttribute("_docApplicationSpecializer",
                                Parameter.class);
                if (docApplicationSpecializerParameter != null) {
                    //if there is a docApplicationSpecializer, let it handle the
                    //error instead of just throwing the exception
                    String docApplicationSpecializerClassName = docApplicationSpecializerParameter
                            .getExpression();
                    Class docApplicationSpecializerClass = Class
                            .forName(docApplicationSpecializerClassName);
                    final DocApplicationSpecializer docApplicationSpecializer = (DocApplicationSpecializer) docApplicationSpecializerClass
                            .newInstance();
                    docApplicationSpecializer.handleDocumentationNotFound(
                            className, context);
                } else {
                    throw new Exception(
                            "Could not get find documentation for "
                                    + className
                                    + "."
                                    + (DocManager
                                            .getRemoteDocumentationURLBase() != null ? " Also tried looking on \""
                                            + DocManager
                                                    .getRemoteDocumentationURLBase()
                                            + "\"."
                                            : ""));
                }
            }
        } catch (Exception ex) {
            // Try to open the DocBuilderGUI
            try {
                Parameter remoteDocumentationURLBaseParameter = (Parameter) configuration
                        .getAttribute("_remoteDocumentationURLBase",
                                Parameter.class);
                String tentativeRemoteDocumentationURLBase = null;
                if (remoteDocumentationURLBaseParameter != null) {
                    tentativeRemoteDocumentationURLBase = remoteDocumentationURLBaseParameter
                            .getExpression();
                } else {
                    if (VersionAttribute.CURRENT_VERSION.getExpression()
                            .indexOf(".devel") != -1) {
                        tentativeRemoteDocumentationURLBase = "http://chess.eecs.berkeley.edu/ptexternal/src/ptII/";
                    } else {
                        tentativeRemoteDocumentationURLBase = "http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII"
                                + VersionAttribute.majorCurrentVersion()
                                + "/ptII/";
                    }
                }
                // Pop up a query an prompt the user
                String message = "The documentation for " + className + " was not found.\n"
                        + (_lastClassName != null
                                && DocManager.getRemoteDocumentationURLBase() != null ? " We looked in \""
                                + DocManager.getRemoteDocumentationURLBase()
                                + "\" but did not find anything.\n"
                                : "") + "You may\n"
                        + "1) Build the documentation, which requires "
                        + "configure and make, or\n"
                        + "2) Use the documentation from the website at \""
                        + tentativeRemoteDocumentationURLBase + "\" or\n"
                        + "3) Cancel";
                Object[] options = { "Build", "Use Website", "Cancel" };
                int selected = JOptionPane.showOptionDialog(null, message,
                        "Choose Documentation Source",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                switch (selected) {
                case 2:
                    // Cancel
                    return;
                case 1:
                    // Use Website
                    DocManager
                            .setRemoteDocumentationURLBase(tentativeRemoteDocumentationURLBase);
                    _lastClassName = className;
                    getDocumentation(configuration, className, context);
                    break;
                case 0:
                    // Build
                    // Need to create an effigy and tableau.
                    ComponentEntity effigy = context
                            .getEntity("DocBuilderEffigy");
                    if (effigy == null) {
                        try {
                            effigy = new DocBuilderEffigy(context,
                                    "DocBuilderEffigy");
                        } catch (KernelException exception) {
                            throw new InternalErrorException(exception);
                        }
                    }
                    if (!(effigy instanceof DocBuilderEffigy)) {
                        MessageHandler.error("Found an effigy named "
                                + "DocBuilderEffigy that "
                                + "is not an instance of DocBuilderEffigy!");
                    }
                    //((DocEffigy) effigy).setDocAttribute(docAttribute);
                    ComponentEntity tableau = ((Effigy) effigy)
                            .getEntity("DocBuilderTableau");
                    if (tableau == null) {
                        try {
                            tableau = new DocBuilderTableau(
                                    (DocBuilderEffigy) effigy,
                                    "DocBuilderTableau");
                            ((DocBuilderTableau) tableau)
                                    .setTitle("Documentation for " + className);
                        } catch (KernelException exception) {
                            throw new InternalErrorException(exception);
                        }
                    }
                    if (!(tableau instanceof DocBuilderTableau)) {
                        MessageHandler.error("Found a tableau named "
                                + "DocBuilderTableau that "
                                + "is not an instance of DocBuilderTableau!");
                    }
                    // FIXME: Tell the user what to do here.
                    ((DocBuilderTableau) tableau).show();
                    break;
                default:
                    throw new InternalErrorException("Unknown return value \""
                            + selected
                            + "\" from Choose Documentation Source window.");
                    //break;
                }
            } catch (Throwable throwable) {
                MessageHandler.error("Cannot find documentation for "
                        + className + "\nTry Running \"make\" in ptII/doc."
                        + "\nor installing the documentation component.",
                        throwable);
            }
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

    /** Set the effigy to be used if the effigy is not evident from the
     *  model being edited.  This is used if you are showing the documentation
     *  from code that is not in a model.
     *  @param effigy the effigy to set.
     */
    public void setEffigy(Effigy effigy) {
        _effigy = effigy;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The configuration. */
    protected Configuration _configuration;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Allow optional use of multiple documentation windows when the
     * _multipleDocumentationAllowed attribute is found in the
     * Configuration.
     */
    private static boolean _isMultipleDocumentationAllowed() {
        // FIXME: This is necessary for Kepler, but not for Ptolemy?
        // Why?
        boolean retVal = false;
        List configsList = Configuration.configurations();
        Configuration config = null;

        for (Iterator it = configsList.iterator(); it.hasNext();) {
            config = (Configuration) it.next();
            if (config != null) {
                break;
            }
        }
        if (config == null) {
            throw new KernelRuntimeException("Could not find "
                    + "configuration, list of configurations was "
                    + configsList.size() + " elements, all were null.");
        }
        // Look up the attribute (if it exists)
        StringAttribute multipleDocumentationAllowed = (StringAttribute) config
                .getAttribute("_multipleDocumentationAllowed");
        if (multipleDocumentationAllowed != null) {
            retVal = Boolean.parseBoolean(multipleDocumentationAllowed
                    .getExpression());
        }
        return retVal;
    }

    /**
     * Find and show the tableau for a given DocAttribute.
     * @param docAttribute the attribute to show
     * @param target the target of the documentation viewing
     */
    private void _showDocAttributeTableau(DocAttribute docAttribute,
            NamedObj target) {
        // Need to create an effigy and tableau.
        ComponentEntity effigy = null;
        Effigy context = Configuration.findEffigy(target);
        if (_effigy == null) {
            NamedObj container = target.getContainer();
            while (container != null && context == null) {
                context = Configuration.findEffigy(container);
                container = container.getContainer();
            }
            if (context == null) {
                MessageHandler.error("Cannot find an effigy for "
                        + target.getFullName());
                return;
            }
            effigy = context.getEntity("DocEffigy");
        } else {
            effigy = _effigy;
        }

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
        } else {
            if (_isMultipleDocumentationAllowed() ||
                            // For some reason, the frame might be null.
                            (tableau instanceof Tableau) && ((Tableau)tableau).getFrame() == null) {
                try {
                    // FIXME: This is necessary for Kepler, but
                    // not for Ptolemy?  Why?

                    // Create a new tableau with a unique name
                    tableau = new DocTableau((DocEffigy) effigy,
                            effigy.uniqueName("DocTableau"));
                    ((DocTableau) tableau).setTitle("Documentation for "
                            + target.getFullName());
                } catch (KernelException exception) {
                    MessageHandler.error("Failed to display documentation for "
                            + "\" " + target.getFullName() + "\".", exception);
                }
            }
        }
        if (!(tableau instanceof DocTableau)) {
            MessageHandler.error("Found a tableau named DocTableau that "
                    + "is not an instance of DocTableau!");
        }
        ((DocTableau) tableau).show();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Defines a preference for whether to display kepler documentation or
     * ptolemy documentation.  This can be set in the constructor and it
     * default to ptolemy.  0 is ptolemy, 1 is kepler.
     */
    private int _docPreference = 0;

    /**
     * Defines the effigy to use if the effigy is not apparent from the model
     */
    private Effigy _effigy = null;

    /** The name of the last class for which we looked.  If the user
     *  looks again for the same class and gets an error and
     *  remoteDocumentationURLBase is set, we print a little more information.
     */
    private static String _lastClassName = null;
}
