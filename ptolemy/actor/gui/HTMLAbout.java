/* Utility methods to handle HTML Viewer about: calls

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.HyperlinkEvent;


//////////////////////////////////////////////////////////////////////////
//// HTMLAbout
/**
This class contains static methods that are called
by when HTMLViewer.hyperlinkUpdate() is invoked on a hyperlink
that starts with <code>about:</code>.  This facility is primarily
used for testing.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 3.0
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
     *  then that attributed is used as the name of the application
     *  in the generated text.  If _applicationName is not present,
     *  then the default name is "Ptolemy II".
     *
     *  @param configuration The configuration to look for the
     *  _applicationName attribute in
     */   
    public static String about(Configuration configuration) {
        String version = VersionAttribute.CURRENT_VERSION.getExpression();

        String applicationName = "Ptolemy II";
        try {
            StringAttribute applicationNameAttribute =
                (StringAttribute) configuration.getAttribute(
                        "_applicationName",
                        StringAttribute.class);
            if (applicationNameAttribute != null) {
                applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Exception ex) {
            // Ignore and use the default applicationName
        }
        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append(
                "<html><head><title>About " + applicationName
                + "</title></head>"
                + "<body><h1>About " + applicationName + "</h1>\n"
                + "The HTML Viewer in " + applicationName
                + " handles the <code>about:</code>\n"
                + "tag specially.\n"
                + "<br>The following urls are handled:\n"
                + "<ul>\n"
                + "<li><a href=\"about:configuration\">"
                + "<code>about:configuration</code></a> "
                + "Expand the configuration (good way to test for "
                + "missing classes).\n"
                + "<li><a href=\"about:copyright\">"
                + "<code>about:copyright</code></a> "
                + " Display information about the copyrights.\n");

        // set needCloser to true if we found a configuration and need
        // to close the html tag.
        boolean needCloser = false;
        if (_configurationExists("full")) {
            needCloser = true;
            htmlBuffer.append(
                    "<li>Full\n"
                    + " <ul>\n"
                    + "  <li><a href=\"about:demos\"><code>about:demos</code></a>"
                    + "   Open up all the .xml in"
                    + "   <code>ptolemy/configs/doc/completeDemos.htm</code>.\n"
                    + "   (May fail in Ptiny, DSP, HyVisual or VisualSense configurations).\n"
                    + "  <li><a href=\"about:demos#ptolemy/configs/doc/demos.htm\">"
                    + "   <code>about:demos#ptolemy/configs/doc/demos.htm</code></a>\n"
                    + "   Open up the .xml files in\n"
                    + "   <code>ptolemy/configs/doc/demos.htm</code>.\n"
                    + "  <li><a href=\"about:demos#ptolemy/configs/doc/whatsNew"
                    + version + ".htm\">"
                    + "   <code>about:demos#ptolemy/configs/doc/whatsNew"
                    + version + ".htm</code></a>\n"
                    + "   Open up the .xml files in\n"
                    + "   <code>ptolemy/configs/doc/whatsNew"
                    + version + ".htm</code>.\n"
                    + "  <li><a href=\"about:demos#ptolemy/configs/doc/"
                    + "whatsNew3.0.2.htm\">"
                    + "   <code>about:demos#ptolemy/configs/doc/"
                    + "whatsNew3.0.2.htm</code></a>\n"
                    + "   Open up the .xml files in\n"
                    + "   <code>ptolemy/configs/doc/whatsNew3.0.2.htm</code>.\n"
                    + " </ul>\n");
        }

        // Don't include DSP here, it uses the Ptiny demos anyway.

        if (_configurationExists("hyvisual")) {
            needCloser = true;
            htmlBuffer.append(
                    "<li>HyVisual\n"
                    + " <ul>\n"
                    + "  <li><a href=\"about:demos#ptolemy/configs/hyvisual/intro.htm\">"
                    + "   <code>about:demos#ptolemy/configs/hyvisual/intro.htm</code></a>"
                    + "   \nOpen up the .xml files in\n"
                    + "   <code>ptolemy/configs/hyvisual/intro.htm</code>.\n"
                    + " </ul>\n");
        }           

        if (_configurationExists("ptiny")) {
            needCloser = true;
            htmlBuffer.append(
                    "<li>Ptiny\n"
                    + " <ul>\n"
                    + "  <li><a href=\"about:demos#ptolemy/configs/doc/completeDemosPtiny.htm\">"
                    + "   <code>about:demos#ptolemy/configs/doc/completeDemosPtiny.htm</code></a>"
                    + "   \nOpen up the .xml files in\n"
                    + "   <code>ptolemy/configs/doc/completeDemosPtiny.htm</code>.\n"
                    
                    + "  <li><a href=\"about:demos#ptolemy/configs/doc/demosPtiny.htm\">"
                    + "   <code>about:demos#ptolemy/configs/doc/demosPtiny.htm</code></a>"
                    + "   \nOpen up the .xml files in\n"
                    + "   <code>ptolemy/configs/doc/demosPtiny.htm</code>.\n"
                    + " </ul>\n");
        } 

//             + "<li><a href=\"about:runAllDemos\"><code>about:runAllDemos</code></a>"
//             + "Run all the demonstrations.\n"
//             + "<li><a href=\"about:runAllDemos#ptolemy/configs/doc/demosPtiny.htm\">"
//             + "<code>about:runAllDemosdemos#ptolemy/configs/doc/demosPtiny.htm</code></a>"
//             + "\nRun all the .xml files in\n"
//             + "<code>ptolemy/configs/doc/completeDemosPtiny.htm</code>.\n"

        if (_configurationExists("visualsense")) {
            needCloser = true;
            htmlBuffer.append(
                    "<li>VisualSense\n"
                    + " <ul>\n"
                    + "  <li><a href=\"about:demos#ptolemy/configs/visualsense/intro.htm\">"
                    + "   <code>about:demos#ptolemy/configs/visualsense/intro.htm</code></a>"
                    + "   \nOpen up the .xml files in\n"
                    + "   <code>ptolemy/configs/visualsense/intro.htm</code>.\n"
                    + " </ul>\n");
        }           

        if (needCloser) {
            htmlBuffer.append("</ul>\n");
        }

        htmlBuffer.append("</body>\n</html>\n");
        return htmlBuffer.toString();
    }

    /** Call Configuration.openModel() on all the local .xml files that
     *  are linked to from an HTML file.
     *  @param demosFileName The name of the HTML file that contains links
     *  to the .xml files.  If this argument is the empty string, then
     *  "ptolemy/configs/doc/completeDemos.htm" is used.
     *  @param configuration  The configuration to open the files in.
     *  @return the URL of the HTML file that was searched. 
     */   
    public static URL demos(String demosFileName, Configuration configuration)
            throws Exception {

        URL demosURL = _getDemoURL(demosFileName);
        List modelList = _getModelURLs(demosURL);
        Iterator models = modelList.iterator();
        while (models.hasNext()) {
            String model = (String)models.next();
            URL modelURL = new URL(demosURL, model);
            try {
                configuration.openModel(demosURL, modelURL,
                        modelURL.toExternalForm());
            } catch (Throwable throwable) {
                throw new Exception("Failed to open '" + modelURL
                        + "'", throwable);
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
        if (event.getDescription().equals("about:copyright")) {
            // Note that if we have a link that is
            // <a href="about:copyright">about:copyright</a>
            // then event.getURL() will return null, so we have
            // to use getDescription()
            newURL = _temporaryHTMLFile("copyright", ".htm",
                    GenerateCopyrights.generateHTML(configuration));
        } else if (event.getDescription()
                .equals("about:configuration")) {
            // about:expandConfiguration will expand the configuration
            // and report any problems such as missing classes.

            // Open up the configuration as a .txt file because if
            // we open it up as a .xml file, we get a graphical browser
            // that does not tell us much.  If we open it up as a .htm,
            // then the output is confusing.
            newURL = _temporaryHTMLFile("configuration", ".txt",
                    configuration.exportMoML());
        } else if (event.getDescription()
                .startsWith("about:demos")) {
            // Expand all the local .xml files in the fragment
            // and return a URL pointing to the fragment.
            // If there is no fragment, then use
            // "ptolemy/configs/doc/completeDemos.htm"
            URI aboutURI = new URI(event.getDescription());
            newURL = demos(aboutURI.getFragment(), configuration);
        } else if (event.getDescription()
                .startsWith("about:runAllDemos")) {
            URI aboutURI = new URI(event.getDescription());
            newURL = runAllDemos(aboutURI.getFragment(),
                    configuration);
        } else {
            // Display a message about the about: facility 
            newURL = _temporaryHTMLFile("about", ".htm", about(configuration));
        }
        return newURL;
    }

    /** Run all the local .xml files that are linked to from an HTML file.
     *  @param demosFileName The name of the HTML file that contains links
     *  to the .xml files.  If this argument is the empty string, then
     *  "ptolemy/configs/doc/completeDemos.htm" is used.
     *  @param configuration  The configuration to run the files in.
     *  @return the URL of the HTML file that was searched. 
     */   
    public static URL runAllDemos(String demosFileName,
            Configuration configuration)
            throws Exception {

        URL demosURL = _getDemoURL(demosFileName);
        List modelList = _getModelURLs(demosURL);
        Iterator models = modelList.iterator();
        while (models.hasNext()) {
            String model = (String)models.next();
            URL modelURL = new URL(demosURL, model);
            System.out.println("Model: " + modelURL);
            Tableau tableau = configuration.openModel(demosURL, modelURL,
                    modelURL.toExternalForm());
            if ( ((Effigy)tableau.getContainer()) instanceof PtolemyEffigy) {
                PtolemyEffigy effigy = (PtolemyEffigy)(tableau.getContainer());
                CompositeActor actor = (CompositeActor)effigy.getModel();
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
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return the URL of the file that contains links to .xml files
    private static URL _getDemoURL(String demosFileName) throws IOException {
        // Open the completeDemos.htm file and read the contents into
        // a String
        if (demosFileName == null || demosFileName.length() == 0) {
            demosFileName = "ptolemy/configs/doc/completeDemos.htm";
        }
        return MoMLApplication.specToURL(demosFileName);
    }



    // Return a list of URLs for local .xml files linked to in demosURL.
    private static List  _getModelURLs(URL demosURL) throws IOException {

	BufferedReader in = new BufferedReader(
				new InputStreamReader(
				demosURL.openStream()));

        StringBuffer demosBuffer = new StringBuffer();

	String inputLine;
	while ((inputLine = in.readLine()) != null) {
            demosBuffer.append(inputLine);
        }
	in.close();       

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
                String modelLink =
                    demos.substring(modelStartIndex + 6, modelEndIndex);
                if (!modelLink.startsWith("http://")
                        && modelLink.endsWith(".xml")) {
                    // If the link does not start with http://, but ends
                    // with .xml, then we add it to the list
                    modelList.add(modelLink);
                }
            }
            modelStartIndex = demos.indexOf("href=\"", modelEndIndex);
        } 
        return modelList;
    }

    // Return true if a configuration can be found
    // @param configurationName The name of the configuration, for
    // example "full"
    // @returns True if ptolemy/configs/<i>configuration</i>/configuration.xml
    // can be found
    private static boolean _configurationExists(String configurationName) {
        boolean configurationExists  = false;
        try {
            URL url = Thread.currentThread()
                .getContextClassLoader().getResource(
                        "ptolemy/configs/" + configurationName 
                        + "/configuration.xml");
            if (url != null) {
                configurationExists = true;
            }
        } catch (Exception ex) {
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
    private static URL _temporaryHTMLFile(String prefix, 
            String suffix, String contents) 
            throws IOException {
        // Generate a copyright page in a temporary file
        File temporaryFile = File.createTempFile(
                prefix, suffix);
        temporaryFile.deleteOnExit();

        FileWriter fileWriter = new FileWriter(temporaryFile);
        fileWriter.write(contents, 0 , contents.length());
        fileWriter.close();
        return temporaryFile.toURL();
    }
}
