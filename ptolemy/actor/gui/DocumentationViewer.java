/* A viewer for documentation of Ptolemy classes.

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// FIXME: Trim these
// Ptolemy imports.
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

// Java imports.
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.*;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// DocumentationViewer
/**
This class is a viewer for documentation of ptolemy classes.  It assumes
that class documentation for all classes has been created in the
<code>doc/codeDoc</code> directory relative to some point in the classpath.
In other words, if the Object is an instance of 
<code>ptolemy.gui.DocumentationViewer</code, then this class will attempt 
to load the resource 
<code>doc.codeDoc.ptolemy.gui.DocumentationViewer.html</code>.
To automatically create documentation for the ptolemy tree in this directory,
run make in the doc directory.

@deprecated use DocumentationViewerTableau instead.
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class DocumentationViewer extends HTMLViewer {

    /** Construct a viewer for the documentation of the class of 
     *  the specified object.  If the documentation cannot be found, then
     *  display an error message.
     */
    public DocumentationViewer(Object object) {
        super();
	String className = object.getClass().getName();
	String docName = "doc.codeDoc." + className;
	URL docURL = getClass().getClassLoader().getResource(
            docName.replace('.', '/') + ".html");
	try {
	    setPage(docURL);
	} catch (IOException ex) {
            try {
                MessageHandler.warning(
                        "Could not find any documentation for\n" + className);
            } catch (CancelException exception) {}
	}
    }
}
