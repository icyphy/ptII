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

    /** Generate HTML for the Entities in a configuration. 
     *  @param configurationPath The configuration, for example
     *  "ptolemy/configs/full/configuration.xml"
     *  @return A String containing HTML that describes what
     *  copyrights are used by Entities in the configuration
     */
    public static String generateHTML(String configurationPath)
            throws Exception {
        CompositeEntity configurationEntity =
            _expandConfiguration(configurationPath);
        
        // A set of copyrights, where the key is a URL naming
        // the copyright and the value is a List of entities
        // that use that as a copyright
        Map copyrightsMap = new HashMap();

        List entityList = configurationEntity.deepEntityList();
        Iterator entities = entityList.iterator();
        while (entities.hasNext()) {
            Object object = entities.next();

            if (!(object instanceof NamedObj)) {
                System.out.println("Not a NamedObj: " + object);
                continue;
            }
            //System.out.println("Is a NamedObj: " + object);
            NamedObj namedObj = (NamedObj)object; 
            FileAttribute copyrightAttribute
                = (FileAttribute) namedObj.getAttribute("_copyright");
            if (copyrightAttribute != null) {
                URL copyrightURL = copyrightAttribute.asURL();
                Set entitiesSet = (Set) copyrightsMap.get(copyrightURL);
                if (entitiesSet == null) {
                    // This is the first time we've seen this copyright,
                    // add a key/value pair to copyrights, where the key
                    // is the URL of the copyright and the value is Set
                    // of entities that correspond with that copyright.

                    entitiesSet = new HashSet();

                    entitiesSet.add(namedObj.getMoMLInfo().className);
                    copyrightsMap.put(copyrightURL, entitiesSet);
                } else {
                    // Add the full name of the entity to the set of entities
                    // that correspond with this copyright
                    entitiesSet.add(namedObj.getFullName());
                }
            }
        }

        // Ok, now generate HTML
        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append("<html>\n<head>\n<title>Copyrights</title>\n"
                + "</head>\n<body>\n<dl>\n");
        Iterator copyrights = copyrightsMap.entrySet().iterator();
        while (copyrights.hasNext()) {
            Map.Entry entry = (Map.Entry)copyrights.next();
            URL copyrightURL = (URL)entry.getKey();
            Set entitiesSet = (Set)entry.getValue();

            StringBuffer entityBuffer = new StringBuffer();
            entities = entitiesSet.iterator();
            while (entities.hasNext()) {
                if (entityBuffer.length() > 0) {
                    entityBuffer.append(", ");
                }
                String entityClassName = (String)entities.next();

                // If we have javadoc, link to it.
                String entityHTML = entityClassName;

                // Assuming that entityClassName contains a dot separated
                // classpath here.
                String docName = "doc.codeDoc." + entityBuffer;
                try {
                    // This works in Web Start, see
                    // http://java.sun.com/products/javawebstart/faq.html#54
                    URL toRead = Thread.currentThread()
                        .getContextClassLoader().getResource(
                                docName.replace('.', '/') + ".html");
                    entityHTML = "<a href=\"" + toRead.toString() 
                        + "\">" + entityHTML + "</a>";
                } catch (Exception ex) {
                    // Ignore, we could not find the documentation.
                }
                entityBuffer.append(entityHTML);
            }


            htmlBuffer.append("<dt>" + entityBuffer
                    + "\n<dd> <a href=\"" + copyrightURL + "\"><code>"
                    + copyrightURL + "</code></a>\n");
        }
        htmlBuffer.append("</dl>\n</body>\n</html>");
        return htmlBuffer.toString();
    }

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            String configurationPath
                = "ptolemy/configs/full/configuration.xml";
            if (args.length == 1) {
                configurationPath = args[0];
            }
            System.out.println(GenerateCopyrights
                    .generateHTML(configurationPath));
        } catch (Exception ex) {
            System.out.println("Command failed:" +  ex);
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Parse a configuration and return a CompositeEntity. 
     *  @param configurationPath The configuration, for example
     *  "ptolemy/configs/full/configuration.xml"
     *  @return A CompositeEntity that contains the configuration.
     */
    // FIXME: should be private
    public static CompositeEntity _expandConfiguration(String
            configurationPath) throws Exception {
        MoMLParser parser = new MoMLParser();
        // FIXME: save old filters?
        parser.setMoMLFilters(null);
        parser.addMoMLFilters(BackwardCompatibility.allFilters());
        List inputFileNamesToSkip = new LinkedList();
        inputFileNamesToSkip.add("/apps/apps.xml");
        inputFileNamesToSkip.add("/charon/charon.xml");
        inputFileNamesToSkip.add("/experimentalDirectors.xml");
        inputFileNamesToSkip.add("/io/comm/comm.xml");

        inputFileNamesToSkip.add("/lib/interactive.xml");
        inputFileNamesToSkip.add("/jai/jai.xml");
        inputFileNamesToSkip.add("/jmf/jmf.xml");
        inputFileNamesToSkip.add("/joystick/jstick.xml");
        inputFileNamesToSkip.add("/jxta/jxta.xml");
        inputFileNamesToSkip.add("/matlab.xml");
        
        RemoveGraphicalClasses filter = new RemoveGraphicalClasses();
        filter.put("caltrop.ptolemy.actors.CalInterpreter", null);
        parser.addMoMLFilter(filter);

        // FIXME: save old value?
        parser.inputFileNamesToSkip = inputFileNamesToSkip;

        ClassLoader loader = parser.getClass().getClassLoader();
        URL configurationURL = loader.getResource(configurationPath);
        return
            (CompositeEntity)parser.parse(configurationURL, configurationURL);
    }

}
