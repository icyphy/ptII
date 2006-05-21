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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.DocApplicationSpecializer;
import ptolemy.vergil.actor.DocBuilderEffigy;
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

        // We handle the applicationName specially so that we open
        // only the docs for the app we are running.
        try {
            StringAttribute applicationNameAttribute =
                (StringAttribute) _configuration
                .getAttribute("_applicationName", StringAttribute.class);

            if (applicationNameAttribute != null) {
                _applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Throwable throwable) {
            // Ignore and use the default applicationName: "",
            // which means we look in doc.codeDoc.
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
            Effigy context = Configuration.findEffigy(target);
            if (context == null) {
                context = Configuration.findEffigy(target.getContainer());
                if (context == null) {
                    MessageHandler.error("Cannot find an effigy for "
                            + target.getFullName());
                }
            }
            getDocumentation(_configuration, _applicationName, className, context);
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

    /** Get the documentation for a particular class.  
     *  <p>If the configuration has a parameter _docApplicationSpecializer
     *  and that parameter names a class that that implements the
     *  DocApplicationSpecializer interface, then we call
     *  docClassNameToURL().
     *
     *  <p>If the documentation is not found, pop up a dialog and ask the
     *  user if they would like to build the documentation, use the
     *  website documentation or cancel.  The location of the website
     *  documentation is set by the _remoteDocumentationBase attribute
     *  in the configuration.  That attribute, if present, should be a
     *  parameter that whose value is a string that represents the URL
     *  where the documentation may be found.  If the
     *  _remoteDocumentationAttribution attribute is not set, then the
     *  location of the website documentation defaults to
     *  <code>http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII/<i>Major.Version</i>,
     *  where <code><i>Major.Version</i> is the value returned by
     *  {@link ptolemy.kernel.attributes.VersionAttribute#majorCurrentVersion()}.
     *
     *  @param configuration The configuration.
     *  @param applicationName The name of the application, usually
     *  from the _applicationName StringAttribute in
     *  configuration.xml.  If the value is the empty string, then use
     *  the default documentation in doc/codeDoc.
     *  @param className The dot separated fully qualified name of the class.
     *  @param context The context.
     */
    public static void getDocumentation(Configuration configuration,
            String applicationName, String className, Effigy context) {

        try {

            URL toRead = null;

            // If the configuration has a parameter _docApplicationSpecializer
            // and that parameter names a class that that implements the
            // DocApplicationSpecializer interface, then we call
            // docClassNameToURL(). 
            
            Parameter docApplicationSpecializerParameter =
                (Parameter) configuration
                .getAttribute("_docApplicationSpecializer",
                        Parameter.class);
            if (docApplicationSpecializerParameter != null) {
                String docApplicationSpecializerClassName = 
                    docApplicationSpecializerParameter.getExpression();

                try {
                    Class docApplicationSpecializerClass = Class
                        .forName(docApplicationSpecializerClassName);
                    DocApplicationSpecializer docApplicationSpecializer = (DocApplicationSpecializer) docApplicationSpecializerClass.newInstance();
                    toRead = docApplicationSpecializer.docClassNameToURL(className);
                } catch (Throwable throwable) {
                    throw new Exception("Failed to call doc application initializer "
                        + "class \"" + docApplicationSpecializerClassName
                        + "\" on class \"" + className + "\".");
                }
            }

            // We search first on the local machine and then ask
            // the user and then possibly look on the remote machine.
            // So, we define the strings in an array for ease of reuse.

            String docNames [] = {
                "doc/codeDoc"
                + (applicationName.equals("") ?
                        "/" : applicationName + "/doc/codeDoc/")
                + className.replace('.', '/') + ".xml",

                "doc/codeDoc/" + className.replace('.', '/')
                + ".xml",

                "doc/codeDoc"
                + (applicationName.equals("") ?
                        "/" : applicationName + "/doc/codeDoc/")
                + className.replace('.', '/') + ".html",

                "doc/codeDoc/" + className.replace('.', '/')
                + ".html"
            };

            // We could use these to print out a list of places
            // were we searched, but we don't right now.
            String docClassNames [] = {
                "doc.codeDoc." 
                + (applicationName.equals("") ?
                        "." : applicationName + ".doc.codeDoc.")
                + className,

                "doc.codeDoc." + className,

                "doc.codeDoc." 
                + (applicationName.equals("") ?
                        "." : applicationName + ".doc.codeDoc.")
                + className,

                "doc.codeDoc." + className

            };

            // We look for the documentation relative to this classLoader.n
            ClassLoader referenceClassLoader = Class.forName("ptolemy.vergil.basic.GetDocumentationAction").getClassLoader();

            // Rather than using a deeply nested set of if/else's, we
            // just keep checking toRead == null.

            // If applicationName is not "", then look in
            // doc/codeDoc_applicationName/doc/codeDoc.
            if (toRead == null) {
                toRead = referenceClassLoader.getResource(docNames[0]);
            }

            if (toRead == null
                    && applicationName != null
                    && !applicationName.equals("")) {
                // applicationName was set, try looking in the
                // documentation for vergil.
                toRead = referenceClassLoader.getResource(docNames[1]);
            }

            // Deterimine the maximum index in docNames[] that we will
            // search if we search on the remote host.  If the class
            // we are looking for is assignable from NamedObj and
            // applicationName is non-null, then the value is 3, which
            // means search all names.  If the class is assignable
            // from NamedObj and the applicationName is null or "",
            // then the value is 2.  If the class is not assignable
            // from NamedObj . . .  otherwise the value is 0, which
            // means try the first entry.

            int maximumDocNamesIndex = 0;  // Used for remote searches.
            if (applicationName != null && !applicationName.equals("")) {
                maximumDocNamesIndex = 1; 
            }
            if (toRead == null) {
                // If the class does not extend NamedObj, try to open
                // the javadoc .html
                Class targetClass = Class.forName(className);
                if (!_namedObjClass.isAssignableFrom(targetClass)) {

                    if (applicationName != null
                            && !applicationName.equals("")) {
                        maximumDocNamesIndex = 3;  // Used for remote searches.
                    } else {
                        maximumDocNamesIndex = 2;  
                    }

                    // Look in the Application specific codeDoc directory.
                    toRead = referenceClassLoader.getResource(docNames[2]);
                    if (toRead == null) {
                        // Try looking in the documentation for vergil.
                        toRead = referenceClassLoader.getResource(docNames[3]);
                    }
                }
            }

            if (toRead == null && _remoteDocumentationURLBase != null) {
                // Try searching on a remote host.

                // Loop through each docNames[i] and try to open
                // a stream.  Stop if once we open a stream.
                for (int i = 0; i <= maximumDocNamesIndex; i++) {
                    if (i > 0 && docNames[i].equals(docNames[i-1])) {
                        // applicationName is not set, docNames[0] and [1]
                        // are the same, so skip.
                        continue;
                    }
                    toRead = new URL(_remoteDocumentationURLBase
                            + docNames[i]);
                    if (toRead != null) {
                        InputStream toReadStream = null;
                        try {
                            toReadStream = toRead.openStream();
                        } catch (IOException ex) {
                            toRead = null;
                        } finally {
                            if (toReadStream != null) {
                                try {
                                    toReadStream.close();
                                } catch (IOException ex2) {
                                    // Ignore.
                                }
                            }
                        }
                        if (toRead != null) {
                            break;
                        }
                    }
                }
            }

            if (toRead != null) {
                _lastClassName = null;
                configuration.openModel(null, toRead, toRead
                        .toExternalForm());
            } else {
                throw new Exception("Could not get " + docClassNames[0]
                        + " or " + docNames[0] + " as a resource."
                        + ( _remoteDocumentationURLBase != null ?
                                " Also tried looking on \""
                                + _remoteDocumentationURLBase + "\"."
                                : ""));
            }
        } catch (Exception ex) {
            // Try to open the DocBuilderGUI
            try {
                // Pop up a query an prompt the user
                String message =  "The documentation was not found.\n"
                    + (_lastClassName != null &&
                            _remoteDocumentationURLBase != null
                            ? "We looked in \"" 
                            + _remoteDocumentationURLBase
                            + "\" but did not find anything.\n"
                            : "")
                    + "You may\n"
                    + "1) Build the documentation, which requires "
                    + "configure and make, or\n"
                    + "2) Use the documentation from the website, or\n"
                    + "3) Cancel";
                Object[] options = {"Build", "Use Website", "Cancel"};
                int selected = JOptionPane.showOptionDialog(null,
                        message,
                        "Choose Documentation Source",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                switch (selected) {
                case 2 :
                    // Cancel
                    return;
                case 1 :
                    // Use Website
                    Parameter remoteDocumentationURLBaseParameter =
                        (Parameter) configuration
                        .getAttribute("_remoteDocumentationURLBase",
                        Parameter.class);
                    if (remoteDocumentationURLBaseParameter != null) {
                        _remoteDocumentationURLBase = 
                            remoteDocumentationURLBaseParameter.getExpression();
                    } else {
                        _remoteDocumentationURLBase = "http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII"
                            + VersionAttribute.majorCurrentVersion()
                            + "/ptII/";
                    }
                    _lastClassName = className;
                    getDocumentation(configuration,
                            applicationName, className, context);
                    break;
                case 0:
                    // Build
                    // Need to create an effigy and tableau.
                    ComponentEntity effigy = context.getEntity("DocBuilderEffigy");
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
                    ComponentEntity tableau = ((Effigy) effigy).getEntity("DocBuilderTableau");
                    if (tableau == null) {
                        try {
                            tableau = new DocBuilderTableau(
                                    (DocBuilderEffigy) effigy,
                                    "DocBuilderTableau");
                            ((DocBuilderTableau) tableau).setTitle(
                                    "Documentation for "
                                    + className);
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
                    //break;;
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The configuration. */
    protected Configuration _configuration;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the application, usually from the _applicationName
     *  StringAttribute in configuration.xml.
     *  If the value is the empty string, then use the default
     *  documentation in doc/codeDoc.
     */
    private String _applicationName = "";

    /** The name of the last class for which we looked.  If the user
     *  looks again for the same class and gets an error and
     *  _remoteDocumentation is set, we print a little more information
     */
    private static String _lastClassName = null;

    /** The remote URL were we look for the documentation.
     *  Set from the _remoteDocumentationURLBase parameter in
     *  the configuration.
     */   
    private static String _remoteDocumentationURLBase = null;

    /** The NamedObj class, used to check to see if the class we are
     *  looking for is assignable from NamedObj.  If it is not, we look
     *  for the codeDoc .html file.
     */
    private static Class _namedObjClass;
    static {
        try {
            _namedObjClass = Class.forName("ptolemy.kernel.util.NamedObj");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
