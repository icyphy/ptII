/* Utility methods to handle HTML Viewer about: calls

Copyright (c) 2003-2005 The Regents of the University of California.
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
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.HyperlinkEvent;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.StringAttribute;


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
     *  then that attributed is used as the name of the application
     *  in the generated text.  If _applicationName is not present,
     *  then the default name is "Ptolemy II".
     *
     *  @param configuration The configuration to look for the
     *  _applicationName attribute in
     */
    public static String about(Configuration configuration) {
        // Use an explicit version here - the name of the whatsNew file
        // does not changes as quickly as the version.
        String version = "5.0";

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
                + "missing classes).\n" + "<li><a href=\"about:copyright\">"
                + "<code>about:copyright</code></a> "
                + " Display information about the copyrights.\n");

        htmlBuffer.append("</ul>\n<table>\n");

        if (_configurationExists("full")) {
            htmlBuffer.append(
                    "<tr rowspan=4><center><b>Full</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/doc/completeDemos.htm")
                    + _aboutHTML("ptolemy/configs/doc/demos.htm")
                    + _aboutHTML("ptolemy/configs/doc/whatsNew" + version + ".htm")
                    + _aboutHTML("ptolemy/configs/doc/whatsNew4.0.htm")
                    + _aboutHTML("ptolemy/configs/doc/whatsNew3.0.2.htm"));
        }

        // Don't include DSP here, it uses the Ptiny demos anyway.
        if (_configurationExists("hyvisual")) {
            htmlBuffer.append(
                    "<tr rowspan=4><center><b>HyVisual</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/hyvisual/intro.htm"));
        }

        if (_configurationExists("ptiny")) {
            htmlBuffer.append(
                    "<tr rowspan=4><center><b>Ptiny</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/doc/completeDemosPtiny.htm")
                    + _aboutHTML("ptolemy/configs/doc/demosPtiny.htm"));
        }

        if (_configurationExists("visualsense")) {
            htmlBuffer.append(
                    "<tr rowspan=4><center><b>VisualSense</b></center></tr>\n"
                    + _aboutHTML("ptolemy/configs/visualsense/intro.htm"));
        }

        htmlBuffer.append("</table>\n");

        htmlBuffer.append("</body>\n</html>\n");
        return htmlBuffer.toString();
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
     */
    public static URL generateLinks(String demosFileName, String regexp,
            Configuration configuration) throws Exception {
        URL demosURL = _getDemoURL(demosFileName);
        List modelList = _getURLs(demosURL, regexp);
        Iterator models = modelList.iterator();

        while (models.hasNext()) {
            String model = (String) models.next();
            URL modelURL = new URL(demosURL, model);

            try {
                configuration.openModel(demosURL, modelURL,
                        modelURL.toExternalForm());
            } catch (Throwable throwable) {
                throw new Exception("Failed to open '" + modelURL + "'",
                        throwable);
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
        } else if (event.getDescription().equals("about:configuration")) {
            // about:expandConfiguration will expand the configuration
            // and report any problems such as missing classes.
            // Open up the configuration as a .txt file because if
            // we open it up as a .xml file, we get a graphical browser
            // that does not tell us much.  If we open it up as a .htm,
            // then the output is confusing.
            newURL = _temporaryHTMLFile("configuration", ".txt",
                    configuration.exportMoML());
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
                    ".*(.htm|.html|.pdf|.xml)", configuration);
        } else if (event.getDescription().startsWith("about:runAllDemos")) {
            URI aboutURI = new URI(event.getDescription());
            newURL = runAllDemos(aboutURI.getFragment(), configuration);
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
            Configuration configuration) throws Exception {
        URL demosURL = _getDemoURL(demosFileName);
        List modelList = _getURLs(demosURL, ".*.xml$");
        Iterator models = modelList.iterator();

        while (models.hasNext()) {
            String model = (String) models.next();
            URL modelURL = new URL(demosURL, model);
            System.out.println("Model: " + modelURL);

            Tableau tableau = configuration.openModel(demosURL, modelURL,
                    modelURL.toExternalForm());

            if (((Effigy) tableau.getContainer()) instanceof PtolemyEffigy) {
                PtolemyEffigy effigy = (PtolemyEffigy) (tableau.getContainer());
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Return a string containing HTML with links the the about:demos
    // and about:links pages
    private static String _aboutHTML(String fileName) {
        return "  <tr>\n" + "    <code>" + fileName + "</code>\n"
            + "    <td><a href=\"about:demos#" + fileName
            + "\">&nbsp;Open the .xml&nbsp;</a></td>\n"
            + "    <td><a href=\"about:links#" + fileName
            + "\">&nbsp;Open the .htm, .html, .xml and .pdf&nbsp;</a></td>\n"
            // RunAllDemos does not work, it runs in the wrong thread?
            // + "    <td><a href=\"about:runAllDemos#" + fileName
            // + "\">&nbsp;Run all demos&nbsp;</a></td>\n"
            + "  </tr>\n";
    }

    // Return the URL of the file that contains links to .xml files
    private static URL _getDemoURL(String demosFileName)
            throws IOException {
        // Open the completeDemos.htm file and read the contents into
        // a String
        if ((demosFileName == null) || (demosFileName.length() == 0)) {
            demosFileName = "ptolemy/configs/doc/completeDemos.htm";
        }

        return MoMLApplication.specToURL(demosFileName);
    }

    private static List _getURLs(URL demosURL, String regexp)
            throws IOException {
        StringBuffer demosBuffer = new StringBuffer();
    	BufferedReader in = null;
    	try {
    		in = new BufferedReader(new InputStreamReader(
    				
    				demosURL.openStream()));
    		
    		String inputLine;
    		
    		while ((inputLine = in.readLine()) != null) {
    			demosBuffer.append(inputLine);
    		}
    	} finally {
    		if (in != null){
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
        boolean configurationExists = false;

        try {
            URL url = Thread.currentThread().getContextClassLoader()
                .getResource("ptolemy/configs/" + configurationName
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
    private static URL _temporaryHTMLFile(String prefix, String suffix,
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
        return temporaryFile.toURL();
    }
}
