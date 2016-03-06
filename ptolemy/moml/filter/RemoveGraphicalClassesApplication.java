/* Remove Graphical Classes Application.

 Copyright (c) 1998-2016 The Regents of the University of California.
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
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RemoveGraphicalClasses Application

/** An application that removes graphical classes.

 @author  Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class RemoveGraphicalClassesApplication {

    // The main() method was moved from RemoveGraphicaClasses so
    // that we could avoid a dependency on ActorModuleInitializer in
    // Triquetrum.

    /** Read in a MoML file, remove graphical classes and
     *  write the results to standard out.
     *  <p> For example, to remove the graphical classes from
     *  a file called <code>RemoveGraphicalClasses.xml</code>
     *  <pre>
     *  java -classpath "$PTII" ptolemy.moml.filter.RemoveGraphicalClassesApplication test/RemoveGraphicalClasses.xml &gt; output.xml
     *  </pre>
     *  @param args An array of one string
     *  <br> The name of the MoML file to be cleaned.
     *  @exception Exception If there is a problem reading or writing
     *  a file.
     */
    public static void main(String[] args) throws Exception {
        try {
            // The HandSimDroid work in $PTII/ptserver uses dependency
            // injection to determine which implementation actors such as
            // Const and Display to use.  This method reads the
            // ptolemy/actor/ActorModule.properties file.</p>
            ActorModuleInitializer.initializeInjector();

            MoMLParser parser = new MoMLParser();

            // The list of filters is static, so we reset it in case there
            // filters were already added.
            MoMLParser.setMoMLFilters(null);

            // Add the backward compatibility filters.
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());
            MoMLParser.addMoMLFilter(new HideAnnotationNames());
            NamedObj topLevel = parser.parseFile(args[0]);
            System.out.println(topLevel.exportMoML());
        } catch (Throwable throwable) {
            System.err.println("Failed to filter \"" + args[0] + "\"");
            throwable.printStackTrace();
            StringUtilities.exit(1);
        }
    }
}
