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

import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.MoMLParser;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// HTMLAbout
/**
This class contains static methods that are called
by when HTMLViewer.hyperlinkUpdate() is invoked on a hyperlink
that starts with <code>about:</code>.

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

    /** Parse a configuration and return a CompositeEntity. 
     *  This method is primarily used for testing.
     *  <p>For example, if vergil -ptiny was called, then the configuration
     *  at <code>ptolemy/configs/ptiny/configuration.xml</code>
     *  will have been used by {@link ptolemy.vergil.VergilApplication}.
     *  If the Vergil {@link HTMLViewer} is pointed at a link
     *  <a href="about:expandConfiguration#full"><code>about:expandConfiguration#full</code></a>,
     *  then the configuration will be expanded, which is a good way to test
     *  that all of the classes in a configuration are present.
     *  Note that the <code>about:expandConfiguration</code> link will
     *  only work inside Ptolemy II, browsers such as IE or Mozilla will
     *  not understand it.
     *
     *  @param configurationSubdirectory The subdirectory (if any) of
     *  ptolemy/configs where the configuration will be found.
     *  @return A CompositeEntity that contains the configuration.
     *  @exception Exception If there is a problem opening the configuration.
     *  @see ptolemy.actor.gui.HTMLViewer
     */
    public static String expandConfiguration(String 
            configurationSubdirectory) throws Exception {

        // This method is not in HTMLViewer because HTMLViewer
        // does not import anything outside of ptolemy.gui

        MoMLParser parser = new MoMLParser();
        ClassLoader loader = parser.getClass().getClassLoader();
        // Search for / or . so as to avoid arbitrary expansion outside
        // of ptolemy/configs
        if (configurationSubdirectory.indexOf("/") != -1) {
            throw new Exception("configurationSubdirectory must not "
                    + "contain a '/', it was: '"
                    + configurationSubdirectory + "'");

        }

        if (configurationSubdirectory.indexOf(".") != -1) {
            throw new Exception("configurationSubdirectory must not "
                    + "contain a '.', it was: '"
                    + configurationSubdirectory + "'");

        }

        String configurationResource = "ptolemy/configs/"
            + configurationSubdirectory + "/configuration.xml";
        URL configurationURL =
            loader.getResource(configurationResource);

        if (configurationURL == null) {
            throw new FileNotFoundException("Could not find '"
                    + configurationResource + "' ");
        }
        CompositeEntity configuration =
            (CompositeEntity)parser.parse(configurationURL,
                    configurationURL);
        return configuration.exportMoML();
    }
}
