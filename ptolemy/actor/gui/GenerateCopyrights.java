/* Generate a web page that contains links for the appropriate copyrights

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// GenerateCopyrights

/**
   Generate an HTML file that contains links to the appropriate
   copyrights for entities in the configuration.
   This class looks for particular classes, and if the class is found
   in the classpath, then a corresponding html file is included
   in the list of copyrights.

   @author Christopher Hylands
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class GenerateCopyrights {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate HTML about the copyrights for classes that might
     *  be present in the configuration.  This method contains
     *  a list of classes and corresponding copyrights.  If
     *  a class in the list is present, then we generate html
     *  that contains a link to the copyright.  Note that if the
     *  copyright file need not be present on the local machine,
     *  we generate a link to the copy on the Ptolemy website.
     *
     *  <p>If the configuration contains an _applicationName attribute
     *  then that attributed is used as the name of the application
     *  in the generated text.  If _applicationName is not present,
     *  then the default name is "Ptolemy II".
     *
     *  <p>If the configuration contains an _applicationCopyright
     *  StringAttribute, then the value of that attributed is used
     *  as the location of the copyright html file.  If
     *  _applicationCopyright is not present, then
     *  "ptolemy/configs/doc/copyright.htm" is used.
     *
     *  <p>If the configuration has a parameter called
     *  _applicationCopyrights that is an array of records where
     *  each element is a record
     *  <pre>
     *  {actor="ptolemy.actor.lib.Foo", copyright="foo.htm"}
     *  </pre>
     *  then we add that actor/copyright pair to the list of potential
     *  copyrights.
     *
     *  @param configuration The configuration to look for the
     *  _applicationName and _applicationCopyright attributes in.
     *  @return A String containing HTML that describes what
     *  copyrights are used by Entities in the configuration
     */
    public static String generateHTML(Configuration configuration) {
        // A Map of copyrights, where the key is a URL naming
        // the copyright and the value is a List of entities
        // that use that as a copyright.
        HashMap<String, Set<String>> copyrightsMap = new HashMap<String, Set<String>>();

        // Add the classnames and copyrights.
        // Alphabetical by className.
        _addIfPresent(copyrightsMap, "com.cureos.numerics.Cobyla",
                "com/cureos/jcobyla-license.htm");

        _addIfPresent(copyrightsMap, "com.github.ojdcheck.OpenJavaDocCheck",
                "lib/ojdcheck-license.htm");

        _addIfPresent(copyrightsMap, "com.sun.jna.Pointer",
                "lib/jna-license.htm");

        _addIfPresent(copyrightsMap, "com.sleepycat.db.Environment",
                "ptdb/lib/db-dbxml-license.htm");

        _addIfPresent(copyrightsMap, "diva.gui.ExtensionFileFilter",
                "diva/gui/ExtensionFileFilter-license.htm");

        _addIfPresent(copyrightsMap, "com.google.protobuf.MessageOrBuilder",
                "lib/protobuf-license.htm");

        _addIfPresent(copyrightsMap, "g4ltl.SolverUtility",
                "lib/g4ltl-license.htm");

        _addIfPresent(copyrightsMap, "hla.rti.jlc.RtiFactory",
                "lib/jcerti-license.htm");

        _addIfPresent(copyrightsMap, "interfaces.util.ChicUI",
                "lib/chic-license.htm");

        _addIfPresent(copyrightsMap, "io.socket.SocketIO", "lib/socketio-license.htm");

        _addIfPresent(copyrightsMap, "javax.servlet.http.HttpServlet",
                "lib/javax.servlet-api-license.htm");

        _addIfPresent(copyrightsMap, "jni.GenericJNIActor",
                "jni/launcher/launcher-copyright.htm");

        _addIfPresent(copyrightsMap, "mescal.domains.mescalPE.kernel.parser",
                "mescal/configs/doc/cup-copyright.htm");

        _addIfPresent(copyrightsMap,
                "net.jimblackler.Utils.YieldAdapterIterator",
                "net/jimblackler/Utils/jimblacklerUtils-license.htm");

        _addIfPresent(copyrightsMap, "oscP5.OscP5",
                "lib/oscP5-license.htm");

        _addIfPresent(copyrightsMap, "org.apache.oltu.oauth2.common.OAuth",
                "lib/org.apache.oltu.oauth2-license.htm");

        _addIfPresent(copyrightsMap, "org.eclipse.jetty.server.Server",
                "lib/jetty-all-license.htm");

        _addIfPresent(copyrightsMap, "org.json.JSONObject",
                "org/json/json-license.htm");

        _addIfPresent(copyrightsMap, "org.jsoup.parser.Parser",
                "lib/jsoup-license.htm");

        _addIfPresent(copyrightsMap, "org.mozilla.javascript.Context",
                "lib/js-license.htm");

        _addIfPresent(copyrightsMap, "org.junit.runner.JUnitCore",
                "lib/junit-license.htm");

        _addIfPresent(copyrightsMap, "org.netbeans.api.visual.widget.Scene",
                "lib/netbeans-visual-library-license.htm");

        _addIfPresent(copyrightsMap, "org.ptolemy.fmi.driver.OutputRow",
                "org/ptolemy/fmi/driver/fmusdk-license.htm");

        _addIfPresent(copyrightsMap,
                "org.satlive.jsat.objects.ExternalLiteral",
                "mescal/configs/doc/jsat-copyright.htm");

        // Vertx
        _addIfPresent(copyrightsMap, "com.fasterxml.jackson.core.base.GeneratorBase",
                "lib/jackson-2.2.2-license.htm");

        _addIfPresent(copyrightsMap, "io.netty.handler.traffic.ChannelTrafficShapingHandler",
                "lib/netty-all-4.0.20.Final-license.htm");

        _addIfPresent(copyrightsMap, "org.vertx.java.spi.cluster.impl.hazelcast.HazelcastAsyncMap",
                "lib/hazelcast-3.2.3-license.htm");

        _addIfPresent(copyrightsMap, "org.vertx.java.core.Vertx",
                "lib/vertx-2.1.2-license.htm");


        _addIfPresent(copyrightsMap, "ptolemy.actor.gui.BrowserLauncher",
                "ptolemy/actor/gui/BrowserLauncher-license.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.gui.run.PtolemyFormEditor",
                "com/jgoodies/jgoodies-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.gui.run.RunLayoutFrame",
                "org/mlc/mlc-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.colt.ColtRandomSource",
                "ptolemy/actor/lib/colt/colt-copyright.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.database.DatabaseManager",
                "com.mysql.jdbc.Driver",
                "ptolemy/actor/lib/database/mysql-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.excel.Excel",
                "ptolemy/actor/lib/excel/jxl-copyright.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.fmi.fmipp.FMUModelExchange",
                "ptolemy/actor/lib/fmi/fmipp/fmipp-license.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.io.comm.SerialComm",
                "ptolemy/actor/lib/io/comm/rxtx-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.jai.JAIImageToken",
                "ptolemy/actor/lib/jai/jai-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.jmf.JMFImageToken",
                "ptolemy/actor/lib/jmf/jmf-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.joystick.Joystick",
                "ptolemy/actor/lib/joystick/joystick-copyright.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.logic.fuzzy.FuzzyLogic",
                "ptolemy/actor/lib/logic/fuzzy/FuzzyEngine-copyright.htm");
        _addIfPresent(copyrightsMap, "javax.mail.Address",
                "lib/javamail-license.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.opencv.OpenCVReader",
                "ptolemy/actor/lib/opencv/opencv-copyright.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.opencv.javacv.CameraReader",
                "ptolemy/actor/lib/opencv/javacv/javacv-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.python.PythonScript",
                "ptolemy/actor/lib/python/jython-license.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.lib.x10.X10Interface",
                "ptolemy/actor/lib/x10/x10-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.actor.ptalon.PtalonActor",
                "ptolemy/actor/ptalon/ptalon-copyright.html");

        _addIfPresent(copyrightsMap,
                "ptolemy.backtrack.eclipse.ast.TypeAnalyzer",
                "ptolemy/backtrack/eclipse/ast/eclipse-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.backtrack.util.ClassFileLoader",
                "ptolemy/backtrack/util/gcj-copyright.html");

        _addIfPresent(copyrightsMap,
                "ptolemy.caltrop.actors.AbstractCalInterpreter",
                "ptolemy/caltrop/cup-copyright.htm");
        _addIfPresent(copyrightsMap, "ptolemy.caltrop.actors.CalInterpreter",
                "ptolemy/caltrop/saxon-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.copernicus.kernel.KernelMain",
                "ptolemy/copernicus/kernel/soot-copyright.html");

        _addIfPresent(copyrightsMap, "ptolemy.data.ontologies.Concept",
                "ptolemy/data/ontologies/doc/udunits2Database/udunits-license.htm");

        _addIfPresent(copyrightsMap, "ptolemy.domains.gr.kernel.GRActor",
                "ptolemy/domains/gr/lib/java3d-copyright.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.domains.gr.lib.quicktime.MovieViewScreen2D",
                "ptolemy/domains/gr/lib/quicktime/quicktime-copyright.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.domains.gr.lib.vr.GRTexture2D.java",
                "ptolemy/domains/gr/lib/vr/vr-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.domains.gro.kernel.GRODirector",
                "ptolemy/domains/gro/JOGL-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.domains.psdf.kernel.PSDFScheduler",
                "ptolemy/domains/psdf/mapss-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.domains.ptinyos.util.nc2moml.MoMLLib",
                "ptolemy/domains/ptinyos/lib/jdom-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.gui.ExtensionFilenameFilter",
                "ptolemy/gui/ExtensionFilenameFilter-license.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.homer.widgets.ResizableImageWidget",
                "ptolemy/homer/widgets/ResizableImageWidget-license.htm");

        _addIfPresent(copyrightsMap, "ptolemy.matlab.Expression",
                "ptolemy/matlab/matlab-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.media.Audio",
                "ptolemy/media/Audio-license.htm");

        _addIfPresent(copyrightsMap, "ptolemy.util.test.junit.TclTests",
                "lib/JUnitParams-license.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.export.html.ExportHTMLAction",
                "ptolemy/vergil/basic/export/html/javascript/javascript-license.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.export.html.ExportToWeb",
                "ptolemy/vergil/basic/export/html/javascript/fancybox/fancybox-license.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.export.itextpdf.ExportPDFAction",
                "ptolemy/vergil/basic/export/itextpdf/itextpdf-copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.layout.kieler.KielerLayout",
                "lib/guava-license.htm");
        _addIfPresent(copyrightsMap,
                "ptolemy.vergil.basic.layout.kieler.KielerGraphUtil",
                "ptolemy/vergil/basic/layout/kieler/kieler-copyright.htm");

        _addIfPresent(copyrightsMap, "ptolemy.vergil.pdfrenderer.PDFAttribute",
                "ptolemy/vergil/pdfrenderer/PDFRenderer-copyright.htm");

        _addIfPresent(copyrightsMap, "tcl.lang.Shell", "lib/ptjacl-license.htm");

        _addIfPresent(copyrightsMap, "thales.vergil.SingleWindowApplication",
                "thales/thalesSingleWindow-license.htm");

        _addIfPresent(copyrightsMap, "org.jivesoftware.smack.XMPPConnection",
                "lib/smack-license.htm");

        // Check for the _applicationCopyrights parameter
        try {
            Parameter applicationCopyrights = (Parameter) configuration
                    .getAttribute("_applicationCopyrights", Parameter.class);

            if (applicationCopyrights != null) {
                ArrayToken copyrightTokens = (ArrayToken) applicationCopyrights
                        .getToken();

                for (int i = 0; i < copyrightTokens.length(); i++) {
                    StringToken actorToken = (StringToken) ((RecordToken) copyrightTokens
                            .getElement(i)).get("actor");
                    StringToken copyrightToken = (StringToken) ((RecordToken) copyrightTokens
                            .getElement(i)).get("copyright");
                    _addIfPresent(copyrightsMap, actorToken.stringValue(),
                            copyrightToken.stringValue());
                }
            }
        } catch (Exception ex) {
            // This application has no _applicationCopyrights
        }

        // We also create a file that contains all the copyrights.
        StringBuffer masterCopyrightBuffer = new StringBuffer();

        StringBuffer masterCopyrightTable = new StringBuffer();

        // Read in the header
        String copyrightStartFileName = "$CLASSPATH/ptolemy/actor/gui/masterCopyrightStart.htm.in";
        try {
            // Probably wrong to read it as bytes and convert to a
            // string, but we have a convenient method, so we use it.
            masterCopyrightBuffer.append(new String(FileUtilities
                    .binaryReadURLToByteArray(FileUtilities.nameToURL(
                            copyrightStartFileName, null, null))));
        } catch (IOException ex) {
            // Print only a message, we want to print the copyrights
            System.err.println("Could not read " + copyrightStartFileName
                    + ": " + ex);
        }

        StringBuffer htmlBuffer = new StringBuffer(
                generatePrimaryCopyrightHTML(configuration));

        // Sort by the copyright file name, which should start with the package name.
        FileNameComparator fileNameComparator = new FileNameComparator();
        TreeMap<String, Set<String>> sortedCopyrightsMap = new TreeMap<String, Set<String>>(
                fileNameComparator);

        sortedCopyrightsMap.putAll(copyrightsMap);

        if (copyrightsMap.size() != sortedCopyrightsMap.size()) {
            // Print only a message, we want to print the copyrights.
            System.err
            .println("GenerateCopyrights: the size of the unsorted copyright table "
                    + "and the sorted copyright table are not the same?  Perhaps there are two "
                    + "or more copyrights with the same file name? (foo/copyright.htm and "
                    + "bar/copyright.htm will not work.)");

        }
        Iterator copyrights = sortedCopyrightsMap.entrySet().iterator();
        if (copyrights.hasNext()) {
            // DSP configuration might not include other actors.
            htmlBuffer
            .append("<p>Below we list features and the "
                    + "corresponding copyright "
                    + " of the package that is used.  If a feature is not "
                    + "listed below, then the "
                    + _getApplicationName(configuration)
                    + " copyright is the "
                    + "only copyright."
                    + "<table>\n"
                    + "  <tr>\n"
                    + "      <th>Copyright of package used by the feature</th>\n"
                    + "      <th>Feature</th>\n" + "  </tr>\n");

            while (copyrights.hasNext()) {
                Map.Entry entry = (Map.Entry) copyrights.next();
                String copyrightURL = (String) entry.getKey();
                Set entitiesSet = (Set) entry.getValue();

                StringBuffer entityBuffer = new StringBuffer();
                Iterator entities = entitiesSet.iterator();

                while (entities.hasNext()) {
                    if (entityBuffer.length() > 0) {
                        entityBuffer.append(", ");
                    }

                    String entityClassName = (String) entities.next();

                    // If we have javadoc, link to it.
                    // Assuming that entityClassName contains a dot separated
                    // classpath here.
                    String docName = "doc.codeDoc." + entityClassName;
                    String codeDoc = _findURL(docName.replace('.', '/')
                            + ".html");
                    entityBuffer.append("<a href=\"" + codeDoc + "\">"
                            + entityClassName + "</a>");
                }

                //System.out.println("GenerateCopyrights: url: " + copyrightURL);
                String foundCopyright = _findURL(copyrightURL);

                if (copyrightURL.contains("backtrack") && !copyrightURL.contains("http")) {
                    // If the backtrack copyright is not local, then don't print it.
                    continue;
                } 
                htmlBuffer.append("<tr>\n" + "  <td> <a href=\""
                        + foundCopyright + "\"><code>"
                        + _canonicalizeURLToPTII(foundCopyright)
                        + "</code></a></td>\n" + "  <td>" + entityBuffer
                        + "</td>\n" + "</tr>\n");
                try {
                    String copyright = new String(
                            FileUtilities.binaryReadURLToByteArray(new URL(
                                    foundCopyright)));

                    // Append the text between the body tags.

                    // Look for <body> or </head>
                    int startIndex = 0;
                    if ((startIndex = copyright.indexOf("<body>")) == -1) {
                        if ((startIndex = copyright.indexOf("</head>")) != -1) {
                            startIndex += "</head>".length();
                        } else {
                            // Print only a message, we want to print the copyrights.
                            System.out
                            .println("Could not find body or head in "
                                    + foundCopyright + " "
                                    + copyright.substring(0, 200));
                            startIndex = 0;
                        }
                    } else {
                        startIndex += "<body>".length();
                    }

                    // Look for </body> or </html>
                    int endIndex = 0;
                    if ((endIndex = copyright.indexOf("</body>")) == -1) {
                        if ((endIndex = copyright.indexOf("</html>")) == -1) {
                            endIndex = copyright.length();
                        }
                    }

                    // Get read of the html head and close body.
                    copyright = copyright.substring(startIndex, endIndex);

                    // If there is a <h1> tag, make sure that it has <a name=...
                    int hIndex = 0;
                    if ((hIndex = copyright.indexOf("<h1>")) == -1) {
                        if ((hIndex = copyright.indexOf("<h2>")) == -1) {
                            // Print only a message, we want to print the copyrights.
                            System.out.println("Warning, no h1 or h2 in "
                                    + foundCopyright);
                        } else {
                        }
                    }
                    if (hIndex != -1) {
                        int hEndIndex = copyright.indexOf("</h");
                        if (hEndIndex < hIndex) {
                            throw new RuntimeException(
                                    "Generating copyrights: " + "hEndIndex "
                                            + hEndIndex + " < " + hIndex + " "
                                            + foundCopyright + " copyright:\n"
                                            + copyright);
                        }
                        String header = copyright.substring(hIndex, hEndIndex);
                        String newHeader = header;
                        if (header.indexOf("<a name") == -1) {
                            // Insert a name tag that is the name of the package.

                            // If the copyright file name contains a -,then the name of the package
                            // is what is before the -.  If the name does not have a -, then
                            // the package name is the directory.
                            String packageName = "unknownPackage";
                            File copyrightFile = new File(copyrightURL);
                            String copyrightFileName = copyrightFile.getName();
                            int hyphenIndex = 0;
                            if ((hyphenIndex = copyrightFileName.indexOf("-")) > 0) {
                                packageName = copyrightFileName.substring(0,
                                        hyphenIndex);
                            } else {
                                packageName = copyrightFile.getParent();
                            }
                            newHeader = "<a name=\"" + packageName + "\">"
                                    + header;
                            copyright = copyright.replace(header, newHeader);
                        }

                        int nameIndex = 0;
                        if ((nameIndex = newHeader.indexOf("<a name=\"")) != -1) {
                            int labelIndex = 0;
                            if ((labelIndex = newHeader.indexOf("\">")) != -1) {
                                String target = newHeader.substring(nameIndex
                                        + "<a name=\"".length(), labelIndex);
                                // Skip the <h1> or <h2>
                                String label = newHeader
                                        .substring(labelIndex + 6);

                                //_guessCopyright(label, copyright);

                                masterCopyrightTable
                                .append(_generateLicenseTableRow(
                                        target, label));
                            }
                        }
                    }
                    masterCopyrightBuffer.append(copyright);
                } catch (IOException ex) {
                    // Ignore this, we want to print the copyrights no matter what.
                    System.out.println("Could not read " + foundCopyright
                            + ": " + ex);
                }
            }

            htmlBuffer.append("</table>\n</p>");
        }

        String tableTarget = "<!-- Table Contents Goes Here -->";
        int tableTargetIndex = -1;
        if ((tableTargetIndex = masterCopyrightBuffer.indexOf(tableTarget)) == -1) {
            System.err
            .println("GenerateCopyrights: Could not find \""
                    + tableTarget
                    + "\" in the generated copyright text, "
                    + "maybe ptolemy/actor/gui/masterCopyrightStart.htm.in does not have it?");
        } else {
            masterCopyrightBuffer
            .insert(tableTargetIndex, masterCopyrightTable);
        }
        try {
            URL masterCopyrightURL = HTMLAbout
                    ._temporaryHTMLFile("mastercopyright", ".htm",
                            masterCopyrightBuffer.toString());
            htmlBuffer.append("<p>For the complete copyrights in one file\n"
                    + "See the <a href=\"" + masterCopyrightURL
                    + "\">master copyright</a>.</p>\n");
        } catch (IOException ex) {
            // Ignore this, we want to print the copyrights.
            System.err
            .println("Could not write a temporary file with the complete copyrights: "
                    + ex);
        }

        htmlBuffer.append("<p>Other information <a href=\"about:\">about</a>\n"
                + "this configuration.\n" + "</body>\n</html>");

        return htmlBuffer.toString();
    }

    /** Generate the primary copyright.  Include a link to the other
     *  copyrights.
     *  @param configuration The configuration in which to look for the
     *  _applicationName attribute.
     *  @return A String containing HTML that describes what
     *  copyrights are used by Entities in the configuration
     */
    public static String generatePrimaryCopyrightHTML(
            Configuration configuration) {

        String defaultApplicationCopyright = "ptolemy/configs/doc/copyright.htm";
        String applicationCopyright = defaultApplicationCopyright;

        try {
            StringAttribute applicationCopyrightAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationCopyright",
                            StringAttribute.class);

            if (applicationCopyrightAttribute != null) {
                applicationCopyright = applicationCopyrightAttribute
                        .getExpression();
            }
        } catch (Exception ex) {
            // Ignore and use the default applicationCopyright
            applicationCopyright = defaultApplicationCopyright;
        }

        String applicationName = _getApplicationName(configuration);
        String applicationCopyrightURL = _findURL(applicationCopyright);

        String aelfredCopyright = _findURL("com/microstar/xml/aelfred-license.htm");
        String graphCopyright = _findURL("ptolemy/graph/graph-license.htm");

        String defaultCSS = _findURL("doc/default.css");
        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append("<html>\n<head>\n<title>Copyrights</title>\n"
                + "<link href=\"" + defaultCSS + "\" rel=\"stylesheet\""
                + "type=\"text/css\">\n" + "</head>\n<body>\n" + "<h1>"
                + applicationName + "</h1>\n"
                + "The primary copyright for the " + applicationName
                + " System can be\n" + "found in <a href=\""
                + applicationCopyrightURL + "\"><code>"
                + _canonicalizeURLToPTII(applicationCopyrightURL)
                + "</code></a>.\n"
                + "This configuration includes code that uses packages\n"
                + "with the following copyrights.\n");

        if (!applicationCopyright.equals(defaultApplicationCopyright)) {
            // If the Ptolemy II copyright is not the main copyright, add it.
            String ptolemyIICopyright = _findURL(defaultApplicationCopyright);
            htmlBuffer.append("<p>" + applicationName + " uses Ptolemy II "
                    + VersionAttribute.CURRENT_VERSION.getExpression() + ".\n"
                    + "PtolemyII is covered by the copyright in\n "
                    + "<a href=\"" + ptolemyIICopyright + "\"><code>"
                    + _canonicalizeURLToPTII(ptolemyIICopyright)
                    + "</code></a>\n");
        }

        htmlBuffer
        .append("<p>"
                + applicationName
                + " uses AElfred as an XML Parser.\n"
                + "AElfred is covered by the copyright in\n "
                + "<a href=\""
                + aelfredCopyright
                + "\"><code>."
                + _canonicalizeURLToPTII(aelfredCopyright)
                + "</code></a>\n</p>"
                + "<p>"
                + applicationName
                + " uses the ptolemy.graph package for scheduling and analysis of Ptolemy II models."
                + "Significant portions of the ptolemy.graph package were developed by "
                + "<a href=\"http://www.ece.umd.edu/~ssb/#in_browser\">Professor Shuvra S. Bhattacharyya</a> "
                + "and his group. and are covered by a BSD License in\n "
                + "<a href=\"" + graphCopyright + "\"><code>"
                + _canonicalizeURLToPTII(graphCopyright)
                + "</code></a>\n</p>");

        return htmlBuffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* If a className is can be found, then add the className
     * and copyrightPath to copyrightsMap
     * @param The map of copyrights.
     * @param applicationClassName The fully qualified, dot separated
     * class name of an application class, such as a Ptolemy actor
     * that uses (either by import or by reflection) the libraryClassName.
     * @param libraryClassName The fully qualified, dot separated
     * class name of the copyrighted or licensed library.
     * @param copyrightPath The path or URL to the copyright for
     * the library
     */
    private static void _addIfPresent(Map<String, Set<String>> copyrightsMap,
            String applicationClassName, String libraryClassName,
            String copyrightPath) {
        // If actor.lib.database.DatabaseManager is present, then
        // we look for MySQL JDBC packages.
        try {
            Class.forName(applicationClassName);
            _addIfPresent(copyrightsMap, libraryClassName, copyrightPath);
        } catch (Throwable throwable) {
            // Ignore, this just means that the applicationClassName
            // could not be found, so we need not include information
            // about the copyright.
        }
    }

    /* If a className is can be found, then add the className
     * and copyrightPath to copyrightsMap
     * @param The map of copyrights.
     * @param className The fully qualified, dot separated
     * class name of the copyrighted or licensed library.
     * @param copyrightPath The path or URL to the copyright for
     * the library.
     */
    private static void _addIfPresent(Map<String, Set<String>> copyrightsMap,
            String className, String copyrightPath) {

        boolean addIt = false;

        try {
            Class.forName(className);
            addIt = true;
        } catch (Throwable throwable) {
            // Usually, ignore, this just means that the classname could
            // not be found, so we need not include information
            // about the copyright.

            // However, under MacOSX, include any backtracking links
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS") && copyrightPath.contains("backtrack")) {
                addIt = true;
            }
        }

        if (addIt) {
            try {
                Set<String> entitiesSet = copyrightsMap.get(copyrightPath);

                if (entitiesSet == null) {
                    // This is the first time we've seen this copyright,
                    // add a key/value pair to copyrights, where the key
                    // is the URL of the copyright and the value is Set
                    // of entities that correspond with that copyright.
                    entitiesSet = new HashSet<String>();

                    entitiesSet.add(className);
                    copyrightsMap.put(copyrightPath, entitiesSet);
                } else {
                    // Other classes are using this copyright, so add this
                    // one to the list.
                    entitiesSet.add(className);
                }
            } catch (Throwable throwable) {
                // Ignore errors, it must means we 
            }
        }
    }

    // Truncate a jarURL so that the very long jar:file:...! is
    // converted to $PTII.  If the string does not start with jar:file
    // or if it startes with jar:file but does not contain a !, then
    // it is returned unchanged.  This method is used to truncate
    // the very long paths that we might see under Web Start.
    private static String _canonicalizeURLToPTII(String path) {
        if (!path.startsWith("jar:file")) {
            return path;
        } else {
            int index = path.lastIndexOf("!");

            if (index == -1) {
                return path;
            } else {
                return "$PTII" + path.substring(index + 1, path.length());
            }
        }
    }

    // Look for the localURL, and if we cannot find it, refer
    // to the url on the website that corresponds with this version of
    // Ptolemy II
    private static String _findURL(String localURL) {
        try {
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource(localURL);
            return url.toString();
        } catch (Exception ex) {
            // Ignore it and use the copyright from the website
            // Substitute in the first two tuples of the version
            // If the version is 3.0-beta-2, we end up with 3.0
            StringBuffer majorVersionBuffer = new StringBuffer();

            Iterator tuples = VersionAttribute.CURRENT_VERSION.iterator();

            // Get the first two tuples and separate them with a dot.
            if (tuples.hasNext()) {
                majorVersionBuffer.append((String) tuples.next());

                if (tuples.hasNext()) {
                    majorVersionBuffer.append(".");
                    majorVersionBuffer.append((String) tuples.next());
                }
            }

            String majorVersion = majorVersionBuffer.toString();
            return "http://ptolemy.eecs.berkeley.edu/ptolemyII/" + "ptII"
            + majorVersion + "/ptII" + majorVersion + "/" + localURL;
        }
    }

    /** Generate a row for the master table.
     *  @param target The HTML target for the license
     *  @param label The label for the license.
     *  @return HTML for the row that represents the table.
     */
    private static String _generateLicenseTableRow(String target, String label) {
        StringBuffer results = new StringBuffer(" <tr>\n" + "  <td>\n"
                + "     <a href=\"#" + target + "\">"
                + label.replace("License for", "").replace("Copyright for", "")
                + "</a>\n" + "  </td>\n");
        int rowIndex = -1;
        for (int i = 0; i < _licenses.length; i++) {
            if (_licenses[i][0].equals(target)) {
                rowIndex = i;
                break;
            }
        }
        if (rowIndex == -1) {
            results.append("  <td>?</td> <td>?</td> <td>?</td> <td>Update ptolemy/actor/gui/GenerateCopyrights.java for \""
                    + target + "\".</td>");
        } else {
            results.append("  <td>" + _licenses[rowIndex][1] + "</td>\n"
                    + "  <td>" + _licenses[rowIndex][2] + "</td>\n" + "  <td>"
                    + _licenses[rowIndex][3] + "</td>\n" + "  <td>"
                    + _licenses[rowIndex][4] + "</td>\n" + "  <td>"
                    + _licenses[rowIndex][5] + "</td>\n");
        }
        results.append("</tr>\n");
        return results.toString();
    }

    /** Get the application name from the _applicationName parameter.
     *  If the _applicationName parameter is not set, then return
     *  "Ptolemy II"
     */
    private static String _getApplicationName(Configuration configuration) {
        // Now generate the HTML
        String applicationName = "Ptolemy II";

        try {
            StringAttribute applicationNameAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationName", StringAttribute.class);

            if (applicationNameAttribute != null) {
                applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Exception ex) {
            // Ignore and use the default applicationName
        }
        return applicationName;
    }

    //     /** Guess the type of copyright, used for priming the table. */
    //     protected void _guessCopyright(String label, String copyright) {
    //         StringBuffer type = new StringBuffer();
    //         String [] types = {"Apache License", "CDDL", "Eclipse", "GNU Lesser General Public", "Oracle", "Research in Motion", "Sun" };
    //         for (int i = 0; i < types.length; i++) {
    //             if (copyright.indexOf(types[i]) != -1) {
    //                 if (type.length() > 0) {
    //                     type.append(" + ");
    //                 }
    //                 type.append(types[i]);
    //             }
    //         }
    //         System.out.println("{\"" + target + "\", \" \", \" \", \"Y\", \""
    //                 + type + "\"},");
    //     }

    /** Compare two filenames.
     */
    static class FileNameComparator implements Comparator<String> {

        /** Compare to Strings that should represent files by the name of the file.
         *  @param a The first file name.
         *  @param b The second file name.
         *  @return -1, 0 or 1
         */
        @Override
        public int compare(String a, String b) {
            File fileA = new File(a);
            File fileB = new File(b);
            return fileA.getName().compareTo(fileB.getName());
        }
    }

    /** A table of copyright labels, whether it is in Ptiny, the
     * Windows JRE, included in sources, included in Kepler and a
     * summary of the license(s).
     * We use a simple table here for ease of maintenance.
     */
    private static String[][] _licenses = {
        // Name, Ptiny, Windows JRE, Included in Sources, Included in Kepler, Summary
        { "aelfred", "Y", " ", "Y", "Y", "Include Microstar's copyrigh" },
        { "Audio", "Y", " ", "Y", "Y ", "Include credit text" },
        { "BrowserLauncher", "Y", " ", "Y", "Y",
        "Include the BrowserLauncher copyright" },
        { "ExtensionFileFilter", "Y", " ", "Y", "Y",
        "Include Oracle's copyright" },
        { "ExtensionFilenameFilter", "Y", " ", "Y", "Y", "Oracle" },
        { "JUnitParams", " ", " ", "Y", "Y", "Apache License" },
        { "javamail", " ", " ", "Y", " ", "CDDL + GPLv2 with Classpath exception" },
        { "PDFRenderer", " ", " ", "Y", "Y", "GNU Lesser General Public" },
        { "ResizableImageWidget", " ", " ", "Y", " ", "CDDL + Oracle + Oracle" },
        { "chic", " ", " ", "Y", "Y", "BSD" },
        { "colt", "Y", " ", "Y", "Y", "BSD and others" },
        { "cup", " ", " ", "Y", " ", "Similar to BSD" },
        { "db", " ", " ", "Y", " ",
        "Similar to <font color=\"red\">like GPL</font>" }, // GPL!!
        { "fmipp", " ", " ", "Y", " ", "FMUSDK: Qtronic: Similar to BSD" },
        { "fmusdk", " ", " ", "Y", " ", " QTronic: Similar to BSD" },
        { "g4ltl", " ", " ", "Y", "Y", "Apache License" },
        { "gcj", " ", " ", "Y", " ", "GPL with libgcc Exception" }, // Backtracking.
        { "guava", " ", " ", "Y", "Y", "Apache License" },
        { "hazelcast", " ", " ", "Y", " ", "Apache License" },
        { "itextpdf", " ", " ", "Y", " ",
        "Affero General Public License <font color=\"red\">like GPL</font>" },
        { "jackson", " ", " ", "Y", " ", "Apache License or LGPL" },
        { "jai", " ", "Y", " ", " ", "Oracle" },
        { "java3d", " ", "Y", "", " ", "Oracle" },
        { "javascript", " ", " ", "Y", "Y", "JQuery and Fancybox: MIT" },
        { "javax.servlet", " ", " ", "Y", "Y", "Apache License" },
        { "jcerti", " ", " ", "Y", "Y", "GNU Lesser General Public" },
        { "jcobyla", " ", " ", "Y", "", "MIT" },
        { "jdom", " ", " ", "Y", "",
        "Similar to BSD.  No use of sponsor name in advertising" },
        { "jetty", " ", " ", "Y", "Y", "Apache License + Eclipse" },
        { "jgoodies", " ", " ", "Y", "Y", "3 Clause BSD" },
        { "jimblacklerUtils", " ", " ", "Y", "Y", "Public Domain" },
        { "jmf", " ", "Y", " ", " ", "Oracle" },
        { "jna", " ", " ", "Y", "Y",
        "Apache License + GNU Lesser General Public" },
        { "joystick", " ", " ", "Y", " ", "Artistic License" },
        { "js", " ", " ", "Y", " ", "Mozilla + Oracle" },
        { "json", " ", " ", "Y", "Y", "BSD-like" },
        { "jsoup", " ", " ", "Y", "Y", "MIT" },
        { "junit", " ", " ", "Y", "Y", "Common Public License - v 1.0" },
        { "jxl", " ", " ", "Y", " ", "GNU Lesser General Public" },
        { "jython", "Y", " ", "Y", "Y",
        "Apache License + Python V2 + other licenses" },
        { "kieler", "Y", " ", "Y", "Y", "Eclipse" },
        { "mapss", " ", " ", "Y", "Y", "BSD" },
        { "matlab", "Y", " ", "Y", "Y", "Research in Motion BSD" },
        { "mlc", " ", " ", "Y", "Y", "GNU Lesser General Public + Oracle" },
        { "mysql", " ", " ", "Y", " ", "GPL + exceptions or Commercial" },
        { "netbeans", " ", " ", "Y", "Y", "CDDL + Oracle" },
        { "opencv", " ", " ", "Y", " ", "" },
        { "org.apache.oltu.oauth2", " ", " ", " ", "Y ", "Apache" },
        { "oscP5", " ", " ", " ", "Y ", "GNU Lesser General Public License 3.0" },
        { "protobuf", " ", " ", "Y", "Y", "BSD 3-Clause" },
        { "ptalon", "Y", " ", "Y", " ", "Antler: Public Domain" },
        { "ptjacl", " ", " ", "Y", "Y", "Apache License + BSD + Oracle" },
        { "quicktime", " ", " ", " ", " ", "Apple" }, // License prohibits distribution
        { "rxtx", " ", "Y", " ", " ", "GNU Lesser General Public 2.1 + Oracle" },
        { "saxon", " ", " ", "Y", " ", "Mozilla Public License" },
        { "smack", " ", " ", "Y", "Y", "Apache License" },
        { "socketio", " ", " ", "Y", "Y", "BSD" },
        { "soot", " ", " ", "Y", "Y", "BSD + LGPL 2" },
        { "thalesSingleWindow", " ", " ", "Y", " ", "BSD" },
        { "udunits", " ", " ", "Y", " ",
          "Similar to BSD.  No use of sponsor name in advertising" }, 
        { "vertx", " ", " ", "Y", " ", "Apache License" }
    };

}
