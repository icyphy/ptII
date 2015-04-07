/* Utility methods to handle HTML Viewer about: calls

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// HTMLAbout

/**
 This class contains static methods that are called
 by when HTMLViewer.hyperlinkUpdate() is invoked on a hyperlink
 that starts with <code>about:</code>.  This facility is primarily
 used for testing.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see HTMLViewer#hyperlinkUpdate(HyperlinkEvent)
 */
public class HTMLAbout {
    // This class is separate from HTMLViewer because this class
    // import lots of Ptolemy specify classes that HTMLViewer does
    // otherwise need to import
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a string containing HTML that describes the about:
     *  features.
     *
     *  <p>If the configuration contains an _applicationName attribute
     *  then that attribute is used as the name of the application
     *  in the generated text.  If _applicationName is not present,
     *  then the default name is "Ptolemy II".
     *
     *  <p>If the configuration contains an _applicationDemos Parameter
     *  then that parameter is assumed to be an array of strings name
     *  naming HTML files that should be searched for demos and expanded.

     *  @param configuration The configuration to look for the
     *  _applicationName attribute in
     *  @return A string containing HTML that describes the about: features.
     */
    public static String about(Configuration configuration) {
        // Use an explicit version here - the name of the whatsNew file
        // does not changes as quickly as the version.
        String version = VersionAttribute.majorCurrentVersion();

        String applicationName = "Ptolemy II";

        try {
            StringAttribute applicationNameAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationName", StringAttribute.class);

            if (applicationNameAttribute != null) {
                applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Throwable throwable) {
            // Ignore and use the default applicationName
        }

        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append("<html><head><title>About " + applicationName
                + "</title></head>" + "<body><h1>About " + applicationName
                + "</h1>\n" + "The HTML Viewer in " + applicationName
                + " handles the <code>about:</code>\n" + "tag specially.\n"
                + "<br>The following urls are handled:\n" + "<ul>\n"
                + "<li><a href=\"about:configuration\">"
                + "<code>about:configuration</code></a> "
                + "Expand the configuration (good way to test for "
                + "missing classes).\n"
                + "<li><a href=\"about:expandLibrary\">"
                + "<code>about:expandLibrary</code></a> "
                + "Open a model and expand library tree (good way to test for "
                + "missing classes, check standard out).\n"
                + "<li><a href=\"about:copyright\">"
                + "<code>about:copyright</code></a> "
                + " Display information about the copyrights.\n");

        if (_configurationExists("full")) {
            htmlBuffer.append("<li><a href=\"about:checkCompleteDemos\">"
                    + "<code>about:checkCompleteDemos</code></a> "
                    + "Check that each of the demos listed in the individual "
                    + "files is present in "
                    + "<code>ptolemy/configs/doc/completeDemos.htm</code>.\n");
        }

        htmlBuffer.append("</ul>\n<table>\n");

        _demosURLs = new LinkedList();
        if (_configurationExists("full")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>Full</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/doc/completeDemos.htm")
                    + _aboutHTML("ptolemy/configs/doc/demos.htm")
                    + _aboutHTML("ptolemy/configs/doc/whatsNew"
                            + version + ".htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew10.0.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew8.0.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew7.0.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew6.0.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew5.1.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew5.0.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew4.0.htm")
                            + _aboutHTML("ptolemy/configs/doc/whatsNew3.0.2.htm"));
        }

        if (_configurationExists("bcvtb")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>BCVTB</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/bcvtb/intro.htm")
                    + _aboutHTML("ptolemy/configs/doc/completeDemosBcvtb.htm")
                    + _aboutHTML("ptolemy/configs/doc/demosBcvtb.htm")
                    + _aboutHTML("ptolemy/configs/doc/docsBcvtb.htm"));
        }
        if (_configurationExists("cyphysim")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>CyPhySim</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/cyphysim/intro.htm")
                    + _aboutHTML("ptolemy/configs/cyphysim/demonstrations.htm")
                    + _aboutHTML("ptolemy/configs/cyphysim/docs.htm")
                    + _aboutHTML("ptolemy/configs/doc/docs.htm"));
        }
        // Don't include DSP here, it uses the Ptiny demos anyway.
        if (_configurationExists("hyvisual")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>HyVisual</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/hyvisual/intro.htm"));
        }

        if (_configurationExists("ptiny")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>Ptiny</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/doc/completeDemosPtiny.htm")
                    + _aboutHTML("ptolemy/configs/doc/demosPtiny.htm")

                    + _aboutHTML("doc/mainVergilPtiny.htm"));
        }

        if (_configurationExists("ptinyKepler")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>Ptiny for Kepler</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/kepler/doc-index.htm")
                    + _aboutHTML("doc/mainVergilPtinyKepler.htm")
                    + _aboutHTML("ptolemy/configs/doc/demosPtinyKepler.htm")
                    + _aboutHTML("ptolemy/configs/doc/docsPtinyKepler.htm")
                    + _aboutHTML("ptolemy/configs/doc/completeDemosPtinyKepler.htm"));

        }
        if (_configurationExists("visualsense")) {
            htmlBuffer
            .append("<tr rowspan=4><center><b>VisualSense</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/visualsense/intro.htm"));
        }

        try {
            // Check for the _applicationDemos parameter
            Parameter applicationDemos = (Parameter) configuration
                    .getAttribute("_applicationDemos", Parameter.class);

            if (applicationDemos != null) {
                htmlBuffer.append("<tr rowspan=4><center><b>" + applicationName
                        + "</b></center></tr>\n");

                ArrayToken demoTokens = (ArrayToken) applicationDemos
                        .getToken();

                for (int i = 0; i < demoTokens.length(); i++) {
                    StringToken demoToken = (StringToken) demoTokens
                            .getElement(i);
                    htmlBuffer.append(_aboutHTML(demoToken.stringValue()));
                    System.out.println("HTMLAbout: adding "
                            + demoToken.stringValue());
                }
            }
        } catch (Exception ex) {
            throw new InternalErrorException(configuration, ex,
                    "Bad configuration for " + applicationName);
        }

        htmlBuffer.append("</table>\n");
        htmlBuffer.append("</body>\n</html>\n");
        return htmlBuffer.toString();
    }

    /** Check that all the demos in otherDemos are in completeDemos.
     *  Be sure to call {@link #about(Configuration)} before calling this method.
     *  @param completeDemos A URL pointing to the completeDemos.htm file
     *  @return HTML listing demos in otherDemos that are not in completeDemos.
     *  @exception IOException If there is a problem reading the
     *  completeDemos.htm file.
     */
    public static String checkCompleteDemos(String completeDemos)
            throws IOException {
        URL demosURL = _getDemoURL(completeDemos);
        StringBuffer results = new StringBuffer(
                "<h1>Results of checking for demos not listed in full "
                        + "demos</h1>\n"
                        + "For each of the files below, we list demos that are "
                        + "not included in <a href=\"" + demosURL + "\">"
                        + "<code>" + demosURL + "</code></a>\n");
        List completeDemosList = _getURLs(demosURL, ".*.xml$", true, 1);
        if (_demosURLs == null) {
            throw new NullPointerException(
                    "_demosURLs is null.  Call HTMLAbout.about(Configuration) first.");
        }
        Iterator demosFileNames = _demosURLs.iterator();
        while (demosFileNames.hasNext()) {
            String demosFileName = (String) demosFileNames.next();
            URL demoURL = _getDemoURL(demosFileName);
            if (demoURL != null) {
                results.append("<h2><a href=\"" + demoURL + "\"><code>"
                        + demoURL + "</code></a></h2>\n<ul>\n");

                List demosList = _getURLs(demoURL, ".*.xml$", true, 0);
                Iterator demos = demosList.iterator();
                while (demos.hasNext()) {
                    String demo = (String) demos.next();
                    if (!completeDemosList.contains(demo)) {
                        try {
                            URL missingDemoURL = ConfigurationApplication
                                    .specToURL(demo);
                            results.append(" <li><a href=\"" + missingDemoURL
                                    + "\">" + missingDemoURL + "</a></li>\n");
                        } catch (IOException ex) {
                            results.append(" <li><a href=\"file:/" + demo
                                    + "\">" + demo + "</a></li>\n");
                        }
                    }
                }
                results.append("</ul>\n");
            }
        }
        return results.toString();
    }

    /** Call Configuration.openModel() on relative URLs that match a regexp.
     *  files are linked to from an HTML file.
     *  @param demosFileName The name of the HTML file that contains links
     *  to the .xml, .htm and .html files.
     *  If this argument is the empty string, then
     *  "ptolemy/configs/doc/completeDemos.htm" is used.
     *  @param regexp The regular expression of the links we are interested
     *  in.
     *  @param configuration  The configuration to open the files in.
     *  @return the URL of the HTML file that was searched.
     *  @exception Exception If there is a problem opening a model.
     */
    public static URL generateLinks(String demosFileName, String regexp,
            Configuration configuration) throws Exception {
        URL demosURL = _getDemoURL(demosFileName);
        List modelList = _getURLs(demosURL, regexp);
        Iterator models = modelList.iterator();

        while (models.hasNext()) {
            String model = (String) models.next();
            if (model.startsWith("ptdoc:")) {
                Effigy context = configuration.getDirectory()
                        .entityList(Effigy.class).iterator().next();
                HTMLViewer.getDocumentation(configuration, model.substring(6),
                        context);
            } else {
                URL modelURL = new URL(demosURL, model);

                try {
                    configuration.openModel(demosURL, modelURL,
                            modelURL.toExternalForm());
                } catch (Throwable throwable) {
                    throw new Exception("Failed to open '" + modelURL + "'",
                            throwable);
                }
            }
        }

        return demosURL;
    }

    /** Process an "about:" HyperlinkEvent.
     *  @param event The HyperlinkEvent to process.  The description of
     *  the event should start with "about:".  If there are no specific
     *  matches for the description, then a general usage message is
     *  returned.
     *  @param configuration The configuration in which we are operating.
     *  @return A URL that points to the results.
     *  @exception Throwable If there is a problem invoking the about
     *  task.
     */
    public static URL hyperlinkUpdate(HyperlinkEvent event,
            Configuration configuration) throws Throwable {
        URL newURL = null;

        if (event.getDescription().equals("about:allcopyrights")) {
            // Note that if we have a link that is
            // <a href="about:copyright">about:allcopyrights</a>
            // then event.getURL() will return null, so we have
            // to use getDescription()
            try {
                newURL = _temporaryHTMLFile("generatecopyright", ".htm",
                        GenerateCopyrights.generateHTML(configuration));
            } catch (SecurityException ex) {
                // Could be that we were running with -sandbox and
                // cannot write the temporary file.
                newURL = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/configs/doc/copyright.htm", null,
                        null);
            }

        } else if (event.getDescription()
                .startsWith("about:checkCompleteDemos")) {
            newURL = _temporaryHTMLFile("checkCompleteDemos", ".htm",
                    checkCompleteDemos("ptolemy/configs/doc/completeDemos.htm"));
        } else if (event.getDescription().startsWith("about:checkModelSizes")) {
            // Expand all the local .xml files in the fragment
            // and check their sizes and locations
            URI aboutURI = new URI(event.getDescription());
            URL demosURL = _getDemoURL(aboutURI.getFragment());
            // Third arg is true so modelList should contain absolute URLs.
            List modelList = _getURLs(demosURL, ".*(.htm|.html|.xml)", true, 2);
            // Convert the list to a Set and avoid duplicates.
            Set modelSet = new HashSet(modelList);
            newURL = _temporaryHTMLFile("checkModelSizes", ".htm",
                    CheckModelSize.checkModelSize(configuration,
                            (String[]) modelSet.toArray(new String[modelSet
                                                                   .size()])));
        } else if (event.getDescription().equals("about:copyright")) {
            // Note that if we have a link that is
            // <a href="about:copyright">about:copyright</a>
            // then event.getURL() will return null, so we have
            // to use getDescription()
            try {
                newURL = _temporaryHTMLFile(
                        "copyright",
                        ".htm",
                        GenerateCopyrights
                        .generatePrimaryCopyrightHTML(configuration)
                        + "<p>Other <a href=\"about:allcopyrights\">copyrights</a>\n"
                        + "about this configuration \n"
                        + "(<i>may take a moment to run</i>).\n"
                        + "</body>\n</html>");
            } catch (SecurityException ex) {
                // Could be that we were running with -sandbox and
                // cannot write the temporary file.
                newURL = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/configs/doc/copyright.htm", null,
                        null);
            }
        } else if (event.getDescription().equals("about:configuration")) {
            // about:configuration will expand the configuration
            // and report any problems such as missing classes.
            // Open up the configuration as a .txt file because if
            // we open it up as a .xml file, we get a graphical browser
            // that does not tell us much.  If we open it up as a .htm,
            // then the output is confusing.
            newURL = _temporaryHTMLFile("configuration", ".txt",
                    configuration.check() + configuration.exportMoML());
        } else if (event.getDescription().startsWith("about:demos")) {
            // Expand all the local .xml files in the fragment
            // and return a URL pointing to the fragment.
            // If there is no fragment, then use
            // "ptolemy/configs/doc/completeDemos.htm"
            URI aboutURI = new URI(event.getDescription());
            newURL = generateLinks(aboutURI.getFragment(), ".*.xml$",
                    configuration);
        } else if (event.getDescription().startsWith("about:links")) {
            // Expand all the local .html, .htm, .pdf, .xml files in
            // the fragment and return a URL pointing to the fragment.
            // If there is no fragment, then use
            // "ptolemy/configs/doc/completeDemos.htm"
            URI aboutURI = new URI(event.getDescription());
            newURL = generateLinks(aboutURI.getFragment(),
                    "(ptdoc:.*|.*(.htm|.html|.pdf|.xml))", configuration);
        } else if (event.getDescription().startsWith("about:runAllDemos")) {
            URI aboutURI = new URI(event.getDescription());
            newURL = runAllDemos(aboutURI.getFragment(), configuration);
        } else if (event.getDescription().startsWith("about:expandLibrary")) {
            //URI aboutURI = new URI(event.getDescription());
            newURL = _expandLibrary(".*.xml", configuration);
        } else {
            // Display a message about the about: facility
            newURL = _temporaryHTMLFile("about", ".htm", about(configuration));
        }

        return newURL;
    }

    /** Generate a file that contains urls of models.
     *  @param args The optional name of the file containing the demos
     *  followed by the optional name of the output file.  The default
     *  demo file is ptolemy/configs/doc/completeDemos.htm, the default
     *  output file is models.txt.
     *  @exception IOException If there is a problem reading the demo
     *  file or writing the model file.
     */
    public static void main(String[] args) throws IOException {
        String demoFileName = "ptolemy/configs/doc/completeDemos.htm";
        String outputFileName = "models.txt";

        if (args.length > 2) {
            System.err.println("Usage: [demoFileName [outputFilename]\n"
                    + "demoFileName defaults to " + demoFileName + "\n"
                    + "outputFileName defaults to " + outputFileName + "\n");
            StringUtilities.exit(3);
        }
        if (args.length >= 1) {
            demoFileName = args[0];
        }
        if (args.length == 2) {
            outputFileName = args[1];
        }
        writeDemoURLs(demoFileName, outputFileName);
    }

    /** Run all the local .xml files that are linked to from an HTML file.
     *  @param demosFileName The name of the HTML file that contains links
     *  to the .xml files.  If this argument is the empty string, then
     *  "ptolemy/configs/doc/completeDemos.htm" is used.
     *  @param configuration  The configuration to run the files in.
     *  @return the URL of the HTML file that was searched.
     *  @exception Exception If there is a problem running a demo.
     */
    public static URL runAllDemos(String demosFileName,
            Configuration configuration) throws Exception {
        URL demosURL = _getDemoURL(demosFileName);
        List modelList = _getURLs(demosURL, ".*.xml$");
        Iterator models = modelList.iterator();

        while (models.hasNext()) {
            String model = (String) models.next();
            URL modelURL = new URL(demosURL, model);

            Tableau tableau = configuration.openModel(demosURL, modelURL,
                    modelURL.toExternalForm());

            if ((Effigy) tableau.getContainer() instanceof PtolemyEffigy) {
                PtolemyEffigy effigy = (PtolemyEffigy) tableau.getContainer();
                CompositeActor actor = (CompositeActor) effigy.getModel();

                // Create a manager if necessary.
                Manager manager = actor.getManager();

                if (manager == null) {
                    manager = new Manager(actor.workspace(), "manager");
                    actor.setManager(manager);
                }

                //manager.addExecutionListener(this);
                manager.execute();
            }
        }

        return demosURL;
    }

    /** Write the urls of the demo urls.  The HTML file referred to by
     * demoURLName is scanned for links to .xml files and for links to
     * other .htm* files.  The children of demoURLName are scanned,
     * but not the grandchildren.  The names of the demos will have
     * $CLASSPATH/ prepended. This method is used to generate a list of all
     * demos in ptolemy/configs/doc/models.txt.
     * @param demosFileName The name of the demo file.
     * @param outputFileName The name of the file that is generated.
     * @exception IOException If there is a problem reading the demo file
     * or writing the output file.
     */
    public static void writeDemoURLs(String demosFileName, String outputFileName)
            throws IOException {
        // Get PTII as C:/cxh/ptII
        String ptII = null;
        try {
            ptII = new URI(StringUtilities.getProperty("ptolemy.ptII.dirAsURL"))
            .normalize().getPath();
            // Under Windows, convert /C:/foo/bar to C:/foo/bar
            ptII = new File(ptII).getCanonicalPath().replace('\\', '/');
        } catch (URISyntaxException ex) {
            throw new InternalErrorException(null, ex,
                    "Failed to process PTII " + ptII);
        }
        if (ptII.length() == 0) {
            throw new InternalErrorException("Failed to process "
                    + "ptolemy.ptII.dirAsURL property, ptII = null?");
        }
        URL demoURL = ConfigurationApplication.specToURL(demosFileName);
        List demosList = _getURLs(demoURL, ".*.xml", true, 2);
        Set demosSet = new HashSet(demosList);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(outputFileName);
            Iterator demos = demosSet.iterator();
            while (demos.hasNext()) {
                String demo = (String) demos.next();
                // Look for the value of $PTII and substitute in $CLASSPATH
                // so that we can use FileUtilities.nameToURL() from within
                // ptolemy.moml.filter.ActorIndex
                fileWriter.write(StringUtilities.substitute(demo, ptII,
                        "$CLASSPATH") + "\n");
            }
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Return a string containing HTML with links the the about:demos
    // and about:links pages
    private static String _aboutHTML(String fileName) {
        _demosURLs.add(fileName);
        return "  <tr>\n"
        + "    <code>"
        + fileName
        + "</code>\n"
        + "    <td><a href=\"about:demos#"
        + fileName
        + "\">&nbsp;Open the .xml&nbsp;</a></td>\n"
        + "    <td><a href=\"about:links#"
        + fileName
        + "\">&nbsp;Open the ptdoc: .htm, .html, .xml and .pdf&nbsp;</a></td>\n"
        + "    <td><a href=\"about:checkModelSizes#"
        + fileName
        + "\">&nbsp;Check the sizes/centering of the models&nbsp;</a></td>\n"
        // RunAllDemos does not work, it runs in the wrong thread?
        // + "    <td><a href=\"about:runAllDemos#" + fileName
        // + "\">&nbsp;Run all demos&nbsp;</a></td>\n"
        + "  </tr>\n";
    }

    /** Expand the left hand library pane.  We search for a model,
     *  first in the intro.htm file, then the complete demos page and
     *  then on the Ptolemy website.  The model is opened and then the
     *  left hand library pane is expanded.  This test is useful for
     *  looking for problem with icons, such as icons that cause
     *  ChangeRequests.
     *  @param regexp The regular expression of the links we are interested
     *  in.
     *  @param configuration  The configuration to open the files in.
     *  @return the URL of the HTML file that was searched.
     *  @exception Exception If there is a problem opening a model.
     */
    public static URL _expandLibrary(String regexp, Configuration configuration)
            throws Exception {
        FileParameter aboutAttribute = (FileParameter) configuration
                .getAttribute("_about", FileParameter.class);

        URL baseURL = null;
        URL modelURL = null;

        // HyVisual, VisualSense: Search for models in the _about
        // attribute of the configuration.
        // If we don't have an _about, or _about returns
        // no models, then look in the complete demos location

        if (aboutAttribute != null) {
            baseURL = aboutAttribute.asURL();
            String aboutURLString = baseURL.toExternalForm();
            String base = aboutURLString.substring(0,
                    aboutURLString.lastIndexOf("/"));
            baseURL = ConfigurationApplication.specToURL(base + "/intro.htm");
            System.out
            .println("HTMLAbout._expandLibrary(): looking in about URL: "
                    + baseURL);
            List modelList = _getURLs(baseURL, regexp);
            if (modelList.size() > 0) {
                // Get the first model and open it
                String model = (String) modelList.get(0);
                System.out
                .println("HTMLAbout._expandLibrary(): looking for model relative to about URL: "
                        + model);
                modelURL = new URL(baseURL, model);
            } else {
                // Get the first url from intro.htm, look in it and get the
                // first model
                System.out
                .println("HTMLAbout._expandLibrary(): looking inside "
                        + baseURL + " for .htm files");
                List urlList = _getURLs(baseURL, ".*.htm");
                Iterator urls = urlList.iterator();
                while (urls.hasNext() && modelURL == null) {
                    // Looping through files, looking for a model
                    String model = (String) urls.next();
                    System.out
                    .println("HTMLAbout._expandLibrary(): looking inside "
                            + model);
                    URL possibleModelURL = new URL(baseURL, model);
                    modelList = _getURLs(possibleModelURL, regexp);
                    if (modelList.size() > 0) {
                        model = (String) modelList.get(0);
                        // Get the first model and open it
                        System.out
                        .println("HTMLAbout._expandLibrary(): looking for model relative to first URL: "
                                + model);
                        modelURL = new URL(baseURL, model);
                    }
                }
            }
        }
        if (modelURL == null) {
            // Get completeDemos.htm
            baseURL = _getDemoURL(null);
            System.out
            .println("HTMLAbout._expandLibrary(): looking in completeDemos URL: "
                    + baseURL);
            List modelList = _getURLs(baseURL, regexp);
            if (modelList.size() > 0) {
                // Get the first model and open it
                String model = (String) modelList.get(0);
                System.out
                .println("HTMLAbout._expandLibrary(): looking for model relative to completeDemos URL: "
                        + model);
                modelURL = new URL(baseURL, model);
            } else {
                String model = "http://ptolemy.eecs.berkeley.edu/ptolemyII/ptIIlatest/ptII/ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml";
                System.out
                .println("HTMLAbout._expandLibrary(): looking for model relative to completeDemos URL: "
                        + model);
                modelURL = new URL(model);
            }
        }

        System.out.println("HTMLAbout._expandLibrary(): baseURL: " + baseURL);
        System.out.println("HTMLAbout._expandLibrary(): modelURL: " + modelURL);
        Tableau tableau = configuration.openModel(baseURL, modelURL,
                modelURL.toExternalForm());
        final JFrame jFrame = tableau.getFrame();
        //jFrame.show();

        String errorMessage = "Expanding the library <b>should</b> result in expanding "
                + "everything in the left hand tree pane. "
                + "<p>If the left hand tree pane expands and then contracts, "
                + "there is a problem with one of the leaves of the tree "
                + "such as invoking a change request in an "
                + "<i>XXX</i>Icon.xml. "
                + "<p>The quickest way to find this is to restart vergil "
                + "and expand each branch in the tree by hand.";
        try {
            ((PtolemyFrame) jFrame).expandAllLibraryRows();
        } catch (Throwable throwable) {
            throw new IllegalActionException(tableau, throwable,
                    "Failed to expand library.\n" + errorMessage);
        }

        return _temporaryHTMLFile("expandLibrary", ".htm", errorMessage);

    }

    // Return the URL of the file that contains links to .xml files
    private static URL _getDemoURL(String demosFileName) throws IOException {
        // Open the completeDemos.htm file and read the contents into
        // a String
        if (demosFileName == null || demosFileName.length() == 0) {
            demosFileName = "ptolemy/configs/doc/completeDemos.htm";
        }

        URL url = null;
        try {
            url = ConfigurationApplication.specToURL(demosFileName);
        } catch (Exception ex) {
            System.out.println("Warning: " + demosFileName + " not found: "
                    + ex);
        }
        return url;
    }

    /** Open up a file, return a list of relative URLs that match a regexp.
     *  @param demosURL The URL of the file containing URLs.
     *  @param regexp The regular expression, for example ".*.xml$".
     *  @return a list of relative URLS
     */
    private static List _getURLs(URL demosURL, String regexp)
            throws IOException {
        // Return relative URLS
        return _getURLs(demosURL, regexp, false, 0);
    }

    /** Open up a file, return a list of relative or absolute URLs
     *  that match a regexp.
     *  @param demosURL The URL of the file containing URLs.
     *  @param regexp The regular expression, for example ".*.xml$".
     *  @param absoluteURLs True if we should return absolute URLs.
     *  @param depth Depth to recurse.  Depth of 0 do not recurse.
     *  Recursing only makes sense if the regexp argument includes .htm* files:
     *  ".*(.htm|.html|.xml)"
     *  @return a list of Strings naming absolute or relative URLs.
     */
    private static List _getURLs(URL demosURL, String regexp,
            boolean absoluteURLs, int depth) throws IOException {
        //System.out.println("HTMLAbout._getURLs(" + demosURL + ", " + regexp + ", " + absoluteURLs + ", " + depth);
        StringBuffer demosBuffer = new StringBuffer();
        BufferedReader in = null;
        String demosURLParent = demosURL.toString().substring(0,
                demosURL.toString().lastIndexOf("/") + 1);
        try {
            in = new BufferedReader(
                    new InputStreamReader(demosURL.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                demosBuffer.append(inputLine);
            }
        } catch (Exception ex) {
            System.out.println("HTMLAbout: failed to open " + demosURL + "\n"
                    + ex);
            return new LinkedList();
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // demos contains the contents of the html file that has
        // links to the demos we are interested in.
        String demos = demosBuffer.toString();

        // All the models we find go here.
        List modelList = new LinkedList();

        // Loop through the html file that contains links to the demos
        // and pull out all the links by looking for href=" and then
        // for the closing "
        int modelStartIndex = demos.indexOf("href=\"");

        while (modelStartIndex != -1) {
            int modelEndIndex = demos.indexOf("\"", modelStartIndex + 6);

            if (modelEndIndex != -1) {
                String modelLink = demos.substring(modelStartIndex + 6,
                        modelEndIndex);

                if (!modelLink.startsWith("http://")
                        && modelLink.matches(regexp)) {
                    // If the link does not start with http://, but ends
                    // with .xml, then we add it to the list
                    String model = modelLink;
                    //System.out.println("HTMLAbout: modelLink: " + modelLink);
                    if (absoluteURLs) {
                        model = demosURLParent + modelLink;
                        Exception ex1 = null;
                        try {
                            model = new URI(demosURLParent + modelLink)
                            .normalize().getPath();
                            // Under Windows, convert /C:/foo/bar to C:/foo/bar
                            model = new File(model).toString().replace('\\',
                                    '/');
                        } catch (URISyntaxException ex) {
                            ex1 = ex;
                        } catch (NullPointerException ex2) {
                            // model == null, probably a jar url in Webstart
                        }
                        if (model == null) {
                            try {
                                // Could be a jar url
                                model = ConfigurationApplication.specToURL(
                                        modelLink).toString();
                            } catch (Exception ex) {
                                if (modelLink.startsWith("/")) {
                                    modelLink = modelLink.substring(1);
                                    try {
                                        model = ConfigurationApplication
                                                .specToURL(modelLink)
                                                .toString();
                                    } catch (Exception ex2) {
                                        System.out.println("Failed to look up "
                                                + demosURLParent + modelLink
                                                + " and " + modelLink + "\n"
                                                + ex2);
                                    }
                                } else {
                                    String absoluteModelLink = demosURLParent
                                            + modelLink;
                                    try {
                                        model = ConfigurationApplication
                                                .specToURL(absoluteModelLink)
                                                .toString();
                                    } catch (Exception ex3) {
                                        System.out.println("Failed to look up "
                                                + demosURLParent + modelLink
                                                + " and " + modelLink + " and "
                                                + absoluteModelLink + "\n"
                                                + ex1 + "\n" + ex3);
                                    }

                                }
                            }
                        }
                    }
                    if (model != null) {
                        URL modelURL = null;
                        if (model.startsWith("jar:file:/")) {
                            modelURL = new URL(model);
                            //System.out.print("HTMLAbout._getURLs(): jar:file:/: " + model);
                        } else {
                            if (model.startsWith("file:/")) {
                                model = model.substring("file:/".length());
                            }
                            //System.out.print("HTMLAbout._getURLs(): file:/: " + model);
                            modelURL = new File(model).toURI().toURL();
                        }
                        //System.out.println(" " + modelURL);
                        boolean sawModel = modelList.contains(model);
                        if (!sawModel) {
                            modelList.add(model);
                            if (depth > 0 && model.matches(".*(.htm|.html)")) {
                                modelList.addAll(_getURLs(modelURL, regexp,
                                        absoluteURLs, depth - 1));
                            }
                        }
                    }
                }
            }

            modelStartIndex = demos.indexOf("href=\"", modelEndIndex);
        }

        return modelList;
    }

    // Return true if a configuration can be found
    // @param configurationName The name of the configuration, for
    // example "full"
    // @return True if ptolemy/configs/<i>configuration</i>/configuration.xml
    // can be found
    private static boolean _configurationExists(String configurationName) {
        boolean configurationExists = false;

        try {
            URL url = Thread
                    .currentThread()
                    .getContextClassLoader()
                    .getResource(
                            "ptolemy/configs/" + configurationName
                            + "/configuration.xml");

            if (url != null) {
                configurationExists = true;
            }
        } catch (Throwable throwable) {
            // Ignored, the configuration does not exist.
        }

        return configurationExists;
    }

    // Save a string in a temporary html file and return a URL to it.
    // @param prefix The prefix string to be used in generating the temporary
    // file name; must be at least three characters long.
    // @param suffix The suffix string to be used in generating the temporary
    // file name.
    // @param contents  The contents of the temporary file
    // @return A URL pointing to a temporary file.
    /*private*/static URL _temporaryHTMLFile(String prefix, String suffix,
            String contents) throws IOException {
        // Generate a copyright page in a temporary file
        File temporaryFile = File.createTempFile(prefix, suffix);
        temporaryFile.deleteOnExit();

        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(temporaryFile);
            fileWriter.write(contents, 0, contents.length());
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }

        return temporaryFile.toURI().toURL();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static List _demosURLs;
}
