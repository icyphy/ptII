/* An component accessor that consists of an interface and a script.

   Copyright (c) 2015-2017 The Regents of the University of California.
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
package org.terraswarm.accessor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ptolemy.actor.lib.jjs.JavaScript;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.Actionable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.CancelException;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StreamExec;
import ptolemy.util.StringBufferExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JSAccessor

/**
   An component accessor that consists of an interface and a script.

   <p>The "<a href="#VisionOfSwarmLets">Vision of Swarmlets</a>" paper
   defines three types of accessors: Interface, Component and Composite.
   The paper states: "A component accessor has an interface and a
   script...  The script defines one or more functions that are invoked
   by the swarmlet host."</p>

   <p>This is a specialized JavaScript actor that hides the script
   from casual users by putting it in "expert" mode.
   It also sets the actor to "restricted" mode, which restricts
   the functionality of the methods methods and variables
   provided in the JavaScript context.</p>

   <p>FIXME: This should support versioning of accessors.
   It should check the accessorSource for updates and replace
   itself if there is a newer version and the user agrees to
   the replacement. This will be tricky because any parameters
   and connections previously set should be preserved.</p>

   <p>This actor extends {@link ptolemy.actor.lib.jjs.JavaScript}
   and thus requires Nashorn, which is present in Java-1.8 and
   later.</p>

   <h2>References</h2>

   <p><a name="VisionOfSwarmLets">Elizabeth Latronico, Edward A. Lee, Marten Lohstroh, Chris Shaver, Armin Wasicek, Matt Weber.</a>
   <a href="https://www.terraswarm.org/pubs/332.html">A Vision of Swarmlets</a>,
   <i>IEEE Internet Computing, Special Issue on Building Internet of Things Software</i>,
   19(2):20-29, March 2015.</p>

   @author Edward A. Lee, Contributor: Christopher Brooks
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class JSAccessor extends JavaScript {

    /** Construct an accessor with the given container and name.
     *  @param container The container.
     *  @param name The name of this accessor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JSAccessor(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        accessorSource = new ActionableAttribute(this, "accessorSource");

        // Make the source editable so that you can update from another site.
        // accessorSource.setVisibility(Settable.NOT_EDITABLE);

        SingletonParameter hide = new SingletonParameter(script, "_hide");
        hide.setExpression("true");

        // The base class, by default, exposes the instance of this actor in the
        // JavaScript variable "actor", which gives an accessor full access
        // to the model, and hence a way to invoke Java code. Prevent this
        // by putting the actor in "restricted" mode.
        _restricted = true;

        // Set the script parameter to Visibility EXPERT.
        script.setVisibility(Settable.EXPERT);

        // Hide the port for the script.
        (new SingletonParameter(script.getPort(), "_hide")).setExpression("true");

        checkoutOrUpdateAccessorsRepository = new SharedParameter(this, "checkoutOrUpdateAccessorsRepository", getClass(),
                                                                  "true");
        checkoutOrUpdateAccessorsRepository.setTypeEquals(BaseType.BOOLEAN);
    }

    // Upon loading this class, change the icon loader to look for accessor icons.
    static {
        // To prevent overriding some other icon loader (e.g. Kepler),
        // set the custom icon loader only if there isn't one already.
        if (MoMLParser.getIconLoader() == null) {
            MoMLParser.setIconLoader(new AccessorIconLoader());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The source of the accessor (a URL). */
    public ActionableAttribute accessorSource;

    /** If true, then check out the accessors svn
     *  repository when the accessor is reloaded and run ant to build
     *  the PtDoc files.  This repository is not currently publically
     *  readable.  This parameter is a boolean, and the initial
     *  default value is true.  This parameter is a shared parameter,
     *  meaning that changing it for any one instance in a model will
     *  change it for all instances in the model.
     */
    public SharedParameter checkoutOrUpdateAccessorsRepository;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Generate MoML for an Accessor.
     *  <p>The MoML is wrapped in &lt;entity&gt;&lt;/entity&gt; and
     *  suitable for handleAccessorMoMLChangeRequest().</p>
     *
     *  <p>The accessor is read in from a url, processed with
     *  XSLT and MoML is returned.</p>
     *
     *  <p>In this method, the value of the
     *  <i>checkoutOrUpdateRepository</i> parameter is honored.</p>
     *
     *  @param urlSpec The URL of the accessor.
     *  @return MoML of the accessor, which is typically passed to
     *  handleAccessorMoMLChangeRequest().
     *  @exception IOException If the urlSpec cannot be converted,
     *  opened read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory
     *  cannot be created from the xslt file.
     *  @exception IllegalActionException If no source file is
     *  specified.
     */
    public static String accessorToMoML(final String urlSpec)
        throws IOException, TransformerConfigurationException, IllegalActionException {
        return JSAccessor.accessorToMoML(urlSpec, true);
    }

    /** Generate MoML for an Accessor.
     *  <p>The MoML is wrapped in &lt;entity&gt;&lt;/entity&gt; and
     *  suitable for handleAccessorMoMLChangeRequest().</p>
     *
     *  <p>The accessor is read in from a url, processed with
     *  XSLT and MoML is returned.</p>
     *
     *  @param urlSpec The URL of the accessor.
     *  @param obeyCheckoutOrUpdateRepositoryParameter If true, then
     *  use the value of the <i>checkoutOrUpdateRepository</i>
     *  parameter.  If false, then override the value of the
     *  <i>checkoutOrUpdateRepository</i> parameter and do not
     *  checkout or update the repository or invoke JSDoc.  During
     *  testing, this parameter is set to false after the first reload
     *  of an accessor so as to improve the performance of the tests.
     *  @return MoML of the accessor, which is typically passed to
     *  handleAccessorMoMLChangeRequest().
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     *  @exception IllegalActionException If no source file is specified.
     */
    public static String accessorToMoML(final String urlSpec,
                                        boolean obeyCheckoutOrUpdateRepositoryParameter)
        throws IllegalActionException, IOException, TransformerConfigurationException {

        // This method is a separate method so that we can use it for
        // testing the reimportation of accessors.  See
        // https://accessors.org/wiki/Main/TestAPtolemyAccessorImport

        // First get the file name only.
        String fileName = urlSpec.substring(urlSpec.lastIndexOf('/') + 1, urlSpec.length());
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        String instanceNameRoot = StringUtilities.sanitizeName(fileNameWithoutExtension);

        // Wrap in a group element that will rename the instance if there is a
        // naming conflict.
        // Wrap the transformed MoML in <entity></entity>
        return "<group name=\"auto\">\n"
            + "<entity name=\""  + instanceNameRoot
            + "\" class=\"org.terraswarm.accessor.jjs.JSAccessor\">"
            + "<property name=\"accessorSource\" value=\"" + urlSpec + "\"/>"
            + _accessorToMoML(urlSpec, obeyCheckoutOrUpdateRepositoryParameter)
            + "<property name=\"_tableauFactory\" class=\"ptolemy.vergil.toolbox.TextEditorTableauFactory\">"
            + "  <property name=\"attributeName\" value=\"script\"/>"
            + "  <property name=\"syntaxStyle\" value=\"text/javascript\"/>"
            + "</property><property name=\"script\">"
            + "  <property name=\"style\" class=\"ptolemy.actor.gui.style.NoteStyle\">"
            + "    <property name=\"note\" value=\"NOTE: To see the script, invoke Open Actor\"/>"
            + "</property></property></entity></group>";
    }

    /** React to a change in an attribute, and if the attribute is the
     *  script parameter, FIXME.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If evaluating the script fails.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == script) {
            // If either the new script or the old script is the initial
            // script created by the base class constructor, then
            // indicate that the script is not overridden.
            // The new value will be assumed to be that specified by the class
            // of which this accessor is an instance.
            // NOTE: Assume here that if the new script equals the old,
            // then it will not marked as overridden unless it was
            // previously marked as overridden. Trust the base classes
            // to do that.
            StringToken newScript = (StringToken)script.getToken();
            if (_previousScript == null
                || _previousScript.equals(_INITIAL_SCRIPT)
                || _INITIAL_SCRIPT.equals(newScript)) {
                // FIXME: Should this be MAX_VALUE or 1?
                attribute.setDerivedLevel(Integer.MAX_VALUE);
                // The above will have the side effect that a script will not be saved
                // when you save the model. Force it to be saved.
                // This ensures that the script is always available.
                attribute.setPersistent(true);
            }
            _previousScript = newScript;
        } else if (attribute == checkoutOrUpdateAccessorsRepository) {
            // Update the static cached version of this variable.
            _checkoutOrUpdateAccessorsRepository = ((BooleanToken) checkoutOrUpdateAccessorsRepository.getToken()).booleanValue();
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JSAccessor newObject = (JSAccessor) super.clone(workspace);

        newObject._previousScript = null;
        return newObject;
    }

    /** Check out or update the accessor repository, unless
     *  the <i>checkoutOrUpdateAccessorsRepository</i> parameter is
     *  false, in which case, do nothing.
     *  If the udpate succeeds, and the response from the SVN server
     *  does not include "At revision ", then also run ant in the
     *  accessors/web directory to update the documentation.
     *  If the checkout or update fails once, the it will not be tried
     *  again until the JVM is restarted.
     *  @exception IOException If the repository cannot be checked out.
     */
    public static void getAccessorsRepository() throws IOException {
        boolean updateNeeded =
            _checkoutOrUpdateAccessorsRepository
            && !_checkoutOrUpdateFailed
            && (_lastRepoUpdateTime < 0
                || (System.currentTimeMillis() - _lastRepoUpdateTime > 43200000L)); // 12 hours
        if (!updateNeeded) {
            return;
        }

        File accessorsRepoDirectory = new File(JSAccessor._accessorDirectory(), "accessors");
        final StringBufferExec exec = new StringBufferExec(true /*appendToStderrAndStdout*/);
        boolean newCheckout = false;
        boolean updated = false;
        try {
            List execCommands = new LinkedList<String>();
            // If the org/terraswarm/accessor/accessors directory
            // exists, then run svn update, otherwise try to check out
            // the repo.

            if (accessorsRepoDirectory.isDirectory()) {
                exec.setWorkingDirectory(accessorsRepoDirectory);
                // Use --accept postpone so that the updated proceeds despite local mods.
                // See http://lists.nceas.ucsb.edu/kepler/pipermail/kepler-dev/2010-January/017045.html
                String svnUpdateCommand = "svn update --non-interactive --trust-server-cert --accept postpone";
                execCommands.add(svnUpdateCommand);
                _commands = "cd " + accessorsRepoDirectory + "\n" + svnUpdateCommand;
                MessageHandler.status("Updating local copy of the accessors repository.");
            } else {
                newCheckout = true;
                // Default to anonymous, read-only access.
                String accessorsRepo = "https://repo.eecs.berkeley.edu/svn-anon/projects/icyphy/accessors/trunk/accessors";
                // If the ptII svn repo is read/write, then try read write access to the accessors repo.
                if (_ptIISvnRepoIsReadWrite()) {
                    accessorsRepo = "https://repo.eecs.berkeley.edu/svn/projects/icyphy/accessors/trunk/accessors";
                }
                exec.setWorkingDirectory(JSAccessor._accessorDirectory());
                String svnCommand = "svn co --non-interactive --trust-server-cert " + accessorsRepo;
                execCommands.add(svnCommand);
                _commands = "cd " + JSAccessor._accessorDirectory() + "\n" + svnCommand;
                MessageHandler.status("Checking out the accessors repository.");
            }

            exec.setCommands(execCommands);

            exec.setWaitForLastSubprocess(true);
            exec.start();
            // The following is redundant. Gets reported twice.
            // _commands += exec.buffer.toString();
            int returnCode = exec.getLastSubprocessReturnCode();
            if (returnCode == 0) {
                updated = true;
                _checkoutOrUpdateFailed = false;
                if (newCheckout) {
                    MessageHandler.status("Checked out accessor repository.");
                } else {
                    MessageHandler.status("Updated accessor repository.");
                }
            } else {
                _checkoutOrUpdateFailed = true;
                if (newCheckout) {
                    MessageHandler.status("Failed to check out the accessors repository.");
                    throw new IOException("Failed to check out the accessors repository with:\n"
                                          + _commands + "\n"
                                          + "The output was: " + exec.buffer);
                } else {
                    String message = "Could not update the accessors repository. Using local version.";
                    if (exec.buffer.toString().indexOf("Unable to conect to a repository") != -1
                        || exec.buffer.toString().indexOf("No more credentials or we tried too many times.") != -1) {
                        String osName = StringUtilities.getProperty("os.name");
                        if (osName.startsWith("Mac OS X")) {
                            message += "  Under Mac OS X, this can occur if the svn " +
                                "command does not have access to your keychain. " +
                                "One possible solution is reboot and run the command " +
                                "by hand.  A dialog will pop up asking if the svn " +
                                "command should have access to the keychain. " +
                                "Select 'Always' and rerun the demo.";
                        }
                    }
                    MessageHandler.status(message);
                }
            }
        } catch (Throwable throwable) {
            _checkoutOrUpdateFailed = true;
            if (newCheckout) {
                MessageHandler.status("Failed to check out the accessors repository.");
                IOException ioException = new IOException("Failed to check out the accessors repository with:\n"
                                                          + _commands + "\n"
                                                          + "Perhaps you don't have read access?  "
                                                          + "The accessors repository is under development.\n"
                                                          + "The output was: " + exec.buffer);
                ioException.initCause(throwable);
                throw ioException;
            } else {
                MessageHandler.status("Could not update the accessors repository. Using local version.");
            }
        }
        _lastRepoUpdateTime = System.currentTimeMillis();
        if (!updated || exec.buffer.toString().contains("At revision ")) {
            _checkoutOrUpdateFailed = false;
            // Presumably, there is no reason to update the docs, since there was no update.
            return;
        } else {
            _checkoutOrUpdateFailed = true;
        }

        JSAccessor._ptDoc();
    }

    /** If the URL can be found locally, return it, otherwise return
     *  the value of the passed in URL.
     *
     *  @param urlSpec The String URL of the accessor or *PtDoc.xml file.
     *  @param accessorOrPtDocURL The proposed URL of the accessor or *PtDoc.xml file.
     *  @return If the urlSpec matches
     *  https*://(www\.)*icyphy.org/accessors and the
     *  corresponding file can be found in the directory returned by
     *  _accessorDirectory(), then return a URL that refers to the
     *  local file.  Otherwise, return the value of the
     *  accessorOrPtDocURL argument.
     *  @exception IOException If thrown while getting the accessor directory.
     *  @exception MalformedURLException if throw while creating a URL.
     */
    public static URL getLocalURL(String urlSpec, URL accessorOrPtDocURL) throws IOException, MalformedURLException {
        // See the class comment for more information.

        // A possible enhancement would be to check the mod times of
        // the website and the local file and act accordingly.
        // Another enhancement would be to add a parameter to control
        // this functionality.

        String target = null;
        if (urlSpec.matches("https*://(www\\.)*icyphy.org/accessors/.*")) {
            target = "icyphy.org/accessors/";
        } else if (urlSpec.matches("https*://(www\\.)*accessors.org/.*")) {
            target = "accessors.org/";
        }
        if (target != null) {
            String urlSpecTailPath = urlSpec.substring(urlSpec.indexOf(target) + target.length());
            try {
                File urlSpecLocalFile = new File(_accessorDirectory(), "accessors/web/" + urlSpecTailPath);
                if (urlSpecLocalFile.exists()) {
                    if (urlSpecLocalFile.length() == 0) {
                        System.out.println("JSAccessor: urlSpec is " + urlSpec
                                           + ", but " + urlSpecLocalFile + " has length 0, so the former is being read");
                    } else {
                        // Print one message per local url spec.
                        if (_urlSpecLocalFilesPrinted == null) {
                            _urlSpecLocalFilesPrinted = new HashSet<File>();
                        }
                        if (!_urlSpecLocalFilesPrinted.contains(urlSpecLocalFile)) {
                            _urlSpecLocalFilesPrinted.add(urlSpecLocalFile);
                            // Don't print messages about the icons, it is annoying.
                            //System.out.println("JSAccessor: urlSpec is " + urlSpec
                            //                   + ", but " + urlSpecLocalFile + " exists, so the latter is being read. (This message is printed once per local file.)");
                        }
                        accessorOrPtDocURL = urlSpecLocalFile.toURI().toURL();
                    }
                }
            } catch (IOException ex) {
                System.out.println("JSAccessor: Could not look up the local accessor directory: " + ex);
            }
        }
        return accessorOrPtDocURL;
    }

    /** Handle an accessor-specific MoMLChangeRequest.
     *
     *  In the postParse() phase, the _location and accessorSource
     *  attributes are updated.
     *
     *  @param originator The originator of the change request.
     *  @param urlSpec The URL string specification.
     *  @param context The context in which the FMU actor is created.
     *  @param changeRequest The text of the change request,
     *  typically generated by {@link #accessorToMoML(String)}.
     *  @param x The x-axis value of the actor to be created.
     *  @param y The y-axis value of the actor to be created.
     */
    public static void handleAccessorMoMLChangeRequest(Object originator,
                                                       final String urlSpec,
                                                       NamedObj context, String changeRequest,
                                                       final double x, final double y) {

        // This method is a separate method because it makes sense
        // to move it away from the gui code in
        // ptolemy/vergil/basic/imprt/accessor/ImportAccessorAction.java

        MoMLChangeRequest request = new MoMLChangeRequest(originator, context,
                                                          changeRequest) {
                @Override
                protected void _postParse(MoMLParser parser) {
                    List<NamedObj> topObjects = parser.topObjectsCreated();
                    if (topObjects == null) {
                        return;
                    }
                    for (NamedObj object : topObjects) {
                        Location location = (Location) object
                            .getAttribute("_location");
                        // Set the location.
                        if (location == null) {
                            try {
                                location = new Location(object, "_location");
                            } catch (KernelException e) {
                                // Ignore.
                            }
                        }
                        if (location != null) {
                            try {
                                location.setLocation(new double[] { x, y });
                            } catch (IllegalActionException e) {
                                // Ignore.
                            }
                        }
                        // Set the source.
                        Attribute source = object.getAttribute("accessorSource");
                        if (source instanceof StringAttribute) {
                            // This has already been set in the MoML.
                            // ((StringAttribute) source).setExpression(urlSpec);
                            // Have to mark persistent or the urlSpec will be assumed to be part
                            // of the class definition and hence will not be exported to MoML.
                            ((StringAttribute) source).setDerivedLevel(Integer.MAX_VALUE);
                            ((StringAttribute) source).setPersistent(true);
                        }
                    }
                    parser.clearTopObjectsList();
                    super._postParse(parser);
                }

                @Override
                protected void _preParse(MoMLParser parser) {
                    super._preParse(parser);
                    parser.clearTopObjectsList();
                }
            };
        context.requestChange(request);
    }

    /** Reload an accessor.  The svn repository containing the
     *  accessors is checked out or updated and JSDoc is run on the
     *  documentation.
     *  @exception IllegalActionException If no source file is specified.
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     */
    public void reload() throws IllegalActionException, IOException, TransformerConfigurationException {
        reload(true);
    }

    /** Reload an accessor.
     *  @param obeyCheckoutOrUpdateRepositoryParameter If true, then use the value
     *  of the <i>checkoutOrUpdateRepository</i> parameter.  If false,
     *  then override the value of the
     *  <i>checkoutOrUpdateRepository</i> parameter and do not
     *  checkout or update the repository or invoke JSDoc.  During
     *  testing, this parameter is set to false after the first reload
     *  of an accessor so as to improve the performance of the tests.
     *  @exception IllegalActionException If no source file is specified.
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     */
    public void reload(boolean obeyCheckoutOrUpdateRepositoryParameter) throws IllegalActionException, IOException, TransformerConfigurationException {
        // This method is a separate method so that we can test it.

        /* No longer need the following, since we don't overwrite overrides.
           try {
           MessageHandler.warning("Warning: Overridden parameter values will be lost. Proceed?");
           } catch (CancelException e) {
           return;
           }
        */

        // Use FileParameter to preprocess the source to resolve
        // relative classpaths and references to $CLASSPATH, etc.
        // NOTE: This used to have name=\"doNotOverwriteOverrides\",
        // but then the script would not be replaced, and reload would
        // silently do nothing at all.  This is not OK.
        // Anyway, avoiding overwriting overrides is already handled
        // when the setup() method in the script is invoked.
        if (accessorSource == null) {
            throw new NullPointerException("accessorSource == null?");
        }
        if (accessorSource.asURL() == null) {
            throw new NullPointerException("accessorSource.asURL() == null? accessorSource was: " + accessorSource
                                           + " This can happen if there is no accessorSource attribute in the MoML file because a JavaScript actor was replaced with a JSAccessor actor via an edit of the MoML file.");
        }
        String moml = "<group>"
            + JSAccessor._accessorToMoML(accessorSource.asURL().toExternalForm(),
                                         obeyCheckoutOrUpdateRepositoryParameter)
            + "</group>";

        if (script.isOverridden()) {
            String diff = "";
            try {
                String scriptValue = script.getExpression().replaceAll("&#10;", "\n");
                String momlEscaped = StringUtilities.unescapeForXML(moml.replaceAll("&#10;", "\n"));
                diff = ptolemy.util.test.Diff.diff(scriptValue, momlEscaped);
            } catch (Throwable ex) {
                diff = "Failed to diff? " + ex;
                ex.printStackTrace();
            }

            if (!MessageHandler.yesNoQuestion("Overwrite local changes in " + getName() + "?\n" +
                                              "Below are the differences between the current contents of " +
                                              "the script (<) and the proposed new contents (>)\n" + diff)) {

                return;
            }
        }

        final NamedObj context = this;
        MoMLChangeRequest request = new MoMLChangeRequest(context, context, moml) {
                // Wrap this to give a more useful error message.
                protected void _execute() throws Exception {
                    try {
                        super._execute();
                    } catch (Exception e) {
                        // FIXME: Can we undo?
                        throw new IllegalActionException(context, e,
                                                         "Failed to reload accessor. Perhaps changes are too extensive."
                                                         + " Try re-importing the accessor.");
                    }
                    // Indicate that the script is not overridden.
                    // In other words, each time you reload the script,
                    // the new value will be assumed to be that specified by the class
                    // of which this accessor is an instance.
                    script.setDerivedLevel(Integer.MAX_VALUE);
                    // The above will have the side effect that a script will not be saved
                    // when you save the model. Force it to be saved so that there is always
                    // a local copy.
                    script.setPersistent(true);
                }
            };
        request.setUndoable(true);
        requestChange(request);
    }

    /** Reload all the JSAccessors in a CompositeEntity.
     *  The first time this method is invoked, the accessors
     *  repository will be checked out or updated and JSDoc
     *  invoked.  The second and subsequent times the method is
     *  invoked, the checkout or update and JSDoc invocation will
     *  not occur.  This is done so as to make testing faster.
     *
     *  If the script of a JSAccessor has local modifications, then
     *  the accessor will not be reloaded.
     *
     *  @param composite The composite that contains the JSAccessors
     *  @return true if the model contained any JSAccessors.
     *  @exception IllegalActionException If no source file is specified.
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     */
    public static boolean reloadAllAccessors(CompositeEntity composite)
        throws IllegalActionException, IOException, TransformerConfigurationException {
        // This method is use by the test harness.
        if (composite == null) {
            return false;
        }
        System.out.println("reloadAllAccessors: " + composite.getFullName());
        boolean containsJSAccessors = false;
        List entities = composite.allAtomicEntityList();
        for (Object entity : entities) {
            if (entity instanceof JSAccessor) {
                containsJSAccessors = true;
                if (!_invokedReloadAllAccessorsOnce) {
                    System.out.println("This is the first time that the reloadAllAccessors "
                                       + "method has been invoked in this JVM, so the the accessors "
                                       + "repo will be checked out or updated and JSDoc invoked. "
                                       + "Note that running the tests may end up invoking a new "
                                       + "JVM for each directory, so the repo may be checked out "
                                       + "or updated and JSDoc invoked more than once when the tests are run.");
                }
                if (((JSAccessor)entity).script.isOverridden()) {
                    System.out.println("reloadAllAccesors: The script of "
                                       + ((JSAccessor)entity).getFullName()
                                       + " has local modifications, so we are not reloading the accessor.");
                } else {
                    // The first time, we checkout or update the accessors
                    // repo and invoke JSDoc.  The second and subsequent
                    // times, we do not.  This is useful for testing.
                    ((JSAccessor)entity).reload(!_invokedReloadAllAccessorsOnce);
                    _invokedReloadAllAccessorsOnce = true;
                }
            }
        }
        return containsJSAccessors;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** For the given URL specification, attempt to find a local copy
     *  of the resource and return that if it exists. Otherwise,
     *  return the URL specified.  If updateRepository is true, or if
     *  an exception occurs accessing the local copy, then attempt to
     *  update the accessor repository on the local file system.  Do
     *  this only once in this instance of the JVM, as it is quite
     *  costly, unless the last update was more than 12 hours ago.
     *  @param urlSpec The URL specification.
     *  @param updateRepository True to update the repository before
     *   attempting to find the local file.
     *  @return The local version of the URL, or the URL given by the
     *   specification if the local version cannot be found.
     * @exception IllegalActionException If no urlSpec is given.
     * @exception IOException If the URL cannot be found.
     * @exception MalformedURLException If the URL specification is malformed.
     */
    protected static URL _sourceToURL(
                                      final String urlSpec, final boolean updateRepository)
        throws IllegalActionException, IOException, MalformedURLException {
        if (urlSpec == null || urlSpec.trim().equals("")) {
            throw new IllegalActionException("No source file specified.");
        }
        // If the source makes no mention of the accessors repo,
        // then just use FileParameter to find the source file.
        if (urlSpec.indexOf("org/terraswarm/accessor/accessors") < 0
            && urlSpec.indexOf("icyphy.org/accessors") < 0
            && urlSpec.indexOf("accessors.org/") < 0) {
            return FileUtilities.nameToURL(urlSpec, null, null);
        }

        URL accessorURL = null;
        try {
            JSAccessor.getAccessorsRepository();
        } catch (Throwable throwable) {
            System.err.println("Failed to checkout or update the accessors repo.  "
                               + "This could happen if you don't have read access to the repo.  "
                               + "The message was:\n"
                               + throwable);
        }
        try {
            accessorURL = FileUtilities.nameToURL(urlSpec.trim(), null, null);
        } catch(IOException ex) {
            // Note that if we get an exception, we try to get the repository
            // no matter what the value of updateRepository is.

            // If the urlSpec could be in the accessors repo, then try
            // to either check out or update the repo.
            if (urlSpec.indexOf("org/terraswarm/accessor/accessors") != -1) {
                boolean restore = _checkoutOrUpdateAccessorsRepository;
                try {
                    _checkoutOrUpdateAccessorsRepository = true;
                    JSAccessor.getAccessorsRepository();
                    accessorURL = FileUtilities.nameToURL(urlSpec.trim(), null, null);
                } catch (IOException ex2) {
                    IOException ioException = new IOException(ex.getMessage()
                                                              + "In addition, tried checking out the accessors repo with \n"
                                                              + JSAccessor._commands + "\n"
                                                              + "but that failed with: " + ex2.getMessage());
                    ioException.initCause(ex2);
                    throw ioException;
                } finally {
                    _checkoutOrUpdateAccessorsRepository = restore;
                }
            }
        }

        if (accessorURL == null) {
            throw new IOException("Failed to find accessor file: " + urlSpec.trim()
                                  + "\nWhich is converted to: " + accessorURL);
        }

        // Use the local file if possible.  See the method comment for
        // details.
        accessorURL = getLocalURL(urlSpec, accessorURL);
        return accessorURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** The directory that contains the accessors svn repository.
     *  The default value is $PTII/org/terraswarm/accessor
     *  Note that this svn repo is world readable.
     *  The command to check out the repo anonymously read-only is:</p>
     *  <pre>
     *  cd $PTII/org/terraswarm/accessor
     *  svn co svn co https://repo.eecs.berkeley.edu/svn-anon/projects/icyphy/accessors/trunk/accessors
     *  </pre>
     *
     *  <p>For read/write access, you need an account on repo.eecs.berkeley.edu,
     *  see <a href="https://accessors.org/svn.html#in_browser">https://accessors.org/svn.html</a>.
     *  The command is:</p>
     *  <pre>
     *  cd $PTII/org/terraswarm/accessor
     *  svn co svn co https://repo.eecs.berkeley.edu/svn/projects/icyphy/accessors/trunk/accessors
     *  </pre>
     *
     *  @return The $PTII/org/terraswarm/accessor directory.
     *  @exception IOException If the ptolemy.ptII.dir property does
     *  not exist or if the directory does not exist.
     */
    private static File _accessorDirectory() throws IOException {
        String ptII = StringUtilities.getProperty("ptolemy.ptII.dir");
        if (ptII == null) {
            throw new IOException("Could not get the property \"ptolemy.ptII.dir\"?");
        }
        File accessorDirectory = new File(ptII, "org/terraswarm/accessor");
        if ( !accessorDirectory.isDirectory()) {
            throw new IOException("The accessor directory \""
                                  + accessorDirectory + "\" does not exist or is not a directory.");
        }
        return accessorDirectory;
    }

    /** Generate MoML for an Accessor. This produces only the body MoML.
     *  It must be wrapped in an &lt;entity&gt;&lt;/entity&gt; or &lt;class&gt;&lt;/class&gt;
     *  element to be instantiable, or in a &lt;group&gt;&lt;/group&gt; to be used
     *  to update an accessor.
     *  The accessor is read in from a url, processed with
     *  XSLT (if XML) and MoML is returned.
     *
     *  <p>The first time this method is run, it will attempt to
     *  either checkout or update the accessors repo
     *  located at $PTII/org/terraswarm/accessor/accessors.  Note that
     *  this repo is not necessarily world readable. </p>
     *
     *  <p>If the <i>urlSpec</i> is not found, then the 
     *  accessor repo will be checked out or updated.</p>
     *
     *  <p>After the repo is loaded or updated, if url starts with
     *  https://accessors.org/ or
     *  https://icyphy.org/accessors/, and the corresponding file
     *  in the local svn accessors repo directory exists, then the
     *  file in the local svn directory is used instead of the file
     *  from the website.</p>
     *
     *  @param urlSpec The URL of the accessor.
     *  @param updateRepository If true, then checkout or update the
     *  accessor repository and invoke JSDoc.  During testing, this
     *  parameter is set to false after the first reload of an
     *  accessor so as to improve the performance of the tests.  The
     *  <i>checkoutOrUpdateRepositoryParameter</i> is not used here
     *  because this method is static.
     *  @return MoML of the accessor, which is typically passed to
     *  handleAccessorMoMLChangeRequest().
     *  @exception IOException If the urlSpec cannot be converted, opened
     *  read, parsed or closed.
     *  @exception TransformerConfigurationException If a factory cannot
     *  be created from the xslt file.
     *  @exception IllegalActionException If no source file is specified.
     */
    private static String _accessorToMoML(
                                          final String urlSpec, final boolean updateRepository)
        throws IOException, TransformerConfigurationException, IllegalActionException {

        // This method is a separate method so that we can use it for
        // testing the reimportation of accessors.  See
        // https://accessors.org/wiki/Main/TestAPtolemyAccessorImport
        URL accessorURL = _sourceToURL(urlSpec, updateRepository);

        final URL url = accessorURL;
        BufferedReader in = null;
        try {
            // If the URL starts with http, then we follow
            // up to 10 redirects.  Otherwise, we just
            // call URL.getInputStream().
            in = new BufferedReader(new InputStreamReader(
                                                          FileUtilities.openStreamFollowingRedirects(url), StandardCharsets.UTF_8));
            StringBuffer contents = new StringBuffer();
            String input;
            while ((input = in.readLine()) != null) {
                contents.append(input);
                contents.append("\n");
            }

            // If the spec is a JavaScript file, then do not use XSLT, but just
            // instantiate JSAccessor with the script parameter.
            String extension = urlSpec.substring(urlSpec.lastIndexOf('.') + 1, urlSpec.length());
            extension = extension.toLowerCase().trim();
            if (extension == null || extension.equals("")) {
                throw new IllegalActionException("Can't tell file type from extension: " + urlSpec);
            }
            if (extension.equals("js")) {
                // JavaScript specification.
                StringBuffer result = new StringBuffer();

                // Get the DocAttribute by looking for an adjacent *PtDoc.xml file.
                try {
                    if (urlSpec.endsWith("utilities/Mutable.js")) {
                        System.out.println("The Mutable accessor has no documentation, so we are not running jsdoc.");
                    } if (urlSpec.indexOf("test/auto") != -1) {
                        System.out.println("Accessors in test/auto do not typically have documentation, so we are not "
                                           + "running jsdoc on " + urlSpec);
                    } else {
                        result.append(_getPtDoc(accessorURL.toExternalForm()));
                    }
                } catch (IOException ex) {
                    try {
                        // Attempt to run "ant ptdoc".  This requires ant, node, npm and a network connection.
                        if (_ptDocFailed || JSAccessor._ptDoc() != 0) {
                            // Epicycles all the way down.
                            _ptDocFailed = true;
                            System.out.println("Building ptdoc failed, so we are going to try the website.");
                            result.append(_getPtDoc(urlSpec));
                        } else {
                            result.append(_getPtDoc(accessorURL.toExternalForm()));
                        }
                    } catch (IOException ex2) {
                        System.err.println("\nWarning: Cannot find PtDoc file for "
                                           + urlSpec.trim()
                                           + ". Importing without documentation. Initial exception was: " + ex
                                           + "\n Exception after attempting \"ant ptdoc\" was:" + ex2);
                    }
                }

                result.append("<property name=\"script\" value=\"");
                // NOTE: The following is no longer relevant. Not doing variable substitution
                // in the script.
                // Since $ causes the expression parser to try to substitute a variable, we need
                // to escape it.
                // String escaped = contents.toString().replaceAll("$", "$$");
                result.append(StringUtilities.escapeForXML(contents.toString()));
                result.append("\"/>");
                return result.toString();
            } else if (extension.equals("xml")) {
                // XML specification.
                // For this to work the CLASSPATH should include $PTII/lib/saxon8.jar:${PTII}/lib/saxon8-dom.jar
                TransformerFactory factory = TransformerFactory.newInstance();
                String xsltLocation = "$CLASSPATH/org/terraswarm/accessor/XMLJSToMoML.xslt";
                Source xslt = new StreamSource(FileUtilities.nameToFile(
                                                                        xsltLocation, null));
                Transformer transformer = factory.newTransformer(xslt);

                // If the URL starts with http, then we follow
                // up to 10 redirects.  Otherwise, we just
                // call URL.getInputStream().
                //StreamSource source = new StreamSource(new InputStreamReader(
                //        FileUtilities.openStreamFollowingRedirects(url)));
                StringWriter outWriter = new StringWriter();
                // NOTE: Could target a DOMResult here instead, which would give
                // much more flexibility.
                StreamResult result = new StreamResult(outWriter);
                try {
                    // If we have an Accessor that is written in a .xml file,
                    // then it may refer to the DTD with:

                    // <!DOCTYPE class PUBLIC "-//TerraSwarm//DTD Accessor 1//EN"
                    //     "https://accessors.org/Accessor_1.dtd">

                    // The problem is that https://accessors.org redirects and the
                    // resolver does not follow the redirect.

                    // Also, we should not be hitting on the website, instead we should
                    // use the Accessor_1.dtd that ships with this class.

                    // So, we define an entity resolver.  See
                    // http://stackoverflow.com/questions/1572808/java-xml-xslt-prevent-dtd-validation

                    XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                    xmlReader.setEntityResolver(new EntityResolver() {
                            public InputSource resolveEntity(String publicID, String systemID)
                                throws SAXException {
                                if (publicID.equals("-//TerraSwarm//DTD Accessor 1//EN")
                                    && systemID.equals("https://accessors.org/Accessor_1.dtd")) {
                                    try {
                                        // See also ptolemy/configs/test/ValidatingXMLParser.java
                                        String dtd = FileUtilities.getFileAsString("$CLASSPATH/org/terraswarm/accessor/accessors/web/Accessor_1.dtd");
                                        InputSource source = new InputSource(new StringReader(dtd));
                                        source.setPublicId(publicID);
                                        source.setSystemId(systemID);
                                        return source;
                                    } catch (Exception ex) {
                                        throw new SAXException("Failed to read Accessor_1.dtd from local file system", ex);
                                    }
                                } else {
                                    System.out.println("JSAccessor._accessorToMoML.resolveEntity(): "
                                                       + " Minor warning: Can't resolve "
                                                       + publicID + ", " + systemID
                                                       + ".  Returning the empty string.");
                                    return new InputSource(new ByteArrayInputStream(new byte[] {}));
                                }
                            }
                        });


                    Source source = new SAXSource(xmlReader, new InputSource(new InputStreamReader(
                                                                                                   FileUtilities.openStreamFollowingRedirects(url))));


                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
                    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
                    contents = outWriter.getBuffer();
                    transformer.transform(source, result);
                    contents = outWriter.getBuffer();
                } catch (Throwable throwable) {
                    IOException ioException = new IOException("Failed to parse \""
                                                              + url
                                                              + "\".");
                    ioException.initCause(throwable);
                    throw ioException;
                }
                return contents.toString();
            } else {
                throw new IllegalActionException("Unrecognized file extension: " + extension);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /** Get the PtDoc for an accessor or create a link to a html file.
     *
     *  Given a urlSpec for foo.js, look for PtDoc.xml.  If it is found,
     *  read it and return the text.
     *
     *  FIXME: Not done yet: If it is not found, create a placehold
     *  doc that just points to the online HTML file.
     *
     * @param urlSpec The URL of the accessor, which must end in ".js".
     * @return The contents of the *PtDoc.xml file, if any.
     * @exception IOException If *PtDoc.xml cannot be found or read.
     */
    private static String _getPtDoc(String urlSpec) throws IOException {
        // By definition, this must be called on a url that ends with .js
        // FIXME: what happens with a JarURL?
        String ptDocSpec = urlSpec.substring(0, urlSpec.length() - 3) + "PtDoc.xml";

        URL url = FileUtilities.nameToURL(ptDocSpec , null, null);

        // Look for a local version of the PtDoc file.
        // This assumes that getAccessorsRepository() was previously invoked.
        url = getLocalURL(ptDocSpec, url);

        // Check that the URL is readable, and import without docs if not.
        // This will throw IOException if not.
        // If the URL starts with http, then we follow
        // up to 10 redirects.  Otherwise, we just
        // call URL.getInputStream().
        BufferedReader in = null;
	//InputStream in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                                                          FileUtilities.openStreamFollowingRedirects(url)));
            String line = in.readLine();
            // We used to call availavble() on the inputStream, but that
            // does not always work.  See
            // http://stackoverflow.com/questions/15030026/httpurlconnection-getinputstream-returns-empty-stream-in-android
            if (line == null || line.length() == 0) {
		//if ( in.available() == 0) {
                throw new IOException("Could not find PtDoc for urlSpec: \"" + urlSpec + "\"."
                                      + "The url \"" + url + "\" was opened, but had 0 bytes available?  "
                                      + "Perhaps the file has a zero length because there are no "
                                      + "JSDoc directives in it?");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // For some reason, using an input tag results in updating
        // the docs on reload, whereas using the previous method below
        // does not.
        return "<input source=\""
            + StringUtilities.escapeForXML(url.toExternalForm())
            + "\"/>";

        /* Old version below:
           BufferedReader in = null;
           try {
           in = new BufferedReader(new InputStreamReader(
           url.openStream()));
           StringBuffer contents = new StringBuffer();
           String input;
           while ((input = in.readLine()) != null) {
           contents.append(input);
           contents.append("\n");
           }
           return contents.toString();
           } catch (IOException ex) {
           // FIXME: Look for a .html file in the same location as
           // the .js file.
           System.err.println("JSAccessor._getPtDoc(" + url + "): "
           + " Could not find PtDoc file" + ex);
           //throw ex;
           } finally {
           if (in != null) {
           in.close();
           }
           }
           return "";
        */
    }

    /** Return true if the ptII tree was checked out with svn
     *  read/write access.  Return false if the ptII tree was not
     *  checked out of svn or if the ptII tree was checked out with
     *  anonymous svn access.
     */
    private static boolean _ptIISvnRepoIsReadWrite() {
        String ptII = StringUtilities.getProperty("ptolemy.ptII.dir");

        // Return false if $PTII/.svn is not a directory.
        if (! (new File(ptII, ".svn").isDirectory())) {
            return false;
        }

        // Run svn info, get the Repository Root: line and return the value.
        StringBufferExec exec = new StringBufferExec(true /*appendToStderrAndStdout*/);
        exec.setWorkingDirectory(new File(ptII));

        List execCommands = new LinkedList<String>();
        execCommands.add("svn info");
        exec.setCommands(execCommands);

        exec.setWaitForLastSubprocess(true);

        System.out.println("Invoking \"svn info\" in order to determine whether the anonymous, read only accessors repo should be used or the user-specific, read/write accessors repo should be used.");
        exec.start();

        // If the subprocess returns non-zero, then return false
        int returnCode = exec.getLastSubprocessReturnCode();
        if (returnCode != 0) {
            MessageHandler.status("In " + ptII + ", \"svn info\" returned " + returnCode
                                  + ".  The output was: " + exec.buffer);
            return false;
        }

        // Search for "Repository Root:"
        String repositoryRoot = "Repository Root:";
        int index = exec.buffer.toString().indexOf(repositoryRoot);
        if (index == -1) {
            return false;
        }

        // Search for the URL after "Repository Root:"
        String readWriteURL = "https://repo.eecs.berkeley.edu/svn/projects/eal/ptII";

        int index2 = exec.buffer.toString().indexOf(readWriteURL,  index + repositoryRoot.length() + 1);
        if (index2 != 1) {
            return true;
        }

        return false;
    }

    /** Build the ptdoc files.
     *  @return 0 if ant and node were found and ant returned 0.   
     */
    private static int _ptDoc() throws IOException {
        File accessorsRepoWebDirectory = new File(JSAccessor._accessorDirectory(), "accessors/web");
        if (accessorsRepoWebDirectory.isDirectory()) {
            // This is similar to code in ptolemy/vergil/actor/DocBuilder.java
            // If the accessors/web directory exists, run ant there.
            MessageHandler.status("Updating documentation for accessors.");
            String antPath = "";
            try {
                StringBufferExec antExec = new StringBufferExec(true /*appendToStderrAndStdout*/);
                antExec.setWorkingDirectory(accessorsRepoWebDirectory);
                List execCommands = new LinkedList<String>();
                // $PTII/configure looks for "ant" in the path and stores it $PTII/lib/ptII.properties
                antPath = StringUtilities.getProperty("ptolemy.ant.path");

                // Common text for error messages.
                String defaultAnt = "This property is set by $PTII/configure and stored in "
                    + "$PTII/lib/ptII.properties.  "
                    + "Defaulting to using \"ant\", "
                    + "which may or may not be in your path.";

                if (antPath.length() == 0) {
                    antPath = "ant";
                } else {
                    File antExecutableFile = new File(antPath);
                    if (!antExecutableFile.isFile()) {
                        System.out.println("The value of the ptolemy.ant.path property was \""
                                           + antPath + "\", which is not a file.  "
                                           + defaultAnt);
                        antPath = "ant";
                    }
                }
                if (!FileUtilities.inPath("ant")) {
                    System.out.println("Could not find the \"ant\" executable in your path, to build the accessor docs, please install ant from http://ant.apache.org/");
                    MessageHandler.status("Could not find \"ant\". Accessor docs will not be generated.");

                    return -1;
                }

                if (!FileUtilities.inPath("node")) {
                    System.out.println("Could not find the \"node\" executable in your path, to build the accessor docs, please install ant from https://nodejs.org/");
                    MessageHandler.status("Could not find \"node\". Accessor docs will not be generated.");
                    return -2;
                }
                execCommands.add(antPath + " ptdoc");
                antExec.setCommands(execCommands);
                antExec.setWaitForLastSubprocess(true);
                antExec.start();
                if (antExec.getLastSubprocessReturnCode() == 0) {
                    MessageHandler.status("Updated documentation for accessors.");
                } else {
                    MessageHandler.status("Update of documentation failed. Using default docs.");
                    System.err.println("Failed: \"" + antPath
                                       + "\" in " + accessorsRepoWebDirectory
                                       + "\n"
                                       + "Output was: " + antExec.buffer);
                }
                return antExec.getLastSubprocessReturnCode();
            } catch (Throwable throwable) {
                MessageHandler.status("Update of documentation failed. Using default docs.");
                System.err.println("Warning: Failed to run \"" + antPath
                                   + "\" in " + accessorsRepoWebDirectory
                                   + ".  Defaulting to using the documentation from the website."
                                   + "Error message was: " + throwable);
                throwable.printStackTrace();
            }
        }
        return -3;
    }

    /** Cached version of the checkoutOrUpdateAccessorsRepository parameter.
     *  The method that uses this field is static.
     */
    private static boolean _checkoutOrUpdateAccessorsRepository = true;

    /** True if the checkout or update of the accessors repository failed.
     */
    private static boolean _checkoutOrUpdateFailed = false;

    /** Commands that were run to check out or update the repo. */
    private static String _commands = "";

    /** If true, then checkout or update the accessors repository and
     *  invoke JSDoc to generate the JavaScript documentation.
     *  The value of the field is initially false, but it is set
     *  to true after reloadAllAccessors is invoked once.
     *  This parameter exists so that when the tests are run,
     *  we checkout or update the repository and invoke JSDoc
     *  only once per directory.
     */
    private static boolean _invokedReloadAllAccessorsOnce = false;

    /** Last time of accessor respository update. */
    private static long _lastRepoUpdateTime = -1L;

    /** Previous value of the script parameter, or null if it has
     *  not been set.
     */
    private StringToken _previousScript = null;

    /** Set to true of the _ptDoc() method returned non-zero,
     *  Indicating that "ant ptdoc" failed.
     */        
    private static boolean _ptDocFailed = false;

    /** Local url specifications that have been printed because
     *  the are local.
     */
    private static Set<File> _urlSpecLocalFilesPrinted = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Attribute with an associated named action.
     */
    public class ActionableAttribute extends FileParameter implements Actionable {

        /** Create a new actionable attribute.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the base class throws it.
         *  @exception NameDuplicationException If the base class throws it.
         */
        public ActionableAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Return "Reload". */
        @Override
        public String actionName() {
            return "Reload";
        }

        /** Reload the accessor. */
        @Override
        public void performAction() throws Exception {
            reload();
        }
    }
}
