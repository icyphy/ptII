/* Utilities for Querys

Copyright (c) 2004-2005 The Regents of the University of California.
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

import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

import java.awt.Frame;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// QueryUtilities

/**
   This class contains utility methods for Ptolemy Query classes
   that access the configuration.

   @author Christopher Brooks
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class QueryUtilities {
    /** Instances of this class cannot be created.
     */
    private QueryUtilities() {
    }

    /** Open a HTML resource in the current configuration if possible.
     *  @param urlName A string naming the url of the file to be opened
     *  as a resource.  For example "doc/expressions.htm".
     *  @param owner The frame that owns the HTMLViewer to be created.
     */
    public static void openHTMLResource(String urlName, Frame owner) {
        // Note: This method is necessary so that we avoid some code
        // duplication on classes that extend Query that want to use
        // the configuration to open up a help file.  ptolemy.gui.Query
        // knows nothing about configuration and PtolemyQuery is
        // misnamed, it is really a ParameterQuery.  We could make
        // this class extend Query and have other classes extend it
        try {
            // Note: call Thread.currentThread() so this works in Web Start
            URL doc = Thread.currentThread().getContextClassLoader()
                .getResource(urlName);

            // Try to use the configuration, if we can.
            boolean success = false;

            if (owner instanceof TableauFrame) {
                Configuration configuration = ((TableauFrame) owner)
                    .getConfiguration();

                if (configuration != null) {
                    configuration.openModel(null, doc, doc.toExternalForm());
                    success = true;
                }
            }

            if (!success) {
                // Just open an HTML page.
                HTMLViewer viewer = new HTMLViewer();
                viewer.setPage(doc);
                viewer.pack();
                viewer.show();
            }
        } catch (Exception ex) {
            try {
                MessageHandler.warning("Cannot open '" + urlName + "'", ex);
            } catch (CancelException exception) {
                // Ignore the cancel.
            }
        }
    }
}
