/* Generate a web page that contains links for the appropriate copyrights

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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.kernel.util.*;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// GenerateCopyrights
/**
Generate an HTML file that contains links to the appropriate
copyrights for entities in the configuration.  

<p>This class expands a Ptolemy II configuration and looks for
FileAttributes named _copyright.  

<pre>
        &lt;property name="_copyright" class="ptolemy.kernel.attributes.FileAttribute" value="$CLASSPATH/ptolemy/actor/lib/python/copyright.htm"&gt;
        &lt;/property&gt;
<pre>

If it finds such an attribute it makes a note of the value, which
should point to a file that contains the copyright information.

After all the entities are processed, an HTML file is generated that
maps the entity to the copyright


@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
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
     *  @return A String containing HTML that describes what
     *  copyrights are used by Entities in the configuration
     */
    public static String generateHTML() {
        // A Map of copyrights, where the key is a URL naming
        // the copyright and the value is a List of entities
        // that use that as a copyright.
        Map copyrightsMap = new HashMap();

        // Add the classnames and copyrights.

        // Alphabetical by className.
        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.joystick.Joystick",
                "ptolemy/actor/lib/joystick/copyright.htm");

        _addIfPresent(copyrightsMap,
                "ptolemy.actor.lib.python.PythonScript",
                "ptolemy/actor/lib/python/copyright.htm");


        _addIfPresent(copyrightsMap,
                "ptolemy.copernicus.kernel.KernelMain"
                "ptolemy/copernicus/kernel/soot-license.html");


        // Now generate the HTML

        String ptIICopyright = _findURL("ptolemy/configs/doc/copyright.htm");
        String aelfredCopyright = _findURL("com/microstar/xml/README.txt");

        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append("<html>\n<head>\n<title>Copyrights</title>\n"
                + "</head>\n<body>\n<dl>\n" 
                + "<h1>Ptolemy II Copyrights</h1>\n"
                + "The primary copyright for Ptolemy II can be found\n"
                + "in <a href=\"" + ptIICopyright + "\"><code>"
                + ptIICopyright + "</code></a>\n"
                + "<p>Ptolemy II uses AElfred as an XML Parser.\n"
                + "AElfred is covered by the copyright in\n "
                + "<a href=\"" + aelfredCopyright + "\"><code>"
                + aelfredCopyright + "</code></a>\n"
                + "<p>Various portions of Ptolemy II use code that falls\n"
                + "under other copyrights.\n"
                + "<p>Below, we list actors and their corresponding "
                + "copyright.\n"
                + "If an actor is not listed below, then the Ptolemy II\n "
                + "copyright is the primary copyright"
                + "<dl>\n");
                
        Iterator copyrights = copyrightsMap.entrySet().iterator();
        while (copyrights.hasNext()) {
            Map.Entry entry = (Map.Entry)copyrights.next();
            String copyrightURL = (String)entry.getKey();
            Set entitiesSet = (Set)entry.getValue();

            StringBuffer entityBuffer = new StringBuffer();
            Iterator entities = entitiesSet.iterator();
            while (entities.hasNext()) {
                if (entityBuffer.length() > 0) {
                    entityBuffer.append(", ");
                }
                String entityClassName = (String)entities.next();

                // If we have javadoc, link to it.
                // Assuming that entityClassName contains a dot separated
                // classpath here.
                String docName = "doc.codeDoc." + entityClassName;
                String codeDoc = _findURL(docName.replace('.', '/') + ".html");
                entityBuffer.append("<a href=\"" + codeDoc
                        + "\">" + entityClassName + "</a>");
            }

            String foundCopyright = _findURL(copyrightURL);

            htmlBuffer.append("<dt>" + entityBuffer
                    + "\n<dd> <a href=\"" + foundCopyright + "\"><code>"
                    + foundCopyright + "</code></a>\n");
        }
        htmlBuffer.append("</dl>\n</body>\n</html>");
        return htmlBuffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // If a className is can be found, then add the className
    // and copyrightPath to copyrightsMap 
    private static void _addIfPresent(Map copyrightsMap,
            String className, String copyrightPath) {
        try {
            Class.forName(className);
            Set entitiesSet = (Set) copyrightsMap.get(copyrightPath);
            if (entitiesSet == null) {
                // This is the first time we've seen this copyright,
                // add a key/value pair to copyrights, where the key
                // is the URL of the copyright and the value is Set
                // of entities that correspond with that copyright.

                entitiesSet = new HashSet();

                entitiesSet.add(className);
                copyrightsMap.put(copyrightPath, entitiesSet);
            } else {
                // Other classes are using this copyright, so add this
                // one to the list.
                entitiesSet.add(className);
            }
        } catch (Exception ex) {
            // Ignore, this just means that the classname could
            // not be found, so we need not include information
            // about the copyright.
            System.out.println("_addIfPresent: " + ex);
        }
    }

    // Look for the localURL, and if we cannot find it, refer
    // to the url on the website that corresponds with this version of
    // Ptolemy II
    private static String _findURL(String localURL) {
        try {
            URL url = Thread.currentThread()
                .getContextClassLoader().getResource(localURL);
            return url.toString();
        } catch (Exception ex) {
            // Ignore it and use the copyright from the website

            // Substitute in the first two tuples of the version
            // If the version is 3.0-beta-2, we end up with 3.0
            StringBuffer majorVersionBuffer = new StringBuffer();

            Iterator tuples = VersionAttribute.CURRENT_VERSION.iterator();

            // Get the first two tuples and separate them with a dot.
            if (tuples.hasNext()) {
                majorVersionBuffer.append((String)tuples.next());
                if (tuples.hasNext()) {
                    majorVersionBuffer.append(".");
                    majorVersionBuffer.append((String)tuples.next());
                }
            }
            String majorVersion = majorVersionBuffer.toString();
            return "http://ptolemy.eecs.berkeley.edu/ptolemyII/"
                + "ptII" + majorVersion + "/ptII"
                + majorVersion + "/" + localURL;
        }
    }
}
