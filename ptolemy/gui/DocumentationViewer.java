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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.gui;

// Ptolemy imports.
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

// Java imports.
import diva.canvas.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
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

@author Steve Neuendorffer
@version $Id$
*/

public class DocumentationViewer extends JPanel implements Printable {

    /** Construct a viewer for the documentation of the class of 
     *  the specified object.  If the documentation cannot be found, then
     *  the viewer will contain an error message instead of the appropriate
     *  documentation.
     */
    public DocumentationViewer(Object object) {
	String className = object.getClass().getName();
	String docName = "doc.codeDoc." + className;
	URL docURL = 
	    getClass().getClassLoader().getResource(docName.replace('.', '/') +
						    ".html");
	setLayout(new BorderLayout(0, 0));
	

	try {
	    _pane = new JEditorPane(docURL);
	    _pane.setEditable(false);
	    JScrollPane scroller = new JScrollPane(_pane);
	    scroller.setPreferredSize(new Dimension(800, 600));
	    add(scroller);	
	} catch (Exception ex) {
	    JTextArea area = 
		new JTextArea("Could not find any documentation for\n" + 
			      className);
	    area.setEditable(false);
	    add(area);	    
	}
    }

    /** Print the documentation to a printer.  The documentation will be 
     *  scaled to fit the width of the paper, growing to as many pages as
     *  is necessary.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @returns PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {

	if (_pane == null) {
            return Printable.NO_SUCH_PAGE;
        }
        //        graphics.translate((int)format.getImageableX(),
        //        (int)format.getImageableY());
        Dimension dimension = _pane.getSize();   
	
	// How much do we have to scale the width?
	double scale = format.getImageableWidth() / dimension.getWidth();
	double scaledHeight = dimension.getHeight() * scale;
	int lastPage = (int) (scaledHeight / format.getImageableHeight());

	// If we're off the end, then we're done.
	if(index > lastPage) {
            return Printable.NO_SUCH_PAGE;
        }
        AffineTransform at = new AffineTransform();
	at.translate((int)format.getImageableX(),
		     (int)format.getImageableY());
	at.translate(0, -(format.getImageableHeight() * index));
	at.scale(scale, scale);
	
        ((Graphics2D) graphics).transform(at);
	        
        _pane.paint(graphics);
        return Printable.PAGE_EXISTS;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    JEditorPane _pane = null;
}
